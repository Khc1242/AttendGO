package com.sucfyp.attendgo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    //Initialize variables
    private static final String TAG = "RegisterActivity";
    ImageView userPhoto;
    static int PreReqCode = 1;
    static int REQUEST_CODE = 1;
    Uri profilePicUri;

    private TextInputEditText signUpEmail, signUpPassword, signUpName;
    private ProgressBar signUpProgress;
    private Button signUpBtn;
    FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private ImageView signUpClosePage;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        // Assign variables.
        signUpEmail = findViewById(R.id.registerEmail);
        signUpPassword = findViewById(R.id.registerPassword);
        signUpName = findViewById(R.id.registerName);
        signUpBtn = findViewById(R.id.registerBtn);

        signUpProgress = findViewById(R.id.registerPB);
        signUpProgress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        signUpClosePage = findViewById(R.id.registerCloseIV);
        signUpClosePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpBtn.setVisibility(View.INVISIBLE);
                signUpProgress.setVisibility(View.VISIBLE);
                final String email = signUpEmail.getText().toString();
                final String password = signUpPassword.getText().toString();
                final String name = signUpName.getText().toString();
                final String image = userPhoto.getDrawable().toString();

                if (image.isEmpty() || email.isEmpty() || name.isEmpty() || password.isEmpty()) {
                    // something goes wrong : all fields must be filled
                    // we need to display an error message
                    showMessage("Please fill in all fields!");
                    signUpBtn.setVisibility(View.VISIBLE);
                    signUpProgress.setVisibility(View.INVISIBLE);
                } else {
                    // everything is ok and all fields are filled now we can start creating user account
                    // CreateUserAccount method will try to create the user if the email is valid
                    CreateUserAccount(email, name, password);
                }
            }
        });

        userPhoto = findViewById(R.id.registerCIV);
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 27) {
                    checkAndReqForPermission();
                } else {
                    openGallery();
                }
            }
        });
    }

    private void CreateUserAccount(String email, String name, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // user account created successfully
                    showMessage("Account created");
                    userID = mAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fStore.collection("User").document(userID);
                    Map<String, Object> user = new HashMap<>();
                    user.put("Name", name);
                    user.put("Email", email);
                    user.put("Password", password);
                    user.put("Register Date", new Timestamp(new Date()));
                    user.put("UserID", userID);
                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: user Profile is created for " + userID);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: " + e.toString());
                        }
                    });
                    // After we creating user account we need to update his profile picture and name, check if user photo is picked or not
                            if (profilePicUri != null){
                                updateUserInfo(name, profilePicUri, mAuth.getCurrentUser());
                            }
                            else {
                                updateUserInfoWithoutPhoto(name,mAuth.getCurrentUser());
                            }
                        } else {
                            //Account creating failed.
                            showMessage("Account creation failed. " + task.getException().getMessage());
                            signUpBtn.setVisibility(View.VISIBLE);
                            signUpProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });

    }

    private void updateUserInfo(String name, Uri profilePicUri, FirebaseUser currentUser) {
        // first, need to upload user photo to firebase storage and get url
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("User/Profile Picture");
        final StorageReference imageFilePath = mStorage.child(profilePicUri.getLastPathSegment());
        imageFilePath.putFile(profilePicUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // image uploaded successfully, get the image url
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // uri contain user image url
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(name).setPhotoUri(uri).build();
                        currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // user info updated successfully
                                    showMessage("Done Registration!");
                                    updateUI();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    //Without image
    private void updateUserInfoWithoutPhoto(String name, FirebaseUser currentUser) {
        // first we need to upload user photo to firebase storage and get url
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        currentUser.updateProfile(profileUpdate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // user info updated successfully
                            showMessage("Register Complete");
                            updateUI();
                        }
                    }
                });
    }

    private void updateUI() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUEST_CODE);

    }

    private void checkAndReqForPermission() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessage("Please accept for required permission!");
            } else {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PreReqCode);
            }
        } else {
            openGallery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE && data != null) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            profilePicUri = data.getData();
            userPhoto.setImageURI(profilePicUri);
        }
    }

}