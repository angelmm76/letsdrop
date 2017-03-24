package alphalfa.android.letsdrop;

import alphalfa.android.letsdrop.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class HelpActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_layout);
        
		ImageButton closeHelpButton = (ImageButton) findViewById(R.id.close_help_button);
		closeHelpButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Toast.makeText(getApplicationContext(), "Close help button", Toast.LENGTH_LONG).show();
				finish();
			}
		});
    }
    
    @Override
    public void onWindowFocusChanged (boolean hasFocus){
    	super.onWindowFocusChanged(hasFocus);
    	if(!hasFocus){
    		finish();
    	}
    }
}
