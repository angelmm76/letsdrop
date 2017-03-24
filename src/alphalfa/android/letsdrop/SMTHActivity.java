package alphalfa.android.letsdrop;

import alphalfa.android.letsdrop.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SMTHActivity extends Activity implements
		SensorEventListener {

	// References to SensorManager and accelerometer
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	// Filtering constant
	private final float mAlpha = 0.7f;

	// Arrays for storing filtered values
	private float[] mGravity = new float[3];

	private TextView mPromptView, mHeightView, mDropsView;
	
	private Button mStartButton;

	private long mLastUpdate;
	private int mTries = 0;
	private long mStartTime, mStopTime;
	private float mFlyingTime, mHeight; //mRawTime,
	private double mGravityMagnitude;
	private int mSmthDrops = 0;
	private boolean onMeasure, onFly = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.smth_layout);

		//mGravityView = (TextView) findViewById(R.id.accel);
		mPromptView = (TextView) findViewById(R.id.prompt_view);
		mHeightView = (TextView) findViewById(R.id.height_view);
		mDropsView = (TextView) findViewById(R.id.smthdrops);

		// Get reference to SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		mStartTime =System.currentTimeMillis();

		// Get reference to Accelerometer
		if (null == (mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)))
			finish();

		mLastUpdate = System.currentTimeMillis();
		
		final ImageButton OKButton = (ImageButton) findViewById(R.id.smth_OK_button);
		OKButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				sendResult();
			}
		});
		
		mStartButton = (Button) findViewById(R.id.start_button);
		mStartButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTries < 5) {
					mTries++;
					mPromptView.setText("Drop...");
					mHeightView.setText("");
					onMeasure= true;
				}
				else {
					Toast.makeText(getApplicationContext(), "Five tries are enough. You have got " + mSmthDrops + " drops!",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	// Register listener
	@Override
	protected void onResume() {
		super.onResume();

		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);

	}

	// Unregister listener
	@Override
	protected void onPause() {
		super.onPause();

		mSensorManager.unregisterListener(this);
	}
	
	public void sendResult(){
		
		String sDrops = String.valueOf((int)(mSmthDrops));
		
		// Create a new intent and save the city field as an extra
		Intent smthIntent = new Intent(SMTHActivity.this, MainActivity.class);
		smthIntent.putExtra(Intent.EXTRA_TEXT, sDrops);
		
		// Set Activity's result with result code RESULT_OK
		setResult(RESULT_OK, smthIntent);
		
		//Finish the Activity
		finish();
	}
	
	// Process new reading
	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			long actualTime = System.currentTimeMillis();

			if (actualTime - mLastUpdate > 10) {

				mLastUpdate = actualTime;

				float rawX = event.values[0];
				float rawY = event.values[1];
				float rawZ = event.values[2];

				// Apply low-pass filter
				mGravity[0] = lowPass(rawX, mGravity[0]);
				mGravity[1] = lowPass(rawY, mGravity[1]);
				mGravity[2] = lowPass(rawZ, mGravity[2]);
				
				mGravityMagnitude = getMagnitude(mGravity);

				if (onMeasure){
					if (mGravityMagnitude < 5.0 && !onFly){
						startFlyMeasurement();
					} 
					if (mGravityMagnitude > 5.0 && onFly) {
						stopFlyMeasurement();
					}
				}
			}
		}
	}

	private void startFlyMeasurement(){
		
		onFly = true;
		mStartTime = System.currentTimeMillis();
	}
	
	private void stopFlyMeasurement(){
		
		onMeasure = false;
		onFly = false;
		mStopTime = System.currentTimeMillis();
		mFlyingTime = 0.001f * (mStopTime - mStartTime);
		mHeight = 100.0f * 0.5f * 9.81f * mFlyingTime * mFlyingTime;
		mPromptView.setText("Good!");
		//mHeightView.setText("Fly time: " + String.format("%.2f", mFlyingTime) + " seconds"
				//+ "\n" + "Height: " +  String.valueOf((int)mHeight) + " cm");
		mHeightView.setText("Height: " +  String.valueOf((int)mHeight) + " cm");
		mSmthDrops = mSmthDrops + (int)mHeight;
		mDropsView.setText("+ " + mSmthDrops);
		
		if (mTries >= 5) {
			//mStartButton.setEnabled(false);
			mStartButton.setTextColor(Color.GRAY);
		}

	}
	
	private float getMagnitude(float[] vector) {

		return (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
	}
	
	// Deemphasize transient forces
	private float lowPass(float current, float gravity) {

		return gravity * mAlpha + current * (1 - mAlpha);
	}

	// Deemphasize constant forces
	private float highPass(float current, float gravity) {

		return current - gravity;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// NA
	}
}