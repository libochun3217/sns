package com.charlee.sns.helper;

import android.content.Context;

import com.charlee.sns.R;

import java.util.Random;

/**
 */
public class RecommendUtils {

    private static final int[] resArray = {
            R.string.reason0,
            R.string.reason1,
            R.string.reason2,
            R.string.reason3,
    };

    public static String getRecommendReason(Context context) {
        int index = new Random().nextInt(4);
        return context.getResources().getString(resArray[index]);
    }

}
