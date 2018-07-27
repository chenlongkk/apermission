package com.cck.apermission;

import java.util.List;

/**
 * todo 修改为deny
 */
public interface OnPermissionDeny {
    void onPermissionsDeny(int requestCode, List<String> denyPermissions);
}
