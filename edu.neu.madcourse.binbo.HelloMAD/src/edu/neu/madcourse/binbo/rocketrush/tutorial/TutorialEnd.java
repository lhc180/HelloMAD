package edu.neu.madcourse.binbo.rocketrush.tutorial;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import edu.neu.madcourse.binbo.R;
import edu.neu.madcourse.binbo.rocketrush.RocketRushActivity;

public class TutorialEnd extends Fragment implements OnClickListener {
	private Context mContext = null;
	private TutorialView mView   = null;
	private ImageButton mButton = null;
	
	public static Fragment newInstance(Context context) {
		TutorialEnd f = new TutorialEnd(context);	
		
		return f;
	}
	
	public TutorialEnd(Context context) {
		super();
		mContext = context;				
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.tutorial_end, null);
		mView = (TutorialView)root.findViewById(R.id.tutorialEndView);
		mButton = (ImageButton) root.findViewById(R.id.endTutorialButton); 		
		mButton.setOnClickListener(this);
		return root;
	}

	@Override
	public void onPause() {
		mView.onPause();
		super.onPause();
	}
	
	public void onClick(View v) {
		Intent i = null;
		
		switch (v.getId()) {
		case R.id.endTutorialButton:	
			i = new Intent(mContext, RocketRushActivity.class);
			startActivity(i);
			((Activity) mContext).finish();
			break;
		}
	}
}