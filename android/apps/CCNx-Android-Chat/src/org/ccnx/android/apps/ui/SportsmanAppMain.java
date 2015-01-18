package org.ccnx.android.apps.ui;

import java.io.File;

import org.ccnx.android.apps.ui.helper.FieldDataProvider;
import org.ccnx.ccn.config.UserConfiguration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SportsmanAppMain extends Activity {
	private final static String USERNAME = "Piotrek";
	protected static final String NAMESPACE="sportsman";
	protected static final String PREF_NAMESPACE="namespace";
	protected static final String PREF_HANDLE="handle";
	protected static final String PREF_REMOTEHOST="remotehost";
	protected static final String PREF_REMOTEPORT="remoteport";
    private FieldDataProvider fieldConverter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
        createUserSwitchView();
    }

    private void init() {
        fieldConverter = new FieldDataProvider(this);
    }
    
    private void createUserSwitchView() {
        Button connect = (Button) findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	Log.d("ccnx", "User clicked a connect button");
            	Intent myIntent = new Intent(view.getContext(), UserSelection.class);
            	connect(myIntent);
                startActivityForResult(myIntent, 0);
            }
        });
    }
    
	@SuppressWarnings("deprecation")
	private void connect(Intent intent) {
		try {
			File ff = getDir("storage", Context.MODE_WORLD_READABLE);
			UserConfiguration.setUserConfigurationDirectory(ff.getAbsolutePath());
			UserConfiguration.setUserName(USERNAME);
			intent.putExtra(PREF_NAMESPACE, NAMESPACE);
			intent.putExtra(PREF_HANDLE, USERNAME);
			intent.putExtra(PREF_REMOTEHOST, getIpAddress());
			intent.putExtra(PREF_REMOTEPORT,getPort());
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
	}

    private String getIpAddress() {
        return fieldConverter.readEditField(R.id.ipText);
    }

    private String getPort() {
        return fieldConverter.readEditField(R.id.portText);
    }
}
