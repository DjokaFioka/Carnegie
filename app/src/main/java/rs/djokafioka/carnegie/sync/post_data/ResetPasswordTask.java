package rs.djokafioka.carnegie.sync.post_data;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rs.djokafioka.carnegie.sync.BaseBackgroundTask;
import rs.djokafioka.carnegie.sync.model.SyncDataResult;
import rs.djokafioka.carnegie.utils.AppConsts;

/**
 * Created by Djordje on 20.1.2022..
 */
public class ResetPasswordTask extends BaseBackgroundTask
{
    private static final String TAG = "ResetPasswordTask";

    private final OkHttpClient mClient;
    private Call mCall;
    private Gson mGson;
    private SyncDataResult mSyncDataResult;
    private OnResetPasswordListener mOnResetPasswordListener;
    private ResetPasswordBindingModel mResetPasswordBindingModel;

    public ResetPasswordTask(String email, OnResetPasswordListener onResetPasswordListener)
    {
        mResetPasswordBindingModel = new ResetPasswordBindingModel(email);
        mClient = new OkHttpClient.Builder()
                .connectTimeout(AppConsts.CONNECTIVITY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(AppConsts.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AppConsts.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        mGson = new Gson();
        mSyncDataResult = new SyncDataResult();
        mOnResetPasswordListener = onResetPasswordListener;
    }

    @Override
    public void doInBackground()
    {
        Request request = new Request.Builder()
                .url(composeServerAddress())
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
            }
            else
            {
                mSyncDataResult.setSuccess(false);
                mSyncDataResult.setResponseCode(response.code());

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
    }

    @Override
    public void onPostExecute()
    {
        if (mOnResetPasswordListener != null)
            mOnResetPasswordListener.onSendNewPasswordCompleted(mSyncDataResult);
    }

    private String getJsonBody()
    {
        return mGson.toJson(mResetPasswordBindingModel);
    }

    private String composeServerAddress()
    {
        return getAPIURL() + AppConsts.API_RESET_PASSWORD;
    }

    public interface OnResetPasswordListener
    {
        void onSendNewPasswordCompleted(SyncDataResult syncDataResult);
    }

    private static class ResetPasswordBindingModel
    {
        @Expose
        @SerializedName("Email")
        private String mEmail;

        public ResetPasswordBindingModel(String email)
        {
            mEmail = email;
        }

        public String getEmail()
        {
            return mEmail;
        }

        public void setEmail(String email)
        {
            mEmail = email;
        }
    }
}
