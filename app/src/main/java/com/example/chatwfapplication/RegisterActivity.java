package com.example.chatwfapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword,UserConfirmPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        InitializeFields();
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {

        Pattern specialCharPatten = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern UpperCasePatten = Pattern.compile("[A-Z ]");
        Pattern LowerCasePatten = Pattern.compile("[a-z ]");
        Pattern DigitCasePatten = Pattern.compile("[0-9 ]");

        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();
        String confirmpassword=UserConfirmPassword.getText().toString();


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

        else if(TextUtils.isEmpty(confirmpassword))
        {
            Toast.makeText(this, "Please Enter confirmed password", Toast.LENGTH_SHORT).show();
            UserConfirmPassword.setError("Please Enter your Confirmed Password");
            UserConfirmPassword.requestFocus();
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Please Enter a valid email", Toast.LENGTH_SHORT).show();
            UserEmail.setError("Please Enter your valid Email");
            UserEmail.requestFocus();
        }

        else if(password.length() <= 7)
        {
            Toast.makeText(this, "Password should have 8 characters or more", Toast.LENGTH_SHORT).show();
            UserPassword.setError("Password should have 8 characters or more");
            UserPassword.requestFocus();
        }

        else if(!specialCharPatten.matcher(password).find())
        {
            Toast.makeText(this, "Password should have atleast one special character", Toast.LENGTH_SHORT).show();
            UserPassword.setError("Password should have atleast one special character");
            UserPassword.requestFocus();
        }

        else if(!UpperCasePatten.matcher(password).find())
        {
            Toast.makeText(this, "Password should have atleast one uppercase character", Toast.LENGTH_SHORT).show();
            UserPassword.setError("Password should have atleast one uppercase character");
            UserPassword.requestFocus();
        }

        else if(!LowerCasePatten.matcher(password).find())
        {
            Toast.makeText(this, "Password should have atleast one lowercase character", Toast.LENGTH_SHORT).show();
            UserPassword.setError("Password should have atleast one lowercase character");
            UserPassword.requestFocus();
        }

        else if(!DigitCasePatten.matcher(password).find())
        {
            Toast.makeText(this, "Password should have atleast one digit", Toast.LENGTH_SHORT).show();
            UserPassword.setError("Password should have atleast one digit");
            UserPassword.requestFocus();
        }

        else if(!password.equals(confirmpassword))
        {
            Toast.makeText(this, "Please Enter the same password", Toast.LENGTH_SHORT).show();
            //clear the entered passwords
            UserPassword.clearComposingText();
            UserConfirmPassword.clearComposingText();
        }

        else {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait while we are creating new account for you");
            loadingBar.setCanceledOnTouchOutside(true);
            //if user touches on screen when loading bar is on screen then it will disappear
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                //store user unique id in firebase database
                                String currentUserID=mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currentUserID).setValue("");
                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String msg=task.getException().toString();
                                Log.d("myTag","error: "+msg);
                                Toast.makeText(RegisterActivity.this, "Error : "+msg, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }

    private void InitializeFields() {
        CreateAccountButton=(Button) findViewById(R.id.register_button);
        UserEmail=(EditText) findViewById(R.id.register_email);
        UserPassword=(EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink=(TextView) findViewById(R.id.already_have_an_account_link);
        loadingBar=new ProgressDialog(this);
        UserConfirmPassword=(EditText) findViewById(R.id.register_confirm_password);
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent= new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent= new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}