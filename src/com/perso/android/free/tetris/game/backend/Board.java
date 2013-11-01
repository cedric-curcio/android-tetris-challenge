package com.perso.android.free.tetris.game.backend;

/**
 * 
 * The tetris mBoard.
 * 
 * @author ced
 *
 */
public class Board {

	private int mWidthUnit;
	private int mHeightUnit;
	private boolean mBoard [][];

	static private Board instance;

	static public Board getInstance(){
		if(instance == null){
			instance = new Board();
		}
		return instance;
	}

	/**
	 * Initialize the mBoard with the given size.
	 * 
	 * @param w
	 * @param h
	 */
	public void init(int w, int h){
		mWidthUnit = w;
		mHeightUnit = h;
		mBoard  = new boolean[h][];
		for (int i = 0; i<h ; i++){
			mBoard[i] = new boolean[w];
		}
	}
	
	/**
	 * Once a piece has stopped moving, set it on the board.
	 * @param p
	 */
	public void setPiece(Piece p){
		for(int j = 0; j<p.getShapeHeightLength() ;j++){
			for(int i = 0; i<p.getShapeWidthLength() ;i++){
				mBoard[j+p.getY()][i+p.getX()]=p.getShape()[j][i]; 
			}
		}
	}

	public int getWidthUnit() {
		return mWidthUnit;
	}

	public int getHeightUnit() {
		return mHeightUnit;
	}

	public boolean[][] getBoard() {
		return mBoard;
	}
	
	
	
}
