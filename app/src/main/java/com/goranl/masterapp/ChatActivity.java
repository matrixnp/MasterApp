package com.goranl.masterapp;

import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.goranl.masterapp.gcm.ServerUtilities;

public class ChatActivity extends Activity implements MessagesFragment.OnFragmentInteractionListener, EditContactDialog.OnFragmentInteractionListener {

	private EditText msgEdit;
	private String profileId;
	private String profileName;
	private String profileChatId;
	private boolean isGroup;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		profileId = getIntent().getStringExtra(Common.PROFILE_ID);
		msgEdit = (EditText) findViewById(R.id.msg_edit);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Cursor c = getContentResolver().query(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), null, null, null, null);
		if (c.moveToFirst()) {
			isGroup = c.getInt(c.getColumnIndex(DataProvider.COL_ISGROUP)) != 0;
			profileName = c.getString(c.getColumnIndex(DataProvider.COL_NAME));
			profileChatId = c.getString(c.getColumnIndex(DataProvider.COL_CHATID));
			actionBar.setTitle(profileName);
			actionBar.setSubtitle(profileChatId);
		}
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		Common.setCurrentChat(profileChatId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		
		if (!isGroup) menu.findItem(R.id.action_share).setVisible(false);
		return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_share:
			Util.share(this, profileChatId, isGroup);
			return true;		
		
		case R.id.action_edit:
			EditContactDialog dialog = new EditContactDialog();
			Bundle args = new Bundle();
			args.putString(Common.PROFILE_ID, profileId);
			args.putString(DataProvider.COL_NAME, profileName);
			dialog.setArguments(args);
			dialog.show(getFragmentManager(), "EditContactDialog");
			return true;
			
		case R.id.action_delete:
			getContentResolver().delete(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), null, null);
			finish();
			return true;			
			
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;			
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.send_btn:
			String msg = msgEdit.getText().toString();
			if (!TextUtils.isEmpty(msg)) {
				send(msg);
				msgEdit.setText(null);
			}
			break;
		}
	}
	
	@Override
	public void onEditContact(String name) {
		getActionBar().setTitle(name);
	}	
	
	@Override
	public String getProfileChatId() {
		return profileChatId;
	}	
	
	private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                	msg = ServerUtilities.send(txt, profileChatId);
                    
        			ContentValues values = new ContentValues(2);
        			values.put(DataProvider.COL_MSG, txt);
        			values.put(DataProvider.COL_TO, profileChatId);
        			getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
        			
                } catch (IOException ex) {
                    msg = ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            	if (!TextUtils.isEmpty(msg)) {
            		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            	}
            }
        }.execute(null, null, null);		
	}	

	@Override
	protected void onPause() {
		ContentValues values = new ContentValues(1);
		values.put(DataProvider.COL_COUNT, 0);
		getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), values, null, null);
		Common.setCurrentChat(null);
		super.onPause();
	}	

}
