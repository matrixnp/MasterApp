package com.goranl.masterapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.goranl.masterapp.gcm.Constants;

public class Common extends Application {
	
	public static final String PROFILE_ID = "profile_id";
	
	//parameters recognized by demo server
	public static final String FROM = "chatId";
	public static final String REG_ID = "regId";
	public static final String MSG = "msg";
	public static final String TO = "chatId2";	
	
	private static SharedPreferences prefs;

	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	public static String getDisplayName() {
		return prefs.getString("display_name", "");
	}
	
	public static String getChatId() {
		return prefs.getString("chat_id", "");
	}
	public static void setChatId(String chatId) {
		prefs.edit().putString("chat_id", chatId).commit();
	}
	
	public static String getCurrentChat() {
		return prefs.getString("current_chat", null);
	}
	public static void setCurrentChat(String chatId) {
		prefs.edit().putString("current_chat", chatId).commit();
	}	
	
	public static boolean isNotify() {
		return prefs.getBoolean("notifications_new_message", true);
	}	
	
	public static String getRingtone() {
		return prefs.getString("notifications_new_message_ringtone", android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString());
	}
	
	public static String getServerUrl() {
		return prefs.getString("server_url_pref", Constants.SERVER_URL);
	}
	
	public static String getSenderId() {
		return prefs.getString("sender_id_pref", Constants.SENDER_ID);
	}	
    	
}
