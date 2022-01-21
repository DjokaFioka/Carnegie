package rs.djokafioka.carnegie;

import android.app.Application;
import android.content.Context;

import rs.djokafioka.carnegie.utils.SharedPreferencesHelper;

/**
 * Created by Djordje on 20.1.2022..
 */
public class CarnegieApplication extends Application
{
    @Override
    protected void attachBaseContext(Context base)
    {
        SharedPreferencesHelper.init(base);
        super.attachBaseContext(base);
    }
}
