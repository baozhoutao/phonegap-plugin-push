package com.baidu.phonegap.push;

import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.android.pushservice.PushMessageReceiver;

/*
 * Push消息处理receiver。请编写您需要的回调函数， 一般来说： onBind是必须的，用来处理startWork返回值；
 *onMessage用来接收透传消息； onSetTags、onDelTags、onListTags是tag相关操作的回调；
 *onNotificationClicked在通知被点击时回调； onUnbind是stopWork接口的返回值回调

 * 返回值中的errorCode，解释如下：
 *0 - Success
 *10001 - Network Problem
 *10101  Integrate Check Error
 *30600 - Internal Server Error
 *30601 - Method Not Allowed
 *30602 - Request Params Not Valid
 *30603 - Authentication Failed
 *30604 - Quota Use Up Payment Required
 *30605 -Data Required Not Found
 *30606 - Request Time Expires Timeout
 *30607 - Channel Token Timeout
 *30608 - Bind Relation Not Found
 *30609 - Bind Number Too Many

 * 当您遇到以上返回错误时，如果解释不了您的问题，请用同一请求的返回值requestId和errorCode联系我们追查问题。
 *
 */


public class BAIDUPushMessageReceiver extends PushMessageReceiver {
  /** TAG to Log */
  public static final String TAG = BAIDUPushMessageReceiver.class.getSimpleName();


  /**
  * 调用PushManager.startWork后，sdk将对push
  * server发起绑定请求，这个过程是异步的。绑定请求的结果通过onBind返回。 如果您需要用单播推送，需要把这里获取的channel
  * id和user id上传到应用server中，再调用server接口用channel id和user id给单个手机或者用户推送。
  *
  * @param context
  *            BroadcastReceiver的执行Context
  * @param errorCode
  *            绑定接口返回值，0 - 成功
  * @param appid
  *            应用id。errorCode非0时为null
  * @param userId
  *            应用user id。errorCode非0时为null
  * @param channelId
  *            应用channel id。errorCode非0时为null
  * @param requestId
  *            向服务端发起的请求id。在追查问题时有用；
  * @return none
  */
  @Override
  public void onBind(Context context, int errorCode, String appid,
      String userId, String channelId, String requestId) {
    String responseString = "onBind errorCode=" + errorCode + " appid="
        + appid + " userId=" + userId + " channelId=" + channelId
        + " requestId=" + requestId;
    Log.d(TAG, responseString);

    // 绑定成功，设置已绑定flag，可以有效的减少不必要的绑定请求
    if (errorCode == 0) {
      Utils.setBind(context, true);

      HashMap data = new HashMap();
      data.put("errorCode", errorCode + "");
      data.put("appid", appid);
      data.put("userId", userId);
      data.put("channelId", channelId);
      data.put("requestId", requestId);
      data.put("event", "registered");

      PushPlugin.getInstance().sendSuccess(data);
    } else {
      Utils.setBind(context, false);
      PushPlugin.getInstance().sendError(
          "绑定到百度失败，errorCode：" + errorCode);
    }
  }

  @Override
  public void onDelTags(Context context, int errorCode,
      List<String> sucessTags, List<String> failTags, String requestId) {
    String responseString = "onDelTags errorCode=" + errorCode
        + " sucessTags=" + sucessTags + " failTags=" + failTags
        + " requestId=" + requestId;
    Log.d(TAG, responseString);
  }

  @Override
  public void onListTags(Context context, int errorCode, List<String> tags,
      String requestId) {
    String responseString = "onListTags errorCode=" + errorCode + " tags="
        + tags;
    Log.d(TAG, responseString);
  }

  @Override
  public void onMessage(Context context, String message,
      String customContentString) {
    String messageString = "透传消息 message=\"" + message
        + "\" customContentString=" + customContentString;
    Log.d(TAG, messageString);

    HashMap data = new HashMap();
    data.put("event", "message");

    HashMap payload = new HashMap();
    payload.put("message", message);

    // 自定义内容获取方式，mykey和myvalue对应透传消息推送时自定义内容中设置的键和值
    if (!TextUtils.isEmpty(customContentString)) {
      JSONObject customJson = null;
      try {
        customJson = new JSONObject(customContentString);
        if (customJson.has("badge") && !customJson.isNull("badge")) {
          payload.put("badge", customJson.getString("badge"));
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    PushPlugin.getInstance().sendJavascript(data, payload);
  }


  /**
     * 接收通知到达的函数。
     *
     * @param context
     *            上下文
     * @param title
     *            推送的通知的标题
     * @param description
     *            推送的通知的描述
     * @param customContentString
     *            自定义内容，为空或者json字符串
     */

    @Override
    public void onNotificationArrived(Context context, String title,
            String description, String customContentString) {

        String notifyString = "onNotificationArrived  title=\"" + title
                + "\" description=\"" + description + "\" customContent="
                + customContentString;
        Log.d(TAG, notifyString);

        // 自定义内容获取方式，mykey和myvalue对应通知推送时自定义内容中设置的键和值
        if (!TextUtils.isEmpty(customContentString)) {
            JSONObject customJson = null;
            try {
                customJson = new JSONObject(customContentString);
                String myvalue = null;
                if (!customJson.isNull("mykey")) {
                    myvalue = customJson.getString("mykey");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        // 你可以參考 onNotificationClicked中的提示从自定义内容获取具体值
        //updateContent(context, notifyString);

        Log.d(TAG, notifyString);
    }


  @Override
  public void onNotificationClicked(Context context, String title,
      String description, String customContentString) {
    String notifyString = "通知点击 title=\"" + title + "\" description=\""
        + description + "\" customContent=" + customContentString;
    Log.d(TAG, notifyString);

    HashMap data = new HashMap();
    data.put("event", "notification_clicked");

    HashMap payload = new HashMap();
    payload.put("title", title);
    payload.put("description", description);

    // 自定义内容获取方式，mykey和myvalue对应通知推送时自定义内容中设置的键和值
    if (!TextUtils.isEmpty(customContentString)) {
      JSONObject customJson = null;
      try {
        customJson = new JSONObject(customContentString);
        if (customJson.has("badge") && !customJson.isNull("badge")) {
          payload.put("badge", customJson.getString("badge"));
        }
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    PushPlugin.getInstance().sendJavascript(data, payload);
  }

  @Override
  public void onSetTags(Context context, int errorCode,
      List<String> sucessTags, List<String> failTags, String requestId) {
    String responseString = "onSetTags errorCode=" + errorCode
        + " sucessTags=" + sucessTags + " failTags=" + failTags
        + " requestId=" + requestId;
    Log.d(TAG, responseString);
  }

  @Override
  public void onUnbind(Context context, int errorCode, String requestId) {
    String responseString = "onUnbind errorCode=" + errorCode
        + " requestId = " + requestId;
    Log.d(TAG, responseString);

    // 解绑定成功，设置未绑定flag，
    if (errorCode == 0) {
      Utils.setBind(context, false);

      HashMap data = new HashMap();
      data.put("errorCode", errorCode + "");
      data.put("requestId", requestId);
      data.put("event", "unregistered");

      PushPlugin.getInstance().sendSuccess(data);
    } else {
      PushPlugin.getInstance().sendError(
          "从百度解除绑定失败，errorCode：" + errorCode);
    }
  }
}
