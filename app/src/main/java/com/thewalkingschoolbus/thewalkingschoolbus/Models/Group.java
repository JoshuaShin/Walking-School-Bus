package com.thewalkingschoolbus.thewalkingschoolbus.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jackyx on 2018-03-08.
 */

public class Group {


    private String id;

    private String groupDescription;

    private double[] routeLatArray;
    private double[] routeLngArray;

    private User leader;
    private List<User> memberUsers;

    private String href;

    public Group(){
        id = null;
        groupDescription = null;
        leader=null;
        memberUsers = new ArrayList<>();
        routeLatArray = new double[3];
        routeLngArray = new double[3];
        href = null;
    }
    public Group(String name, double[] routeLatArray,double[] routeLngArray){
        groupDescription = name;
        this.routeLatArray = routeLatArray;
        this.routeLngArray = routeLngArray;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public double[] getRouteLatArray() {
        return routeLatArray;
    }

    public void setRouteLatArray(double[] routeLatArray) {
        this.routeLatArray = routeLatArray;
    }

    public double[] getRouteLngArray() {
        return routeLngArray;
    }

    public void setRouteLngArray(double[] routeLngArray) {
        this.routeLngArray = routeLngArray;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public List<User> getMemberUsers() {
        return memberUsers;
    }

    public void setMemberUsers(List<User> memberUsers) {
        this.memberUsers = memberUsers;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

}
