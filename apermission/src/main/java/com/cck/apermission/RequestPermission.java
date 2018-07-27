package com.cck.apermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ActivityCompat
 *    .requestPermissions(
 *        (Activity) mContext,
 *        new String[]{Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR},
 *        REQUEST_CODE
 *     );
 * public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){}
 *
 * 1. 对于实例方法，权限获取成功的情况下，则执行被注解的方法，如果权限获取失败
 * 则回调当前类中的onPermissionResult方法
 * 2. 对于静态方法,声明方法时，可以将方法的最后一个参数声明为OnPermissionRequest类型，
 * 权限获取成功时，执行被注解的方法，如果权限获取失败，若最后一个参数是OnPermissionRequest类型，
 * 则回调它
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestPermission {
    String[] value();
    int requestCode();
}
