package com.sensordemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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

import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends Activity {
	

	private TextView stateView;
	private TextView numberView;
	private ImageView trainButton;
	private ImageView lockButton;

	private int dataNumber;

	private CollectDataService.CollectDataBinder collectDataBinder;
	private CollectDataService collectDataService;
	private ServiceConnection collectDataServiceConnection;
	private boolean isCollecting;
	private Timer CollectTimer;

	private DetectService.DetectBinder detectBinder;
	private DetectService detectService;
	private ServiceConnection detectDataServiceConnection;
	private boolean isDetecting;

	private boolean isTrainFinished;

	public static final String configureFilePath = "config.xml";

	public static Properties configureObject;

	public static final int RESULT_OK = 1;
	public static final int RESULT_WRONG = -1;

	public static final int SETPASSWORD = 1;

	final static int MESSAGE_UPDATE_NUMBER = 1;
	final static int MESSAGE_CHANGE_STATE =2;
	final static int MESSAGE_SET_INVISIBLE =3;


	public static boolean isSet;

	private Intent detectIntent;
	private Intent collectIntent;

	public Handler messageHandler;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		stateView = (TextView) findViewById (R.id.state);
	
		numberView = (TextView) findViewById (R.id.number);
		trainButton = (ImageView) findViewById (R.id.trainButton);
		lockButton = (ImageView) findViewById(R.id.lockButton);

		stateView.setText(getString(R.string.app_state_ini));
		numberView.setText("0");

		isCollecting = false;
		isDetecting = false;
		CollectTimer = new Timer();

		detectIntent = new Intent(this, DetectService.class);
		collectIntent = new Intent(this,CollectDataService.class);

		messageHandler = new Handler(){
			public void handleMessage(Message m){
				switch(m.what){
					case MESSAGE_UPDATE_NUMBER:
						numberView.setText(String.valueOf(dataNumber));
						break;
					case MESSAGE_CHANGE_STATE:
						stateView.setText(getString(R.string.app_state_ready));
						break;
					case MESSAGE_SET_INVISIBLE:
						lockButton.setVisibility(View.VISIBLE);
						break;
					default:
						break;
				}
			}
		};

		configureObject = new Properties();
		try {
			isSet = false;

			configureObject.loadFromXML(getApplicationContext().openFileInput(configureFilePath));
			TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			configureObject.setProperty(getString(R.string.property_imei),tm.getDeviceId());
			//configureObject.setProperty("TrainFinish","false");
			String pwhs = configureObject.getProperty(getString(R.string.password_property_name));
			//pwhs = "null";
			if(pwhs.equals("null")){
				startActivityForResult(new Intent(this,PasswordActivity.class),SETPASSWORD);
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
			Log.e("Load Error",e.getMessage());
			configureObject.setProperty(getString(R.string.property_collect_finish),"false");
			configureObject.setProperty(getString(R.string.password_property_name),"null");
			TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			configureObject.setProperty(getString(R.string.property_imei),tm.getDeviceId());
			configureObject.setProperty(getString(R.string.property_collect_finish),"false");
			startActivityForResult(new Intent(this,PasswordActivity.class),SETPASSWORD);
		}
		catch (Exception e){
			Log.e("Load Error", e.getMessage());
		}
	}

	@Override
	protected void onDestroy() {
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
				Log.d("MainActivity-start","Finish initializing collect service");
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
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				detectBinder = null;
				detectService = null;
			}
		};
		startService(detectIntent);
		bindService(new Intent(this,DetectService.class),detectDataServiceConnection,BIND_AUTO_CREATE);
	}

	public void startPasswordSetting(View view){
		startActivityForResult(new Intent(this,PasswordActivity.class),SETPASSWORD);
	}

	public void onDetectButtonClick(View view){
		if(isSet == false){
			startPasswordSetting(view);
		}
		if(isDetecting){
			detectService.pauseDetectService();
			collectDataService.pauseCollect();
			isDetecting = false;
			isCollecting = false;
			stateView.setText(getString(R.string.app_state_ready));
		}
		else{
			if(detectService == null) {
				//TODO: generate a dialog to let user wait for a while
				return;
			}
			detectService.startDetectService();
			collectDataService.isFinish = true;
			collectDataService.collect();
			isCollecting = true;
			isDetecting = true;
			stateView.setText(getString(R.string.app_state_protecting));
		}

	}

	public void onCollectButtonClick(View view){
		if(isSet == false){
			startPasswordSetting(view);
		}
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
			}
		},1000,1000);
	}
}
