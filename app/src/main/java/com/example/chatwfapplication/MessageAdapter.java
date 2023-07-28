package com.example.chatwfapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
        private List<Messages> userMessagesList;
        public MessageAdapter(List<Messages> userMessagesList)
        {
            this.userMessagesList=userMessagesList;
        }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, recieverMessageText;
        public CircleImageView recieverProfileImage;

        public ImageView messageSenderPicture, messageRecieverPicture;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText=(TextView) itemView.findViewById(R.id.sender_message_text);
            recieverMessageText=(TextView) itemView.findViewById(R.id.reciever_message_text);
            recieverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);

            messageRecieverPicture=(ImageView) itemView.findViewById(R.id.message_reciever_image_view);
            messageSenderPicture=(ImageView) itemView.findViewById(R.id.message_sender_image_view);

        }
    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout,parent,false);

        mAuth =FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            String messageSenderID=mAuth.getCurrentUser().getUid();
            Messages messages = userMessagesList.get(position);

            String fromUserID=messages.getFrom();
            String fromMessageType=messages.getType();
            usersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild("image"))
                    {
                        String recieverImage = snapshot.child("image").getValue().toString();
                        //Picasso.get().load(recieverImage).placeholder(R.drawable.profile_image).into(holder.recieverProfileImage);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        holder.recieverMessageText.setVisibility(View.GONE);
        holder.recieverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageRecieverPicture.setVisibility(View.GONE);

        if(fromMessageType.equals("text"))
            {

                if(fromUserID.equals(messageSenderID))
                {
                    holder.senderMessageText.setVisibility(View.VISIBLE);
                    holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                    holder.senderMessageText.setText(messages.getMessage() + "\n\n " + messages.getTime()+" - " + messages.getDate());

                }

                else {
                    holder.recieverMessageText.setVisibility(View.VISIBLE);
                    holder.recieverProfileImage.setVisibility(View.VISIBLE);

                    holder.recieverMessageText.setBackgroundResource(R.drawable.reviever_message_layout);
                    holder.recieverMessageText.setText(messages.getMessage()+ "\n\n " + messages.getTime()+" - " + messages.getDate());
                }
            }
    }



    @Override
    public int getItemCount() {

        return userMessagesList.size();
    }



}
