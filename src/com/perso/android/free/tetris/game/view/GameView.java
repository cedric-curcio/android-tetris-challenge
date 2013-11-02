package com.perso.android.free.tetris.game.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

import com.perso.android.free.tetris.R;
import com.perso.android.free.tetris.game.GameActivity;
import com.perso.android.free.tetris.game.GameRunnable;
import com.perso.android.free.tetris.game.backend.Board;
import com.perso.android.free.tetris.game.backend.Piece;

/**
 * The game view where the field, 
 * messages and game objects are displayed.
 * 
 * @author cedric
 *
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback{

	private static final String TAG = GameView.class.getSimpleName();

	/** Holds the game mechanics */
	private GameRunnable mGameRunnable; 
	/** needed to stop and start game*/
	private Thread mGameThread;
	/** interface controlling the surface */
	private SurfaceHolder mSurfaceHolder;
	/** the context*/
	private Context mContext;
	/** the loading thread*/
	private Thread mLoadThread;

	/** the paints */
	private Paint mTeamAPaint = new Paint();
	private Paint mTeamBPaint = new Paint();
	private Paint clearPaint = new Paint();
	private Paint mGreyPaint = new Paint(); //when we pause, when game is over
	private Paint mMessagesPaint = new Paint();
	private Paint mNotificationBorderPaint = new Paint();
	private Paint mNotificationPaint = new Paint();
	private Paint mNotificationShadowPaint = new Paint();

	private Rect mBounds = new Rect();
	private Rect mSurfaceRect;

	/** Current height of the surface/canvas*/
	private int mCanvasHeight = 1;
	/** Current width of the surface/canvas*/
	private int mCanvasWidth = 1;

	private Rect[][] mRectTable;

	private boolean mIsReady = false; // loading is finished
	private boolean mIsPausedOneDraw = false;
	private boolean isXlarge = false;


	public GameView(final Context context, AttributeSet attrs) {
		super(context, attrs);
		//		Log.d(TAG, "SimpleTypeSpeedView under creation");
		// register our interest in hearing about changes to our surface
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mGameRunnable = new GameRunnable(context, this, mSurfaceHolder);
		mGameThread = new Thread(mGameRunnable);
		clearPaint.setColor(Color.BLACK);
		mContext = context;
		GameViewTouchListener touchListener = new GameViewTouchListener(context, mGameRunnable); 
		setOnTouchListener(touchListener);
		mLoadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				loadAll();
			}
			private void loadAll(){

				int w = ((GameActivity)mContext).getIntent().getIntExtra("boardWidth", -1);
				int h = ((GameActivity)mContext).getIntent().getIntExtra("boardWeight", -1);
				if(w <= 5 || h <= 5 ){
					w = 10;
					h= 18;
				}
				int tileSize1 = mCanvasHeight/h;
				int tileSize2 = mCanvasWidth/w;
				int tileSize  = tileSize1<tileSize2?tileSize2:tileSize1;
				mRectTable = new Rect[h][w];
				for(int j = 0 ; j<h ; j++){
					for(int i = 0 ; i<w ; i++){
						mRectTable[j][i] = new Rect();
						mRectTable[j][i].bottom = (j+1) * tileSize;
						mRectTable[j][i].top = j * tileSize;
						mRectTable[j][i].right = (i+1) * tileSize;
						mRectTable[j][i].left = i * tileSize;
					}
				}
				load(context.getResources());
				mIsReady = true;
			}
		});
		//		isXlarge = Boolean.parseBoolean(mContext.getString(R.string.isXlarge));
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// start the game thread here
		mGameRunnable.setRunning(true);
		if(!mGameThread.isAlive()){
			if(mGameRunnable.isInBackground()){
				mGameRunnable.setInBackground(false);
				mIsPausedOneDraw = false;
			}
			if(mGameRunnable.hasNeverBeenUsed()){
				mGameThread.start();
				mGameRunnable.setHasBeenUsed();
			}
		}
		Log.d(TAG, "Surface created, width = " + mCanvasWidth + ", height = " + mCanvasHeight);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		synchronized (mSurfaceHolder) {
			mCanvasWidth = width;
			mCanvasHeight = height;
		}
		if(mSurfaceRect == null){
			mSurfaceRect = new Rect();
		}
		mSurfaceRect.right = mCanvasWidth;
		mSurfaceRect.bottom = mCanvasHeight;



		DisplayMetrics metrics = new DisplayMetrics();
		((GameActivity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);

		Rect rectgle= new Rect();
		Window window= ((GameActivity)mContext).getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rectgle);

		Log.d(TAG, "metrics.heightPixels = " + metrics.heightPixels);
		Log.d(TAG, "Surface changed,  width = " + mCanvasWidth + ", height = " + mCanvasHeight);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mGameRunnable.pause();
		Log.d(TAG, "Surface destroyed");
	}

	public GameRunnable getThread() {
		return mGameRunnable;
	}

	public void setGameState(int statePause) {
		mGameRunnable.setState(statePause);
	}

	public int getCanvasWidth() {
		return mCanvasWidth;
	}

	public int getCanvasHeight() {
		return mCanvasHeight;
	}

	public void drawAll(Canvas canvas) {
		if(canvas == null){
			return;
		}
		synchronized (mSurfaceHolder) {// si normalement une seule thread dessine, c'est inutile
			int runnableState = mGameRunnable.getState();
			if(runnableState == GameRunnable.STATE_GAME_OVER ){
				drawGameOver(canvas);
				mGameRunnable.setRedraw(false);
			}
			else if(runnableState == GameRunnable.STATE_LOADING){
				drawLoading(canvas);
				final Resources res = mContext.getResources();
				if(mLoadThread.getState()==Thread.State.NEW){
					mLoadThread.start();
				}
			}
			else if(mGameRunnable.isReDraw() && !mIsPausedOneDraw ){
				//clear all
				canvas.drawRect(0, 0, mCanvasWidth, mCanvasHeight, clearPaint);

				//draw the board
				Board board = Board.getInstance();
				for(int j=0 ; j<board.getHeightUnit() ; j++){
					for(int i=0 ; i<board.getWidthUnit() ; i++){
						if(board.getBoard()[j][i]){
							canvas.drawRect(mRectTable[j][i], mTeamAPaint);
						}
					}
				}
				//draw the piece
				Piece p = mGameRunnable.getGameRules().getCurrentPiece();
				for(int j=0 ; j<p.getShapeHeightLength() ; j++){
					for(int i=0 ; i<p.getShapeWidthLength() ; i++){
						if(p.getShape()[j][i]){
							mTeamBPaint.setColor(p.getColor());
							canvas.drawRect(mRectTable[p.getY()+j][p.getX()+i], mTeamBPaint);
						}
					}
				}	
			}	
		}
	}


	//TODO clean if no text in box
	private float checkPaintSize(float maxWidth, String s, Paint paint) {
		float retSize = paint.getTextSize();
		if(10==Math.floor(retSize)){
			return retSize;
		}
		paint.getTextBounds(s, 0, s.length(), mBounds);
		int textW = mBounds.width();
		if(textW > maxWidth){
			paint.setTextSize(retSize-1);
			return checkPaintSize(maxWidth,s,paint);
		}
		return retSize;
	}


	//TODO clean if no text in box
	private void checkNameSize(String name, float maxWidth,
			Paint paint) {
		paint.getTextBounds(name, 0, name.length()-1, mBounds);
		float nameW = mBounds.width();
		if(nameW >= maxWidth){
			//cut the text
			float charW = nameW/name.length();
			int nbVisibleChar = (int) (maxWidth/charW);
			String newName = name.substring(0, nbVisibleChar);
			if(newName.equals(name)){
				return;
			}
			//			team.setDisplayName(newName);
			String nextName = "";//team.getDisplayName();
			checkNameSize(nextName, maxWidth,paint);
		}
	}

	private void drawGameOver(Canvas canvas){
		Log.d(TAG, "drawing game over");
		Resources res = mContext.getResources();

		//1 draw black layer 
		canvas.drawColor(mGreyPaint.getColor());

		//message gameover
		canvas.drawText("GAME OVER", mCanvasWidth/2, mCanvasHeight/2, mMessagesPaint);
	}

	private void drawLoading(Canvas canvas){
		Log.d(TAG, "drawing load");
		canvas.drawColor(clearPaint.getColor());
		String loadText = mContext.getString(R.string.loadText);
		Rect bounds = new Rect();
		Paint loadPaint = new Paint();
		loadPaint.setColor(Color.RED);
		loadPaint.setTextSize(40);
		loadPaint.setAntiAlias(true);
		loadPaint.getTextBounds(loadText, 0, loadText.length()-1, bounds);
		float looseW = bounds.width();
		canvas.drawText(loadText, mCanvasWidth/2 - looseW/2, mCanvasHeight/2, loadPaint);
	}

	/**
	 * Release memory (mostly bitmaps) and stop thread.
	 */
	public void cleanUp() {

		boolean retry = true;

		if(!mGameRunnable.isInBackground()){
			mGameRunnable.setRunning(false);
			while (retry) {
				try {
					mGameThread.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}
		}

	}

	private void load(Resources res){
		int mScoreTextSize = 10;//res.getDimension(R.dimen.scoreSize);

		mTeamAPaint.setColor(Color.RED);
		mTeamAPaint.setTextSize(mScoreTextSize);
		mTeamAPaint.setAntiAlias(true);
		mTeamBPaint.setColor(Color.BLUE);
		mTeamBPaint.setTextSize(mScoreTextSize);
		mTeamBPaint.setAntiAlias(true);

		//		mMessagesPaint.setColor(res.getColor(R.color.messageColor));
		mMessagesPaint.setTextSize(mScoreTextSize);
		mMessagesPaint.setAntiAlias(true);

		mNotificationBorderPaint.setAntiAlias(true);
		mNotificationBorderPaint.setStyle(Paint.Style.STROKE);
		mNotificationBorderPaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
		mNotificationBorderPaint.setStrokeWidth(1);

		mNotificationPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mNotificationPaint.setStyle(Paint.Style.FILL);
		mNotificationPaint.setColor(Color.argb(0xaf, 0x50, 0x51, 0x51));

		mNotificationShadowPaint.setAntiAlias(true);
		mNotificationShadowPaint.setStyle(Paint.Style.STROKE);
		mNotificationShadowPaint.setColor(Color.argb(0x0f, 0x00, 0x11, 0x11));
		mNotificationShadowPaint.setStrokeWidth(10);

		mGreyPaint.setStyle(Paint.Style.FILL);
		mGreyPaint.setColor(Color.argb(0xaf, 0x00, 0x11, 0x11));

		mSurfaceRect = new Rect(0,0,mCanvasWidth,mCanvasHeight);
		//load bitmap

	}

	/**
	 * Return true if all the bitmap have been loaded.
	 * @return
	 */
	public boolean isReady() {
		return mIsReady;
	}

	public void setIsPausedOneDraw(boolean b){
		mIsPausedOneDraw = b;
	}

}
