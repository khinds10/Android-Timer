package com.kevinhinds.timer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecipeActivity extends Activity {

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
			String menuType = menuValues[iteration];
			tv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(RecipeActivity.this, TimerActivity.class);
					startActivity(intent);
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
}
