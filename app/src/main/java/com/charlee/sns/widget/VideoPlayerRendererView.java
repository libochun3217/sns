package com.charlee.sns.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.charlee.sns.R;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;


public class VideoPlayerRendererView extends FrameLayout implements View.OnClickListener {
    private static final String LOG_TAG = "VideoPlayerRendererView";

    private AspectRatioFrameLayout ratioFragmentLayout;
    private TextureView videoTextureView;
    private ImageView imgVoiceSwitch;

    public VideoPlayerRendererView(Context context) {
        super(context);
        initialize(context);
    }

    public VideoPlayerRendererView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public VideoPlayerRendererView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public TextureView getTextureView() {
        return videoTextureView;
    }

    public ImageView getVoiceSwitchView() {
        return imgVoiceSwitch;
    }

    private void initialize(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ratioFragmentLayout = (AspectRatioFrameLayout) inflater.inflate(R.layout.layout_video_surface_view, null);
        addView(ratioFragmentLayout);

        videoTextureView = (TextureView) findViewById(R.id.video_surface_view);
        imgVoiceSwitch = (ImageView) findViewById(R.id.img_voice_switch);

    }

    public void setAspectRatio(float widthHeightRatio) {
        ratioFragmentLayout.setAspectRatio(widthHeightRatio);
    }

    @Override
    public void onClick(View v) {

    }

}
