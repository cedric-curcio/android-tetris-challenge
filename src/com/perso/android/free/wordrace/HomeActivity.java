package com.perso.android.free.wordrace;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.perso.android.free.wordrace.engine.SoundManager;
import com.perso.android.free.wordrace.game.GameActivity;

public class HomeActivity extends Activity implements View.OnClickListener{

	private static final String TAG = HomeActivity.class.getSimpleName();
	// the info start button
	private ImageView mAboutInfoButton;
	// the exhibition button
	private Button mStartButton;
	// the history button
	private Button mHistoryButton;
	//the hidden about view
	private TextView mAboutView;


	/**
	 * Required method from parent class
	 * 
	 * @param savedInstanceState - The previous instance of this app
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.home_activity_layout);

		mStartButton = (Button)findViewById(R.id.homeStartButton);
		mStartButton.setOnClickListener(this);

		mAboutInfoButton = (ImageView)findViewById(R.id.homeInfoButton);
		mAboutInfoButton.setOnClickListener(this);
		mAboutView = (TextView) findViewById(R.id.homeAboutInfoTextId);
		mAboutView.setOnClickListener(this);
		
		SoundManager sm = SoundManager.getInstance();
		sm.init(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		if(v == mAboutInfoButton){
//			SoundManager.getInstance().playSound(R.raw.woodbathit_asbutton);
			mAboutView.setVisibility(View.VISIBLE);
		}
		else if(v == mAboutView){
			mAboutView.setVisibility(View.INVISIBLE);
		}
		else if(v == mStartButton){
			
			//start option activity
			intent = new Intent(this, GameActivity.class);
			startActivity(intent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}

