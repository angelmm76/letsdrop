package alphalfa.android.letsdrop;

import alphalfa.android.letsdrop.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GetDropsActivity extends Activity {

    private int mDropsGot;
	private final String TAG = "Lets Drop - Get";
	private static final int REQUEST_GET = 1;
	private static final int REQUEST_RAIN = 2;
	private static final int REQUEST_SMTH = 3;
	private static final int REQUEST_BUY = 4;
	private static final int REQUEST_MATH = 5;
	private static final int REQUEST_TAP = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.getdrops_layout);
        
        Log.i(TAG, "Get Drops");
		
		Button SmthButton = (Button) findViewById(R.id.button_smth);
		SmthButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Create an intent stating which Activity you would like to start
				Intent intent= new Intent(GetDropsActivity.this, SMTHActivity.class);
				// Launch the Activity using the intent
				startActivityForResult(intent, REQUEST_SMTH);
			}
		});
		
		Button RainButton = (Button) findViewById(R.id.button_rain);
		RainButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent= new Intent(GetDropsActivity.this, RainActivity.class);
				startActivityForResult(intent, REQUEST_RAIN);
			}
		});
		
		Button BuyButton = (Button) findViewById(R.id.button_buy);
		BuyButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Toast.makeText(getApplicationContext(), "Buy drops", Toast.LENGTH_LONG).show();
				Intent intent= new Intent(GetDropsActivity.this, TapActivity.class);
	            startActivityForResult(intent, REQUEST_BUY);
				
			}
		});
		
		Button MathButton = (Button) findViewById(R.id.button_math);
		MathButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "Item clicked");
				// Create an intent stating which Activity you would like to start
				Intent intent= new Intent(GetDropsActivity.this, MathActivity.class);
				// Launch the Activity using the intent
	            startActivityForResult(intent, REQUEST_MATH);
				//startActivity(intent);
				
			}
		});
		
		Button TapButton = (Button) findViewById(R.id.button_tap);
		TapButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "Item clicked");
				// Create an intent stating which Activity you would like to start
				Intent intent= new Intent(GetDropsActivity.this, TapActivity.class);
				// Launch the Activity using the intent
	            startActivityForResult(intent, REQUEST_TAP);
				//startActivity(intent);
				
			}
		});
		
	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode == RESULT_OK) {	
			switch (requestCode) {
				case REQUEST_MATH:
					mDropsGot = Integer.parseInt(data.getStringExtra(data.EXTRA_TEXT));
					break;
				case REQUEST_RAIN:
					mDropsGot = Integer.parseInt(data.getStringExtra(data.EXTRA_TEXT));
					break;
				case REQUEST_SMTH:
					mDropsGot = Integer.parseInt(data.getStringExtra(data.EXTRA_TEXT));
					break;
				case REQUEST_TAP:
					mDropsGot = Integer.parseInt(data.getStringExtra(data.EXTRA_TEXT));
					break;
				case REQUEST_BUY:
					mDropsGot = Integer.parseInt(data.getStringExtra(data.EXTRA_TEXT));
					break;
			}
			
			sendResultMain();
		}
	}
	
	public void sendResultMain() {
		super.onPause();
		// Save user provided input from the EditText field
		String sDrops = String.valueOf(mDropsGot);
	
		// Create a new intent and save the drops as an extra
		Intent getDropsIntent = new Intent(GetDropsActivity.this, MainActivity.class);
		getDropsIntent.putExtra(Intent.EXTRA_TEXT, sDrops);
	
		// Set Activity's result with result code RESULT_OK
		setResult(RESULT_OK, getDropsIntent);
	
		//Finish the Activity
		finish();
	}
}
