package org.ccnx.android.apps.ui.helper;

import android.app.Activity;
import android.widget.EditText;
import android.widget.RatingBar;

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
    
    public void setEditField(int id, String value) {
    	EditText dataField = (EditText) activity.findViewById(id);
    	dataField.setText(value);
    }
    
    public float readRateBar(int id) {
    	RatingBar bar = (RatingBar) activity.findViewById(id);
    	return bar.getRating();
    }
    
    public void setRateBar(int id, float rating) {
    	RatingBar bar = (RatingBar) activity.findViewById(id);
    	bar.setRating(rating);
    }

}
