package com.charlee.sns.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.charlee.sns.R;
import com.charlee.sns.helper.CounterDoubleClick;


/**
 * 使用此类需要定义的内容
 * top_bar_layout：定义整个bar的布局，包括leftview，titleview和rightview
 */
public class TopBarLayout extends RelativeLayout implements OnClickListener {
    private boolean mIsDark;
    private boolean mShowSeparator;
    private View mTopView;
    private View mLineSeparator;
    private Context mContext;
    private OnTitleClickListener mOnTitleClickListener;
    private OnBackClickListener mOnBackClickListener;

    private int mLeftType;
    public static final int LEFT_BLANK = 0;
    public static final int LEFT_BACK = 1;

    private LinearLayout mLeftViewContainer;
    private LinearLayout mRightViewContainer;
    private RelativeLayout mTitleViewContainer;

    private View mBackView;

    private View mLeftView;
    private View mRightView;
    private View mTitleView;

    private TextView mDefaultTitleView;

    public TopBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        initViews(attrs);
        initListeners();
    }

    public View createButton(int resid) {
        return createButton(resid, null);
    }

    public View createButton(int resid, ColorStateList colorStateList) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(R.layout.top_bar_button, null);
        TextView textView = (TextView) view.findViewById(R.id.top_btn_text);
        textView.setText(resid);
        if (colorStateList != null) {
            textView.setTextColor(colorStateList);
        }

        return view;
    }

    public View getLeftView() {
        return mLeftView != null ? mLeftView : mBackView;
    }

    public void setLeftView(View leftView) {
        mLeftViewContainer.removeAllViews();
        mLeftView = leftView;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mLeftViewContainer.addView(leftView, params);
    }

    public View getRightView() {
        return mRightView;
    }

    public void setRightView(View rightView) {
        mRightViewContainer.removeAllViews();
        mRightView = rightView;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mRightViewContainer.addView(rightView, params);
    }

    public void setTitle(int labelId) {
        if (labelId != 0) {
            mDefaultTitleView.setText(labelId);
        }

        mDefaultTitleView.setVisibility(labelId == 0 ? View.GONE : View.VISIBLE);
    }

    public void setTitle(String labelStr) {
        if (labelStr != null) {
            mDefaultTitleView.setText(labelStr);
        }

        mDefaultTitleView.setVisibility(labelStr == null ? View.GONE : View.VISIBLE);
    }

    public CharSequence getTitle() {
        return mDefaultTitleView.getText();
    }

    public void setTitleView(View titleView) {
        mTitleViewContainer.removeView(titleView);
        mTitleView = titleView;

        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mTitleViewContainer.addView(mTitleView, params);
    }

    public void setOnBackClickListener(OnBackClickListener listener) {
        mOnBackClickListener = listener;
    }

    public void setOnTitleClickListener(OnTitleClickListener listener) {
        mOnTitleClickListener = listener;
    }

    public void setUnderlineVisible(boolean visibility) {
        mLineSeparator.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
    }

    private void initViews(AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mTopView = inflater.inflate(R.layout.top_bar_layout, this);
        mLineSeparator = mTopView.findViewById(R.id.bottom_separator);

        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.TopBar);
        mLeftType = array.getInt(R.styleable.TopBar_left, 0);
        mIsDark = array.getBoolean(R.styleable.TopBar_color_dark, true);
        mShowSeparator = array.getBoolean(R.styleable.TopBar_showSeparator, true);

        mLineSeparator.setVisibility(mShowSeparator ? View.VISIBLE : View.GONE);

        mLeftViewContainer = (LinearLayout) mTopView.findViewById(R.id.top_left_view_container);
        mRightViewContainer = (LinearLayout) mTopView.findViewById(R.id.top_right_view_container);
        mTitleViewContainer = (RelativeLayout) mTopView.findViewById(R.id.top_title_view_container);

        mDefaultTitleView = (TextView) mTopView.findViewById(R.id.top_btn_title);
        int resid = array.getResourceId(R.styleable.TopBar_titleText, 0);
        if (resid > 0) {
            mDefaultTitleView.setText(resid);
        }
        mDefaultTitleView.setTextAppearance(mContext, R.style.top_bar_title_txt);
        mDefaultTitleView.setOnClickListener(this);

        if (mTopView.getBackground() == null) {
            if (mIsDark) {
                mTopView.setBackgroundResource(R.color.top_bar_background_white);
            } else {
                mTopView.setBackgroundResource(R.color.top_bar_background_orange);
            }
        }

        int resId = 0;
        switch (mLeftType) {
            case LEFT_BLANK:
                break;
            case LEFT_BACK:
                resId = array.getResourceId(R.styleable.TopBar_leftText, 0);
                initBackView(inflater, resId);
                break;
            default:
                break;
        }

        array.recycle();
    }

    private void initBackView(LayoutInflater inflater, int textResId) {
        mBackView = inflater.inflate(R.layout.top_bar_back_button, mLeftViewContainer);
        TextView tv = (TextView) mBackView.findViewById(R.id.top_btn_text);
        if (textResId > 0) {
            tv.setText(textResId);
        }
        mBackView.setOnClickListener(this);
    }

    private void initListeners() {
        if (mBackView != null) {
            mBackView.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (CounterDoubleClick.handle()) {
            return;
        }

        if (v == mBackView && mOnBackClickListener != null) {
            mOnBackClickListener.onBack();
            return;
        }

        if (v == mDefaultTitleView && mOnTitleClickListener != null) {
            mOnTitleClickListener.onTitleClick();
            return;
        }

    }

    public interface OnBackClickListener {
        void onBack();
    }

    public interface OnTitleClickListener {
        void onTitleClick();
    }

}
