package com.example.chatwfapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private tabsAccessorAdapter myTabsAccessorAdapter;
    private TabLayout myTabLayout;
    private ViewPager2 myviewPager;
    private String currentUserID;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private String[] titles={"Chats","Groups","Contacts","Requests"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();

        RootRef=FirebaseDatabase.getInstance().getReference();

        mToolBar =(Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("ChatWF");

        myviewPager=findViewById(R.id.main_tabs_pager);
        myTabLayout=findViewById(R.id.main_tabs);
        myTabsAccessorAdapter =new tabsAccessorAdapter(this);

        myviewPager.setAdapter(myTabsAccessorAdapter);

        new TabLayoutMediator(myTabLayout,myviewPager,((tab, position) -> tab.setText(titles[position]))).attach();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            //If there is no user logged in before then he will be sent to login activity to log in to its account first
            SendUserToLoginActivity();
        }
        else {
            updateUserStatus("online");
            VerifyUserExistence();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null)
        {
            updateUserStatus("offline");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null)
        {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistence() {
        String currentUserId=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //If the user is not new then ok else he need to set his name and status
                if((snapshot.child("name").exists())){}
                else
                {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent= new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_option)
        {
            updateUserStatus("offline");
            mAuth.signOut();
            SendUserToLoginActivity();
        }

        if(item.getItemId() == R.id.main_create_group_option)
        {
            RequestNewGroup();
        }

        if(item.getItemId() == R.id.main_settings_option)
        {
            SendUserToSettingsActivity();
        }

        if(item.getItemId() == R.id.main_find_friends_option)
        {
            SendUserToFindFriendsActivity();
        }
        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");

        final EditText groupNameField=new EditText(MainActivity.this);
        groupNameField.setHint("e.g. Coding Cafe");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName=groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Please Add Group Name", Toast.LENGTH_SHORT).show();
                }

                else {
                    CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();

    }

    private void CreateNewGroup(String groupName) {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, groupName+" is created Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToSettingsActivity() {

        Intent settingsIntent= new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToFindFriendsActivity() {

        Intent findFriendsIntent= new Intent(MainActivity.this, FindFreindsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void updateUserStatus(String state)
    {
        String savedCurrentTime, savedCurrenDate;
        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        savedCurrenDate = currentDate.format(calendar.getTime());

        savedCurrentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        HashMap<String,Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time",savedCurrentTime);
        onlineStateMap.put("date",savedCurrenDate);
        onlineStateMap.put("state",state);

        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);


    }
}