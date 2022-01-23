package rs.djokafioka.carnegie.sync.post_data;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.util.concurrent.TimeUnit;

import androidx.annotation.IntDef;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rs.djokafioka.carnegie.model.Contact;
import rs.djokafioka.carnegie.sync.BaseBackgroundTask;
import rs.djokafioka.carnegie.sync.model.SyncDataResult;
import rs.djokafioka.carnegie.utils.AppConsts;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Djordje on 23.1.2022..
 */
public class SaveContactTask extends BaseBackgroundTask
{
    @Retention(SOURCE)
    @IntDef({ADD_CONTACT, EDIT_CONTACT})
    public @interface SaveType{}
    public static final int ADD_CONTACT = 0;
    public static final int EDIT_CONTACT = 1;

    private final OkHttpClient mClient;
    private Call mCall;
    private Gson mGson;
    private SyncDataResult mSyncDataResult;
    private int mSaveType;
    private Contact mContact;
    private OnContactChangeListener mOnContactChangeListener;

    public SaveContactTask(@SaveType int saveType, Contact contact, OnContactChangeListener onContactChangeListener)
    {
        mClient = new OkHttpClient.Builder()
                .connectTimeout(AppConsts.CONNECTIVITY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(AppConsts.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AppConsts.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        mGson = new Gson();
        mSyncDataResult = new SyncDataResult();
        mSaveType = saveType;
        mContact = contact;
        mOnContactChangeListener = onContactChangeListener;
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
                if (mSaveType == ADD_CONTACT)
                {
                    body = response.body();
                    if(body != null)
                    {
                        Contact c = mGson.fromJson(body.string(), Contact.class);
                        mContact.setId(c.getId());
                    }
                }
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
        if (mOnContactChangeListener != null)
            mOnContactChangeListener.onContactChanged(mSyncDataResult);
    }

    private String composeServerAddress()
    {
        if (mSaveType == EDIT_CONTACT)
            return getAPIURL() + AppConsts.API_EDIT_CONTACT;
        else
            return getAPIURL() + AppConsts.API_ADD_CONTACT;
    }

    private String getJsonBody()
    {
        return mGson.toJson(mContact);
    }

    public interface OnContactChangeListener
    {
        void onContactChanged(SyncDataResult syncDataResult);
    }

}
