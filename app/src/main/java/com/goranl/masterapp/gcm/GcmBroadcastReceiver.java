package com.goranl.masterapp.gcm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;

import com.goranl.masterapp.Common;
import com.goranl.masterapp.DataProvider;
import com.goranl.masterapp.MainActivity;
import com.goranl.masterapp.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "GcmBroadcastReceiver";
	
	private Context ctx;
	private ContentResolver cr;

	@Override
	public void onReceive(Context context, Intent intent) {
		ctx = context;
		cr = context.getContentResolver();
		
		PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();
		
		try {
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
			
			String messageType = gcm.getMessageType(intent);
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				sendNotification("Send error", false);
				
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				sendNotification("Deleted messages on server", false);
				
			} else {
				String msg = intent.getStringExtra(Common.MSG);
				String from = intent.getStringExtra(Common.FROM);
				String to = intent.getStringExtra(Common.TO);
				
				//find contact
				String contactName = null;
				Cursor c = context.getContentResolver().query(
						DataProvider.CONTENT_URI_PROFILE, 
						new String[]{DataProvider.COL_NAME}, 
						DataProvider.COL_CHATID+" = ?", 
						new String[]{from}, 
						null);
				if (c != null) {
					if (c.moveToFirst()) {
						contactName = c.getString(0);
					}
					c.close();
				}
				
				//contact not found
				if (contactName == null) return;
				
				ContentValues values = new ContentValues(2);
				values.put(DataProvider.COL_MSG, msg);
				values.put(DataProvider.COL_FROM, from);
				values.put(DataProvider.COL_TO, to);
				cr.insert(DataProvider.CONTENT_URI_MESSAGES, values);
				
				if (!from.equals(Common.getCurrentChat()) && !to.equals(Common.getCurrentChat())) {
					if (Common.isNotify()) 
						sendNotification(contactName+": "+msg, true);
					
					incrementMessageCount(context, from, to);
				}
			}
			setResultCode(Activity.RESULT_OK);
			
		} finally {
			mWakeLock.release();
		}
	}
	
	private void sendNotification(String text, boolean launchApp) {
		NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification.Builder mBuilder = new Notification.Builder(ctx)
			.setAutoCancel(true)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(ctx.getString(R.string.app_name))
			.setContentText(text);

		if (!TextUtils.isEmpty(Common.getRingtone())) {
			mBuilder.setSound(Uri.parse(Common.getRingtone()));
		}
		
		if (launchApp) {
			Intent intent = new Intent(ctx, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(pi);
		}
		
		mNotificationManager.notify(1, mBuilder.getNotification());
	}
	
	private void incrementMessageCount(Context context, String from, String to) {
		String chatId;
		if (!Common.getChatId().equals(to)) {//group
			chatId = to;
		} else {
			chatId = from;
		}
		
		String selection = DataProvider.COL_CHATID+" = ?";
		String[] selectionArgs = new String[]{chatId};
		Cursor c = cr.query(DataProvider.CONTENT_URI_PROFILE, 
				new String[]{DataProvider.COL_COUNT}, 
				selection, 
				selectionArgs, 
				null);
		
		if (c != null) {
			if (c.moveToFirst()) {
				int count = c.getInt(0);
				
				ContentValues cv = new ContentValues(1);
				cv.put(DataProvider.COL_COUNT, count+1);
				cr.update(DataProvider.CONTENT_URI_PROFILE, cv, selection, selectionArgs);
			}
			c.close();
		}
	}
}
