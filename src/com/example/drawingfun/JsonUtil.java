package com.example.drawingfun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;

public class JsonUtil {
	
	/**
	 * Create a JSONObject from the motionevent object
	 * @param event
	 * @return
	 */
	public static JSONObject toJson(MotionEvent event){
				
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("x", Float.toString(event.getX()));
			jsonObj.put("y", Float.toString(event.getY()));
			jsonObj.put("type", event.getAction());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObj;
	}

	/**
	 * This method creates an android.graphics.Path object from the JSON array containing the touch events.
	 * It assumes that the first event array is the touch begin and the last event in the array is the touch end. All the events 
	 * in between are added to the path.
	 * @param events
	 * @return
	 */
	// create a path from the JSON object containing an array of points
	public static Path createAndroidPathFromJson(JSONArray events){
		
		Path drawPath = new Path();
		try {

			JSONObject point = (JSONObject) events.getJSONObject(0);
			
			float touchX = Float.parseFloat(point.getString("x"));
			float touchY = Float.parseFloat(point.getString("y"));
			drawPath.moveTo(touchX, touchY);
			
			for (int i=1; i < events.length() - 1; i++) {	
				    point = events.getJSONObject(i);
				    String x = point.getString("x");
				    String y = point.getString("y");
				    
					touchX = Float.parseFloat(x);
					touchY = Float.parseFloat(y);
				    drawPath.lineTo(touchX, touchY);
				    
				}

		    point = events.getJSONObject(events.length() - 1);
			touchX = Float.parseFloat(point.getString("x"));
			touchY = Float.parseFloat(point.getString("y"));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return drawPath;
		
	}
	
	/**
	 * This method creates a new httpclient and posts the JSONObject to the server using Http post 
	 * @param url URL of the server
	 * @param jsonObject The object to post to the server
	 * @return 
	 */
	public static String POST(String url, JSONObject jsonObject){
		System.out.println("Posting data....");
		InputStream inputStream = null;
		String result = "";
		try {

			// create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// make POST request to the given URL
			HttpPost httpPost = new HttpPost(url);

			String json = "";

			// convert JSONObject to JSON to String
			json = jsonObject.toString();
//			System.out.println("Data posted" + json);
			
			//  set json to StringEntity 
			StringEntity se = new StringEntity(json);

			// set httpPost Entity
			httpPost.setEntity(se);

			// 7. Set some headers to inform server about the type of the content   
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			// 8. Execute POST request to the given URL
			HttpResponse httpResponse = httpclient.execute(httpPost);

			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// convert inputstream to string
			if(inputStream != null)
				result = convertInputStreamToString(inputStream);
			else
				result = "Did not work!";


		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}
		
		return result;
	}
	/**
	 *  This is a utility function to convert the input stream object to String
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static String convertInputStreamToString(InputStream inputStream) throws IOException{
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;

	}

}
