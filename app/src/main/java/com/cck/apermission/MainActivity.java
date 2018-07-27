package com.cck.apermission;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.cck.apermission.demo.R;

import java.util.List;
public class MainActivity extends AppCompatActivity {
    private Demo demo = new Demo();
    private Demo2 demo2 = new Demo2();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.camera1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera1(v);
            }
        });
        findViewById(R.id.camera2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera2(v);
            }
        });
        findViewById(R.id.camera3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera3(v);
            }
        });
        findViewById(R.id.camera4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera4(v);
            }
        });
        findViewById(R.id.camera5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera5(v);
            }
        });
        findViewById(R.id.camera6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera6(v);
            }
        });
    }

    private void camera1(View view) {
        Demo.camera1(this, new OnPermissionDeny() {
            @Override
            public void onPermissionsDeny(int requestCode, List<String> denyPermissions) {
                Demo.onPermissionDeny(requestCode,denyPermissions);
            }
        });
    }

    private void camera2(View view) {
        Demo.camera2(this);
    }

    private void camera3(View view) {
        demo.camera3(this);
    }

    private void camera4(View view) {
        demo.camera4(this, new OnPermissionDeny() {
            @Override
            public void onPermissionsDeny(int requestCode, List<String> denyPermissions) {
                Demo.onPermissionDeny(requestCode,denyPermissions);
            }
        });
    }

    private void camera5(View view) {
        demo2.camera5(this);
    }

    private void camera6(View view) {
        demo.camera6(this, new OnPermissionDeny() {
            @Override
            public void onPermissionsDeny(int requestCode, List<String> denyPermissions) {
                Demo.onPermissionDeny(requestCode,denyPermissions);
            }
        });
    }

}
