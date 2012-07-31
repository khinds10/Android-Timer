package com.kevinhinds.timer;

import com.kevinhinds.timer.sound.SoundManager;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class TimerActivity extends Activity {
	private com.kevinhinds.timer.sound.SoundManager mSoundManager;
	private com.kevinhinds.timer.sound.SoundManager mSoundManagerRinger;
	private boolean clickSoundPlaying;
	private CountDownTimer countDownTimer = null;
	private long resumeMilliseconds = 0;
	protected CharSequence hoursRemaining = "0";
	protected CharSequence minutesRemaining = "0";
	private boolean timerRunning;
	private View layout = null;
	private PopupWindow pw;
	private float degreesFrom = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer);

		try {
			/** get the searchType from the intent extras */
			Bundle extras = getIntent().getExtras();
			int timeInMinutes = extras.getInt("timeInMinutes");
			String timerTitle = extras.getString("timerTitle");
			resumeMilliseconds = timeInMinutes * 60 * 1000;
			
			String humanReadableTime = getHumanReadableTimeValue(resumeMilliseconds);
			
			TextView mainTimerCount = (TextView) findViewById(R.id.mainTimerCount);
			mainTimerCount.setText(humanReadableTime);
		
			TextView currentTimerName = (TextView) findViewById(R.id.currentTimerName);
			currentTimerName.setText(timerTitle);
			
		} catch (Exception e) {
			resumeMilliseconds = 5000 * 60;
		}
		
		/** setup the clicking sound */
		setupClickSound();

		/** click the play sound checkBox */
		CheckBox playSoundCheckBox = (CheckBox) findViewById(R.id.playSoundCheckBox);
		playSoundCheckBox.setChecked(true);
		playSoundCheckBox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				checkPlayClickSound();
			}
		});

		/** click the start button */
		Button startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startTimer();
			}
		});

		/** click the stop button */
		Button stopButton = (Button) findViewById(R.id.stopButton);
		stopButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				shutdownTimer();
			}
		});

		Button setButton = (Button) findViewById(R.id.setButton);
		setButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				shutdownTimer();
				initiatePopupWindow();
			}
		});

		Button presetsButton = (Button) findViewById(R.id.presetsButton);
		presetsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				shutdownTimer();
				Intent intent = new Intent(TimerActivity.this, RecipeActivity.class);
				startActivity(intent);
			}
		});

		rotateTimer(resumeMilliseconds / 1000 / 60);
	}

	/**
	 * rotate the timer to a certain number of minutes
	 * 
	 * @param minutesToGo
	 *            how many minutes to rotate the timer too
	 */
	private void rotateTimer(float minutesToGo) {

		/** if over an hour then it's just the remainder on the dial */
		if (minutesToGo > 60) {
			minutesToGo = minutesToGo % 360;
		}

		/** base 60 minutes to degrees */
		float degreesTo = (int) (minutesToGo / 0.166666667);

		/** rotate the dial to the number of minutes via RotateAnimation */
		ImageView timerDial = (ImageView) findViewById(R.id.dialImage);
		RotateAnimation rAnim = new RotateAnimation(degreesFrom, degreesTo, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rAnim.setFillAfter(true);
		rAnim.setDuration(500);
		timerDial.startAnimation(rAnim);
		degreesFrom = degreesTo;
	}

	/**
	 * if a timer is currently running, shut-er-down
	 */
	private void shutdownTimer() {
		if (countDownTimer != null) {
			countDownTimer.cancel();
			stopTimer();
		}
	}

	/**
	 * start timer click
	 */
	private void startTimer() {

		/**
		 * create a new instance of the Android Countdown timer if another timer is running, cancel it
		 */
		shutdownTimer();

		/** the timer is now flagged as running */
		timerRunning = true;

		/** resume the clicking sound if the box is checked */
		CheckBox playSoundCheckBox = (CheckBox) findViewById(R.id.playSoundCheckBox);
		if (playSoundCheckBox.isChecked()) {

			/** there's a bug with Android Sound Manager, create a new thread and 1 second out resume the clicking sound */
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						synchronized (this) {
							wait(1000);
						}
					} catch (InterruptedException ex) {
					}
					/** resume the clicking sound after 1 second on this thread */
					checkPlayClickSound();
				}
			};
			thread.start();
		}

		/** create the timer class */
		countDownTimer = new CountDownTimer(resumeMilliseconds, 1000) {

			/**
			 * set the value of the timer time left each time the timer 'ticks' per second
			 */
			public void onTick(long millisUntilFinished) {
				resumeMilliseconds = millisUntilFinished;
				TextView mainTimerCount = (TextView) findViewById(R.id.mainTimerCount);
				mainTimerCount.setText(getHumanReadableTimeValue(millisUntilFinished));

				/** rotate the timer to the time selected */
				float rotateToTime = (float) millisUntilFinished / 1000 / 60;
				if (rotateToTime == 1) {
					rotateToTime = 0;
				}
				rotateTimer(rotateToTime);
			}

			/**
			 * timer has finished on its own event
			 */
			public void onFinish() {
				TextView mainTimerCount = (TextView) findViewById(R.id.mainTimerCount);
				mainTimerCount.setText("00:00:00");
				stopTimer();
				playRinger();
			}
		}.start();
	}

	/**
	 * get a human readable time value to show to the user for how much time left
	 * 
	 * @param millisUntilFinished
	 * @return human readable time left for the timer
	 */
	private String getHumanReadableTimeValue(long millisUntilFinished) {
		String timerLengthValue = "";
		String tempValue = "";
		int secUntilFinished = (int) millisUntilFinished / 1000;
		int hours = secUntilFinished / 3600;
		int minutes = (secUntilFinished % 3600) / 60;
		int seconds = (secUntilFinished % 60);

		if (hours > 0) {
			tempValue = Integer.toString(hours);
			if (hours < 10) {
				tempValue = "0" + tempValue;
			}
			timerLengthValue = timerLengthValue + tempValue + ":";
		} else {
			timerLengthValue = timerLengthValue + "00:";
		}
		if (minutes > 0) {
			tempValue = Integer.toString(minutes);
			if (minutes < 10) {
				tempValue = "0" + tempValue;
			}
			timerLengthValue = timerLengthValue + tempValue + ":";
		} else {
			timerLengthValue = timerLengthValue + "00:";
		}
		if (seconds > 0) {
			tempValue = Integer.toString(seconds);
			if (seconds < 10) {
				tempValue = "0" + tempValue;
			}
			timerLengthValue = timerLengthValue + tempValue;
		} else {
			timerLengthValue = timerLengthValue + "00";
		}
		return timerLengthValue;
	}

	/**
	 * play the ringer because the timer has finished
	 */
	private void playRinger() {

		/** there's a bug with Android Sound Manager, create a new thread and 1 second out play the ringer sound */
		mSoundManagerRinger = new SoundManager();
		mSoundManagerRinger.initSounds(this);
		mSoundManagerRinger.addSound(1, R.raw.ringing);

		/** play the ringer sound after 1 second on this thread */
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						wait(1000);
					}
				} catch (InterruptedException ex) {
				}
				mSoundManagerRinger.playSound(1);
			}
		};
		thread.start();
	}

	/**
	 * stop timer, either based on user request or it actually finished
	 */
	private void stopTimer() {
		timerRunning = false;
		stopClickSound();
	}

	/**
	 * click the play sound checkBox and deal appropriately
	 */
	private void checkPlayClickSound() {
		if (clickSoundPlaying) {
			stopClickSound();
		} else {
			if (timerRunning) {
				playClickSound();
			}
		}
	}

	/**
	 * setup the new soundmanager with the context being "this" and the clicking sound
	 */
	private void setupClickSound() {
		mSoundManager = new SoundManager();
		mSoundManager.initSounds(this);
		mSoundManager.addSound(1, R.raw.clicking);
	}

	/**
	 * play the clicking sound looped forever
	 */
	private void playClickSound() {
		clickSoundPlaying = true;
		mSoundManager.playLoopedSound(1);
	}

	/**
	 * stop sound and setup the new instance of the clicking sound
	 */
	private void stopClickSound() {
		clickSoundPlaying = false;
		mSoundManager.stopSound(1);
		setupClickSound();
	}

	/**
	 * show the popup window
	 */
	private void initiatePopupWindow() {

		/** get screen metrics */
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;

		/** reset the hours and minutes remaining */
		hoursRemaining = "0";
		minutesRemaining = "0";

		/** adjust the popup WxH */
		float popupWidth = (float) (width * .75);
		float popupHeight = (float) (height * .60);

		/** We need to get the instance of the LayoutInflater, use the context of this activity */
		LayoutInflater inflater = (LayoutInflater) TimerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		/** Inflate the view from a predefined XML layout */
		layout = inflater.inflate(R.layout.popup_layout, (ViewGroup) findViewById(R.id.popup_element));
		/** create a 300px width and 350px height PopupWindow */
		pw = new PopupWindow(layout, (int) popupWidth, (int) popupHeight, true);
		/** display the popup in the center */
		pw.showAtLocation(layout, Gravity.CENTER, 0, 0);

		/** setup hoursAmount enabled = false */
		final EditText hoursAmount = (EditText) layout.findViewById(R.id.hoursAmount);
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

		Button setButton = (Button) layout.findViewById(R.id.setButton);
		setButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int hoursRemainingInteger = Integer.parseInt(hoursRemaining.toString());
				int minRemainingInteger = Integer.parseInt(minutesRemaining.toString());
				int resumeMillisecondsLeft = (hoursRemainingInteger * 60 * 60 * 1000) + minRemainingInteger * 60 * 1000;
				resumeMilliseconds = (long) resumeMillisecondsLeft;
				TextView mainTimerCount = (TextView) findViewById(R.id.mainTimerCount);

				String humanReadableTime = getHumanReadableTimeValue(resumeMilliseconds);
				mainTimerCount.setText(humanReadableTime);

				TextView currentTimerName = (TextView) findViewById(R.id.currentTimerName);
				currentTimerName.setText(humanReadableTime + " Timer");

				/** rotate the timer to the time selected */
				rotateTimer((int) (resumeMillisecondsLeft / 1000 / 60));
				pw.dismiss();
			}
		});

		/** cancel button to close */
		Button cancelButton = (Button) layout.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				pw.dismiss();
			}
		});
	}

	/**
	 * set the minutes current time by altering the current minutes amount
	 * 
	 * @param currentTime
	 * @param timeChange
	 * @return
	 */
	protected static CharSequence setMinutes(int currentTime, int timeChange) {
		int returnValue = currentTime + timeChange;
		if (returnValue > 59) {
			returnValue = 59;
		}
		if (returnValue < 0) {
			returnValue = 0;
		}
		return (CharSequence) Integer.toString(returnValue);
	}

	/**
	 * set the hours current time by altering the current minutes amount
	 * 
	 * @param currentTime
	 * @param timeChange
	 * @return
	 */
	protected static CharSequence setHours(int currentTime, int timeChange) {
		int returnValue = currentTime + timeChange;
		if (returnValue > 24) {
			returnValue = 24;
		}
		if (returnValue < 0) {
			returnValue = 0;
		}
		return (CharSequence) Integer.toString(returnValue);
	}

	@Override
	/**
	 * when the activity is destroyed, be sure to stop the sound manager :)
	 */
	protected void onDestroy() {
		super.onDestroy();
		mSoundManager.stopSound(1);
	}
}