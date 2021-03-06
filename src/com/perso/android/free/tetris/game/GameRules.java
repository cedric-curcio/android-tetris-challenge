package com.perso.android.free.tetris.game;

import android.content.Context;

import com.perso.android.free.tetris.game.backend.Board;
import com.perso.android.free.tetris.game.backend.Piece;
import com.perso.android.free.tetris.game.backend.PieceFactory;
import com.perso.android.free.tetris.game.event.GameOverEvent;
import com.perso.android.free.tetris.game.event.GeneratePieceEvent;
import com.perso.android.free.tetris.game.view.GameView;


/**
 * Game rules manages the state of game objects. 
 * @author ced
 *
 */
public class GameRules {

	public static final int DEFAULT_BOARD_WIDTH = 10;
	public static final int DEFAULT_BOARD_HEIGHT = 18; 

	private GameView mGameView;
	private GameRunnable mGameRunnable;
	private Context mContext;

	//TODO add the game object here
	private Piece mCurrentPiece;

	public GameRules(Context c, GameRunnable run){
		mContext = c;
		mGameRunnable = run;
	}

	/**
	 * Here we load the board.
	 */
	public void initBoard(){
		int w = ((GameActivity)mContext).getIntent().getIntExtra("boardWidth", -1);
		int h = ((GameActivity)mContext).getIntent().getIntExtra("boardHeight", -1);
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
			mGameRunnable.sendGameEvent(new GameOverEvent());
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
			mGameRunnable.sendGameEvent(new GeneratePieceEvent());
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
				Board.getInstance().getColorBoard()[j][i]=Board.getInstance().getColorBoard()[j-1][i]; 
			}
		}
	}

	/**
	 * Test the collision in the give direction
	 * @param dx
	 * @param dy
	 * @return
	 */
	public boolean isCollision(Piece piece, int dx, int dy){
		boolean [][] board = Board.getInstance().getBoard();
		for(int j = 0; j<piece.getShapeHeightLength() ;j++){
			for(int i = 0; i<piece.getShapeWidthLength() ;i++){
				if(j+piece.getY()+dy < board.length && i+piece.getX()+dx<board[0].length){
					if(board[j+piece.getY()+dy][i+piece.getX()+dx]== true && true==piece.getShape()[j][i]){
						return true;
					}
				}
				else {
					return true;
				}
			}
		}
		return false;
	}

	public void moveLeft(){
		if(mCurrentPiece.getX()>0 && !isCollision(mCurrentPiece, -1, 0)){
			mCurrentPiece.moveLeft();
		}
	}

	public void moveRight(){
		if(mCurrentPiece.getX()+mCurrentPiece.getShapeWidthLength() < Board.getInstance().getWidthUnit() && !isCollision(mCurrentPiece, 1, 0)){
			mCurrentPiece.moveRight();
		}
	}

	public boolean canMoveDown(){
		if(mCurrentPiece.getY()+mCurrentPiece.getShapeHeightLength()< Board.getInstance().getHeightUnit() && !isCollision(mCurrentPiece, 0, 1)){
			return true;
		}
		return false;
	}

	public void moveDown(){
		if(canMoveDown()){
			mCurrentPiece.moveDown();
		}
	}

	public void rotate() {
		//make a copy, rotate it, test if there is a collision, if no collision we can rotate the real piece
		Piece p = new Piece(mCurrentPiece);
		p.rotatePiece();
		if(!isCollision(p, 0, 0)){

			while(p.getX()+p.getShapeWidthLength()> Board.getInstance().getWidthUnit()){
				p.moveLeft();
				mCurrentPiece.moveLeft();
			}
			
			mCurrentPiece.rotatePiece();

		}

	}

	public void generatePiece() {
		mCurrentPiece = PieceFactory.getRandomPiece();
	}

	public Piece getCurrentPiece() {
		return mCurrentPiece;
	}

}
