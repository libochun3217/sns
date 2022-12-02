package com.charlee.sns.data;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 */
public class CarouselResult extends ResultBase {
    public static class CarouselItem {
        private final String title;
        private final String img;
        private final int jumpType;
        private final int openType;
        private final String openUrl;
        private final String uri;

        public CarouselItem(@Nullable String title,
                            @NonNull String imageUrl,
                            int jumpType,
                            int openType,
                            String openUrl,
                            String uri
        ) {
            this.title = title;
            this.img = imageUrl;
            this.jumpType = jumpType;
            this.openType = openType;
            this.openUrl = openUrl;
            this.uri = uri;
        }

        @Nullable
        public String getTitle() {
            return title;
        }

        @NonNull
        public String getImageUrl() {
            return img;
        }

        public int getJumpType() {
            return jumpType;
        }

        public int getOpenType() {
            return openType;
        }

        public String getOpenUrl() {
            return openUrl;
        }

        public String getUri() {
            return uri;
        }
    }

    private final List<CarouselItem> carousels;

    public CarouselResult(int errCode,
                          @Nullable String errMsg,
                          Boolean hasMore,
                          List<CarouselItem> carouselItems) {
        super(errCode, errMsg, hasMore);
        this.carousels = carouselItems;
    }

    public List<CarouselItem> getBannerItems() {
        return carousels;
    }

}
