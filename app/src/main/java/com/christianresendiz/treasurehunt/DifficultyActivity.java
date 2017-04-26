package com.christianresendiz.treasurehunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DifficultyActivity extends AppCompatActivity {

    Button easy;
    Button medium;
    Button hard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        easy = (Button) findViewById(R.id.easyBtn);
        medium = (Button) findViewById(R.id.mediumBtn);
        hard = (Button) findViewById(R.id.hardBtn);

        easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dif = 1;
                Intent intent = new Intent(DifficultyActivity.this, CameraActivity.class).putExtra("difficulty", dif);
                DifficultyActivity.this.startActivity(intent);
            }
        });

        medium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dif = 2;
                Intent intent = new Intent(DifficultyActivity.this, CameraActivity.class).putExtra("difficulty", dif);
                DifficultyActivity.this.startActivity(intent);
            }
        });

        hard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dif = 3;
                Intent intent = new Intent(DifficultyActivity.this, CameraActivity.class).putExtra("difficulty", dif);
                DifficultyActivity.this.startActivity(intent);
            }
        });
    }
}
