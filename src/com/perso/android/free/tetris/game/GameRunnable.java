package com.perso.android.free.tetris.game;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.perso.android.free.tetris.game.event.GameEvent;
import com.perso.android.free.tetris.game.event.GameOverEvent;
import com.perso.android.free.tetris.game.event.GeneratePieceEvent;
import com.perso.android.free.tetris.game.event.MoveDownEvent;
import com.perso.android.free.tetris.game.event.MoveLeftEvent;
import com.perso.android.free.tetris.game.event.MoveRightEvent;
import com.perso.android.free.tetris.game.event.RotateEvent;
import com.perso.android.free.tetris.game.view.GameView;

/**
 * The game loop & behavior. 
 * We use a ConcurrentLinkedQueue<GameEvent> mEventQueue to store the events. 
 * And we process them every 33 ms.
 * @author cedric
 *
 */
public class GameRunnable implements Runnable {

	private static final String TAG = GameRunnable.class.getSimpleName();

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
	/** Handle the dropping piece*/
	private DroppingPieceRunnable mDroppingPieceRunnable;

	/** game loop delay*/
	private long mDelay = 33;
	
	/** dropping piece timer and task*/
	private Timer mTimer;;
	private TimerTask mDroppingPieceTask;

	private long mLastPauseTime;
	private long deltaPausedTime = 0;

	/** now time */
	private long mNowTime = 0;

	/** if we have to redraw */
	private boolean mReDraw = false;

	/** Queue for GameEvents */
	private ConcurrentLinkedQueue<GameEvent> mEventQueue = new ConcurrentLinkedQueue<GameEvent>();

	/** handle the game loop */
	private Handler mLoopHandler = new Handler();

	private Context mContext;
	private GameRules mGameRules;

	private boolean mIsInBackground = false;
	private boolean mHasNeverBeenUsed = true;

	public GameRunnable(Context c, GameView v, SurfaceHolder surfaceHolder){
		mContext = c;
		mGameView = v;
		mSurfaceHolder = surfaceHolder;
		mGameRules = new GameRules(c, this);
		mTimer = new Timer();
		setState(STATE_LOADING);
	}

	@Override
	public void run() {

		//update and post handler
		Canvas canvas = null;
		long sleepTime;
		if(mRun){
			mNowTime = System.currentTimeMillis();
			if(mGameState == STATE_LOADING){
				checkLoading();
			}
			else if(mGameState == STATE_PLAY){
				initGame();//first enter
			}
			else if (mGameState == STATE_RUNNING) {
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

	private void finishActivity(){
		Log.d(TAG, "method finishActivity");
		((GameActivity)mContext).finish();
	}

	private void initGame() {
		//clean all
		
		mEventQueue.clear();
		deltaPausedTime=0;
		mNowTime = 0;
		mGameRules.initBoard();
//		mDroppingPieceRunnable = new DroppingPieceRunnable(this, 500);
		
		 mDroppingPieceTask = new TimerTask() {
			@Override
			public void run() {
				sendGameEvent(new MoveDownEvent());
			}
		};
		mTimer.schedule(mDroppingPieceTask, 300, 500);
		sendGameEvent(new GeneratePieceEvent());
		//start the game
//		Thread t = new Thread(mDroppingPieceRunnable);
//		t.start();
		mReDraw = true;
		setState(STATE_RUNNING);
	}

	/**
	 * Update all events here
	 */
	protected void updateGameState() {
		synchronized (mSurfaceHolder) {
			if(mEventQueue.size()>0){
				mReDraw = true;
			}
			while(true){
				GameEvent event = mEventQueue.poll();
				if(event == null){
					break;
				}
				else if(event instanceof RotateEvent){
					mGameRules.rotate();
				}
				else if(event instanceof MoveLeftEvent){
					mGameRules.moveLeft();
				}
				else if(event instanceof MoveRightEvent){
					mGameRules.moveRight();
				}
				else if(event instanceof MoveDownEvent){
					if(mGameRules.getCurrentPiece() != null){
						if(mGameRules.canMoveDown()){
							mGameRules.moveDown();
						}
						else {
							mGameRules.onPieceFinishedMoving();
						}
					}
				}
				else if(event instanceof GeneratePieceEvent){
					mGameRules.generatePiece();
				}
				else if(event instanceof GameOverEvent){
					setState(STATE_GAME_OVER);
					mDroppingPieceTask.cancel();
					mTimer.purge();
				}
			}
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

	public GameRules getGameRules(){
		return mGameRules;
	}

}
