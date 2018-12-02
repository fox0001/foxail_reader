package org.foxail.android.common;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class CommonUtil {
	
	public final static String MAIN_PACKAGE_NAME = "org.foxail.android.reader";
	
	/**
	 * 获取本软件的versionCode
	 * 
	 * @param context
	 * @return
	 */
	public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(MAIN_PACKAGE_NAME, 0).versionCode;
        } catch (NameNotFoundException e) {
        	e.printStackTrace();
        }
        return verCode;
    }
    
	/**
	 * 获取本软件的versionName
	 * 
	 * @param context
	 * @return
	 */
    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(MAIN_PACKAGE_NAME, 0).versionName;
        } catch (NameNotFoundException e) {
        	e.printStackTrace();
        }
        return verName;
    }
}
