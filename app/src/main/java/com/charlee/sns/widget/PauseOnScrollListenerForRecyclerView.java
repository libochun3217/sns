package com.charlee.sns.widget;


import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 */
public class PauseOnScrollListenerForRecyclerView extends RecyclerView.OnScrollListener {
    private ImageLoader imageLoader;
    private final boolean pauseOnScroll;
    private final boolean pauseOnFling;
    private final RecyclerView.OnScrollListener externalListener;

    public PauseOnScrollListenerForRecyclerView(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
        this(imageLoader, pauseOnScroll, pauseOnFling, null);
    }

    public PauseOnScrollListenerForRecyclerView(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling,
                                                RecyclerView.OnScrollListener customListener) {
        this.imageLoader = imageLoader;
        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnFling = pauseOnFling;
        this.externalListener = customListener;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        switch (newState) {
            case 0:
                this.imageLoader.resume();
                break;
            case 1:
                if (this.pauseOnScroll) {
                    this.imageLoader.pause();
                }
                break;
            case 2:
                if (this.pauseOnFling) {
                    this.imageLoader.pause();
                }
                break;
            default:
                break;
        }

        if (this.externalListener != null) {
            this.externalListener.onScrollStateChanged(recyclerView, newState);
        }

    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (this.externalListener != null) {
            this.externalListener.onScrolled(recyclerView, dx, dy);
        }

    }
}
