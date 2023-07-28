package com.example.chatwfapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView chatsList;
    private FirebaseAuth mAuth;
    private String currentUserID;

    //String retImage="default_Image";
    private DatabaseReference ChatsRef,userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView= inflater.inflate(R.layout.fragment_chats, container, false);
        //LayoutInflater will instantiate the xml file to be used in java file
        //ViewGroup is the parent viewgroup that contains the fragment
        //Bundle if true then fragment will resume from previous saved state else it will start from beginning

        mAuth=FirebaseAuth.getInstance();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if(currentUser==null)
//        {
//            sendUserToLoginActivity();
//        }
        currentUserID=mAuth.getCurrentUser().getUid();

        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        //use firebase recycler to retrieve contacts list of each user

//        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
//
//
//        mAuth = FirebaseAuth.getInstance();
//        currentUserID = mAuth.getCurrentUser().getUid();
//        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
//        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
//
//
//        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
//        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
//
//
//        return PrivateChatsView;


    return PrivateChatsView;
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent= new Intent(getContext(),LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatsRef,Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder > adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contacts model) {
                        final String usersIDs = getRef(holder.getBindingAdapterPosition()).getKey();
                        final String[] retImage = {"default_image"};

                        userRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    if(snapshot.hasChild("image"))
                                    {
                                        retImage[0] = snapshot.child("image").getValue().toString();
                                        //Picasso.get().load(retImage).into(holder.profileImage);
                                    }

                                    final String retName = snapshot.child("name").getValue().toString();
                                    final String retStatus = snapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName);
                                    holder.userStatus.setText("Last seen:"+ "\n "+"Date "+ " Time");


                                    if(snapshot.child("userState").hasChild("state"))
                                    {
                                        String state = snapshot.child("userState").child("state").getValue().toString();
                                        String date = snapshot.child("userState").child("date").getValue().toString();
                                        String time = snapshot.child("userState").child("time").getValue().toString();

                                        if(state.equals("online"))
                                        {
                                            holder.userStatus.setText("online");

                                        }
                                        else if(state.equals("offline"))
                                        {
                                            holder.userStatus.setText("Last Seen: "+date+ " "+ time);
                                        }

                                    }
                                    else
                                    {
                                        holder.userStatus.setText("offline");
                                    }


                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", usersIDs);
                                            chatIntent.putExtra("visit_user_name",retName);
                                            chatIntent.putExtra("visit_image", retImage[0]);
                                            startActivity(chatIntent);

                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_diaplay_layout,parent,false);
                        return new ChatsViewHolder(view);
                    }
                };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);

        }

        CircleImageView profileImage;
        TextView userStatus, userName;


    }
}