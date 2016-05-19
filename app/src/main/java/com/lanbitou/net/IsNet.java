package com.lanbitou.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.widget.Toast;

import com.lanbitou.thread.HttpGetThread;
import com.lanbitou.thread.ThreadPoolUtils;

/**
 *
 * deprecated:检查网络是否连接
 * author:joyce
 * */
public class IsNet {

	public static boolean isConnect(Context context){
		if(context!=null){
			ConnectivityManager mConnectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo=mConnectivityManager.getActiveNetworkInfo();
			if(mNetworkInfo!=null){
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
}
