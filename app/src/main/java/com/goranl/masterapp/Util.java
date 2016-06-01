package com.goranl.masterapp;

import android.content.Context;
import android.content.Intent;

public class Util {
	
	public static boolean isEmailValid(CharSequence email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	public static void share(Context context, String chatId, boolean isGroup) {
		Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
		sendIntent.setType("text/plain");
		sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		// Add data to the intent, the receiving app will decide what to do with it.
		if (isGroup) {
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Join my group");
			sendIntent.putExtra(Intent.EXTRA_TEXT, "My group ID is " + chatId);

		} else {
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Invitation to chat");
			sendIntent.putExtra(Intent.EXTRA_TEXT, "My chat ID is " + chatId);
		}
		
		context.startActivity(Intent.createChooser(sendIntent, "Invite via"));
	}

	
	

}
