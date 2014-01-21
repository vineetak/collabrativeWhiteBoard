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

//import com.example.httppostget.HttpAsyncTask;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.graphics.Color;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;

import android.view.View;
import android.content.Context;
import android.util.AttributeSet;

public class DrawingView extends View {

 	private static String userID;
	//drawing path
	private Path drawPath;
	//drawing and canvas paint
	private Paint drawPaint, canvasPaint;
	//initial color
	private int paintColor = 0xFF660000;
	//canvas
	private static Canvas drawCanvas;
	//canvas bitmap
	private Bitmap canvasBitmap;
	// brush sizes
	private float brushSize, lastBrushSize;
	
	// create a new JSON array 
	private JSONArray eventsJsonArray ;
	
	private JSONObject eventJsonObject;
	
	
	private boolean erase=false;
	
	public DrawingView(Context context, AttributeSet attrs){
	    super(context, attrs);
	    
	    // set up the drawing
	    setupDrawing();
	}
	
	public void setupDrawing(){
		// create a new draw path 
		drawPath = new Path();
		// create new paint to draw the path
		drawPaint = new Paint();
		
		drawPaint.setColor(paintColor);
		
		drawPaint.setAntiAlias(true);
//		drawPaint.setStrokeWidth(20);
		drawPaint.setStrokeWidth(brushSize);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
		
		canvasPaint = new Paint(Paint.DITHER_FLAG);
		canvasPaint.setColor(Color.WHITE);
		// setting the brush size as medium in the beginning
		brushSize = getResources().getInteger(R.integer.medium_size);
		lastBrushSize = brushSize;
		
	}
	
	public static void setUserID(String id){
		
		userID = id;
	}
	
	public static String getUserID(){
		
		return userID;
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
		super.onSizeChanged(w, h, oldw, oldh);
		// instantiate the canvas using the given width and height
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
	
	}
	@Override
	protected void onDraw(Canvas canvas) {
		//draw view
		if(canvas == null )
			return;
		
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		canvas.drawPath(drawPath, drawPaint);
	}
	
	public JSONArray getJSONObjectToPOST(){
		
		
		return this.eventsJsonArray;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	//detect user touch
		float touchX = event.getX();
		float touchY = event.getY();
		
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			System.out.println("down");
		    drawPath.moveTo(touchX, touchY);
		    eventsJsonArray = new JSONArray();

		    // add to json array
		    eventJsonObject = JsonUtil.toJson(event);
		    eventsJsonArray.put(eventJsonObject);

		    break;
		case MotionEvent.ACTION_MOVE:
		    drawPath.lineTo(touchX, touchY);
		    // add to json array
		    eventJsonObject = JsonUtil.toJson(event);
		    eventsJsonArray.put(eventJsonObject);
	        
		    break;
		case MotionEvent.ACTION_UP:
			System.out.println("up");
			// draw the path on the canvas
//			drawPathOnView(drawPath,drawPaint);
		   
			// rest the draw path 
		    
		    
		    // add to json array
		    eventJsonObject = JsonUtil.toJson(event);
		    eventsJsonArray.put(eventJsonObject);

		    drawCanvas.drawPath(drawPath, drawPaint);
		   
		    drawPath.reset();
		    // TODO get this URL from some configuration
		    new HttpAsyncTask().execute("http://1-dot-collaborativewhiteboard.appspot.com/collabrativewhiteboard");

		    break;
		default:
		    return false;
		}
		invalidate();
		return true;


	}
	
	public void setColor(String newColor){
		// first invalidate the view 
	//	invalidate();	
		
		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
		
	}
	
	public void setBrushSize(float newSize){
		//update size
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
			    newSize, getResources().getDisplayMetrics());
		brushSize=pixelAmount;
		drawPaint.setStrokeWidth(brushSize);
	}
	
	public void setLastBrushSize(float lastSize){
	    lastBrushSize=lastSize;
	}
	
	public float getLastBrushSize(){
	    return lastBrushSize;
	}

	// TODO for now using existing brush size and color to draw
	public  static synchronized void drawPathOnView(Path path, Paint paint){
		
		// Do not do anything if there is not path to draw
		if(path == null || paint == null){
			return;
		}
		drawCanvas.drawPath(path, paint);

	}
	
	public void setErase(boolean isErase){
		//set erase true or false
		erase=isErase;
		if(erase){ 
//			drawPaint.setColor(canvasPaint.getColor());
			
//			drawPaint.setAlpha(0xFF);
			drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
		
		}
		else drawPaint.setXfermode(null);
	}
	public void startNew(){
	    drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
	    invalidate();
	}
	
	   public static String POST(String url, JSONObject jsonObject){
		    System.out.println("Posting data....");
	        InputStream inputStream = null;
	        String result = "";
	        try {
	 
	            // 1. create HttpClient
	            HttpClient httpclient = new DefaultHttpClient();
	 
	            // 2. make POST request to the given URL
	            HttpPost httpPost = new HttpPost(url);
	 
	            String json = "";
	 
	            // 4. convert JSONObject to JSON to String
	            json = jsonObject.toString();
	            System.out.println("Data psoted" + json);
	            // 5. set json to StringEntity 
	            StringEntity se = new StringEntity(json);
	 
	            // 6. set httpPost Entity
	            httpPost.setEntity(se);
	 
	            // 7. Set some headers to inform server about the type of the content   
	            httpPost.setHeader("Accept", "application/json");
	            httpPost.setHeader("Content-type", "application/json");
	 
	            // 8. Execute POST request to the given URL
	            HttpResponse httpResponse = httpclient.execute(httpPost);
	 
	            // 9. receive response as inputStream
	            inputStream = httpResponse.getEntity().getContent();
	 
	            // 10. convert inputstream to string
	            if(inputStream != null)
	                result = convertInputStreamToString(inputStream);
	            else
	                result = "Did not work!";
	            
//	            System.out.println("Posting result = "+ result);
	            
	        } catch (Exception e) {
	            Log.d("InputStream", e.getLocalizedMessage());
	        }
	        
	        // 11. return result
	        return result;
	    }
   private class HttpAsyncTask extends AsyncTask<String, Void, String> {
       @Override
       protected String doInBackground(String... urls) {
       	
    	   JSONObject jsonObject = new JSONObject();
           try {
				jsonObject.put("userID", userID);
				jsonObject.put("events",eventsJsonArray);
				jsonObject.put("color", paintColor);
				jsonObject.put("brush-size", brushSize);
				
			} catch (JSONException e) {
//				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           return POST(urls[0], jsonObject	);
       }
       // onPostExecute displays the results of the AsyncTask.
       @Override
       protected void onPostExecute(String result) {
           //Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
      }
   }
   public static String convertInputStreamToString(InputStream inputStream) throws IOException{
       BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
       String line = "";
       String result = "";
       while((line = bufferedReader.readLine()) != null)
           result += line;

       inputStream.close();
       return result;

   }

   public void updateView() {
	   invalidate();
   } 
	
}
