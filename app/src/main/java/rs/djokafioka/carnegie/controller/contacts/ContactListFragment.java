package rs.djokafioka.carnegie.controller.contacts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import rs.djokafioka.carnegie.BackButtonHandler;
import rs.djokafioka.carnegie.MainActivity;
import rs.djokafioka.carnegie.R;
import rs.djokafioka.carnegie.controller.dialogs.PasswordChangeDialog;
import rs.djokafioka.carnegie.controller.settings.SettingsFragment;
import rs.djokafioka.carnegie.model.Contact;
import rs.djokafioka.carnegie.sync.get_data.GetContactsTask;
import rs.djokafioka.carnegie.sync.model.SyncContactsResult;
import rs.djokafioka.carnegie.sync.delete_data.DeleteContactTask;
import rs.djokafioka.carnegie.sync.model.SyncDataResult;
import rs.djokafioka.carnegie.sync.post_data.ChangePasswordTask;

/**
 * Created by Djordje on 21.1.2022..
 */
public class ContactListFragment extends Fragment implements BackButtonHandler, ContactListAdapter.OnContactItemClickListener,
        GetContactsTask.OnGetContactsListener, DeleteContactTask.OnContactDeleteListener, PasswordChangeDialog.OnPasswordChangeListener
{
    private static final String TAG = "ContactListFragment";
    public static final int PASSWORD_CHANGE_DIALOG_REQUEST_CODE = 1122;

    private ImageView mImgDeleteAll;
    private RecyclerView mRecContactList;
    private ContactListAdapter mContactListAdapter;
    private FloatingActionButton mFABAddContact;

    private ArrayList<Contact> mContactList;
    private String mSearchText = "";

    private boolean mIsGetContactsRunning;
    private GetContactsTask mGetContactsTask;

    private boolean mIsDeleteRunning;
    private DeleteContactTask mDeleteContactTask;

    private boolean mIsPasswordChangeRunning;
    private ChangePasswordTask mChangePasswordTask;

    public static ContactListFragment newInstance()
    {
        return new ContactListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mContactList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);

        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_contact_list, null);

        mImgDeleteAll = (ImageView) v.findViewById(R.id.img_delete_all);
        mImgDeleteAll.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.dlg_warning_header))
                        .setMessage(R.string.contact_delete_all_confirmation)
                        .setPositiveButton(R.string.dlg_confirm, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                deleteAllContacts();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.dlg_cancel, null)
                        .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.vd_warning_accent, null))
                        .show();
            }
        });

        mRecContactList = (RecyclerView) v.findViewById(R.id.recycler_contacts);
        mRecContactList.setHasFixedSize(true);
        mRecContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecContactList.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        mFABAddContact = (FloatingActionButton) v.findViewById(R.id.fab_new_contact);
        mFABAddContact.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Contact contact = new Contact();
                ((MainActivity)getActivity()).showFragment(ContactFragment.newInstance(contact), true);
            }
        });

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().setTitle(R.string.contact_header);
        refreshContactList(mSearchText);
        getContacts();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        stopGettingContacts();
        stopDeletingTask();
        stopChangingPassword();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.contact_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        MenuItem searchViewMenuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchViewMenuItem.getActionView();

        if (!mSearchText.equals(""))
        {
            searchView.setIconified(false);
            searchView.setQuery(mSearchText, false);
        }

        searchView.setQueryHint(getResources().getString(R.string.search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                mSearchText = query;
                refreshContactList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query)
            {
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener()
        {
            @Override
            public boolean onClose()
            {
                mSearchText = "";
                refreshContactList("");
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                ((MainActivity)getActivity()).showFragment(SettingsFragment.newInstance(), true);
                return true;
            case R.id.menu_change_password:
                showPasswordChangeDialog();
                return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed()
    {
        showLogoutDialog();
        return true;
    }

    private void refreshContactList(String searchText)
    {
        ArrayList<Contact> contactList;
        if (searchText.isEmpty())
        {
            contactList = mContactList;
        }
        else
        {
            contactList = new ArrayList<>();
            for (Contact contact : mContactList)
            {
                if(contact.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                        contact.getPhone().toLowerCase().contains(searchText.toLowerCase()) ||
                        contact.getEmail().toLowerCase().contains(searchText.toLowerCase()))
                {
                    contactList.add(contact);
                }
            }
        }

        if (contactList != null)
        {
            if (mContactListAdapter == null)
            {
                mContactListAdapter = new ContactListAdapter(contactList, this);
                mContactListAdapter.notifyDataSetChanged();
            }
            else
            {
                mContactListAdapter.updateContactList(contactList);
            }
            mRecContactList.setAdapter(mContactListAdapter);
        }
    }

    private void showLogoutDialog()
    {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.dlg_warning_header))
                .setMessage(R.string.dlg_logout_confirmation)
                .setPositiveButton(R.string.dlg_confirm, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (getActivity() != null)
                            getActivity().finish();
                    }
                })
                .setNegativeButton(R.string.dlg_cancel, null)
                .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.vd_warning_accent, null))
                .show();
    }

    private void getContacts()
    {
        if (!mIsGetContactsRunning)
        {
            mIsGetContactsRunning = true;
            //((MainActivity)getActivity()).showMainProgressBar();
            mGetContactsTask = new GetContactsTask(this);
            mGetContactsTask.execute();
        }
    }

    private void stopGettingContacts()
    {
        if (mGetContactsTask != null && mIsGetContactsRunning)
        {
            //((MainActivity) getActivity()).dismissMainProgressBar();
            mGetContactsTask.cancel();
            mIsGetContactsRunning = false;
        }
    }

    @Override
    public void onGetContactsCompleted(SyncContactsResult syncContactResult)
    {
        mIsGetContactsRunning = false;
        //((MainActivity) getActivity()).dismissMainProgressBar();
        if (syncContactResult.isSuccess() && syncContactResult.getError().isEmpty())
        {
            mContactList = syncContactResult.getContactList();
            if (mContactListAdapter != null)
                mContactListAdapter.updateContactList(mContactList);
        }
        else
        {
            String errorMessage = syncContactResult.getError();
            if (syncContactResult.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                errorMessage = getString(R.string.api_unauthorized_error);

            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.dlg_error_header))
                    .setMessage(getString(R.string.dlg_get_contact_list_error_message, errorMessage))
                    .setPositiveButton(R.string.ok, null)
                    .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.vd_error_red_24dp, null))
                    .show();
        }

    }

    @Override
    public void onContactItemDeleteClick(Contact contact, int position)
    {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.dlg_warning_header))
                .setMessage(R.string.contact_delete_confirmation)
                .setPositiveButton(R.string.dlg_confirm, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        deleteContact(contact, position);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dlg_cancel, null)
                .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.vd_warning_accent, null))
                .show();
    }

    @Override
    public void onContactItemClick(Contact contact)
    {
        ((MainActivity)getActivity()).showFragment(ContactFragment.newInstance(contact), true);
    }

    private void deleteContact(Contact contact, int position)
    {
        if (!mIsDeleteRunning)
        {
            mIsDeleteRunning = true;
            ((MainActivity)getActivity()).showMainProgressBar();
            mDeleteContactTask = new DeleteContactTask(contact, this);
            mDeleteContactTask.setPosition(position);
            mDeleteContactTask.execute();
        }
    }

    private void deleteAllContacts()
    {
        if (!mIsDeleteRunning)
        {
            if (mContactList != null && mContactList.size() == 0)
            {
                Toast.makeText(getContext(), R.string.contact_delete_error_no_contacts, Toast.LENGTH_SHORT).show();
            }
            else
            {
                mIsDeleteRunning = true;
                ((MainActivity)getActivity()).showMainProgressBar();
                mDeleteContactTask = new DeleteContactTask(this);
                mDeleteContactTask.execute();
            }
        }
    }

    private void stopDeletingTask()
    {
        if (mDeleteContactTask != null && mIsDeleteRunning)
        {
            ((MainActivity) getActivity()).dismissMainProgressBar();
            mDeleteContactTask.cancel();
            mIsDeleteRunning = false;
        }
    }

    @Override
    public void onContactDeleteCompleted(SyncContactsResult syncContactsResult)
    {
        mIsDeleteRunning = false;
        ((MainActivity) getActivity()).dismissMainProgressBar();
        if (syncContactsResult.isSuccess() && syncContactsResult.getError().isEmpty())
        {
            if (syncContactsResult.getContact() == null)
            {
                mContactList.clear();
                if (mContactListAdapter != null)
                    mContactListAdapter.updateContactList(mContactList);
            }
            else
            {
                if (mContactListAdapter != null)
                    mContactListAdapter.removeAt(syncContactsResult.getPosition());
            }
        }
        else
        {
            String error = syncContactsResult.getError();
            if (syncContactsResult.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                error = getString(R.string.api_unauthorized_error);

            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.dlg_error_header))
                    .setMessage(getString(R.string.contact_delete_error, error))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            getContacts();
                        }
                    })
                    .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.vd_error_red_24dp, null))
                    .show();
        }
    }

    private void showPasswordChangeDialog()
    {
        PasswordChangeDialog passwordChangeDialog = PasswordChangeDialog.newInstance();
        passwordChangeDialog.setTargetFragment(this, PASSWORD_CHANGE_DIALOG_REQUEST_CODE);
        passwordChangeDialog.show(getActivity().getSupportFragmentManager(), PasswordChangeDialog.TAG);
    }

    @Override
    public void onPasswordChangeConfirmed(String oldPassword, String newPassword, String confirmPassword)
    {
        if (oldPassword.isEmpty())
        {
            Toast.makeText(getContext(), R.string.old_password_empty_error, Toast.LENGTH_SHORT).show();
        }
        if (newPassword.isEmpty())
        {
            Toast.makeText(getContext(), R.string.new_password_empty_error, Toast.LENGTH_SHORT).show();
        }
        else if (!newPassword.equals(confirmPassword))
        {
            Toast.makeText(getContext(), R.string.compare_password_error, Toast.LENGTH_SHORT).show();
        }
        else
        {
            startChangePasswordTask(oldPassword, newPassword, confirmPassword);
        }
    }

    private void startChangePasswordTask(String oldPassword, String newPassword, String confirmPassword)
    {
        if (!mIsPasswordChangeRunning)
        {
            mIsPasswordChangeRunning = true;
            ((MainActivity)getActivity()).showMainProgressBar();
            mChangePasswordTask = new ChangePasswordTask(oldPassword, newPassword, confirmPassword, new ChangePasswordTask.OnChangePasswordListener()
            {
                @Override
                public void onChangePasswordCompleted(SyncDataResult syncDataResult)
                {
                    mIsPasswordChangeRunning = false;
                    ((MainActivity) getActivity()).dismissMainProgressBar();
                    if (syncDataResult.isSuccess() && syncDataResult.getError().isEmpty())
                    {
                        Toast.makeText(getContext(), R.string.password_change_successfully, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String error = syncDataResult.getError();
                        if (syncDataResult.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                            error = getString(R.string.api_unauthorized_error);

                        new AlertDialog.Builder(getContext())
                                .setTitle(getString(R.string.dlg_error_header))
                                .setMessage(getString(R.string.contact_delete_error, error))
                                .setPositiveButton(R.string.ok, null)
                                .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.vd_error_red_24dp, null))
                                .show();
                    }
                }
            });
            mChangePasswordTask.execute();
        }
    }

    private void stopChangingPassword()
    {
        if (mChangePasswordTask != null && mIsPasswordChangeRunning)
        {
            ((MainActivity) getActivity()).dismissMainProgressBar();
            mChangePasswordTask.cancel();
            mIsPasswordChangeRunning = false;
        }
    }

}
