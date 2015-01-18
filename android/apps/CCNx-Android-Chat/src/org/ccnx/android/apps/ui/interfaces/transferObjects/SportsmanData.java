package org.ccnx.android.apps.ui.interfaces.transferObjects;

import java.io.Serializable;

public class SportsmanData implements Serializable {
	private final static long serialVersionUID = 1;

	private String name;
	private String surname;
	private String weight;
	private String height;

	public SportsmanData(String name, String surname, String height, String weight) {
		this.name = name;
		this.surname = surname;
		this.height = height;
		this.weight = weight;
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

	public String getHeight() {
		return height;
	}
}
