package org.ccnx.android.apps.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v7.app.ActionBarActivity;

public class SportsmanAppMain extends ActionBarActivity {
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
