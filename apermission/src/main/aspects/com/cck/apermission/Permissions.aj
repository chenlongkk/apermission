package com.cck.apermission;

import com.cck.apermission.log.PLog;
import com.cck.apermission.utils.Strings;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * todo
 * 1. 增加全局的权限获取失败的回调
 * 2. 考虑是否使用inter-type处理实例方法的失败回调
 * 3. 如何写单元测试？
 */
public aspect Permissions {
    private PermissionsCall mPermissionCall;
    //使用inter-type为PermissionReceiver注解的类增加回调
    private interface BridgePermissionDeny extends OnPermissionDeny{}
    declare parents: (@PermissionReceiver *) implements BridgePermissionDeny;

    public void BridgePermissionDeny.onPermissionsDeny(int requestCode, List<String> denyPermissions) {
        System.out.println("default permission deny.");
    }
    /* 匹配被PermissionReceiver注解的类 */
    private pointcut withPermissionReceiverClass():
            within(@PermissionReceiver *);

    /* 匹配最后一个参数是OnPermissionDeny的方法，包含静态方法和实例方法 */
    private pointcut callbackMethodWithAnnotation(RequestPermission permission,OnPermissionDeny callback):
            execution(!synthetic * *(..,OnPermissionDeny))
            && @annotation(permission)
            && args(..,callback);

    /* 匹配最后一个参数不是OnPermissionResult的静态方法 */
    private pointcut staticNoCallbackMethodWithAnnotation(RequestPermission permission):
            (execution(static * *(..,!OnPermissionDeny))
                    ||execution(static * *()))
            && @annotation(permission);

    /* 匹配最后一个参数不是OnPermissionResult,并且该方法所属的类没有被PermissionReceiver注解的实例方法 */
    private pointcut noStaticNoCallbackMethodWithAnnotation(RequestPermission permission):
            (execution(!synthetic !static * *(..,!OnPermissionDeny))
                ||execution(!synthetic !static * *()))
            && @annotation(permission)
            && !withPermissionReceiverClass();

//
    /* 匹配最后一个参数不是OnPermissionResult,并且该方法所属的类被PermissionReceiver注解的实例方法 */
    private pointcut withReceiverMethodWithAnnotation(RequestPermission permission,BridgePermissionDeny callback):
            (execution(!synthetic !static * *(..,!OnPermissionDeny))
                    ||execution(!synthetic !static * *()))
            && @annotation(permission)
            && withPermissionReceiverClass()
            && this(callback);


    void around(final RequestPermission permission):
            staticNoCallbackMethodWithAnnotation(permission)
            || noStaticNoCallbackMethodWithAnnotation(permission){
        requestPermission((ProceedingJoinPoint) thisJoinPoint, permission, null, new ProceedCall() {
            @Override
            public void onProceed() {
                proceed(permission);
            }
        });
    }

    void around(final RequestPermission permission,final OnPermissionDeny callback):
            callbackMethodWithAnnotation(permission,callback) {
        requestPermission((ProceedingJoinPoint) thisJoinPoint, permission, callback, new ProceedCall() {
            @Override
            public void onProceed() {
                proceed(permission,callback);
            }
        });
    }

    void around(final RequestPermission permission,final BridgePermissionDeny callback):
            withReceiverMethodWithAnnotation(permission,callback) {
        requestPermission((ProceedingJoinPoint) thisJoinPoint, permission, callback, new ProceedCall() {
            @Override
            public void onProceed() {
                proceed(permission,callback);
            }
        });
    }

    public void injectPermissionCall(PermissionsCall permissionsCall) {
        this.mPermissionCall = permissionsCall;
    }

    private void requestPermission(final ProceedingJoinPoint joinPoint,final RequestPermission permission,final OnPermissionDeny callback,final ProceedCall call){
        if(permission == null || permission.value().length == 0) {
            PLog.d("permission annotation is invalid, just proceed origin method.");
            return ;
        }
        String[] permissions = permission.value();
        boolean allGranted = false;
        for(String permissionName : permissions) {
            boolean result = mPermissionCall.checkSelfPermission(joinPoint.getThis(),permissionName) == PermissionsCall.PERMISSION_GRANTED;
            if(!result) {
                PLog.d(permissionName +"not granted.");
                break;
            }else{
                allGranted = true;
            }
        }
        if(allGranted) {
            PLog.d("all permission has been granted, go on.");
            call.onProceed();
        }else{
            mPermissionCall.requestPermissions(joinPoint.getThis(),permissions, permission.requestCode(), new OnPermissionsResult() {
                @Override
                public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                    if(requestCode == permission.requestCode()) {
                        if(permissions == null) {
                            PLog.d("permissions is NULL.");
                            return ;
                        }

                        if(grantResults == null) {
                            PLog.d("grantResults is NULL.");
                            return ;
                        }

                        int size = Math.min(permissions.length,grantResults.length);
                        List<String> denyPermissions = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            int grantRes = grantResults[i];
                            boolean result = grantRes == PermissionsCall.PERMISSION_GRANTED;
                            if(!result) {
                                denyPermissions.add(permissions[i]);
                            }
                        }
                        if(denyPermissions.isEmpty()) {
                            PLog.d("requestCode:"+requestCode
                                    +",permissions:"+Strings.join(permissions,",")
                                    +" granted.");
                            call.onProceed();
                        }else {
                            try{
                                PLog.d("requestCode:"+requestCode
                                        +",permissions:"+Strings.join(permissions,",")
                                        +" deny.");
                                if(callback != null) {
                                    callback.onPermissionsDeny(requestCode,denyPermissions);
                                }
                            }catch (Exception e) {
                                PLog.d(e.getMessage());
                            }
                        }
                    }
                }
            });
        }
    }
    private interface ProceedCall{
        void onProceed();
    }
}
