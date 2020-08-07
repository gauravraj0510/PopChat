package com.example.popchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class InstructionActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView instructions, howTo;
    private TextView githubLink, githubRepoLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        githubLink = findViewById(R.id.tv_github_profile_link);
        githubRepoLink = findViewById(R.id.tv_github_repo_link);

        githubLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/gauravraj0510";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        githubRepoLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/gauravraj0510/PopChat";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        mToolbar = findViewById(R.id.instruction_toolbar);
        instructions = findViewById(R.id.textView_instructions);
        howTo = findViewById(R.id.textView_use);

        instructions.setText(Html.fromHtml("<p><u>PopChat Guide</u></p>"));
        howTo.setText(Html.fromHtml("<p><u>How to use PopChat?</u></p>"));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Instructions");


    }
}