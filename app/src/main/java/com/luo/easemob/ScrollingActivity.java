package com.luo.easemob;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.luo.easemob.utill.App;
import com.luo.easemob.utill.CommonUtils;

public class ScrollingActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private boolean progressShow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "regit", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                regit();
            }
        });






    }

    private void regit() {
//EMChat.getInstance().isLoggedIn() 可以检测是否已经登录过环信，如果登录过则环信SDK会自动登录，不需要再次调用登录操作
        if (EMChat.getInstance().isLoggedIn()) {
            progressDialog = getProgressDialog();
            progressDialog.setMessage(getResources().getString(R.string.progress));
            progressDialog.show();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        //加载本地数据库中的消息到内存中
                        EMChatManager.getInstance().loadAllConversations();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("ScrollingActivity","areldLogin");
                    toChatActivity();
                }
            }).start();
        } else {
            //随机创建一个用户并登录环信服务器
            createRandomAccountAndLoginChatServer();
        }
    }


    private void createRandomAccountAndLoginChatServer() {
        Log.d("ScrollingActivity","createAccountToServer");
        // 自动生成账号
        final String randomAccount = "luozhimin0918";
        final String userPwd = "123456";
        progressDialog = getProgressDialog();
        progressDialog.setMessage(getResources().getString(R.string.progress));
        progressDialog.show();
        createAccountToServer(randomAccount, userPwd, new EMCallBack() {

            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //登录环信服务器
                        loginHuanxinServer(randomAccount, userPwd);
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(final int errorCode, final String message) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (!ScrollingActivity.this.isFinishing()) {
                            progressDialog.dismiss();
                        }
                        if (errorCode == EMError.NONETWORK_ERROR) {
                            Toast.makeText(getApplicationContext(), "网络不可用", Toast.LENGTH_SHORT).show();
                        } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                            Toast.makeText(getApplicationContext(), "用户已存在", Toast.LENGTH_SHORT).show();
                        } else if (errorCode == EMError.UNAUTHORIZED) {
                            Toast.makeText(getApplicationContext(), "无开放注册权限", Toast.LENGTH_SHORT).show();
                        } else if (errorCode == EMError.ILLEGAL_USER_NAME) {
                            Toast.makeText(getApplicationContext(), "用户名非法", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "注册失败：" + message, Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                });
            }
        });
    }



    //注册用户
    private void createAccountToServer(final String uname, final String pwd, final EMCallBack callback) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    EMChatManager.getInstance().createAccountOnServer(uname, pwd);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } catch (EaseMobException e) {
                    if (callback != null) {
                        callback.onError(e.getErrorCode(), e.getMessage());
                    }
                }
            }
        });
        thread.start();
    }


    public void loginHuanxinServer(final String uname, final String upwd) {
        progressShow = true;
        progressDialog = getProgressDialog();
        progressDialog.setMessage(getResources().getString(R.string.progress));
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        // login huanxin server
        EMChatManager.getInstance().login(uname, upwd, new EMCallBack() {
            @Override
            public void onSuccess() {
                if (!progressShow) {
                    return;
                }
                App.setUserName(uname);
                App.setPassword(upwd);
                try {
                    EMChatManager.getInstance().loadAllConversations();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                toChatActivity();
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(final int code, final String message) {
                if (!progressShow) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(ScrollingActivity.this,
                                "登录环信失败" + message,
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }



    private ProgressDialog getProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(ScrollingActivity.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    progressShow = false;
                }
            });
        }
        return progressDialog;
    }
    private void toChatActivity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!ScrollingActivity.this.isFinishing())
                    progressDialog.dismiss();
                // 进入主页面
              /*  startActivity(new Intent(ScrollingActivity.this, ChatActivity.class).putExtra(
                        Constant.INTENT_CODE_IMG_SELECTED_KEY, selectedIndex).putExtra(
                        Constant.MESSAGE_TO_INTENT_EXTRA, messageToIndex));
                finish();*/
                EMGroupManager.getInstance().loadAllGroups();
                EMChatManager.getInstance().loadAllConversations();
                Log.d("ScrollingActivity","登录成功"+App.getUserName());
            }
        });
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}
