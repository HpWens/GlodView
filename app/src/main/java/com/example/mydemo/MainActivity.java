package com.example.mydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private GoldLayoutView glGoldLayoutView;
    private View addView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glGoldLayoutView = findViewById(R.id.gold_view);
        addView = findViewById(R.id.view);


        addView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glGoldLayoutView.addGoldChildView(666);
            }
        });

        glGoldLayoutView.setOnItemClickListener(new GoldLayoutView.OnItemClickListener() {
            @Override
            public void onClick(int childIndex, int goldNumber) {
                Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT).show();
                glGoldLayoutView.startRemoveAnim(childIndex);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        glGoldLayoutView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glGoldLayoutView.onResume();
    }
}
