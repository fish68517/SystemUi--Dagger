package com.ellison.jetpackdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showDagger2Demo(View view) {
        jumpToActivity(com.ellison.jetpackdemo.dagger2.DemoActivity.class);
    }

    public void showHiltDemo(View view) {
        jumpToActivity(com.ellison.jetpackdemo.hilt.DemoActivity.class);
    }

    private void jumpToActivity(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Target screen not found.", Toast.LENGTH_SHORT).show();
        }
    }
}
