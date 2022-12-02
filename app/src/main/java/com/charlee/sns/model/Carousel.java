package com.charlee.sns.model;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;


import com.charlee.sns.data.CarouselResult;
import com.charlee.sns.manager.IAdBridge;
import com.charlee.sns.manager.SnsEnvController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * banner数据模型对象，一开始加载本地缓存数据，如果网络返回数据后，则忽视本地缓存数据
 */
public class Carousel {
    private static final int EXTERNAL_BROWSER = 1;
    private static final int DOWNLOAD = 2;
    private static final int WEB_VIEW = 3;
    private static final int OPEN_GOOGLE_PLAY = 4;
    private static final int AD_JUMP = 1;
    private static final int FUNCTION_JUMP = 2;

    // 服务器返回的列表
    private CarouselResult carouselResult;

    // 本地缓存列表
    private List<CacheItem> cacheItemList;

    public Carousel(CarouselResult carouselResult) {
        this.carouselResult = carouselResult;
    }

    public Carousel() {
        cacheItemList = new ArrayList<>();
    }

    public CarouselResult getCarouselResult() {
        // 如果是googleChannel需要过滤掉直接下载apk的广告类型
        if (SnsEnvController.isGooglePlayChannel) {
            List<CarouselResult.CarouselItem> itemList = carouselResult.getBannerItems();
            Iterator<CarouselResult.CarouselItem> itr = itemList.iterator();
            while (itr.hasNext()) {
                CarouselResult.CarouselItem item = itr.next();
                if (item.getOpenType() == DOWNLOAD) {
                    itr.remove();
                }
            }
        }
        return carouselResult;
    }

    public void setCarouselResult(CarouselResult result) {
        carouselResult = result;
    }

    /**
     * 如果服务器没有返回数据对象，则执行本地缓存对象的点击行为
     * @param context
     * @param position
     */
    public void onClick(Context context, int position) {
        if (carouselResult != null) {
            CarouselResult.CarouselItem item = carouselResult.getBannerItems().get(position);
            if (item.getJumpType() == AD_JUMP) {
                launchAdModule(context, item.getOpenType(), item.getTitle(), item.getOpenUrl());
            } else {
                launchFunctionModule(context, item.getUri());
            }
        } else {
            CacheItem item = cacheItemList.get(position);
            if (item.getUri() != null) {
                launchFunctionModule(context, item.getUri());
            }
        }
    }

    private void launchAdModule(Context context, int openType, String title, String url) {
        IAdBridge adBridge = SnsEnvController.getInstance().getAdBridge();
        if (adBridge == null) {
            return;
        }
        switch (openType) {
            case EXTERNAL_BROWSER:
                adBridge.openByBrowser(context, url);
                break;
            case DOWNLOAD:
                adBridge.openByDownloadDialog(context, title, url);
                break;
            case WEB_VIEW:
                adBridge.openByWebView(context, title, "", url);
                break;
            case OPEN_GOOGLE_PLAY:
                adBridge.openGooglePlayMarket(context, url);
                break;
            default:
                break;
        }
    }

    private void launchFunctionModule(Context context, String uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getCount() {
        if (carouselResult != null) {
            return carouselResult.getBannerItems().size();
        } else {
            return cacheItemList.size();
        }
    }

    public int getResId(int position) {
        if (carouselResult != null) {
            return 0;
        } else {
            return cacheItemList.get(position).getResId();
        }
    }

    public String getImgUrl(int position) {
        if (position > carouselResult.getBannerItems().size()) {
            return null;
        }
        return carouselResult.getBannerItems().get(position).getImageUrl();
    }

    public void addCacheItem(CacheItem cacheItem) {
        cacheItemList.add(cacheItem);
    }

    public static class CacheItem {
        private final int resId;
        private final String uri;

        public CacheItem(int resId, String uri) {
            this.resId = resId;
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }

        public int getResId() {
            return resId;
        }

    }

}
