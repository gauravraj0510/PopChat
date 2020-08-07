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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View privatChatsView;
    private RecyclerView chatListRecyclerView;
    private DatabaseReference chatsRef, usersRef;
    private String currentUserId;
    private FirebaseAuth mAuth;

    private String profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/popchat-229ab.appspot.com/o/Image%20files%2Fprofile_image.png?alt=media&token=846fa687-3d01-464d-b4c2-11730a35b7c8";

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privatChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            currentUserId = mAuth.getCurrentUser().getUid();
            chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        }
        else{
            chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        }
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatListRecyclerView = privatChatsView.findViewById(R.id.chat_list_recycler_view);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return privatChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactsAdapter> options = new FirebaseRecyclerOptions.Builder<ContactsAdapter>()
                .setQuery(chatsRef, ContactsAdapter.class)
                .build();

        FirebaseRecyclerAdapter<ContactsAdapter, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsAdapter, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull final ContactsAdapter model) {

                final String userId = getRef(position).getKey();
                final String[] retImage = {"default_image"};

                usersRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){

                            if(snapshot.child("userState").hasChild("state")){

                                String state = snapshot.child("userState").child("state").getValue().toString();

                                if(state.equals("online")){
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("offline")){
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else{
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if(snapshot.hasChild("image")){
                                retImage[0] = snapshot.child("image").getValue().toString();
                                Picasso.get().load(retImage[0]).into(holder.profileImage);
                            }
                            else{
                                Picasso.get().load(profileImageUrl).into(holder.profileImage);
                            }

                            final String retUserName = snapshot.child("name").getValue().toString();
                            final String retUserStatus = snapshot.child("status").getValue().toString();

                            holder.userName.setText(retUserName);
                            holder.userStatus.setText("Last seen:"+ "Date Time");

                            if(snapshot.child("userState").hasChild("state")){
                                String state = snapshot.child("userState").child("state").getValue().toString();
                                String date = snapshot.child("userState").child("date").getValue().toString();
                                String time = snapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online")){
                                    holder.userStatus.setText("Online");
                                }
                                else if(state.equals("offline")){
                                    holder.userStatus.setText("Last seen: "+ date+" "+time);
                                }
                            }
                            else{
                                holder.userStatus.setText("offline");
                            }



                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visitUserId",userId);
                                    chatIntent.putExtra("visitUserName",retUserName);
                                    chatIntent.putExtra("visitUserImage", retImage[0]);
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                return new ChatsViewHolder(view);
            }
        };

        chatListRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);

        }
    }
}
