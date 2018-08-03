package com.cck.apermission.runtime;

import android.app.Application;
import android.os.Bundle;
import com.cck.apermission.Permissions;
import com.cck.apermission.log.Log;
import com.cck.apermission.log.PLog;

public enum  APermission {
    INSTANCE;
    static final String CONFIG_ENABLE_DEBUG = "_config_enable_debug";
    private Application mApp;

    public void init(Application application,Bundle params) {
        boolean enableDebug = params.getBoolean(CONFIG_ENABLE_DEBUG);
        if(enableDebug) {
            PLog.init(new LogImpl());
        }

        mApp = application;
        Permissions.injectPermissionCall(new APermissionCall());
    }

    public Application getApplication() {
        return mApp;
    }

    private static class LogImpl implements Log {

        @Override
        public void d(String tag, String msg) {
            android.util.Log.d(tag,msg);
        }
    }


}
