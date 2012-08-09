package com.kevinhinds.timer;

import com.kevinhinds.timer.sound.SoundManager;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class TimerActivity extends Activity {
	private com.kevinhinds.timer.sound.SoundManager mSoundManager;
	private com.kevinhinds.timer.sound.SoundManager mSoundManagerRinger;
	private boolean clickSoundPlaying = false;
	private boolean clickSoundShouldPlay = true;
	private CountDownTimer countDownTimer = null;
	private long resumeMilliseconds = 0;
	protected CharSequence hoursRemaining = "0";
	protected CharSequence minutesRemaining = "0";
	private boolean timerRunning;
	private View layout = null;
	private PopupWindow pw;
	private float degreesFrom = 0;
	private Uri chosenRingtone = null;
	private String timerTitle;
	private String humanReadableTime;
	private int screenHeight;
	private int screenWidth;
	private int dialHeight;
	private int screenCenterPoint;
	int minutesMove = 0;
	private int minutesConfirm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer);

		try {
			/** get the searchType from the intent extras */
			Bundle extras = getIntent().getExtras();
			int timeInMinutes = extras.getInt("timeInMinutes");
			timerTitle = extras.getString("timerTitle");
			resumeMilliseconds = timeInMinutes * 60 * 1000;
			humanReadableTime = TimeParser.getHumanReadableTimeValue(resumeMilliseconds);

			TextView mainTimerCount = (TextView) findViewById(R.id.mainTimerCount);
			mainTimerCount.setText(humanReadableTime);

			TextView currentTimerName = (TextView) findViewById(R.id.currentTimerName);
			currentTimerName.setText(timerTitle);

		} catch (Exception e) {
			timerTitle = "5 Minute Timer";
			resumeMilliseconds = 5000 * 60;
			humanReadableTime = TimeParser.getHumanReadableTimeValue(resumeMilliseconds);
		}

		/** get screen metrics */
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenHeight = displaymetrics.heightPixels;
		screenWidth = displaymetrics.widthPixels;
		dialHeight = screenHeight / 3;
		screenCenterPoint = screenWidth / 2;

		/** setup the clicking sound */
		setupClickSound();

		/** click the start button */
		Button startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startTimer();
				startNotification();
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

		rotateTimer(resumeMilliseconds / 1000 / 60, 500);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clicking:
			clickSoundShouldPlay = !clickSoundShouldPlay;
			checkPlayClickSound();
			break;
		case R.id.chooseAlarm:
			Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (CharSequence) null);
			this.startActivityForResult(intent, 5);
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		if (resultCode == Activity.RESULT_OK && requestCode == 5) {
			Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if (uri != null) {
				chosenRingtone = uri;
				Toast.makeText(this, "Alarm Sound Updated", Toast.LENGTH_LONG).show();
			} else {
				chosenRingtone = null;
			}
		}
	}

	/**
	 * capture a touch event to possibly move the timer
	 */
	public boolean onTouchEvent(MotionEvent event) {

		/** get the touch event coordinates */
		int eventaction = event.getAction();
		float eventX = event.getX();
		float eventY = event.getY();

		/** if we've capture a touch action within the range of where the dial is */
		if ((int) eventY > dialHeight) {

			switch (eventaction) {

			/** finger touches the screen */
			case MotionEvent.ACTION_DOWN:
				break;

			/** finger moves on the screen */
			case MotionEvent.ACTION_MOVE:

				/** any current timer is now shutdown */
				shutdownTimer();

				/** right side of dial */
				if (eventX > screenCenterPoint) {

					/** the calculations below are just trial and error, need a real algorithm */
					float percent = (eventY / screenHeight) * 100;
					float minutesPercent = (float) (percent - 40 * 1.2);
					minutesMove = (int) minutesPercent;
					if (minutesMove < 0) {
						minutesMove = 0;
					}
					if (minutesMove > 30) {
						minutesMove = 30;
					}
					rotateTimer(minutesMove, 500);
				}

				/** left side of dial */
				if (eventX < screenCenterPoint) {

					/** the calculations below are just trial and error, need a real algorithm */
					float percent = (eventY / screenHeight) * 100;
					float minutesPercent = (float) (percent - 10 * 1.2);
					minutesMove = (int) minutesPercent;
					if (minutesMove < 30) {
						minutesMove = 30;
					}
					if (minutesMove > 59) {
						minutesMove = 59;
					}
					rotateTimer(30 - minutesMove, 500);
				}
				break;

			/** finger leaves the screen */
			case MotionEvent.ACTION_UP:

				/** made up algorithm, need to clean this up */
				minutesConfirm = minutesMove;
				if (minutesConfirm > 30) {
					minutesConfirm = 60 - minutesConfirm;
					minutesConfirm = minutesConfirm + 30;
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(TimerActivity.this);
				builder.setTitle("Update Timer");
				builder.setMessage("Update the current timer to " + Integer.toString(minutesConfirm) + " minutes?").setCancelable(false)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								int resumeMillisecondsLeft = minutesConfirm * 60 * 1000;
								resumeMilliseconds = (long) resumeMillisecondsLeft;
								TextView mainTimerCount = (TextView) findViewById(R.id.mainTimerCount);

								humanReadableTime = TimeParser.getHumanReadableTimeValue(resumeMilliseconds);
								mainTimerCount.setText(humanReadableTime);

								TextView currentTimerName = (TextView) findViewById(R.id.currentTimerName);
								timerTitle = humanReadableTime + " Timer";
								currentTimerName.setText(timerTitle);

								/** rotate the timer to the time selected */
								rotateTimer((int) (resumeMillisecondsLeft / 1000 / 60), 0);
								dialog.dismiss();
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								/** rotate the timer to the time selected */
								rotateTimer((int) (resumeMilliseconds / 1000 / 60), 0);
								dialog.cancel();
							}
						});
				builder.setIcon(R.drawable.ic_launcher);
				AlertDialog alert = builder.create();
				alert.show();
				break;
			}
		}

		/** tell the system that we handled the event and no further processing is required */
		return true;
	}

	/**
	 * rotate the timer to a certain number of minutes
	 * 
	 * @param minutesToGo
	 *            how many minutes to rotate the timer too
	 */
	private void rotateTimer(float minutesToGo, int duration) {

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
		rAnim.setDuration(duration);
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

		/** resume the clicking sound if should play sound is true */
		if (clickSoundShouldPlay) {

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
				mainTimerCount.setText(TimeParser.getHumanReadableTimeValue(millisUntilFinished));

				/** rotate the timer to the time selected */
				float rotateToTime = (float) millisUntilFinished / 1000 / 60;
				if (rotateToTime == 1) {
					rotateToTime = 0;
				}
				rotateTimer(rotateToTime, 500);
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

				if (chosenRingtone == null) {
					mSoundManagerRinger.playSound(1);
				} else {
					try {
						Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), chosenRingtone);
						r.play();
					} catch (Exception e) {
					}
				}
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
	 * start a new notification because the timer is running
	 */
	private void startNotification() {

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher, "Timer is Running", System.currentTimeMillis());

		/** Hide the notification after its selected */
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Intent intent = new Intent(this, TimerActivity.class);
		PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
		notification.setLatestEventInfo(this, timerTitle, humanReadableTime, activity);
		notification.number += 1;
		notificationManager.notify(0, notification);
	}

	/**
	 * click the play sound checkBox and deal appropriately
	 */
	private void checkPlayClickSound() {

		String OnOff = "OFF";
		if (clickSoundShouldPlay) {
			OnOff = "ON";
		}
		try {
			Toast.makeText(this, "Timer Clicking " + OnOff, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {

		}

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

		/** reset the hours and minutes remaining */
		hoursRemaining = "0";
		minutesRemaining = "0";

		/** adjust the popup WxH */
		float popupWidth = (float) (screenWidth * .85);
		float popupHeight = (float) (screenHeight * .75);

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

		Button setButton = (Button) layout.findViewById(R.id.setButton);
		setButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int hoursRemainingInteger = Integer.parseInt(hoursRemaining.toString());
				int minRemainingInteger = Integer.parseInt(minutesRemaining.toString());
				int resumeMillisecondsLeft = (hoursRemainingInteger * 60 * 60 * 1000) + minRemainingInteger * 60 * 1000;
				resumeMilliseconds = (long) resumeMillisecondsLeft;
				TextView mainTimerCount = (TextView) findViewById(R.id.mainTimerCount);

				humanReadableTime = TimeParser.getHumanReadableTimeValue(resumeMilliseconds);
				mainTimerCount.setText(humanReadableTime);

				TextView currentTimerName = (TextView) findViewById(R.id.currentTimerName);
				timerTitle = humanReadableTime + " Timer";
				currentTimerName.setText(timerTitle);

				/** rotate the timer to the time selected */
				rotateTimer((int) (resumeMillisecondsLeft / 1000 / 60), 500);
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

	@Override
	/**
	 * when the activity is destroyed, be sure to stop the sound manager :)
	 */
	protected void onDestroy() {
		super.onDestroy();
		mSoundManager.stopSound(1);
	}
}