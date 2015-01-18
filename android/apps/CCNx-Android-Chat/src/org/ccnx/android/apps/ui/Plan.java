package org.ccnx.android.apps.ui;

import org.ccnx.android.apps.ui.callback.GuiUpdaterWrapper;
import org.ccnx.android.apps.ui.helper.FieldCleaner;
import org.ccnx.android.apps.ui.helper.FieldDataProvider;
import org.ccnx.android.apps.ui.interfaces.transferObjects.TrainingPlan;
import org.ccnx.database.SportsmanDatabaseHandler;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class Plan extends Activity {
	private FieldCleaner fieldCleaner;
	private FieldDataProvider fieldDataProvider;
	private SportsmanDatabaseHandler db;
	@SuppressWarnings("unused")
	private BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plan);
		init();
		prepareSendButton();
		prepareResetButton();
		updateFields();
	}

	private void init() {
		db = new SportsmanDatabaseHandler(this, null, null, 1);
		fieldCleaner = new FieldCleaner(this);
		fieldDataProvider = new FieldDataProvider(this);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateFields();
			}
		};
	}

	private void prepareSendButton() {
		Button sendBtn = (Button) findViewById(R.id.btnWyslijPlan);
		sendBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				saveFromFromTextFields();
				GuiUpdaterWrapper.getGuiUpdater().send();
			}
		});
	}

	private void updateFields() {
		TrainingPlan training = db.getLastTraining();
		fieldDataProvider.setEditField(R.id.trainingName, training.getName());
		fieldDataProvider.setEditField(R.id.etDescription,
				training.getDescription());
		fieldDataProvider.setRateBar(R.id.selfRatingBar,getRankValue(training.getSelfRank()));
	}
	
	private float getRankValue(String text) {
		float number;
		try {			
			number = Float.parseFloat(text);
		} catch (NumberFormatException ex) {
			return 2.5f;
		}
		return number;
	}

	private void saveFromFromTextFields() {
		String trainingName = fieldDataProvider
				.readEditField(R.id.trainingName);
		String description = fieldDataProvider
				.readEditField(R.id.etDescription);
		String selfRank = fieldDataProvider.readEditField(R.id.selfRatingBar);
		String comment = fieldDataProvider.readEditField(R.id.review_Comment);
		Float rank = fieldDataProvider.readRateBar(R.id.ratingBar2);

		db.addTraining(new TrainingPlan(trainingName, description, selfRank,
				comment, rank.toString()));
	}

	private void prepareResetButton() {
		Button btnC = (Button) findViewById(R.id.btnResetPlan);
		btnC.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fieldCleaner.clearTextFields(R.id.etDescription);
				fieldCleaner.clearRateBar(R.id.selfRatingBar);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_plan, menu);
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
