package edu.neu.madcourse.binbo.rocketrush;

import android.content.Context;
import android.media.MediaPlayer;

public class BackgroundMusic {
	private static MediaPlayer mPlayer = null;
	private static int mSeekPos = 0;
	/** Stop old song and start new one */

	public void create(Context context, int resource) {
		stop();
		// Start music only if not disabled in preferences
		if (Prefs.getMusic(context)) {
			mPlayer = MediaPlayer.create(context, resource);
			if (mPlayer != null) {
				mPlayer.setLooping(true);
				mPlayer.setVolume(0.2f, 0.2f);				
			}
		}
		mSeekPos = 0;
	}
	
	public void setLooping(boolean looping) {
		if (mPlayer != null) {
			mPlayer.setLooping(looping);
		}
	}
	
	public void setVolume(float leftVolume, float rightVolume) {
		if (mPlayer != null) {
			mPlayer.setVolume(leftVolume, rightVolume);				
		}
	}
	
	public void play() {
		if (mPlayer != null) {
			if (!mPlayer.isPlaying()) {
				mPlayer.seekTo(mSeekPos);
				mPlayer.start();						
			}			
		}
	}
	
	public void reset() {
		mSeekPos = 0;		
	}
	
	public void pause() {
		if (mPlayer != null) {
			if (mPlayer.isPlaying()) {
				mPlayer.pause();
				mSeekPos = mPlayer.getCurrentPosition();
			}			
		}
	}

	/** Stop the music */
	public void stop() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
			System.gc();
		}
	}
}