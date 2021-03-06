package edu.neu.madcourse.binbo.persistentboggle;

import org.json.JSONException;

public interface IRemoteData {
	public boolean acquire() throws JSONException;
	public boolean commit() throws JSONException;
	public int getDataId();
}
