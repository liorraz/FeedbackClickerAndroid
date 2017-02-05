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

public class MainActivity extends AppCompatActivity {


    private float values[] = {300, 400, 100};
    private MyGraphView pieChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout pieChartContainer = (LinearLayout) findViewById(R.id.linear);
        // calculate in re-write data
        values = calculateDataToAngles(values);
        pieChartView = new MyGraphView(this, values);
        pieChartContainer.addView(pieChartView);
    }

    private float[] calculateDataToAngles(float[] data) {
        float total = 0;
        for (float value : data) {
            total += value;
        }
        for (int i = 0; i < data.length; i++) {
            data[i] = 360 * (data[i] / total);
        }
        return data;

    }

    // on click handler
    public void sendQuestionOnClick(View view) {
        updateChartValues();
    }

    private void updateChartValues() {
        float[] temp = new float[values.length];
        System.arraycopy(values, 0, temp, 0, values.length);
        for (int i = 0; i < values.length; i++) {
            values[i] = temp[(i + 1) % values.length];
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pieChartView.setValues(values);
            }
        });
    }


    private class MyGraphView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float[] value_degree;
        private final int[] COLORS = {Color.GREEN, Color.RED, Color.GRAY};
        private RectF rectf = new RectF(20, 20, 300, 300);


        private MyGraphView(Context context, float[] values) {
            super(context);
            value_degree = new float[values.length];
            setValues(values);
        }


        private void setValues(float[] values) {
            System.arraycopy(values, 0, value_degree, 0, values.length);
            invalidate();
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float temp = 0;
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
