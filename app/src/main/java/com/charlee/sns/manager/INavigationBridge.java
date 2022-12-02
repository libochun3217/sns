package com.charlee.sns.manager;

import android.content.Context;

/**
 */
public interface INavigationBridge {

    void navigateToUserScenario(Context context, int toType, int materialId, String campaignId);

}