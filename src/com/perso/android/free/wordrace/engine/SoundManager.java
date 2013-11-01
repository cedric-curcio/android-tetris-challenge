package com.perso.android.free.wordrace.engine;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Singleton that manages sound through the application.
 * 
 * @author cedric
 *
 */
public class SoundManager {
	private static SoundManager instance;
	private SoundPool mSoundPool;
	private HashMap<Integer, Integer> mSoundPoolMap;
	private AudioManager mAudioManager;
	private Context mContext;
	private boolean isInit = false;
//	private int mIndex;

	private SoundManager(){
//		mIndex = 0;
	}
	
	public static SoundManager getInstance(){
		if (instance == null){
			instance = new SoundManager();
		}
		return instance;
	}


	/**
	 * Initialize the singleton.
	 * This method should be called just after getInstance().
	 * @param c
	 */
	public void init(Context c){
		mContext = c;
		mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		mSoundPoolMap = new HashMap<Integer, Integer> ();
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		isInit = true;
	}
	/**
	 * 
	 * Add a sound to the pool.
	 * 
	 * @param index
	 * @param soundID
	 */
	public void addSound(int soundID){
		mSoundPoolMap.put(soundID, mSoundPool.load(mContext, soundID, 1));
	}

	/**
	 * Play the sound one time at the given index
	 * @param soundID
	 */
	public void playSound(int soundID){
		if(mAudioManager == null){
			return;
		}
		float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mSoundPool.play(mSoundPoolMap.get(soundID), streamVolume, streamVolume, 1, 0, 1f);
	}

	/**
	 * Play the sound in loop at the given index
	 * @param soundID
	 */
	public void playLoopedSound(int soundID){
		if(mAudioManager == null){
			return;
		}
		float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mSoundPool.play(mSoundPoolMap.get(soundID), streamVolume, streamVolume, 1, -1, 1f);
	}
	
	/**
	 * Stop the sound at the given index.
	 * @param soundID
	 */
	public void stopSound(int soundID){
		mSoundPool.stop(mSoundPoolMap.get(soundID));
	}

	public boolean isInit() {
		return isInit;
	}

}
