package rs.djokafioka.carnegie.sync;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import rs.djokafioka.carnegie.utils.SharedPreferencesHelper;

/**
 * Created by Djordje on 20.1.2022..
 */
public abstract class BaseBackgroundTask
{
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Executor mBackgroundExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean mIsCancelled;

    public void execute(){
        mBackgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (!mIsCancelled)
                    doInBackground();
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mIsCancelled)
                            onPostExecute();
                    }
                });
            }
        });
    }

    public void cancel(){
        mIsCancelled = true;
    }

    public boolean isCancelled(){
        return mIsCancelled;
    }

    public String getToken()
    {
        return SharedPreferencesHelper.getInstance().getToken();
    }

    public String getAPIURL()
    {
        return SharedPreferencesHelper.getInstance().getApiUrl();
    }

    public String getHeaderFormattedToken()
    {
        return "Bearer " + getToken();
    }

    public abstract void doInBackground();
    public abstract void onPostExecute();
}
