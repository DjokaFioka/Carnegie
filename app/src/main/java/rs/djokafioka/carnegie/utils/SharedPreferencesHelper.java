package rs.djokafioka.carnegie.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

/**
 * Created by Djordje on 20.1.2022..
 */
@SuppressLint("ApplySharedPref")
public class SharedPreferencesHelper
{
    public static final String API_URL_KEY = "settings_api_url";
    public static final String DEVICE_ID_KEY = "settings_device_id";
    public static final String APP_VERSION_KEY = "settings_app_version";
    public static final String REGISTERED_USER_KEY = "settings_registered_user";

    public static final String TOKEN = "preferences_token";

    private SharedPreferences mAppSharedPreferences;
    private Context mContext;

    private static SharedPreferencesHelper sInstance;

    private SharedPreferencesHelper(Context context)
    {
        mAppSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
    }

    public static void init(Context context)
    {
        if (sInstance == null)
        {
            sInstance = new SharedPreferencesHelper(context);
        }
    }

    public static SharedPreferencesHelper getInstance()
    {
        return sInstance;
    }

    public void setApiUrl(String url)
    {
        mAppSharedPreferences.edit().putString(API_URL_KEY, url).commit();
    }

    public String getApiUrl()
    {
        String apiURL = mAppSharedPreferences.getString(API_URL_KEY, "");
        if (apiURL != null && !apiURL.isEmpty() && !apiURL.endsWith("/"))
            apiURL += "/";
        return apiURL;
    }

    @SuppressLint("HardwareIds")
    public String getDeviceId()
    {
        return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void setRegisteredUser(String email)
    {
        mAppSharedPreferences.edit().putString(REGISTERED_USER_KEY, email).commit();
    }

    public String getRegisteredUser()
    {
        return mAppSharedPreferences.getString(REGISTERED_USER_KEY, "");
    }

    public void setToken(String token)
    {
        mAppSharedPreferences.edit().putString(TOKEN, token).commit();
    }

    public String getToken()
    {
        return mAppSharedPreferences.getString(TOKEN, "");
    }
}
