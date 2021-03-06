package com.dooboolab.naverlogin;

import android.app.Activity;
import android.util.Log;
import android.os.AsyncTask;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.UiThreadUtil;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class RNNaverLoginModule extends ReactContextBaseJavaModule {
  final String TAG = "ReactNaverModule";

  private final ReactApplicationContext reactContext;
  private OAuthLogin mOAuthLoginModule;
  private Callback mCallback;

  public RNNaverLoginModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNNaverLogin";
  }

//  @ReactMethod
//  public void getProfile(String accessToken, final Callback cb) {
//    AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
//    asyncHttpClient.addHeader("Authorization", "Bearer " + accessToken);
//    asyncHttpClient.get(reactContext, "https://openapi.naver.com/v1/nid/me", new JsonHttpResponseHandler() {
//      @Override
//      public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//        super.onSuccess(statusCode, headers, response);
//        cb.invoke(null, response.toString());
//      }
//    });
//  }

  @ReactMethod
  public void logout() {
    if (mOAuthLoginModule != null) {
      mOAuthLoginModule.logout(reactContext);
    }
  }

  // only android
  @ReactMethod
  public void logoutWithCallback(final Callback cb) {
    try {
      mOAuthLoginModule.logout(reactContext);
      cb.invoke(null, true);
    } catch (Exception e) {
      cb.invoke(e.getMessage(), null);
    }
  }

  @ReactMethod
  public void login(String initials, final Callback cb) {
    if (mCallback != null) {
      return;
    }
    mCallback = cb;
    final Activity activity = getCurrentActivity();
    try {
      JSONObject jsonObject = new JSONObject(initials);
      mOAuthLoginModule = OAuthLogin.getInstance();
      mOAuthLoginModule.init(
          reactContext,
          jsonObject.getString("kConsumerKey"),
          jsonObject.getString("kConsumerSecret"),
          jsonObject.getString("kServiceAppName")
      );
      UiThreadUtil.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mOAuthLoginModule.startOauthLoginActivity(
              activity,
              new OAuthLoginHandler() {
                @Override
                public void run(boolean success) {
                  if (success) {
                    final String accessToken = mOAuthLoginModule.getAccessToken(reactContext);
                    String refreshToken = mOAuthLoginModule.getRefreshToken(reactContext);
                    long expiresAt = mOAuthLoginModule.getExpiresAt(reactContext);
                    String tokenType = mOAuthLoginModule.getTokenType(reactContext);

                    try {
                      JSONObject response = new JSONObject();
                      response.put("accessToken", accessToken);
                      response.put("refreshToken", refreshToken);
                      response.put("expiresAt", expiresAt);
                      response.put("tokenType", tokenType);
                      // cb.invoke(null, response.toString());
                      if (mCallback != null) {
                        mCallback.invoke(null, accessToken);
                        mCallback = null;
                      }
                    } catch (JSONException je) {
                      Log.e(TAG, "JSONEXception: " + je.getMessage());
                      if (mCallback != null) {
                        mCallback.invoke(je.getMessage(), null);
                        mCallback = null;
                      }
                    }

                  } else {
                    String errCode = mOAuthLoginModule.getLastErrorCode(reactContext).getCode();
                    String errDesc = mOAuthLoginModule.getLastErrorDesc(reactContext);
                    if (mCallback != null) {
                      mCallback.invoke("login failed", null);
                      mCallback = null;
                    }
                    Log.e(TAG, "errCode: " + errCode + ", errDesc: " + errDesc);
                  }
                }
              }
          );
        }
      });
    } catch (JSONException je) {
      Log.d(TAG, "JSONException: " + je);
    }
  }
  @ReactMethod
    public void loginSilently(String initials, final Callback cb) {
      final Activity activity = getCurrentActivity();
        try {
          JSONObject jsonObject = new JSONObject(initials);
          mOAuthLoginModule = OAuthLogin.getInstance();
          mOAuthLoginModule.init(
            reactContext,
            jsonObject.getString("kConsumerKey"),
            jsonObject.getString("kConsumerSecret"),
            jsonObject.getString("kServiceAppName")
          );
          RefreshTokenTask task = new RefreshTokenTask();
          task.cb = cb;
          task.execute();
         } catch (JSONException je) {
           Log.d(TAG, "JSONException: " + je);
         }
       }

       @ReactMethod
       public void getToken(String initials, final Callback cb) {
           try {
                     JSONObject jsonObject = new JSONObject(initials);
                     mOAuthLoginModule = OAuthLogin.getInstance();
                     mOAuthLoginModule.init(
                       reactContext,
                       jsonObject.getString("kConsumerKey"),
                       jsonObject.getString("kConsumerSecret"),
                       jsonObject.getString("kServiceAppName")
                     );
                     cb.invoke(null, mOAuthLoginModule.getAccessToken(reactContext));
                    } catch (JSONException je) {
                      Log.d(TAG, "JSONException: " + je);
                    }
       }

    class RefreshTokenTask extends AsyncTask<Void, Void, String> {
      Callback cb;

      @Override
      protected String doInBackground(Void... params) {
        return OAuthLogin.getInstance().refreshAccessToken(reactContext);
      }

      protected void onPostExecute(String res) {
       if (cb != null) {
         if (res != null) {
          cb.invoke(null, res);
         }
         else {
          cb.invoke("token refresh fail", null);
         }
       }
       cb = null;
      }
   }
}
