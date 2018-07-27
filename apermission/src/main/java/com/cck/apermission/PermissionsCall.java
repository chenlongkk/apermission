package com.cck.apermission;

public interface PermissionsCall {
    int PERMISSION_GRANTED = 0;
    int PERMISSION_DENIED  = -1;
    int PERMISSION_DENIED_APP_OP = -2;
    void requestPermissions(Object target, String[] permissions, int requestCode, OnPermissionsResult callback);
    int checkSelfPermission(Object target, String permission);
}
