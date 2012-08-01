package com.kevinhinds.timer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recipe);
		/** get the string resources to get the menuValues array */
		Resources res = getResources();
		final String[] menuValues = res.getStringArray(R.array.assetFilesArray);

		/**
		 * attach to the LinearLayout to add TextViews dynamically via menuValues
		 */
		LinearLayout ll = (LinearLayout) findViewById(R.id.mainRecipeLayout);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		for (int i = 0; i < menuValues.length; i++) {
			/**
			 * i needs to be final because it will be used in an additional scope
			 */
			final int iteration = i;

			TextView tv = new TextView(this);
			tv.setId(i);
			tv.setTextSize(20);
			tv.setText((CharSequence) menuValues[iteration]);
			tv.setLayoutParams(lp);
			tv.setClickable(true);
			tv.setPadding(5, 10, 0, 10);
			final String menuType = menuValues[iteration];
			tv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					initiatePopupWindow(menuType);
				}
			});
			ll.addView(tv);

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

		try {
			InputStream is = getAssets().open(menuType);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			try {
				String line;
				RowDataValues.clear();
				while ((line = reader.readLine()) != null) {

					RowData = line.split("\t");
					RowDataValues.add(line);
					String name = RowData[0];
					String style = RowData[1];
					String time = RowData[2];
					String unit = RowData[3];

					/**
					 * attach to the LinearLayout to add TextViews dynamically via menuValues
					 */
					LinearLayout ll = (LinearLayout) layout.findViewById(R.id.recipeContainer);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

					TextView tvTitle = new TextView(layout.getContext());
					tvTitle.setId(loop);
					tvTitle.setTextSize(18);
					CharSequence tvText = "";
					tvText = (CharSequence) name;
					tvTitle.setText(Html.fromHtml("<u>" + tvText + "</u>"));
					tvTitle.setLayoutParams(lp);
					tvTitle.setClickable(true);
					tvTitle.setPadding(5, 10, 0, 10);
					tvTitle.setContentDescription((CharSequence) Integer.toString(loop));
					tvTitle.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							LinearLayout recipeFormContainer = (LinearLayout) layout.findViewById(R.id.recipeFormContainer);
							recipeFormContainer.setVisibility(View.VISIBLE);

							ScrollView timerLayout = (ScrollView) layout.findViewById(R.id.timerLayout);
							timerLayout.setVisibility(View.GONE);

							String RowDataValue = RowDataValues.get(Integer.parseInt((String) v.getContentDescription()));
							String[] RowDataValueArray = RowDataValue.split("\t");
							final String name = RowDataValueArray[0];
							final String style = RowDataValueArray[1];
							final String time = RowDataValueArray[2];
							final String unit = RowDataValueArray[3];

							TextView textViewSuggestedTimeValue = (TextView) layout.findViewById(R.id.textViewSuggestedTimeValue);
							if (unit.equals("meal")) {
								textViewSuggestedTimeValue.setText(time);
							} else {
								textViewSuggestedTimeValue.setText(time + " [per: " + unit + "]");
							}

							final int timeInMinutes = RecipeActivity.parseSuggestedTime(time);

							TextView editTextTitle = (TextView) layout.findViewById(R.id.editTextTitle);
							editTextTitle.setText(name);

							TextView editTextStyle = (TextView) layout.findViewById(R.id.editTextStyle);
							editTextStyle.setText(style);

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
							hoursAmount.setText(Integer.toString(RecipeActivity.parseMinutesToHours(timeInMinutes)));
							hoursAmount.setEnabled(false);

							/** hours increase 1 click */
							Button hoursIncreaseButton = (Button) layout.findViewById(R.id.hoursIncreaseButton);
							hoursIncreaseButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) hoursAmount.getText().toString());
									hoursRemaining = TimerActivity.setHours(currentTime, 1);
									hoursAmount.setText(hoursRemaining);
								}
							});

							/** hours decrease -1 click */
							Button hoursDecreaseButton = (Button) layout.findViewById(R.id.hoursDecreaseButton);
							hoursDecreaseButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) hoursAmount.getText().toString());
									hoursRemaining = TimerActivity.setHours(currentTime, -1);
									hoursAmount.setText(hoursRemaining);

								}
							});

							/** setup minutesAmount enabled = false */
							final EditText minutesAmount = (EditText) layout.findViewById(R.id.minutesAmount);
							minutesAmount.setText(Integer.toString(RecipeActivity.parseMinutesRemaining(timeInMinutes)));
							minutesAmount.setEnabled(false);

							/** minutes increase 1 click */
							Button minutesIncreaseButton = (Button) layout.findViewById(R.id.minutesIncreaseButton);
							minutesIncreaseButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
									minutesRemaining = TimerActivity.setMinutes(currentTime, 1);
									minutesAmount.setText(minutesRemaining);
								}
							});

							/** minutes decrease -1 click */
							Button minutesDecreaseButton = (Button) layout.findViewById(R.id.minutesDecreaseButton);
							minutesDecreaseButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
									minutesRemaining = TimerActivity.setMinutes(currentTime, -1);
									minutesAmount.setText(minutesRemaining);
								}
							});

							/** minutes increase 5 click */
							Button minutesIncreaseButtonPlus5 = (Button) layout.findViewById(R.id.minutesIncreaseButtonPlus5);
							minutesIncreaseButtonPlus5.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
									minutesRemaining = TimerActivity.setMinutes(currentTime, 5);
									minutesAmount.setText(minutesRemaining);
								}
							});

							/** minutes decrease -5 click */
							Button minutesIncreaseButtonMinus5 = (Button) layout.findViewById(R.id.minutesIncreaseButtonMinus5);
							minutesIncreaseButtonMinus5.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int currentTime = Integer.parseInt((String) minutesAmount.getText().toString());
									minutesRemaining = TimerActivity.setMinutes(currentTime, -5);
									minutesAmount.setText(minutesRemaining);
								}
							});

							/** save and start the timer selected from recipe */
							Button SaveButton = (Button) layout.findViewById(R.id.SaveButton);
							SaveButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
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

					TextView tvDescription = new TextView(layout.getContext());
					tvDescription.setId(loop);
					tvDescription.setTextSize(15);
					CharSequence tvTextDesc = "";
					if (unit.equals("meal")) {
						tvTextDesc = (CharSequence) style + " : " + time;
					} else {
						tvTextDesc = (CharSequence) style + " : " + time + " \n[per: " + unit + "]";
					}
					tvDescription.setText(tvTextDesc);
					tvDescription.setLayoutParams(lp);
					tvDescription.setClickable(true);
					tvDescription.setPadding(15, 0, 0, 10);
					ll.addView(tvDescription);

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

	private static int parseSuggestedTime(String time) {

		int timeInMinutes = 0;
		try {

			boolean isHoursTime = false;
			int isHoursTimeCheck = time.indexOf(" hours");
			if (isHoursTimeCheck > 0) {
				isHoursTime = true;
			}

			if (isHoursTime) {
				time = time.replace(" hours", "");
			} else {
				time = time.replace(" minutes", "");
			}

			boolean isRangeValue = false;
			int isRangeValueCheck = time.indexOf(" to ");
			if (isRangeValueCheck > 0) {
				isRangeValue = true;
			}

			if (isRangeValue) {
				String[] rangeValues = time.split(" to ");
				String fromTime = rangeValues[0];
				String toTime = rangeValues[1];
				timeInMinutes = ((Integer.parseInt(fromTime) + Integer.parseInt(toTime)) / 2);
			} else {
				timeInMinutes = Integer.parseInt(time);
			}

			if (isHoursTime) {
				timeInMinutes = timeInMinutes * 60;
			}

		} catch (Exception e) {
			timeInMinutes = 0;
		}
		return timeInMinutes;
	}

	private static int parseMinutesToHours(int timeInMinutes) {
		return timeInMinutes / 60;
	}

	private static int parseMinutesRemaining(int timeInMinutes) {
		return timeInMinutes % 60;
	}
}