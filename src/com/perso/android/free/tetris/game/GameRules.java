package com.perso.android.free.tetris.game;

import android.content.Context;

import com.perso.android.free.tetris.game.backend.Board;
import com.perso.android.free.tetris.game.backend.Piece;
import com.perso.android.free.tetris.game.view.GameView;


/**
 * Game rules manages the state of game objects. 
 * @author ced
 *
 */
public class GameRules {

	public static final int DEFAULT_BOARD_WIDTH = 18;
	public static final int DEFAULT_BOARD_HEIGHT = 10; 

	private GameView mGameView;
	private GameRunnable mGameRunnable;
	private Context mContext;

	//TODO add the game object here
	private Piece mCurrentPiece;

	public GameRules(Context c){
		mContext = c;
	}

	/**
	 * Here we load the board.
	 */
	public void initGame(){
		int w = ((GameActivity)mContext).getIntent().getIntExtra("boardWidth", -1);
		int h = ((GameActivity)mContext).getIntent().getIntExtra("boardWeight", -1);
		if(w <= 5 || h <= 5 ){
			Board.getInstance().init(DEFAULT_BOARD_WIDTH, DEFAULT_BOARD_HEIGHT);
		}
		else {
			Board.getInstance().init(w, h);
		}
	}

	public boolean isGameOver(){
		if(!canMoveDown() && mCurrentPiece.getY()==0){
			return true;
		}
		return false;
	}

	/**
	 * When piece has finished moving test gameover, 
	 * test if it destroys a line 
	 * and generate a new piece.
	 * 
	 * 
	 */
	public void onPieceFinishedMoving(){

		if(isGameOver()){
			//do stuff like send gameover event
		}
		else {
			//fix the piece on the board
			Board.getInstance().setPiece(mCurrentPiece);
			boolean [][] board = Board.getInstance().getBoard();
			boolean result;
			for(int j = 0; j<mCurrentPiece.getShapeHeightLength() ;j++){
				result = true;
				for(int i = 0; i<Board.getInstance().getWidthUnit() ;i++){
					result &= board[j+mCurrentPiece.getY()][i];
				}
				if(result){
					destroyLine(j+mCurrentPiece.getY());
				}
			}
			//generate new piece with a event
			
		}
	}

	/**
	 * move down all square above the line
	 * @param line
	 */
	private void destroyLine(int line){
		boolean [][] board = Board.getInstance().getBoard();
		for(int j = line; j>0 ;j--){
			for(int i = 0; i<Board.getInstance().getWidthUnit() ;i++){
				board[j][i]=board[j-1][i]; 
			}
		}
	}

	/**
	 * Test the collision in the give direction
	 * @param dx
	 * @param dy
	 * @return
	 */
	public boolean isCollision(int dx, int dy){
		boolean [][] board = Board.getInstance().getBoard();
		for(int j = 0; j<mCurrentPiece.getShapeHeightLength() ;j++){
			for(int i = 0; i<mCurrentPiece.getShapeWidthLength() ;i++){
				if(board[j+mCurrentPiece.getY()+dy][i+mCurrentPiece.getX()+dx]==mCurrentPiece.getShape()[j][i]){
					return true;
				}
			}
		}
		return false;
	}

	public void moveLeft(){
		if(mCurrentPiece.getX()>0 && !isCollision(-1,0)){
			mCurrentPiece.moveLeft();
		}
	}

	public void moveRight(){
		if(mCurrentPiece.getX()+mCurrentPiece.getShapeWidthLength() < Board.getInstance().getWidthUnit() && !isCollision(1,0)){
			mCurrentPiece.moveRight();
		}
	}

	public boolean canMoveDown(){
		if(mCurrentPiece.getY()+mCurrentPiece.getShapeHeightLength()< Board.getInstance().getHeightUnit() && !isCollision(0, 1)){
			return true;
		}
		return false;
	}

	public void moveDown(){
		if(canMoveDown()){
			mCurrentPiece.moveDown();
		}
	}

}
