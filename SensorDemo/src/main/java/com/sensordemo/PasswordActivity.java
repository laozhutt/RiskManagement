package com.sensordemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import com.sensordemo.utils.CustomizedDialog;

public class PasswordActivity extends Activity {

    private TextView Title;
    private TextView FirstLabel, SecondLabel, ThirdLabel;
    private EditText FirstInput, SecondInput, ThirdInput;
    private Button ConfirmButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        Title = (TextView) findViewById(R.id.PasswordTitle);
        FirstInput = (EditText) findViewById(R.id.PasswordInput1);
        SecondInput = (EditText) findViewById(R.id.PasswordInput2);
        ThirdInput = (EditText) findViewById(R.id.PasswordInput3);
        FirstLabel = (TextView) findViewById(R.id.PasswordLabel1);
        SecondLabel = (TextView) findViewById(R.id.PasswordLabel2);
        ThirdLabel = (TextView) findViewById(R.id.PasswordLabel3);
        ConfirmButton = (Button) findViewById(R.id.PasswordConfirmButton);

        if(MainActivity.isSet == false){
            Title.setText(getString(R.string.password_title_set));
            FirstLabel.setText(getString(R.string.password_label_enter_new));
            SecondLabel.setText(getString(R.string.password_label_reenter_new));
            ThirdLabel.setVisibility(View.INVISIBLE);
            ThirdInput.setVisibility(View.INVISIBLE);
        }
        else {
            Title.setText(getString(R.string.password_title_change));
            FirstLabel.setText(getString(R.string.password_label_enter_old));
            SecondLabel.setText(getString(R.string.password_label_enter_new));
            ThirdLabel.setText(getString(R.string.password_label_reenter_new));
        }
        Log.d("password",MainActivity.configureObject.getProperty(getString(R.string.password_property_name)));
    }

    public void onConfirmButtonClick(View view){
        if(MainActivity.isSet == true) {
            if(authenticatePassword(FirstInput.getText().toString())){
                String newPassword = ThirdInput.getText().toString();
                if(SecondInput.getText().toString().equals(newPassword)){
                    if(updatePassword(newPassword)){
                        setResult(MainActivity.RESULT_OK);
                        CustomizedDialog.createSimpleDialog(this,"Success","New Password Set Successfully",true);
                    }
                    else{
                        CustomizedDialog.createSimpleDialog(this,"Error","Password Setting Failed",false);
                    }
                }else{
                    CustomizedDialog.createSimpleDialog(this,"Error","New Passwords mismatched",false);
                }
            }
            else{
                CustomizedDialog.createSimpleDialog(this,"Error","Old Password Wrong",false);
            }
        }
        else{
            String newPassword = SecondInput.getText().toString();
            if(FirstInput.getText().toString().equals(newPassword)){
                if(updatePassword(newPassword)){
                    MainActivity.isSet = true;
                    CustomizedDialog.createSimpleDialog(this,"Success","New Password Set Successfully",true);
                }
                else{
                    CustomizedDialog.createSimpleDialog(this,"Error","Password Setting Failed",false);
                }
            }else{
                CustomizedDialog.createSimpleDialog(this,"Error","New Passwords mismatched",false);
            }
        }
    }

    private boolean updatePassword(String input){
        String oldPassword = MainActivity.configureObject.getProperty(getString(R.string.password_property_name));
        MainActivity.configureObject.setProperty(getString(R.string.password_property_name),String.valueOf(input.hashCode()));
        Log.d("Input Hash",input+" "+String.valueOf(input.hashCode()));
        try {
            MainActivity.configureObject.storeToXML(getApplicationContext().openFileOutput(MainActivity.configureFilePath, ContextThemeWrapper.MODE_PRIVATE),null);
        }
        catch(Exception e){
            Log.d("PasswordActivity", e.getMessage());
            MainActivity.configureObject.setProperty(getString(R.string.password_property_name), oldPassword);
            return false;
        }
        return true;
    }

    private boolean authenticatePassword(String input){
        if(MainActivity.configureObject == null){
            Log.d("PasswordActivity","Cannot load configureProperties");
        }
        String hash = MainActivity.configureObject.getProperty(getString(R.string.password_property_name));
        if(String.valueOf(input.hashCode()).equals(hash)){
            return true;
        }
        else
            return false;
    }
}
