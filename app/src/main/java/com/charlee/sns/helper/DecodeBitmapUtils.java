package com.charlee.sns.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.IOException;

/**
 */
public class DecodeBitmapUtils {

    public static Bitmap createBitmap(Context context, final String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        WindowManager mWm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWm.getDefaultDisplay().getMetrics(mDisplayMetrics);
        int width = mDisplayMetrics.widthPixels;
        int height = mDisplayMetrics.heightPixels;

        options.inJustDecodeBounds = false;
        options.inSampleSize = DecodeBitmapUtils.computeSampleSize(options, -1, width * height);
        Bitmap bitmap = null;

        int degree = 0;
        ExifInterface exif;
        try {
            bitmap = BitmapFactory.decodeFile(filePath, options);
            exif = new ExifInterface(filePath);
            if (exif == null) {
                return bitmap;
            }

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
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
            if (degree != 0) {
                Matrix m = new Matrix();
                m.postRotate(degree);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h /
                minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}
