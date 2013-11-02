package com.perso.android.free.tetris;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.perso.android.free.tetris.game.GameActivity;

/**
 * Entrance activity.
 * 
 * @author ced
 *
 */
public class HomeActivity extends Activity implements View.OnClickListener{

	private static final String TAG = HomeActivity.class.getSimpleName();
	private Button mStartButton;

	private EditText mEditWidth, mEditHeight;

	/**
	 * Required method from parent class
	 * 
	 * @param savedInstanceState - The previous instance of this app
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_activity_layout);

		mStartButton = (Button)findViewById(R.id.homeStartButton);
		mStartButton.setOnClickListener(this);

		mEditWidth = (EditText)findViewById(R.id.homeEditTextWidth);
		mEditHeight = (EditText)findViewById(R.id.homeEditTextHeight);
		
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		 if(v == mStartButton){
			
			//start option activity
			intent = new Intent(this, GameActivity.class);
			intent.putExtra("boardWidth", Integer.parseInt(mEditWidth.getText().toString()));
			intent.putExtra("boardHeight", Integer.parseInt(mEditHeight.getText().toString()));
			Log.d(TAG, "input : w = "+mEditWidth.getText().toString()+""+mEditHeight.getText().toString());
			startActivity(intent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}

