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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;

public class TimerActivity extends Activity {
	private com.kevinhinds.timer.sound.SoundManager mSoundManager;
	private com.kevinhinds.timer.sound.SoundManager mSoundManagerRinger;
	private boolean clickSoundPlaying;
	private CountDownTimer countDownTimer = null;
	private long resumeMilliseconds = 1000 * 10;
	private boolean timerRunning;
	private View layout = null;
	private PopupWindow pw;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer);

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
				if (countDownTimer != null) {
					countDownTimer.cancel();
					stopTimer();
				}
			}
		});

		Button setButton = (Button) findViewById(R.id.setButton);
		setButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				initiatePopupWindow();
			}
		});
	}

	/**
	 * start timer click
	 */
	private void startTimer() {

		/**
		 * create a new instance of the Android Countdown timer if another timer is running, cancel it
		 */
		if (countDownTimer != null) {
			countDownTimer.cancel();
			stopTimer();
		}

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

		/** adjust the popup WxH */
		float popupWidth = (float) (width * .75);
		float popupHeight = (float) (height * .60);
		//float popupButtonPadding = (float) (height * .38);

		/** We need to get the instance of the LayoutInflater, use the context of this activity */
		LayoutInflater inflater = (LayoutInflater) TimerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		/** Inflate the view from a predefined XML layout */
		layout = inflater.inflate(R.layout.popup_layout, (ViewGroup) findViewById(R.id.popup_element));
		/** create a 300px width and 350px height PopupWindow */
		pw = new PopupWindow(layout, (int) popupWidth, (int) popupHeight, true);
		/** display the popup in the center */
		pw.showAtLocation(layout, Gravity.CENTER, 0, 0);

		/** customize the button position of the popup based on the height determined from the screen resolution */
		LinearLayout buttonContainerLayout = (LinearLayout) layout.findViewById(R.id.buttonContainer);
		//buttonContainerLayout.setPadding(0, ((int) popupButtonPadding), 0, 0);

		Button cancelButton = (Button) layout.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(cancel_button_click_listener);
	}

	private OnClickListener cancel_button_click_listener = new OnClickListener() {
		public void onClick(View v) {
			pw.dismiss();
		}
	};

	@Override
	/**
	 * when the activity is destroyed, be sure to stop the sound manager :)
	 */
	protected void onDestroy() {
		super.onDestroy();
		mSoundManager.stopSound(1);
	}
}