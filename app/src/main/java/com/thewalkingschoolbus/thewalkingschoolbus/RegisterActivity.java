package com.thewalkingschoolbus.thewalkingschoolbus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.thewalkingschoolbus.thewalkingschoolbus.Interface.OnTaskComplete;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.User;
import com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask;

import static com.thewalkingschoolbus.thewalkingschoolbus.InitialActivity.LOGIN_PASSWORD;
import static com.thewalkingschoolbus.thewalkingschoolbus.InitialActivity.LOGIN_STATES;
import static com.thewalkingschoolbus.thewalkingschoolbus.InitialActivity.REGISTER_EMAIL;
import static com.thewalkingschoolbus.thewalkingschoolbus.MainActivity.*;
import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.*;

public class RegisterActivity extends AppCompatActivity {

    public static final String PLEASE_CORRECT_YOUR_DATE_OF_BIRTH = "Please correct your date of birth";
    private EditText nameET, emailET, passwordET,
            birthYearET, birthMonthET, addressET,
            cellPhoneET, homePhoneET, gradeET,
            teacherNameDT, emergencyContactInfoET;

    private String loginName, registerEmail, registerPassword,
            birthYear, birthMonth, address, cellPhone, homePhone,
            grade, teacherName, emergencyContactInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setupTextviews();
        setupRegisterButton();
    }

    private void setupRegisterButton() {
        Button registerBtn = (Button)findViewById(R.id.register2id);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginName = nameET.getText().toString();
                registerEmail = emailET.getText().toString();
                registerPassword = passwordET.getText().toString();
                birthYear = birthYearET.getText().toString();
                birthMonth = birthMonthET.getText().toString();
                address = addressET.getText().toString();
                cellPhone = cellPhoneET.getText().toString();
                homePhone = homePhoneET.getText().toString();
                grade = gradeET.getText().toString();
                teacherName = teacherNameDT.getText().toString();
                emergencyContactInfo = emergencyContactInfoET.getText().toString();



                if(loginName.isEmpty() || registerEmail.isEmpty()|| registerPassword.isEmpty()){
                    Toast.makeText(getApplicationContext(),MainActivity.USERNAME_EMAIL_AND_PASSWORD_REQUIRED_EMPTY_MESSAGE, Toast.LENGTH_SHORT)
                            .show();
                } else if(!birthMonth.isEmpty()
                        && ((Integer.parseInt(birthMonth) > 12 || Integer.parseInt(birthMonth) < 0))){
                    Toast.makeText(getApplicationContext(), PLEASE_CORRECT_YOUR_DATE_OF_BIRTH, Toast.LENGTH_SHORT)
                            .show();
                } else if(!birthYear.isEmpty()
                        && (Integer.parseInt(birthYear) > 2018 || Integer.parseInt(birthYear) < 1900)) {
                    Toast.makeText(getApplicationContext(), PLEASE_CORRECT_YOUR_DATE_OF_BIRTH, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    User user = new User();
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


                new GetUserAsyncTask(CREATE_USER, user,null, null,null ,new OnTaskComplete() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(getApplicationContext(),REGISTER_SUCCESSFULLY_MESSAGE, Toast.LENGTH_SHORT)
                                    .show();
                            storeUserInfoToSharePreferences();

                            User.setLoginUser(new User());
                            User.getLoginUser().setId(((User)result).getId());
                            User.getLoginUser().setEmail(registerEmail);
                            User.getLoginUser().setEmail(registerPassword);

                            Intent intent = MainMenuActivity.makeIntent(getApplicationContext());
                            startActivity(intent);
                            finish();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getApplicationContext(),REGISTER_FAIL_MESSAGE, Toast.LENGTH_SHORT)
                                    .show();
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

        editor.putString(REGISTER_EMAIL, registerEmail );
        editor.putString(LOGIN_PASSWORD, registerPassword );
        editor.apply();
    }

    private void setupTextviews() {
        nameET = (EditText)findViewById(R.id.registernameid);
        emailET = (EditText)findViewById(R.id.registeremailid);
        passwordET = (EditText) findViewById(R.id.registerpasswordid);
        birthYearET = (EditText) findViewById(R.id.yearid);
        birthMonthET = (EditText) findViewById(R.id.monthid);
        addressET = (EditText) findViewById(R.id.addressid);
        cellPhoneET = (EditText) findViewById(R.id.cellphoneid);
        homePhoneET = (EditText) findViewById(R.id.homePhoneid);
        gradeET = (EditText) findViewById(R.id.gradeid);
        teacherNameDT = (EditText) findViewById(R.id.teacherNameid);
        emergencyContactInfoET = (EditText) findViewById(R.id.emergencyid);

    }

    public static Intent makeIntent(MainActivity mainActivity) {
        return new Intent(mainActivity, RegisterActivity.class);
    }
}
