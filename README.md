# APermission
基于aspectj的Android动态权限申请工具

# TODO
1. 无法开启java8语法支持
2. instantRun失效

# 使用方式
```java
@RequestPermission(requestCode = 10038,value = Manifest.permission.CAMERA)
public void camera(Context context) {
    Toast.makeText(context,"take a photo",Toast.LENGTH_SHORT).show();
}
```