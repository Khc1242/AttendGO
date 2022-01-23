package com.sucfyp.attendgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends AppCompatActivity {
    private static final String TAG = "ChangePasswordActivity";
    private TextInputEditText currentPassword, newPassword;
    private Button savePasswordBtn;
    private ImageView changePasswordClosePage;
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FirebaseUser fUser;
    String userID, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = mAuth.getCurrentUser();
        userEmail = fUser.getEmail();

        changePasswordClosePage = findViewById(R.id.changePasswordCloseIV);
        changePasswordClosePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        currentPassword = findViewById(R.id.currentPasswordInput);
        newPassword = findViewById(R.id.newPasswordInput);

        savePasswordBtn = findViewById(R.id.savePasswordBtn);
        savePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String currentPwd = currentPassword.getText().toString();
                final String newPwd = newPassword.getText().toString();

                if (currentPwd.isEmpty() || newPwd.isEmpty()) {
                    // something goes wrong : all fields must be filled
                    // we need to display an error message
                    showMessage("Please fill in all fields!");
                }

                AuthCredential credential = EmailAuthProvider.getCredential(userEmail, currentPwd);

                fUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    fUser.updatePassword(newPwd).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Password updated");
                                                showMessage("Password updated");

                                                userID = mAuth.getCurrentUser().getUid();
                                                DocumentReference documentReference = fStore.collection("User").document(userID);
                                                Map<String,Object> user = new HashMap<>();
                                                user.put("Password",newPwd);
                                                documentReference.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(ChangePasswordActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                        finish();
                                                    }
                                                });
                                            } else {
                                                Log.d(TAG, "Error: password not updated");
                                                showMessage("Error: password not updated");
                                            }
                                        }
                                    });
                                } else {
                                    Log.d(TAG, "Error: auth failed");
                                    showMessage("Error: auth failed");
                                }
                            }
                        });
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}