package com.example.acsha.ellipsizetextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ElleleTextView sample1;
    private ElleleTextView sample2;
    private ElleleTextView sample3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sample1 = (ElleleTextView) findViewById(R.id.sample_textview_1);
        sample1.setText("예술가의 별난 삶에서 찾은 예술 창작의 힘으로 살아가는 우리들의 이야기라는 사실을 알고 계시나요?");

        sample2 = (ElleleTextView) findViewById(R.id.sample_textview_2);
        sample2.setText("예술가의 별난 삶에서 찾은                예술 창작의 힘으로 살아가는 우리들의 이야기라는 사실을 알고 계시나요?");

        sample3 = (ElleleTextView) findViewById(R.id.sample_textview_3);
        sample3.setText("예술가의 별난 삶에서 찾은                 예술 창작의 힘으로 살아가          는 우리들의 이야기라는 사실을 알고 계시나요?");
    }
    
}
