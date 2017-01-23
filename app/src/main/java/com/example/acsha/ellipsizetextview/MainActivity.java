package com.example.acsha.ellipsizetextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ElleleTextView elleleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        elleleTextView = (ElleleTextView) findViewById(R.id.test_textview);
        elleleTextView.setText("예술가의 별난 삶에서 찾은 예술 창작의 힘");

    }
}
