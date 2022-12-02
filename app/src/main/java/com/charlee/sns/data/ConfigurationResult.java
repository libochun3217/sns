package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 客户端配置
 */
public class ConfigurationResult extends ResultBase {
    private final Configuration configuration;

    public ConfigurationResult(int errCode, @Nullable String errMsg, Boolean hasMore,
                               @NonNull Configuration configuration) {
        super(errCode, errMsg, hasMore);
        this.configuration = configuration;
    }

    @NonNull
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public boolean isValid() {
        return this.configuration != null && this.configuration.isValid();
    }
}
