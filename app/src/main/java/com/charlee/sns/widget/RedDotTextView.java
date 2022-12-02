package com.charlee.sns.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class RedDotTextView extends TextView {
    private Paint redDotPaint;
    private boolean isDrawRedDot = false;

    private float proXPercentFromLeft = 0;
    private float proYPercentFromTop = 0;

    public RedDotTextView(Context context) {
        super(context);
        init();
    }

    public RedDotTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RedDotTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        redDotPaint = new Paint();
        redDotPaint.setAntiAlias(true);
        redDotPaint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isDrawRedDot) {
            int radus = (int) (4 * getResources().getDisplayMetrics().density);
            int width = getWidth();
            canvas.drawCircle(width - radus, radus, radus, redDotPaint);
        }
    }

    public void drawRedDot(boolean toDrawRedDot) {
        isDrawRedDot = toDrawRedDot;
        postInvalidate();
    }

    public boolean isDrawRedDot() {
        return isDrawRedDot;
    }
}