package com.charlee.sns.player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.charlee.sns.R;
import com.charlee.sns.adapter.FullMessageViewHolder;
import com.charlee.sns.widget.VideoPlayerRendererView;
import com.google.android.exoplayer2.ExoPlayer;

import java.util.HashMap;

/**
 * 视频播放器
 * 1，为简化播放流程，使用者只需要提供视频地址url以及渲染输出的view对象
 * 2，播放器内部处理暂停、静音等逻辑
 * 3，一个播放器对应多个视频资源，切换视频时完全释放当前正在播放的视频
 */
public class VideoPlayer implements View.OnClickListener {
    private static final String LOG_TAG = "VideoPlayer";


    // 播放视频的容器
    private ViewGroup rendererContainer;
    FullMessageViewHolder holder;

    // 播放视频的渲染对象
    private VideoPlayerRendererView rendererView;
    private TextureView textureView;
    private Surface rendererSurface;
    private ImageView imgVoiceSwitch;

    // 是否静音
    private boolean isVoiceMuted;

    // 标记对象
    private Object tagObject;

    private static VideoPlayer instance;

    public static VideoPlayer getInstance(Context context) {
        if (instance == null) {
            instance = new VideoPlayer(context);
        }
        return instance;
    }

    private VideoPlayer(Context context) {
        initialize(context);
    }

    private void initialize(Context context) {
        rendererView = new VideoPlayerRendererView(context);

        textureView = rendererView.getTextureView();
        textureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        textureView.setSurfaceTextureListener(onSurfaceTextureListener);

        imgVoiceSwitch = rendererView.getVoiceSwitchView();
        imgVoiceSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                playerWrapper.setMute(!isVoiceMuted);
                isVoiceMuted = !isVoiceMuted;
                imgVoiceSwitch.setImageResource(isVoiceMuted ? R.drawable.ic_voice_mute : R.drawable.ic_voice);
                if (holder != null) {
                    holder.setMuted(isVoiceMuted);
                }
            }
        });
    }

    public void startPlayer(ViewGroup container) {
        rendererContainer = container;

        removeRendererView();
        addRendererView();

        if (rendererSurface != null) {
            playVideo();
        }
    }

    public void stopPlayer() {
        releasePlayer();
        instance = null;
    }

    public void setTag(Object object) {
        tagObject = object;
    }

    public Object getTag() {
        return tagObject;
    }

    private void releasePlayer() {
        removeRendererView();
//        playerWrapper.release();
    }

    private void addRendererView() {
        imgVoiceSwitch.setImageResource(R.drawable.ic_voice);
        rendererContainer.addView(rendererView);
    }

    private void removeRendererView() {
        ViewGroup parent = (ViewGroup) rendererView.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(rendererView);
        if (index >= 0) {
            parent.removeViewAt(index);
        }
    }

    private void updateVoiceStatus() {
        holder = (FullMessageViewHolder) getTag();
        if (holder != null) {
            isVoiceMuted = holder.isMuted();
        }
        imgVoiceSwitch.setImageResource(isVoiceMuted ? R.drawable.ic_voice_mute : R.drawable.ic_voice);
    }


    private void playVideo() {
//        if (rendererSurface != null && playerWrapper != null) {
//            playerWrapper.setSurface(rendererSurface);
//            playerWrapper.setPlayWhenReady(true);
//        }
        updateVoiceStatus();
    }

    @Override
    public void onClick(View v) {
//        playerWrapper.setPlayWhenReady(!playerWrapper.getPlayWhenReady());
    }


    private TextureView.SurfaceTextureListener onSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            rendererSurface = new Surface(surface);
            playVideo();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

}
