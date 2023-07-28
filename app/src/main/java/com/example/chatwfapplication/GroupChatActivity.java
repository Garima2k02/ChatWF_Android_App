package com.example.chatwfapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {


    private Toolbar mToolBar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private String currentDate,currentTime,currentGroupName, currentUserID, currentUserName;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, GroupNameRef, GroupmsgKeyRef ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName=getIntent().getExtras().get("groupName").toString();


        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        //Reference to any group that is clicked
        GroupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        InitializeFields();

        getUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessageInfoToDatabase();

                userMessageInput.setText("");

                //Automatic scroll on sending a new message
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    DisplayMessages(snapshot);
                }
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

    private void DisplayMessages(DataSnapshot snapshot) {
        Iterator iterator=snapshot.getChildren().iterator();

        while(iterator.hasNext())
        {
            String chatDate=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatMsg=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatName=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName+ " :\n" + chatMsg + "\n" + chatDate + "   " + chatTime + "\n\n\n");

            //Automatic scrolling to the new msg sent
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }
    }

    private void SendMessageInfoToDatabase() {
        String msg=userMessageInput.getText().toString();

        //create key for each msg
        String msgKey=GroupNameRef.push().getKey();
        if(TextUtils.isEmpty(msg))
        {
            Toast.makeText(this, "Please write a msg", Toast.LENGTH_SHORT).show();

        }
        else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd, yyyy");
            currentDate=currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=currentTimeFormat.format(calForTime.getTime());

            HashMap<String , Object> groupmsgKey = new HashMap<>();
            GroupNameRef.updateChildren(groupmsgKey);

            GroupmsgKeyRef=GroupNameRef.child(msgKey);

            HashMap<String,Object> msgInfoMap=new HashMap<>();
            msgInfoMap.put("name",currentUserName);
            msgInfoMap.put("msg",msg);
            msgInfoMap.put("date",currentDate);
            msgInfoMap.put("time",currentTime);
            GroupmsgKeyRef.updateChildren(msgInfoMap);
        }
    }

    private void getUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    currentUserName=snapshot.child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFields() {
        mToolBar=(Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(currentGroupName);
        SendMessageButton=(ImageButton) findViewById(R.id.send_message_button);
        userMessageInput=(EditText) findViewById(R.id.input_group_message);
        displayTextMessages=(TextView) findViewById(R.id.group_chat_text_display);
        mScrollView=(ScrollView) findViewById(R.id.my_scroll_view);

    }
}