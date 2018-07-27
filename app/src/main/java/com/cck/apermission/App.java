package com.cck.apermission;

import android.app.Application;
import android.content.Context;
import com.cck.apermission.runtime.APermission;
import com.cck.apermission.runtime.ConfigBuilder;

public class App extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.enableDebug(true);
        APermission.INSTANCE.init(this,configBuilder.build());
    }
}
