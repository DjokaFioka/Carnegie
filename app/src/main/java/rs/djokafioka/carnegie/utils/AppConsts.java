package rs.djokafioka.carnegie.utils;

import okhttp3.MediaType;

/**
 * Created by Djordje on 20.1.2022..
 */
public class AppConsts
{
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_RAW = MediaType.parse("text; charset=utf-8");
    public static final int CONNECTIVITY_TIMEOUT_SECONDS = 10;
    public static final int READ_TIMEOUT_SECONDS = 60;
    public static final int WRITE_TIMEOUT_SECONDS = 20;

    public static final String API_TOKEN = "Token";
    public static final String API_REGISTER = "api/Account/Register";
    public static final String API_RESET_PASSWORD = "api/Account/ResetPassword";
    public static final String API_CHANGE_PASSWORD = "api/Account/ChangePassword";
    public static final String API_GET_CONTACTS = "api/Contact/GetContacts";
    public static final String API_ADD_CONTACT = "api/Contact/Add";
    public static final String API_EDIT_CONTACT = "api/Contact/Edit";
    public static final String API_DELETE_CONTACT = "api/Contact/Delete";
    public static final String API_DELETE_ALL_CONTACTS = "api/Contact/DeleteAll";
    public static final String API_AUTHORIZATION_CODE = "HelloCarnegie";
}
