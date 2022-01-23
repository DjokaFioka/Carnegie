package rs.djokafioka.carnegie.controller;

import android.content.Context;
import android.os.Build;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import rs.djokafioka.carnegie.R;

/**
 * Created by Djordje on 20.1.2022..
 */
public class PasswordView extends LinearLayout
{

    private TextView mTxtPasswordCaption;
    private EditText mTxtPassword;
    private ImageView mImgShowHide;
    private boolean mIsPasswordVisible;

    public PasswordView(Context context)
    {
        super(context);
        init();
    }

    public PasswordView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public PasswordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PasswordView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        inflate(getContext(), R.layout.view_password, this);

        mTxtPasswordCaption = (TextView) findViewById(R.id.txt_password_caption);
        mTxtPassword = (EditText) findViewById(R.id.txt_password);
        mImgShowHide = (ImageView) findViewById(R.id.img_show_hide);

        mImgShowHide.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showHidePassword();
            }
        });
    }

    public void setPasswordCaption(CharSequence caption) {
        mTxtPasswordCaption.setText(caption);
    }

    public void setPasswordCaption(int stringResId) {
        mTxtPasswordCaption.setText(stringResId);
    }

    public void setPassword(CharSequence password) {
        mTxtPassword.setText(password);
    }

    public void setPassword(int stringResId) {
        mTxtPassword.setText(stringResId);
    }

    public String getPassword()
    {
        return mTxtPassword.getText().toString();
    }

    private void showHidePassword()
    {
        if (mIsPasswordVisible)
        {
            mImgShowHide.setImageResource(R.drawable.vd_eye_off);
            mTxtPassword.setTransformationMethod(new PasswordTransformationMethod());
            mIsPasswordVisible = false;
        }
        else
        {
            mImgShowHide.setImageResource(R.drawable.vd_eye_on);
            mTxtPassword.setTransformationMethod(null);
            mIsPasswordVisible = true;
        }
    }
}
