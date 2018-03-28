package com.thewalkingschoolbus.thewalkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.thewalkingschoolbus.thewalkingschoolbus.Interface.OnTaskComplete;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.User;
import com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask;

import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.GET_USER_BY_EMAIL;
import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.LOGIN_REQUEST;

public class InitialActivity extends AppCompatActivity {
    public static final String LOGIN_STATES = "LOGIN_STATES";
    public static final String REGISTER_EMAIL = "registerEmail";
    public static final String LOGIN_PASSWORD = "loginPassword";
    private String email;
    private String password;
    private User attemptLoginUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        if(checkSharePreferences() == false){
            startLogin();
        }else{
            startMainMenu();
        }
    }
    private boolean checkSharePreferences(){
        SharedPreferences preferences = getApplication().getSharedPreferences(LOGIN_STATES, MODE_PRIVATE);
        email = preferences.getString(REGISTER_EMAIL, null);
        password = preferences.getString(LOGIN_PASSWORD, null);

        if(email == null || password == null){
            return false;
        }

        return true;
    }

    private void startLogin(){
        Intent intent = MainActivity.makeIntent(this);
        startActivity(intent);
        finish();
    }

    private void startMainMenu(){
        attemptLoginUser = new User();
        attemptLoginUser.setPassword(password);
        attemptLoginUser.setEmail(email);
        new GetUserAsyncTask(LOGIN_REQUEST, attemptLoginUser, null, null, null, new OnTaskComplete() {
            @Override
            public void onSuccess(Object result) {
                setAsLoginUser();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(InitialActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).execute();
    }

    private void setAsLoginUser(){
        final Context self = this;
        new GetUserAsyncTask(GET_USER_BY_EMAIL, attemptLoginUser, null, null, null, new OnTaskComplete() {
            @Override
            public void onSuccess(Object result) {

                User user = (User) result;
                User.setLoginUser(new User());
                User.getLoginUser().setId(user.getId());
                User.getLoginUser().setEmail(user.getEmail());
                User.getLoginUser().setPassword(user.getPassword());

                storeUserInfoToSharePreferences();
                Intent intent = MainMenuActivity.makeIntent(self);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(InitialActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).execute();
    }

    private void storeUserInfoToSharePreferences() {
        SharedPreferences preferences = getSharedPreferences(LOGIN_STATES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(REGISTER_EMAIL, email );
        editor.putString(LOGIN_PASSWORD, password );
        editor.commit();

    }

}
