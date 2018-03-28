package com.thewalkingschoolbus.thewalkingschoolbus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.thewalkingschoolbus.thewalkingschoolbus.Interface.OnTaskComplete;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.User;
import com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask;

import static com.thewalkingschoolbus.thewalkingschoolbus.InitialActivity.LOGIN_STATES;
import static com.thewalkingschoolbus.thewalkingschoolbus.InitialActivity.REGISTER_EMAIL;

import static com.thewalkingschoolbus.thewalkingschoolbus.RegisterActivity.PLEASE_CORRECT_YOUR_DATE_OF_BIRTH;
import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.CREATE_USER;
import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.EDIT_USER;

/**
 * Created by Vaanyi Igiri on 2018-03-19.
 */

public class UserProfileActivity extends AppCompatActivity {

    public static final String EDIT_SUCCESSFULLY_MESSAGE = "edit successfully";
    private EditText nameET, emailET,
            birthYearET, birthMonthET, addressET,
            cellPhoneET, homePhoneET, gradeET,
            teacherNameDT, emergencyContactInfoET;
    private String loginName, registerEmail, registerPassword,
            birthYear, birthMonth, address, cellPhone, homePhone,
            grade, teacherName, emergencyContactInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        setupTextviews();
        setupSavebutton();
    }

    private void setupSavebutton() {
        Button saveButton = (Button)findViewById(R.id.button4);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginName = nameET.getText().toString();
                registerEmail = emailET.getText().toString();
                birthYear = birthYearET.getText().toString();
                birthMonth = birthMonthET.getText().toString();
                address = addressET.getText().toString();
                cellPhone = cellPhoneET.getText().toString();
                homePhone = homePhoneET.getText().toString();
                grade = gradeET.getText().toString();
                teacherName = teacherNameDT.getText().toString();
                emergencyContactInfo = emergencyContactInfoET.getText().toString();

                if(loginName.isEmpty() || registerEmail.isEmpty()){
                    Toast.makeText(getApplicationContext(),MainActivity.USERNAME_EMAIL_AND_PASSWORD_REQUIRED_EMPTY_MESSAGE, Toast.LENGTH_SHORT)
                            .show();
                } else if(!birthMonth.isEmpty()
                        && ((Integer.parseInt(birthMonth) > 12 || Integer.parseInt(birthMonth) < 0))){
                    Toast.makeText(getApplicationContext(), PLEASE_CORRECT_YOUR_DATE_OF_BIRTH, Toast.LENGTH_SHORT)
                            .show();
                } else if(!birthYear.isEmpty()
                        && (Integer.parseInt(birthYear)>2018 || Integer.parseInt(birthYear)<1900)) {
                    Toast.makeText(getApplicationContext(), PLEASE_CORRECT_YOUR_DATE_OF_BIRTH, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    final User user = new User();
                    user.setId(User.getLoginUser().getId());
                    user.setEmail(registerEmail);
                    user.setName(loginName);
                    user.setPassword(registerPassword);
                    user.setBirthYear(birthYear);
                    user.setBirthMonth(birthMonth);
                    user.setAddress(address);
                    user.setCellPhone(cellPhone);
                    user.setHomePhone(homePhone);
                    user.setGrade(grade);
                    user.setTeacherName(teacherName);
                    user.setEmergencyContactInfo(emergencyContactInfo);

                    new GetUserAsyncTask(EDIT_USER, user,null, null,null ,new OnTaskComplete() {
                        @Override
                        public void onSuccess(Object result) {
                            Toast.makeText(getApplicationContext(), EDIT_SUCCESSFULLY_MESSAGE, Toast.LENGTH_SHORT)
                                    .show();
                            User.getLoginUser( ).setEmail(registerEmail);
                            storeUserInfoToSharePreferences();
                            finish();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            /*
                            Toast.makeText(getApplicationContext(),"edit failed", Toast.LENGTH_SHORT)
                                    .show();*/
                            Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }).execute();
                }
            }
        });
    }

    private void storeUserInfoToSharePreferences() {
        SharedPreferences preferences = getSharedPreferences(LOGIN_STATES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(REGISTER_EMAIL, User.getLoginUser().getEmail());
        editor.apply();
    }

    private void setupTextviews() {
        nameET = (EditText)findViewById(R.id.enter_fullnameid);
        emailET = (EditText)findViewById(R.id.enter_emailid);
        birthYearET = (EditText) findViewById(R.id.birthdayYearid);
        birthMonthET = (EditText) findViewById(R.id.enter_birth_month);
        addressET = (EditText) findViewById(R.id.enter_address);
        cellPhoneET = (EditText) findViewById(R.id.enter_phoneid);
        homePhoneET = (EditText) findViewById(R.id.enter_homephone_number);
        gradeET = (EditText) findViewById(R.id.enter_current_grade);
        teacherNameDT = (EditText) findViewById(R.id.enter_teacher_name);
        emergencyContactInfoET = (EditText) findViewById(R.id.emergency_contact_info);

        nameET.setText(User.getLoginUser().getName());
        emailET.setText(User.getLoginUser().getEmail());
        birthYearET.setText(User.getLoginUser().getBirthYear());
        birthMonthET.setText(User.getLoginUser().getBirthMonth());
        addressET.setText(User.getLoginUser().getAddress());
        cellPhoneET.setText(User.getLoginUser().getCellPhone());
        homePhoneET.setText(User.getLoginUser().getHomePhone());
        gradeET.setText(User.getLoginUser().getGrade());
        teacherNameDT.setText(User.getLoginUser().getTeacherName());;
        emergencyContactInfoET.setText(User.getLoginUser().getEmergencyContactInfo());;





    }


    public static Intent makeIntent(Context context) {
        return new Intent(context, UserProfileActivity.class);
    }
}