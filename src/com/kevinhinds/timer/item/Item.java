package com.kevinhinds.timer.item;

/**
 * Item class related to the SQLiteDB table "item"
 * 
 * @author khinds
 */
public class Item {
	private long id;
	public String name;
	public long milliseconds;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getMilliseconds() {
		return milliseconds;
	}

	public void setMilliseconds(long milliseconds) {
		this.milliseconds = milliseconds;
	}

	/**
	 * Will be used by the ArrayAdapter in the ListView
	 */
	@Override
	public String toString() {
		return name;
	}
}