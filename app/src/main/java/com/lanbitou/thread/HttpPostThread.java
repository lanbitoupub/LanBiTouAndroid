package com.lanbitou.thread;

import android.os.Handler;
import android.os.Message;

import com.lanbitou.net.GetPostUtil;

public class HttpPostThread implements Runnable{
	
	private Handler handler;
	private String url = null;
	private String value;
	private String img = "";
	Message msg = new Message();
	
	public HttpPostThread(Handler handler, String url, String value)
	{
		this.handler = handler;
		this.url = url;
		this.value = value;
	}
	
	public HttpPostThread(Handler handler, String url, String value, String img)
	{
		this.handler = handler;
		this.url = url;
		this.value = value;
		this.img = img;
	}
	

	@Override
	public void run() {
		
		String result = null;
		if(img.equals(""))
		{
			result = GetPostUtil.doPost(url, value);
		}
		else
		{
			result = GetPostUtil.doPost(url, value);
		}
		
		msg.what = 0x123;
		msg.arg1 = 1;
		msg.obj = result;
		handler.sendMessage(msg);
	}

}
