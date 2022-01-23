package rs.djokafioka.carnegie.sync.model;

import java.util.ArrayList;

import rs.djokafioka.carnegie.model.Contact;

/**
 * Created by Djordje on 21.1.2022..
 */
public class SyncContactsResult extends SyncDataResult
{
    private int mPosition;
    private Contact mContact;
    private ArrayList<Contact> mContactList;

    public int getPosition()
    {
        return mPosition;
    }

    public void setPosition(int position)
    {
        mPosition = position;
    }

    public Contact getContact()
    {
        return mContact;
    }

    public void setContact(Contact contact)
    {
        mContact = contact;
    }

    public ArrayList<Contact> getContactList()
    {
        return mContactList;
    }

    public void setContactList(ArrayList<Contact> contactList)
    {
        mContactList = contactList;
    }
}
