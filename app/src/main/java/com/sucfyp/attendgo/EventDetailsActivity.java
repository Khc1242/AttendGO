package com.sucfyp.attendgo;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EventDetailsActivity extends AppCompatActivity {
    private static final String TAG = "EventDetailsActivity";
    FirebaseStorage mStorage;
    StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        getSupportActionBar().hide();

        ImageView edCloseIV = findViewById(R.id.eventDetailsIV);
        edCloseIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        EventsModel evModel = (EventsModel) getIntent().getSerializableExtra("model");

        TextView eventName = findViewById(R.id.eventName);
        TextView eventID = findViewById(R.id.eventID);
        TextView eventOrganization = findViewById(R.id.eventOrganization);
        TextView hostName = findViewById(R.id.hostName);
        TextView hostEmail = findViewById(R.id.hostEmail);
        TextView description = findViewById(R.id.description);
        TextView eventDate = findViewById(R.id.eventDate);
        TextView eventTime = findViewById(R.id.eventTime);
        TextView eventAddress = findViewById(R.id.eventAddress);

        eventName.setText(evModel.getEventName());
        eventID.setText(evModel.getEventID());
        eventOrganization.setText(evModel.getEventOrganization());
        hostName.setText(evModel.getHostName());
        hostEmail.setText(evModel.getHostEmail());
        description.setText(evModel.getDescription());
        eventDate.setText(evModel.getEventDate());
        eventTime.setText(evModel.getEventTime());
        eventAddress.setText(evModel.getEventAddress());

        // mStorageRef.child("Event").child(eventName+"-"+eventID).child("QR_CODE").child(filename)
        Button downloadQRCode = findViewById(R.id.downloadQRCodeBtn);
        downloadQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String evName = evModel.getEventName();
                String evID = evModel.getEventID();
                mStorage = FirebaseStorage.getInstance();
                mStorageRef = mStorage.getReference();
                StorageReference qrCodeStorageRef = mStorageRef.child("Event").child(evName+" - "+evID).child("QR_CODE").child(evName+".png");
                qrCodeStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String url = uri.toString();
                        downloadFile(EventDetailsActivity.this,evName+"_EventQrCode.png",DIRECTORY_DOWNLOADS,url);
                        Log.d(TAG, "QR Code downloaded!");
                        showMessage("QR Code downloaded!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.toString());
                        showMessage("onFailure: " + e.toString());
                    }
                });
            }
        });

        Button checkAttendee = findViewById(R.id.checkAttendeeBtn);
        checkAttendee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String evID = evModel.getEventID();
                Intent intent = new Intent(EventDetailsActivity.this, AttendeeActivity.class);
                intent.putExtra("EVENT_ID", evID);
                startActivity(intent);
                finish();
            }
        });
    }

    public void downloadFile(Context context, String fileName, String destinationDirectory, String url) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request dmRequest = new DownloadManager.Request(uri);
        dmRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        dmRequest.setDestinationInExternalFilesDir(context,destinationDirectory,fileName);

        downloadManager.enqueue(dmRequest);
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}