package edu.neu.madcourse.binbo.rocketrush;

import android.content.Context;
import android.hardware.SensorEventListener;
import android.os.Handler;
import edu.neu.madcourse.binbo.R;

public class WaitingMode extends GameMode {

	protected WaitingScene mScene = null; 
	protected WaitingModeThread mThread = null;	
	protected Context mContext = null;
	
	public WaitingMode(Context context, GameEngine engine, Handler handler) {
		super(engine);
		setHandler(handler);
		mContext = context;
		mScene = new WaitingScene(context);
		mScene.load();		
	}
	
	@Override
	public GameScene getScene() {
		return mScene;
	}	
	
	@Override
	public void resume() {
		if (!mEnable) return;
		
		if (mThread == null) {
			mThread = new WaitingModeThread(mHandler);
			mThread.start();
		}		
		super.resume();
	}

	@Override
	public void pause() {
		if (mThread != null) {
			mThread.end();
			mThread = null;
		}		
		super.pause();
	}
	
	@Override
	public void start() {		
		if (!mEnable) return;
		
		mBackgroundMusic.create(mContext, R.raw.bkg_music_1);
		mBackgroundMusic.play();
		super.start();		
	}

	@Override
	public void stop() {
		mBackgroundMusic.pause();
		mBackgroundMusic.stop();
		super.stop();
	}
	
	@Override
	public void reset() {		
		synchronized (mScene) {
			mScene.reset();
		}
		mBackgroundMusic.reset();
	}

	private final class WaitingModeThread extends BaseThread {
		
		public WaitingModeThread(Handler handler) {
			super(handler);
		}
		
		@Override
		public void run() {			
	
			while (mRun) {
				handleEvent(mEventQueue.poll());			
								
				// update the game scene with the engine
				synchronized (mScene) {
					mEngine.updateGameScene(mScene);
				}
				
				synchronized (this) {
					try {
						wait(GameEngine.ENGINE_SPEED);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} // end of run
	}

	@Override
	public void release() {
		if (mScene != null) {
			mScene.release();
			mScene = null;
		}
		super.release();
	}

	@Override
	public SensorEventListener getSensorListener() {
		// TODO Auto-generated method stub
		return super.getSensorListener();
	}	

}
