package rs.djokafioka.carnegie.sync.model;

import java.util.ArrayList;

import rs.djokafioka.carnegie.model.Contact;

/**
 * Created by Djordje on 21.1.2022..
 */
public class SyncContactsResult extends SyncDataResult
{
    private ArrayList<Contact> mContactList;

    public ArrayList<Contact> getContactList()
    {
        return mContactList;
    }

    public void setContactList(ArrayList<Contact> contactList)
    {
        mContactList = contactList;
    }
}
