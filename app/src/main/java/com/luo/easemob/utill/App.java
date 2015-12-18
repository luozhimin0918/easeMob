package com.luo.easemob.utill;

import android.app.Application;

import com.easemob.chat.EMChat;

/**
 * Created by Administrator on 2015/12/18.
 */
public class App extends Application {
    public static String UserName;
    public static  String  Password;

    public static String getUserName() {
        return UserName;
    }

    public static void setUserName(String userName) {
        UserName = userName;
    }

    public static String getPassword() {
        return Password;
    }

    public static void setPassword(String password) {
        Password = password;
    }

    @Override
    public void onCreate() {
        super.onCreate();

            EMChat.getInstance().init(getApplicationContext());

            /**
             * debugMode == true 时为打开，sdk 会在log里输入调试信息
             * @param debugMode
             * 在做代码混淆的时候需要设置成false
             */
            EMChat.getInstance().setDebugMode(true);//在做打包混淆时，要关闭debug模式，避免消耗不必要的资源
    }
}
