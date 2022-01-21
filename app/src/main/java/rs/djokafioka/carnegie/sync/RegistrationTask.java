package rs.djokafioka.carnegie.sync;

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
import rs.djokafioka.carnegie.sync.model.SyncDataResult;
import rs.djokafioka.carnegie.utils.AppConsts;

/**
 * Created by Djordje on 21.1.2022..
 */
public class RegistrationTask extends BaseBackgroundTask
{
    private static final String TAG = "RegistrationTask";

    private final OkHttpClient mClient;
    private Call mCall;
    private Gson mGson;
    private SyncDataResult mSyncDataResult;
    private OnRegistrationListener mOnRegistrationListener;
    private RegisterBindingModel mRegisterBindingModel;

    public RegistrationTask(String email, String password, String confirmPassword, OnRegistrationListener onRegistrationListener)
    {
        mRegisterBindingModel = new RegisterBindingModel(email, password, confirmPassword);
        mClient = new OkHttpClient.Builder()
                .connectTimeout(AppConsts.CONNECTIVITY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(AppConsts.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AppConsts.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        mGson = new Gson();
        mSyncDataResult = new SyncDataResult();
        mOnRegistrationListener = onRegistrationListener;
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
    public void onProgressUpdate(double progress)
    {

    }

    @Override
    public void onPostExecute()
    {
        if (mOnRegistrationListener != null)
            mOnRegistrationListener.onRegistrationCompleted(mSyncDataResult);
    }

    private String getJsonBody()
    {
        return mGson.toJson(mRegisterBindingModel);
    }

    private String composeServerAddress()
    {
        return getAPIURL() + AppConsts.API_REGISTER;
    }

    public interface OnRegistrationListener
    {
        void onRegistrationCompleted(SyncDataResult syncDataResult);
    }

    private static class RegisterBindingModel
    {
        @Expose
        @SerializedName("Email")
        private String mEmail;

        @Expose
        @SerializedName("Password")
        private String mPassword;

        @Expose
        @SerializedName("ConfirmPassword")
        private String mConfirmPassword;

        @Expose
        @SerializedName("AuthorizationCode")
        private final String mAuthorizationCode;

        public RegisterBindingModel(String email, String password, String confirmPassword)
        {
            mEmail = email;
            mPassword = password;
            mConfirmPassword = confirmPassword;
            mAuthorizationCode = AppConsts.API_AUTHORIZATION_CODE;
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
