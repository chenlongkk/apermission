package cck.com.ap.test.jar;

import com.cck.apermission.RequestPermission;

public class Test {
    @RequestPermission(requestCode = 1000,value = "android.permission.CAMERA")
    public static void save2Sdcard(){
        System.out.println("hahahahhhah");
    }
}
