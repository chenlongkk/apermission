package com.cck.apermission;

import com.cck.apermission.log.PLog;
import com.cck.apermission.utils.Strings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;

import java.util.ArrayList;
import java.util.List;

@Aspect
public class Permissions {
    private static PermissionsCall sPermissionCall;

    /* ITD */
    @DeclareParents(value = "(@PermissionReceiver *)",defaultImpl = DefaultBridgeImpl.class)
    private BridgePermissionDeny implementedInterface;

    /* 匹配被PermissionReceiver注解的类 */
    @Pointcut("within(@PermissionReceiver *)")
    private void withPermissionReceiverClass() {}

    /* 匹配最后一个参数是OnPermissionDeny的方法，包含静态方法和实例方法 */
    @Pointcut(value= "execution(!synthetic * *(..,OnPermissionDeny))&&@annotation(permission)&&args(..,callback)",
              argNames = "permission,callback")
    private void callbackMethodWithAnnotation(RequestPermission permission,OnPermissionDeny callback) {}

    /* 匹配最后一个参数不是OnPermissionResult的静态方法 */
    @Pointcut(value = "(execution(static * *(..,!OnPermissionDeny))||execution(static * *()))&& @annotation(permission)",
              argNames = "permission")
    private void staticNoCallbackMethodWithAnnotation(RequestPermission permission) {}

    /* 匹配最后一个参数不是OnPermissionResult,并且该方法所属的类没有被PermissionReceiver注解的实例方法 */
    @Pointcut(value = "(execution(!synthetic !static * *(..,!OnPermissionDeny))" +
                        "||execution(!synthetic !static * *()))" +
              "&& !withPermissionReceiverClass() && @annotation(permission)",
              argNames = "permission")
    private void noStaticNoCallbackMethodWithAnnotation(RequestPermission permission) {}

    /* 匹配最后一个参数不是OnPermissionResult,并且该方法所属的类被PermissionReceiver注解的实例方法 */
    @Pointcut(value = " (execution(!synthetic !static * *(..,!OnPermissionDeny))" +
            "                    ||execution(!synthetic !static * *()))" +
            "            && @annotation(permission)" +
            "            && withPermissionReceiverClass()" +
            "            && this(callback)",argNames = "permission,callback")
    private void withReceiverMethodWithAnnotation(RequestPermission permission, BridgePermissionDeny callback) {}

    @Around(value = "staticNoCallbackMethodWithAnnotation(permission) || noStaticNoCallbackMethodWithAnnotation(permission)",
            argNames = "thisJoinPoint,permission")
    public Object callMethodWithoutCallback(ProceedingJoinPoint thisJoinPoint,RequestPermission permission) {
        return requestPermission(thisJoinPoint, permission, null);
    }

    @Around(value = "callbackMethodWithAnnotation(permission,callback)",
            argNames = "thisJoinPoint,permission,callback")
    public Object callMethodWithCallbackParams(ProceedingJoinPoint thisJoinPoint,RequestPermission permission,OnPermissionDeny callback) {
        return requestPermission(thisJoinPoint, permission, callback);
    }

    @Around(value = "withReceiverMethodWithAnnotation(permission,callback)",
            argNames = "thisJoinPoint,permission,callback")
    public Object callMethodWithReceiver(ProceedingJoinPoint thisJoinPoint,RequestPermission permission,BridgePermissionDeny callback) {
        return requestPermission(thisJoinPoint,permission,callback);
    }

    public static void injectPermissionCall(PermissionsCall permissionsCall) {
        sPermissionCall = permissionsCall;
    }

    public Object requestPermission(final ProceedingJoinPoint joinPoint,final RequestPermission permission,final OnPermissionDeny callback){
        if(permission == null || permission.value().length == 0) {
            PLog.d("permission annotation is invalid, just proceed origin method.");
            return null;
        }
        String[] permissions = permission.value();
        boolean allGranted = false;
        for(String permissionName : permissions) {
            boolean result = sPermissionCall.checkSelfPermission(joinPoint.getThis(),permissionName) == PermissionsCall.PERMISSION_GRANTED;
            if(!result) {
                PLog.d(permissionName +"not granted.");
                break;
            }else{
                allGranted = true;
            }
        }
        if(allGranted) {
            PLog.d("all permission has been granted, go on.");
            try{
                return joinPoint.proceed();
            }catch (Throwable e) {
                e.printStackTrace();
            }
        }else{
            sPermissionCall.requestPermissions(joinPoint.getThis(),permissions, permission.requestCode(), new OnPermissionsResult() {
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
                            try{
                                joinPoint.proceed();
                            }catch (Throwable e) {
                                e.printStackTrace();
                            }
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
        return null;
    }

    public static class DefaultBridgeImpl implements BridgePermissionDeny {
        @Override
        public void onPermissionsDeny(int requestCode, List<String> denyPermissions) {
            System.out.println("default permission deny.");
        }
    }

    public interface BridgePermissionDeny extends OnPermissionDeny {
        void onPermissionsDeny(int requestCode, List<String> denyPermissions);
    }
}
