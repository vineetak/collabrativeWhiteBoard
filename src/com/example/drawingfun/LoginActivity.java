package com.example.drawingfun;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * This is class represents the login avtivity, which lets the user select a login ID 
 * and creats the drawing Activity
 * @author vineetak
 *
 */
public class LoginActivity extends Activity {

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
		    	
                Intent intentDrawView =new Intent(getApplicationContext(),DrawingActivity.class);
                
                String userName=editTextUserName.getText().toString();
                intentDrawView.putExtra("userID", userName);
                startActivity(intentDrawView);
		    }
		 };
		 
		btnSignIn.setOnClickListener(loginHandler);
            

	}
}
