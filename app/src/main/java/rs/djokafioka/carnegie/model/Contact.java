package rs.djokafioka.carnegie.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Djordje on 21.1.2022..
 */
public class Contact implements Serializable
{
    public static final long serialVersionUID = 20220121221100L;

    @Expose
    @SerializedName("Id")
    private int mId;

    @Expose
    @SerializedName("Name")
    private String mName;

    @Expose
    @SerializedName("Address")
    private String mAddress;

    @Expose
    @SerializedName("Phone")
    private String mPhone;

    @Expose
    @SerializedName("Email")
    private String mEmail;

    public Contact(String name, String address, String phone, String email)
    {
        mName = name;
        mAddress = address;
        mPhone = phone;
        mEmail = email;
    }

    public Contact()
    {
    }

    public int getId()
    {
        return mId;
    }

    public void setId(int id)
    {
        mId = id;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public String getAddress()
    {
        return mAddress;
    }

    public void setAddress(String address)
    {
        mAddress = address;
    }

    public String getPhone()
    {
        return mPhone;
    }

    public void setPhone(String phone)
    {
        mPhone = phone;
    }

    public String getEmail()
    {
        return mEmail;
    }

    public void setEmail(String email)
    {
        mEmail = email;
    }
}
