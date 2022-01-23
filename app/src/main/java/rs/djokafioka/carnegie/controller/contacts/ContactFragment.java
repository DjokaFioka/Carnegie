package rs.djokafioka.carnegie.controller.contacts;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.HttpURLConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import rs.djokafioka.carnegie.MainActivity;
import rs.djokafioka.carnegie.R;
import rs.djokafioka.carnegie.model.Contact;
import rs.djokafioka.carnegie.sync.model.SyncDataResult;
import rs.djokafioka.carnegie.sync.post_data.SaveContactTask;

/**
 * Created by Djordje on 23.1.2022..
 */
public class ContactFragment extends Fragment implements SaveContactTask.OnContactChangeListener
{
    private static final String TAG = "ContactFragment";

    public static final String CONTACT_EXTRA_ARGUMENT = "rs.djokafioka.carnegie.CONTACT_EXTRA_ARGUMENT";

    private EditText mTxtContactName;
    private EditText mTxtPhone;
    private EditText mTxtEmail;
    private EditText mTxtAddress;
    private Button mBtnSave;

    private Contact mContact;

    private boolean mIsRunning;
    private SaveContactTask mSaveContactTask;

    public static ContactFragment newInstance(Contact contact)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CONTACT_EXTRA_ARGUMENT, contact);

        ContactFragment contactFragment = new ContactFragment();
        contactFragment.setArguments(bundle);
        return contactFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_contact, null);

        mTxtContactName = (EditText) v.findViewById(R.id.txt_contact_name);
        mTxtPhone = (EditText) v.findViewById(R.id.txt_contact_phone);
        mTxtEmail = (EditText) v.findViewById(R.id.txt_contact_email);
        mTxtAddress = (EditText) v.findViewById(R.id.txt_contact_address);
        mBtnSave = (Button) v.findViewById(R.id.btn_save);

        mBtnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mTxtContactName.getText().toString().isEmpty())
                {
                    Toast.makeText(getContext(), R.string.contact_name_mandatory_error, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mContact.setName(mTxtContactName.getText().toString());
                    mContact.setPhone(mTxtPhone.getText().toString());
                    mContact.setEmail(mTxtEmail.getText().toString());
                    mContact.setAddress(mTxtAddress.getText().toString());

                    if (mContact.getId() == 0)
                        addContact(mContact);
                    else
                        editContact(mContact);
                }
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null)
            mContact = (Contact) getArguments().getSerializable(CONTACT_EXTRA_ARGUMENT);

        mTxtContactName.setText(mContact != null && mContact.getName() != null ? mContact.getName() : "");
        mTxtPhone.setText(mContact != null && mContact.getPhone() != null ? mContact.getPhone() : "");
        mTxtEmail.setText(mContact != null && mContact.getEmail() != null ? mContact.getEmail() : "");
        mTxtAddress.setText(mContact != null && mContact.getAddress() != null ? mContact.getAddress() : "");

    }

    @Override
    public void onResume()
    {
        super.onResume();
        setTitle();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        stopContactTask();
    }

    private void setTitle()
    {
        getActivity().setTitle(mContact != null && mContact.getName() != null ? mContact.getName() : getString(R.string.new_contact));
    }

    private void addContact(Contact contact)
    {
        if (!mIsRunning)
        {
            mIsRunning = true;
            ((MainActivity)getActivity()).showMainProgressBar();
            mSaveContactTask = new SaveContactTask(SaveContactTask.ADD_CONTACT, contact, this);
            mSaveContactTask.execute();
        }
    }

    private void editContact(Contact contact)
    {
        if (!mIsRunning)
        {
            mIsRunning = true;
            ((MainActivity)getActivity()).showMainProgressBar();
            mSaveContactTask = new SaveContactTask(SaveContactTask.EDIT_CONTACT, contact, this);
            mSaveContactTask.execute();
        }
    }

    private void stopContactTask()
    {
        if (mSaveContactTask != null && mIsRunning)
        {
            ((MainActivity) getActivity()).dismissMainProgressBar();
            mSaveContactTask.cancel();
            mIsRunning = false;
        }
    }

    @Override
    public void onContactChanged(SyncDataResult syncDataResult)
    {
        mIsRunning = false;
        ((MainActivity) getActivity()).dismissMainProgressBar();
        if (syncDataResult.isSuccess() && syncDataResult.getError().isEmpty())
        {
            Toast.makeText(getContext(), R.string.contact_saved_success, Toast.LENGTH_SHORT).show();
            setTitle();
        }
        else
        {
            String errorMessage = syncDataResult.getError();
            if (syncDataResult.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                errorMessage = getString(R.string.api_unauthorized_error);

            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.dlg_error_header))
                    .setMessage(getString(R.string.contact_saved_error, errorMessage))
                    .setPositiveButton(R.string.ok, null)
                    .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.vd_error_red_24dp, null))
                    .show();
        }
    }
}
