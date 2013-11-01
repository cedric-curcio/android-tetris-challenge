package com.perso.android.free.tetris.game.backend;

/**
 * A tetris piece.
 * 
 * @author ced
 *
 */
public class Piece {

	private int mX;
	private int mY;
	private boolean mShape[][];
	

	/**
	 * basic constructor for a piece, piece starts at (0;0)
	 * @param shape the shape of the piece;
	 */
	public Piece(boolean[][] shape){
		mX = 0;
		mY = 0;
		mShape = shape;
	}

	public Piece(Piece currentPiece) {
		mX = currentPiece.getX();
		mY = currentPiece.getY();
		mShape = currentPiece.getShape();
	}

	/**
	 * Rotate the piece 90° to the right
	 */
	public void rotatePiece(){
		//create temporary table
		boolean tmpShape[][] = new boolean[mShape[0].length][];
		for(int i=0 ; i< mShape[0].length;i++){
			tmpShape[i] = new boolean[mShape.length];
		}
		//fill it
		for(int j = 0 ; j<mShape.length ; j++){
			for(int i = 0 ; i < mShape[0].length; i++){
				tmpShape[j][i] = mShape[mShape.length - (i+1)][j];
			}
		}
		//set it as the member shape
		mShape = tmpShape;
	}
	
	public void moveDown(){
		mY++;
	}
	
	public void moveLeft(){
		mX--;
	}
	
	public void moveRight(){
		mY++;
	}
	

	public int getX() {
		return mX;
	}


	public int getY() {
		return mY;
	}


	public boolean[][] getShape() {
		return mShape;
	}
	
	public int getShapeWidthLength(){
		return mShape[0].length;
	}
	
	public int getShapeHeightLength(){
		return mShape.length;
	}
}
