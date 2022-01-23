package com.sucfyp.attendgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    //Initialize variables
    private static final String TAG = "LoginActivity";
    private EditText signInEmail,signInPassword;
    private Button signInBtn;
    private ProgressBar signInProgress;
    private FirebaseAuth mAuth;
    private TextView registerTV;
    private TextView resetTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        registerTV = findViewById(R.id.newUserTV);
        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        resetTV = findViewById(R.id.forgotPasswordTV);
        resetTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
        signInEmail = findViewById(R.id.loginEmail);
        signInPassword = findViewById(R.id.loginPassword);
        signInProgress = findViewById(R.id.loginPB);
        signInProgress.setVisibility(View.INVISIBLE);
        signInBtn = findViewById(R.id.loginBtn);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInProgress.setVisibility(View.VISIBLE);
                signInBtn.setVisibility(View.INVISIBLE);

                final String mail = signInEmail.getText().toString();
                final String password = signInPassword.getText().toString();

                if (mail.isEmpty() || password.isEmpty()) {
                    showMessage("Please fill in all fields!");
                    signInBtn.setVisibility(View.VISIBLE);
                    signInProgress.setVisibility(View.INVISIBLE);
                }
                else
                {
                    signIn(mail,password);
                }
            }
        });
    }

    private void signIn(String mail, String password) {
        mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    signInProgress.setVisibility(View.INVISIBLE);
                    signInBtn.setVisibility(View.VISIBLE);
                    updateUI();
                }
                else {
                    showMessage(task.getException().getMessage());
                    signInBtn.setVisibility(View.VISIBLE);
                    signInProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void updateUI() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null) {
            //user is already connected  so we need to redirect him to home page
            updateUI();
        }
    }
}