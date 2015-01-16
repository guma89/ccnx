package org.ccnx.android.apps.ui;

import android.app.Activity;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;

public class FieldCleaner {
    private static final String CLEAR_TEXT = "";
    private Activity activityToClear;

    public FieldCleaner(Activity activity) {
        this.activityToClear = activity;
    }

    public void clearTextFields(int... listOfIds) {
        for (int id : listOfIds) {
            EditText fieldToClear = (EditText) activityToClear.findViewById(id);
            fieldToClear.setText(CLEAR_TEXT);
        }
    }

    public void clearRateBar(int id) {
        RatingBar bar = (RatingBar) activityToClear.findViewById(id);
        bar.setRating(1);
    }

    public void clearRadioButton(int id) {
        RadioGroup radio = (RadioGroup) activityToClear.findViewById(id);
        radio.clearCheck();
    }
}
