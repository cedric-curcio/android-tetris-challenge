package com.perso.android.free.tetris.game.backend;

import java.util.Random;

import android.graphics.Color;

/**
 * Generate the tetris pieces.
 * Since we use a enum, new piece can be designed here.
 * @author ced
 *
 */
public class PieceFactory {

	private static Random mRand = new Random();
	
	public static Piece getRandomPiece(){
		int which = mRand.nextInt(Pieces.values().length);
		return Pieces.values()[which].generatePiece();
	}

	enum Pieces {
		/**
		 * A is the square 2x2
		 * ++
		 * ++
		 */
		A {
			@Override
			public Piece generatePiece() {
				boolean tab[][] = {{true, true},{true,true}};
				return new Piece(tab, Color.RED);
			}
		},
		/**
		 * B is the H/V bar
		 * +
		 * +
		 * +
		 * +
		 */
		B {
			@Override
			public Piece generatePiece() {
				boolean tab[][] = {{true},{true},{true},{true}};
				return new Piece(tab, Color.BLUE);
			}
		},
		/**
		 * C is the L bar
		 * +
		 * +
		 * ++
		 */
		C {
			@Override
			public Piece generatePiece() {
				boolean tab[][] = {{true, false},{true, false},{true,true}};
				return new Piece(tab, Color.DKGRAY);
			}
		},
		/**
		 * D is the second version of L bar
		 *  +
		 *  +
		 * ++
		 */
		D {
			@Override
			public Piece generatePiece() {
				boolean tab[][] = {{false, true},{false, true},{true,true}};
				return new Piece(tab, Color.MAGENTA);
			}
		},
		/**
		 * E is the T bar
		 * +++
		 *  +
		 */
		E {
			@Override
			public Piece generatePiece() {
				boolean tab[][] = {{true, true, true},{false, true, false}};
				return new Piece(tab, Color.GREEN);
			}
		},
		/**
		 * F is the S bar
		 *  ++
		 * ++
		 */
		F {
			@Override
			public Piece generatePiece() {
				boolean tab[][] = {{false, true, true},{true, true, false}};
				return new Piece(tab, Color.YELLOW);
			}
		},
		/**
		 * G is the second version of S bar
		 * ++
		 *  ++
		 */
		G {
			@Override
			public Piece generatePiece() {
				boolean tab[][] = {{true, true, false},{false, true, true}};
				return new Piece(tab, Color.CYAN);
			}
		};
		public abstract Piece generatePiece();
	}

}
