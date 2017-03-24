package alphalfa.android.letsdrop;

import alphalfa.android.letsdrop.R;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TapActivity extends Activity {

	private final String TAG = "Lets Drop - Tap";
	
	private TextView mTextSeconds, mTextScore, mTextDrops;
	private Button ansButton0, ansButton1, ansButton2, ansButton3;
	private View mTapPadView;
	
	@SuppressWarnings("unused")
	private static final int INIT_TIME_SECONDS = 60;
	private int mDrops = 0;
	
	private float mPadRadius;
	private float mPadXcenter, mPadYcenter;
	
	private GestureDetector mGestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tap_layout);
        Log.i(TAG,"Tap, onCreate");
        
        mTextDrops = (TextView) findViewById(R.id.text_drops);
           
        mTapPadView = (View) findViewById(R.id.tap_pad);
        
        setTapPad();
        
        int w = 200;
        final TapView tapView = new TapView(getApplicationContext(), w);
                
        setupGestureDetector();
        
		final Button StartButton = (Button) findViewById(R.id.tap_start_button);
		StartButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setupGestureDetector();
			}
		});
		
		final ImageButton OKButton = (ImageButton) findViewById(R.id.tap_OK_button);
		OKButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				sendResult();
			}
		});
		
    }
    
	// Set up GestureDetector
	private void setupGestureDetector() {
		Log.i(TAG, "Setup GestureDetector");

		mGestureDetector = new GestureDetector(this,
		new GestureDetector.SimpleOnGestureListener() {

			// If a single tap intersects a BubbleView, then pop the BubbleView
			// Otherwise, create a new BubbleView at the tap's location and add
			// it to mFrame. You can get all views from mFrame with ViewGroup.getChildAt()

			@Override
			public boolean onSingleTapConfirmed(MotionEvent event) {

				// TODO - Implement onSingleTapConfirmed actions.
				// You can get all Views in mFrame using the ViewGroup.getChildCount() method
				
				Log.i(TAG, "TAP " + event.getX() + " " + event.getY());
				
				// View TapPadView = (View) findViewById(R.id.tap_pad);
					
				if (tapInside(event.getX(), event.getY())){
					mDrops++;
					Log.i(TAG, "Tap on pad. Number of drops: " + mDrops);
					mTextDrops.setText(String.valueOf(mDrops));
					return true;
				}
				
				return true;
			}
		});
	}
    
	public boolean tapInside(float tapX, float tapY) {
		
		// Tap inside
		if (((tapX - mPadXcenter) * (tapX - mPadXcenter) + (tapY - mPadYcenter) * (tapY - mPadYcenter)) 
				< mPadRadius * mPadRadius) {
			Log.i(TAG,"Tap inside!");
			return true;
		}
		return false;		
	}
	
	public void sendResult() {
		super.onPause();
		
		String sBuy = String.valueOf(mDrops);
	
		// Create a new intent and save the city field as an extra
		Intent buyIntent = new Intent(TapActivity.this, MainActivity.class);
		buyIntent.putExtra(Intent.EXTRA_TEXT, sBuy);
	
		// Set Activity's result with result code RESULT_OK
		setResult(RESULT_OK, buyIntent);
	
		//Finish the Activity
		finish();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		//Delegate the touch to the gestureDetector
		return mGestureDetector.onTouchEvent(event);
		
	}
	
	public class TapView extends View {
		
		TapView(Context context, int width) {
			super(context);
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		    int size = 0;
		    int width = getMeasuredWidth();
		    int height = getMeasuredHeight();

		    if (width < height) {
		        size = height;
		    } else {
		        size = width;
		    }
		    setMeasuredDimension(size, size);
		}
		
	}
	
	public void setTapPad(){
		// Get Parent layout dimensions
		RelativeLayout parentLayout = (RelativeLayout) findViewById(R.id.parent_layout);
		int width = parentLayout.getWidth();
		int height = parentLayout.getHeight();
		Log.i(TAG, "Parent width, height: " + width + ", "+ height);
		
        // Get Pad original dimensions and coordinates
		int padding = mTapPadView.getPaddingTop();
		
		// Set new pad dimensions
		int minDimens = height < width ? height : width;
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mTapPadView.getLayoutParams();	
		params.height = minDimens;
		params.width = minDimens;
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		mTapPadView.setLayoutParams(params);
		
		//mTapPadView.getLayoutParams().height = minDimens;
		//mTapPadView.getLayoutParams().width = minDimens;
		
		mPadRadius = minDimens - padding;
		
		int[] coord = new int[]{0, 0};
		mTapPadView.getLocationInWindow(coord);
		Log.i(TAG, "Tap View-> Height/Width: " + minDimens + ". Coords " + coord[0] + "," + coord[1] + 
				". Radius: " + mPadRadius);
		
		mPadXcenter = coord[0] + mPadRadius;
		mPadYcenter = coord[1] + mPadRadius;
		
        // Getting the screen width
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int displayWidth = dm.widthPixels;
//        
//        float scaleX = (float)displayWidth/100f;
//        float scaleY = (float)displayWidth/100f;
//        Log.i(TAG,"Scale X,Y: " + scaleX + "," + scaleY);
//		
//        mTapPadView.setScaleX(scaleX);
//        mTapPadView.setScaleY(scaleY);
//        mTapPadView.setTop(coord[1] + (int)(scaleX * mPadRadius));
//        // mTapPadView.setLayoutParams(params);
		
	}
}
