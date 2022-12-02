package com.charlee.sns.widget;

/**
 */

import com.facebook.drawee.drawable.DrawableUtils;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class ProgressCircleDrawable extends Drawable {
    private final Paint paintBackground = new Paint(1);
    private int backgroundColor = 0x66000000;
    private final Paint paintCircle = new Paint(1);
    private final Paint paintArc = new Paint(1);
    private int color = 0xe6ffffff;
    private int size = 20;
    private int level = 0;
    private boolean hideWhenZero = false;

    public ProgressCircleDrawable() {
    }

    public void setColor(int color) {
        if (this.color != color) {
            this.color = color;
            this.invalidateSelf();
        }
    }

    public int getColor() {
        return this.color;
    }

    public void setBackgroundColor(int backgroundColor) {
        if (this.backgroundColor != backgroundColor) {
            this.backgroundColor = backgroundColor;
            this.invalidateSelf();
        }
    }

    public int getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setSize(int size) {
        if (this.size != size) {
            this.size = size;
            this.invalidateSelf();
        }
    }

    public int getSize() {
        return this.size;
    }

    public void setHideWhenZero(boolean hideWhenZero) {
        this.hideWhenZero = hideWhenZero;
    }

    public boolean getHideWhenZero() {
        return this.hideWhenZero;
    }

    @Override
    protected boolean onLevelChange(int level) {
        this.level = level;
        this.invalidateSelf();
        return true;
    }

    public void setAlpha(int alpha) {
        this.paintBackground.setAlpha(alpha);
        this.paintCircle.setAlpha(alpha);
        this.paintArc.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter cf) {
        this.paintCircle.setColorFilter(cf);
        this.paintArc.setColorFilter(cf);
    }

    public int getOpacity() {
        return DrawableUtils.getOpacityFromColor(this.paintBackground.getColor());
    }

    public void draw(Canvas canvas) {
        if (!this.hideWhenZero || this.level != 0) {
            Rect bounds = this.getBounds();
            float centerX = bounds.exactCenterX();
            float centerY = bounds.exactCenterY();
            float radius = (float) size / 2.0f;
            RectF rect = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

            // Backgound
            paintBackground.setColor(backgroundColor);
            canvas.drawArc(rect, 0, 360, true, paintBackground);

            // Outer circle
            paintCircle.setAntiAlias(true);
            paintCircle.setColor(color);
            paintCircle.setStyle(Paint.Style.STROKE);
            paintCircle.setStrokeWidth(1);
            canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), radius, paintCircle);

            // Percentage arc
            float gap = radius / 10;
            paintArc.setAntiAlias(true);
            paintArc.setColor(color);
            paintArc.setStyle(Paint.Style.FILL);
            RectF arcRect = new RectF(rect.left + gap, rect.top + gap, rect.right - gap, rect.bottom - gap);
            canvas.drawArc(arcRect, -90, 360 * level / 10000, true, paintArc);
        }
    }
}

