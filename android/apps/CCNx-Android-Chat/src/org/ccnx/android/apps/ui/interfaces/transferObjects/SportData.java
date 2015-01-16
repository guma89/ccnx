package org.ccnx.android.apps.ui.interfaces.transferObjects;

/**
 * Created by Piotrek on 2015-01-14.
 */
public class SportData {
    private SportsmanData sportsman;
    private TrainingPlan plan;

    public SportData(SportsmanData sportsman, TrainingPlan plan) {
        this.sportsman = sportsman;
        this.plan = plan;
    }

    public SportsmanData getSportsman() {
        return sportsman;
    }

    public TrainingPlan getPlan() {
        return plan;
    }
}
