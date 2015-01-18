package org.ccnx.android.apps.ui;

import org.ccnx.android.apps.ui.callback.GuiUpdaterWrapper;
import org.ccnx.android.apps.ui.helper.FieldCleaner;
import org.ccnx.android.apps.ui.helper.FieldDataProvider;
import org.ccnx.android.apps.ui.interfaces.transferObjects.SportsmanData;
import org.ccnx.database.SportsmanDatabaseHandler;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Profile extends Activity {

	private FieldCleaner fieldCleaner;
	private FieldDataProvider fieldDataProvider;
	private SportsmanDatabaseHandler db;
	@SuppressWarnings("unused")
	private BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profil);
		init();
		preparePlanButton();
		prepareResetButton();
		prepareSendButton();
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

	private void preparePlanButton() {
		Button plan = (Button) findViewById(R.id.btnPlan);
		plan.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(), Plan.class);
				startActivityForResult(myIntent, 0);
			}
		});
	}

	private void prepareResetButton() {
		Button btn = (Button) findViewById(R.id.btnReset);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fieldCleaner.clearTextFields(R.id.edImie, R.id.edNazwisko,
						R.id.edWzrost, R.id.edWaga);
				fieldCleaner.clearRadioButton(R.id.radioSex);
			}
		});
	}

	private void prepareSendButton() {
		Button sendBtn = (Button) findViewById(R.id.btnWyslij);
		sendBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				saveDataFromTextFields();
				GuiUpdaterWrapper.getGuiUpdater().send();
			}
		});
	}

	private void saveDataFromTextFields() {
		String name = fieldDataProvider.readEditField(R.id.edImie);
		String surname = fieldDataProvider.readEditField(R.id.edNazwisko);
		String height = fieldDataProvider.readEditField(R.id.edWzrost);
		String weight = fieldDataProvider.readEditField(R.id.edWaga);
		db.addProfile(new SportsmanData(name, surname, height, weight));
	}

	private void updateFields() {
		SportsmanData profile = db.getLatestProfile();
		fieldDataProvider.setEditField(R.id.edImie, profile.getName());
		fieldDataProvider.setEditField(R.id.edNazwisko, profile.getSurname());
		fieldDataProvider.setEditField(R.id.edWzrost, profile.getHeight());
		fieldDataProvider.setEditField(R.id.edWaga, profile.getWeight());
	}

}
