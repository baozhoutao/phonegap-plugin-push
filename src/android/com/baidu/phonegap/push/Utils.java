package com.baidu.phonegap.push;

import android.content.Context;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
public class Utils {
  private static final String TAG = Utils.class.getSimpleName();

  // 获取ApiKey
  public static String getMetaValue(Context context, String metaKey) {
      Log.v(TAG, "Utils.getMetaValue , key is " + metaKey);
      Bundle metaData = null;
      String apiKey = null;
      if (context == null || metaKey == null) {
          return null;
      }
      try {
          ApplicationInfo ai = context.getPackageManager()
                  .getApplicationInfo(context.getPackageName(),
                          PackageManager.GET_META_DATA);
          if (null != ai) {
              metaData = ai.metaData;
          }
          if (null != metaData) {
              apiKey = metaData.getString(metaKey);
          }
      } catch (NameNotFoundException e) {
         Log.e(TAG, "Utils.getMetaValue, Exception " + e.getMessage());
      }
      return apiKey;
  }

  public static void setBind(Context context, boolean flag) {
    String flagStr = "not";
    if (flag) {
      flagStr = "ok";
    }
    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(context);
    Editor editor = sp.edit();
    editor.putString("bind_flag", flagStr);
    editor.commit();
  }

  // 用share preference来实现是否绑定的开关。在ionBind且成功时设置true，unBind且成功时设置false
  public static boolean hasBind(Context context) {
    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(context);
    String flag = sp.getString("bind_flag", "");
    if ("ok".equalsIgnoreCase(flag)) {
      return true;
    }
    return false;
  }
}
