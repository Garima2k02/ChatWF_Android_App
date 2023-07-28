package com.example.chatwfapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private Toolbar SettingsToolBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth=FirebaseAuth.getInstance();
        currentUserID= mAuth.getCurrentUser().getUid();
        RootRef=FirebaseDatabase.getInstance().getReference();

        Initializefields();

        //userName.setVisibility(View.INVISIBLE);

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();


    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if((snapshot.exists())&& (snapshot.hasChild("name") && (snapshot.hasChild("image"))))
                        {
                            String retrieveUserName=snapshot.child("name").getValue().toString();
                            String retrieveStatus=snapshot.child("status").getValue().toString();
                            String retrieveProfileImage=snapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            //set profile image

                        }
                        else if((snapshot.exists())&& (snapshot.hasChild("name")))
                        {
                            String retrieveUserName=snapshot.child("name").getValue().toString();
                            String retrieveStatus=snapshot.child("status").getValue().toString();
                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                        }
                        else
                        {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please Set and Update your Profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void UpdateSettings() {
        String setUserName=userName.getText().toString();
        String setStatus=userStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please Write your User name", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please Write your Status", Toast.LENGTH_SHORT).show();
        }

        else
        {
            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();
                            }

                            else
                            {
                                String msg=task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "error: "+msg, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent= new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void Initializefields() {
        updateAccountSettings=(Button) findViewById(R.id.update_settings_button);
        userName=(EditText) findViewById(R.id.set_user_name);
        userStatus=(EditText) findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView) findViewById(R.id.set_profile_image);
        SettingsToolBar=(Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }
}