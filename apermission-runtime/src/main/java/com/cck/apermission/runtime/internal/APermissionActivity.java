package com.cck.apermission.runtime.internal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.WindowManager;
import com.cck.apermission.OnPermissionsResult;
import com.cck.apermission.log.PLog;
import com.cck.apermission.runtime.APermission;

/**
 * 获取权限的Activity
 */
public class APermissionActivity extends Activity {
    /** 请求权限的requestCode */
    static final String BUNDLE_KEY_REQUEST_CODE  = "_request_code";
    /** 请求的权限 */
    static final String BUNDLE_KEY_PERMISSIONS   = "_permissions";
    /** 权限请求的结果回调 */
    static final String BUNDLE_KEY_CALLBACK      = "_callback";
    /** 权限请求的结果 */
    static final String BUNDLE_KEY_GRANT_RESULTS = "_grant_results";
    /** 回调成功的resultCode */
    static final int    RESULT_CODE_OK           = 1;
    private ResultReceiver mReceiver;
    public static void request(int requestCode, String[] permissions, OnPermissionsResult callback) {
        Intent intent = new Intent(APermission.INSTANCE.getApplication(),APermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BUNDLE_KEY_REQUEST_CODE,requestCode);
        intent.putExtra(BUNDLE_KEY_PERMISSIONS,permissions);
        intent.putExtra(BUNDLE_KEY_CALLBACK,new PermissionResultReceiver(callback));
        APermission.INSTANCE.getApplication().startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }


        initData(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData(intent);
    }

    private void initData(Intent intent) {
        if(intent == null) {
            PLog.d("intent is NULL");
            finish();
            return ;
        }

        int requestCode = intent.getIntExtra(BUNDLE_KEY_REQUEST_CODE,0);
        String[] permissions = intent.getStringArrayExtra(BUNDLE_KEY_PERMISSIONS);
        mReceiver = intent.getParcelableExtra(BUNDLE_KEY_CALLBACK);

        if(permissions == null || permissions.length == 0) {
            PLog.d("permissions is invalid");
            finish();
            return ;
        }

        if(mReceiver == null) {
            PLog.d("receiver is NULL");
            finish();
            return ;
        }
        ActivityCompat.requestPermissions(this,permissions,requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(mReceiver == null) {
            finish();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_KEY_REQUEST_CODE,requestCode);
        bundle.putStringArray(BUNDLE_KEY_PERMISSIONS,permissions);
        bundle.putIntArray(BUNDLE_KEY_GRANT_RESULTS,grantResults);
        mReceiver.send(RESULT_CODE_OK,bundle);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
