package org.ccnx.android.apps.ui.interfaces.transferObjects;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Piotrek on 2015-01-14.
 */
public class SportData implements Serializable {
	private final static long serialVersionUID = 1;

	private SportsmanData sportsman;
	private List<TrainingPlan> plans;

	public SportData(SportsmanData sportsman, List<TrainingPlan> plans) {
		this.sportsman = sportsman;
		this.plans = plans;
	}

	public SportsmanData getSportsman() {
		return sportsman;
	}

	public List<TrainingPlan> getPlan() {
		return plans;
	}
}
