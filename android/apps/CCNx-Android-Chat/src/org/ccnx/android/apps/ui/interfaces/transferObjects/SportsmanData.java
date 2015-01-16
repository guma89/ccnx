package org.ccnx.android.apps.ui.interfaces.transferObjects;

/**
 * Created by Piotrek on 2015-01-14.
 */
public class SportsmanData {
    private String name;
    private String surname;
    private String weight;
    private double bmi;
    private double height;

    public SportsmanData(String name, String surname, String weight, double bmi, double height) {
        this.name = name;
        this.surname = surname;
        this.weight = weight;
        this.bmi = bmi;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getWeight() {
        return weight;
    }

    public double getBmi() {
        return bmi;
    }

    public double getHeight() {
        return height;
    }
}
