package com.lanbitou.thread;

import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.lanbitou.entities.NoteEntity;
import com.lanbitou.net.GetPostUtil;

import java.util.Date;

/**
 * 网络Get请求的线程
 * */
public class HttpGetThread implements Runnable {

	private Handler handler;
	private String url;
	Message msg = new Message();

	public HttpGetThread(Handler handler, String url) {
		this.handler = handler;

		this.url = url;
	}

	@Override
	public void run() {

		String result = GetPostUtil.doGet(url);
		msg.what = 0x123;//表示get请求
		msg.obj = result;
		msg.arg1 = 1;
		handler.sendMessage(msg);
		
	}
}