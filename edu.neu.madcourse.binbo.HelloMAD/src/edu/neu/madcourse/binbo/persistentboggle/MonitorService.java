package edu.neu.madcourse.binbo.persistentboggle;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import edu.neu.madcourse.binbo.R;


public class MonitorService extends Service {  
	  
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	public static final int NOTIFICATION_ID = 1000; // seems ok
	
	private static final String SERVICE_COMMAND = "service_command";
	private static final int SERVICE_START = 1;
	private static final int SERVICE_END   = 2;
	private static final String PLAYER_NAMES = "player_names";
	private boolean mRun = true;
	private boolean mGameOver = false; 
	private boolean mOppoQuit = false; 
	private long mHostStartTime = 0;
	
	private final class ServiceHandler extends Handler {		
		
		Message mMsg = null;
		
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			@SuppressWarnings("unchecked")
			ArrayList<String> mPlayers = (ArrayList<String>)msg.obj;

			PBPlayerInfo mHost = new PBPlayerInfo(mPlayers.get(0));
			PBPlayerInfo mOppo = new PBPlayerInfo(mPlayers.get(1));			
			mMsg = msg;
			
			while (mRun) {				
				try {
					commitHostInfo(mHost);
					acquireOpponentInfo(mOppo);
					Thread.sleep(2000);
//					showNotification(msg, 0);
				} catch (JSONException e) {
					// do nothing in the service here, handle it
					// when the user returns to the activity
					e.printStackTrace(); 
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			mGameOver = false;
			mOppoQuit = false;
			
			NotificationManager nm = 
					(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			nm.cancel(0);
			stopSelf(msg.arg1);
		}
		
		private void commitHostInfo(PBPlayerInfo host) throws JSONException {
			if (host.commit() == false) { // something wrong with the server
				// deal with it after we return to the PBGame activity
			} else {
				// do nothing, we commit the host information to update the 
				// update_time, which used by other players to determine whether
				// you are online or dropped.
			}
		}				
		
		private void acquireOpponentInfo(PBPlayerInfo opponent) throws JSONException {
			if (opponent.acquire() == false) { // something wrong with the server
				// we do nothing but still try to acquire, because we don't
				// want to interrupt the user from doing other things, and the 
				// server might be good after a little while. 
			} else {
				// here we get the opponent information successfully, we just check its status
				// to figure out whether we should notify the user about the game information.
				long elapsed = (System.currentTimeMillis() - mHostStartTime) / 1000;
				if (elapsed >= PBGame.DEFAULT_GAME_TIME) {
					// notify the host that the game is over
					if (!mGameOver) { // send only one notification
						showGameOverNotification(0);
						mGameOver = true;
					}
				} else if (opponent.getStatus().compareTo("playing") != 0) { 
					// your opponent might quit the game, notify the host
					if (!mOppoQuit) {
						showOppoQuitNotification(0);
						mOppoQuit = true;
					}
				} else { 
					// everything is fine, do nothing
				}
			}
		}
		
		// if a notification has been sent, it should not be sent again to bother the user
		private void showGameOverNotification(int id) {
			NotificationManager nm = 
					(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			Notification notification = 
					new Notification(R.drawable.ic_launcher, "Persistent Boggle", System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			Intent intent = new Intent(getApplicationContext(), PBGame.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent contentIntent = PendingIntent.getActivity(
					getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			notification.setLatestEventInfo(getApplicationContext(), "Persistent Boggle", "Time for the game is over",
					contentIntent);
			nm.notify(id, notification);
		}
		
		private void showOppoQuitNotification(int id) {
			NotificationManager nm = 
					(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			Notification notification = 
					new Notification(R.drawable.ic_launcher, "Persistent Boggle", System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			Intent intent = new Intent(getApplicationContext(), PBGame.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent contentIntent = PendingIntent.getActivity(
					getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			notification.setLatestEventInfo(getApplicationContext(), "Persistent Boggle", "Your opponent has quit the game",
					contentIntent);
			nm.notify(id, notification);
		}
	}

	@Override
	public void onCreate() {

		HandlerThread thread = new HandlerThread("ServiceStartArguments", 0);
		thread.start();
		
		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();		
		Bundle bundle = intent.getExtras();
		int cmd = bundle.getInt(SERVICE_COMMAND);
		mHostStartTime = bundle.getLong(PBGame.HOST_START_TIME);
		
		if (cmd == SERVICE_START) {
			mRun = true;
			// give it an id, so we can know which one to stop after the work is done
			Message msg = mServiceHandler.obtainMessage();
			msg.arg1 = startId;
			msg.obj  = bundle.getStringArrayList(PLAYER_NAMES);
			mServiceHandler.sendMessage(msg);
		} else if (cmd == SERVICE_END) {
			mRun = false;
		}
		
		return START_STICKY; // restart if be killed
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}
	
}