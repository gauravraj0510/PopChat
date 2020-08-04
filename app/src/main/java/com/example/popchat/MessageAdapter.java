package com.example.popchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<Messages> userMessageList) {
        this.userMessageList = userMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();
        usersRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(fromUserId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("image")){
                    String receiverImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.senderDateTime.setVisibility(View.GONE);
        holder.receiverDateTime.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.imageSenderDateTime.setVisibility(View.GONE);
        holder.imageReceiverDateTime.setVisibility(View.GONE);
        holder.senderFileName.setVisibility(View.INVISIBLE);
        holder.receiverFileName.setVisibility(View.INVISIBLE);


        if(fromMessageType.equals("text")){

            if(fromUserId.equals(messageSenderId)){
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderDateTime.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setText(messages.getMessage());
                holder.senderDateTime.setText(messages.getTime()+" - "+messages.getDate());
            }
            else{
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverDateTime.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage());
                holder.receiverDateTime.setText(messages.getTime()+" - "+messages.getDate());

            }
        }
        else if(fromMessageType.equals("image")){
            if(fromUserId.equals(messageSenderId)){
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
                holder.imageSenderDateTime.setVisibility(View.VISIBLE);
                holder.imageSenderDateTime.setText(messages.getTime()+" - "+messages.getDate());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);

                    }
                });
            }
            else{
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.imageReceiverDateTime.setVisibility(View.VISIBLE);
                holder.imageReceiverDateTime.setText(messages.getTime()+" - "+messages.getDate());
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);

                    }
                });
            }
        }

        else{

            if(fromUserId.equals(messageSenderId)){

                holder.imageSenderDateTime.setVisibility(View.VISIBLE);
                holder.imageSenderDateTime.setText(messages.getTime()+" - "+messages.getDate());

                holder.senderFileName.setVisibility(View.VISIBLE);
                holder.senderFileName.setText(messages.getName());

                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.messageSenderPicture.setBackgroundResource(R.drawable.file);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));

                        holder.itemView.getContext().startActivity(intent);
                    }
                });

            }
            else{

                holder.imageReceiverDateTime.setVisibility(View.VISIBLE);
                holder.imageReceiverDateTime.setText(messages.getTime()+" - "+messages.getDate());

                holder.receiverFileName.setVisibility(View.VISIBLE);
                holder.receiverFileName.setText(messages.getName());

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setBackgroundResource(R.drawable.file);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);

                    }
                });
            }

        }

    }



    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText, receiverMessageText, senderDateTime, receiverDateTime, imageSenderDateTime, imageReceiverDateTime;
        public TextView senderFileName, receiverFileName;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            senderDateTime = itemView.findViewById(R.id.sender_date_time);
            receiverDateTime = itemView.findViewById(R.id.receiver_date_time);
            imageReceiverDateTime = itemView.findViewById(R.id.files_receiver_date_time);
            imageSenderDateTime = itemView.findViewById(R.id.files_sender_date_time);
            senderFileName = itemView.findViewById(R.id.sender_file_name);
            receiverFileName = itemView.findViewById(R.id.receiver_file_name);
        }
    }

    private void  deleteSentMessages(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Message deleted successfully!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(holder.itemView.getContext(), "Error occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void  deleteReceivedMessages(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Message deleted successfully!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(holder.itemView.getContext(), "Error occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void  deleteMessagesForEveryone(final int position, final MessageViewHolder holder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    rootRef.child("Messages")
                            .child(userMessageList.get(position).getTo())
                            .child(userMessageList.get(position).getFrom())
                            .child(userMessageList.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "Message deleted successfully!", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(holder.itemView.getContext(), "Error occurred!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
    }
}
