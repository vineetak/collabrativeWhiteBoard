package com.example.drawingfun;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.database.CursorJoiner.Result;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;

public class UpdateDrawingView extends Service {

	private String url= "http://1-dot-collaborativewhiteboard.appspot.com/collabrativewhiteboard";
	
	// TODO change this 
	public static final String BROADCAST_ACTION = "com.websmithing.broadcasttest.displayevent";
    private final Handler handler = new Handler();
    Intent intent;

    
    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);   
       // userID = intent.getExtras().getString("userID");
       // System.out.println("UPDATE DRAWING VIEW ID " + userID);
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second   
    }
    
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
       //     requestHttp();
        	new DoBackgroundTask().execute("http://1-dot-collaborativewhiteboard.appspot.com/collabrativewhiteboard");
            handler.postDelayed(this, 1000); // 10 seconds
        }
    };
    
    @Override
    public IBinder onBind(Intent intent) {
            return null;
    }

    @Override
    public void onDestroy() {                
    handler.removeCallbacks(sendUpdatesToUI);                
            super.onDestroy();
    }         
    
	private class DoBackgroundTask extends AsyncTask<String, String, String> {


	private String url = "";
    @Override
    protected String doInBackground(String... params) {
    	
    	System.out.println("Getting response...");
 	      this.url = params[0];
 	      requestHttp();
 	      
		return null;    	
    }
    
    private void requestHttp(){
    	HttpClient httpClient = new DefaultHttpClient();
    	String getUrl = url + "?userID=" + DrawingView.getUserID();
        HttpGet httpGet = new HttpGet(getUrl);
        HttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(httpGet);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		if(httpResponse != null){
		
			String result = null;
			InputStream inputStream = null;
			try {
				inputStream = httpResponse.getEntity().getContent();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
            // 10. convert inputstream to string
            if(inputStream != null)
				try {
					result = DrawingView.convertInputStreamToString(inputStream);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
            if(result.isEmpty())
            	return;
            
            System.out.println("getting result = "+ result);
         
            // the response may contain multiple paths
            try {
				JSONObject responseJsonObjects = new JSONObject(result);
				System.out.println("Json object created");

				JSONArray objectsArray = responseJsonObjects.getJSONArray("objects");
				for (int i=0; i < objectsArray.length() ; i++) {	
				    JSONObject responseJson = objectsArray.getJSONObject(i);
				    
				    System.out.println(responseJson.toString());
				   
				    String userID = responseJson.getString("userID");
				    System.out.println("User id is " + userID);
				
				    JSONArray eventsArray = responseJson.getJSONArray("events");
				    int paintColor = responseJson.getInt("color");
				
				    float brushSize = Float.parseFloat(responseJson.getString("brush-size"));
				    Path drawPath = JsonUtil.createAndroidPathFromJson(eventsArray);
				
				// TO see if strokes and all are needed
				
				Paint drawPaint = new Paint();
				

				drawPaint.setColor(paintColor);
				
				drawPaint.setAntiAlias(true);
				drawPaint.setStrokeWidth(brushSize);
				
				drawPaint.setStyle(Paint.Style.STROKE);
				drawPaint.setStrokeJoin(Paint.Join.ROUND);
				drawPaint.setStrokeCap(Paint.Cap.ROUND);
				
				// draw the path on the view
				DrawingView.drawPathOnView(drawPath, drawPaint);
				
				
				}
				// invalidate the view
				sendBroadcast(intent);
				
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
		}
    }
    
    
}
}
