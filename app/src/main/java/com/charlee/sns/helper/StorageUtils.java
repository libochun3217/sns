package com.charlee.sns.helper;

import android.os.Environment;

import java.io.File;

/**
 */
public class StorageUtils {


    public static File getTempDir() {
        File ext = Environment.getExternalStorageDirectory();
        File tempDir = new File(ext, "/photowonder/temp");
        if (tempDir.exists()) {
            return tempDir;
        }
        tempDir.mkdirs();
        return tempDir;
    }

    public static File createTempShareFile() {
        return new File(getTempDir() + File.separator + System.currentTimeMillis() + ".jpg");
    }

    public static File createPortraitTempFile() {
        return new File(getTempDir() + File.separator + System.currentTimeMillis() + ".jpg");
    }

    public static File getVideoCache() {
        File ext = Environment.getExternalStorageDirectory();
        File tempDir = new File(ext, "/photowonder/video_cache");
        if (tempDir.exists()) {
            return tempDir;
        }
        tempDir.mkdirs();
        return tempDir;
    }

}
