package com.kevinhinds.timer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.kevinhinds.timer.item.Item;
import com.kevinhinds.timer.item.ItemsDataSource;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

public class RecipeActivity extends Activity {
	private View layout = null;
	private PopupWindow pw;
	String[] RowData = null;
	ArrayList<String> RowDataValues = new ArrayList<String>();
	protected CharSequence hoursRemaining = "0";
	protected CharSequence minutesRemaining = "0";
	protected int loop = 0;
	private ItemsDataSource itemsDataSource;
	private int timeInMinutes;
	private String name;
	private String style;
	private String time;
	private String unit;
	private List<Item> savedItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recipe);
		/** get the string resources to get the menuValues array */
		Resources res = getResources();
		final String[] menuValues = res.getStringArray(R.array.assetFilesArray);

		/** open data connections */
		openDataConnections();

		/** get all the items saved in the DB */
		savedItems = itemsDataSource.getAllItems();

		/** add presets from the local DB */
		LinearLayout llPreset = (LinearLayout) findViewById(R.id.mainPresetLayout);
		LinearLayout.LayoutParams lpPreset = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		Iterator<Item> iterator = savedItems.iterator();
		while (iterator.hasNext()) {
			Item presetItem = iterator.next();
			TextView tvPreset = new TextView(this);
			tvPreset.setId((int) presetItem.getId());
			tvPreset.setTextSize(20);
			tvPreset.setText((CharSequence) presetItem.getName());
			tvPreset.setLayoutParams(lpPreset);
			tvPreset.setClickable(true);
			tvPreset.setPadding(5, 10, 0, 10);
			tvPreset.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					initiatePresetPopupWindow(v.getId());
				}
			});
			llPreset.addView(tvPreset);
		}

		/** add the create new preset option */
		TextView tvPreset = new TextView(this);
		tvPreset.setTextSize(20);
		tvPreset.setText((CharSequence) "Create New Preset...");
		tvPreset.setLayoutParams(lpPreset);
		tvPreset.setClickable(true);
		tvPreset.setPadding(0, 10, 0, 10);

		tvPreset.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			}
		});
		llPreset.addView(tvPreset);

		/**
		 * attach to the LinearLayout to add TextViews dynamically via menuValues
		 */
		LinearLayout llRecipe = (LinearLayout) findViewById(R.id.mainRecipeLayout);
		LinearLayout.LayoutParams lpRecipe = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		for (int i = 0; i < menuValues.length; i++) {
			/**
			 * i needs to be final because it will be used in an additional scope
			 */
			final int iteration = i;

			/** create a option to create a new preset from each recipe file type */
			TextView tv = new TextView(this);
			tv.setId(i);
			tv.setTextSize(20);
			tv.setText((CharSequence) menuValues[iteration]);
			tv.setLayoutParams(lpRecipe);
			tv.setClickable(true);
			tv.setPadding(5, 10, 0, 10);
			final String menuType = menuValues[iteration];
			tv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					initiatePopupWindow(menuType);
				}
			});
			llRecipe.addView(tv);
		}

		/** icon returns to home screen */
		ImageView imageViewIcon = (ImageView) findViewById(R.id.imageViewIcon);
		imageViewIcon.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(RecipeActivity.this, TimerActivity.class);
				startActivity(intent);
			}
		});

		/** less than sign returns to home screen */
		TextView textViewBack = (TextView) findViewById(R.id.textViewBack);
		textViewBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(RecipeActivity.this, TimerActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * show the preset popup window
	 * 
	 * @param id
	 */
	private void initiatePresetPopupWindow(int id) {

		/** get screen metrics */
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;

		/** adjust the popup WxH */
		float popupWidth = (float) (width * .90);
		float popupHeight = (float) (height * .90);
		loop = 0;

		/** We need to get the instance of the LayoutInflater, use the context of this activity */
		LayoutInflater inflater = (LayoutInflater) RecipeActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		/** Inflate the view from a predefined XML layout */
		layout = inflater.inflate(R.layout.preset_layout, (ViewGroup) findViewById(R.id.popup_element));
		/** create a 300px width and 350px height PopupWindow */
		pw = new PopupWindow(layout, (int) popupWidth, (int) popupHeight, true);
		/** display the popup in the center */
		pw.showAtLocation(layout, Gravity.CENTER, 0, 0);

		/** cancel button to close */
		Button cancelButton = (Button) layout.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				pw.dismiss();
			}
		});

		Item currentItem = itemsDataSource.getById(id);

		final EditText editTextTitle = (EditText) layout.findViewById(R.id.editTextTitle);
		editTextTitle.setText(currentItem.getName());

		TextView textViewSuggestedTimeValue = (TextView) layout.findViewById(R.id.textViewSuggestedTimeValue);
		textViewSuggestedTimeValue.setText(TimeParser.getHumanReadableTimeValue(currentItem.getMilliseconds()));
		int presetTimeInMinutes = (int) (currentItem.getMilliseconds() / 1000 / 60);

		/** setup hoursAmount enabled = false */
		final EditText hoursAmount = (EditText) layout.findViewById(R.id.hoursAmount);
		hoursAmount.setText(Integer.toString(TimeParser.parseMinutesToHours(presetTimeInMinutes)));
		hoursAmount.setEnabled(false);

		/** hours increase 1 click */
		Button hoursIncreaseButton = (Button) layout.findViewById(R.id.hoursIncreaseButton);
		hoursIncreaseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int currentTime = Integer.parseInt((String) hoursAmount.getText().toString());
				hoursRemaining = TimeParser.setHours(currentTime, 1);
				hoursAmount.setText(hoursRemaining);
			}
		});

		/** hours decrease -1 click */
		Button hoursDecreaseButton = (Button) layout.findViewById(R.id.hoursDecreaseButton);
		hoursDecreaseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int currentTime = Integer.parseInt((String) hoursAmount.getText().toString());
				hoursRemaining = TimeParser.setHours(currentTime, -1);
				hoursAmount.setText(hoursRemaining);

			}
		});

		/** setup minutesAmount enabled = false */
		final EditText minutesAmount = (EditText) layout.findViewById(R.id.minutesAmount);
		minutesAmount.setText(Integer.toString(TimeParser.parseMinutesRemaining(presetTimeInMinutes)));
		minutesAmount.setEnabled(false);

		/** minutes increase 1 click */
		Button minutesIncreaseButton = (Button) layout.findViewById(R.id.minutesIncreaseButton);
		minutesIncreaseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
				minutesRemaining = TimeParser.setMinutes(currentTime, 1);
				minutesAmount.setText(minutesRemaining);
			}
		});

		/** minutes decrease -1 click */
		Button minutesDecreaseButton = (Button) layout.findViewById(R.id.minutesDecreaseButton);
		minutesDecreaseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
				minutesRemaining = TimeParser.setMinutes(currentTime, -1);
				minutesAmount.setText(minutesRemaining);
			}
		});

		/** minutes increase 5 click */
		Button minutesIncreaseButtonPlus5 = (Button) layout.findViewById(R.id.minutesIncreaseButtonPlus5);
		minutesIncreaseButtonPlus5.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
				minutesRemaining = TimeParser.setMinutes(currentTime, 5);
				minutesAmount.setText(minutesRemaining);
			}
		});

		/** minutes decrease -5 click */
		Button minutesIncreaseButtonMinus5 = (Button) layout.findViewById(R.id.minutesIncreaseButtonMinus5);
		minutesIncreaseButtonMinus5.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
				minutesRemaining = TimeParser.setMinutes(currentTime, -5);
				minutesAmount.setText(minutesRemaining);
			}
		});

		/** save and start the timer selected from recipe */
		Button SaveButton = (Button) layout.findViewById(R.id.SaveButton);
		SaveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				/** get name to save to DB and pass to intent */
				Editable currentNameEntry = editTextTitle.getText();
				String currentNameSaved = currentNameEntry.toString();

				/** get hours selected to save to DB and pass to intent */
				Editable currentlyChosenHours = hoursAmount.getText();
				String currentHoursSave = currentlyChosenHours.toString();

				/** get minutes selected to save to DB and pass to intent */
				Editable currentlyChosenMinutes = minutesAmount.getText();
				String currentMinutesSave = currentlyChosenMinutes.toString();

				/** get time in minutes of the hours and minutes selected put together */
				long currentTimeSaved = (long) Integer.parseInt(currentHoursSave) * 60;
				currentTimeSaved = currentTimeSaved + (long) Integer.parseInt(currentMinutesSave);

				/** delete and add the item saved from the recipe selection */
				try {
					itemsDataSource.deleteItemByName(currentNameSaved);
				} catch (Exception e) {
				}
				try {
					itemsDataSource.createItem(currentNameSaved, currentTimeSaved);
				} catch (Exception e) {
				}

				/** begin the new timer setup with the user's selected recipe values */
				Intent intent = new Intent(RecipeActivity.this, TimerActivity.class);
				intent.putExtra("timeInMinutes", currentTimeSaved);
				intent.putExtra("timerTitle", currentNameSaved);
				startActivity(intent);
			}
		});
	}

	/**
	 * show the popup window
	 */
	private void initiatePopupWindow(String menuType) {

		/** get screen metrics */
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;

		/** adjust the popup WxH */
		float popupWidth = (float) (width * .90);
		float popupHeight = (float) (height * .90);
		loop = 0;

		/** We need to get the instance of the LayoutInflater, use the context of this activity */
		LayoutInflater inflater = (LayoutInflater) RecipeActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		/** Inflate the view from a predefined XML layout */
		layout = inflater.inflate(R.layout.recipe_layout, (ViewGroup) findViewById(R.id.popup_element));
		/** create a 300px width and 350px height PopupWindow */
		pw = new PopupWindow(layout, (int) popupWidth, (int) popupHeight, true);
		/** display the popup in the center */
		pw.showAtLocation(layout, Gravity.CENTER, 0, 0);

		/** cancel button to close */
		Button cancelButton = (Button) layout.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				pw.dismiss();
			}
		});

		/** open the specific recipe data text file and display results of each recipe on the popup as a list */
		try {
			InputStream is = getAssets().open(menuType);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			try {
				String line;
				RowDataValues.clear();
				while ((line = reader.readLine()) != null) {

					/** the recipe file is tab separated so split on tabs for each value */
					RowData = line.split("\t");
					RowDataValues.add(line);
					String currentName = RowData[0];
					String currentStyle = RowData[1];
					String currentTime = RowData[2];
					String currentUnit = RowData[3];

					/**
					 * attach to the LinearLayout to add TextViews dynamically via menuValues
					 */
					LinearLayout ll = (LinearLayout) layout.findViewById(R.id.recipeContainer);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

					/** create the underlined "link" of the recipe to create a new preset from it */
					TextView tvTitle = new TextView(layout.getContext());
					tvTitle.setId(loop);
					tvTitle.setTextSize(18);
					CharSequence tvText = "";
					tvText = (CharSequence) currentName;
					tvTitle.setText(Html.fromHtml("<u>" + tvText + "</u>"));
					tvTitle.setLayoutParams(lp);
					tvTitle.setClickable(true);
					tvTitle.setPadding(5, 10, 0, 10);
					tvTitle.setContentDescription((CharSequence) Integer.toString(loop));

					/** on the selection of a recipe display more information so the user can save or edit it to setup a new timer */
					tvTitle.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {

							/** the list of recipes is now "gone" and the form to show the details of a specific recipe is now shown */
							LinearLayout recipeFormContainer = (LinearLayout) layout.findViewById(R.id.recipeFormContainer);
							recipeFormContainer.setVisibility(View.VISIBLE);
							ScrollView timerLayout = (ScrollView) layout.findViewById(R.id.timerLayout);
							timerLayout.setVisibility(View.GONE);

							/**
							 * get a line item of the recipe selected from the list of recipes stored in the text file as a whole via "contentDescription" value in the textView as the ID of the row
							 */
							String RowDataValue = RowDataValues.get(Integer.parseInt((String) v.getContentDescription()));
							String[] RowDataValueArray = RowDataValue.split("\t");
							name = RowDataValueArray[0];
							style = RowDataValueArray[1];
							time = RowDataValueArray[2];
							unit = RowDataValueArray[3];

							/** begin to setup the form with the populated recipe values */
							TextView textViewSuggestedTimeValue = (TextView) layout.findViewById(R.id.textViewSuggestedTimeValue);
							if (unit.equals("meal")) {
								textViewSuggestedTimeValue.setText(time);
							} else {
								textViewSuggestedTimeValue.setText(time + " [per: " + unit + "]");
							}

							/** get total time in minutes to store and pass back to the main timer activity */
							timeInMinutes = TimeParser.parseSuggestedTime(time);

							/** set recipe title and style in the form */
							TextView editTextTitle = (TextView) layout.findViewById(R.id.editTextTitle);
							editTextTitle.setText(name);
							TextView editTextStyle = (TextView) layout.findViewById(R.id.editTextStyle);
							editTextStyle.setText(style);

							/** back button on the form will cause the form to disappear and the list of all the recipes to return */
							Button BackButton = (Button) layout.findViewById(R.id.BackButton);
							BackButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									LinearLayout recipeFormContainer = (LinearLayout) layout.findViewById(R.id.recipeFormContainer);
									recipeFormContainer.setVisibility(View.GONE);
									ScrollView timerLayout = (ScrollView) layout.findViewById(R.id.timerLayout);
									timerLayout.setVisibility(View.VISIBLE);
								}
							});

							/** setup hoursAmount enabled = false */
							final EditText hoursAmount = (EditText) layout.findViewById(R.id.hoursAmount);
							hoursAmount.setText(Integer.toString(TimeParser.parseMinutesToHours(timeInMinutes)));
							hoursAmount.setEnabled(false);

							/** hours increase 1 click */
							Button hoursIncreaseButton = (Button) layout.findViewById(R.id.hoursIncreaseButton);
							hoursIncreaseButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) hoursAmount.getText().toString());
									hoursRemaining = TimeParser.setHours(currentTime, 1);
									hoursAmount.setText(hoursRemaining);
								}
							});

							/** hours decrease -1 click */
							Button hoursDecreaseButton = (Button) layout.findViewById(R.id.hoursDecreaseButton);
							hoursDecreaseButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) hoursAmount.getText().toString());
									hoursRemaining = TimeParser.setHours(currentTime, -1);
									hoursAmount.setText(hoursRemaining);

								}
							});

							/** setup minutesAmount enabled = false */
							final EditText minutesAmount = (EditText) layout.findViewById(R.id.minutesAmount);
							minutesAmount.setText(Integer.toString(TimeParser.parseMinutesRemaining(timeInMinutes)));
							minutesAmount.setEnabled(false);

							/** minutes increase 1 click */
							Button minutesIncreaseButton = (Button) layout.findViewById(R.id.minutesIncreaseButton);
							minutesIncreaseButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
									minutesRemaining = TimeParser.setMinutes(currentTime, 1);
									minutesAmount.setText(minutesRemaining);
								}
							});

							/** minutes decrease -1 click */
							Button minutesDecreaseButton = (Button) layout.findViewById(R.id.minutesDecreaseButton);
							minutesDecreaseButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
									minutesRemaining = TimeParser.setMinutes(currentTime, -1);
									minutesAmount.setText(minutesRemaining);
								}
							});

							/** minutes increase 5 click */
							Button minutesIncreaseButtonPlus5 = (Button) layout.findViewById(R.id.minutesIncreaseButtonPlus5);
							minutesIncreaseButtonPlus5.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
									minutesRemaining = TimeParser.setMinutes(currentTime, 5);
									minutesAmount.setText(minutesRemaining);
								}
							});

							/** minutes decrease -5 click */
							Button minutesIncreaseButtonMinus5 = (Button) layout.findViewById(R.id.minutesIncreaseButtonMinus5);
							minutesIncreaseButtonMinus5.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
									minutesRemaining = TimeParser.setMinutes(currentTime, -5);
									minutesAmount.setText(minutesRemaining);
								}
							});

							/** save and start the timer selected from recipe */
							Button SaveButton = (Button) layout.findViewById(R.id.SaveButton);
							SaveButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {

									/** delete and add the item saved from the recipe selection */
									String currentNameDB = name + " " + style + " " + time;
									long currentTimeDB = (long) (timeInMinutes * 60 * 1000);
									try {
										itemsDataSource.deleteItemByName(currentNameDB);
									} catch (Exception e) {
									}
									try {
										itemsDataSource.createItem(currentNameDB, currentTimeDB);
									} catch (Exception e) {
									}

									/** begin the new timer setup with the user's selected recipe values */
									Intent intent = new Intent(RecipeActivity.this, TimerActivity.class);
									intent.putExtra("timeInMinutes", timeInMinutes);
									if (unit.equals("meal")) {
										intent.putExtra("timerTitle", name + " " + style + " " + time);
									} else {
										intent.putExtra("timerTitle", name + " " + style + " " + time + "\n[per: " + unit + "]");
									}
									startActivity(intent);
								}
							});
						}
					});
					ll.addView(tvTitle);

					/** continue populating the main menu of recipes by adding the "style" or description of the recipe below the title */
					TextView tvDescription = new TextView(layout.getContext());
					tvDescription.setId(loop);
					tvDescription.setTextSize(15);
					CharSequence tvTextDesc = "";
					if (currentUnit.equals("meal")) {
						tvTextDesc = (CharSequence) currentStyle + " : " + currentTime;
					} else {
						tvTextDesc = (CharSequence) currentStyle + " : " + currentTime + " \n[per: " + currentUnit + "]";
					}
					tvDescription.setText(tvTextDesc);
					tvDescription.setLayoutParams(lp);
					tvDescription.setClickable(true);
					tvDescription.setPadding(15, 0, 0, 10);
					ll.addView(tvDescription);

					/** add a small borderline to separate each recipe for ease on the eyes */
					LinearLayout borderSeparator = new LinearLayout(layout.getContext());
					LinearLayout.LayoutParams borderSeparatorLP = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 2);
					borderSeparator.setLayoutParams(borderSeparatorLP);
					borderSeparator.setBackgroundResource(getResources().getIdentifier("separator", "drawable", getPackageName()));
					borderSeparator.setPadding(0, 20, 0, 20);
					ll.addView(borderSeparator);
					loop++;
				}
			} catch (IOException ex) {
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		} catch (IOException e) {
		}
	}

	/**
	 * open our data connections to items
	 */
	private void openDataConnections() {
		itemsDataSource = new ItemsDataSource(this);
		itemsDataSource.open();
	}

	@Override
	protected void onResume() {
		super.onResume();
		itemsDataSource.open();
	}

	@Override
	protected void onPause() {
		super.onPause();
		itemsDataSource.close();
	}
}