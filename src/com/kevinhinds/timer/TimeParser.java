package com.kevinhinds.timer;

/**
 * TimeParser
 * 
 * @author khinds
 */
public class TimeParser {

	/**
	 * parse long length of time to length in minutes
	 * 
	 * @param time
	 * @return
	 */
	static int parseSuggestedTime(String time) {

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

	/**
	 * get the the number of hours for a length of minutes
	 * 
	 * @param timeInMinutes
	 * @return
	 */
	static int parseMinutesToHours(int timeInMinutes) {
		return timeInMinutes / 60;
	}

	/**
	 * get the the number of minutes remaining after the number of hours is divided out
	 * 
	 * @param timeInMinutes
	 * @return
	 */
	static int parseMinutesRemaining(int timeInMinutes) {
		return timeInMinutes % 60;
	}

	/**
	 * get a human readable time value to show to the user for how much time left
	 * 
	 * @param millisUntilFinished
	 * @return human readable time left for the timer
	 */
	static String getHumanReadableTimeValue(long millisUntilFinished) {
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
	 * set the minutes current time by altering the current minutes amount
	 * 
	 * @param currentTime
	 * @param timeChange
	 * @return
	 */
	static CharSequence setMinutes(int currentTime, int timeChange) {
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
	static CharSequence setHours(int currentTime, int timeChange) {
		int returnValue = currentTime + timeChange;
		if (returnValue > 24) {
			returnValue = 24;
		}
		if (returnValue < 0) {
			returnValue = 0;
		}
		return (CharSequence) Integer.toString(returnValue);
	}
}