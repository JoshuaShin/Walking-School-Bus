package com.thewalkingschoolbus.thewalkingschoolbus.api_binding;

import android.util.Log;

import com.thewalkingschoolbus.thewalkingschoolbus.Models.ApiException;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.GpsLocation;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.Group;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.Message;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.User;

import com.google.gson.Gson;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerManager {

    private String GET = "GET";
    private String POST = "POST";
    private String DELETE = "DELETE";

    private String API_KEY = "BB390E20-F40E-40D1-BE2D-2F99AAF8E449"; //api key for flame group
    // Debug: Proxy server for plaintext debugging purpose
    // private String BASE_URL = "http://walkgroup.api.tabjy.com/https://cmpt276-1177-bf.cmpt.sfu.ca:8443";
    private String BASE_URL="https://cmpt276-1177-bf.cmpt.sfu.ca:8443";

    private String LOGIN = "/login";
    private String CREATE_USER = "/users/signup";
    private String LIST_USERS = "/users";
    private String GET_USER_BY_ID = "/users/%s";
    private String GET_USER_BY_EMAIL = "/users/byEmail?email=%s";
    private String DELETE_USER = "/users/%s";
    private String EDIT_USER = "/users/%s";
    private String GET_GPS_LOCATION = "/users/%s/lastGpsLocation";
    private String POST_GET_LOCATION = "/users/%s/lastGpsLocation";

    private String USER_MONITORING_LIST = "/users/%s/monitorsUsers";
    private String USER_MONITORING_BY_LIST = "/users/%s/monitoredByUsers";
    private String CREATE_MONITORING = "/users/%s/monitorsUsers";
    private String DELETE_MONITORING = "/users/%s/monitorsUsers/%s";

    private String LIST_GROUPS = "/groups";
    private String CREATE_GROUP = "/groups";
    private String GET_ONE_GROUP = "/groups/%s";
    private String UPDATE_EXISTING_GROUP = "/groups/%s";
    private String DELETE_GROUP = "/groups/%s";
    private String GET_MEMBERS_OF_GROUP = "/groups/%s/memberUsers";
    private String ADD_MEMBERS_TO_GROUP = "/groups/%s/memberUsers";
    private String REMOVE_MEMBER_OF_GROUP = "/groups/%s/memberUsers/%s";

    private String GET_ALL_MESSAGES = "/messages";
    private String GET_ALL_EMERGENCY_MESSAGES = "/messages?is-emergency=true";
    private String GET_MESSAGES_FOR_GROUP = "/messages?togroup=%s";
    private String GET_EMERGENCY_MESSAGES_FOR_GROUP = "/messages?togroup=%s&is-emergency=true";
    private String GET_MESSAGES_FOR_USER = "/messages?foruser=%s";
    private String GET_UNREAD_MESSAGES_FOR_USER = "/messages?foruser=%s&status=unread";
    private String GET_READ_MESSAGES_FOR_USER = "/messages?foruser=%s&status=read";
    private String GET_UNREAD_EMERGENCY_MESSAGES_FOR_USER = "/messages?foruser=%s&status=unread&is-emergency=true";

    private String POST_MESSAGE_TO_GROUP = "/messages/togroup/%s";
    private String POST_MESSAGE_TO_PARENTS = "/messages/toparentsof/%s";
    private String GET_ONE_MESSAGE = "/messages/%s";
    private String SET_MESSAGE_AS_READ_OR_UNREAD =  "/messages/%s/readby/%s";


    private String SUCCESSFUL = "SUCCESSFUL";

    // for any type of post request, this does the initial connection and sending json file
    // Such as: create User, create Group, create monitoring
    // return httpURLconnection which is used to getResponseCode from server side
    private HttpURLConnection httpRequestPost(String url, JSONObject jsonObject)throws Exception{
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod(POST);
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("apiKey",API_KEY);

        if(User.getToken()!= null){
            connection.setRequestProperty("Authorization", User.getToken());
        }

        if(jsonObject != null){
            connection.setDoOutput(true);
            PrintStream printStream = new PrintStream(connection.getOutputStream());
            printStream.println(jsonObject .toString());
            printStream.close();
        }
        return connection;
    }

    // for any type of Get request, this does the initial connection and sending json file(for login)
    // Such as: login User, list Group, list monitoring
    // return httpURLconnection which is used to getResponseCode from server side
    private HttpURLConnection httpRequestGet(String url,JSONObject jsonObject)throws Exception{
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod(GET);
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("apiKey",API_KEY);

        if(User.getToken()!= null){
            connection.setRequestProperty("Authorization", User.getToken());
        }

        if(jsonObject != null){
            connection.setDoOutput(true);
            PrintStream printStream = new PrintStream(connection.getOutputStream());
            printStream.println(jsonObject .toString());
            printStream.close();
        }
        return connection;
    }

    // for any type of Delete request, this does the initial connection
    // Such as: login User, list Group, list monitoring
    // return httpURLconnection which is used to getResponseCode from server side
    private HttpURLConnection httpRequestDelete(String url)throws Exception{
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        //connection.setDoOutput(true);
        connection.setRequestMethod(DELETE);
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("apiKey",API_KEY);

        if(User.getToken()!= null){
            connection.setRequestProperty("Authorization", User.getToken());
        }

        return connection;
    }

    // turns json file in to string.. as name suggest return a StringBuffer well organize
    // preparation to map Json file into each object
    private StringBuffer readJsonIntoString (HttpURLConnection connection) throws Exception{
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        StringBuffer response = new StringBuffer();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response;
    }

    // takes loginRequest takes parentUser(as the user that want to login) and enter password as parameters
    // return null for login error:
    //      wrong password/email, server connection error..
    // return SUCCESSFUL if server approve login
    public String loginRequest(User user,String enteredPassword)throws Exception, ApiException{
        String url = BASE_URL+LOGIN;
        // create json file here
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email",user.getEmail());
        if(enteredPassword == null){
            jsonObject.put("password",user.getPassword());
        }else {
            jsonObject.put("password", enteredPassword);
        }

        HttpURLConnection connection = httpRequestGet(url,jsonObject);

        int responseCode = connection.getResponseCode();

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }
        // save token
        String token = connection.getHeaderField("authorization");

        User.setToken("Bearer "+token);
        return SUCCESSFUL;
    }

    // take parentUser and enteredPassword ass parameters
    // return null if server returns error:
    //      user already exist ...
    // return user object for with server generated ID
    public User createUser(User user)throws Exception, ApiException{

        String url = BASE_URL+CREATE_USER;

        String jsonFile = new Gson().toJson(user);
        JSONObject jsonObject = new JSONObject(jsonFile);

        HttpURLConnection connection = httpRequestPost(url,jsonObject);
        // send json file

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        // read json file and save in User
        StringBuffer response = readJsonIntoString(connection);
        user = new Gson().fromJson(response.toString(),User.class);
        return user;

    }

    // Does not need a parameters
    // return null if server returns error:
    //      problem with token, server connection issues
    // return array of user object on approve
    //      monitoring stuff, group stuff for every user
    //      in every user's list of monitoring and monitoring by only conntain user ID
    public User[] listUsers()throws Exception {
        String url = BASE_URL+LIST_USERS;
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        User[] allUsers = new Gson().fromJson(response.toString(),User[].class);
        return allUsers;
    }

    // takes parentUser with id that want to be found
    // return null if server returns error:
    //      user not found, connection ...
    // return User Object in detail
    //      monitoring stuff, group stuff for this user
    //      in the list of monitoring user only contain IDs
    public User getUserById(User user) throws Exception {

        String url = BASE_URL+String.format(GET_USER_BY_ID,user.getId());
        HttpURLConnection connection = httpRequestGet(url,null);
        connection.setRequestProperty("JSON-DEPTH","2");

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        user = new Gson().fromJson(response.toString(),User.class);
        return user;
    }

    // takes parentUser with email that want to be found
    // return null if server returns error:
    //      user not found, connection ...
    // return User Object in detail
    //      monitoring stuff, group stuff for this user
    //      in the list of monitoring user only contain IDs
    public User getUserByEmail(User user) throws Exception{
        String url = BASE_URL + String.format(GET_USER_BY_EMAIL,user.getEmail());
        HttpURLConnection connection = httpRequestGet(url,null);
        connection.setRequestProperty("JSON-DEPTH","2");

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        user = new Gson().fromJson(response.toString(),User.class);
        return user;
    }

    // delete user with id
    // user parentUser as the user class and use their id to delete

    public String deleteUser(User user) throws Exception{
        String url = BASE_URL + String.format(DELETE_USER,user.getId());
        HttpURLConnection connection = httpRequestDelete(url);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        return SUCCESSFUL;
    }

    // find user using ID and update all user info
    // need to have all the info not just the updated ones
    // throw exception :
    //  user must have unique email addresses. Trying to change the current user to an email
    //  address which is already in use by another user will generate an error.
    public String editUser(User user)throws Exception{
        String url = BASE_URL + String.format(EDIT_USER,user.getId());
        String jsonFile = new Gson().toJson(user);
        JSONObject jsonObject = new JSONObject(jsonFile);
        HttpURLConnection connection = httpRequestPost(url,jsonObject);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        return SUCCESSFUL;
    }


    public GpsLocation getGpsLocation(User user)throws Exception{
        String url = BASE_URL + String.format(GET_GPS_LOCATION,user.getId());
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        return new Gson().fromJson(response.toString(), GpsLocation.class);
    }

    // The time format is sensitive to capitalization of the values. If the values are not correctly
    //capitalized, it returns an HTTP 400 status with no error
    public GpsLocation postGpsLocation(User user)throws Exception{
        String url = BASE_URL + String.format(POST_GET_LOCATION,user.getId());
        String jsonFile = new Gson().toJson(user.getLastGpsLocation());
        JSONObject jsonObject = new JSONObject(jsonFile);
        HttpURLConnection connection = httpRequestPost(url,jsonObject);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        return new Gson().fromJson(response.toString(), GpsLocation.class);

    }


    // take parentUser as the one that want to return list
    // throw exception : if server returns error
    //      id not found+ more
    // return a array of User if need can be convert to list
    public User[] userMonitoringList(User user) throws Exception {

        if(user.getId() == null) {
            user = getUserByEmail(user);
        }

        String url = BASE_URL+String.format(USER_MONITORING_LIST, user.getId());
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        User[] listMonitoring = new Gson().fromJson(response.toString(), User[].class);
        return listMonitoring;

    }

    // take parentUser as parameter
    // return null if server returns error
    //      id not found+ more
    // return a array of User if need can be convert to list
    public User[] userMonitoringByList(User user) throws Exception {
        if(user.getId() == null) {
            user = getUserByEmail(user);
        }
        String url = BASE_URL+String.format(USER_MONITORING_BY_LIST,user.getId());
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        User[] listMonitoringBy = new Gson().fromJson(response.toString(), User[].class);
        return listMonitoringBy;
    }

    // take both parentUser and childUser as parameters
    // return null if server return error
    //      parent not found, child not found, already exist(maybe)
    // return Arrary of User monitoring by parent User
    public User[] createMonitoring(User parentUser, User childUser) throws Exception {
        if(parentUser.getId() == null) {
            parentUser = getUserByEmail(parentUser);
        }
        if(childUser.getId() == null) {
            childUser = getUserByEmail(childUser);
        }
        String url = BASE_URL+String.format(CREATE_MONITORING,parentUser.getId());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",childUser.getId());
        HttpURLConnection connection = httpRequestPost(url,jsonObject);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        User [] listMonitoringByParentUser = new Gson().fromJson(response.toString(),User[].class);
        return listMonitoringByParentUser;
    }
    // take both parentUser and childUser as parameters
    // return null if server return error
    //      parent not found, child not found, child not in the list
    // return SUCCESSFUL if deleted
    public String deleteMonitoring (User parentUser, User childUser)throws Exception{

        if(parentUser.getId() == null) {
            parentUser = getUserByEmail(parentUser);
        }

        if(childUser.getId() == null) {
            childUser = getUserByEmail(childUser);
        }

        String url = BASE_URL+ String.format(DELETE_MONITORING, parentUser.getId(), childUser.getId());
        HttpURLConnection connection = httpRequestDelete(url);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        return SUCCESSFUL;
    }

    // take mGroup as parameter
    // return null
    //      authentication problem...
    // return Array of group object
    public Group[] listGroups()throws Exception{
        String url = BASE_URL+LIST_GROUPS;
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        Group [] groups = new Gson().fromJson(response.toString(),Group[].class);
        return groups;
    }

    // take mGroup as paramemter
    // return null
    //      group already exist
    // return group object if group is created
    public Group createGroup(Group group)throws Exception{
        String url = BASE_URL+CREATE_GROUP;
        String string = new Gson().toJson(group);
        JSONObject jsonObject = new JSONObject(string);
        HttpURLConnection connection = httpRequestPost(url,jsonObject);

        if (connection.getResponseCode() >= 400) {
            // failed
            Log.e("TAG","problem");
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }
        Log.i("TAG","no problem");

        StringBuffer response = readJsonIntoString(connection);
        group = new Gson().fromJson(response.toString(),Group.class);
        return group;
    }

    // take mGroup as parameter
    // return null
    //      group already exist
    // return group object if group is created
    public Group createGroupWithDetail(Group group)throws Exception{
        String url = BASE_URL+CREATE_GROUP;
        String string = new Gson().toJson(group);
        JSONObject jsonObject = new JSONObject(string);
        HttpURLConnection connection = httpRequestPost(url,jsonObject);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        group = new Gson().fromJson(response.toString(),Group.class);
        return group;
    }

    // take mGroup as parameter and using its id
    // return null
    //      group id does not exist
    // return group object if group is found
    public Group getOneGroup(Group group)throws Exception{
        String url = BASE_URL+String.format(GET_ONE_GROUP,group.getId());
        HttpURLConnection connection = httpRequestGet(url,null);
        connection.setRequestProperty("JSON-DEPTH","2");

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        Group mGroup = new Gson().fromJson(response.toString(),Group.class);
        return mGroup;
    }

    // take mGroup as parameter to be used as updated group info
    // return null
    //      group id not found, group with same description...
    // return new Group object is successfully updated
    public Group updateExistingGroup(Group group)throws Exception{
        String url = BASE_URL+String.format(UPDATE_EXISTING_GROUP,group.getId());
        String string = new Gson().toJson(group);
        JSONObject jsonObject = new JSONObject(string);
        HttpURLConnection connection = httpRequestPost(url, jsonObject);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        group = new Gson().fromJson(response.toString(),Group.class);
        return group;
    }

    // take mGroup as the group wanted to be deleted
    // return null
    // return SUCCESSFUL
    public String deleteGroup(Group group)throws Exception{
        String url = BASE_URL + String.format(DELETE_GROUP,group.getId());
        HttpURLConnection connection = httpRequestDelete(url);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        return SUCCESSFUL;
    }

    // take mGroup with id to retrieve info about group memebr
    // return null
    //      group not found
    // return array of User object for all the member in the group
    public User[] getMembersOfGroup (Group group) throws Exception{
        String url = BASE_URL+ String.format(GET_MEMBERS_OF_GROUP,group.getId());
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        User[] listMembers = new Gson().fromJson(response.toString(),User[].class);
        return listMembers;
    }

    // take user with id and mGroup with id to identify the group wanted to add new member
    // return null
    //      user not found, grounp not found...
    // return array of user object as group member
    public User[] addMemberToGroup(User user,Group group) throws Exception{
        String url = BASE_URL+ String.format(ADD_MEMBERS_TO_GROUP,group.getId());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",user.getId());
        HttpURLConnection connection = httpRequestPost(url,jsonObject);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer response = readJsonIntoString(connection);
        User[] listMembers = new Gson().fromJson(response.toString(),User[].class);
        return listMembers;
    }

    // take user with id and group with id to identify the group and user
    // return null
    // return SUCCESSFUL if successfully removed
    public String removeMemberOfGroup(User user, Group group)throws Exception{
        String url = BASE_URL+ String.format(REMOVE_MEMBER_OF_GROUP,group.getId(),user.getId());
        HttpURLConnection connection = httpRequestDelete(url);
        Log.i("TAG",url);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        return SUCCESSFUL;
    }

    // for messages

    public Message[] getAllMessages()throws Exception{
        String url = BASE_URL+ GET_ALL_MESSAGES;
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }
        StringBuffer result = readJsonIntoString(connection);
        Message[] messages = new Gson().fromJson(result.toString(),Message[].class);
        return messages;
    }

    public Message[] getAllEmergencyMessages()throws Exception{
        String url = BASE_URL+ GET_ALL_EMERGENCY_MESSAGES;
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }
        StringBuffer result = readJsonIntoString(connection);
        Message[] messages = new Gson().fromJson(result.toString(),Message[].class);
        return messages;
    }


    public Message[] getMessagesForGroup(Group group)throws Exception{
        String url = BASE_URL+ String.format(GET_MESSAGES_FOR_GROUP,group.getId());
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }
        StringBuffer result = readJsonIntoString(connection);
        Message[] messages = new Gson().fromJson(result.toString(),Message[].class);
        return messages;
    }

    public Message[] getEmergencyMessagesForGroup(Group group)throws Exception{
        String url = BASE_URL+ String.format(GET_EMERGENCY_MESSAGES_FOR_GROUP,group.getId());
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }
        StringBuffer result = readJsonIntoString(connection);
        Message[] messages = new Gson().fromJson(result.toString(),Message[].class);
        return messages;
    }

    public Message[] getMessagesForUser(User user)throws Exception{
        String url = BASE_URL+ String.format(GET_MESSAGES_FOR_USER,user.getId());
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }
        StringBuffer result = readJsonIntoString(connection);
        Message[] messages = new Gson().fromJson(result.toString(),Message[].class);
        return messages;
    }


    public Message[] getUnreadMessagesForUser(User user)throws Exception{
        String url = BASE_URL+ String.format(GET_UNREAD_MESSAGES_FOR_USER,user.getId());
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }
        StringBuffer result = readJsonIntoString(connection);
        Message[] messages = new Gson().fromJson(result.toString(),Message[].class);
        return messages;
    }

    public Message[] getReadMessagesForUser(User user)throws Exception{
        String url = BASE_URL+ String.format(GET_READ_MESSAGES_FOR_USER,user.getId());
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }
        StringBuffer result = readJsonIntoString(connection);
        Message[] messages = new Gson().fromJson(result.toString(),Message[].class);
        return messages;
    }

    public Message[] getUnreadEmergencyMessagesForUser(User user)throws Exception{
        String url = BASE_URL+ String.format(GET_UNREAD_EMERGENCY_MESSAGES_FOR_USER,user.getId());
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }
        StringBuffer result = readJsonIntoString(connection);
        Message[] messages = new Gson().fromJson(result.toString(),Message[].class);
        return messages;
    }

    public Message postMessageToGroup (Group group, Message message)throws Exception{
        String url = BASE_URL+ String.format(POST_MESSAGE_TO_GROUP,group.getId());
        String str = new Gson().toJson(message);
        JSONObject jsonObject = new JSONObject(str);
        HttpURLConnection connection = httpRequestPost(url,jsonObject);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer result = readJsonIntoString(connection);
        Message messages = new Gson().fromJson(result.toString(),Message.class);
        return messages;
    }

    public Message postMessageToParents(User user,Message message)throws Exception{
        String url = BASE_URL + String.format(POST_MESSAGE_TO_PARENTS,user.getId());
        String str = new Gson().toJson(message);
        JSONObject jsonObject = new JSONObject(str);
        HttpURLConnection connection = httpRequestPost(url,jsonObject);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer result = readJsonIntoString(connection);
        Message messages = new Gson().fromJson(result.toString(),Message.class);
        return messages;
    }

    public Message getOneMessage(Message message)throws Exception{
        String url = BASE_URL + String.format(GET_ONE_MESSAGE,message.getId());
        HttpURLConnection connection = httpRequestGet(url,null);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        StringBuffer result = readJsonIntoString(connection);
        Message messages = new Gson().fromJson(result.toString(),Message.class);
        return messages;
    }

    public String setMessageAsReadOrUnread(User user, Message message)throws Exception{
        String url = BASE_URL + String.format(SET_MESSAGE_AS_READ_OR_UNREAD,message.getId(),user.getId());
        JSONObject jsonObject = new JSONObject(String.valueOf(message.isEmergency()));
        HttpURLConnection connection = httpRequestGet(url,jsonObject);

        if (connection.getResponseCode() >= 400) {
            // failed
            BufferedReader error = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            throw new Gson().fromJson(error, ApiException.class);
        }

        return SUCCESSFUL;
    }

}
