package org.ccnx.android.apps.chat;

public class DataStruct implements java.io.Serializable {
	
	private static final long serialVersionUID = -4693607500919831611L;
	
	public String name;
	public String surname;
	public int age;
	
	public DataStruct(String name, String surname, int age) {
		this.name = name;
		this.surname = surname;
		this.age = age;
	}

	public String toString() {
		return name + " " + surname + " " + Integer.toString(age);
	}
}
