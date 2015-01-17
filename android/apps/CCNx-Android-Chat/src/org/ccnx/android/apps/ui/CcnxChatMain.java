package org.ccnx.android.apps.ui;

import java.io.File;

import org.ccnx.ccn.config.UserConfiguration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public final class CcnxChatMain extends Activity implements OnClickListener{
	protected final static String TAG = "ccnchat.StartScreen";
	private static final String PREFS_NAME="ccnChatPrefs";
	private static final String DEFAULT_NAMESPACE="/ccnchat";
	private String DEFAULT_HANDLE="Android";
	private static final String DEFAULT_REMOTEHOST="";
	private static final String DEFAULT_REMOTEPORT="9695";
	protected static final String PREF_NAMESPACE="namespace";
	protected static final String PREF_HANDLE="handle";
	protected static final String PREF_REMOTEHOST="remotehost";
	protected static final String PREF_REMOTEPORT="remoteport";
	private EditText nameSpaceBtn;
	private EditText usernameBtn;
	private EditText remoteHostBtn;
	private EditText remotePortBtn;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ccnchat_settings);
		
		Log.i(TAG, "onCreate()");
		
		Button button = (Button) findViewById(R.id.btnConnect);
		if( null != button )
			button.setOnClickListener(this);
		else
			Log.e(TAG, "Could not find btnConect!");

		nameSpaceBtn = (EditText) findViewById(R.id.etNamespace);
		usernameBtn = (EditText) findViewById(R.id.etHandle);
		remoteHostBtn = (EditText) findViewById(R.id.etRemoteHost);
		remotePortBtn = (EditText) findViewById(R.id.etRemotePort);
		


		try {
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String s = tm.getLine1Number();
			if ( null != s && s.length() > 0 )
				DEFAULT_HANDLE = s;
			
		} catch(Exception e) {
			Log.e(TAG, "TelephoneManager error", e);
			e.printStackTrace();
		}

		
		restorePreferences();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		savePreferences();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnConnect:
			connect();
			finish();
			break;
		}
	}
	
	private void connect() {
		try {
			File ff = getDir("storage", Context.MODE_WORLD_READABLE);
			Log.i(TAG,"getDir = " + ff.getAbsolutePath());
	
			UserConfiguration.setUserConfigurationDirectory(ff.getAbsolutePath());
			String handle = usernameBtn.getText().toString();
			UserConfiguration.setUserName( handle );

			Intent i = new Intent(this, ChatScreen.class);
			i.putExtra(PREF_NAMESPACE, nameSpaceBtn.getText().toString());
			i.putExtra(PREF_HANDLE, handle);
			i.putExtra(PREF_REMOTEHOST, remoteHostBtn.getText().toString());
			i.putExtra(PREF_REMOTEPORT, remotePortBtn.getText().toString());
			this.startActivity(i);

		} catch(Exception e) {
			Log.e(TAG, "Error with ContentName", e);
			return;
		}
	}
	
	private void restorePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String namespace = settings.getString(PREF_NAMESPACE, DEFAULT_NAMESPACE);
		String handle = settings.getString(PREF_HANDLE, DEFAULT_HANDLE);
		String remotehost = settings.getString(PREF_REMOTEHOST, DEFAULT_REMOTEHOST);
		String remoteport = settings.getString(PREF_REMOTEPORT, DEFAULT_REMOTEPORT);
		
		usernameBtn.setText(handle);
		nameSpaceBtn.setText(namespace);
		remoteHostBtn.setText(remotehost);
		remotePortBtn.setText(remoteport);
		
	}
    
	private void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString(PREF_NAMESPACE, nameSpaceBtn.getText().toString());
		editor.putString(PREF_HANDLE, usernameBtn.getText().toString());
		editor.putString(PREF_REMOTEHOST, remoteHostBtn.getText().toString());
		editor.putString(PREF_REMOTEPORT, remotePortBtn.getText().toString());
		editor.commit();
	}
}
