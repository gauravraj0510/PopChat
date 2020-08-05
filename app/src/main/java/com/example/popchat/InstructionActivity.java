package com.example.popchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class InstructionActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView instructions, howTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

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