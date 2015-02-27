package com.qingyou.http;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.qingyou.qingyouclient.Log;

public class UserLogin extends HttpPacket {

	@Override
	public int makeSendBuffer(Bundle input) {
		
		if (input.getString("account") != null) {
			params.add("username", input.getString("account"));
			params.add("password", input.getString("passwd"));
			params.add("redirect", HttpPacket.SERVER_URL + "?route=qingyou/login_ok");
		}
		url = HttpPacket.SERVER_URL + "?route=common/login";
		
		return 0;
	}

	@Override
	protected void processResult(String response) throws IOException, ProtocolException, JSONException {
		
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(response);
			int status = jsonObj.getInt("status");
			data.putInt("login_status", status);
			if (status == 0) {
				data.putString("token", jsonObj.getString("token"));
				Log.v("PROTO:(UserLogin) OK");
			}
			else {
				errNo = ERR_RESPONSE_INVALID;
				Log.v("PROTO:(UserLogin) Error, status != 0");
			}

			data.putInt("session", HttpUtility.cookieSize());
		} catch(JSONException e) {
			errNo = ERR_RESPONSE_INVALID;
			Log.v("PROTO:(UserLogin) Error");
			e.printStackTrace();
		}

		//System.out.println(response);
	}

}
