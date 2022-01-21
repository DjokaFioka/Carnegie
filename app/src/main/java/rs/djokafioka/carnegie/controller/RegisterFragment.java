package rs.djokafioka.carnegie.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import rs.djokafioka.carnegie.R;

/**
 * Created by Djordje on 21.1.2022..
 */
public class RegisterFragment extends Fragment
{
    public static final String TAG = "RegisterFragment";

    private EditText mTxtEmail;
    private PasswordView mPassword;
    private PasswordView mConfirmPassword;
    private Button mBtnRegister;

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
                String email = mTxtEmail.getText().toString();
                if (email.isEmpty())
                {
                    Toast.makeText(getContext(), R.string.email_empty_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                String password = mPassword.getPassword();
                if (password.isEmpty())
                {
                    Toast.makeText(getContext(), R.string.password_empty_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                String confirmPassword = mConfirmPassword.getPassword();
                if (!password.equals(confirmPassword))
                {
                    Toast.makeText(getContext(), R.string.compare_password_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                registerUser(email, password, confirmPassword);
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
    }

    private void registerUser(String email, String password, String confirmPassword)
    {

    }
}
