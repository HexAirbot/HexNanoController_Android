package com.hexairbot.hexmini;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FeedbackActivity extends Activity {

	private Button save_feedback = null;
	private EditText feedback	 = null;
	private String content		 = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		
		save_feedback = (Button)findViewById(R.id.feedbackSave);
		feedback	  = (EditText)findViewById(R.id.feedbackText);
		
		save_feedback.setOnClickListener(new SaveBtnLinstener());
	}
	
	class SaveBtnLinstener implements OnClickListener {

		@Override
		public void onClick(View v) {
			content	= feedback.getText().toString();
//			feedback.setText(content + "111");
			new Thread(){

				@Override
				public void run() {
					getReultForHttpPost1(content);
					super.run();
				}
				
			}.start();
		}
		
	}
	
	public static String getReultForHttpPost1(String content) {
		try {
			String strResult = null;
			String httpUrl = "http://192.168.0.106/wordpress/wp-feedback.php";
			// HttpPost连接对象
			HttpPost httpRequest = new HttpPost(httpUrl);
			// 使用NameValuePair来保存要传递的Post参数
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// 添加要传递的参数
			params.add(new BasicNameValuePair("content", content));
			// 设置字符集
			HttpEntity httpentity = new UrlEncodedFormEntity(params, "utf-8");
			// 请求httpRequest
			httpRequest.setEntity(httpentity);
			// 取得默认的HttpClient
			HttpClient httpclient = new DefaultHttpClient();
			// 取得HttpResponse
		
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			// HttpStatus.SC_OK表示连接成功
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的字符串;
				strResult = EntityUtils.toString(httpResponse.getEntity());
			}
			return strResult;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
			
	}
}
