package alphalfa.android.letsdrop;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class LocationUtils {

	// Current best location estimate
	private static Location mLocation;

	// Reference to the LocationManager and LocationListener
	private static LocationManager mLocationManager;
	private static LocationListener mLocationListener;

	private static final String TAG = "Lets Drop - Get Location";
	
	public static Location GetLocation (Context context) {

		// Acquire reference to the LocationManager
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		if (mLocationManager != null){
			Log.i(TAG, "Get Location");
			
			// Get the last known location from all providers
			// return best reading that is as accurate as minAccuracy
			Location bestResult = null;
			float bestAccuracy = Float.MAX_VALUE;
			List<String> matchingProviders = mLocationManager.getAllProviders();

			for (String provider : matchingProviders) {

				Location location = mLocationManager.getLastKnownLocation(provider);
				if (location != null) {

					float accuracy = location.getAccuracy();
					if (accuracy < bestAccuracy) {
						bestResult = location;
						bestAccuracy = accuracy;
					}
				}
			}

			mLocation = bestResult;
			
			if (null != mLocation) {
				Log.i(TAG, "Location LAT: " + mLocation.getLatitude() + ", LON: " + mLocation.getLongitude());
		    } else {
		    	Log.i(TAG, "No location available"); 
		    }
		}
		
		mLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) { }
			public void onStatusChanged(String provider, int status, Bundle extras) { }
			public void onProviderEnabled(String provider) { }
			public void onProviderDisabled(String provider) { }
		};
		
		mLocationManager.removeUpdates(mLocationListener);
		return mLocation;
	}


	// Get the address from the location
   
   public class GetAddressTask extends AsyncTask<Location, Void, String> {

	   Context mContext;
	   
	   public GetAddressTask(Context context) {
	        super();
	        mContext = context;
	    }
	   
	    @Override
       protected String doInBackground(Location... params) {
	    	Log.i(TAG, "Get Address Task");
	    	if (Geocoder.isPresent()) {
		    	Geocoder geocoder = new Geocoder(mContext, Locale.US); //Locale.getDefault());
	           // Get the current location from the input parameter list
	           Location loc = params[0];
	           // Create a list to contain the result address
	           List<Address> addresses = null;
	           try {
	               // Return 1 address.
	               addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
	           } catch (IOException e1) {
		           Log.e("LocationSampleActivity", "IO Exception in getFromLocation()");
		           e1.printStackTrace();
		           return ("IO Exception trying to get address");
	           } catch (IllegalArgumentException e2) {
		           // Error message to post in the log
		           String errorString = "Illegal arguments " +
		                   Double.toString(loc.getLatitude()) + " , " +
		                   Double.toString(loc.getLongitude()) +
		                   " passed to address service";
		           Log.e("LocationSampleActivity", errorString);
		           e2.printStackTrace();
		           return errorString;
	           }
	           // If the reverse geocode returned an address
	           if (addresses != null && addresses.size() > 0) {
	               // Get the first address
	               Address address = addresses.get(0);
	               // Format the first line city, and country name.
	               String addressText = String.format("%s, %s",
	                       // If there's a street address, add it
	                       //address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
	                       // Locality is usually a city
	                       address.getLocality(),
	                       // The country of the address
	                       address.getCountryName());
	               // Return the text
	               return addressText;
	           } else {
	               return "No address found";
	           }
		  }
	    	return "";
   }
      
	  // @Override
	   protected void onPostExecute(String address) {
		   //super.onPostExecute(result);
	       // Display the results of the lookup.
	       //mCity.equals(address);
	   }
   }
   
}
   