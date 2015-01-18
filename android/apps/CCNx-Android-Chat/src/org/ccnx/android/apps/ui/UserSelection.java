package org.ccnx.android.apps.ui;

import org.ccnx.android.apps.ui.callback.GuiUpdaterWrapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class UserSelection extends Activity {
	protected static final String PREF_NAMESPACE = "namespace";
	protected static final String PREF_HANDLE = "handle";
	protected static final String PREF_REMOTEHOST = "remotehost";
	protected static final String PREF_REMOTEPORT = "remoteport";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_selection);
		createViews();
		prepareAndStartCcnxWorker();
	}

	private void createViews() {
		createTrainerView();
		createSportsmanView();
	}

	private void createTrainerView() {
		Button trainer = (Button) findViewById(R.id.btnTrener);
		trainer.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(),
						PlanReview.class);
				startActivityForResult(myIntent, 0);
			}
		});
	}

	private void createSportsmanView() {
		Button sportsman = (Button) findViewById(R.id.btnSportowiec);
		sportsman.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(), Profile.class);
				startActivityForResult(myIntent, 0);
			}
		});
	}

	@SuppressWarnings("static-access")
	private void prepareAndStartCcnxWorker() {
		String namespace = getIntent().getStringExtra(PREF_NAMESPACE);
		String username = getIntent().getStringExtra(PREF_HANDLE);
		String remoteHost = getIntent().getStringExtra(PREF_REMOTEHOST);
		String remotePort = getIntent().getStringExtra(PREF_REMOTEPORT);
		// smell like ....
		GuiUpdaterWrapper.getInstance().createGuiUpdaterIfNotExists(username,
				namespace, remoteHost, remotePort);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_profil, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}
