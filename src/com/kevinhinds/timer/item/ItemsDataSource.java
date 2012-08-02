package com.kevinhinds.timer.item;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * helper dataSource for the 'items' table
 * 
 * @author khinds
 */
public class ItemsDataSource {

	/** Database fields */
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_MILLISECONDS };

	/**
	 * contruct ItemsDataSource
	 * 
	 * @param context
	 */
	public ItemsDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Item createItem(String name, long milliseconds) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_NAME, name);
		values.put(MySQLiteHelper.COLUMN_MILLISECONDS, milliseconds);

		long insertId = database.insert(MySQLiteHelper.TABLE_ITEM, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_ITEM, allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		Item newItem = cursorToItem(cursor);
		cursor.close();
		return newItem;
	}

	public void deleteItem(Item item) {
		long id = item.getId();
		System.out.println("Item deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_ITEM, MySQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	public void deleteItemByName(String name) {
		System.out.println("Item deleted with name: " + name);
		try {
			database.delete(MySQLiteHelper.TABLE_ITEM, MySQLiteHelper.COLUMN_NAME + "=?", new String[] { name });
		} catch (Exception e) {
		}
	}

	public Item getById(long id) {
		Cursor cursor = database.rawQuery("select * from " + MySQLiteHelper.TABLE_ITEM + " where " + MySQLiteHelper.COLUMN_ID + "='" + id + "'", null);
		cursor.moveToFirst();
		Item item = cursorToItem(cursor);
		return item;
	}

	public List<Item> getAllItems() {
		List<Item> items = new ArrayList<Item>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_ITEM, allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Item item = cursorToItem(cursor);
			items.add(item);
			cursor.moveToNext();
		}
		/** Make sure to close the cursor */
		cursor.close();
		return items;
	}

	private Item cursorToItem(Cursor cursor) {
		Item item = new Item();
		item.setId(cursor.getLong(0));
		item.setName(cursor.getString(1));
		item.setMilliseconds(Long.parseLong(cursor.getString(2)));
		return item;
	}
}
