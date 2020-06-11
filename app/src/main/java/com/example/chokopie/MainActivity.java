package com.example.chokopie;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;


public class MainActivity extends AppCompatActivity {

    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doShit();
    }

    public void doShit() {
        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python python = Python.getInstance();
        PyObject callScript = python.getModule("myscript");
        PyObject callFunc = callScript.callAttr("func", "photo1.jpg");
        textView = findViewById(R.id.text);
        textView.setText(callFunc.toString());
    }
}

