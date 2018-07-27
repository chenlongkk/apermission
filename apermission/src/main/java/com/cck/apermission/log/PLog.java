package com.cck.apermission.log;

public class PLog {
    private static final String TAG = "APermissions";
    private static Log sLogImpl;

    public static void init(Log log) {
        sLogImpl = log;
    }

    public static void d(String msg) {
        if(sLogImpl != null) {
            sLogImpl.d(TAG,msg);
        }
    }
}
