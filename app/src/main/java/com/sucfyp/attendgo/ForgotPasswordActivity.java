package com.sucfyp.attendgo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends Activity {
    private static final String TAG = "ForgotPasswordActivity";
    private FirebaseAuth mAuth;
    private Button resetBtn;
    private EditText resetLink;
    private ImageView closeIcon;
    private TextInputLayout emailAlert;
    private ProgressBar resetProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        resetLink = findViewById(R.id.emailLink);

        emailAlert = findViewById(R.id.resetLinkEmailAlert);
        emailAlert.setErrorEnabled(true);

        resetProgress = findViewById(R.id.resetLinkPB);
        resetProgress.setVisibility(View.INVISIBLE);

        closeIcon = findViewById(R.id.close);
        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        resetBtn = findViewById(R.id.sendLinkBtn);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = resetLink.getText().toString();

                if(TextUtils.isEmpty(email)){
                    emailAlert.setError(getString(R.string.plz_input_email));
                    return;
                }
                emailAlert.setError("");

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            showMessage("Reset Password Link has sent to your email!");
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