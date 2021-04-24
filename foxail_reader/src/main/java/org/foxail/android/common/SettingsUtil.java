package org.foxail.android.common;

import android.content.Context;
import android.content.SharedPreferences;

import org.foxail.android.reader.client.ClientSource;

public class SettingsUtil {

    private final static String TAG = "SettingsUtil";

    public final static String FILE_NAME = "settings"; // The file name of settings xml

    public final static String CUR_CLIENT = "cur_client"; // Setting Item: Current Client
    private final static ClientSource DEFAULT_CLIENT = ClientSource.cnbeta; // Default Client

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static ClientSource getCurClient(Context context) {
        ClientSource clientSource = null;
        String val = getPreferences(context).getString(CUR_CLIENT, null);
        if(val != null) {
            clientSource =  ClientSource.valueOf(val);
        }
        if(clientSource == null) {
            clientSource = DEFAULT_CLIENT;
        }
        return clientSource;
    }

    public static void setCurClient(Context context, ClientSource clientSource) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        if(clientSource == null) {
            clientSource = DEFAULT_CLIENT;
        }
        editor.putString(CUR_CLIENT, clientSource.name());
    }
}
