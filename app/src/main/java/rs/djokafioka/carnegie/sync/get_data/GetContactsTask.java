package rs.djokafioka.carnegie.sync.get_data;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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
 * Created by Djordje on 21.1.2022..
 */
public class GetContactsTask extends BaseBackgroundTask
{
    private static final String TAG = "GetContactsTask";

    private final OkHttpClient mClient;
    private Call mCall;
    private Gson mGson;
    private SyncContactsResult mSyncContactsResult;
    private OnGetContactsListener mOnGetContactsListener;

    public GetContactsTask(OnGetContactsListener onGetContactsListener)
    {
        mClient = new OkHttpClient.Builder()
                .connectTimeout(AppConsts.CONNECTIVITY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(AppConsts.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AppConsts.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        mGson = new Gson();
        mSyncContactsResult = new SyncContactsResult();
        mOnGetContactsListener = onGetContactsListener;
    }

    @Override
    public void doInBackground()
    {
        Request request = new Request.Builder()
                .url(composeServerAddress())
                .header("Authorization", getHeaderFormattedToken())
                .get()
                .build();

        Response response = null;
        ResponseBody body;
        ArrayList<Contact> contactList = new ArrayList<>();

        try
        {
            JsonReader reader = null;
            mCall = mClient.newCall(request);
            response = mCall.execute();

            if (response.isSuccessful())
            {
                mSyncContactsResult.setSuccess(true);
                body = response.body();
                if(body != null)
                {
                    reader = new JsonReader(body.charStream());
                    reader.beginArray();
                    while (reader.hasNext())
                    {
                        if (isCancelled())
                            break;
                        Contact contact = mGson.fromJson(reader, Contact.class);
                        contactList.add(contact);
                    }
                    reader.endArray();
                }

                mSyncContactsResult.setContactList(contactList);
            }
            else
            {
                mSyncContactsResult.setSuccess(false);
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
    public void onProgressUpdate(double progress)
    {

    }

    @Override
    public void onPostExecute()
    {
        if (mOnGetContactsListener != null)
            mOnGetContactsListener.onGetContactsCompleted(mSyncContactsResult);
    }

    private String composeServerAddress()
    {
        return getAPIURL() + AppConsts.API_GET_CONTACTS;
    }

    public interface OnGetContactsListener
    {
        void onGetContactsCompleted(SyncContactsResult syncContactResult);
    }

}
