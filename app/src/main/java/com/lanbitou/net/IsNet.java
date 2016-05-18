package com.lanbitou.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.widget.Toast;

/**
 *
 * deprecated:检查网络是否连接
 * author:joyce
 * */
public class IsNet {

	private  Context ctx;
	//private String url = "http://glimpse.sinaapp.com/isNet.php";
	public IsNet(Context ctx) {
		this.ctx = ctx;
	}

	
	public boolean IsConnect() {
		
		ConnectivityManager manager = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		/*NetworkInfo info = manager.getActiveNetworkInfo();
		if(info != null)
		{
			return info.isAvailable();
		}
		return false;*/
		
		NetworkInfo info = manager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		State stata = null;
		if (info != null) {
			stata = info.getState();
			if (stata == State.CONNECTED)
				return true;
		}
		
		info = null;
		info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		stata = null;
		if (info != null) {
			stata = info.getState();
			if (stata == State.CONNECTED) {
				
				return true;
				
			}
		}
		Toast.makeText(ctx, "无网络连接", Toast.LENGTH_SHORT).show();
		return false;
	}
}
