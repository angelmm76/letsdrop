package alphalfa.android.letsdrop;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import alphalfa.android.letsdrop.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class GameActivity extends Activity {
	private GameLoop gameLoopThread;
	private int mDrops;
	private int mScore;
	private ArrayList<Item> mAlienItems;
	private ArrayList<Item> mDropItems;
	private Bitmap mDropBitmap;
	private Bitmap mYellowAliensBmps[] = new Bitmap[3];
	private Bitmap mOrangeAliensBmps[] = new Bitmap[3];
	private Bitmap mRedAliensBmps[] = new Bitmap[3];
	private Bitmap mGameOverBitmap;
	private Bitmap mPlayBitmap, mAlienBitmapBar, mDropBitmapBar;

	private GestureDetector mGestureDetector;
	private ScheduledExecutorService mAlienExecutor;
	
	private static final int REQUEST_GET = 1;
	private static final int DROP_TYPE = 0;
	private static final int DROP_VELOC = 10;
	private static final int ALIEN_VELOC = -2;
	private static final int DROP_ACCEL = 5;
	private static final String HIGH_SCORE = "high_score";

	private static final long ALIEN_CREATE_TIME = 600 * 1; // 0.6 seconds
	private static final String TAG = "Game LD";
	
	private static final int ITEM_ROW_NUMBER = 10;
	private static final float BAR_HEIGHT_R = 0.08f;
	
	private int mDisplayHeight;
	private int mDisplayWidth;
	private int mBarHeight;
	private int mBitmapSize;
	private int[][] mAlienPattern;
	private boolean mAlienPatternVert = true;
	private int mAlienPatternLength;
	private long mAlienLine = 0;
	
	private float mAlienVelocModifier = 0.0f;
	private int mAlienVelocModifierCounter = 4;
	
	private boolean mGameOver = false;
	private boolean mPaused = false;
	private boolean mSound;
	private boolean mRestart;
	
	private AudioManager mAudioManager;
	private SoundPool mSoundPool;
	private int mDropSoundID, mScoreSoundID;
	private float mStreamVolume;
	
	private BroadcastReceiver mScreenBroadcastReceiver;
	
	private Drawable mCanvasColor;
	private GradientDrawable mCanvasGradient;
	private int mGradientCounter = 0;
	private int mGradientColor;
		
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDrops = getIntent().getExtras().getInt("drops");		
		mSound = getIntent().getExtras().getBoolean("sound");
		Log.i(TAG, "Drops: " + mDrops + ", Sound: " + mSound);

		setContentView(new GameView(this));
		
		mBarHeight = (int) (mDisplayHeight * BAR_HEIGHT_R);
		mBitmapSize = mDisplayWidth / ITEM_ROW_NUMBER;
		
		mAlienItems = new ArrayList<Item>();
		mDropItems = new ArrayList<Item>();
		
		loadBitmaps();
		loadPattern();
		
		startAliensExecut();
		
		mCanvasColor = getApplicationContext().getResources().getDrawable(R.drawable.game_selector);
		mCanvasColor.setBounds(0, 0, mDisplayWidth, mDisplayHeight);
		
		mGradientColor = Color.rgb(60, 60, 140);//44, 51, 140);
		mCanvasGradient = new GradientDrawable(
  	  	        GradientDrawable.Orientation.TOP_BOTTOM,
  	  	        new int[] {mGradientColor,0xFF111111});
	  	mCanvasGradient.setBounds(0, 0, mDisplayWidth, mDisplayHeight);
	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG,"On Resume");
		
		setupGestureDetector();

		gamePaused(mPaused);
		
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenBroadcastReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	Log.i(TAG, intent.getAction());
		        if (intent.getAction().equals(Intent. ACTION_SCREEN_OFF)) {
		        	gamePaused(true);
		        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
		            gamePaused(mPaused);
		        }
		    }
		}, intentFilter);
		
		if (mSound) {
			mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

			// Maximum Volume
			mStreamVolume = (float) mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC)
					/ mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);


			mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					
					if (0 == status) {
						Log.i(TAG, "Sound loaded");
					} else {
						Log.i(TAG, "Unable to load sound");
						//finish();
					}
				}
			});
			
			// Load the sounds from res/raw/
			mDropSoundID = mSoundPool.load(this, R.raw.drop_pop, 1);
			mScoreSoundID = mSoundPool.load(this, R.raw.score, 1);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "On Pause");
		
		// Unregister receiver
        if(mScreenBroadcastReceiver!= null){
            unregisterReceiver(mScreenBroadcastReceiver);
            mScreenBroadcastReceiver = null;
        }
		
		if (mSound && null != mSoundPool) { 
			mSoundPool.unload(mDropSoundID);
			mSoundPool.unload(mScoreSoundID);
			mSoundPool.release();
			mSoundPool = null;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "On Destroy, Drops " + mDrops);
		mAlienExecutor.shutdown();
	}
	
	public class GameView extends SurfaceView {
	      
	    private SurfaceHolder mHolder;
	    
	    Paint paint = new Paint();
	        
		public GameView(Context context) {
	    	super(context);
	    	  
	    	gameLoopThread = new GameLoop(this);  // Thread
	    	mHolder = getHolder();

	        mHolder.addCallback(new SurfaceHolder.Callback() {
	        	
				// When game stops
				@Override
                public void surfaceDestroyed(SurfaceHolder holder) {
					gameLoopThread.setRunning(false);
					gameLoopThread.interrupt();
					mAlienExecutor.shutdown();
			        try {
						gameLoopThread.join();
			        } catch (InterruptedException e) {  
			        }
			        Log.i(TAG, "Surface Destroyed, thread " + gameLoopThread.getState());
	             }
                
				// Before drawing
				@SuppressLint("WrongCall")
				@Override
                public void surfaceCreated(SurfaceHolder holder) {
                	
                	Log.i(TAG, "Surface Created, thread " + gameLoopThread.getState());
                	if (gameLoopThread.getState() == Thread.State.NEW) {
                		gameLoopThread.setRunning(true);
                		gameLoopThread.start(); 
                	}
                	if (gameLoopThread.getState() == Thread.State.TERMINATED) {
                		// Game restarted after losing focus
                		gameLoopThread = new GameLoop(GameView.this); 
                		gameLoopThread.setRunning(true);
                		gameLoopThread.start();
                		mRestart = true;
                	}
                	Log.i(TAG, "Surface Created2, thread " + gameLoopThread.getState());
	             }
                
				@Override
                public void surfaceChanged(SurfaceHolder holder, int format,int width, int height) {
	            }
	        });
	        
	        //getting the screen size

	        DisplayMetrics dm = new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(dm);
	        mDisplayWidth = dm.widthPixels;
	        mDisplayHeight = dm.heightPixels;

			Log.i(TAG, "Height: " + mDisplayHeight + ", Width: " + mDisplayWidth);
		    	  
	      }
	      
	      // On touch method
	      
	      @Override
	      public boolean onTouchEvent(MotionEvent event) {
		  		
	    	  return mGestureDetector.onTouchEvent(event);
	      }
			
	      // What is drawn
	      
	      @Override
		  protected void onDraw(Canvas canvas) {
	    	  
	    	  Log.i(TAG, "Drawing");
	    	  
	    	  // New level
	    	  //if (mAlienLine / 5 > mAlienPatternLength - 10){ 
	    		//  mGradientCounter++;
	    		 // mGradientColor = Color.rgb(50 , 70, 140);
		  	  	  //mCanvasColor = new GradientDrawable(
			  	  	       // GradientDrawable.Orientation.TOP_BOTTOM,
			  	  	        //new int[] {Color.rgb(60, 60, mGradientCounter % 256),0xFF111111});
		  	  	//mCanvasColor.setBounds(0, 0, mDisplayWidth, mDisplayHeight);
	    	  //}

    	  	  if (canvas != null) mCanvasColor.draw(canvas);
    	  	  //canvas.drawColor(Color.DKGRAY);
    	  	  
    	  	  try {
    	  	  // Aliens
    	  	  for (Iterator<Item> iterator = mAlienItems.iterator(); iterator.hasNext();){
    	  		  Item alien = iterator.next(); //ConcurrentModificationException sometimes...??
    	  		  alien.move(alien.getVelocX(), alien.getVelocY());
    	  		  canvas.drawBitmap(bitmapAlienFromType(alien.getType()), alien.getX(), alien.getY(), null);//nullpoint
    	  		  
    	  		  if (alien.getX() < 0 || alien.getX() > mDisplayWidth - mBitmapSize) {
    	  			  alien.setVelocX(-alien.getVelocX());// Rebound
    	  		  }
    	  		  if (alien.getY() < 0 || alien.getY() > mDisplayHeight) {
    	  			  iterator.remove();
    	  		  }
    	  		  if (alien.getY() < 0.8f * mBitmapSize){
    	  			  Log.i(TAG,"Game Over");
    	  			  mGameOver = true;  	  			  
    	  		  }
    	  	  }
    	  	  
    	  	  //Drops
    	  	  for (Iterator<Item> iterator = mDropItems.iterator(); iterator.hasNext();){
    	  		  Item drop = iterator.next(); //concurentmodifexcep
    	  	  	  drop.move(0, drop.getVelocY());
    	  		  canvas.drawBitmap(mDropBitmap, drop.getX(), drop.getY(), paint);
    	  		  if (drop.getY() < 0 || drop.getY() > mDisplayHeight) {
    	  			  iterator.remove();
    	  		  }
    	  	  }
    	  	  
    	  	/* 	When you create an iterator, it starts to count the modifications that were applied
    	  	 *  on the collection. If the iterator detects that some modifications were made without
    	  	 *  using its method (or using another iterator on the same collection), it cannot guarantee 
    	  	 *  anymore that it will not pass twice on the same element or skip one, so it throws this
    	  	 *  exception. It means that you need to change your code so that you only remove items 
    	  	 *  via iterator.remove (and with only one iterator) OR
    	  	 *  make a list of items to remove then remove them after you finished to iterate. */
    	  	  
    	  	  // Intersections
    	  	  for (Iterator<Item> iterAlien = mAlienItems.iterator(); iterAlien.hasNext();){
    	  		  Item alien = iterAlien.next();
        	  	  for (Iterator<Item> iterDrop = mDropItems.iterator(); iterDrop.hasNext();){
        	  		  Item drop = iterDrop.next();
        	  		  if(intersects(alien, drop)){ 
        	  			  iterDrop.remove();
        	  			  if(alien.getType() > 2){
        	  				  alien.setType(alien.getType() - 3);
        	  			  }
        	  			  else{
        	  				  iterAlien.remove(); //ConcurrentModificationExcep , IllegalStateException
        	  			  }
        	  			  mScore++;
        	  			  if (mSound) {
        	  				mSoundPool.play(mScoreSoundID, mStreamVolume, mStreamVolume, 1, 0, 1.0f);  
        	  			  }
        	  		  }
        	  	  }
    	  	  }
    	  	  }catch (ConcurrentModificationException|IllegalStateException e) {
    	  		  Log.e(TAG, e.toString());
    	  	  }
    	  	  
    	  	  // Top Drop Bar
    	  	  paint.setAlpha((mDrops > 20) ? 255 : (255 * mDrops / 20));	 
    	  	  for (int i = -1; i < 2 * ITEM_ROW_NUMBER ; i++){
    	  		canvas.drawBitmap(mDropBitmap, i * mBitmapSize / 2, 10, paint);
    	  	  }
    	  	  
    	  	  // Bottom Rect
    	  	  paint.setColor(Color.BLACK);
    	  	  paint.setAlpha(200);
    	  	  canvas.drawRect(0, mDisplayHeight - mBarHeight,
    	  			  mDisplayWidth, mDisplayHeight, paint);
    	  	  paint.setAlpha(255);
    	  	  
    	  	  // Score
	    	  paint.setColor(Color.YELLOW);
	    	  paint.setAntiAlias(true);
	    	  paint.setFakeBoldText(true);
	    	  paint.setTextSize(30);
	    	  paint.setTextAlign(Align.CENTER);
	    	  canvas.drawText(" " + mScore, mBitmapSize + 40, mDisplayHeight - 20, paint);
		      canvas.drawBitmap(mAlienBitmapBar, 
		    		  10, mDisplayHeight - mBarHeight + 20, null);
	    	  
    	  	  // Get
	    	  paint.setColor(Color.CYAN);
    	  	  //canvas.drawRect(mDisplayWidth * 0.58f, mDisplayHeight - mBarHeight + 15, 
    	  			//mDisplayWidth * 0.58f + 80, mDisplayHeight - mBarHeight + 65, paint);
    	      //paint.setColor(Color.DKGRAY);
	    	  paint.setAntiAlias(true);
	    	  paint.setFakeBoldText(true);
	    	  paint.setTextSize(30);
	    	  paint.setTextAlign(Align.LEFT);
	    	  canvas.drawText("GET", mDisplayWidth * 0.60f, mDisplayHeight - 20, paint);
	    	  
	    	  // Pause - Play
	    	  canvas.drawBitmap(mPlayBitmap, mDisplayWidth * 0.4f - mBitmapSize / 2 , 
	    				  mDisplayHeight - mBarHeight + 10, null);
		  	  
		      // Drops
		      paint.setColor(Color.CYAN);
		      paint.setAntiAlias(true);
		      paint.setFakeBoldText(true);
		      paint.setTextSize(30);
		      paint.setTextAlign(Align.CENTER);
		      canvas.drawText(" " + mDrops, mDisplayWidth - 50, mDisplayHeight - 20, paint);
		      canvas.drawBitmap(mDropBitmapBar, 
		    		  mDisplayWidth - mBitmapSize - 70, mDisplayHeight - mBarHeight + 20, null);
		      
	  		  // Game Over
		      if(mGameOver) {
	    	  	  paint.setColor(Color.BLACK);
	    	  	  canvas.drawRect(0, mDisplayHeight / 2 - 150,
	    	  			  mDisplayWidth, mDisplayHeight / 2 + 150 , paint);
	    	  	  
		          //canvas.drawBitmap(mGameOverBitmap, 0, mDisplayHeight/2 - 100, null);
	    	  	  paint.setAlpha(128);
		    	  paint.setAntiAlias(true);
		    	  paint.setFakeBoldText(true);
		    	  paint.setTextAlign(Align.CENTER);
		    	  paint.setColor(Color.MAGENTA);
		    	  paint.setTextSize(80);
		    	  canvas.drawText("GAME OVER", mDisplayWidth / 2, mDisplayHeight / 2 - 30, paint);
		    	  paint.setTextSize(30);
		    	  paint.setColor(Color.GREEN);
		    	  canvas.drawText("Press << HERE >> to start a new game", mDisplayWidth / 2,
		    			  mDisplayHeight / 2 + 80, paint);

		    	  gamePaused(true);
	  			  
	  			  storeHighscore(mScore);
	  		  }
	  		  
	  		  if(mRestart){
	  			  // Draw one frame before check if restarted (GET)
	  			  Log.i(TAG, "Restarted, one frame. Paused: " + mPaused);
	  			  gamePaused(mPaused);
	  			  mRestart = false;
	  		  }
	      }
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		//Delegate the touch to the gestureDetector
		return mGestureDetector.onTouchEvent(event);
		
	}
	
	// Set up GestureDetector
	private void setupGestureDetector() {

		mGestureDetector = new GestureDetector(this,
		new GestureDetector.SimpleOnGestureListener() {

			// Fling gesture
			@Override
			public boolean onFling(MotionEvent event1, MotionEvent event2,
					float velocityX, float velocityY) {
				return true;
			}
			
			// Tap gesture
			@Override
			public boolean onDown(MotionEvent event) {
				Log.i(TAG, "Tap: " + event.getX() + ", " + event.getY());
				
				// Low bar "buttons"
				if (event.getY() > mDisplayHeight - mBarHeight){
					// Start Get Activity
					if(event.getX() > 0.55f * mDisplayWidth) {
						gameLoopThread.setPause(true);
						mAlienExecutor.shutdown();
						Intent intent= new Intent(GameActivity.this, GetDropsActivity.class);
			            startActivityForResult(intent, REQUEST_GET);
					}
					// Pause game
					else if (event.getX() > 0.3f * mDisplayWidth && event.getX() < 0.5f * mDisplayWidth){
						mPaused = !mPaused;
						gamePaused(mPaused);
						Log.i(TAG, "Pause: " + mPaused);
					}
				}
				
				if (!mPaused && !mGameOver) {
					// New drop
					if (event.getY() < 1.8f * mBitmapSize){
						if (mDrops > 0) {
							newDrop(event.getX() - mBitmapSize / 2, event.getY());
							mDrops--;
							if (mSound) {
								mSoundPool.play(mDropSoundID, mStreamVolume, mStreamVolume, 1, 0, 1.0f);
							}
						}
						if (mDrops == 0){
							Toast.makeText(getApplicationContext(), "You are run out of drops. Press GET",
									Toast.LENGTH_SHORT).show();
						}
					}
				}
					
				if (mGameOver){
					if (event.getY() > mDisplayHeight / 2 - 150 && 	event.getY() < mDisplayHeight / 2 + 150) {
						restartGame();
					}
				}

				return true;
			}
		});
	}
	
	public class Item {
		float mX;
		float mY;
		float mVelocX;
		float mVelocY;
		int mType;

		public Item(int type, float posX, float posY, float velocX, float velocY) {
			mX = posX;
			mY = posY;
			mVelocX = velocX;
			mVelocY = velocY;
			mType = type;
		}

		synchronized void move (float dx, float dy) {
			mX = mX + dx;
			mY = mY + dy;
		}
		
		synchronized void setType(int type) {
			mType = type;
		}
		
		synchronized int getType() {
			return mType;
		}
		
		synchronized void setX(float x) {
			mX = x;
		}
		
		synchronized void setY(float y) {
			mY = y;
		}
		
		synchronized float getX() {
			return mX;
		}
		
		synchronized float getY() {
			return mY;
		}
		
		synchronized float getVelocX() {
			return mVelocX;
		}
		
		synchronized float getVelocY() {
			return mVelocY;
		}
		
		synchronized void setVelocX(float vx) {
			mVelocX = vx;
		}
		
		synchronized void setVelocYY(float vy) {
			mVelocY = vy;
		}
		 
		@Override
		public String toString () {
			return "(" + mX + "," + mY + ")";
		}
	}
	
	public void startAliensExecut() {
		
		mAlienExecutor = Executors.newScheduledThreadPool(1);
		
		mAlienExecutor.scheduleWithFixedDelay(new Runnable() {
			
			int lineNumber;

			@Override
			public void run() {
				lineNumber = (int) mAlienLine / 5;
				Log.i(TAG, "Executor aliens (" + mAlienLine + ", " + lineNumber + ")");
				if(mAlienLine % 5 == 0) {
					for (int i = 0; i < 10; i++) {
						if(mAlienPattern[lineNumber][i] != -1){
							float vx = (mAlienPatternVert) ? 0 : ((float) (5 * (2 * Math.random() - 1) ));
							float vy = (mAlienPatternVert) ? 
									ALIEN_VELOC * (mAlienVelocModifier + 1) :
									ALIEN_VELOC * (mAlienVelocModifier + 1) * (float) (0.5 * Math.random() + 0.75 );
							Item alien = new Item(mAlienPattern[lineNumber][i], 
									i * mDisplayWidth / 10, mDisplayHeight, vx, vy);
							mAlienItems.add(alien);
						}
					}
					Log.i(TAG, "Number of aliens: " + mAlienItems.size());
				}
				mAlienLine++;
				
				// New cycle, change pattern, increase velocity 
				if (mAlienLine / 5 == mAlienPatternLength){
					Log.i(TAG, "AlienLine: " + mAlienLine);
					mAlienLine = 0;
					mAlienPatternVert = !mAlienPatternVert;
					mAlienVelocModifierCounter++;
					mAlienVelocModifier = mAlienVelocModifier + 1.0f / mAlienVelocModifierCounter;
					loadPattern();
				}
				
			}
		}, 0, ALIEN_CREATE_TIME, TimeUnit.MILLISECONDS);

	}
	
	public void newDrop(float x, float y) {
		Log.i(TAG, "New Drop");
		Item drop = new Item(DROP_TYPE, x, y, 0, DROP_VELOC);
		mDropItems.add(drop);
	}
	
	public boolean intersects(Item item1, Item item2) {
		
		if (Math.pow(item1.getX() - item2.getX(), 2) + Math.pow(item1.getY() - item2.getY(), 2) 
				< Math.pow(0.8f * mBitmapSize, 2)){
			Log.i(TAG, "Intersection");
			return true;
		}
		return false;
	}
	
	public void loadBitmaps() {
		Log.i(TAG, "Load Bitmaps");
	    mDropBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.drop_t_new4);//t);
		mYellowAliensBmps[0] = BitmapFactory.decodeResource(getResources(), R.drawable.alien1_t);
		mYellowAliensBmps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.alien2_t);
		mYellowAliensBmps[2] = BitmapFactory.decodeResource(getResources(), R.drawable.alien3_t);
		mOrangeAliensBmps[0] = BitmapFactory.decodeResource(getResources(), R.drawable.alien4_t);
		mOrangeAliensBmps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.alien5_t);
		mOrangeAliensBmps[2] = BitmapFactory.decodeResource(getResources(), R.drawable.alien6_t);
		mRedAliensBmps[0] = BitmapFactory.decodeResource(getResources(), R.drawable.alien7_t);
		mRedAliensBmps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.alien8_t);
		mRedAliensBmps[2] = BitmapFactory.decodeResource(getResources(), R.drawable.alien9_t);
		//mGameOverBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gameover3);
  	    mPlayBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.playpause_square);
		
		for (int i = 0; i < 3; i++){
			mYellowAliensBmps[i] = Bitmap.createScaledBitmap(mYellowAliensBmps[i],
					mBitmapSize, mBitmapSize, true);
			mOrangeAliensBmps[i] = Bitmap.createScaledBitmap(mOrangeAliensBmps[i],
					mBitmapSize, mBitmapSize, true);
			mRedAliensBmps[i] = Bitmap.createScaledBitmap(mRedAliensBmps[i],
					mBitmapSize, mBitmapSize, true);
		}
		
		mDropBitmap = Bitmap.createScaledBitmap(mDropBitmap, mBitmapSize, mBitmapSize, true);
	  	mPlayBitmap =  Bitmap.createScaledBitmap(mPlayBitmap,
						mBitmapSize, mBitmapSize, true);
	  	mAlienBitmapBar =Bitmap.createScaledBitmap(mYellowAliensBmps[0],
				7 * mBitmapSize / 10, 7 * mBitmapSize / 10, true);
	  	mDropBitmapBar = Bitmap.createScaledBitmap(mDropBitmap, 7* mBitmapSize / 10, 7 * mBitmapSize / 10, true);
		//mGameOverBitmap = Bitmap.createScaledBitmap(mGameOverBitmap, 
	  	// mDisplayWidth, mGameOverBitmap.getHeight() * mDisplayWidth / mGameOverBitmap.getWidth(), true);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode == RESULT_OK && requestCode == REQUEST_GET) {	
			Log.i(TAG, "Result for Get Drops");
			mDrops = mDrops + Integer.parseInt(data.getStringExtra(data.EXTRA_TEXT));

		}
	}
	
	@Override
	public void onBackPressed() {
		
		gamePaused(true);
		 
		Log.i(TAG,"Exit dialog");
	    new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
	        .setTitle("Really?")
	        .setMessage("Are you sure you want to exit?")
	        .setNegativeButton(android.R.string.no,
	        	new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, int id) {
						gamePaused(mPaused);
					}
	    		})
	    
	        .setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, int id) {

						// Get Stored High Score
						storeHighscore(mScore);
						sendResult();
						gameLoopThread.interrupt();
						GameActivity.super.onBackPressed();
	
					}
				}).create().show();
	}
	
	public void loadPattern(){
		
		TypedArray pattern = getResources().obtainTypedArray(R.array.alien_pattern_vert);//test);
		if (!mAlienPatternVert) {
			pattern = getResources().obtainTypedArray(R.array.alien_pattern_diag);
		}
		mAlienPatternLength = pattern.length();
		Log.i(TAG, "Alien pattern length: " + mAlienPatternLength);
		int xmlArray[][] = new int[mAlienPatternLength][];
		mAlienPattern = new int[mAlienPatternLength][ITEM_ROW_NUMBER];
		
		// Read xml
		for (int i = 0; i < mAlienPatternLength; ++i) {
		    int id = pattern.getResourceId(i, 0);
		    if (id > 0) {
		    	xmlArray[i] = getResources().getIntArray(id);
		    	Log.i(TAG, xmlArray[i].toString());
		    } else {
		        // something wrong with the XML
		    	Log.i(TAG, "Something wrong with the array XML");
		    }
		}
		pattern.recycle(); // Important!
        
		for (int i = 0; i < mAlienPatternLength; ++i) {
			// Yellow aliens
			for (int j = 0; j < xmlArray[i][0]; j++){
				mAlienPattern[i][j] = 0 + (int) (3 * Math.random());
			}
			// Orange aliens
			for (int j = xmlArray[i][0]; j < xmlArray[i][0] + xmlArray[i][1]; j++){
				mAlienPattern[i][j] = 3 + (int) (3 * Math.random());
			}
			// Red Aliens
			for (int j = xmlArray[i][0] + xmlArray[i][1]; j < xmlArray[i][0] + xmlArray[i][1] + xmlArray[i][2]; j++){
				mAlienPattern[i][j] = 6 + (int) (3 * Math.random());
			}
			// No Aliens
			for (int j = xmlArray[i][0] + xmlArray[i][1] + xmlArray[i][2]; j < 10; j++){
				mAlienPattern[i][j] = -1;
			}
			shuffle(mAlienPattern[i]);
		}
		
	}	
	
	public static void shuffle(int[] a)	{
	    int n = a.length;
	    for (int i = 0; i < n; i++) {
	        // between i and n-1
	        int r = i + (int) (Math.random() * (n-i));
	        int tmp = a[i];    // swap
	        a[i] = a[r];
	        a[r] = tmp;
	    }
	}
	
	public Bitmap bitmapAlienFromType (int type){
        Bitmap bitmap;   
        
	    switch (type){
	    case 0:
	    	bitmap = mYellowAliensBmps[0];
	    	break;
	    case 1:
	    	bitmap = mYellowAliensBmps[1];
	    	break;
	    case 2:
	    	bitmap = mYellowAliensBmps[2];
	    	break;
	    case 3:
	    	bitmap = mOrangeAliensBmps[0];
	    	break;
	    case 4:
	    	bitmap = mOrangeAliensBmps[1];
	    	break;
	    case 5:
	    	bitmap = mOrangeAliensBmps[2];
	    	break;
	    case 6:
	    	bitmap = mRedAliensBmps[0];
	    	break;
	    case 7:
	    	bitmap = mRedAliensBmps[1];
	    	break;
	    case 8:
	    	bitmap = mRedAliensBmps[2];
	    	break;
	    default:
	    	bitmap = null;
	    	break;
	    }
		return bitmap;
	}
	
	public void storeHighscore(int score){
		
		final SharedPreferences prefs = getApplicationContext().getSharedPreferences("letsdrop", MODE_PRIVATE);
		Log.i(TAG, "Score: " + mScore + ", Highscore: " + prefs.getInt(HIGH_SCORE, 0));
		if (mScore > prefs.getInt(HIGH_SCORE, 0)) {
			// Get and edit high score
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(HIGH_SCORE, mScore);
			editor.commit();
		}
	}
	
	public void restartGame(){
		mAlienItems.clear();
		mDropItems.clear();
		mScore = 0;
		mGameOver = false;
		mPaused = false;
		mRestart = false;
		mAlienLine = 0;
		mAlienVelocModifier = 0.0f;
		mAlienVelocModifierCounter = 4;
		mAlienPatternVert = true;
		loadPattern();
		gamePaused(false);
	}
	
	public void gamePaused(boolean paused){
		if (paused){
			gameLoopThread.setPause(true);
			mAlienExecutor.shutdown();
		}
		else {
			gameLoopThread.setPause(false);
			if (mAlienExecutor.isShutdown()) {
				startAliensExecut();
			}
		}
	}
	
	public void sendResult() {
		super.onPause();
		// Send remaining Drops
		Intent intent = new Intent(GameActivity.this, MainActivity.class);
		intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(mDrops));
		setResult(RESULT_OK, intent);

	}
}
