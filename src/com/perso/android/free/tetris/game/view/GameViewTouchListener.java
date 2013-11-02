package com.perso.android.free.tetris.game.view;

import com.perso.android.free.tetris.game.GameRunnable;
import com.perso.android.free.tetris.game.event.MoveDownEvent;
import com.perso.android.free.tetris.game.event.MoveLeftEvent;
import com.perso.android.free.tetris.game.event.MoveRightEvent;
import com.perso.android.free.tetris.game.event.RotateEvent;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * The touch listener for gameview.
 * Handle the swipe left, right down and simple touch.
 * @author ced
 *
 */
public class GameViewTouchListener implements OnTouchListener {
	
	private final GestureDetector mGestureDetector;
	private GameRunnable mGameRunnable;

	public GameViewTouchListener(Context context, GameRunnable run) {
		mGestureDetector = new GestureDetector(context, new GestureListener());
	}
	@Override
	public boolean onTouch(View v, MotionEvent motionEvent) {
		return mGestureDetector.onTouchEvent(motionEvent);
	}

	private final class GestureListener extends SimpleOnGestureListener {

		
		private static final int SWIPE_THRESHOLD = 100;
		private static final int SWIPE_VELOCITY_THRESHOLD = 100;
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			mGameRunnable.sendGameEvent(new RotateEvent());
			return super.onSingleTapUp(e);
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			boolean result = false;
			try {
				float diffY = e2.getY() - e1.getY();
				float diffX = e2.getX() - e1.getX();
				if (Math.abs(diffX) > Math.abs(diffY)) {
					if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
						if (diffX > 0) {
							onSwipeRight();
						} else {
							onSwipeLeft();
						}
					}
				} else {
					if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
						if (diffY > 0) {
							onSwipeBottom();
						} else {
							onSwipeTop();
						}
					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return result;
		}
	}

	public void onSwipeRight() {
		mGameRunnable.sendGameEvent(new MoveRightEvent());
	}

	public void onSwipeLeft() {
		mGameRunnable.sendGameEvent(new MoveLeftEvent());
	}

	public void onSwipeTop() {
	}

	public void onSwipeBottom() {
		mGameRunnable.sendGameEvent(new MoveDownEvent());
	}
}

