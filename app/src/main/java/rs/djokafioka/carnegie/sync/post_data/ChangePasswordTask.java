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
 * Created by Djordje on 23.1.2022..
 */
public class ChangePasswordTask extends BaseBackgroundTask
{
    private static final String TAG = "ChangePasswordTask";

    private final OkHttpClient mClient;
    private Call mCall;
    private Gson mGson;
    private SyncDataResult mSyncDataResult;
    private OnChangePasswordListener mOnChangePasswordListener;
    private ChangePasswordBindingModel mChangePasswordBindingModel;

    public ChangePasswordTask(String oldPassword, String newPassword, String confirmPassword, OnChangePasswordListener onChangePasswordListener)
    {
        mChangePasswordBindingModel = new ChangePasswordBindingModel(oldPassword, newPassword, confirmPassword);
        mClient = new OkHttpClient.Builder()
                .connectTimeout(AppConsts.CONNECTIVITY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(AppConsts.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AppConsts.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        mGson = new Gson();
        mSyncDataResult = new SyncDataResult();
        mOnChangePasswordListener = onChangePasswordListener;
    }

    @Override
    public void doInBackground()
    {
        Request request = new Request.Builder()
                .url(composeServerAddress())
                .header("Authorization", getHeaderFormattedToken())
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
        if (mOnChangePasswordListener != null)
            mOnChangePasswordListener.onChangePasswordCompleted(mSyncDataResult);
    }

    private String getJsonBody()
    {
        return mGson.toJson(mChangePasswordBindingModel);
    }

    private String composeServerAddress()
    {
        return getAPIURL() + AppConsts.API_CHANGE_PASSWORD;
    }

    public interface OnChangePasswordListener
    {
        void onChangePasswordCompleted(SyncDataResult syncDataResult);
    }

    private static class ChangePasswordBindingModel
    {
        @Expose
        @SerializedName("OldPassword")
        private String mOldPassword;

        @Expose
        @SerializedName("NewPassword")
        private String mNewPassword;

        @Expose
        @SerializedName("ConfirmPassword")
        private String mConfirmPassword;

        public ChangePasswordBindingModel(String oldPassword, String newPassword, String confirmPassword)
        {
            mOldPassword = oldPassword;
            mNewPassword = newPassword;
            mConfirmPassword = confirmPassword;
        }

        public String getOldPassword()
        {
            return mOldPassword;
        }

        public void setOldPassword(String oldPassword)
        {
            mOldPassword = oldPassword;
        }

        public String getNewPassword()
        {
            return mNewPassword;
        }

        public void setNewPassword(String newPassword)
        {
            mNewPassword = newPassword;
        }

        public String getConfirmPassword()
        {
            return mConfirmPassword;
        }

        public void setConfirmPassword(String confirmPassword)
        {
            mConfirmPassword = confirmPassword;
        }
    }
}
