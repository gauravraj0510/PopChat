package com.example.popchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestRecyclerView;
    private DatabaseReference chatRequestRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private String profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/popchat-229ab.appspot.com/o/Image%20files%2Fprofile_image.png?alt=media&token=846fa687-3d01-464d-b4c2-11730a35b7c8";

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        requestFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        myRequestRecyclerView = requestFragmentView.findViewById(R.id.chat_requests_recycler_view);
        myRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactsAdapter> options = new FirebaseRecyclerOptions.Builder<ContactsAdapter>()
                .setQuery(chatRequestRef.child(currentUserId), ContactsAdapter.class)
                .build();

        FirebaseRecyclerAdapter<ContactsAdapter, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsAdapter, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, final int position, @NonNull final ContactsAdapter model) {

                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);



                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visitUserId = getRef(position).getKey();
                        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                        profileIntent.putExtra("visitUserId", visitUserId);
                        startActivity(profileIntent);
                    }
                });

                final String listUsersId = getRef(position).getKey();
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String type = snapshot.getValue().toString();
                            if(type.equals("received")){
                                usersRef.child(listUsersId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("image")){
                                            final String requestUserImage = snapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                        }

                                        final String requestUserName = snapshot.child("name").getValue().toString();
                                        final String requestUserStatus = snapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("sent you a message request");


                                        holder.itemView.findViewById(R.id.request_accept_btn).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                contactsRef.child(currentUserId).child(listUsersId).child("Contacts")
                                                        .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            contactsRef.child(listUsersId).child(currentUserId).child("Contacts")
                                                                    .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        chatRequestRef.child(currentUserId).child(listUsersId)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful()){
                                                                                            chatRequestRef.child(listUsersId).child(currentUserId)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){
                                                                                                                Toast.makeText(getContext(), "Message request accepted and added to the Contacts !", Toast.LENGTH_SHORT).show();
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
                                        });
                                        holder.itemView.findViewById(R.id.request_cancel_btn).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                chatRequestRef.child(currentUserId).child(listUsersId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    chatRequestRef.child(listUsersId).child(currentUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        Toast.makeText(getContext(), "Message request declined !", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            else if(type.equals("sent")){
                                Button requestSentButton = holder.itemView.findViewById(R.id.request_accept_btn);
                                requestSentButton.setText("Request sent successfully!");
                                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.INVISIBLE);

                                usersRef.child(listUsersId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("image")){
                                            final String requestUserImage = snapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestUserImage).into(holder.profileImage);

                                        }
                                        else{
                                            Picasso.get().load(profileImageUrl).into(holder.profileImage);
                                        }

                                        final String requestUserName = snapshot.child("name").getValue().toString();
                                        final String requestUserStatus = snapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("You sent a message request to "+requestUserName);

                                        Button cancelButton = holder.itemView.findViewById(R.id.request_cancel_btn);
                                        cancelButton.setText("Cancel");

                                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.GONE);

                                        holder.itemView.findViewById(R.id.request_cancel_btn).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                chatRequestRef.child(currentUserId).child(listUsersId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    chatRequestRef.child(listUsersId).child(currentUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        Toast.makeText(getContext(), "Message request cancelled !", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent,false);
                RequestViewHolder holder = new RequestViewHolder(view);
                return holder;
            }
        };

        myRequestRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, cancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_btn);
            cancelButton = itemView.findViewById(R.id.request_cancel_btn);

        }
    }
}