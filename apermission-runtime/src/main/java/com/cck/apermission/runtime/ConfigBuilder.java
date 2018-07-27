package com.cck.apermission.runtime;

import android.os.Bundle;

public class ConfigBuilder {

    private Bundle mConfig;
    public ConfigBuilder() {
        mConfig = new Bundle();
    }

    public ConfigBuilder enableDebug(boolean enable) {
        mConfig.putBoolean(APermission.CONFIG_ENABLE_DEBUG,enable);
        return this;
    }

    public Bundle build() {
        return mConfig;
    }
}
