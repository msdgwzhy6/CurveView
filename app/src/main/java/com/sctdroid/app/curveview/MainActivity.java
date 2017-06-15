package com.sctdroid.app.curveview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CurveView curveView = (CurveView) findViewById(R.id.curve_view);
        curveView.setAdapter(new CurveView.Adapter() {

            String text = "吾生也有涯，而知也无涯";

            @Override
            public int getCount() {
                return 11;
            }

            @Override
            public int getLevel(int position) {
                return (int) (50 + (Math.random() * 10) * 5);
            }

            @Override
            public int getMinLevel() {
                return 30;
            }

            @Override
            public int getMaxLevel() {
                return 100;
            }

            @Override
            public Set<CurveView.Mark> onCreateMarks(int position) {
                Set<CurveView.Mark> marks = new HashSet<CurveView.Mark>();
                CurveView.Mark mark = new CurveView.Mark(getXAxisText(position), CurveView.GRAVITY_BOTTOM | CurveView.GRAVITY_CENTER_HORIZONTAL, 0, 10, 0, 0);
                CurveView.Mark mark1 = new CurveView.Mark(getXAxisText(position), CurveView.GRAVITY_START);
                marks.add(mark);
                marks.add(mark1);
                return marks;
            }

            @Override
            public String getXAxisText(int i) {
                return text.substring(i,i+1);
            }
        });
    }
}
