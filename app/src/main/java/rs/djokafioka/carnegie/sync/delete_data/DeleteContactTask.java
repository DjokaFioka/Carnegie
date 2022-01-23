package rs.djokafioka.carnegie.sync.delete_data;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rs.djokafioka.carnegie.model.Contact;
import rs.djokafioka.carnegie.sync.BaseBackgroundTask;
import rs.djokafioka.carnegie.sync.model.SyncContactsResult;
import rs.djokafioka.carnegie.utils.AppConsts;

/**
 * Created by Djordje on 23.1.2022..
 */
public class DeleteContactTask extends BaseBackgroundTask
{

    private final OkHttpClient mClient;
    private Call mCall;
    private Gson mGson;
    private SyncContactsResult mSyncContactsResult;
    private int mPosition;
    private OnContactDeleteListener mOnContactDeleteListener;

    /**
     * Use this constructor to delete all contacts
     * @param onContactDeleteListener callback interface
     */
    public DeleteContactTask(OnContactDeleteListener onContactDeleteListener)
    {
        this(null, onContactDeleteListener);
    }

    /**
     * Use this constructor to delete a single contact
     * @param contact contact to delete
     * @param onContactDeleteListener callback interface
     */
    public DeleteContactTask(Contact contact, OnContactDeleteListener onContactDeleteListener)
    {
        mClient = new OkHttpClient.Builder()
                .connectTimeout(AppConsts.CONNECTIVITY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(AppConsts.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AppConsts.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        mGson = new Gson();
        mSyncContactsResult = new SyncContactsResult();
        mSyncContactsResult.setContact(contact);
        mOnContactDeleteListener = onContactDeleteListener;
    }

    @Override
    public void doInBackground()
    {
        Request request = new Request.Builder()
                .url(composeServerAddress())
                .header("Authorization", getHeaderFormattedToken())
                .delete()
                .build();

        Response response = null;
        ResponseBody body;

        try
        {
            mCall = mClient.newCall(request);
            response = mCall.execute();

            if (response.isSuccessful())
            {
                mSyncContactsResult.setSuccess(true);
            }
            else
            {
                mSyncContactsResult.setSuccess(false);
                mSyncContactsResult.setResponseCode(response.code());

                body = response.body();
                if (body != null)
                {
                    JSONObject json = new JSONObject(body.string());
                    String errorMessage = json.getString("Message");
                    mSyncContactsResult.setError(errorMessage);
                }
                else
                {
                    mSyncContactsResult.setError(response.message());
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            mSyncContactsResult.setError(e.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            mSyncContactsResult.setError(e.getMessage());
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
        if (mOnContactDeleteListener != null)
            mOnContactDeleteListener.onContactDeleteCompleted(mSyncContactsResult);
    }

    private String composeServerAddress()
    {
        //If contact is null it means that all contacts should be deleted. Otherwise, we compose the URL from contact Id
        if (mSyncContactsResult.getContact() == null)
            return getAPIURL() + AppConsts.API_DELETE_ALL_CONTACTS;
        else
            return getAPIURL() + AppConsts.API_DELETE_CONTACT + "?id=" + mSyncContactsResult.getContact().getId();
    }

    public void setPosition(int position)
    {
        mSyncContactsResult.setPosition(position);
    }

    public interface OnContactDeleteListener
    {
        void onContactDeleteCompleted(SyncContactsResult syncContactsResult);
    }
}
