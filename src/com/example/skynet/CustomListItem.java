package com.example.skynet;

public class CustomListItem {

	int imageId;
	String name;
	boolean isChecked;

	public CustomListItem(int imageId, String name, boolean isChecked) {
		this.imageId = imageId;
		this.name = name;
		this.isChecked = isChecked;
	}

	public void setImage(int imageId) {
		this.imageId = imageId;
	}

	public int getImage() {
		return this.imageId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setCheckedState(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public boolean getCheckedState() {
		return this.isChecked;
	}
}
