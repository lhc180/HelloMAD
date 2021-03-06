package edu.neu.madcourse.binbo.persistentboggle;

import java.util.Date;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import edu.neu.madcourse.binbo.R;

public class PBSignUp extends Activity implements OnClickListener {
	private EditText mEditTextAccount = null;
	private PBNameList mNames = new PBNameList();
	private static final String ACCOUNT_NAME = "account_name";		


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pb_signup);
		
		mEditTextAccount = (EditText)findViewById(R.id.pbsignup_account_name_input);
		
		// Set up click listeners for all the buttons
		View btn_signup = this.findViewById(R.id.pbsignup_signup_button);
		btn_signup.setOnClickListener(this);					
	}
	
	public void onClick(View v) {				
		switch (v.getId()) {
		case R.id.pbsignup_signup_button:
			doSignUp();			
			break;
		}
	}
	
	protected PBPlayerInfo addAccount(String account) throws JSONException {
		// add the account 
		// first we add the player info first
		PBPlayerInfo newPlayer = new PBPlayerInfo(account);
		newPlayer.setStatus("offline");
		newPlayer.setUpdateTime((new Date()).getTime());
		if (newPlayer.commit() == false) {
			return null;
		}
		// then we add the account name to the account name list
		mNames.add(account);
		if (mNames.commit() == false) {			
			return null;
		}
		
		return newPlayer;
	}

	public void doSignUp() {
		String account = mEditTextAccount.getText().toString().trim();
		PBPlayerInfo newPlayer = null;

		if (account.compareTo("") == 0) {
			Toast.makeText(this, 
				"Please enter an account name.", Toast.LENGTH_LONG).show();
			return;
		}
		
		try {
			// get account name list			
			if (mNames.acquire() == false) {
				Toast.makeText(this, 
		            "Sorry, server can not be connected. Please try again.",
		            Toast.LENGTH_LONG).show();
				return;
			} else {
				// check whether the account name has been used
				for (int i = 0; i < mNames.size(); i++) {
		    		String existAccount = mNames.get(i);
		    		if (existAccount.compareTo(account) == 0) { // already exist
		    			Toast.makeText(getApplicationContext(), 
		                   "Sorry, the account name has been used.", Toast.LENGTH_LONG).show();
		    			return;
		    		}
		    	}
				
				if ((newPlayer = addAccount(account)) == null) {
					Toast.makeText(this, 
				            "Sorry, server can not be connected. Please try again.",
				            Toast.LENGTH_LONG).show();					
				}
					    		    				    								
			}
		} catch (JSONException e) {
			try {
				if ((newPlayer = addAccount(account)) == null) {
					Toast.makeText(this, 
				            "Sorry, server can not be connected. Please try again.",
				            Toast.LENGTH_LONG).show();				
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}		
		}
		
		if (newPlayer == null) {
			return;
		}
		
		// switch to next activity
		Intent i = new Intent(this, PBMain.class);
		try {
			i.putExtra(PBMain.HOST_INFO, newPlayer.obj2json());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finish();
		startActivity(i);	
	}
}
