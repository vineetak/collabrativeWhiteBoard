package com.example.drawingfun;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


public class LoginActivity extends Activity {

	final Context context = this;

	private String userID = "";
	private String drawingID;
	private String url ="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		System.out.println("Creating the login  activity");
		super.onCreate(savedInstanceState);
		// set the layout of the activity
		setContentView(R.layout.login);

		// get the reference of the button
		Button btnSignIn=(Button)findViewById(R.id.buttonSignIn);
		final EditText editTextUserName=(EditText)findViewById(R.id.editTextUserNameToLogin);

		// create a new handler to listen to the click on this button
		View.OnClickListener loginHandler = new View.OnClickListener() {
			public void onClick(View v) {
				/// Create Intent for drawView activity and abd Start The Activity
				// TODO change the name of this MainActivity

				final Dialog drawingDialog = new Dialog(context);
				drawingDialog.setTitle("Select ");

				// set the layout of the brush dialog 
				drawingDialog.setContentView(R.layout.drawing_type);

				Button startNew = (Button)drawingDialog.findViewById(R.id.startnew);
				startNew.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						final Dialog enterDrawingIDDialog = new Dialog(context); 
						enterDrawingIDDialog.setTitle("Select ");

						// set the layout of the brush dialog 
						enterDrawingIDDialog.setContentView(R.layout.enter_drawing_id);

						final EditText editTextDrawingID=(EditText)enterDrawingIDDialog.findViewById(R.id.editTextDrawingID);

						if(editTextDrawingID == null){

							System.out.println("Editing text null ");
							enterDrawingIDDialog.dismiss();
							return;
						}
						Button buttonOK = (Button)enterDrawingIDDialog.findViewById(R.id.buttonOK);
						buttonOK.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								//	    	    		    	Intent intentDrawView =new Intent(getApplicationContext(),MainActivity.class);

								drawingID= editTextDrawingID.getText().toString();

								System.out.println("The drawing ID entered is" + drawingID);
								url= "http://1-dot-collaborativewhiteboard.appspot.com/collabrativewhiteboard";
								url = url + "?userID=" + userID + "&drawingID=" + drawingID +"&type=new";
								new requestForJoin().execute(url);

								Intent intentDrawView =new Intent(getApplicationContext(),MainActivity.class);
								userID=editTextUserName.getText().toString();
								intentDrawView.putExtra("userID", userID);
								intentDrawView.putExtra("drawingID", drawingID);
								startActivity(intentDrawView);
							    	                    startActivity(intentDrawView);
								enterDrawingIDDialog.dismiss();
							}
						});		

						enterDrawingIDDialog.show();
						
						
						drawingDialog.dismiss();
					}
				});

				Button joinExisting = (Button)drawingDialog.findViewById(R.id.joinexisting);
				joinExisting.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						drawingDialog.dismiss();

						// create a new dialogue to get the drawing ID
						final Dialog enterDrawingIDDialog = new Dialog(context); 
						enterDrawingIDDialog.setTitle("Select ");

						// set the layout of the brush dialog 
						enterDrawingIDDialog.setContentView(R.layout.enter_drawing_id);

						final EditText editTextDrawingID=(EditText)enterDrawingIDDialog.findViewById(R.id.editTextDrawingID);

						if(editTextDrawingID == null){

							System.out.println("Editing text null ");
							enterDrawingIDDialog.dismiss();
							return;
						}
						Button buttonOK = (Button)enterDrawingIDDialog.findViewById(R.id.buttonOK);

						buttonOK.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								//	    	    		    	Intent intentDrawView =new Intent(getApplicationContext(),MainActivity.class);

								drawingID= editTextDrawingID.getText().toString();

								System.out.println("The drawing ID entered is" + drawingID);
								url= "http://1-dot-collaborativewhiteboard.appspot.com/collabrativewhiteboard";
								url = url + "?userID=" + userID + "&drawingID=" + drawingID +"&type=join";
								new requestForJoin().execute(url);

								// TOOO call the the async task to load the canvass
								//	    	                    intentDrawView.putExtra("userID", userName);
								//	    	                    startActivity(intentDrawView);
								enterDrawingIDDialog.dismiss();
							}
						});		

						enterDrawingIDDialog.show();
						// on clikcing ok here run the async task to check if the user is 
						// async task user ID, drawingID , type=joinExisting
						//	                    if(success){
						//	                    	
						//	                    	// start the intent and load the drawing ID
						// add the image as string to the ID
						//	                    }
						//	                    else{
						//	                    	// show toast the there was an error
						//	                    	// show the drawing Dailog again
						//	                    }

						// crete 
						//	                    Toast.makeText(context,"Waiting to join...", 
						//	                            Toast.LENGTH_LONG).show();
					
					}
				});

				drawingDialog.show();
			}
		};

		btnSignIn.setOnClickListener(loginHandler);


	}

	private class requestForJoin extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			System.out.println("requesting to join...");
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
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
			String result = null;

			if(httpResponse != null){
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
				if(inputStream != null){
					try {
						result = DrawingView.convertInputStreamToString(inputStream);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			return result;
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			// TODO check the status 
			Toast.makeText(context,result, 
                    Toast.LENGTH_LONG).show();
		}
		
	}
}
