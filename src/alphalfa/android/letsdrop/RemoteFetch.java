package alphalfa.android.letsdrop;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
 
import org.json.JSONObject;

import alphalfa.android.letsdrop.R;
import android.content.Context;
import android.util.Log;
 
public class RemoteFetch {
 
    private static final String DARK_SKY_FORECAST_API = 
    		"https://api.forecast.io/forecast/%s/%s,%s";
    		// "https://api.forecast.io/forecast/APIKEY/LATITUDE,LONGITUDE"
        
    private static final String TAG = "Lets Drop-Remote Fetch";
    
    // private Map <String, double[]> mCitiesLatLong= new HashMap<String, double[]>();
    private static double mLatit, mLongit;
     
    public static JSONObject getJSON(Context context, double cityLatit, double cityLongit){
    // public static JSONObject getJSON(Context context, String city){
    	   	
        try {
        	String keyAPI = context.getString(R.string.dark_sky_forecast_api_id);
        	// mLatit = 37.88;
        	// mLongit = 0.99;
        	// getLatLong(city);
            URL url = new URL(String.format(DARK_SKY_FORECAST_API, keyAPI, 
            		String.valueOf(cityLatit), String.valueOf(cityLongit)));
            		// String.valueOf(mLatit), String.valueOf(mLongit))); 
            Log.i(TAG, "URL: " + url.toString());
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             
            // connection.addRequestProperty("set-content", "letsdrop");
            // connection.addRequestProperty("x-api-key", 
                    // context.getString(R.string.open_weather_maps_app_id));
            
            int responseCode = connection.getResponseCode(); // 500
            Log.i(TAG, "Response code: " + responseCode);
             
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
             
            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();
             
            JSONObject data = new JSONObject(json.toString());
            Log.i(TAG,"JSON: " +  json.toString());
            
            //Log.i(TAG, "Data cod" + String.valueOf(data.getInt("cod")));
            // This value will be 404 if the request was not successful
            // if(data.getInt("cod") != 200){
               // return null;
            //}
             
            return data;
            
        } catch (Exception e){
        	Log.e("RemoteFetch Exception", e.toString());
            return null;
        }
    }
    
	public static JSONObject readJsonFromStream (StringBuffer jsonBuffer, BufferedReader reader) {
		String tmp = "";
		try {
			while((tmp = reader.readLine()) != null)
	            jsonBuffer.append(tmp).append("\n");
	        reader.close();
	        return new JSONObject(jsonBuffer.toString());
		} catch (Exception e) {
			Log.e("RemoteFetch Exception 2", e.toString());
			return null;
		}
	}
	
	public static void getLatLong(String city) {
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