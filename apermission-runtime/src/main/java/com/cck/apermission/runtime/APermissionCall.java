package com.cck.apermission.runtime;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import com.cck.apermission.OnPermissionsResult;
import com.cck.apermission.PermissionsCall;
import com.cck.apermission.log.PLog;
import com.cck.apermission.runtime.internal.APermissionActivity;

public class APermissionCall implements PermissionsCall {
    private int targetSdkVersion;
    @Override
    public void requestPermissions(Object target, String[] permissions, int requestCode, @NonNull OnPermissionsResult callback) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            APermissionActivity.request(requestCode,permissions,callback);
        }
    }

    @Override
    public int checkSelfPermission(Object target, String permission) {
        Context app = APermission.INSTANCE.getApplication();
        if(app == null) {
            return PackageManager.PERMISSION_DENIED;
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { //23以下，全部grant
            return PackageManager.PERMISSION_GRANTED;
        }else{
            int result;
            if(getTargetSdkVersion()>= Build.VERSION_CODES.M ) {
                result = ContextCompat.checkSelfPermission(APermission.INSTANCE.getApplication(),permission);
            } else {
                result = PermissionChecker.checkSelfPermission(APermission.INSTANCE.getApplication(),permission);
            }
            PLog.d("check permission:"+permission+",result:"+result);
            return result;
        }
    }

    private int getTargetSdkVersion() {
        if(targetSdkVersion == 0) {
            Context app = APermission.INSTANCE.getApplication();
            try{
                PackageInfo packageInfo = app.getPackageManager().getPackageInfo(app.getPackageName(),0);
                targetSdkVersion = packageInfo.applicationInfo.targetSdkVersion;
            }catch (PackageManager.NameNotFoundException e){
                PLog.d(e.getMessage());
            }
        }
        return targetSdkVersion;
    }

}
