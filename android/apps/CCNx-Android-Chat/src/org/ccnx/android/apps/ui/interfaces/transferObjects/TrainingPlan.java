package org.ccnx.android.apps.ui.interfaces.transferObjects;

/**
 * Created by Piotrek on 2015-01-14.
 */
public class TrainingPlan {
    private double distance;
    private double avgSpeed;
    private double burnedCalories;
    private String planComment;
    private int planRate;

    public TrainingPlan(double distance, double burnedCalories, double avgSpeed, String planComment, int planRate) {
        this.distance = distance;
        this.burnedCalories = burnedCalories;
        this.avgSpeed = avgSpeed;
        this.planComment = planComment;
        this.planRate = planRate;
    }

    public double getDistance() {
        return distance;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public double getBurnedCalories() {
        return burnedCalories;
    }

    public String getPlanComment() {
        return planComment;
    }

    public int getPlanRate() {
        return planRate;
    }
}
