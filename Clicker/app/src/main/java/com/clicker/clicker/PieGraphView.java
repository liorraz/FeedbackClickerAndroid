package com.clicker.clicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class PieGraphView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int[] COLORS = {Color.GREEN, Color.RED, Color.GRAY};
    private final RectF rectf = new RectF(50, 50, 500, 500);

    private final float[] valueDegrees;

    public PieGraphView(Context context) {
        this(context, new float[]{0, 0, 0}); // all none
    }

    public PieGraphView(Context context, float[] valueDegrees) {
        this(context, null, valueDegrees);
    }

    public PieGraphView(Context context, AttributeSet attrs, float[] valueDegrees) {
        this(context, attrs, 0, valueDegrees);
    }

    public PieGraphView(Context context, AttributeSet attrs, int defStyleAttr, float[] valueDegrees) {
        super(context, attrs, defStyleAttr);
        validateLength(valueDegrees);
        this.valueDegrees = valueDegrees;
    }


    private void validateLength(float[] valueDegrees) {
        if (COLORS.length != valueDegrees.length) {
            throw new RuntimeException("Can't set values for length different than " + COLORS.length);
        }
    }


    public void setPieDegreesValues(float[] values) {
        validateLength(values);

        // copy to mine
        System.arraycopy(values, 0, valueDegrees, 0, values.length);
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float temp = 0;
        for (int i = 0; i < valueDegrees.length; i++) {
            if (i == 0) {
                paint.setColor(COLORS[i]);
                canvas.drawArc(rectf, 0, valueDegrees[i], true, paint);
            } else {
                temp += valueDegrees[i - 1];
                paint.setColor(COLORS[i]);
                canvas.drawArc(rectf, temp, valueDegrees[i], true, paint);
            }
        }
    }

}

