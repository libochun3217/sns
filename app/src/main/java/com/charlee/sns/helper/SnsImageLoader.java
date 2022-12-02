package com.charlee.sns.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.R;
import com.charlee.sns.model.Constants;
import com.charlee.sns.model.SnsImage;
import com.charlee.sns.widget.ProgressCircleDrawable;
import com.facebook.animated.gif.BuildConfig;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.decoder.ProgressiveJpegConfig;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.ImmutableQualityInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;

import bolts.Task;
import bolts.TaskCompletionSource;
import okhttp3.OkHttpClient;

/**
 * 图片加载器。封装社区图片加载的逻辑。
 */
public class SnsImageLoader {

    private static final String TEMP_IMAGE_NAME = "share_temp";

    private static boolean initialized;

    private static DisplayMetrics displayMetrics;

    // 宽度阈值，设置为屏幕宽度一半。
    // 图片宽度小于此阈值使用smallPhotoOptions，大于此阈值则使用largePhotoOptions
    private static int widthThreshold;

    // 硬件加速（OpenGL ES）允许的最大宽高。（超过尺寸的图片会被缩小显示。）
    private static int maxImageSize = 0;

    // 圆形加载进度图大小
    private static int progressCircleSize;

    private static DisplayImageOptions smallAvatarOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .displayer(new CircleBitmapDisplayer())
            .build();

    private static DisplayImageOptions bigAvatarOptions = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.ic_user_portrait_default_big)
            .showImageOnLoading(R.drawable.ic_user_portrait_default_big)
            .showImageOnFail(R.drawable.ic_user_portrait_default_big)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new CircleBitmapDisplayer())
            .build();

    private static class SnsFrescoControllerListener extends BaseControllerListener<ImageInfo> {
        private final SnsImage snsImage;
        private final String imageUri;
        private final View view;
        private final Context context;
        private final ImageLoadingListener callback;

        public SnsFrescoControllerListener(@NonNull SnsImage snsImage,
                                           @NonNull String imageUri,
                                           @NonNull View view,
                                           @Nullable ImageLoadingListener callback) {
            this.snsImage = snsImage;
            this.imageUri = imageUri;
            this.view = view;
            this.context = view.getContext();
            this.callback = callback;
        }

        @Override
        public void onSubmit(String id, Object o) {
            if (callback != null) {
                callback.onLoadingStarted(imageUri, view);
            }
        }

        @Override
        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo,
                                    @Nullable Animatable animatable) {
            if (callback != null) {
                callback.onLoadingComplete(imageUri, view);
            }
        }

        @Override
        public void onFailure(String id, Throwable throwable) {
            if (callback != null) {
                callback.onLoadingFailed(imageUri, view);
            }

            snsImage.removeFailedUrl(imageUri);

            NetworkMonitor networkMonitor = NetworkMonitor.getInstance(context);
            if (networkMonitor.isNetworkAvailable()) {
                String country = context.getResources().getConfiguration().locale.getCountry();
                String label = EventConstant.LABEL_IMAGE_DOWNLOAD_FALIURE;
                if (throwable instanceof ConnectException) {
                    label = EventConstant.LABEL_IMAGE_FALIURE_CONNECTION;
                } else if (throwable instanceof SocketTimeoutException) {
                    label = EventConstant.LABEL_IMAGE_FALIURE_TIMEOUT;
                }

                label += EventConstant.SEPARATOR + country + EventConstant.SEPARATOR
                        + networkMonitor.getNetTypeString();
            }
        }

        @Override
        public void onRelease(String id) {
            if (callback != null) {
                callback.onLoadingCancelled(imageUri, view);
            }
        }
    }

    private static class AvatarImageLoader extends ImageLoader {
        private static AvatarImageLoader instance;

        protected AvatarImageLoader() {
            super();
        }

        public static AvatarImageLoader getInstance() {
            if (instance == null) {
                synchronized (AvatarImageLoader.class) {
                    if (instance == null) {
                        instance = new AvatarImageLoader();
                    }
                }
            }

            return instance;
        }
    }

    public static class Size {
        public int width;
        public int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public static class ImageLoadingListener {
        public void onLoadingStarted(String imageUri, View view) {
        }

        public void onLoadingFailed(String imageUri, View view) {
        }

        public void onLoadingComplete(String imageUri, View view) {
        }

        public void onLoadingCancelled(String imageUri, View view) {
        }
    }

    public static void init(Context context) {
        if (!initialized) {
            Context appContext = context.getApplicationContext();

            Resources resources = appContext.getResources();
            displayMetrics = resources.getDisplayMetrics();
            widthThreshold = displayMetrics.widthPixels / 2;

            initMaxImageSize();
            SnsImage.init(maxImageSize, displayMetrics.density);

            progressCircleSize = resources.getDimensionPixelSize(R.dimen.sns_circle_progress);

            // 初始化头像ImageLoader
            ImageLoaderConfiguration configForAvatar = new ImageLoaderConfiguration.Builder(context)
                    .diskCacheSize(Constants.AVATAR_IMAGE_DISK_CACHE_MAX_SIZE)
                    .build();
            AvatarImageLoader.getInstance().init(configForAvatar);

            // 初始化Fresco：
            // 1. 配置对渐进式JPEG的支持
            ProgressiveJpegConfig pJpegConfig = new ProgressiveJpegConfig() {
                @Override
                public int getNextScanNumberToDecode(int scanNumber) {
                    return scanNumber + Constants.PROGRESSIVE_JPEG_DECODE_STEP;
                }

                @Override
                public QualityInfo getQualityInfo(int scanNumber) {
                    boolean isGoodEnough = (scanNumber >= Constants.PROGRESSIVE_JPEG_QUALITY_THRESHOLD);
                    return ImmutableQualityInfo.of(scanNumber, isGoodEnough, false);
                }
            };

            // 2. 配置使用OkHttpClient
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(Constants.IMAGE_LOAD_TIMEOUT, TimeUnit.SECONDS)
                    .build();

            ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
                    .newBuilder(appContext, okHttpClient)
                    .setProgressiveJpegConfig(pJpegConfig)
                    .setMainDiskCacheConfig(DiskCacheConfig.newBuilder(context).build())
                    .setSmallImageDiskCacheConfig(DiskCacheConfig.newBuilder(context).build())
                    .build();
            Fresco.initialize(appContext, config);

            initialized = true;
        }
    }

    public static Size getSnsImageSizeForDisplay(SnsImage snsImage, int controlWidth) {
        int width = controlWidth;
        int height;
        // 由于我们前面计算出来的值可能大于硬件加速允许的最大值，所以我们要计算下height是否
        // 超过了允许的最大值。
        float imageAspectRatio = snsImage.getAspectRatio();
        int maxHeight = maxImageSize;
        if (maxHeight > snsImage.getHeight()) {
            maxHeight = snsImage.getHeight();
        }
        if (imageAspectRatio > Constants.UI_MAX_IMG_ASPECT_RATIO) {
            height = (int) (controlWidth * Constants.UI_MAX_IMG_ASPECT_RATIO);
            if (height > maxHeight) {
                height = maxHeight;
            }

            width = (int) (height / imageAspectRatio);
        } else {
            height = (int) (controlWidth * imageAspectRatio);
        }

        return new Size(width, height);
    }

    /**
     * 加载用户头像
     *
     * @param avatarUrl 头像图片URL
     * @param imageView 需要加载头像图片的ImageView
     * @param isBig     是否大头像图片(小图缺省35x35DIP，大图缺省100x100DIP)
     */
    public static void loadAvatar(@NonNull final String avatarUrl, @NonNull final ImageView imageView, boolean isBig) {
        AvatarImageLoader.getInstance().displayImage(
                avatarUrl, imageView, isBig ? bigAvatarOptions : smallAvatarOptions);
    }

    /**
     * 加载图片，仅适用于预先无法确定图片宽度的情况。
     * 如果能获取图片宽度则会调用loadImage(snsImage, imageView, width)。
     *
     * @param snsImage  图片对象
     * @param imageView 需要加载图片的ImageView
     */
    public static void loadImage(@NonNull final SnsImage snsImage, @NonNull final ImageView imageView) {
        int width = imageView.getWidth();
        if (width > 0) {
            loadImage(snsImage, imageView, width, 0, null);
        } else {
            ViewTreeObserver vtObserver = imageView.getViewTreeObserver();
            vtObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = imageView.getWidth();
                    loadImage(snsImage, imageView, width, 0, null);

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    /**
     * 根据图片宽度加载图片
     * 注意：
     * 1. 方法内会根据图片的高宽比设置ImageView的高度和宽度
     * 2. 图片的高宽比最大值为 {@link Constants#UI_MAX_IMG_ASPECT_RATIO}, 超过这个比例的图片
     * 会缩小到根据这个比例计算出的高度。
     * 3. 方法内会使用服务器提供的图片缩放功能获取等比例缩放后的图片而不是原始图片
     *
     * @param snsImage          图片对象
     * @param imageView         需要加载图片的ImageView
     * @param width             图片位置的宽度
     * @param imageHeightAdjust 图片高度的调整值(瀑布流通过SpacesItemDecoration控制间距时需要Layout高度加上这个间距才不会被裁剪)
     */
    public static void loadImage(@NonNull SnsImage snsImage, @NonNull final ImageView imageView,
                                 int width, int imageHeightAdjust) {
        loadImage(snsImage, imageView, width, imageHeightAdjust, null);
    }

    /**
     * 根据图片宽度加载图片
     * 注意：
     * 1. 方法内会根据图片的高宽比设置ImageView的高度和宽度
     * 2. 图片的高宽比最大值为 {@link Constants#UI_MAX_IMG_ASPECT_RATIO}, 超过这个比例的图片
     * 会缩小到根据这个比例计算出的高度。
     * 3. 方法内会使用服务器提供的图片缩放功能获取等比例缩放后的图片而不是原始图片
     *
     * @param snsImage          图片对象
     * @param imageView         需要加载图片的ImageView
     * @param controlWidth      图片位置的宽度
     * @param imageHeightAdjust 图片高度的调整值(瀑布流通过SpacesItemDecoration控制间距时需要Layout高度加上这个间距才不会被裁剪)
     * @param callback          加载进度回调
     */
    public static void loadImage(@NonNull SnsImage snsImage, @NonNull final ImageView imageView,
                                 int controlWidth, int imageHeightAdjust,
                                 @Nullable ImageLoadingListener callback) {
        Size size = getSnsImageSizeForDisplay(snsImage, controlWidth);
        loadImage(snsImage, imageView, controlWidth, size.width, size.height, imageHeightAdjust, callback);
    }

    /**
     * 根据图片宽度加载图片
     * 注意：
     * 1. 方法内会根据输入参数设置ImageView的高度和宽度
     * 2. 方法内会使用服务器提供的图片缩放功能获取按照指定宽度等比例缩放后的图片而不是原始图片
     *
     * @param snsImage          图片对象
     * @param imageView         需要加载图片的ImageView
     * @param controlWidth      图片位置的宽度
     * @param imageWidth        图片的宽度
     * @param height            图片位置的高度(也是图片的高度)
     * @param imageHeightAdjust 图片高度的调整值(瀑布流通过SpacesItemDecoration控制间距时需要Layout高度加上这个间距才不会被裁剪)
     * @param callback          加载进度回调
     */
    private static void loadImage(@NonNull SnsImage snsImage, @NonNull final ImageView imageView,
                                  int controlWidth, int imageWidth, int height, int imageHeightAdjust,
                                  @Nullable ImageLoadingListener callback) {
        int imageViewWidth = imageView.getWidth();
        int imageViewHeight = imageView.getHeight();
        if (imageViewWidth != controlWidth || imageViewHeight != height) {
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.width = controlWidth;
            layoutParams.height = height + imageHeightAdjust;
            imageView.setLayoutParams(layoutParams);
            imageView.invalidate();
        }

        boolean isLargeImageView = controlWidth > widthThreshold;
        String lowResImageUrl = null;
        if (isLargeImageView) {
            lowResImageUrl = snsImage.getLowResUrl(imageWidth);
        }

        boolean isAnimatable = isLargeImageView && snsImage.isAnimatable();

        String imageUrl = isAnimatable ? snsImage.getUrl() : snsImage.getUrl(imageWidth, isLargeImageView);

        if (imageView instanceof SimpleDraweeView) {
            SimpleDraweeView draweeView = (SimpleDraweeView) imageView;
            GenericDraweeHierarchy hierarchy = draweeView.getHierarchy();
            hierarchy.setActualImageScaleType(imageWidth < controlWidth ? ScalingUtils.ScaleType.CENTER_INSIDE :
                    ScalingUtils.ScaleType.CENTER_CROP);
            hierarchy.setFailureImage(imageView.getResources().getDrawable(R.drawable.ic_stub_failed),
                    ScalingUtils.ScaleType.CENTER);

            // Add progress for large image
            if (isLargeImageView && height > progressCircleSize) {
                ProgressCircleDrawable progressCircleDrawable = new ProgressCircleDrawable();
                progressCircleDrawable.setSize(progressCircleSize);
                hierarchy.setProgressBarImage(progressCircleDrawable);
            }

            ImageRequest.CacheChoice choice =
                    imageWidth > widthThreshold ? ImageRequest.CacheChoice.DEFAULT : ImageRequest.CacheChoice.SMALL;
            ImageRequestBuilder requestBuilder = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(imageUrl))
                    .setProgressiveRenderingEnabled(true)
                    .setCacheChoice(choice);

            ImageRequest request = requestBuilder.build();
            PipelineDraweeControllerBuilder controllerBuilder = Fresco.newDraweeControllerBuilder()
                    .setControllerListener(new SnsFrescoControllerListener(snsImage, imageUrl, imageView, callback))
                    .setImageRequest(request)
                    .setOldController(draweeView.getController());

            if (isAnimatable) {
                controllerBuilder.setAutoPlayAnimations(true);
            }

            if (lowResImageUrl != null) {
                ImageRequest lowResRequest = ImageRequestBuilder
                        .newBuilderWithSource(Uri.parse(lowResImageUrl))
                        .setCacheChoice(ImageRequest.CacheChoice.SMALL).build();

                controllerBuilder.setLowResImageRequest(lowResRequest);
            }

            DraweeController controller = controllerBuilder.build();
            draweeView.setController(controller);

        } else if (BuildConfig.DEBUG) {
            throw new IllegalArgumentException(
                    "Please use Fresco's SimpleDraweeView instead of ImageView to display image.");
        }
    }

    /**
     * 加载正方形图片。注意：此方法仅适用于存储在BOS服务器上的图片！！！！！
     *
     * @param snsImage  图片对象
     * @param imageView 需要加载图片的ImageView
     * @param size      图片大小
     */
    public static void loadSquareImage(@NonNull SnsImage snsImage, @NonNull final ImageView imageView, int size) {
        if (snsImage == null) {
            return;
        }

        // 不要删除！检查图片宽度，避免布局和代码修改不同步导致的宽度不一致
        if (BuildConfig.DEBUG) {
            int width = imageView.getWidth();
            if (size == 0 || width > 0 && (size != width || size != imageView.getHeight())) {
                throw new InvalidParameterException("Incorrect image size or not a square image!");
            }
        }

        String scaledImageUrl = snsImage.getSquareUrl(size);

        if (imageView instanceof SimpleDraweeView) {
            SimpleDraweeView draweeView = (SimpleDraweeView) imageView;
            GenericDraweeHierarchy hierarchy = draweeView.getHierarchy();
            hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            hierarchy.setFailureImage(imageView.getResources().getDrawable(R.drawable.ic_stub_failed),
                    ScalingUtils.ScaleType.CENTER);

            ImageRequest request = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(scaledImageUrl))
                    .setProgressiveRenderingEnabled(true)
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(draweeView.getController())
                    .build();

            draweeView.setController(controller);
        } else if (BuildConfig.DEBUG) {
            throw new IllegalArgumentException(
                    "Please use Fresco's SimpleDraweeView instead of ImageView to display image.");
        }
    }

    public static void preloadImage(@NonNull Context context, @NonNull String imageUrl,
                                    @Nullable final SimpleImageLoadingListener callback) {
        ImageRequest request = ImageRequest.fromUri(imageUrl);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<Void> dataSource = imagePipeline.prefetchToDiskCache(request, null);
        dataSource.subscribe(new BaseDataSubscriber<Void>() {
            @Override
            public void onNewResultImpl(DataSource<Void> dataSource) {
                if (callback != null) {
                    callback.onLoadingComplete(null, null, null);
                }
            }

            @Override
            public void onFailureImpl(DataSource<Void> dataSource) {
                if (callback != null) {
                    callback.onLoadingFailed(null, null, null);
                }
            }
        }, AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 获取磁盘缓存的图片路径
     *
     * @param imageUri 图片URL
     * @return 磁盘缓存的图片路径
     */
    @NonNull
    public static Task<Boolean> isImageInDiskCache(@NonNull String imageUri) {
        final TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.isInDiskCache(Uri.parse(imageUri)).subscribe(new BaseDataSubscriber<Boolean>() {
            @Override
            public void onNewResultImpl(DataSource<Boolean> dataSource) {
                boolean failed = dataSource.hasFailed() || !dataSource.hasResult();
                if (!failed) {
                    failed = dataSource.getResult();
                }

                if (failed) {
                    tcs.setResult(false);
                } else {
                    tcs.setResult(true);
                }
            }

            @Override
            public void onFailureImpl(DataSource<Boolean> dataSource) {
                Throwable error = dataSource.getFailureCause();
                if (error instanceof Exception) {
                    tcs.setError((Exception) error);
                } else {
                    tcs.setError(new Exception(error != null ? error.getMessage() : ""));
                }
            }
        }, AsyncTask.THREAD_POOL_EXECUTOR);

        return tcs.getTask();
    }

    /**
     * 创建磁盘缓存中图片的缩略图并返回Uri
     *
     * @param context  Context
     * @param imageUri 已经加载过的图片URL
     * @return 返回缩略图Uri的Task
     */
    public static Task<Uri> createThumbnailForCachedImage(@NonNull final Context context,
                                                          @NonNull final String imageUri) {
        final TaskCompletionSource<Uri> tcs = new TaskCompletionSource<>();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.fetchDecodedImage(ImageRequest.fromUri(imageUri), new Object())
                .subscribe(new BaseBitmapDataSubscriber() {
                    @Override
                    public void onNewResultImpl(@Nullable Bitmap bitmap) {
                        if (bitmap != null) {
                            try {
                                Uri thumbUri = createThumbnailImageFile(context, bitmap);
                                tcs.setResult(thumbUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                                tcs.setError(e);
                            }
                        }
                    }

                    @Override
                    public void onFailureImpl(DataSource dataSource) {
                        Throwable error = dataSource.getFailureCause();
                        if (error instanceof Exception) {
                            tcs.setError((Exception) error);
                        } else {
                            tcs.setError(new Exception(error != null ? error.getMessage() : ""));
                        }
                    }
                }, AsyncTask.THREAD_POOL_EXECUTOR);

        return tcs.getTask();
    }

    private static Uri createThumbnailImageFile(@NonNull Context context, @NonNull Bitmap bitmap) throws IOException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = 1.0f;
        if (Constants.THUMB_SIZE < width) {
            scale = (float) Constants.THUMB_SIZE / width;
        }

        if (scale * height > Constants.THUMB_SIZE) {
            scale = (float) Constants.THUMB_SIZE / height;
        }

        width = (int) (scale * width);
        height = (int) (scale * height);

        Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, width, height);

        File outputFile = StorageUtils.createTempShareFile();
        OutputStream outStream = new FileOutputStream(outputFile);
        thumb.compress(Bitmap.CompressFormat.JPEG, Constants.THUMB_QUALITY, outStream);
        outStream.flush();
        outStream.close();

        return Uri.fromFile(outputFile);
    }

    public static Uri createPortraitImageFile(@NonNull Context context, @NonNull Bitmap bitmap) throws IOException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = 1.0f;
        if (Constants.PORTRAIT_SIZE < width) {
            scale = (float) Constants.PORTRAIT_SIZE / width;
        }

        if (scale * height > Constants.PORTRAIT_SIZE) {
            scale = (float) Constants.PORTRAIT_SIZE / height;
        }

        width = (int) (scale * width);
        height = (int) (scale * height);

        Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, width, height);

        File outputFile = StorageUtils.createPortraitTempFile();
        OutputStream outStream = new FileOutputStream(outputFile);
        thumb.compress(Bitmap.CompressFormat.JPEG, Constants.THUMB_QUALITY, outStream);
        outStream.flush();
        outStream.close();

        return Uri.fromFile(outputFile);
    }

    /**
     * 计算硬件加速（OpenGL ES）允许的最大宽高。（超过尺寸的图片会被缩小显示。）
     *
     * @return
     */
    private static void initMaxImageSize() {
        int[] maxTextureSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        maxImageSize = Math.max(maxTextureSize[0], 2048);
    }
}
