package com.perso.android.free.tetris.game;

import com.perso.android.free.tetris.game.event.MoveDownEvent;

/**
 * it's a periodic runnable that move down the current piece.
 * 
 * @author ced
 *
 */
public class DroppingPieceRunnable implements Runnable {

	private GameRunnable mGameRunnable;
	private int mMoveDownTime; //in milli
	private boolean mIsStopped;
	
	public DroppingPieceRunnable(GameRunnable run, int milli) {
		mGameRunnable = run;
		mMoveDownTime = milli;
	}
	
	@Override
	public void run() {
		
		while(true){
			if(mIsStopped){
				break;
			}
			if(mGameRunnable.getState() == mGameRunnable.STATE_RUNNING){
				mGameRunnable.sendGameEvent(new MoveDownEvent());
			}
			try {
				Thread.sleep(mMoveDownTime);
			} catch (InterruptedException e) {
			}
		}
	}

	public void stop(){
		mIsStopped = true;
	}
}
