package org.foxail.android.common;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

import java.lang.reflect.Field;

public class CommonUtil {
	
	public final static String MAIN_PACKAGE_NAME = "org.foxail.android.reader";

	/**
	 * 获取本软件的versionCode
	 * 
	 * @param context - application context
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
	 * @param context - application context
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

    /**
	 * 根据字符串获取资源ID
	 *
	 * @param variableName - application context
	 * @param c - the Class of resource
	 */
	public static int getResId(String variableName, Class<?> c) {
		try {
			Field idField = c.getDeclaredField(variableName);
			return idField.getInt(idField);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
}
