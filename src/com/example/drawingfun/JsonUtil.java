package com.example.drawingfun;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Path;
import android.view.MotionEvent;

public class JsonUtil {
	
	// create a new json object from the event 
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

	// create a path from the JSON object containing an array of points
	public static Path createAndroidPathFromJson(JSONArray events){
		
		Path drawPath = new Path();
		try {
//			JSONArray jArr = events.getJSONArray("events");
			
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
				    
				    //System.out.println("Touch event x:" + x + "y:" + y );
				}

		    point = events.getJSONObject(events.length() - 1);
			touchX = Float.parseFloat(point.getString("x"));
			touchY = Float.parseFloat(point.getString("y"));

			// now is the time to draw
		//	Object drawPaint;
			// finally draw this path using the paint 
			//drawCanvas.drawPath(drawPath, drawPaint);
			//drawPath.lineTo(touchX, touchY);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return drawPath;
		
	}
}
