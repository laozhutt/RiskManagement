package com.sensordemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends Activity {


	public static TextView stateView;
	private TextView numberView;
	public static ImageView trainButton;
	public static ImageView lockButton;
	private TextView progressView;

	public static int dataNumber;
	public static int fileNumber = 0;//多少个文件才算训练够

	private CollectDataService.CollectDataBinder collectDataBinder;
	public static CollectDataService collectDataService;//全局使用
	private ServiceConnection collectDataServiceConnection;
	public static boolean isCollecting;
	public static Timer CollectTimer;
	public static Timer DetectTimer;

	private DetectService.DetectBinder detectBinder;
	public static  DetectService detectService;
	private ServiceConnection detectDataServiceConnection;
	public static boolean isDetecting;

	public static boolean isTrainFinished;

	public static final String configureFilePath = "config.xml";

	public static Properties configureObject;

	public static final int RESULT_OK = 1;
	public static final int RESULT_WRONG = -1;

	public static final int SETPASSWORD = 1;

	final static int MESSAGE_UPDATE_NUMBER = 1;
	final static int MESSAGE_CHANGE_STATE =2;
	final static int MESSAGE_SET_INVISIBLE =3;
	final static int MESSAGE_STOP_DETECT =4;
	final static int MESSAGE_LEGAL_FILE =5;
	final static int MESSAGE_CLICK_LOCKBUTTON = 6;
	final static int MESSAGE_HANDLE_TRAIN = 7;
	final static int MESSAGE_HANDLE_TEST = 8;
	final static int MESSAGE_HANDLE_ALARM = 9;


	public static boolean isSet;

	private Intent detectIntent;
	private Intent collectIntent;

	private MyBroadcast mybroadcast;

	public static Handler messageHandler;







	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.startService(new Intent(this, LongService.class));
		stateView = (TextView) findViewById (R.id.state);

		numberView = (TextView) findViewById (R.id.number);
		trainButton = (ImageView) findViewById (R.id.trainButton);
		lockButton = (ImageView) findViewById(R.id.lockButton);

		progressView = (TextView)findViewById(R.id.progress);
		stateView.setText(getString(R.string.app_state_ini));

		numberView.setText("0");


		isCollecting = false;
		isDetecting = false;
		CollectTimer = new Timer();

		detectIntent = new Intent(this, DetectService.class);
		collectIntent = new Intent(this,CollectDataService.class);

		lockButton.setVisibility(View.INVISIBLE);

		messageHandler = new Handler(){
			public void handleMessage(Message m){
				switch(m.what){
					case MESSAGE_UPDATE_NUMBER:
						numberView.setText(String.valueOf(dataNumber));
						break;
					case MESSAGE_CHANGE_STATE:
						stateView.setText(getString(R.string.app_state_ready));
//						lockButton.setVisibility(View.VISIBLE);//训练完按钮可见
						break;
					case MESSAGE_SET_INVISIBLE:
//						lockButton.setVisibility(View.VISIBLE);
						break;
					case MESSAGE_STOP_DETECT:
						stopDetectTimer();
					case MESSAGE_LEGAL_FILE:
						if(fileNumber < 100){
							fileNumber = fileNumber + 1;
//							Log.e("progress",fileNumber+"");
						}
						if(fileNumber == 100){
//							lockButton.setVisibility(View.VISIBLE);//训练完按钮可见
//							trainButton.setClickable(false);//训练完就不让训练了
						}

						Log.e("progress",fileNumber+"");
						configureObject.setProperty(getString(R.string.property_file_number), String.valueOf(fileNumber));
//						progressView.setText(Integer.parseInt(configureObject.getProperty(getString(R.string.property_file_number)))*10 + "%");
						progressView.setText(configureObject.getProperty(getString(R.string.property_file_number)) + "%");
						recordFile();
						break;
					case MESSAGE_HANDLE_TRAIN:
						Log.e("broadcasttrain","start");
						trainButton.performClick();
						break;
					case MESSAGE_HANDLE_TEST:
						if(isCollecting){
							if(lockButton.getVisibility() == View.VISIBLE){
								lockButton.performClick();
								lockButton.performClick();
							}

						}
						else{
							if(lockButton.getVisibility() == View.VISIBLE){
								lockButton.performClick();
							}

						}
						break;
					case MESSAGE_HANDLE_ALARM:
//						detectService.pauseDetectService();
						detectService.startDetectService();
//					case MESSAGE_CLICK_LOCKBUTTON:
//						lockButton.performClick();
					default:
						break;
				}
			}
		};

		configureObject = new Properties();

		mybroadcast = new MyBroadcast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.train.test");
		registerReceiver(mybroadcast, filter);  //注册Broadcast Receiver,接受应用开启的信号
		try {
			isSet = false;

//			FileOutputStream fos = new FileOutputStream(configureFilePath);
//			FileInputStream fis = new FileInputStream(configureFilePath);
			configureObject.loadFromXML(getApplicationContext().openFileInput(configureFilePath));
//			configureObject.loadFromXML(fis);
			TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			configureObject.setProperty(getString(R.string.property_imei), tm.getDeviceId());
			configureObject.storeToXML(getApplicationContext().openFileOutput(configureFilePath, ContextThemeWrapper.MODE_PRIVATE), null);
			String fn = configureObject.getProperty(getString(R.string.property_file_number));


			if(fn.equals("null")){
//				Log.e("null","null");
				configureObject.setProperty(getString(R.string.property_file_number), "0");
				recordFile();
			}
			if(fn.equals("100")){
				lockButton.setVisibility(View.VISIBLE);//训练完按钮可见
//				trainButton.setClickable(false);//训练完就不让训练了
			}



			fileNumber = Integer.parseInt(configureObject.getProperty(getString(R.string.property_file_number)));
//			int f = (int)fileNumber;
			progressView.setText(fileNumber + "%");
//			progressView.setText(configureObject.getProperty(getString(R.string.property_file_number)) + "%");
//			Log.e("filenumber", String.valueOf(fileNumber));
//			Log.e("imei",configureObject.getProperty(getString(R.string.property_imei)));
			//configureObject.setProperty("TrainFinish","false");
			String pwhs = configureObject.getProperty(getString(R.string.password_property_name));
			//pwhs = "null";
//			Log.e("password", pwhs);
			if(pwhs.equals("null")){
//				Log.e("pwhs","22222222222222222222222222222222");
//				startActivity(new Intent(this, PasswordActivity.class));
				startActivityForResult(new Intent(this, PasswordActivity.class), SETPASSWORD);
			}

			else{
				isSet =true;
				stateView.setText(getString(R.string.app_state_ready));
			}
			if(isSet == true){
				if(configureObject.getProperty(getString(R.string.property_collect_finish)).equals("true")){
					isTrainFinished = true;

				}
				else{
					lockButton.setVisibility(View.INVISIBLE);

					isTrainFinished = false;
				}
			}
			isCollecting = false;
			isDetecting = false;

			startAndBindCollectDataService();
			startAndBindDetectService();

		}
		catch (IOException e){
			Log.e("Load Error", e.getMessage());
			configureObject.setProperty(getString(R.string.property_collect_finish), "false");
			configureObject.setProperty(getString(R.string.password_property_name),"null");
			configureObject.setProperty(getString(R.string.property_file_number),"null");
			TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			configureObject.setProperty(getString(R.string.property_imei),tm.getDeviceId());
			configureObject.setProperty(getString(R.string.property_collect_finish),"false");
//			startActivityForResult(new Intent(this,PasswordActivity.class),SETPASSWORD);
		}
		catch (Exception e){
			Log.e("Load Error", e.getMessage());
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mybroadcast);//取消注册
		super.onDestroy();
		if(detectService!= null){
			unbindService(detectDataServiceConnection);
			stopService(detectIntent);
		}
		if(collectDataService != null){
			unbindService(collectDataServiceConnection);
			stopService(collectIntent);
		}
		if(isTrainFinished){
			configureObject.setProperty(getString(R.string.property_collect_finish),"true");
		}
		try {
			configureObject.storeToXML(getApplicationContext().openFileOutput(configureFilePath, ContextThemeWrapper.MODE_PRIVATE), null);
		}catch(Exception e){
			Log.e("Destroy",e.getMessage());
		}
	}

	@Override
	protected void onActivityResult(int RequestCode, int ResultCode, Intent data){
		switch (RequestCode){
			case SETPASSWORD:
				if(RequestCode == RESULT_OK){
					isSet = true;
					stateView.setText(getString(R.string.app_state_ready));
				}
				break;
			default:
				break;
		}
	}

	private  void startAndBindCollectDataService(){
		collectDataServiceConnection = new ServiceConnection(){
			public void onServiceDisconnected(ComponentName componentName){
				collectDataBinder = null;
				collectDataService = null;
			}

			public void onServiceConnected(ComponentName componentName, IBinder ibinder){
				collectDataBinder = (CollectDataService.CollectDataBinder) ibinder;
				collectDataService = collectDataBinder.getService();
				Log.e("MainActivity-start","Finish initializing collect service");
			}
		};
		startService(collectIntent);
		bindService(new Intent(this,CollectDataService.class),collectDataServiceConnection, BIND_AUTO_CREATE);

	}

	private void startAndBindDetectService(){
		detectDataServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				detectBinder = (DetectService.DetectBinder) service;
				detectService = detectBinder.getService();
				Log.e("MainActivity-start","Finish initializing detect service");
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				detectBinder = null;
				detectService = null;
			}
		};
		startService(detectIntent);
		bindService(new Intent(this, DetectService.class), detectDataServiceConnection, BIND_AUTO_CREATE);
	}

	public void startPasswordSetting(View view){
		startActivityForResult(new Intent(this, PasswordActivity.class), SETPASSWORD);
	}

	public void onDetectButtonClick(View view){
		if(isSet == false){
			startPasswordSetting(view);
		}
		if(isDetecting){
			detectService.pauseDetectService();
			collectDataService.pauseCollect();
			DetectTimer.cancel();
			isDetecting = false;
			isCollecting = false;
			stateView.setText(getString(R.string.app_state_ready));
		}
		else{
			if(detectService == null) {
				//TODO: generate a dialog to let user wait for a while
				return;
			}

			collectDataService.isFinish = true;//collect 150
//			collectDataService.collect();
			startDetectTimer();

//			detectService.startDetectService();//检测线程
			isCollecting = true;
			isDetecting = true;
			stateView.setText(getString(R.string.app_state_protecting));
		}

	}

	public void onCollectButtonClick(View view){

		if(isSet == false){
			startPasswordSetting(view);
		}
//		trainButton.setClickable(false);//开始搜集不可点击，搜集完释放
		if(isTrainFinished){
			isTrainFinished = false;
			if(isDetecting){
				detectService.pauseDetectService();
				isDetecting = false;
			}
			if(isCollecting){
				collectDataService.pauseCollect();
			}
			collectDataService.isFinish = false;
			collectDataService.collect();
			CollectTimer.cancel();
			startCollectTimer();
			isCollecting = true;
			stateView.setText(getString(R.string.app_state_collecting));
		}
		else {
			if (isCollecting) {
				collectDataService.pauseCollect();
				CollectTimer.cancel();
				isCollecting = false;
				stateView.setText(getString(R.string.app_state_ready));
				CollectTimer.cancel();
			} else {
				if (collectDataService == null) {
					//TODO: generate a dialog to let user wait for a while
					return;
				}
				collectDataService.collect();
				startCollectTimer();
				isCollecting = true;
				stateView.setText(getString(R.string.app_state_collecting));
			}
		}
	}

	private void startCollectTimer(){
		CollectTimer = new Timer();
		CollectTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				int res = collectDataService.checkProcess();
				dataNumber = res;
				Message m = new Message();
				m.what = MESSAGE_UPDATE_NUMBER;
				messageHandler.sendMessage(m);
//				Log.e("startCollectTimer","run");
				if(dataNumber == 150){
					m = new Message();
					m.what = MESSAGE_CHANGE_STATE;
					messageHandler.sendMessage(m);
					isCollecting = false;
					CollectTimer.cancel();
				}
				if(collectDataService.isFinish){

					isTrainFinished = true;
					CollectTimer.cancel();
					isCollecting = false;
					m = new Message();
					m.what = MESSAGE_CHANGE_STATE;
					messageHandler.sendMessage(m);
					m = new Message();
					m.what = MESSAGE_SET_INVISIBLE;
					messageHandler.sendMessage(m);
				}
				//一开始train时候的显示切换（ready or collecting）
//				if(collectDataService.uploadTrain){
//					isTrainFinished = true;
//					CollectTimer.cancel();
//					isCollecting = false;
//					collectDataService.uploadTrain = false;
//					m = new Message();
//					m.what = MESSAGE_CHANGE_STATE;
//					messageHandler.sendMessage(m);
//				}
			}
		},1000,1000);
	}

	/**
	 * 只检测一次，搜集数据每次为3秒钟
	 */
	private void startDetectTimer(){
		DetectTimer = new Timer();
		DetectTimer.schedule(new TimerTask() {
			@Override
			public void run() {

				try{


					collectDataService.collect();

					DetectTimer.cancel();
//					Log.e("startDetectTimer","cancel");
				}catch (Exception e) {
					Log.e("DetectTimer", e.getMessage());
					DetectTimer.cancel();

				}

			}
		},0,5000);
	}

	/**
	 * 停止检测，主要针对用户对话框选择不让检测的情况
	 */
	public void stopDetectTimer(){
		DetectTimer.cancel();
	}

	//记录
	public void recordFile(){
		try {
			configureObject.storeToXML(getApplicationContext().openFileOutput(configureFilePath, ContextThemeWrapper.MODE_PRIVATE), null);
		}catch(Exception e){
			Log.e("recordFile",e.getMessage());
		}
	}



	public class MyBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String state = intent.getStringExtra("state");
			if(state.equals("train")){
				Message m = new Message();
				m.what = MESSAGE_HANDLE_TRAIN;
				messageHandler.sendMessage(m);
			}
			if(state.equals("test")){
				Message m = new Message();
				m.what = MESSAGE_HANDLE_TEST;
				messageHandler.sendMessage(m);
			}
			if(state.equals("alarm")){
				Message m = new Message();
				m.what = MESSAGE_HANDLE_ALARM;
				messageHandler.sendMessage(m);
			}
		}
	}






}
