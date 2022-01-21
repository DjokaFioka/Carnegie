package rs.djokafioka.carnegie.controller.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import rs.djokafioka.carnegie.R;
import rs.djokafioka.carnegie.model.Contact;

/**
 * Created by Djordje on 21.1.2022..
 */
public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactListViewHolder>
{
    private ArrayList<Contact> mContactList;

    @Nullable
    private OnContactItemClickListener mOnContactItemClickListener;

    public ContactListAdapter(ArrayList<Contact> contactList, @Nullable OnContactItemClickListener onContactItemClickListener)
    {
        mContactList = contactList;
        mOnContactItemClickListener = onContactItemClickListener;
    }

    @NonNull
    @Override
    public ContactListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ContactListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactListViewHolder holder, int position)
    {
        final Contact contact = mContactList.get(position);

        holder.setContactName(contact.getName());
        holder.setPhone(contact.getPhone());
        holder.setEmail(contact.getEmail());

        holder.setPhoneVisible(contact.getPhone() != null && !contact.getPhone().isEmpty());
        holder.setEmailVisible(contact.getEmail() != null && !contact.getEmail().isEmpty());

        holder.setOnContactClick(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnContactItemClickListener != null)
                    mOnContactItemClickListener.onContactItemClick(contact);
            }
        });

        holder.setOnImgDeleteClick(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnContactItemClickListener != null)
                    mOnContactItemClickListener.onContactItemDeleteClick(contact);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mContactList.size();
    }

    public void updateTaskItemList(ArrayList<Contact> contactList)
    {
        mContactList = contactList;
        notifyDataSetChanged();
    }

    static final class ContactListViewHolder extends RecyclerView.ViewHolder
    {
        private final LinearLayout mLLContact;
        private final ImageView mImgDelete;
        private final TextView mTxtContactName;
        private final TextView mTxtPhone;
        private final TextView mTxtEmail;

        public ContactListViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mLLContact = (LinearLayout) itemView.findViewById(R.id.ll_contact);
            mImgDelete = (ImageView) itemView.findViewById(R.id.img_delete);
            mTxtContactName = (TextView) itemView.findViewById(R.id.txt_contact_name);
            mTxtPhone = (TextView) itemView.findViewById(R.id.txt_phone);
            mTxtEmail = (TextView) itemView.findViewById(R.id.txt_email);
        }

        void setContactName(String contactName)
        {
            mTxtContactName.setText(contactName);
        }

        void setPhone(String phone)
        {
            mTxtPhone.setText(phone);
        }

        void setEmail(String email)
        {
            mTxtEmail.setText(email);
        }

        void setPhoneVisible(boolean isVisible)
        {
            if(isVisible)
                mTxtPhone.setVisibility(View.VISIBLE);
            else
                mTxtPhone.setVisibility(View.GONE);
        }

        void setEmailVisible(boolean isVisible)
        {
            if(isVisible)
                mTxtEmail.setVisibility(View.VISIBLE);
            else
                mTxtEmail.setVisibility(View.GONE);
        }

        void setOnContactClick(final View.OnClickListener onClickListener)
        {
            mLLContact.setOnClickListener(onClickListener);
        }

        void setOnImgDeleteClick(final View.OnClickListener onClickListener)
        {
            mImgDelete.setOnClickListener(onClickListener);
        }

    }

    public interface OnContactItemClickListener
    {
        void onContactItemDeleteClick(Contact contact);
        void onContactItemClick(Contact contact);
    }
}
