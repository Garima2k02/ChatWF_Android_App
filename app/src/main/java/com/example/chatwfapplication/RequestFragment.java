package com.example.chatwfapplication;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {
    private View RequestsFragmentView;
    private RecyclerView myRequestList;

    private DatabaseReference chatRequestRef, UserRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUID;

    public RequestFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RequestsFragmentView =  inflater.inflate(R.layout.fragment_request, container, false);

        myRequestList = (RecyclerView) RequestsFragmentView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUID = mAuth.getUid();
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        // Inflate the layout for this fragment

        return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRequestRef.child(currentUID),Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Contacts model) {

                        holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                        final String list_userID = getRef(holder.getBindingAdapterPosition()).getKey();
                        DatabaseReference getTypeRef= getRef(holder.getBindingAdapterPosition()).child("request_type").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    String type= snapshot.getValue().toString();

                                    if(type.equals("recieved"))
                                    {
                                        UserRef.child(list_userID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.hasChild("image"))
                                                {
                                                    //final  String requestProfileImage = snapshot.child("image").getValue().toString();
                                                    //Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                                }

                                                final  String requestUserName = snapshot.child("name").getValue().toString();
                                                final  String requestUserStatus = snapshot.child("status").getValue().toString();
                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("Wants to Connect with You");

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName+" Chat Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                if(i==0)
                                                                {
                                                                    //if request accepted
                                                                    //remove from request list and add to contacts list
                                                                    ContactsRef.child(currentUID).child(list_userID).child("Contacts")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        //save contact for reciever as well
                                                                                        ContactsRef.child(list_userID).child(currentUID).child("Contacts")
                                                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            //save contact for reciever as well
                                                                                                            chatRequestRef.child(currentUID).child(list_userID)
                                                                                                                    .removeValue()
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                            if(task.isSuccessful())
                                                                                                                            {
                                                                                                                                chatRequestRef.child(list_userID).child(currentUID)
                                                                                                                                        .removeValue()
                                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                            @Override
                                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                                if(task.isSuccessful())
                                                                                                                                                {
                                                                                                                                                    Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();
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
                                                                                }
                                                                            });
                                                                }
                                                                if(i==1)
                                                                {
                                                                    chatRequestRef.child(currentUID).child(list_userID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        chatRequestRef.child(list_userID).child(currentUID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }

                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }

                                    else if(type.equals("sent"))
                                    {
                                        Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_button);
                                        request_sent_btn.setText("Request Sent");
                                        holder.itemView.findViewById(R.id.request_cancel_button)
                                                .setVisibility(View.INVISIBLE);


                                        UserRef.child(list_userID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.hasChild("image"))
                                                {
                                                    //final  String requestProfileImage = snapshot.child("image").getValue().toString();
                                                    //Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                                }

                                                final  String requestUserName = snapshot.child("name").getValue().toString();
                                                final  String requestUserStatus = snapshot.child("status").getValue().toString();
                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("WYou have sent a request to "+requestUserName);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Cancel Chat REquest"
                                                                };
                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Chat Request Sent");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                                if(i==0)
                                                                {
                                                                    chatRequestRef.child(currentUID).child(list_userID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        chatRequestRef.child(list_userID).child(currentUID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(), "You have cancelled the chat request", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }

                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });



                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_diaplay_layout,parent,false);
                        RequestViewHolder holder = new RequestViewHolder(view);
                        return holder;
                    }
                };

        myRequestList.setAdapter(adapter);
        adapter.startListening();

    }

    //to access user display layout
    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        //CircleImageView profileImage;
        Button AcceptButton, CancelButton;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName =itemView.findViewById(R.id.user_profile_name);
            userStatus =itemView.findViewById(R.id.user_status);
            //profileImage =itemView.findViewById(R.id.users_profile_image);
            CancelButton =itemView.findViewById(R.id.request_cancel_button);
            AcceptButton =itemView.findViewById(R.id.request_accept_button);


        }
    }

}