package alphalfa.android.letsdrop;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.json.JSONObject;

import alphalfa.android.letsdrop.R;
import alphalfa.android.letsdrop.LocationUtils.GetAddressTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class RainActivity extends Activity {
	
	private TextView cityField;
	private TextView updatedField;
	private TextView detailsField;
	private TextView rainingField;
	private TextView weatherIcon;
	private TextView tRainDrops;
	
	private ProgressDialog mProgress;	
	
	private static final int STORM_N = 400;
	private static final int HEAVY_N = 200;
	private static final int MODERATE_N = 100;
	private static final int LIGHT_N = 50;
		
	private static final String TAG = "Lets Drop-Rain";
	private static final String STORM = "STORM";
	private static final String HEAVY_RAIN = "HEAVY RAIN";
	private static final String MODERATE_RAIN = "MODERATE RAIN";
	private static final String LIGHT_RAIN = "LIGHT RAIN";
	private static final String RAIN = "RAIN";
	private static final String DRIZZLE = "DRIZZLE";
	private static final String RAIN_QUESTION = "Is it raining? ";

	// Reference to the LocationManager and LocationListener
	private String mCity;
	private static double mLatit, mLongit;
	private String mCheckedCities = "";
	private String mCitiesList[];
	private int mRainDrops;
	private boolean mUserLocated = false;
		    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rain_layout);
		Log.i(TAG, "Create RainActivity");
		
        cityField = (TextView) findViewById(R.id.city);
        updatedField = (TextView) findViewById(R.id.updated);
        detailsField = (TextView) findViewById(R.id.details);
        rainingField = (TextView) findViewById(R.id.raining);
        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        tRainDrops = (TextView) findViewById(R.id.raindrops);
        
        // TypedArray citiesArray = getResources().obtainTypedArray(R.array.cities_array);
        // shuffle(citiesArray);
        
        mCitiesList = getResources().getStringArray(R.array.cities);
        shuffle(mCitiesList);
        
	    Typeface font = Typeface.createFromAsset(getAssets(), "weather.ttf");
	    weatherIcon.setTypeface(font);
        
		final ImageButton OKButton = (ImageButton) findViewById(R.id.rain_OK_button);
		OKButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendResult();
			}
		});
		
		// Select city from list
		final Button selectButton = (Button) findViewById(R.id.rain_select);
		selectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mUserLocated = false;
				//Instantiating the DialogFragment class
                AlertDialogRadio alert = new AlertDialogRadio();

                //Creating the dialog fragment object, which will in turn open the alert dialog window
                alert.show(getFragmentManager(), "alert_dialog_radio");
 
			}
		});
		
//		final Button randomButton = (Button) findViewById(R.id.rain_random);
//		randomButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				mCity = mCitiesList[getRandom(mCitiesList.length)];
//				updateWeatherData(mCity);
//			}
//		});
		
		// Obtain city from user location
		final Button mylocButton = (Button) findViewById(R.id.rain_mylocation);
		mylocButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Toast.makeText(getApplicationContext(),"Getting your location...", Toast.LENGTH_SHORT).show();
				Location location =  LocationUtils.GetLocation(getApplicationContext());

				if (location == null) {
					Toast.makeText(getApplicationContext(),"No location available", Toast.LENGTH_LONG).show();
				}
				else {
					mUserLocated = true;
					mLatit = location.getLatitude();
					mLongit = location.getLongitude();
		     	    try {
		     	    	LocationUtils loc = new LocationUtils();
			     	    GetAddressTask task = loc.new GetAddressTask(getApplicationContext());
			     	    task.execute(location);
			     	    mCity = task.get();
			     	    Log.i(TAG, "Address city: " + mCity);
						updateWeatherData(mCity);
			     	    
		     	    } catch (ExecutionException e1) {
		     	    	Log.e("GetAddress", "ExecutionException in AsyncTask");
		     	    	e1.printStackTrace();
		     	    } catch (InterruptedException e2) {
		     	    	Log.e("GetAddress", "InterruptedException in AsyncTask");
		 	     	    	e2.printStackTrace();
		     	    }
				}
			}
		});
        
	}
	
	private void updateWeatherData(final String city){ 

	    if (mCheckedCities.contains(city)){
	    	Toast.makeText(getApplicationContext(), city + " is already checked",
	    			Toast.LENGTH_LONG).show();
	    }
	    else {
		    mCheckedCities = mCheckedCities + " " + city;
		    Log.i(TAG, "Checked cities: " + mCheckedCities);
			
			mProgress = new ProgressDialog(RainActivity.this, ProgressDialog.THEME_HOLO_DARK);
			//mProgress.setTitle("hey!");
			mProgress.setMessage("Retrieving weather data... ");
		    mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		    mProgress.setIndeterminate(true);
		    mProgress.setCancelable(false);
		    mProgress.show();    
		      	
			Log.i(TAG, "Update Weather Data");
		    new Thread(){
		    	
		    	Handler mHandler = new Handler();
		    	public void run(){
		    		if (!mUserLocated) {
		    			getLatLongOfCitiesList(city);
		    		}
		    		
		    		// final JSONObject json = RemoteFetch.getJSON(RainActivity.this, city);
		    		final JSONObject json = RemoteFetch.getJSON(RainActivity.this, mLatit, mLongit);
		            mProgress.dismiss();
		            		
		            if(json == null){
		            	Log.i(TAG, "Place not found");
		            	mHandler.post(new Runnable(){
		                    public void run(){
		                    	Toast.makeText(getApplicationContext(),"Weather data not found",
		                    			Toast.LENGTH_LONG).show();  
		                    }
		                });
		            } else {
		            	Log.i(TAG, "Place found");
		            	mHandler.post(new Runnable(){
		                    public void run(){
		                        renderWeather(json);    
		                    }
		                });
		            }               
		        }
		    }.start();
	    }
	}
	
	private void renderWeather(JSONObject json){
	    try {
	    	Log.i(TAG, "Render Weather");
	    	cityField.setText(mCity);
//	    	cityField.setText(json.getString("name").toUpperCase(Locale.getDefault()) + ", " + 
//	                json.getJSONObject("sys").getString("country"));
	    	
	    	JSONObject details = json.getJSONObject("currently");
//	        JSONObject details = json.getJSONArray("weather").getJSONObject(0);
//	        JSONObject main = json.getJSONObject("main");
//	        
	        String description = details.getString("summary").toUpperCase(Locale.getDefault());
	        String temp = String.format("%.1f", (details.getDouble("temperature") - 32) / 1.8);
	        String pres = String.format("%.1f", details.getDouble("pressure"));
	        
	        detailsField.setText(description + "\n" + "Temperature: " + temp + " C" + 
	        		"\n" + "Pressure: " + pres + " hPa");
	        
	        if (description.contains(STORM)){
	        	mRainDrops = mRainDrops + STORM_N;
	        	rainingField.setText(RAIN_QUESTION + "YES!");
	        }
	        else if (description.contains(HEAVY_RAIN)) {
	        	mRainDrops = mRainDrops + HEAVY_N;
	        	rainingField.setText(RAIN_QUESTION + "YES!");
	        }
	        else if (description.contains(LIGHT_RAIN)) {
	        	mRainDrops = mRainDrops + LIGHT_N;
	        	rainingField.setText(RAIN_QUESTION + "YES!");
	        }
	        else if (description.contains(MODERATE_RAIN) || description.contains(RAIN) 
	        		|| description.contains(DRIZZLE)) {
	        	mRainDrops = mRainDrops + MODERATE_N;
	        	rainingField.setText(RAIN_QUESTION + "YES!");
	        }
	        else{
	        	rainingField.setText(RAIN_QUESTION + "NO...");
	        }
	 
	        DateFormat df = DateFormat.getDateTimeInstance();
	        String updatedOn = df.format(new Date(details.getLong("time")*1000));
	        updatedField.setText("Last update: " + updatedOn);
	        tRainDrops.setText("+ " + mRainDrops);
	 
	        setWeatherIcon(details.getString("icon"));
	         
	    }catch(Exception e){
	        Log.e(TAG, "One or more fields not found in the JSON data");
	    }
	}
	
	private void setWeatherIcon(String iconText){
	    String icon = "";
        switch(iconText) {
        	case "clear-day": icon = getString(R.string.weather_sunny);
					  break;
        	case "clear-night": icon = getString(R.string.weather_clear_night);
	  				  break;			  
	        case "thunderstorm" : icon = getString(R.string.weather_thunder);
	                 break;         
//	        case 3 : icon = getString(R.string.weather_drizzle);
//	                 break;     
	        case "fog" : icon = getString(R.string.weather_foggy);
	                 break;
	        case "cloudy" : icon = getString(R.string.weather_cloudy);
	                 break;
	        case "partly-cloudy-day" : icon = getString(R.string.weather_cloudy);
            		 break;
	        case "partly-cloudy-night" : icon = getString(R.string.weather_cloudy);
            		 break;
	        case "overcast" : icon = getString(R.string.weather_cloudy);
                     break;
	        case "snow" : icon = getString(R.string.weather_snowy);
	                 break;
	        case "rain" : icon = getString(R.string.weather_rainy);
                      break;
	        default : icon = getString(R.string.weather_sunny);;
        		 	  break;
        	}
	    
	    weatherIcon.setText(icon);
	}
	
	public void sendResult() {
		super.onPause();
		// Save user provided input from the EditText field
		String sRain = String.valueOf(mRainDrops); //mRaindrops + " " + cityField.getText().toString();
	
		// Create a new intent and save the city field as an extra
		Intent rainIntent = new Intent(RainActivity.this, MainActivity.class);
		rainIntent.putExtra(Intent.EXTRA_TEXT, sRain);
	
		// Set Activity's result with result code RESULT_OK
		setResult(RESULT_OK, rainIntent);
	
		//Finish the Activity
		finish();
	}
	
	public class AlertDialogRadio extends DialogFragment{
		 
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Log.i(TAG, "onCreateDialog");
		    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    // Set the dialog title
		    //builder.setTitle(R.string.select_city)
		    // Specify the list array, the items to be selected by default (null for none),
		    // and the listener through which to receive callbacks when items are selected
		    builder.setSingleChoiceItems(mCitiesList, -1, //R.array.cities, -1,
		                      new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int item) {
									mCity = mCitiesList[item];
									Log.i(TAG, "City selected (dialog): " + mCity);
									updateWeatherData(mCity);
									dismiss();				                   
				               }
		           });

		    return builder.create();
		}
	}
	
	public int getRandom(int maximum){
		// Get int numbers from 0 to maximum - 1
		Random rand = new Random();
		int randomNum = rand.nextInt(maximum);
		return randomNum;
   }
	
	public static void shuffle(String[] a)	{
	    int n = a.length;
	    for (int i = 0; i < n; i++) {
	        // between i and n-1
	        int r = i + (int) (Math.random() * (n-i));
	        String tmp = a[i];    // swap
	        a[i] = a[r];
	        a[r] = tmp;
	    }
	}
	
	public static void getLatLongOfCitiesList(String city) {
        switch(city){
    	case "Mumbai, IN": mLatit = 19.075984; mLongit= 72.877656; break;
    	case "Manaus, BR": mLatit = -3.119028; mLongit= -60.021731; break;
    	case "Tampa, US": mLatit = 27.950575; mLongit= -82.457178; break;
    	case "Guangzhou, CN": mLatit = 23.129110; mLongit= 113.264385; break;
    	case "Baguio, PH": mLatit = 16.402333; mLongit= 120.596007; break;
    	case "Shiraz, IR": mLatit = 29.591768; mLongit= 52.583698; break;
    	case "Akita, JP": mLatit = 39.720008; mLongit= 140.102564; break;
    	case "Turku, FI": mLatit = 60.451813; mLongit= 22.266630; break;
    	case "Alexandria, EG": mLatit = 31.200092; mLongit= 31.200092; break;
    	case "San Jose, CR": mLatit = 9.928069; mLongit= -84.090725; break;
    	case "Lubumbashi, CD": mLatit = -11.687603; mLongit= 27.502617; break;
    	case "Hannover, DE": mLatit = 52.375892; mLongit= 9.732010; break;
    	case "Adelaide, AU": mLatit = -34.928621; mLongit= 138.599959; break;
    	case "Curitiba, BR": mLatit = -25.428954; mLongit= -49.267137; break;
    	case "Krakow, PL": mLatit = 50.064650; mLongit= 19.944980; break;
    	case "Charlotte, US": mLatit = 35.227087; mLongit= -80.843127; break;
    	case "Chittagong, BD": mLatit = 22.347537; mLongit= 91.812332; break;
    	case "Izmir, TR": mLatit = 38.423734; mLongit= 27.142826; break;
    	case "Barquisimeto, VE": mLatit = 10.067772; mLongit= -69.347351; break;
    	case "Chongqing, CN": mLatit = 29.563010; mLongit= 106.551557; break;
    	case "Los Angeles, US": mLatit = 34.052234; mLongit= -118.243685; break;
    	case "Devprayag, IN": mLatit = 30.145947; mLongit= 78.599293; break;
    	case "London, GB": mLatit = 51.507351; mLongit= -0.127758; break;
    	case "Tianjin, CN": mLatit = 39.084158; mLongit= 117.200983; break;
    	case "Santander, ES": mLatit = 43.462306; mLongit= -3.809980; break;
    	case "Singapore, SG": mLatit = 1.355379; mLongit= 103.867744; break;
    	case "Quito, EC": mLatit = -0.180653; mLongit= -78.467838; break;
    	case "Sao Paulo, BR": mLatit = -23.550520; mLongit= -46.633309; break;
    	case "Jakarta, ID": mLatit = -6.208763; mLongit= 106.845599; break;
    	case "Lagos, NG": mLatit = 6.524379; mLongit= 3.379206; break;
    	case "Irkutsk, RU": mLatit = 52.286974; mLongit= 104.305018; break;
    	case "Vancouver, CA": mLatit = 49.282729; mLongit= -123.120738; break;
    	case "Pisa, IT": mLatit = 43.722839; mLongit= 10.401689; break;
    	case "Suez, EG": mLatit = 29.966834; mLongit= 32.549807; break;
    	case "Antananarivo, MG": mLatit = -18.879190; mLongit= 47.507905; break;
    	case "Pune, IN": mLatit = 18.520430; mLongit= 73.856744; break;
    	case "Havana, CU": mLatit = 23.054070; mLongit= -82.345189; break;
    	case "Bahia Blanca, AR": mLatit = -38.718318; mLongit= -62.266348; break;
    	case "Tashkent, UZ": mLatit = 41.299496; mLongit= 69.240073; break;
    	case "Kigali, RW": mLatit = -1.970579; mLongit= 30.104429; break;
    	case "Timbuktu, ML": mLatit = 16.766589; mLongit= -3.002561; break;
    	case "Makassar, ID": mLatit = -5.147665; mLongit= 119.432731; break;
    	case "Montevideo, UY": mLatit = -34.901113; mLongit= -56.164531; break;
    	case "Karachi, PK": mLatit = 24.861462; mLongit= 67.009939; break;
    	case "Matsubara, JP": mLatit = 34.577879; mLongit= 135.551850; break;
    	case "Harbin, CN": mLatit = 45.803775; mLongit= 126.534967; break;
    	case "Sarajevo, BA": mLatit = 43.856259; mLongit= 18.413076; break;
    	case "Mazatlan, MX": mLatit = 23.249415; mLongit= -106.411142; break;
    	case "Jaipur, IN": mLatit = 26.912434; mLongit= 75.787271; break;
    	case "Maastricht, NL": mLatit = 50.851368; mLongit= 5.690973; break;
        default: mLatit = -6.208763; mLongit= 106.845599; break;
    	}
	}
}

