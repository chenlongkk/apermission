package com.cck.apermission;

import android.Manifest;
import android.content.Context;
import android.widget.Toast;

import java.util.List;

@PermissionReceiver
public class Demo2 implements OnPermissionDeny{
    /**
     * 实例方法
     * 请求单个权限，包含失败回调
     * @param context
     */
    @RequestPermission(requestCode = 10038,value = Manifest.permission.CAMERA)
    public void camera5(Context context) {
        Toast.makeText(context,"take a photo",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDeny(int requestCode, List<String> denyPermissions) {
        Demo.onPermissionDeny(requestCode,denyPermissions);
    }
}
