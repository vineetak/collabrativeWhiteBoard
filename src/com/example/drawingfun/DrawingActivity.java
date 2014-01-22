package com.example.drawingfun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import android.provider.MediaStore;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.format.Time;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Toast;

import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

/**
 * This class represents the drawing activity. It runs the background service to 
 * receive the strokes from the server and updates the drawingView. It also provides functionality 
 * for the draw , select color , erase and save buttons in the view. 
 * @author vineetak
 *
 */
public class DrawingActivity extends Activity implements OnClickListener{

	private  DrawingView drawView;
	
	private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn;

	private Intent intent;
	public static float smallBrush, mediumBrush, largeBrush;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_activity);
        
        String userID = getIntent().getExtras().getString("userID");
           
        // get the view using the id defined in the main activity file
        drawView = (DrawingView)findViewById(R.id.drawing);
        DrawingView.setUserID(userID);
        
        System.out.println(" MAIN DRAWING VIEW ID " + userID);

        // get the paint colors
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        
        // get the first paint color
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        
        // set a different image on the first selected button
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
 
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
        
        /* Add listeners to the buttons in the layout*/
        // draw
        drawBtn = (ImageButton)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);
        drawView.setBrushSize(mediumBrush);
        
        // erase
        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);
        
        // new 
        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        // save 
        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);
        
        // start the Updatedrawing view service to get the strokes from other apps using http get requests to the server
        intent = new Intent(this, UpdateDrawingView.class);
        intent.putExtra("userID", userID);


    }

    /**
     * This is the receiver for the UpdateDrawingView class. This is used to refresh the 
     * drawing view after the Update Service draws on the canvas.
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
       
		@Override
		public void onReceive(Context context, Intent intent) {
			drawView.updateView();
		}
    };  

    /**
     * This method resumes the UpdateView service on activity resume.
     */
    @Override
    public void onResume() {
            super.onResume();                
            startService(intent);
            registerReceiver(broadcastReceiver, new IntentFilter(UpdateDrawingView.BROADCAST_ACTION));
    }
    
    /**
     * This mehod stopes the Update view service on activity pause.
     */
    
    @Override
    public void onPause() {
            super.onPause();
            unregisterReceiver(broadcastReceiver);
            stopService(intent);                 
    }    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /**
     * This method is the handler to select the pain color
     * @param view
     */
    public void paintClicked(View view){
        //use chosen color
    	drawView.setErase(false);
    	drawView.setBrushSize(drawView.getLastBrushSize());
    	
    	if(view!=currPaint){
    		
    		// get the color from the selected button 
    		ImageButton imgView = (ImageButton)view;
    		String color = view.getTag().toString();
    		
    		// set the color in the draw view
    		drawView.setColor(color);
    		
    		// update the UI to reflect the new chosen color
    		imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
    		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
    		currPaint=(ImageButton)view;
    	}
    	
    	
    }
    /**
     * This method handles clicks for all the buttons in the UI,
     */
    @Override
    public void onClick(View view){
    	// if the clicked button is the draw button
    	if(view.getId()==R.id.draw_btn){
   
    	    //draw button clicked
    		final Dialog brushDialog = new Dialog(this);
    		brushDialog.setTitle("Brush size:");
    		
    		// set the layout of the brush dialog 
    		brushDialog.setContentView(R.layout.brush_chooser);

    		ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
    		smallBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setBrushSize(smallBrush);
    		        drawView.setLastBrushSize(smallBrush);
    		        drawView.setErase(false);
    		        brushDialog.dismiss();
    		    }
    		});
    		
    		ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
    		mediumBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setBrushSize(mediumBrush);
    		        drawView.setLastBrushSize(mediumBrush);
    		        drawView.setErase(false);
    		        brushDialog.dismiss();
    		    }
    		});
    		
    		ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
    		largeBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setBrushSize(largeBrush);
    		        drawView.setLastBrushSize(largeBrush);
    		        drawView.setErase(false);
    		        brushDialog.dismiss();
    		    }
    		});
    		
    		brushDialog.show();
    	
    	}
    	else if(view.getId()==R.id.erase_btn){
    		final Dialog brushDialog = new Dialog(this);
    		brushDialog.setTitle("Eraser size:");
    		brushDialog.setContentView(R.layout.brush_chooser);
    		
    		ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
    		smallBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setErase(true);
    		        drawView.setBrushSize(smallBrush);
    		        brushDialog.dismiss();
    		    }
    		});
    		ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
    		mediumBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setErase(true);
    		        drawView.setBrushSize(mediumBrush);
    		        brushDialog.dismiss();
    		    }
    		});
    		ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
    		largeBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setErase(true);
    		        drawView.setBrushSize(largeBrush);
    		        brushDialog.dismiss();
    		    }
    		});
    		brushDialog.show();
    	}
    	
    	else if(view.getId()==R.id.new_btn){
    	    //new button
    		AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
    		newDialog.setTitle("New drawing");
    		newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
    		newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
    		    public void onClick(DialogInterface dialog, int which){
    		        drawView.startNew();
    		        dialog.dismiss();
    		    }
    		});
    		newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
    		    public void onClick(DialogInterface dialog, int which){
    		        dialog.cancel();
    		    }
    		});
    		newDialog.show();
    	}
    	else if(view.getId()==R.id.save_btn){
    		
            //save drawing
    		AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
    		saveDialog.setTitle("Save drawing");
    		saveDialog.setMessage("Save drawing to device Gallery?");
    		saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
    		    public void onClick(DialogInterface dialog, int which){
    		        //save drawing
    		    	drawView.setDrawingCacheEnabled(true);
    		    	String imgSaved = MediaStore.Images.Media.insertImage(
    		    		    getContentResolver(), drawView.getDrawingCache(),
    		    		    UUID.randomUUID().toString()+".png", "drawing");

    		    	if(imgSaved!=null){
    		    	    Toast savedToast = Toast.makeText(getApplicationContext(),
    		    	        "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
    		    	    savedToast.show();
    		    	}
    		    	else{
    		    	    Toast unsavedToast = Toast.makeText(getApplicationContext(),
    		    	        "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
    		    	    unsavedToast.show();
    		    	}
    		    	drawView.destroyDrawingCache();

    		    }
    		});
    		saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
    		    public void onClick(DialogInterface dialog, int which){
    		        dialog.cancel();
    		    }
    		});
    		saveDialog.show();
    	}

    }
    
  
}
