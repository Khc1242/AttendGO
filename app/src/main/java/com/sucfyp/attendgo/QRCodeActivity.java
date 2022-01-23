package com.sucfyp.attendgo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class QRCodeActivity extends AppCompatActivity {
    private static final String TAG = "QRCodeActivity";
    String code, eventID, eventName, userID;
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;
    private static int REQUEST_CODE = 100;
    OutputStream outputStream;
    Uri QRCodeUri;
    Button download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        getSupportActionBar().hide();

        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        userID = mAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        eventID = intent.getStringExtra("EVENT_ID");
        eventName = intent.getStringExtra("EVENT_NAME");
        code = intent.getStringExtra("QR_CODE");
        // TextView qrCodeTV = findViewById(R.id.QRCodeTV);
        // qrCodeTV.setText(code);

        Button createClosePage = findViewById(R.id.backBtn);
        createClosePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission is granted");
            //File write logic here

        } else {
            ActivityCompat.requestPermissions(QRCodeActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);
        }
        generateQRCode();
    }

    private void generateQRCode() {
        ImageView qrCode = findViewById(R.id.QRCodeOutputIV);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BarcodeEncoder encoder = new BarcodeEncoder();
        int width = 500;
        int height = 500;

        try {
            assert code != null;
            BitMatrix bitMatrix = qrCodeWriter.encode(code, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            qrCode.setImageBitmap(bitmap);
            qrCode.setVisibility(View.VISIBLE);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);

            String root = Environment.getExternalStorageDirectory().toString();

            File myDir = new File(root + "/AttendGO/" + eventName+"-"+eventID + "/QR_CODES/");
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            String filename = eventName +".png";
            File file = new File (myDir, filename);
            if (file.exists ())
                file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d(TAG, "onSuccess: QR Code is created successfully for " + eventName);
            Uri uri = Uri.fromFile(file);
            StorageReference qrCodeStorageRef = mStorageRef.child("Event").child(eventName+" - "+eventID).child("QR_CODE").child(filename);
            qrCodeStorageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(
                        UploadTask.TaskSnapshot taskSnapshot)
                {
                    showMessage("QR Code has been successfully saved in Phone Storage and uploaded to Cloud Storage!");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    showMessage("QR Code UploadTask Failed: " + e.getMessage());
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onFailure: " + e.toString());
            showMessage(e.toString());
        }
    }

    private void askPermission() { ActivityCompat.requestPermissions(QRCodeActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE); }

    private void removeQRCode() {
        //Determines document path for the corresponding module
        DocumentReference documentReference = fStore.collection("Event").document(eventID);

        //Sets QR Code field to null
        documentReference.update("QR Code", null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Code successfully removed");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Code not removed");
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}