package com.charlee.sns.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RedDotImageView extends ImageView {
    private Paint redDotPaint;
    private boolean isDrawRedDot = false;
    private boolean isDrawIcon = false;

    private int paddingTopPixel = 0;
    private int paddingRightPixel = 0;

    private Bitmap iconBitmap;

    public RedDotImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        redDotPaint = new Paint();
        redDotPaint.setAntiAlias(true);
        redDotPaint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isDrawRedDot) {
            int radus = (int) (4 * getResources().getDisplayMetrics().density);
            int centerX = getWidth() - paddingRightPixel - radus;
            int centerY = paddingTopPixel + radus;
            canvas.drawCircle(centerX, centerY, radus, redDotPaint);
        } else if (isDrawIcon && iconBitmap != null) {
            int startX = getWidth() - paddingRightPixel - iconBitmap.getWidth();
            int startY = paddingTopPixel;
            canvas.drawBitmap(iconBitmap, startX, startY, null);
        }
    }

    public void drawRedDot(boolean toDrawRedDot, int paddingTopDp, int paddingRightDp) {
        isDrawRedDot = toDrawRedDot;
        if (isDrawRedDot) {
            isDrawIcon = false;
            this.paddingTopPixel = (int) (paddingTopDp * getResources().getDisplayMetrics().density);
            this.paddingRightPixel = (int) (paddingRightDp * getResources().getDisplayMetrics().density);
        }
        postInvalidate();
    }

    public void drawIcon(boolean toDrawIcon, Bitmap iconBitmap, int paddingTopDp, int paddingRightDp) {
        isDrawIcon  = toDrawIcon;
        if (isDrawIcon) {
            isDrawRedDot = false;
            this.paddingTopPixel = (int) (paddingTopDp * getResources().getDisplayMetrics().density);
            this.paddingRightPixel = (int) (paddingRightDp * getResources().getDisplayMetrics().density);
            this.iconBitmap = iconBitmap;
        }
        postInvalidate();
    }

    public boolean isDrawRedDot() {
        return isDrawRedDot;
    }

    public boolean isDrawIcon() {
        return isDrawIcon;
    }
}