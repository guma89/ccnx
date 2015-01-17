package org.ccnx.android.apps.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class PlanReview extends Activity implements AdapterView.OnItemSelectedListener {

    private ArrayAdapter<String> adapter;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_review);
        init();
        addItemsToSpinner("jeden", "dwa", "trzy");
    }

    private void init() {
        adapter = createSpinnerAdapter();
        spinner = getSpinner();
        spinner.setAdapter(adapter);
    }

    private  ArrayAdapter<String> createSpinnerAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private Spinner getSpinner() {
        return (Spinner) findViewById(R.id.reviewSpinner);
    }

    public void addItemsToSpinner(String... item) {
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        System.out.println("Selected: "+id);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // nothing to do here :(
    }
}
