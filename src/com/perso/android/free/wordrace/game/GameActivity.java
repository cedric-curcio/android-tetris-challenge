package com.perso.android.free.wordrace.game;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.ads.AdView;
import com.perso.android.free.wordrace.R;
import com.perso.android.free.wordrace.game.view.GameView;

/**
 * Game activity.
 * Here the game view will be created.
 * 
 * @author cedric
 *
 */
public class GameActivity extends Activity implements View.OnClickListener{
	private static final String TAG = GameActivity.class.getName();
	//pub :
	private AdView adView;
	
	private GameRunnable mGameRunnable;
	private GameView mRaceView;
	private Button mPauseButton, mMatchOverButton ;

	private TextView mYouWinTextView, mYouLooseTextView, mChooseBonusTextView, mGameOverTextView;
	private TextView mCurrentScoreTextView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		setContentView(R.layout.game_activity_layout_ad_bottom);

		mRaceView = (GameView)findViewById(R.id.BaseBallViewId);
		mGameRunnable = mRaceView.getThread();

		mPauseButton = (Button)findViewById(R.id.pauseButtonId);
		mPauseButton.setOnClickListener(this);
		mMatchOverButton = (Button)findViewById(R.id.buttonMatchOver);
		mMatchOverButton.setOnClickListener(this);
		mYouWinTextView = (TextView)findViewById(R.id.matchYouWinTextViewId);
		mYouLooseTextView = (TextView)findViewById(R.id.matchYouLooseTextViewId);
		mGameOverTextView = (TextView)findViewById(R.id.matchGameOverTextViewId);
		mCurrentScoreTextView = (TextView)findViewById(R.id.gameRoundId);

		//pub :
		adView = (AdView)this.findViewById(R.id.adView);
		adView.setBackgroundColor(Color.BLACK);
	}

	@Override
	public void onClick(View v) {
		if(v == mPauseButton){
			if(mGameRunnable.getState() == GameRunnable.STATE_PAUSE){
				mGameRunnable.unPause();
				mPauseButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause));
			}
			else if(mRaceView.getThread().getState() == GameRunnable.STATE_RUNNING){
				mGameRunnable.pause();
				mPauseButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.play));
			}
		}
		else if(v == mMatchOverButton){
			finish();
		}
		
	}


	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	public void myRefresh() {
		//invalidate ui
		Log.d(TAG, "called refresh");
		mPauseButton.setVisibility(View.GONE);
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				findViewById(R.id.rootActivityId).requestLayout();
				mPauseButton.setVisibility(View.VISIBLE);
			}
		},1000);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "asked to stop activity");
		mGameRunnable.setInBackground(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "asked to destroy activity");
		mRaceView.cleanUp();
		mGameRunnable.setInBackground(false);

	}

	public void setTextWinner(){

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
//				Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.you_win_loose_anim);
//					SoundManager.getInstance().playSound(R.raw.ambient_noise_win_crowd_cheer);
					mYouWinTextView.setText(getString(R.string.matchTie));
					mYouWinTextView.setVisibility(View.VISIBLE);
//					mYouWinTextView.startAnimation(animation);
			}
		});
	}

	

	public void setCurrentRound(final int score){
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				mCurrentScoreTextView.setVisibility(View.VISIBLE);
				mCurrentScoreTextView.setText(getString(R.string.gameScoreTxt)+" "+score);
			}
		});

	}
}
