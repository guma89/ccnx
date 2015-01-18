package org.ccnx.android.apps.ui;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class PlanReview extends Activity implements
		AdapterView.OnItemSelectedListener {

	private ArrayAdapter<String> adapter;
	private Spinner spinner;
	private SportsmanDatabaseHandler db;
	@SuppressWarnings("unused")
	private BroadcastReceiver receiver;
	private FieldDataProvider fieldDataProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plan_review);
		init();
		updateData();
	}

	private void init() {
		adapter = createSpinnerAdapter();
		spinner = getSpinner();
		spinner.setAdapter(adapter);
		fieldDataProvider = new FieldDataProvider(this);
		db = new SportsmanDatabaseHandler(this, null, null, 1);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateData();
			}
		};
	}

	private ArrayAdapter<String> createSpinnerAdapter() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, android.R.id.text1);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	private Spinner getSpinner() {
		return (Spinner) findViewById(R.id.reviewSpinner);
	}

	private void updateData() {
		TrainingPlan plan = db.getLastTraining();
		setItemsToSpinner(plan.getName());
		fieldDataProvider.setEditField(R.id.review_Comment, plan.getComment());
		fieldDataProvider.setRateBar(R.id.ratingBar2, getRankValue(plan.getRank()));
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

	private void setItemsToSpinner(String... item) {
		adapter.clear();
		adapter.addAll(item);
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_plan_review, menu);
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

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// nothing to do here :(
	}
}
