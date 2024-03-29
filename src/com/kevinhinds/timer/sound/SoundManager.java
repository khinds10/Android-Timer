package com.kevinhinds.timer.sound;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {

	private SoundPool mSoundPool;
	private HashMap<Integer, Integer> mSoundPoolMap;
	private AudioManager mAudioManager;
	private Context mContext;

	public SoundManager() {
		
	}

	/**
	 * initialize the sounds
	 * @param theContext
	 */
	public void initSounds(Context theContext) {
		mContext = theContext;
		mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		mSoundPoolMap = new HashMap<Integer, Integer>();
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * add a sound by identifier to be played
	 * @param Index
	 * @param SoundID
	 */
	public void addSound(int Index, int SoundID) {
		mSoundPoolMap.put(Index, mSoundPool.load(mContext, SoundID, 1));
	}

	/**
	 * play a sound by identifier
	 * @param index
	 */
	public void playSound(int index) {
		int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mSoundPool.play(mSoundPoolMap.get(index), streamVolume, streamVolume, 1, 0, 1f);
	}

	/**
	 * play the sound looped forever
	 * @param index
	 */
	public void playLoopedSound(int index) {
		int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mSoundPool.play(mSoundPoolMap.get(index), streamVolume, streamVolume, 1, -1, 1f);
	}

	/**
	 * stop sound by identifier
	 * @param index
	 */
	public void stopSound(int index) {
		mSoundPool.stop(mSoundPoolMap.get(index));
		mSoundPool.release();
	}
}