package org.ccnx.android.apps.ui;

import java.io.File;
import org.ccnx.ccn.config.UserConfiguration;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class SportsmanAppMain extends Activity {
	protected final static String TAG = "SportsmanAppMain";
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
        createViews();
    }

    private void init() {
        fieldConverter = new FieldDataProvider(this);
    }

    private void createViews() {
        createTrainerView();
        createSportsmanView();
    }

    private void createTrainerView() {
        Button trainer = (Button) findViewById(R.id.btnTrener);
        trainer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	Intent myIntent = new Intent(view.getContext(), PlanReview.class);
            	connect(myIntent);
                startActivityForResult(myIntent, 0);
            }
        });
    }

    private void createSportsmanView() {
        Button sportsman = (Button) findViewById(R.id.btnSportowiec);
        sportsman.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	Intent myIntent = new Intent(view.getContext(), Profile.class);
            	connect(myIntent);
                startActivityForResult(myIntent, 0);
            }
        });
    }
    
	private void connect(Intent intent) {
		try {
			File ff = getDir("storage", Context.MODE_WORLD_READABLE);
			Log.i(TAG,"getDir = " + ff.getAbsolutePath());
	
			UserConfiguration.setUserConfigurationDirectory(ff.getAbsolutePath());
			UserConfiguration.setUserName(USERNAME);

			intent.putExtra(PREF_NAMESPACE, NAMESPACE);
			intent.putExtra(PREF_HANDLE, USERNAME);
			intent.putExtra(PREF_REMOTEHOST, getIpAddress());
			intent.putExtra(PREF_REMOTEPORT,getPort());
		} catch(Exception e) {
			Log.e(TAG, "Error with ContentName", e);
			return;
		}
	}

    private String getIpAddress() {
        return fieldConverter.readEditField(R.id.ipText);
    }

    private String getPort() {
        return fieldConverter.readEditField(R.id.portText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ccnx_chat_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
