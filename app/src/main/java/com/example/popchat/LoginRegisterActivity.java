package com.example.popchat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class LoginRegisterActivity extends AppCompatActivity {

    private static final String TAG = "LoginRegisterActivity";
    int AUTH_UI_REQ_CODE = 10001;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private TextView githubLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        githubLink = findViewById(R.id.tv_github_profile_link);
        githubLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/gauravraj0510";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        rootRef = FirebaseDatabase.getInstance().getReference();

        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            startActivity(new Intent(this, MainActivity.class));
            this.finish();
        }
    }

    public void handleLoginRegister(View view) {

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build()
        );

        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.pop_logo)
                .build();

        startActivityForResult(intent, AUTH_UI_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AUTH_UI_REQ_CODE){
            if(resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d(TAG, "onActivityResult: " + user.getEmail());
                if(user.getMetadata().getCreationTimestamp() == user.getMetadata().getLastSignInTimestamp()){

                    String currentUserId = user.getUid();
                    rootRef.child("Users")
                            .child(currentUserId).setValue("");
                }
                else{
                    Toast.makeText(this, "Welcome back again!", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                this.finish();
            }
            else{
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if(response == null)
                {
                    Log.d(TAG, "onActivityResult: User cancelled Signin");
                }
                else
                {
                    Log.d(TAG, "onActivityResult: "+ response.getError());
                }
            }
        }
    }
}
