package com.perso.android.free.wordrace.game.view;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

import com.perso.android.free.wordrace.R;
import com.perso.android.free.wordrace.game.GameActivity;
import com.perso.android.free.wordrace.game.GameRunnable;

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
	private Paint teamAPaint = new Paint();
	private Paint teamBPaint = new Paint();
	private Paint clearPaint = new Paint();
	private Paint mGreyPaint = new Paint(); //when we pause, when game is over
	private Paint mMessagesPaint = new Paint();
	private Paint mNotificationBorderPaint = new Paint();
	private Paint mNotificationPaint = new Paint();
	private Paint mNotificationShadowPaint = new Paint();

	private RectF mNotificationRect = new RectF();
	private Rect mBounds = new Rect();
	private Rect mSurfaceRect;
	private RectF xlargeDestRect = new RectF();

	/** our bitmaps*/
	private Bitmap mFieldBitmap;
	private Bitmap mBaseBitmap;

	/** Current height of the surface/canvas*/
	private int mCanvasHeight = 1;
	/** Current width of the surface/canvas*/
	private int mCanvasWidth = 1;

	private ArrayList<String> mOtherMessageList;
	private Object messageLock = new Object();
	private boolean mIsMessageToDisplay = false;

	private boolean mIsReady = false; // loading is finished
	private boolean mIsPausedOneDraw = false;
	private boolean isXlarge = false;


	public GameView(final Context context, AttributeSet attrs) {
		super(context, attrs);
		//		Log.d(TAG, "SimpleTypeSpeedView under creation");
		// register our interest in hearing about changes to our surface
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mOtherMessageList = new ArrayList<String>();
		mGameRunnable = new GameRunnable(context, this, mSurfaceHolder);
		mGameThread = new Thread(mGameRunnable);
		clearPaint.setColor(Color.BLACK);
		mContext = context;
		mLoadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				loadAll();
			}
			private void loadAll(){
				load(context.getResources());
				if(checkAll()){
					mIsReady = true;
				}
				else{
					mIsReady = false;
					loadAll();
					Log.e(TAG, "Not all data was loaded - redo");
				}
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
			}
		}
	}

	//TODO clean if no tutorial 
	private void drawMessage(Canvas canvas, ArrayList<String> messages) {
		int nbLine = messages.size();
		mMessagesPaint.setTextSize(40);
		//draw top rect which will contains the message

		canvas.drawRect(mNotificationRect, mNotificationPaint);
		canvas.drawRect(mNotificationRect, mNotificationBorderPaint);
		canvas.drawRect(mNotificationRect, mNotificationShadowPaint);


		float paintSize = 100, tmpPaintSize;
		float maxWidth = mNotificationRect.width() - 4;
		for(String s:messages){
			tmpPaintSize = checkPaintSize( maxWidth,s, mMessagesPaint);
			if(tmpPaintSize<paintSize){
				paintSize = tmpPaintSize;
				mMessagesPaint.setTextSize(paintSize);
			}
		}
		//calculate text position
		mMessagesPaint.getTextBounds(messages.get(0), 0, messages.get(0).length(), mBounds);
		float textW;
		float textH = mBounds.height();

		float xText;
		float ySpan = (int) (((mNotificationRect.height() - 5 ) - textH*nbLine)/(nbLine+1));
		float yText = mNotificationRect.top ;
		for(String s:messages){
			mMessagesPaint.getTextBounds(s, 0, s.length(), mBounds);
			//			textH = mBounds.height();
			textW = mBounds.width();
			yText = yText + ySpan + textH;
			xText = (mNotificationRect.left +2) + (((mNotificationRect.right-2) -  (mNotificationRect.left +2)) - textW)/2;
			canvas.drawText(s, xText, yText, mMessagesPaint);

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
		if(mFieldBitmap!=null){
			mFieldBitmap.recycle();
		}

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

		teamAPaint.setColor(Color.RED);
		teamAPaint.setTextSize(mScoreTextSize);
		teamAPaint.setAntiAlias(true);
		teamBPaint.setColor(Color.BLUE);
		teamBPaint.setTextSize(mScoreTextSize);
		teamBPaint.setAntiAlias(true);

//		mMessagesPaint.setColor(res.getColor(R.color.messageColor));
		mMessagesPaint.setTextSize(mScoreTextSize);
		mMessagesPaint.setAntiAlias(true);

		mNotificationRect.top = mCanvasHeight/11;
		mNotificationRect.bottom = mNotificationRect.top*3;
		mNotificationRect.left = 	mCanvasWidth/5;
		mNotificationRect.right = mNotificationRect.left*4; 

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

	private boolean checkAll(){
		boolean isAllLoaded = true;
		if(mBaseBitmap == null || mFieldBitmap==null){
			isAllLoaded = false;
		}
		return isAllLoaded;
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

	
	/**
	 * Tutorial stuff remove it if no tutorial.
	 */
	
	/**
	 * Add tutorial message which whill be displayed in a box
	 * @param ss the messages to display.
	 */
	public void addMessageToDisplay(String ...ss){
		synchronized (messageLock) {
			for(String s:ss){
				mOtherMessageList.add(s);
			}
			mIsMessageToDisplay = true;
		}
	}

	public void stopShowingMessage(){
		synchronized (messageLock) {
			mIsMessageToDisplay = false;
			mOtherMessageList.clear();
		}
	}


	public void showZone(RectF rect){
		synchronized (messageLock) {
			Rect rectToShow = new Rect();
			rectToShow.top = (int) rect.top - 5;
			rectToShow.left = (int) rect.left - 5;
			rectToShow.bottom = (int) rect.bottom +5;
			rectToShow.right = (int) rect.right +5;
//			mRectToShowList.add(rectToShow);
//			mIsObjectToShow = true;
		}
	}
}
