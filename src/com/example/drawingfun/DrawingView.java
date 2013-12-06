package com.example.drawingfun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.graphics.Color;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.TypedValue;

import android.view.View;
import android.content.Context;
import android.util.AttributeSet;

public class DrawingView extends View {

	//drawing path
	private Path drawPath;
	//drawing and canvas paint
	private Paint drawPaint, canvasPaint;
	//initial color
	private int paintColor = 0xFF660000;
	//canvas
	private Canvas drawCanvas;
	//canvas bitmap
	private Bitmap canvasBitmap;
	// brush sizes
	private float brushSize, lastBrushSize;
	
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
		
		// setting the brush size as medium in the beginning
		brushSize = getResources().getInteger(R.integer.medium_size);
		lastBrushSize = brushSize;
		
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
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		canvas.drawPath(drawPath, drawPaint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	//detect user touch
		float touchX = event.getX();
		float touchY = event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		    drawPath.moveTo(touchX, touchY);
		    break;
		case MotionEvent.ACTION_MOVE:
		    drawPath.lineTo(touchX, touchY);
		    break;
		case MotionEvent.ACTION_UP:
		    drawCanvas.drawPath(drawPath, drawPaint);
		    drawPath.reset();
		    break;
		default:
		    return false;
		}
		invalidate();
		return true;
	}
	
	public void setColor(String newColor){
		// first invalidate the view 
		invalidate();	
		
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

	public void setErase(boolean isErase){
		//set erase true or false
		erase=isErase;
		if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		else drawPaint.setXfermode(null);
	}
	public void startNew(){
	    drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
	    invalidate();
	}
}
