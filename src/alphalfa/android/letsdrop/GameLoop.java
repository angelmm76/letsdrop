package alphalfa.android.letsdrop;

import alphalfa.android.letsdrop.GameActivity.GameView;
import alphalfa.android.letsdrop.GameActivity.Item;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;

public class GameLoop extends Thread {
	
	private GameView view;
	private static final long FPS = 24; // Frames per second
	private static final long ALIEN_RATE_PS = 1; // Aliens lines created per second
	private boolean running = false;
	private boolean isPaused;
	private static final String TAG = "GameLoop";
   
	public GameLoop(GameView view) {
         this.view = view;
	}

	public void setRunning(boolean run) {
         running = run;
	}
   
	public void setPause(boolean paused) {
	   synchronized (view.getHolder()) {
    	   isPaused = paused;
        }
	}
 
    @SuppressLint("WrongCall")
	@Override

	public void run() {
    	long ticksPS = 1000 / FPS;
	 	long startTime = 0;
	 	long sleepTime;
	    while (running) {
	    	//pause and resume
	    	if (isPaused) {
	    		try {
	    			this.sleep(50);
	            } 
	            catch (InterruptedException e) {
	            	e.printStackTrace();
	            }
			}
	        else {
	        	Canvas c = null;
	            startTime = System.currentTimeMillis();
	            try {
	            	// Lock canvas and draw
	            	c = view.getHolder().lockCanvas();  
	                synchronized (view.getHolder()) {
	                	view.onDraw(c); // NullPointerExc canvas = null ??
	                }
	             } catch (NullPointerException e) {
	            	 Log.e("GameLoop", "Canvas " + e.toString());
	             }
	             
	             finally {
	            	 // Unlock canvas and update display
	            	 if (c != null) {
	            		 view.getHolder().unlockCanvasAndPost(c);
	                 }
	             }
	         }
	    	 sleepTime = ticksPS - (System.currentTimeMillis() - startTime); 

	         try {
	        	 if (sleepTime > 0)
	        		 sleep(sleepTime);
	             else
	                 sleep(ticksPS / 10); // ????????
	         } 
	         catch (Exception e) {}
	         
	    }
	}
    
}  