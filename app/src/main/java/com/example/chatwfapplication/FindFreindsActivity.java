package com.example.chatwfapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFreindsActivity extends AppCompatActivity {


    private Toolbar mToolBar;
    private DatabaseReference UserRef;
    private RecyclerView FindFreindsRecylcerList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_freinds);

        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        FindFreindsRecylcerList=(RecyclerView) findViewById(R.id.find_frinds_recycle_list);
        FindFreindsRecylcerList.setLayoutManager(new LinearLayoutManager(this));
        mToolBar=(Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


    }

    @Override
    protected  void onStart() {

        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(UserRef,Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position, @NonNull Contacts model) {

                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        //Picasso.get().load(model.getImage()).into(holder.profileImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id = getRef(holder.getBindingAdapterPosition()).getKey();

                                Intent profileIntent=new Intent(FindFreindsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);

                            }
                        });
                    }

                    @NonNull
                    @Override
                    //user display layout
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_diaplay_layout,parent,false);
                        FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);

                        return viewHolder;
                    }
                };
        FindFreindsRecylcerList.setAdapter(adapter);
        adapter.startListening();
    }

    public  static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
       //CircleImageView profileImage;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            //profileImage=itemView.findViewById(R.id.users_profile_image);

        }
    }
}