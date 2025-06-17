package com.sinloingok.app.aop;

import com.sinloingok.app.models.user.ManageUser;
import com.sinloingok.app.models.user.User;

public class UserContext {
    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();

    private static final ThreadLocal<ManageUser> mUserHolder = new ThreadLocal<>();
    
    public static void setUser(User user) {
        userHolder.set(user);
    }

    public static void  setMUser(ManageUser mUser){
        mUserHolder.set(mUser);
    }

    public static ManageUser getMUser(){
        return mUserHolder.get();
    }

    public static User getUser() {
        return userHolder.get();
    }
    
    public static void clear() {
        userHolder.remove();
    }
}