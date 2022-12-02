package com.charlee.sns.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.charlee.sns.R;


/**
 * 用于列表为空时的占位提示。
 */
public class EmptyPlaceholderView extends LinearLayout {

    private String hintString;
    private Drawable hintImage;
    private String actionString;
    private Drawable actionIcon;

    private ImageView hintImageView;
    private TextView hintTextView;
    private View actionButton;
    private TextView actionButtonTxt;
    private ImageView actionButtonImage;

    /**
     * 配置参数
     * actionStringResId和actionIconResId同时为0则不显示按钮
     */
    public static class PlaceHolder {
        @StringRes
        public int hintStringResId;
        @DrawableRes
        public int hintImageResId;
        @StringRes
        public int actionStringResId;
        @DrawableRes
        public int actionIconResId;
        @Nullable
        public OnClickListener actionOnClickListener;

        public PlaceHolder() {
        }

        public PlaceHolder(@StringRes int hintStringResId,
                           @DrawableRes int hintImageResId,
                           @StringRes int actionStringResId,
                           @DrawableRes int actionIconResId,
                           @Nullable OnClickListener actionOnClickListener) {
            this.hintStringResId = hintStringResId;
            this.hintImageResId = hintImageResId;
            this.actionStringResId = actionStringResId;
            this.actionIconResId = actionIconResId;
            this.actionOnClickListener = actionOnClickListener;
        }
    }

    public EmptyPlaceholderView(Context context) {
        super(context);
        init(null, 0);
    }

    public EmptyPlaceholderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EmptyPlaceholderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EmptyPlaceholderView, defStyle, 0);

        hintString = a.getString(R.styleable.EmptyPlaceholderView_hintString);

        if (a.hasValue(R.styleable.EmptyPlaceholderView_hintImage)) {
            hintImage = a.getDrawable(R.styleable.EmptyPlaceholderView_hintImage);
            hintImage.setCallback(this);
        }

        actionString = a.getString(R.styleable.EmptyPlaceholderView_actionString);

        if (a.hasValue(R.styleable.EmptyPlaceholderView_actionIcon)) {
            actionIcon = a.getDrawable(R.styleable.EmptyPlaceholderView_actionIcon);
            actionIcon.setCallback(this);
        }

        a.recycle();

        View.inflate(getContext(), R.layout.view_empty_placeholder, this);
        hintImageView = (ImageView) findViewById(R.id.image_hint);
        hintTextView = (TextView) findViewById(R.id.txt_hint);
        actionButton = findViewById(R.id.btn_action);
        actionButtonTxt = (TextView) findViewById(R.id.btn_action_txt);
        actionButtonImage = (ImageView) findViewById(R.id.btn_action_icon);

        setHintString(hintString);
        setHintImage(hintImage);
        setActionString(actionString);

        if (actionIcon != null) {
            setActionIcon(actionIcon);
        }
    }

    public void setPlaceHolder(PlaceHolder data) {
        setHintString(data.hintStringResId);
        setHintImage(data.hintImageResId);
        if (data.actionIconResId > 0) {
            setActionIcon(data.actionIconResId);
        }

        setActionString(data.actionStringResId);
        if (data.actionOnClickListener != null) {
            setActionClickedListener(data.actionOnClickListener);
        }
    }

    /**
     * 获取提示信息
     *
     * @return 提示信息
     */
    public String getHintString() {
        return hintString;
    }

    /**
     * 设置提示信息.
     *
     * @param hint 提示信息.
     */
    public void setHintString(String hint) {
        hintString = hint;
        hintTextView.setText(hintString);
    }

    /**
     * 设置提示信息.
     *
     * @param hintId 提示信息.
     */
    public void setHintString(@StringRes int hintId) {
        hintTextView.setText(hintId);
    }

    /**
     * 获取提示所用的图片
     *
     * @return 提示所用的图片
     */
    public Drawable getHintImage() {
        return hintImage;
    }

    /**
     * 设置提示所用的图片
     *
     * @param hintImage 提示所用的图片
     */
    public void setHintImage(Drawable hintImage) {
        this.hintImage = hintImage;
        hintImageView.setImageDrawable(hintImage);
    }

    /**
     * 设置提示所用的图片
     *
     * @param hintImageRes 提示所用的图片资源ID
     */
    public void setHintImage(@DrawableRes int hintImageRes) {
        hintImageView.setImageResource(hintImageRes);
    }

    /**
     * 获取用户操作按钮上的文字
     *
     * @return 操作按钮上的文字
     */
    public String getActionString() {
        return actionString;
    }

    /**
     * 设置用户操作按钮上的文字
     *
     * @param actionStringId 操作按钮上的文字资源ID
     */
    public void setActionString(@StringRes int actionStringId) {
        actionButton.setVisibility(actionStringId > 0 ? View.VISIBLE : View.GONE);
        actionButtonTxt.setVisibility(actionStringId > 0 ? View.VISIBLE : View.GONE);
        actionButtonTxt.setText(actionStringId);
    }

    /**
     * 设置用户操作按钮上的文字
     *
     * @param action 操作按钮上的文字
     */
    public void setActionString(String action) {
        actionString = action;
        actionButton.setVisibility(!TextUtils.isEmpty(actionString) ? View.VISIBLE : View.GONE);
        actionButtonTxt.setVisibility(!TextUtils.isEmpty(actionString) ? View.VISIBLE : View.GONE);
        actionButtonTxt.setText(actionString);
    }

    /**
     * 获取用户操作按钮上的图标
     *
     * @return 用户操作按钮上的图标
     */
    public Drawable getActionIcon() {
        return actionIcon;
    }

    /**
     * 设置用户操作按钮上的图标
     *
     * @param actionIcon 用户操作按钮上的图标
     */
    public void setActionIcon(Drawable actionIcon) {
        this.actionIcon = actionIcon;
        actionButton.setVisibility(View.VISIBLE);
        actionButtonImage.setVisibility(View.VISIBLE);
        actionButtonImage.setImageDrawable(this.actionIcon);
    }

    public void setActionButtonEnabled(boolean enabled) {
        actionButton.setEnabled(enabled);
    }

    public void setActionButtonVisibility(boolean visible) {
        actionButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setActionButtonIconVisibility(boolean visible) {
        actionButtonImage.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置用户操作按钮上的图标
     *
     * @param actionIconRes 设置用户操作按钮上的图标资源ID
     */
    public void setActionIcon(@DrawableRes int actionIconRes) {
        actionButtonImage.setVisibility(View.VISIBLE);
        actionButtonImage.setImageResource(actionIconRes);
    }

    /**
     * 设置用户操作按钮的点击回调
     * @param listener  点击回调
     */
    public void setActionClickedListener(OnClickListener listener) {
        actionButton.setOnClickListener(listener);
    }

    /**
     * 设置用户操作按钮的点击回调
     * @param listener  点击回调
     * @param actionButtonId 设置actionButtonId,为listener回调判断提供便利
     */
    public void setActionClickedListener(OnClickListener listener, int actionButtonId) {
        actionButton.setId(actionButtonId);
        actionButton.setOnClickListener(listener);
    }
}
