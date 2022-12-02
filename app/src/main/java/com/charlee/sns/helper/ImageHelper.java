package com.charlee.sns.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.charlee.sns.model.Constants;

import bolts.Task;

/**
 */
public class ImageHelper {

    private static final String TEMP_IMAGE_NAME = "share_temp";

    /**
     * 创建符合社区分享条件的图片
     * 如果图片文件大小小于5M，则直接返回原图，否则压缩图片
     *
     * @param context  Context
     * @param imageUri 原图URI
     *
     * @return 返回图片Uri的Task
     */
    public static Task<Uri> createImageForShare(@NonNull final Context context,
                                                @NonNull final String imageUri) {
        return Task.callInBackground(new Callable<Uri>() {
            @Override
            public Uri call() throws Exception {
                return createImageByLimitedSize(context, imageUri);
            }
        });
    }

    private static Uri createImageByLimitedSize(@NonNull final Context context,
                                                @NonNull final String imageUri) {
        Bitmap srcBitmap = null;

        try {
            // 获取图片大小
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(Uri.parse(imageUri).getPath(), options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;

            // 检查是否需要旋转
            int angle = getExifOrientation(Uri.parse(imageUri).getPath());

            // 检查图片大小
            int size = imageHeight * imageWidth * 4;
            if (size < Constants.IMAGE_LIMITED_SIZE) {
                if (angle != 0) {
                    srcBitmap = BitmapFactory.decodeFile(Uri.parse(imageUri).getPath());
                    return saveImage(context, srcBitmap, angle);
                }
                return Uri.parse(imageUri);
            }

            // 创建图像
            int sampleSize = getSampleSize(options);
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            srcBitmap = BitmapFactory.decodeFile(Uri.parse(imageUri).getPath(), options);
            Uri retUri = saveImage(context, srcBitmap, angle);
            srcBitmap.recycle();
            return retUri;

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return Uri.parse(imageUri);
    }

    private static int getSampleSize(BitmapFactory.Options options) {
        // 根据图片大小计算缩放比例
        int size = options.outWidth * options.outHeight * 4;
        float ratio = (float) Constants.IMAGE_LIMITED_SIZE / size;

        // 初步计算缩放后的图片尺寸
        int width = (int) (options.outWidth * ratio);
        int height = (int) (options.outHeight * ratio);

        // 适当的进行阈值调整
        float threshold = 0.9f;
        width = (int) (width * threshold);
        height = (int) (height * threshold);

        return calculateInSampleSize(options, width, height);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static Uri saveImage(@NonNull Context context, @NonNull Bitmap bitmap, int angle) throws IOException {
        if (bitmap == null) {
            return null;
        }

        if (angle > 0) {
            Matrix m = new Matrix();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            m.setRotate(angle);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);
        }

        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile(TEMP_IMAGE_NAME, ".jpg", outputDir);
        OutputStream outStream = new FileOutputStream(outputFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.THUMB_QUALITY, outStream);
        outStream.flush();
        outStream.close();

        return Uri.fromFile(outputFile);
    }

    private static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            // ignore
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                    default:
                        break;
                }
            }
        }
        return degree;
    }
}
