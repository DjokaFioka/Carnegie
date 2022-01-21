package rs.djokafioka.carnegie.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import rs.djokafioka.carnegie.MainActivity;
import rs.djokafioka.carnegie.R;
import rs.djokafioka.carnegie.controller.contacts.ContactListFragment;
import rs.djokafioka.carnegie.sync.LoginTask;
import rs.djokafioka.carnegie.sync.ResetPasswordTask;
import rs.djokafioka.carnegie.sync.model.SyncDataResult;
import rs.djokafioka.carnegie.utils.SharedPreferencesHelper;

/**
 * Created by Djordje on 20.1.2022..
 */
public class LoginFragment extends Fragment implements LoginTask.OnLoginListener, ResetPasswordTask.OnResetPasswordListener
{
    private static final String TAG = "LoginFragment";

    private EditText mTxtEmail;
    private EditText mTxtPassword;
    private Button mBtnLogin;
    private Button mBtnRegister;
    private ImageView mImgShowHidePassword;
    private ImageView mImgForgottenPassword;
    private ImageView mImgSettings;

    private boolean mIsPasswordVisible;

    private LoginTask mLoginTask;
    private boolean mIsRunning;

    private ResetPasswordTask mResetPasswordTask;
    private boolean mIsForgottenPassTaskRunning;

    public static LoginFragment newInstance()
    {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_login, null);

        mTxtEmail = (EditText) v.findViewById(R.id.txt_email);
        mTxtPassword = (EditText) v.findViewById(R.id.txt_password);
        mImgShowHidePassword = (ImageView) v.findViewById(R.id.img_show_hide_pass);
        mImgForgottenPassword = (ImageView) v.findViewById(R.id.img_forgotten_pass);
        mBtnLogin = (Button) v.findViewById(R.id.btn_login);
        mBtnRegister = (Button) v.findViewById(R.id.btn_register);
        mImgSettings = (ImageView) v.findViewById(R.id.img_settings);

        mImgShowHidePassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showHidePassword();
            }
        });

        mImgForgottenPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mTxtEmail.getText().toString().isEmpty())
                {
                    Toast.makeText(getContext(), R.string.email_empty_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                sendNewPassword(mTxtEmail.getText().toString());
            }
        });

        mBtnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mTxtEmail.getText().toString().isEmpty())
                {
                    Toast.makeText(getContext(), R.string.email_empty_error, Toast.LENGTH_SHORT).show();
                }
                else if (mTxtPassword.getText().toString().isEmpty())
                {
                    Toast.makeText(getContext(), R.string.password_empty_error, Toast.LENGTH_SHORT).show();
                }
                else if (SharedPreferencesHelper.getInstance().getApiUrl().isEmpty())
                {
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_edittext, null);
                    final EditText txtAPIURL = (EditText) dialogView.findViewById(R.id.dlg_text);

                    new AlertDialog.Builder(getContext())
                            .setIcon(R.drawable.vd_cloud)
                            .setMessage(R.string.api_url_dlg_msg)
                            .setTitle(R.string.api_url)
                            .setView(dialogView)
                            .setPositiveButton(R.string.dlg_confirm, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    String apiURL = txtAPIURL.getText().toString();
                                    if (!apiURL.isEmpty())
                                    {
                                        SharedPreferencesHelper.getInstance().setApiUrl(apiURL);
                                        loginUser(mTxtEmail.getText().toString(), mTxtPassword.getText().toString());
                                    }
                                    else
                                    {
                                        Toast.makeText(getContext(), R.string.api_url_empty_login_error, Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.dlg_cancel, null)
                            .show();
                }
                else
                {
                    loginUser(mTxtEmail.getText().toString(), mTxtPassword.getText().toString());
                }
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((MainActivity)getActivity()).showFragment(RegisterFragment.newInstance(), true);
            }
        });

        mImgSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((MainActivity)getActivity()).showFragment(SettingsFragment.newInstance(), true);
            }
        });

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().setTitle(R.string.app_name);
        mTxtEmail.requestFocus();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        stopLoginTask();
        stopSendingNewPassword();
    }

    private void showHidePassword()
    {
        if (mIsPasswordVisible)
        {
            mImgShowHidePassword.setImageResource(R.drawable.vd_eye_off);
            mTxtPassword.setTransformationMethod(new PasswordTransformationMethod());
            mIsPasswordVisible = false;
        }
        else
        {
            mImgShowHidePassword.setImageResource(R.drawable.vd_eye_on);
            mTxtPassword.setTransformationMethod(null);
            mIsPasswordVisible = true;
        }
    }

    private void loginUser(String email, String password)
    {
        if (!mIsRunning)
        {
            mIsRunning = true;
            ((MainActivity)getActivity()).showMainProgressBar();
            mLoginTask = new LoginTask(email, password, this);
            mLoginTask.execute();
        }
    }

    private void stopLoginTask()
    {
        if (mLoginTask != null && mIsRunning)
        {
            ((MainActivity) getActivity()).dismissMainProgressBar();
            mLoginTask.cancel();
            mIsRunning = false;
        }
    }

    private void sendNewPassword(String email)
    {
        if (!mIsForgottenPassTaskRunning)
        {
            mIsForgottenPassTaskRunning = true;
            ((MainActivity)getActivity()).showMainProgressBar();
            mResetPasswordTask = new ResetPasswordTask(email, this);
            mResetPasswordTask.execute();
        }
    }

    private void stopSendingNewPassword()
    {
        if (mResetPasswordTask != null && mIsForgottenPassTaskRunning)
        {
            ((MainActivity) getActivity()).dismissMainProgressBar();
            mResetPasswordTask.cancel();
            mIsForgottenPassTaskRunning = false;
        }
    }

    @Override
    public void onLoginCompleted(SyncDataResult syncDataResult)
    {
        mIsRunning = false;
        ((MainActivity) getActivity()).dismissMainProgressBar();
        if (syncDataResult.isSuccess() && syncDataResult.getError().isEmpty())
        {
            Toast.makeText(getContext(), R.string.login_success_message, Toast.LENGTH_SHORT).show();
            ((MainActivity)getActivity()).showFragment(ContactListFragment.newInstance(), false);
        }
        else
        {
            //TODO Handle different Http Response codes

            String errorMessage = syncDataResult.getError();

            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.dlg_error_header))
                    .setMessage(getString(R.string.dlg_login_error_message, errorMessage))
                    .setPositiveButton(R.string.ok, null)
                    .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.vd_error_red_24dp, null))
                    .show();
        }
    }

    @Override
    public void onSendNewPasswordCompleted(SyncDataResult syncDataResult)
    {
        ((MainActivity) getActivity()).dismissMainProgressBar();
        mIsForgottenPassTaskRunning = false;
        if (syncDataResult.isSuccess() && syncDataResult.getError().isEmpty())
        {
            Toast.makeText(getContext(), R.string.forgotten_password_on_email, Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getContext(), syncDataResult.getError(), Toast.LENGTH_SHORT).show();
        }
    }
}
