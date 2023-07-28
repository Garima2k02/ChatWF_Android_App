package com.example.chatwfapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
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

public class ProfileActivity extends AppCompatActivity {


    private CircleImageView userProfileImage;
    private String current_Sate, senderUserID;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineRequestButton;
    private String recieverUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,ChatRequestRef,ContactsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        current_Sate="new";
        mAuth=FirebaseAuth.getInstance();
        ChatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");


        senderUserID=mAuth.getCurrentUser().getUid();
        recieverUserID = getIntent().getExtras().get("visit_user_id").toString();

        userProfileImage=(CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView) findViewById(R.id.visit_profile_status);
        sendMessageRequestButton=(Button) findViewById(R.id.send_message_request_button);
        declineRequestButton=(Button) findViewById(R.id.decline_message_request_button);

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
UserRef.child(recieverUserID).addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(snapshot.exists() && (snapshot.hasChild("image")))
        {
            //String UserImage = snapshot.child("image").getValue().toString();
            String userName = snapshot.child("name").getValue().toString();
            String userStatus = snapshot.child("status").getValue().toString();

            //Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
            userProfileName.setText(userName);
            userProfileStatus.setText(userStatus);

            ManageChatRequests();
        }

        else {
            String userName = snapshot.child("name").getValue().toString();
            String userStatus = snapshot.child("status").getValue().toString();

            userProfileName.setText(userName);
            userProfileStatus.setText(userStatus);

            ManageChatRequests();
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});
    }

    private void ManageChatRequests() {
        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild(recieverUserID))
                        {
                            String request_type = snapshot.child(recieverUserID).child("request_type").getValue().toString();
                            if(request_type.equals("sent"))
                            {
                                current_Sate="request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("recieved")){
                                current_Sate="request_recieved";
                                sendMessageRequestButton.setText("Accept Chat Request");
                                declineRequestButton.setVisibility(View.VISIBLE);
                                declineRequestButton.setEnabled(true);

                                declineRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }

                        else {
                            ContactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(recieverUserID))
                                    {
                                        current_Sate="friends";
                                        sendMessageRequestButton.setText("Remove");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        if(!senderUserID.equals(recieverUserID))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessageRequestButton.setEnabled(false);

                    if(current_Sate.equals("new"))
                    {
                        SendChatRequest();
                    }

                    if(current_Sate.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if(current_Sate.equals("request_recieved"))
                    {
                        AcceptChatRequest();
                    }
                    if(current_Sate.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }

                }
            });
        }
        else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserID).child(recieverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ContactsRef.child(recieverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                current_Sate="new";
                                                sendMessageRequestButton.setText("Send Message");

                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
ContactsRef.child(senderUserID).child(recieverUserID)
        .child("Contacts").setValue("Saved")
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ContactsRef.child(recieverUserID).child(senderUserID)
                            .child("Contacts").setValue("Saved")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        ChatRequestRef.child(senderUserID).child(recieverUserID)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            ChatRequestRef.child(recieverUserID).child(senderUserID)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            sendMessageRequestButton.setEnabled(true);
                                                                            current_Sate="friends";

                                                                            sendMessageRequestButton.setText("Remove");
                                                                            declineRequestButton.setVisibility(View.INVISIBLE);
                                                                            declineRequestButton.setEnabled(false);
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserID).child(recieverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ChatRequestRef.child(recieverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                current_Sate="new";
                                                sendMessageRequestButton.setText("Send Message");

                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(senderUserID).child(recieverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ChatRequestRef.child(recieverUserID).child(senderUserID)
                                    .child("request_type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                current_Sate="request_sent";
                                                sendMessageRequestButton.setText("Cancel Chat Request");

                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}