package org.ccnx.android.apps.ui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.ccnx.android.apps.chat.R;

public class Profile extends ActionBarActivity {
    private FieldCleaner fieldCleaner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        fieldCleaner = new FieldCleaner(this);
        preparePlanButton();
        prepareResetButton();
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
        Button btn=(Button) findViewById(R.id.btnReset);
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                fieldCleaner.clearTextFields(R.id.edImie, R.id.edNazwisko, R.id.edWzrost, R.id.edWaga);
                fieldCleaner.clearRadioButton(R.id.radioSex);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profil, menu);
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
