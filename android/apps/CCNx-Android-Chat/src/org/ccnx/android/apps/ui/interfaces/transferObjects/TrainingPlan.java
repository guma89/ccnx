package org.ccnx.android.apps.ui.interfaces.transferObjects;

import java.io.Serializable;

public class TrainingPlan implements Serializable {
	private final static long serialVersionUID = 1;
	private String name;
	private String description;
	private String selfRank;
	private String comment;
	private String rank;
	
	public TrainingPlan(String name, String description, String selfRank, String rank, String comment) {
		super();
		this.name = name;
		this.description = description;
		this.selfRank = selfRank;
		this.comment = comment;
		this.rank = rank;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getSelfRank() {
		return selfRank;
	}
	
	public String getRank() {
		return rank;
	}
	
	public String getComment() {
		return comment;
	}

}
