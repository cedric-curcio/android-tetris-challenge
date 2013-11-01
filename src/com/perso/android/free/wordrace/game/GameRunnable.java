package com.perso.android.free.wordrace.game;

import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.perso.android.free.wordrace.engine.SoundManager;
import com.perso.android.free.wordrace.game.event.GameEvent;
import com.perso.android.free.wordrace.game.view.GameView;

/**
 * The game loop & behavior.
 * @author cedric
 *
 */
public class GameRunnable implements Runnable {

	private static final String TAG = GameRunnable.class.getSimpleName();

	private static final boolean TEST_MODE = false;
	private static final boolean TESTING_GAME_OVER = false;
	private int accessCounter = 0;

	/**
	 *  Game state constants.
	 */
	public static final int STATE_LOADING = -1;	//when we load all bitmaps in memory
	public static final int STATE_PLAY = 0; 	//when we initialize variable, on a retry we can go back to this state
	public static final int STATE_GAME_OVER = 1;//when the game ends
	public static final int STATE_PAUSE = 2;	//when the game is paused
	public static final int STATE_RUNNING = 3;	//when the game is running

	/** current game state */
	private volatile int mGameState;

	/** thread running or not*/
	private volatile boolean mRun;

	/** Handle to the surface manager object we interact with */
	private GameView mGameView;
	/** Handle to the surface manager object we interact with */
	private SurfaceHolder mSurfaceHolder;

	/** game loop delay*/
	private long mDelay = 33;


	private long mLastPauseTime;
	private long mPausedDelay = 0;
	private long deltaPausedTime = 0;

	/** timers for typing enter */
	private long mNowTime = 0;
	/** timer managing thrower throw */
	private Timer mTimer = null;

	/** if we have to redraw */
	private boolean mReDraw = false;

	/** Queue for GameEvents */
	private ConcurrentLinkedQueue<GameEvent> mEventQueue = new ConcurrentLinkedQueue<GameEvent>();

	/** handle the game loop */
	private Handler mLoopHandler = new Handler();

	private Context mContext;
	private GameRules mRaceRules;

	private boolean mIsInBackground = false;
	private boolean mHasNeverBeenUsed = true;

	public GameRunnable(Context c, GameView v, SurfaceHolder surfaceHolder){
		mContext = c;
		mGameView = v;
		mSurfaceHolder = surfaceHolder;
		mRaceRules = new GameRules(c);
		setState(STATE_LOADING);
		mTimer = new Timer();
		//load the sound
		SoundManager sm = SoundManager.getInstance();
		if(!sm.isInit()){
			sm.init(mContext);
		}
		//		sm.addSound(R.raw.metalbathit_loud);
	}

	@Override
	public void run() {

		//update and post handler
		Canvas canvas = null;
		long sleepTime;
		if(mRun){
			mNowTime = System.currentTimeMillis();
			if(mGameState == STATE_LOADING){
				//we will set a waiting bar
				checkLoading();
			}
			else if(mGameState == STATE_PLAY){
				initGame();//first enter
			}
			else if(mGameState == STATE_GAME_OVER){
			}
			else if (mGameState == STATE_RUNNING) {
				if(TEST_MODE){
					doTestScenario();
				}
				updateGameState();
			}
			//draw
			if(mReDraw){
				if(mGameState == STATE_PAUSE){
					mDelay = 500;
				}
				else {
					mDelay = 33;
				}
				try {
					canvas = mSurfaceHolder.lockCanvas(null);
					mGameView.drawAll(canvas);
				} finally {
					if (canvas != null) {
						mSurfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
				mReDraw = false;
			}
			else {
				mDelay = 500;//to save battery/processor if nothing to draw
			}
			sleepTime = mDelay-(System.currentTimeMillis() - mNowTime);
			if(sleepTime>0){
				mLoopHandler.postDelayed(this, sleepTime);
			}
			else{
				mLoopHandler.postDelayed(this, 1);
			}
		}else {
			finishActivity();
		}
	}


	private void doTestScenario() {
		accessCounter++;
		if(TESTING_GAME_OVER && accessCounter>8){
		}
	}

	private void finishActivity(){
		Log.d(TAG, "method finishActivity");
		((GameActivity)mContext).finish();
	}

	private void initGame() {
		//clean all
		mEventQueue.clear();
		deltaPausedTime=0;
		mNowTime = 0;

		// init ia

		//start the game
		mReDraw = true;
		setState(STATE_RUNNING);
	}

	/**
	 * Update all events here
	 */
	protected void updateGameState() {
		GameEvent nextBallMoveEvent = null;
		synchronized (mSurfaceHolder) {
			if(mEventQueue.size()>0){
				mReDraw = true;
			}
			while(true){
				GameEvent event = mEventQueue.poll();
				if(event == null){
					break;
				}
				//				else if(event instanceof ReceiverRunEvent){
				//				}
			}
			//post the next event depending of current situation
			//or change the situation if needed
			//			int state = getRaceState();
			//			if(state == RACE_STATE_INIT_THROW){
			//				mGameRules.initThrow();
			//				mReDraw = true;
			//			}
			//			else if(state == RACE_STATE_END_GAME){
			//				//game is finished
			//				setState(STATE_GAME_OVER);
			//			}
		}
	}

	/**
	 * Pause the game or unpause if paused.
	 */
	public void pause(){
		if(mGameState == STATE_RUNNING){
			setState(STATE_PAUSE);
			mLastPauseTime = System.currentTimeMillis();
		}
	}

	public void unPause() {
		if(mGameState == STATE_PAUSE){
			setState(STATE_RUNNING);
			mGameView.setIsPausedOneDraw(false);
			long mLastUnpauseTime = System.currentTimeMillis();
			deltaPausedTime += (mLastUnpauseTime - mLastPauseTime);
			// = now - all the pause - posted time
			long trueElapsedTime = (mLastUnpauseTime) - deltaPausedTime ;

		}
	}

	public int getState() {
		return mGameState;
	}

	public void setState(int state) {
		if (mGameState != state) {
			mGameState = state;
			mReDraw = true;
		}
	}

	public void setRunning(boolean b) {
		mRun = b;
		if (mRun == false) {
			;
		}
	}

	public boolean isReDraw() {
		return mReDraw;
	}

	public void sendGameEvent(GameEvent gameEvent) {
		mEventQueue.add(gameEvent);
	}

	private void checkLoading() {
		if(mGameView.isReady()){
			setState(STATE_PLAY);
			mReDraw = false;
		}
		else {
			mReDraw = true;
		}
	}

	public boolean isInBackground() {
		return mIsInBackground;
	}

	public void setInBackground(boolean b) {
		mIsInBackground = b;
	}

	public boolean hasNeverBeenUsed() {
		return mHasNeverBeenUsed ;
	}

	public void setHasBeenUsed() {
		mHasNeverBeenUsed = false;		
	}

	public void setRedraw(boolean b) {
		mReDraw = b;
	}

}
