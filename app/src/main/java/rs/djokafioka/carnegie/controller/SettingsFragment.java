package rs.djokafioka.carnegie.controller;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import rs.djokafioka.carnegie.R;
import rs.djokafioka.carnegie.utils.SharedPreferencesHelper;

/**
 * Created by Djordje on 20.1.2022..
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private EditTextPreference mAPIURLPrefs;
    private Preference mDeviceIdPrefs;
    private Preference mAppVersionPrefs;
    private Preference mRegisteredUserPrefs;

    public static SettingsFragment newInstance()
    {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        mAPIURLPrefs = (EditTextPreference) findPreference(SharedPreferencesHelper.API_URL_KEY);
        mAPIURLPrefs.setSummary(mAPIURLPrefs.getText());

        mDeviceIdPrefs = findPreference(SharedPreferencesHelper.DEVICE_ID_KEY);
        mDeviceIdPrefs.setSummary(SharedPreferencesHelper.getInstance().getDeviceId());

        mAppVersionPrefs = findPreference(SharedPreferencesHelper.APP_VERSION_KEY);
        mAppVersionPrefs.setSummary(getAppVersion());

        mRegisteredUserPrefs = findPreference(SharedPreferencesHelper.REGISTERED_USER_KEY);
        mRegisteredUserPrefs.setSummary(SharedPreferencesHelper.getInstance().getRegisteredUser());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setDivider(null);
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        getActivity().setTitle(R.string.menu_settings);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(SharedPreferencesHelper.API_URL_KEY))
        {
            mAPIURLPrefs.setSummary(SharedPreferencesHelper.getInstance().getApiUrl());
            SharedPreferencesHelper.getInstance().setApiUrl(mAPIURLPrefs.getText());
        }
    }

    private String getAppVersion()
    {
        try
        {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            return packageInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return "N/A";
    }


}
