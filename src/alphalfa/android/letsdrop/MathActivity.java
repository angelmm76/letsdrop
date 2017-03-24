package alphalfa.android.letsdrop;

import java.util.Random;

import alphalfa.android.letsdrop.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MathActivity extends Activity {
	
	private final String TAG = "Lets Drop - Math";
	private Random mRandGenerator = new Random(System.currentTimeMillis());
	
	private TextView mTextHeader, mTextScore, mTextQuestion;
	private Button ansButton0, ansButton1, ansButton2, ansButton3;
	
	//private int mNumQuestions = 20;
	private static final int PENALTY = 0;
	private static final int CORRECT = 20;
	private int mCountQuestion;
	private int mCorrect = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.math_layout);
		
		mTextQuestion = (TextView) findViewById(R.id.text_math_question);
		mTextHeader = (TextView) findViewById(R.id.text_header);
		mTextScore = (TextView) findViewById(R.id.text_score);
		
		ansButton0 = (Button) findViewById(R.id.button0);
		ansButton1 = (Button) findViewById(R.id.button1);
		ansButton2 = (Button) findViewById(R.id.button2);
		ansButton3 = (Button) findViewById(R.id.button3);

		enableAnsButtons(true);
		newQuestion(getQuestionStrings());
		
		final ImageButton OKButton = (ImageButton) findViewById(R.id.math_OK_button);
		OKButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				sendResult();
			}
		});
		
		final ImageButton refreshButton = (ImageButton) findViewById(R.id.math_refresh_button);
		refreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				enableAnsButtons(true);
				newQuestion(getQuestionStrings());
			}
		});
	}

	private void newQuestion(String data[]) {
	
		mTextQuestion.setText(data[4]);
		
		int ran4 = getRandom(4);
		int ans = 0;

		String[] buttonLabels = {data[ran4], data[(ran4 + 1) % 4], data[(ran4 + 2) % 4], data[(ran4 + 3) % 4]};
		for (int i = 0; i < 4; i++){
			//buttonLabels[i] = data[(ran4 + i) % 4];
			if (((ran4 + i) % 4) == 0) {
				ans = i;
			}		
		}
			
		final int correctAnswer = ans;
		Log.i(TAG, "Correct answer: " + correctAnswer);
	
		mTextHeader.setText("");
		
		ansButton0.setText(buttonLabels[0]);
		ansButton0.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Toast.makeText(getApplicationContext(), "Very good " + mAnswer1, Toast.LENGTH_SHORT).show();
				mCountQuestion++;
				checkAnswer(correctAnswer == 0);
			}
		});
		
		ansButton1.setText(buttonLabels[1]);
		ansButton1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Toast.makeText(getApplicationContext(), "Very good " + mAnswer2, Toast.LENGTH_SHORT).show();
				mCountQuestion++;
				checkAnswer(correctAnswer == 1);
			}
		});
		
		ansButton2.setText(buttonLabels[2]);
		ansButton2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Toast.makeText(getApplicationContext(), "Very good " + mAnswer3, Toast.LENGTH_SHORT).show();
				mCountQuestion++;
				checkAnswer(correctAnswer == 2);
			}
		});
		
		ansButton3.setText(buttonLabels[3]);
		ansButton3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Toast.makeText(getApplicationContext(), "Very good " + mAnswer4, Toast.LENGTH_SHORT).show();
				mCountQuestion++;
				checkAnswer(correctAnswer == 3);
			}
		});
		
		mTextScore.setText("+ " + String.valueOf(mCorrect));// + " / " + String.valueOf(mCountQuestion));
		
	}
	
	public void sendResult() {

		// Save user provided input from the EditText field
		String sScore = String.valueOf(mCorrect);
	
		// Create a new intent and save the city field as an extra
		Intent mathIntent = new Intent(MathActivity.this, MainActivity.class);
		mathIntent.putExtra(Intent.EXTRA_TEXT, sScore);
	
		// Set Activity's result with result code RESULT_OK
		setResult(RESULT_OK, mathIntent);
	
		//Finish the Activity
		finish();
	}
	
	public String[] getQuestionStrings() {
		
		// New question = {result, incorrect1, incorrect2, incorrect3, questionString}
		int d1, d2, res = 0;
		int inc[] ={0, 0, 0};
		String question = "";
		
		int iOper = getRandom(4);
		
		switch (iOper) {
		// Add
		case 0:
			d1 = 2 + getRandom(68);
			d2 = 2 + getRandom(48);
			res = d1 + d2;
			do {
				inc[0] = res + getRandom(10) - getRandom(10);
				inc[1] = res + getRandom(10) - getRandom(10);
				inc[2] = res + getRandom(10) - getRandom(10);
			} while (!optionsOK(res, inc[0], inc[1],inc[2]));
			question = String.valueOf(d1) + " + " +  String.valueOf(d2);
			break;
		// Substract
		case 1:
			d1 = 4 + getRandom(56);
			d2 = 2 + getRandom(38);
			d1 = d1 + d2;
			res = d1 - d2;
			do {
				inc[0] = res + getRandom(7) - getRandom(7);
				inc[1] = res + getRandom(7) - getRandom(7);
				inc[2] = res + getRandom(7) - getRandom(7);
			} while (!optionsOK(res, inc[0], inc[1],inc[2]));
			question = String.valueOf(d1) + " - " +  String.valueOf(d2);			
			break;
		// Multiply
		case 2:
			d1 = 2 + getRandom(13);
			d2 = 2 + getRandom(8);
			res = d1 * d2;
			do {
				inc[0] = res + getRandom(7) - getRandom(7);
				inc[1] = res + getRandom(7) - getRandom(7);
				inc[2] = res + getRandom(7) - getRandom(7);
			} while (!optionsOK(res, inc[0], inc[1],inc[2]));
			question = String.valueOf(d1) + " x " +  String.valueOf(d2);
			break;
		// Divide
		case 3:
			d1 = 2 + getRandom(13);
			d2 = 2 + getRandom(8);
			d1 = d1 * d2;
			res = d1 / d2;
			do {
				inc[0] = res + getRandom(5) - getRandom(5);
				inc[1] = res + getRandom(5) - getRandom(5);
				inc[2] = res + getRandom(5) - getRandom(5);
			} while (!optionsOK(res, inc[0], inc[1],inc[2]));
			question = String.valueOf(d1) + " / " +  String.valueOf(d2);
			break;
		}

		String sOutput[] = {String.valueOf(res), String.valueOf(inc[0]),
				String.valueOf(inc[1]), String.valueOf(inc[2]), question};
		return sOutput;
	}
	
	public int getRandom(int maximum){
		
		// Get int numbers from 0 to maximum - 1
		int randomNum = mRandGenerator.nextInt(maximum);
		return randomNum;
	}
	
	public void checkAnswer(boolean answer){
		if (answer){
			mTextHeader.setText("YES!");
			mCorrect = mCorrect + CORRECT;
		}
		else{
			mTextHeader.setText("NO");
			mCorrect = mCorrect - PENALTY;	
		}
		mTextScore.setText("+ " + String.valueOf(mCorrect));
		enableAnsButtons(false);
	}
	
	public boolean optionsOK(int a, int b, int c, int d) {
		return (b > 1 && c > 1 && d > 1 && a != b && a != c && a != d && 
				b != c  && b != d && c != d);
	}
	
	public void enableAnsButtons(boolean enabled) {
		
		ansButton0.setEnabled(enabled);
		ansButton1.setEnabled(enabled);
		ansButton2.setEnabled(enabled);
		ansButton3.setEnabled(enabled);
	}
}

