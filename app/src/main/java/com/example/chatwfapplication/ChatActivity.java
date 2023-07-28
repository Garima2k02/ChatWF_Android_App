package com.example.chatwfapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
private TextView userName, userLastSeen;
private CircleImageView userImage;
private ImageButton SendMessageButton, SendFilesButton;
private FirebaseAuth mAuth;
    private String savedCurrentTime, savedCurrenDate;
private EditText MessageInputText;
private RecyclerView userMessagesList;
private DatabaseReference RootRef;
private String checker="";
private final List<Messages> messagesList = new ArrayList<>();
private LinearLayoutManager linearLayoutManager;
private MessageAdapter messageAdapter;

private Toolbar chatToolbar;
    String messageRecieverID, messageRecieverName,messageRecieverImage;
    String messageSenderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderID=mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageRecieverID=getIntent().getExtras().get("visit_user_id").toString();
        messageRecieverName=getIntent().getExtras().get("visit_user_name").toString();
        messageRecieverImage=getIntent().getExtras().get("visit_image").toString();

        InitializeControllers();

        userName.setText(messageRecieverName);
        //Picasso.get().load(messageRecieverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        DisplayLastSeen();

    }

    private void InitializeControllers() {


        chatToolbar=(Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView=layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userImage=(CircleImageView) findViewById(R.id.custom_profile_IMAGE);
        userName=(TextView) findViewById(R.id.custom_profile_name);
        userLastSeen=(TextView) findViewById(R.id.custom_user_last_seen);

        SendMessageButton=(ImageButton) findViewById(R.id.send_message_btn);
        SendFilesButton=(ImageButton) findViewById(R.id.send_files_btn);
        MessageInputText=(EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList=(RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        savedCurrenDate = currentDate.format(calendar.getTime());

        savedCurrentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());


    }


    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageRecieverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("userState").hasChild("state"))
                {
                    String state = snapshot.child("userState").child("state").getValue().toString();
                    String date = snapshot.child("userState").child("date").getValue().toString();
                    String time = snapshot.child("userState").child("time").getValue().toString();

                    if(state.equals("online"))
                    {
                        userLastSeen.setText("online");

                    }
                    else if(state.equals("offline"))
                    {
                        userLastSeen.setText("Last Seen: "+date+ " "+ time);
                    }

                }
                else
                {
                    userLastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RootRef.child("Messages").child(messageSenderID).child(messageRecieverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Messages messages=snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void SendMessage()
    {
        String messageText=MessageInputText.getText().toString();
        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "First Write your message", Toast.LENGTH_SHORT).show();

        }

        else {
            String messageSenderRef ="Messages/"+messageSenderID+"/"+messageRecieverID;
            String messageRecieverRef ="Messages/"+messageRecieverID+"/"+messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID).child(messageRecieverID).push();
            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageRecieverID);
            messageTextBody.put("messageID",messagePushID);
            messageTextBody.put("time",savedCurrentTime);
            messageTextBody.put("date",savedCurrenDate);

            Map messageBodyDetail = new HashMap();
            messageBodyDetail.put(messageSenderRef+"/"+messagePushID,messageTextBody);
            messageBodyDetail.put(messageRecieverRef+"/"+messagePushID,messageTextBody);

            RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        //Toast.makeText(ChatActivity.this, "Message Sent Successfuly", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    MessageInputText.setText("");
                }
            });
        }
    }

}