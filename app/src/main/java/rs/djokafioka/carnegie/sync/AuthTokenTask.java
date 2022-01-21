package rs.djokafioka.carnegie.sync;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rs.djokafioka.carnegie.sync.model.SyncDataResult;
import rs.djokafioka.carnegie.utils.AppConsts;
import rs.djokafioka.carnegie.utils.SharedPreferencesHelper;

/**
 * Created by Djordje on 20.1.2022..
 */
public final class AuthTokenTask
{
    private static final String TAG = "AuthTokenTask";

    private String mEmail;
    private String mPassword;
    private SyncDataResult mSyncDataResult;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Executor mBackgroundExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean mIsCancelled;

    private final OkHttpClient mClient;
    private Call mCall;
    private Gson mGson;

    private OnAuthTokenListener mOnAuthTokenListener;

    public AuthTokenTask(String email, String password, OnAuthTokenListener onAuthTokenListener)
    {
        mEmail = email;
        mPassword = password;
        mClient = new OkHttpClient.Builder()
                .connectTimeout(AppConsts.CONNECTIVITY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(AppConsts.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AppConsts.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        mGson = new Gson();
        mSyncDataResult = new SyncDataResult();
        mOnAuthTokenListener = onAuthTokenListener;
    }

    public void execute(){
        mBackgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (!mIsCancelled)
                    mSyncDataResult = doInBackground();
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mIsCancelled)
                            onPostExecute(mSyncDataResult);
                    }
                });
            }
        });
    }

    public SyncDataResult doInBackground()
    {
        Request request = new Request.Builder()
                .url(composeServerAddress())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .post(RequestBody.create(getJsonBody(), AppConsts.MEDIA_TYPE_JSON))
                .build();

        Response response = null;
        ResponseBody body;

        try
        {
            mCall = mClient.newCall(request);
            response = mCall.execute();

            if (response.isSuccessful())
            {
                mSyncDataResult.setSuccess(true);

                body = response.body();
                if (body != null)
                {
                    JSONObject json = new JSONObject(body.string());
                    String token = json.getString("access_token");
                    String userName = json.getString("userName");
                    Log.d(TAG, "doInBackground: token = " + token);
                    Log.d(TAG, "doInBackground: userName = " + userName);
                    SharedPreferencesHelper.getInstance().setRegisteredUser(userName);
                    SharedPreferencesHelper.getInstance().setToken(token);
                }
            }
            else
            {
                mSyncDataResult.setSuccess(false);
                body = response.body();
                if (body != null)
                {
                    JSONObject json = new JSONObject(body.string());
                    String errorMessage = json.getString("Message");
                    mSyncDataResult.setError(errorMessage);
                }
                else
                {
                    mSyncDataResult.setError(response.message());
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            mSyncDataResult.setError(e.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            mSyncDataResult.setError(e.getMessage());
        } finally
        {
            if (response != null)
            {
                response.close();
            }
        }

        return mSyncDataResult;
    }

    private void onPostExecute(SyncDataResult syncDataResult)
    {
        if (mOnAuthTokenListener != null)
            mOnAuthTokenListener.onAuthTokenCompleted(syncDataResult);
    }

    public void cancel(){
        mIsCancelled = true;
    }

    public boolean isCancelled(){
        return mIsCancelled;
    }

    public String getEmail()
    {
        return mEmail;
    }

    public void setEmail(String email)
    {
        mEmail = email;
    }

    public String getPassword()
    {
        return mPassword;
    }

    public void setPassword(String password)
    {
        mPassword = password;
    }

    private String getJsonBody()
    {
        return "username=" + mEmail + "&password=" + mPassword + "&grant_type=password";
    }

    private String composeServerAddress()
    {
        String apiURL = SharedPreferencesHelper.getInstance().getApiUrl();
        if (!apiURL.isEmpty())
            apiURL += AppConsts.API_TOKEN;
        return apiURL;
    }

    public interface OnAuthTokenListener
    {
        void onAuthTokenCompleted(SyncDataResult syncDataResult);
    }
}
