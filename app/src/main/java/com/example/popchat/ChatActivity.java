package com.example.popchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId, messageReceiverName, messageReceiverImage, messageSenderId;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private RecyclerView userMessagesRecyclerView;

    private ProgressDialog loadingBar;

    private Toolbar chatToolbar;
    private ImageButton sendMessageButton, sendFilesButton;
    private EditText messageInputText;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String saveCurrentTime, saveCurrentDate;


    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private String checker = "", myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverId = getIntent().getExtras().get("visitUserId").toString();
        messageReceiverName = getIntent().getExtras().get("visitUserName").toString();
        messageReceiverImage = getIntent().getExtras().get("visitUserImage").toString();

        initializeControllers();

        getMessages();


    }

    private void getMessages() {

        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        Messages messages = snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();

                        userMessagesRecyclerView.smoothScrollToPosition(userMessagesRecyclerView.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });
    }

    private void initializeControllers() {

        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        userMessagesRecyclerView = findViewById(R.id.private_messages_list_recycler_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);

        sendMessageButton = findViewById(R.id.send_message_private_button);
        sendFilesButton = findViewById(R.id.send_files_button);
        messageInputText = findViewById(R.id.input_private_message);

        messageAdapter = new MessageAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesRecyclerView.setLayoutManager(linearLayoutManager);
        userMessagesRecyclerView.setAdapter(messageAdapter);

        loadingBar = new ProgressDialog(this);

        displayLastSeen();

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                             "Image",
                             "PDF File",
                                "Word File"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);

                builder.setTitle("Select file type:-");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"),438);
                        }
                        if(which == 1){
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword*");
                            startActivityForResult(intent.createChooser(intent, "Select PDF file"),438);
                        }
                        if(which == 2){
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword*");
                            startActivityForResult(intent.createChooser(intent, "Select WORD file"),438);
                        }


                    }
                });
                builder.show();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            loadingBar.setTitle("Sending file! Just a moment!");
            loadingBar.setMessage("Please wait! Your new profile image is getting updated!");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();
            if(!checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document files");

                final String messageSenderRef = "Messages/"+ messageSenderId+"/"+messageReceiverId;
                final String messageReceiverRef = "Messages/"+ messageReceiverId+"/"+messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(messageSenderId).child(messageReceiverId).push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId+"."+checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();

                                Calendar calendar = Calendar.getInstance();

                                SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                                saveCurrentDate = currentDate.format(calendar.getTime());

                                SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                                saveCurrentTime = currentTime.format(calendar.getTime());

                                Map messageImageBody = new HashMap();
                                messageImageBody.put("message",downloadUrl);
                                messageImageBody.put("name",fileUri.getLastPathSegment());
                                messageImageBody.put("type",checker);
                                messageImageBody.put("from",messageSenderId);
                                messageImageBody.put("to", messageReceiverId);
                                messageImageBody.put("messageID", messagePushId);
                                messageImageBody.put("time", saveCurrentTime);
                                messageImageBody.put("date", saveCurrentDate);


                                Map messageBodyDetail = new HashMap();
                                messageBodyDetail.put(messageSenderRef+ "/" + messagePushId, messageImageBody);
                                messageBodyDetail.put(messageReceiverRef+ "/" + messagePushId, messageImageBody);

                                rootRef.updateChildren(messageBodyDetail);
                                loadingBar.dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0* taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading...");
                    }
                });
            }
            else if(checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image files");

                final String messageSenderRef = "Messages/"+ messageSenderId+"/"+messageReceiverId;
                final String messageReceiverRef = "Messages/"+ messageReceiverId+"/"+messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(messageSenderId).child(messageReceiverId).push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId+".jpg");

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if(!task.isSuccessful()){
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();

                            Calendar calendar = Calendar.getInstance();

                            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                            saveCurrentDate = currentDate.format(calendar.getTime());

                            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                            saveCurrentTime = currentTime.format(calendar.getTime());

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type","image");
                            messageTextBody.put("from",messageSenderId);
                            messageTextBody.put("to",messageReceiverId);
                            messageTextBody.put("messageId",messagePushId);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushId, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId, messageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if(task.isSuccessful()){
                                        loadingBar.dismiss();
                                    }
                                    else{
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error in sending message!", Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else{
                loadingBar.dismiss();
                Toast.makeText(this, "Please select one option first!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayLastSeen(){
        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child("userState").hasChild("state")){
                    String state = snapshot.child("userState").child("state").getValue().toString();
                    String date = snapshot.child("userState").child("date").getValue().toString();
                    String time = snapshot.child("userState").child("time").getValue().toString();

                    if(state.equals("online")){
                        userLastSeen.setText("Online");
                    }
                    else if(state.equals("offline")){
                        userLastSeen.setText("Last seen: "+ date+" "+time);
                    }
                }
                else{
                    userLastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void sendMessage(){

        String message = messageInputText.getText().toString();

        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please write a message first!", Toast.LENGTH_SHORT).show();
        }
        else{
            String messageSenderRef = "Messages/"+ messageSenderId+"/"+messageReceiverId;
            String messageReceiverRef = "Messages/"+ messageReceiverId+"/"+messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                    .child(messageSenderId).child(messageReceiverId).push();

            String messagePushId = userMessageKeyRef.getKey();
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            saveCurrentTime = currentTime.format(calendar.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",message);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);
            messageTextBody.put("to",messageReceiverId);
            messageTextBody.put("messageId",messagePushId);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()){
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "Message send error!", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });
        }

    }

}