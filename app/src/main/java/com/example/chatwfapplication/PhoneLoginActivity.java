package com.example.chatwfapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputPhnNum, InputVerificationCode;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private ProgressDialog loadingbar;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth=FirebaseAuth.getInstance();
        SendVerificationCodeButton=(Button) findViewById(R.id.send_verification_code_button);
        VerifyButton=(Button) findViewById(R.id.verify_button);
        InputPhnNum=(EditText) findViewById(R.id.phone_number_input);
        InputVerificationCode=(EditText) findViewById(R.id.verification_code_input);

        loadingbar=new ProgressDialog(this);
        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber=InputPhnNum.getText().toString();
                if(TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Phone Number is Required", Toast.LENGTH_SHORT).show();
                }

                else {

                    loadingbar.setTitle("Phone Verification");
                    loadingbar.setMessage("Please Wait");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(PhoneLoginActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhnNum.setVisibility(View.INVISIBLE);

                String verificationCode=InputVerificationCode.getText().toString();
                if(TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter the verification code", Toast.LENGTH_SHORT).show();

                }
                else {
                    loadingbar.setTitle("Code Verification");
                    loadingbar.setMessage("Please Wait");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks =new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingbar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();

                InputPhnNum.setVisibility(View.VISIBLE);
                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {


                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "Code Sent", Toast.LENGTH_SHORT).show();

                loadingbar.dismiss();
                InputPhnNum.setVisibility(View.INVISIBLE);
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
                VerifyButton.setVisibility(View.VISIBLE);

            }
        };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingbar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        } else {
                            String msg=task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: "+msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent= new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}