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
	
	/**
	 * get ID
	 * @return
	 */
	public long getId() {
		return id;
	}

	/**
	 * set ID
	 * @param id
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * get name
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * set name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * get milliseconds
	 * @return
	 */
	public long getMilliseconds() {
		return milliseconds;
	}

	/**
	 * set milliseconds
	 * @param milliseconds
	 */
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