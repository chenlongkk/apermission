package com.cck.apermission;

import android.Manifest;
import android.content.Context;
import android.widget.Toast;
import com.cck.apermission.runtime.APermission;

import java.util.List;

public class Demo {

    /**
     * 静态方法
     * 请求单个权限,包含失败回调
     * @param context
     * @param deny
     */
    @RequestPermission(requestCode = 10034,value = Manifest.permission.CAMERA)
    public static void camera1(Context context,@SuppressWarnings("unused") OnPermissionDeny deny){
        Toast.makeText(context,"take a photo",Toast.LENGTH_SHORT).show();
    }

    /**
     * 静态方法
     * 请求单个权限，不包含失败回调
     * @param context
     */
    @RequestPermission(requestCode = 10035,value = Manifest.permission.CAMERA)
    public static void camera2(Context context) {
        Toast.makeText(context,"take a photo",Toast.LENGTH_SHORT).show();
    }

    /**
     * 实例方法
     * 请求单个权限，不包含失败回调
     * @param context
     */
    @RequestPermission(requestCode = 10036,value = Manifest.permission.CAMERA)
    public void camera3(Context context) {
        Toast.makeText(context,"take a photo",Toast.LENGTH_SHORT).show();
    }

    /**
     * 实例方法
     * 请求单个权限，包含失败回调
     * @param context
     */
    @RequestPermission(requestCode = 10037,value = Manifest.permission.CAMERA)
    public void camera4(Context context,@SuppressWarnings("unused") OnPermissionDeny deny) {
        Toast.makeText(context,"take a photo",Toast.LENGTH_SHORT).show();
    }

    /**
     * 实例方法
     * 请求多个权限，包含失败回调
     * @param context
     */
    @RequestPermission(requestCode = 10039,value = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void camera6(Context context,@SuppressWarnings("unused") OnPermissionDeny deny) {
        Toast.makeText(context,"take a photo",Toast.LENGTH_SHORT).show();
    }

    public static void onPermissionDeny(int requestCode,List<String> denyPermissions) {
        String msg = "deny:"+requestCode;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        for (String p : denyPermissions) {
            sb.append(p).append(",");
        }
        Toast.makeText(APermission.INSTANCE.getApplication(),sb.toString(),Toast.LENGTH_SHORT).show();
    }
}
