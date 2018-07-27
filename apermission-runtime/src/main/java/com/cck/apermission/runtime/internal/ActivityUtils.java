package com.cck.apermission.runtime.internal;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static android.app.ActivityManager.ProcessErrorStateInfo.NO_ERROR;

public class ActivityUtils {
    private static final String TAG = "APermission";

    @Nullable
    public static Activity getForegroundActivity(@Nullable Context context) {
        List<Activity> list = getActivities(context, true);
        return list.isEmpty() ? null : list.get(0);
    }

    // http://stackoverflow.com/questions/11411395/how-to-get-current-foreground-activity-context-in-android
    @NonNull
    public static List<Activity> getActivities(@Nullable Context context, boolean foregroundOnly) {
        List<Activity> list = new ArrayList<Activity>();
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = getActivityThread(context, activityThreadClass);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            // check app hasn't crashed, if it has, return empty list of activities.
            if (hasAppCrashed(context, activityThreadClass, activityThread)) {
                return new ArrayList<Activity>();
            }

            Collection c;
            Object collection = activitiesField.get(activityThread);

            if (collection instanceof HashMap) {
                // Older platforms
                Map activities = (HashMap) collection;
                c = activities.values();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    collection instanceof ArrayMap) {
                ArrayMap activities = (ArrayMap) collection;
                c = activities.values();
            } else {
                return list;
            }

            for (Object activityClientRecord : c) {
                Class activityClientRecordClass = activityClientRecord.getClass();
                if (foregroundOnly) {
                    Field pausedField = activityClientRecordClass.getDeclaredField("paused");
                    pausedField.setAccessible(true);
                    if (pausedField.getBoolean(activityClientRecord)) {
                        continue;
                    }
                }
                Field activityField = activityClientRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityClientRecord);
                if (activity != null) {
                    list.add(activity);
                }
            }
        } catch (Throwable e) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Error retrieving activities", e);
            }
        }
        return list;
    }

    /**
     * Checks if the application has crashed by comparing the package name against the list of
     * processes in error state.
     */
    private static boolean hasAppCrashed(
            @Nullable Context context,
            @NonNull Class activityThreadClass,
            @Nullable Object activityThread)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (context == null || activityThread == null) {
            return false;
        }

        String currentPackageName = getPackageName(activityThreadClass, activityThread);

        ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.ProcessErrorStateInfo> processesInErrorState =
                manager.getProcessesInErrorState();
        if (processesInErrorState != null) { // returns null if no process in error state
            for (ActivityManager.ProcessErrorStateInfo info : processesInErrorState) {
                if (info.processName.equals(currentPackageName) && info.condition != NO_ERROR) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "App Thread has crashed, return empty activity list.");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // Use reflection to determine the package name from activity thread.
    private static String getPackageName(
            @NonNull Class activityThreadClass, @Nullable Object activityThread)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method currentPackageNameMethod =
                activityThreadClass.getDeclaredMethod("currentPackageName");
        return (String) currentPackageNameMethod.invoke(activityThread);
    }

    @Nullable
    public static Object getActivityThread(@Nullable Context context,
                                           @Nullable Class<?> activityThread) {
        try {
            if (activityThread == null) {
                activityThread = Class.forName("android.app.ActivityThread");
            }
            Method m = activityThread.getMethod("currentActivityThread");
            m.setAccessible(true);
            Object currentActivityThread = m.invoke(null);
            if (currentActivityThread == null && context != null) {
                // In older versions of Android (prior to frameworks/base 66a017b63461a22842)
                // the currentActivityThread was built on thread locals, so we'll need to try
                // even harder
                Field mLoadedApk = context.getClass().getField("mLoadedApk");
                mLoadedApk.setAccessible(true);
                Object apk = mLoadedApk.get(context);
                Field mActivityThreadField = apk.getClass().getDeclaredField("mActivityThread");
                mActivityThreadField.setAccessible(true);
                currentActivityThread = mActivityThreadField.get(apk);
            }
            return currentActivityThread;
        } catch (Throwable ignore) {
            return null;
        }
    }

}
