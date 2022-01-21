package rs.djokafioka.carnegie.sync;

import rs.djokafioka.carnegie.sync.model.SyncDataResult;

/**
 * Created by Djordje on 20.1.2022..
 */
public class LoginTask extends BaseBackgroundTask
{
    private static final String TAG = "LoginTask";

    private String mEmail;
    private String mPassword;
    private SyncDataResult mSyncDataResult;
    private OnLoginListener mOnLoginListener;

    public LoginTask(String email, String password, OnLoginListener onLoginListener)
    {
        mEmail = email;
        mPassword = password;
        mSyncDataResult = new SyncDataResult();
        mOnLoginListener = onLoginListener;
    }

    @Override
    public void doInBackground()
    {
        AuthTokenTask authTokenTask = new AuthTokenTask(mEmail, mPassword, new AuthTokenTask.OnAuthTokenListener()
        {
            @Override
            public void onAuthTokenCompleted(SyncDataResult syncDataResult)
            {
                mSyncDataResult = syncDataResult;
                if (mOnLoginListener != null)
                    mOnLoginListener.onLoginCompleted(mSyncDataResult);
            }
        });
        authTokenTask.execute();

    }

    @Override
    public void onProgressUpdate(double progress)
    {

    }

    @Override
    public void onPostExecute()
    {
        if (mOnLoginListener != null)
            mOnLoginListener.onLoginCompleted(mSyncDataResult);
    }

    public interface OnLoginListener
    {
        void onLoginCompleted(SyncDataResult syncDataResult);
    }
}
