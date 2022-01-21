package rs.djokafioka.carnegie.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import rs.djokafioka.carnegie.MainActivity;
import rs.djokafioka.carnegie.R;
import rs.djokafioka.carnegie.sync.RegistrationTask;
import rs.djokafioka.carnegie.sync.model.SyncDataResult;
import rs.djokafioka.carnegie.utils.SharedPreferencesHelper;

/**
 * Created by Djordje on 21.1.2022..
 */
public class RegisterFragment extends Fragment implements RegistrationTask.OnRegistrationListener
{
    public static final String TAG = "RegisterFragment";

    private EditText mTxtEmail;
    private PasswordView mPassword;
    private PasswordView mConfirmPassword;
    private Button mBtnRegister;

    private boolean mIsRunning;
    private RegistrationTask mRegistrationTask;

    public static RegisterFragment newInstance()
    {
        return new RegisterFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_register, null);

        mTxtEmail = (EditText) v.findViewById(R.id.txt_email);
        mPassword = (PasswordView) v.findViewById(R.id.password);
        mConfirmPassword = (PasswordView) v.findViewById(R.id.confirm_password);
        mBtnRegister = (Button) v.findViewById(R.id.btn_register);

        mPassword.setPasswordCaption(R.string.password);
        mConfirmPassword.setPasswordCaption(R.string.confirm_password);

        mBtnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String email = mTxtEmail.getText().toString();
                final String password = mPassword.getPassword();
                final String confirmPassword = mConfirmPassword.getPassword();
                if (email.isEmpty())
                {
                    Toast.makeText(getContext(), R.string.email_empty_error, Toast.LENGTH_SHORT).show();
                }
                else if (password.isEmpty())
                {
                    Toast.makeText(getContext(), R.string.password_empty_error, Toast.LENGTH_SHORT).show();
                }
                else if (!password.equals(confirmPassword))
                {
                    Toast.makeText(getContext(), R.string.compare_password_error, Toast.LENGTH_SHORT).show();
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
                                        registerUser(email, password, confirmPassword);
                                    }
                                    else
                                    {
                                        Toast.makeText(getContext(), R.string.api_url_empty_registration_error, Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.dlg_cancel, null)
                            .show();
                }
                else
                {
                    registerUser(email, password, confirmPassword);
                }
            }
        });

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().setTitle(R.string.menu_registration);
        mTxtEmail.requestFocus();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        stopLoginTask();
    }

    private void registerUser(String email, String password, String confirmPassword)
    {
        if (!mIsRunning)
        {
            mIsRunning = true;
            ((MainActivity)getActivity()).showMainProgressBar();
            mRegistrationTask = new RegistrationTask(email, password, confirmPassword, this);
            mRegistrationTask.execute();
        }
    }

    private void stopLoginTask()
    {
        if (mRegistrationTask != null && mIsRunning)
        {
            ((MainActivity) getActivity()).dismissMainProgressBar();
            mRegistrationTask.cancel();
            mIsRunning = false;
        }
    }

    @Override
    public void onRegistrationCompleted(SyncDataResult syncDataResult)
    {
        mIsRunning = false;
        ((MainActivity) getActivity()).dismissMainProgressBar();
        if (syncDataResult.getError().isEmpty())
        {
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.dlg_notification_header))
                    .setMessage(R.string.registration_success_message)
                    .setPositiveButton(R.string.dlg_confirm, null)
                    .setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            getActivity().getSupportFragmentManager().popBackStackImmediate();
                        }
                    })
                    .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.vd_notification, null))
                    .show();
        }
        else
        {
            //TODO Handle different Http Response codes

            String errorMessage = syncDataResult.getError();

            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.dlg_error_header))
                    .setMessage(getString(R.string.dlg_registration_error_message, errorMessage))
                    .setPositiveButton(R.string.ok, null)
                    .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.vd_error_red_24dp, null))
                    .show();
        }
    }
}
