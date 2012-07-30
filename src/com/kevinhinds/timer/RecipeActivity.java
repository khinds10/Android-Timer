package com.kevinhinds.timer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class RecipeActivity extends Activity {
	private View layout = null;
	private PopupWindow pw;

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
		float popupWidth = (float) (width * .80);
		float popupHeight = (float) (height * .85);

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
				int i = 0;
				while ((line = reader.readLine()) != null) {
					i++;
					String[] RowData = line.split("\t");
					String name = RowData[0];
					String style = RowData[1];
					String time = RowData[2];
					String unit = RowData[3];

					/**
					 * attach to the LinearLayout to add TextViews dynamically via menuValues
					 */
					LinearLayout ll = (LinearLayout) layout.findViewById(R.id.recipeContainer);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					TextView tv = new TextView(layout.getContext());
					tv.setId(i);
					tv.setTextSize(15);
					tv.setText((CharSequence) name+"\n"+style+" : "+time+ " \n[per: "+unit+"]");
					tv.setLayoutParams(lp);
					tv.setClickable(true);
					tv.setPadding(5, 10, 0, 10);

					tv.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							pw.dismiss();
						}
					});
					ll.addView(tv);
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
}
