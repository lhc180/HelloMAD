package edu.neu.madcourse.binbo.rocketrush.dialog;

import edu.neu.madcourse.binbo.R;
import edu.neu.madcourse.binbo.rocketrush.GameMode;
import edu.neu.madcourse.binbo.rocketrush.RocketRushActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class VersusModeDialogFragment extends DialogFragment {
	protected RocketRushActivity mActivity = null;
	protected GameMode mGameMode = null;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mActivity = (RocketRushActivity) getActivity();	
		mGameMode = mActivity.getCurrentGameMode();
	    // Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setItems(R.array.versus_mode_dialog_item, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {		    			    	
		    	// The 'which' argument contains the index position
		    	// of the selected item
		    	switch (which) {
		    	case 0: // setting
		    		break;
		    	case 1: // exit		    		
		    		mActivity.switchGameMode(0); // waiting mode
		    		break;
		    	}
		    }
	    });
	    return builder.create();
	}
	
	@Override
	public void onStop() {		
		mGameMode.pause();
		super.onStop();
	}

	@Override
	public void onStart() {
		mGameMode.resume();	
		super.onStart();
	}
}
