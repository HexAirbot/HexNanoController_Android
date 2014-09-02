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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class FeedbackActivity extends Activity {

	private Button save_feedback = null;
	private EditText feedback	 = null;
	private String content		 = null;
	private static int POST_SUCCESS	 = 1;
	private static int POST_FAIL	 = 2;
	private Button backbtn = null;
	private CustomProgressDialog progressDialog = null;
	private int sendResult = POST_SUCCESS;
	
	private Handler myHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			sendResult = msg.what;
			new AlertDialog.Builder(FeedbackActivity.this).
			setTitle(R.string.kindly_reminder).
			setMessage(msg.what==POST_SUCCESS ? getResources().getString(R.string.send_success) : 
						getResources().getString(R.string.send_fail)).
			setPositiveButton(R.string.sure_msg, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (sendResult == POST_SUCCESS) {
						FeedbackActivity.this.finish();
					}
				}
			}).show();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		
		save_feedback = (Button)findViewById(R.id.feedbackSave);
		feedback	  = (EditText)findViewById(R.id.feedbackText);
		save_feedback.setOnClickListener(new SaveBtnLinstener());
		backbtn	  = (Button)findViewById(R.id.backBtn);
		backbtn.setOnClickListener(new ImagebtnLinstener());
	}
	
	class ImagebtnLinstener implements OnClickListener {

		@Override
		public void onClick(View v) {
			FeedbackActivity.this.finish();
		}
		
	}
	
	class SaveBtnLinstener implements OnClickListener {

		@Override
		public void onClick(View v) {
			content	= feedback.getText().toString();
			if(content==null || "".equals(content.trim())){
				Toast.makeText(FeedbackActivity.this, R.string.missing_input, Toast.LENGTH_LONG).show();
				return;
			}
			
			showProgress(R.string.sending);
			
			new Thread(){

				@Override
				public void run() {					
					String response =  getReultForHttpPost1(content);
					progressDialog.dismiss();	
					if ("success".equals(response)) {
						FeedbackActivity.this.myHandler.sendEmptyMessage(POST_SUCCESS);
					}else{
						FeedbackActivity.this.myHandler.sendEmptyMessage(POST_FAIL);
					}
					
					super.run();
				}
				
			}.start();
		}
		
	}

	public void showProgress(int resID) {  
        if (progressDialog != null) {  
            progressDialog.cancel();  
        }  
        progressDialog = new CustomProgressDialog(FeedbackActivity.this, getResources()  
                .getString(resID));  
        progressDialog.show();  
    }
	
	public static String getReultForHttpPost1(String content) {
		try {
			String strResult = null;
			String httpUrl = Const.user_feedback_url;
			
			HttpPost httpRequest = new HttpPost(httpUrl);
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair("content", content));
			
			HttpEntity httpentity = new UrlEncodedFormEntity(params, "utf-8");
			
			httpRequest.setEntity(httpentity);
			
			HttpClient httpclient = new DefaultHttpClient();
			
		
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				strResult = EntityUtils.toString(httpResponse.getEntity());
			}
			return strResult;
			
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
			
	}
}
