package org.ccnx.android.apps.ui;

import org.ccnx.android.ccnlib.R;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class Plan extends ActionBarActivity {
    private FieldCleaner fieldCleaner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        fieldCleaner = new FieldCleaner(this);
        prepareResetButton();
    }

    private void prepareResetButton() {
        Button btnC=(Button) findViewById(R.id.btnResetPlan);
        btnC.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                fieldCleaner.clearTextFields(R.id.etKomentarz);
                fieldCleaner.clearRateBar(R.id.ratingBar);
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
