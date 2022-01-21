package rs.djokafioka.carnegie.sync.model;

/**
 * Created by Djordje on 20.1.2022..
 */
public class SyncDataResult
{
    private boolean mIsSuccess;
    private String mError;
    private int mResponseCode;

    public SyncDataResult(boolean isSuccess, String error)
    {
        setSuccess(isSuccess);
        setError(error);
    }

    public SyncDataResult()
    {
        this(false, "");
    }

    public boolean isSuccess()
    {
        return mIsSuccess;
    }

    public void setSuccess(boolean success)
    {
        mIsSuccess = success;
    }

    public String getError()
    {
        return mError;
    }

    public void setError(String error)
    {
        mError = error;
    }

    public int getResponseCode()
    {
        return mResponseCode;
    }

    public void setResponseCode(int responseCode)
    {
        mResponseCode = responseCode;
    }
}
