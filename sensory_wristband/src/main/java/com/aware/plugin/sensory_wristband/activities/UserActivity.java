package com.aware.plugin.sensory_wristband.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.aware.plugin.sensory_wristband.R;

public class UserActivity extends AppCompatActivity {

    private static final int MEN = 1;
    private static final int WOMEN = 0;
    private static final int UNKNOWN = -1;
    private static final String NAME = "Name";
    private static final String AGE = "Age";
    private static final String HEIGHT = "Height";
    private static final String WEIGHT = "Weight";
    private static final String SEX = "Sex";

    private EditText aliasEditText;
    private EditText ageEditText;
    private EditText heightEditText;
    private EditText weightEditText;
    private RadioButton menRadioButton;
    private RadioButton womenRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        getFields();

        findViewById(R.id.saveUsrDataButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validate if fields are not empty
                if(validateFields()){
                    //Create new intent and open main activity
                    Intent intent = new Intent();
                    intent.putExtra("device",getIntent().getParcelableExtra("device"));
                    intent.putExtra("sex",getSex());
                    intent.putExtra("age",Integer.parseInt(ageEditText.getText().toString()));
                    intent.putExtra("height",Integer.parseInt(heightEditText.getText().toString()));
                    intent.putExtra("weight",Integer.parseInt(weightEditText.getText().toString()));
                    intent.putExtra("alias",aliasEditText.getText().toString());
                    intent.setClass(UserActivity.this, MainActivity.class);
                    UserActivity.this.startActivity(intent);
                    UserActivity.this.finish();
                }
            }
        });

    }

    /**
     * Get fields form view and set to variables
     */
    private void getFields(){
        aliasEditText = (EditText) findViewById(R.id.aliasEditText);
        ageEditText = (EditText) findViewById(R.id.ageEditText);
        heightEditText = (EditText) findViewById(R.id.heightEditText);
        weightEditText = (EditText) findViewById(R.id.weightEditText);
        menRadioButton = (RadioButton) findViewById(R.id.menRadioButton);
        womenRadioButton = (RadioButton) findViewById(R.id.womenRadioButton);
    }

    /**
     * Check if fields are not empty
     * @return true - not empty | false - empty
     */
    private boolean validateFields(){
        if(aliasEditText.getText().length() == 0){
            showAlertEmptyField(NAME);
            return false;
        }
        if(ageEditText.getText().length() == 0){
            showAlertEmptyField(AGE);
            return false;
        }
        if(heightEditText.getText().length() == 0){
            showAlertEmptyField(HEIGHT);
            return false;
        }
        if(weightEditText.getText().length() == 0){
            showAlertEmptyField(WEIGHT);
            return false;
        }
        if(!menRadioButton.isChecked() && !womenRadioButton.isChecked()){
            showAlertEmptyField(SEX);
            return false;
        }
        return true;
    }

    /**
     * Show alert that field is empty
     * @param fieldName - name of empty field
     */
    private void showAlertEmptyField(String fieldName){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Empty field");
        builder.setMessage("Fill " + fieldName + " field.");
        builder.setPositiveButton(android.R.string.ok,null);
        builder.show();
    }

    /**
     * Get sex
     */
    private int getSex(){
        if(menRadioButton.isChecked()){
            return MEN;
        } else if(womenRadioButton.isChecked()){
            return WOMEN;
        }
        return UNKNOWN;
    }
}
