package com.example.chatwfapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private Button LoginButton, PhoneLoginButton;
    private EditText UserEmail, UserPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private TextView NeedNewAccountLink, ForgetPasswordLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth=FirebaseAuth.getInstance();

        InitializeFields();


        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });

        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent PhnloginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(PhnloginIntent);

            }
        });

    }

    private void AllowUserToLogin() {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please Enter email", Toast.LENGTH_SHORT).show();
            UserEmail.setError("Please Enter your Email");
            UserEmail.requestFocus();
        }

        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please Enter password", Toast.LENGTH_SHORT).show();
            UserPassword.setError("Please Enter your Password");
            UserPassword.requestFocus();
        }

        else {
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please Wait");
            loadingBar.setCanceledOnTouchOutside(true);
            //if user touches on screen when loading bar is on screen then it will disappear
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "Logged In Successful", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                            else {
                                String msg = task.getException().toString();
                                Log.d("mytag","Error: "+msg);
                                Toast.makeText(LoginActivity.this, "Error: "+msg, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

        }
    }

    private void InitializeFields() {

        LoginButton = (Button) findViewById(R.id.login_button);
        PhoneLoginButton = (Button) findViewById(R.id.phone_login_button);
        UserEmail=(EditText) findViewById(R.id.login_email);
        UserPassword=(EditText) findViewById(R.id.login_password);
        NeedNewAccountLink=(TextView) findViewById(R.id.need_new_account_link);
        ForgetPasswordLink=(TextView) findViewById(R.id.forget_password_link);
        loadingBar=new ProgressDialog(this);

    }

    private void SendUserToMainActivity() {
        Intent mainIntent= new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent registerIntent= new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }
}