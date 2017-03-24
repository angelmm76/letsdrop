package alphalfa.android.letsdrop;

import alphalfa.android.letsdrop.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private final String TAG = "Lets Drop - Main";
	private static final int REQUEST_PLAY = 0;
	private static final int REQUEST_GET = 1;
	private static final int START_DROPS = 50;
	private static final String HIGH_SCORE = "high_score";
	private static final String SOUND = "sound";
	private static final String DROPS_AVAILABLE = "Available Drops: ";
	
	private TextView mDropsText, mHighScoreText;
	private CheckBox mSoundCheckBox;
	
	private boolean mSound;
	private int mTotalDrops;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_layout);
		
		Log.i(TAG, "Set content");
		
	    Typeface font = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		
		mDropsText= (TextView) findViewById(R.id.av_drops);
		mDropsText.setTypeface(font);
		mTotalDrops = START_DROPS;
		
		mHighScoreText = (TextView) findViewById(R.id.highscore);
		mHighScoreText.setTypeface(font);
		
		mSoundCheckBox = (CheckBox) findViewById(R.id.checkbox_sound);
		// Set an OnClickListener on the CheckBox
		mSoundCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSound = mSoundCheckBox.isChecked();
				//mTotalDrops = mSound ? 50 :  9999;
			}
		});
		
		Button playButton = (Button) findViewById(R.id.button_play);
		playButton.setTypeface(font);
		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Toast.makeText(getApplicationContext(), "Play", Toast.LENGTH_LONG).show();
				Intent intent= new Intent(MainActivity.this, GameActivity.class);
				intent.putExtra("drops", mTotalDrops);
				intent.putExtra("sound", mSound);
				startActivityForResult(intent, REQUEST_PLAY);
			}
		});
		
		Button getButton = (Button) findViewById(R.id.button_get);
		getButton.setTypeface(font);
		getButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Toast.makeText(getApplicationContext(), "Buy drops", Toast.LENGTH_LONG).show();
				Intent intent= new Intent(MainActivity.this, GetDropsActivity.class);
	            startActivityForResult(intent, REQUEST_GET);
				
			}
		});
		
		ImageButton helpButton = (ImageButton) findViewById(R.id.info_button);
		helpButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Intent intent= new Intent(MainActivity.this, HelpActivity.class);
				startActivity(intent);
			}
		});
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode == RESULT_OK) {	
			switch (requestCode) {
				case REQUEST_GET:
					mTotalDrops = mTotalDrops + Integer.parseInt(data.getStringExtra(data.EXTRA_TEXT));
					break;
				case REQUEST_PLAY:
					int remainingDrops = Integer.parseInt(data.getStringExtra(data.EXTRA_TEXT));
					mTotalDrops = remainingDrops > START_DROPS ? remainingDrops : START_DROPS;
					break;
			}
			updateDropsText();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		final SharedPreferences prefs = getApplicationContext().getSharedPreferences("letsdrop", MODE_PRIVATE);
		mHighScoreText.setText("Highscore: " + prefs.getInt(HIGH_SCORE, 0));
		boolean prefSound = prefs.getBoolean(SOUND, false);
		mSound = prefSound;
		mSoundCheckBox.setChecked(prefSound);
		
		Log.i(TAG, "Resume Main. Highscore: " + prefs.getInt(HIGH_SCORE, 0));
		updateDropsText();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		final SharedPreferences prefs = getApplicationContext().getSharedPreferences("letsdrop", MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(SOUND, mSound);
		editor.commit();
	}
	
	public void updateDropsText(){
		mDropsText.setText(DROPS_AVAILABLE + mTotalDrops);
	}
}
