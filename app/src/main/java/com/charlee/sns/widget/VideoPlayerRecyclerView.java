package com.charlee.sns.widget;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.adapter.FullMessageViewHolder;
import com.charlee.sns.player.VideoPlayer;


public class VideoPlayerRecyclerView extends RecyclerView {
    private int videoDefaultHeight = 0;
    private int screenDefaultHeight = 0;

    private VideoPlayer videoPlayer;

    /**
     * 当前播放的视图位置
     */
    private int playPosition = -1;

    public VideoPlayerRecyclerView(Context context) {
        super(context);
        initialize(context);
    }

    public VideoPlayerRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public VideoPlayerRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void playVideo() {
        int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

        if (endPosition - startPosition > 1) {
            endPosition = startPosition + 1;
        }

        if (startPosition < 0 || endPosition < 0) {
            return;
        }

        int targetPosition;
        if (startPosition != endPosition) {
            int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
            int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);
            targetPosition = startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
        } else {
            targetPosition = startPosition;
        }

        if (targetPosition < 0 || targetPosition == playPosition) {
            return;
        }
        playPosition = targetPosition;

        int at = targetPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        FullMessageViewHolder holder = getHolder(at);
        if (holder != null && videoPlayer.getTag() == holder) {
            return;
        } else {
            videoPlayer.stopPlayer();
            videoPlayer.setTag(null);
        }

        if (holder == null || holder.getUserMessage().getVideo() == null) {
            playPosition = -1;
            return;
        }

        videoPlayer.setTag(holder);
//        videoPlayer.startPlayer(holder.getRenderBuilder(), holder.getVideoContainer());
    }

    public void playVideo(FullMessageViewHolder holder) {
        if (holder == null) {
            return;
        }

        if (holder.getUserMessage().getVideo() == null) {
            return;
        }

        playPosition = 0;
        videoPlayer.stopPlayer();
        videoPlayer.setTag(null);

        videoPlayer.setTag(holder);
//        videoPlayer.startPlayer(holder.getRenderBuilder(), holder.getVideoContainer());
    }

    private int getVisibleVideoSurfaceHeight(int playPosition) {
        int at = playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }

        int videoHeight;
        FullMessageViewHolder holder = getHolder(at);
        if (holder != null && holder.getUserMessage().getVideo() != null) {
            videoHeight = holder.getUserMessage().getVideo().getHeight();
        } else {
            videoHeight = videoDefaultHeight;
        }

        int[] location01 = new int[2];
        child.getLocationInWindow(location01);
        if (location01[1] < 0) {
            return location01[1] + videoHeight;
        } else {
            return screenDefaultHeight - location01[1];
        }
    }

    private FullMessageViewHolder getHolder(int pos) {
        View child = getChildAt(pos);
        if (child == null) {
            return null;
        }

        if (child.getTag() instanceof FullMessageViewHolder) {
            return (FullMessageViewHolder) child.getTag();
        }

        return null;
    }

    private void initialize(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        videoDefaultHeight = point.x;
        screenDefaultHeight = point.y;

        videoPlayer = VideoPlayer.getInstance(context);

        // 滑动的时候保持播放，但是当播放窗口滑出界面时停止播放
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                playVideo();
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                if (view.getTag() instanceof FullMessageViewHolder) {
                    FullMessageViewHolder holder = (FullMessageViewHolder) view.getTag();
                    if (playPosition == -1 && holder != null) {
                        playVideo(holder);
                    }
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                // 处理被删除的情况
                if (view.getTag() instanceof FullMessageViewHolder) {
                    FullMessageViewHolder holder = (FullMessageViewHolder) view.getTag();
                    if (playPosition != -1 && holder != null && videoPlayer.getTag() == holder) {
                        onPausePlayer();
                    }
                }
            }
        });
    }

    public void onStartPlayer() {
        playVideo();
    }

    public void onPausePlayer() {
        playPosition = -1;
        videoPlayer.stopPlayer();
        videoPlayer.setTag(null);
    }

    public void onRestartPlayer() {
        playVideo();
    }

}
