package rs.djokafioka.carnegie.controller.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import rs.djokafioka.carnegie.R;
import rs.djokafioka.carnegie.controller.custom.PasswordView;

/**
 * Created by Djordje on 23.1.2022..
 */
public class PasswordChangeDialog extends DialogFragment
{
    public static final String TAG = "PasswordChangeDialog";

    private PasswordView mOldPassword;
    private PasswordView mNewPassword;
    private PasswordView mConfirmPassword;

    private OnPasswordChangeListener mOnPasswordChangeListener;

    public static PasswordChangeDialog newInstance()
    {
        return new PasswordChangeDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getTargetFragment() instanceof OnPasswordChangeListener) {
            mOnPasswordChangeListener = (OnPasswordChangeListener) getTargetFragment();
        } else {
            throw new IllegalArgumentException("Calling Fragment must implement OnPasswordChangeListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_password_change, null);

        mOldPassword = (PasswordView) view.findViewById(R.id.old_password);
        mNewPassword = (PasswordView) view.findViewById(R.id.new_password);
        mConfirmPassword = (PasswordView) view.findViewById(R.id.confirm_password);

        mOldPassword.setPasswordCaption(R.string.old_password);
        mNewPassword.setPasswordCaption(R.string.new_password);
        mConfirmPassword.setPasswordCaption(R.string.confirm_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.change_password)
                .setIcon(R.drawable.vd_user_blue_24)
                .setView(view)
                .setPositiveButton(R.string.dlg_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnPasswordChangeListener != null)
                            mOnPasswordChangeListener.onPasswordChangeConfirmed(mOldPassword.getPassword(),
                                    mNewPassword.getPassword(), mConfirmPassword.getPassword());
                    }
                })
                .setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public interface OnPasswordChangeListener
    {
        void onPasswordChangeConfirmed(String oldPassword, String newPassword, String confirmPassword);
    }
}
