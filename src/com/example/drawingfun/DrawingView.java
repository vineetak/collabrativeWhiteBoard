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
/**
 * This class represents a custom drawing view and consists of a canvas. It listens to the touch events and draws the strokes on the
 * canvas using the selected color. A stroke is enacpasulated into a JSONobject containing the touch events , paint 
 * and brush size, and posted to the server using Http POST.
 * @author vineetak
 *
 */
public class DrawingView extends View {

	private static String userID;

	/**
	 *  Represents the path to draw strokes on the canvasd
	 */
	private Path drawPath;

	/**
	 *  paint used to draw on the canvas and the canvas background
	 */
	private Paint drawPaint, canvasPaint;

	/**
	 * default paint color
	 */
	private int paintColor = 0xFF660000;

	/**
	 *  Canvas object
	 */
	private static Canvas drawCanvas;

	/**
	 *  canvas bit map
	 */
	private Bitmap canvasBitmap;
	/**
	 *  brush sizes
	 */
	private float brushSize, lastBrushSize;

	/**
	 *  JSON array to store the touch events for a single stroke
	 */
	private JSONArray eventsJsonArray ;

	/**
	 *  JSON object to post the event to the server
	 */
	private JSONObject eventJsonObject;

	/**
	 *  This represents if the erase mode is on 
	 */
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

	//	public JSONArray getJSONObjectToPOST(){
	//		return this.eventsJsonArray;
	//	}
	/**
	 *  This method handles the touch events. Motion events between a touch start and touch release define a stroke.
	 *  On completion of a stroke it is encapsulated into a JSON object and  HttpAsyncTask is executed to post the data to the server.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//detect user touch
		float touchX = event.getX();
		float touchY = event.getY();


		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			System.out.println("down");
			drawPath.moveTo(touchX, touchY);

			// initialize the JSON array to 
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

			// add to the events to the JSON array
			eventJsonObject = JsonUtil.toJson(event);
			eventsJsonArray.put(eventJsonObject);

			// draw the path on the cnavas
			drawPathOnView(drawPath,drawPaint);

			drawPath.reset();

			// Call the HttpAsyncTask to post the JSON Object to the server
			new HttpAsyncTask().execute("http://1-dot-collaborativewhiteboard.appspot.com/collabrativewhiteboard");

			break;
		default:
			return false;
		}
		// invalidate the view to refresh
		invalidate();
		return true;


	}

	/**
	 * This method updates the color of the current drawing Paint
	 * @param newColor
	 */
	public void setColor(String newColor){

		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);

	}
	/**
	 * This method sets the size of the brush used to draw on the canvas 
	 * @param newSize : Size of the brush as float
	 */
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

	/**
	 * This method is used to draw the path with the specified paint object on the canvas. 
	 * It is suggested to used this method for drawing on the canvas instead of calling the drawPath directly 
	 * to avoid any synchronization issues.
	 * 
	 * @param path : Represents the path to be drawn on the canvas
	 * @param paint : Represents the paint to be used to draw the path
	 */
	public  static synchronized void drawPathOnView(Path path, Paint paint){

		// Do not do anything if there is not path to draw
		if(path == null || paint == null){
			return;
		}
		drawCanvas.drawPath(path, paint);
	}

	/**
	 *  This method sets and resets the erase mode. In the erase mode the paint color is set as the canvas paint color 
	 * @param isErase
	 */
	public void setErase(boolean isErase){
		//set erase true or false
		erase=isErase;
		if(erase){ 
			drawPaint.setColor(canvasPaint.getColor());
			paintColor = canvasPaint.getColor();
			drawPaint.setAlpha(0xFF);
		}
		return;
	}
	/**
	 *  This method clears the canvas to start a new drawing.
	 */
	public void startNew(){
		drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		invalidate();
	}

	
	/**
	 *  This method invalidates the view for redraw
	 */
	public void updateView() {
		invalidate();
	} 

	/**
	 * This class is used to create a background task to make http post request to the server.
	 *
	 * @author vineetak
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		/**
		 * This method add the user ID, touch events, paint color and brush size to an JSON object 
		 * and posts it to the server
		 */
		@Override
		protected String doInBackground(String... urls) {

			// Add create the JSONObject to post
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("userID", userID);
				jsonObject.put("events",eventsJsonArray);
				jsonObject.put("color", paintColor);
				jsonObject.put("brush-size", brushSize);

			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// Post the JSON object to the server 
			return JsonUtil.POST(urls[0], jsonObject	);
		}
	}


}
