package com.perso.android.free.wordrace.game;

import com.perso.android.free.wordrace.game.view.GameView;

import android.content.Context;


/**
 * Game rules manages the state of game objects. 
 * @author ced
 *
 */
public class GameRules {

	private GameView mGameView;
	private GameRunnable mGameRunnable;
	private Context mContext;
	
	//TODO add the game object here
	
	public GameRules(Context c){
		mContext = c;
	}
	
	/**
	 * Here we load the level.
	 */
	public void initGame(){
		
	}
	
}
