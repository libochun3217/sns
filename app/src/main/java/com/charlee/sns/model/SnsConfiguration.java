package com.charlee.sns.model;


import androidx.annotation.NonNull;

import com.charlee.sns.data.Configuration;
import com.charlee.sns.data.ConfigurationResult;

/**
 */
public class SnsConfiguration {
    private Configuration configuration;

    public SnsConfiguration(@NonNull ConfigurationResult configurationResult) {
        if (configurationResult.isValid()) {
            configuration = configurationResult.getConfiguration();
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
