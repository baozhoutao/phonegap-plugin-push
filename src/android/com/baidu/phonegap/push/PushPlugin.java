package com.baidu.phonegap.push;

import java.util.HashMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.PluginResult;
import java.util.Iterator;

import android.content.Context;
import android.util.Log;
import android.os.Bundle;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

public class PushPlugin extends CordovaPlugin {
  //public static final String TAG = "PushPlugin";
  private static final String TAG = PushPlugin.class.getSimpleName();
  
  public static final String INIT = "init";
  public static final String REGISTER = "register";  
  public static final String UNREGISTER = "unregister";

  public static final String EXIT = "exit";

  private static PushPlugin _THIS;
  private CallbackContext callbackContext = null;
  private static Bundle gCachedExtras = null;
  private static CordovaWebView gWebView;
  private static String gECB;
  private static String pushTopic;

  public PushPlugin() {
    super();
    _THIS = this;
  }

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
  }

  @Override
  public boolean execute(String action, JSONArray args,
      CallbackContext callbackContext) throws JSONException {
    boolean result = false;
    Log.d(TAG, "com.baidu.phonegap.push.execute");
    Log.d(TAG, "execute: action=" + action);

    if (REGISTER.equals(action) || INIT.equals(action)) {

      Log.d(TAG, "execute: data=" + args.toString());
      this.callbackContext = callbackContext;

      try {
        JSONObject jo = args.getJSONObject(0);

        gWebView = this.webView;
        Log.v(TAG, "execute: jo=" + jo.toString());

        if(jo.has("ecb")){
          gECB = (String) jo.get("ecb");  
          Log.v(TAG, "execute: ECB=" + gECB);
        }

        if(jo.has("pushTopic")){
          pushTopic = jo.getString("pushTopic");
        }
        
        //String apiKey = jo.getString("api_key");
        Log.v(TAG, "execute: apiKey ...");
        String apiKey = Utils.getMetaValue(getApplicationContext(), "api_key");
        Log.v(TAG, "execute: apiKey=" + apiKey);

        PushManager.startWork(getApplicationContext(),
            com.baidu.android.pushservice.PushConstants.LOGIN_TYPE_API_KEY, apiKey);

        result = true;
      } catch (JSONException e) {
        Log.e(TAG, "execute: Got JSON Exception " + e.getMessage());
        result = false;
        callbackContext.error(e.getMessage());
      }
    } else if (UNREGISTER.equals(action)) {
      // TODO: 从百度解除绑定
      PushManager.unbind(getApplicationContext());

      Log.v(TAG, "UNREGISTER");
      result = true;
    } else {
      result = false;
      Log.e(TAG, "Invalid action : " + action);
      callbackContext.error("Invalid action : " + action);
    }
    return result;
  }

  /**
   * 绑定到Baidu时成功
   * 
   * @param data
   *            data中的字段(绑定成功后，百度返回的): errorCode, appid, userId, channelId,
   *            requestId
   */
  public void sendSuccess(HashMap registration) {
    if (this.callbackContext != null) {
      this.callbackContext.success(new JSONObject(registration));
    }
  }

  /**
   * 绑定到Baidu时出错
   * 
   * @param error
   */
  public void sendError(String error) {
    if (this.callbackContext != null) {
      HashMap data = new HashMap();
      data.put("error", error);
      callbackContext.error(new JSONObject(data));
    }
  }

 

  /*
   * Sends the pushbundle extras to the client application.
   * If the client application isn't currently active, it is cached for later processing.
   */
  public static void sendExtras(Bundle extras) {
      if (extras != null) {
          if (gWebView != null) {
              //sendEvent(convertBundleToJson(extras));
          } else {
              Log.v(TAG, "sendExtras: caching extras to send at a later time.");
              gCachedExtras = extras;
          }
      }
  }

  /**
   * 客户端接受到百度的推送后，调用 COS.pushController.onNotificationBaidu 方法
   * 
   * @param _json
   */
  public void sendJavascript(HashMap data, HashMap payload) {
    try {
      JSONObject _data = new JSONObject(data);
      payload.put("pushTopic", pushTopic);
      _data.put("payload", new JSONObject(payload));

      String _d = "javascript:" + gECB + "(" + _data.toString() + ")";
      Log.v(TAG, "sendJavascript: " + _d);

      if (gECB != null && gWebView != null) {
        gWebView.sendJavascript(_d);
      }
    } catch (JSONException ex) {
      ex.printStackTrace();
    }
  }

  private Context getApplicationContext() {
    return this.cordova.getActivity().getApplicationContext();
  }

  
  public static PushPlugin getInstance() {
    return _THIS;
  }

  public static boolean isActive() {
    return gWebView != null;
  }
}
