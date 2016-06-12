package com.lanbitou.thread;

import android.os.Handler;
import android.os.Message;

import com.lanbitou.net.GetPostUtil;

/**
 * 网络Get请求的线程
 * */
public class HttpGetThread implements Runnable {

	private Handler handler;
	private String url;
	private int what = 0x123;
	Message msg = new Message();

	public HttpGetThread(Handler handler, String url) {
		this.handler = handler;

		this.url = url;
	}

	/**
	 * what默认为0x123
	 * @param handler
	 * @param url
	 * @param what
     */
	public HttpGetThread(Handler handler, String url,int what) {
		this.handler = handler;
		this.url = url;
		this.what = what;
	}


	@Override
	public void run() {

		String result = GetPostUtil.doGet(url);
		msg.what = what;
		msg.obj = result;
		msg.arg1 = 1;
		handler.sendMessage(msg);
		
	}
}