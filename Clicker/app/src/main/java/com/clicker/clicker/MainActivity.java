package com.clicker.clicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity {


    float values[] = {300, 400, 100/*, 500*/};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout scroll = (LinearLayout) findViewById(R.id.linear);
        values = calculateData(values);
        scroll.addView(new MyGraphView(this, values));

    }

    private float[] calculateData(float[] data) {
        float total = 0;
        for (float value : data) {
            total += value;
        }
        for (int i = 0; i < data.length; i++) {
            data[i] = 360 * (data[i] / total);
        }
        return data;

    }

    public class MyGraphView extends View {
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float[] value_degree;
        private int[] COLORS = {Color.BLUE, Color.GREEN, /*Color.GRAY, Color.CYAN,*/ Color.RED};
        RectF rectf = new RectF(20, 20, 300, 300);
        float temp = 0;

        public MyGraphView(Context context, float[] values) {

            super(context);
            value_degree = new float[values.length];
            System.arraycopy(values, 0, value_degree, 0, values.length);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            for (int i = 0; i < value_degree.length; i++) {
                if (i == 0) {
                    paint.setColor(COLORS[i]);
                    canvas.drawArc(rectf, 0, value_degree[i], true, paint);
                } else {
                    temp += value_degree[i - 1];
                    paint.setColor(COLORS[i]);
                    canvas.drawArc(rectf, temp, value_degree[i], true, paint);
                }
            }
        }

    }
}
