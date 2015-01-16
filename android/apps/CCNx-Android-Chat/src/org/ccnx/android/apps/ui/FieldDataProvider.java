package org.ccnx.android.apps.ui;

import android.app.Activity;
import android.widget.EditText;

/**
 * Created by Piotrek on 2015-01-15.
 */
public class FieldDataProvider {
    private Activity activity;


    public FieldDataProvider(Activity activity) {
        this.activity = activity;
    }

    public String readEditField(int id) {
        EditText dataField = (EditText) activity.findViewById(id);
        return dataField.getText().toString();
    }

}
