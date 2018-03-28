package com.thewalkingschoolbus.thewalkingschoolbus;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.thewalkingschoolbus.thewalkingschoolbus.Interface.OnTaskComplete;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.Group;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.User;
import com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask;

import java.util.List;

import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.GET_ONE_GROUP;
import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.GET_USER_BY_ID;

public class GroupLeaderFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "GroupLeaderFragment";
    private View view;
    private Group[] mGroup;

    // Used for recursive loop in getGroupWithDetailLoop()
    private static boolean populateListReady = false;
    private static int loopCount = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        if (container != null) {
//            container.removeAllViews();
//        }
        view = inflater.inflate(R.layout.fragment_group_leader, container, false);

        //getGroupListAndPopulateList();
        setUpRefresh();
        setUpAddButton();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //clearListView();
        getGroupListAndPopulateList();
    }

    private void getGroupListAndPopulateList(){
        new GetUserAsyncTask(GET_USER_BY_ID, User.getLoginUser(), null, null,null, new OnTaskComplete() {
            @Override
            public void onSuccess(Object result) {
                User returnUser = (User) result;
                List<Group> mGroupList = returnUser.getMemberOfGroups(); // TODO: change this to getLeaderOfGroups() when it is implemented

                mGroup = new Group[mGroupList.size()];
                mGroupList.toArray(mGroup);

                // Without this return statement app will from ArrayIndexOutOfBoundsException!
                if (mGroupList.isEmpty()) {
                    Toast.makeText(getActivity(), "Not a leader of any group!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Begin get group detail recursion
                stringsPrep();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(),"Error :" + e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }
/*
    private void getGroupWithDetail() {
        // Set up recursion
        populateListReady = false;
        loopCount = 0;
        getGroupWithDetailLoop();
    }

    private void getGroupWithDetailLoop() {
        if (loopCount >= mGroup.length - 1) {
            populateListReady = true;
        }
        new GetUserAsyncTask(GET_ONE_GROUP, null, null, mGroup[loopCount],null, new OnTaskComplete() {
            @Override
            public void onSuccess(Object result) {
                mGroup[loopCount] = (Group) result;

                if (populateListReady) {
                    stringsPrep();
                } else {
                    loopCount++;
                    getGroupWithDetailLoop();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(),"Error :" + e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }
    */

    private void stringsPrep(){
        String[] mGroupDisplay;
        int length;
        if(mGroup == null){
            mGroupDisplay = new String[0];
            length = 0;
        }else {
            mGroupDisplay = new String[mGroup.length];
            length = mGroup.length;
        }
        String str;
        for(int i = 0; i < length;i++){
            if(mGroup[i].getGroupDescription() == null){
                str = "id: "+mGroup[i].getId();
            }else {
                str = "id: " + mGroup[i].getId() + " " + "Group Name: " + mGroup[i].getGroupDescription();
            }
            mGroupDisplay[i] = str;
        }

        populateListView(mGroupDisplay);
    }

    private void populateListView(String[] mGroupDisplay){
        Toast.makeText(getActivity(),"Group leader list updated", Toast.LENGTH_SHORT).show();
        // create list of item
        String[] myItems = mGroupDisplay;
        // Build adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.group_entry,myItems);
        // configure the list view
        ListView list = view.findViewById(R.id.groupListViewLeader);
        list.setAdapter(adapter);

        //registerClickCallback
        registerClickCallback();
    }
/*
    private void clearListView(){
        // create list of item
        String[] myItems = {};
        // Build adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.group_entry, myItems);
        // configure the list view
        ListView list = view.findViewById(R.id.groupListViewLeader);
        list.setAdapter(adapter);
    }
*/
    private void registerClickCallback() {
        ListView list = view.findViewById(R.id.groupListViewLeader);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                TextView textView = (TextView) viewClicked;
                Group selectedGroup = mGroup[position];
                Intent intent =  GroupDetailActivity.makeIntent(getActivity(),selectedGroup,User.getLoginUser());
                startActivity(intent);
            }
        });
    }

    private void setUpAddButton(){
        FloatingActionButton btn = view.findViewById(R.id.addGroupBtnLeader);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = JoinOrCreateGroupActivity.makeIntent(getActivity());
                startActivity(intent);
            }
        });
    }

    private void setUpRefresh(){
        final SwipeRefreshLayout mySwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLeader);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        //clearListView();
                        getGroupListAndPopulateList();
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        mySwipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
    }
}
