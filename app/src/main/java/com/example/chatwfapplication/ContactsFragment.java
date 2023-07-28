package com.example.chatwfapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactsList;
    private DatabaseReference ContactsRef, UserRef;
    private String currentUserID;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        //LayoutInflater will instantiate the xml file to be used in java file
        //ViewGroup is the parent viewgroup that contains the fragment
        //Bundle if true then fragment will resume from previous saved state else it will start from beginning

        myContactsList=(RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        UserRef=FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ContactsRef,Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Contacts model) {

                        String userIDs = getRef(holder.getBindingAdapterPosition()).getKey();
                        UserRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    if(snapshot.child("userState").hasChild("state"))
                                    {
                                        String state = snapshot.child("userState").child("state").getValue().toString();
                                        String date = snapshot.child("userState").child("date").getValue().toString();
                                        String time = snapshot.child("userState").child("time").getValue().toString();

                                        if(state.equals("online"))
                                        {
                                            holder.onlineIcon.setVisibility(View.VISIBLE);

                                        }
                                        else if(state.equals("offline"))
                                        {
                                            holder.onlineIcon.setVisibility(View.INVISIBLE);
                                        }

                                    }
                                    else
                                    {
                                        holder.onlineIcon.setVisibility(View.INVISIBLE);
                                    }

                                    if(snapshot.hasChild("image"))
                                    {
                                        //String userImage=snapshot.child("image").getValue().toString();
                                        String profileStatus=snapshot.child("status").getValue().toString();
                                        String profileName=snapshot.child("name").getValue().toString();

                                        holder.userNAme.setText(profileName);
                                        holder.userStatus.setText(profileStatus);
                                        //Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                    }
                                    else {
                                        String profileStatus=snapshot.child("status").getValue().toString();
                                        String profileName=snapshot.child("name").getValue().toString();

                                        holder.userNAme.setText(profileName);
                                        holder.userStatus.setText(profileStatus);
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
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_diaplay_layout,parent,false);

                        ContactsViewHolder viewHolder=new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };

        myContactsList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNAme, userStatus;
        ImageView onlineIcon;
        //CircleImageView profileImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userNAme = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
           // profileImage = itemView.findViewById(R.id.user_profile_image);
            onlineIcon=(ImageView) itemView.findViewById(R.id.user_online_status);


        }
    }
}