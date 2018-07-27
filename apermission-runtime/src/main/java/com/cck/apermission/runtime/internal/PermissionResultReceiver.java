package com.cck.apermission.runtime.internal;


import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import com.cck.apermission.OnPermissionsResult;

public class PermissionResultReceiver extends ResultReceiver {
    private OnPermissionsResult mCallback;
    public PermissionResultReceiver(OnPermissionsResult callback) {
        super(new Handler());
        mCallback = callback;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if(resultCode == APermissionActivity.RESULT_CODE_OK && resultData != null) {
            int requestCode = resultData.getInt(APermissionActivity.BUNDLE_KEY_REQUEST_CODE);
            String[] permissions = resultData.getStringArray(APermissionActivity.BUNDLE_KEY_PERMISSIONS);
            int[] grantResult = resultData.getIntArray(APermissionActivity.BUNDLE_KEY_GRANT_RESULTS);
            mCallback.onRequestPermissionsResult(requestCode,permissions,grantResult);
        }
    }
}
