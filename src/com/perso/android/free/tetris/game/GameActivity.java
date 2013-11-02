package com.perso.android.free.tetris.game;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.perso.android.free.tetris.R;
import com.perso.android.free.tetris.game.view.GameView;

/**
 * Game activity.
 * Here the game view will be created.
 * 
 * @author cedric
 *
 */
public class GameActivity extends Activity implements View.OnClickListener{
	private static final String TAG = GameActivity.class.getName();
	
	private GameRunnable mGameRunnable;
	private GameView mGameView;
	private Button mPauseButton ;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		setContentView(R.layout.game_activity_layout);

		mGameView = (GameView)findViewById(R.id.GameViewId);
		mGameRunnable = mGameView.getThread();

//		mPauseButton = (Button)findViewById(R.id.pauseButtonId);
//		mPauseButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == mPauseButton){
			if(mGameRunnable.getState() == GameRunnable.STATE_PAUSE){
				mGameRunnable.unPause();
				mPauseButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause));
			}
			else if(mGameView.getThread().getState() == GameRunnable.STATE_RUNNING){
				mGameRunnable.pause();
				mPauseButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.play));
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "asked to stop activity");
		mGameRunnable.clean();
		mGameRunnable.setInBackground(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "asked to destroy activity");
		mGameView.cleanUp();
		mGameRunnable.setInBackground(false);

	}

	
}
