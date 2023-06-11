package com.example.HttpServer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.DataServer.DataOperate;
import com.example.hope.MainActivity;
import com.example.hope.ServerActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpOperate {

    public boolean SignInFlag;
    private String LoginUrl = "http://192.168.43.228:8080/login";
    private String RegisterUrl = "http://192.168.43.228:8080/register";
    private String GovenUrl = "http://10.198.59.100:8080/status";

    private String UserUUID;
    public String Usersession;
    private static final String UserSQLName = "UserSQL";
    private final String UserTableName = "T_UserInfo";


    private String NameTemp;

    private DataOperate MyDataObject;

    private Context myContext;
    private MainActivity myMainActivity;
    private ServerActivity myServerActivity;

    public boolean UserHealthyState = false;


    //构造函数
    public HttpOperate(Context context,MainActivity mainActivity)
    {
        //数据库操作类
        MyDataObject = new DataOperate();
        myContext = context;
        myMainActivity = mainActivity;

    }

    public HttpOperate(Context context,ServerActivity serverActivity)
    {
        //数据库操作类
        MyDataObject = new DataOperate();
        myContext = context;
        myServerActivity = serverActivity;
    }



    private String RegisterNameTemp,RegisterCodeTemp;

    //注册服务
    public String HttpRegisterServer(String Name, String Code, AlertDialog myRegister) {
        //第一步创建OKHttpClient
        OkHttpClient client = new OkHttpClient.Builder().build();
        //第二步创建RequestBody（Form表达）
        Map m = new HashMap();
        m.put("name", Name);
        m.put("password", Code);
        RegisterNameTemp = Name;
        RegisterCodeTemp = Code;
        //创建json对象,储存上传的数据
        JSONObject jsonObject = new JSONObject(m);
        String jsonStr = jsonObject.toString();
        RequestBody requestBodyJson =
                RequestBody.create(MediaType.parse("application/json;charset=utf-8")
                        , jsonStr);
        //第三步创建Rquest
        Request request = new Request.Builder()
                .url(RegisterUrl)
                .addHeader("contentType", "application/json;charset=UTF-8")
                .post(requestBodyJson)
                .build();
        //第四步创建call回调对象
        final Call call = client.newCall(request);
        //第五步发起请求
        call.enqueue(new Callback() {
            //接收失败
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            //接收成功
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                UserUUID = result;
                Log.v("zj:",result);
                if (!result.equals("No")) {
                    //添加数据库
                    DataOperate.DBHelper test = MyDataObject.new DBHelper(myContext,
                            UserSQLName, null, 1);
                    //获取写入数据库的权限
                    SQLiteDatabase db = test.getWritableDatabase();

                    ContentValues WritePen = new ContentValues();
                    WritePen.put("name", RegisterNameTemp);
                    WritePen.put("password", RegisterCodeTemp);
                    WritePen.put("uuid",result);
                    //数据库插入用户信息
                    long error = db.insert(UserTableName, null, WritePen);
                    Log.v("zj:", String.valueOf(error));
                    WritePen.clear();
                    Log.v("zj:","向服务器注册成功！");

                    Looper.prepare();
                    Toast.makeText(myMainActivity, "注册成功！", Toast.LENGTH_SHORT).show();
                    myRegister.cancel();
                    Looper.loop();
                } else {
                    Log.v("zj:","向服务器注册失败！");
                    Looper.prepare();
                    Toast.makeText(myMainActivity, "注册失败！", Toast.LENGTH_SHORT).show();
                    myRegister.cancel();

                    //注册成功
//                    RegisterOther();
                    Looper.loop();
                }
            }
        });
        //返回用户的UUID
        return UserUUID;
    }
    //登陆服务
    public void HttpLoginServer(String Name,String Code) {
        //第一步创建OKHttpClient
        OkHttpClient client = new OkHttpClient.Builder().build();
        //第二步创建RequestBody（Form表达）
        Map m = new HashMap();
        m.put("name", Name);
        m.put("password", Code);
        SignInFlag = false;
        NameTemp = Name;

        //创建json对象,储存上传的数据
        JSONObject jsonObject = new JSONObject(m);
        String jsonStr = jsonObject.toString();
        RequestBody requestBodyJson =
                RequestBody.create(MediaType.parse("application/json;charset=utf-8")
                        , jsonStr);
        //第三步创建Rquest
        Request request = new Request.Builder()
                .url(LoginUrl)
                .addHeader("contentType", "application/json;charset=UTF-8")
                .post(requestBodyJson)
                .build();
        //第四步创建call回调对象
        final Call call = client.newCall(request);
        Usersession = new String("接收失败");
        //第五步发起请求
        call.enqueue(new Callback() {
            //接收失败
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.v("zj:","接收失败");
            }
            //接收成功
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                String result = Objects.requireNonNull(response.body()).string();

                if (result.equals("1")) {
                    List<String> cookies = response.headers().values("Set-Cookie");  // 获取cookie
                    Usersession = cookies.get(0);
                    Log.v("zj:","向服务器登录成功！"+Usersession);
                    //将获取的session写入数据库
                    MyDataObject.WriteSession(UserSQLName,UserTableName,NameTemp,myContext,Usersession);
                    //登录的其他乱七八糟的操作
//                    LoginOther();
                    //登录成功
                    SignInFlag = true;

                    Looper.prepare();
                    Toast.makeText(myMainActivity, "登录成功", Toast.LENGTH_SHORT).show();
                    //切换界面
                    Intent intent = new Intent();
                    intent.setClass(myContext, ServerActivity.class);
                    myMainActivity.finish();
                    myContext.startActivity(intent);
                    Looper.loop();

                } else {
                    //登录失败
                    Looper.prepare();
                    Toast.makeText(myMainActivity, "登录失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        });
    }

    //注册的乱七八糟操作
    private void RegisterOther()
    {
        //                    cookies = response.headers().values("Set-Cookie");  // 获取cookie
//                    session = cookies.get(0);  // 获取session

        // 将cookie存储入sqliteDB
//                    ContentValues values = new ContentValues();
//                    values.put("name", name_text);
//                    values.put("password", password_text);
//                    values.put("session", session);
//                    SQLiteDatabase db = DBUtil.db(getApplicationContext());
//                    db.insert("T_UserInfo",null,values);

//                    Cookie.getInstance(MainActivity.this.session);
        //get();
//                    Intent intent = new Intent();
//                    intent.setClass(MainActivity.this,ShowActivity.class);
//                    intent.putExtra("Cookie", MainActivity.this.session);
//                    finish();
//                    startActivity(intent);

//                    //切换界面
//                    Intent intent = new Intent();
//                    intent.setClass(MainActivity.this, UserServerActivity.class);
//                    MainActivity.this.finish();
//                    startActivity(intent);


    }

    //登录的乱七八糟操作
    private void LoginOther()
    {
//                    cookies = response.headers().values("Set-Cookie");  // 获取cookie
//                    session = cookies.get(0);  // 获取session

        // 将cookie存储入sqliteDB
//                    ContentValues values = new ContentValues();
//                    values.put("name", name_text);
//                    values.put("password", password_text);
//                    values.put("session", session);
//                    SQLiteDatabase db = DBUtil.db(getApplicationContext());
//                    db.insert("T_UserInfo",null,values);

//                    Cookie.getInstance(MainActivity.this.session);
        //get();
//                    Intent intent = new Intent();
//                    intent.setClass(MainActivity.this,ShowActivity.class);
//                    intent.putExtra("Cookie", MainActivity.this.session);
//                    finish();
//                    startActivity(intent);


    }


    //用户轨迹上传服务
    public void HttpRaiUploadServer(String Name,String Code) {
        //第一步创建OKHttpClient
        OkHttpClient client = new OkHttpClient.Builder().build();
        //第二步创建RequestBody（Form表达）
        Map m = new HashMap();
        m.put("name", Name);
        m.put("Rail", Code);
        //创建json对象,储存上传的数据
        JSONObject jsonObject = new JSONObject(m);
        String jsonStr = jsonObject.toString();
        RequestBody requestBodyJson =
                RequestBody.create(MediaType.parse("application/json;charset=utf-8")
                        , jsonStr);
        //第三步创建Rquest
        Request request = new Request.Builder()
                .url(RegisterUrl)
                .addHeader("contentType", "application/json;charset=UTF-8")
                .post(requestBodyJson)
                .build();
        //第四步创建call回调对象
        final Call call = client.newCall(request);
        //第五步发起请求
        call.enqueue(new Callback() {
            //接收失败
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            //接收成功
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                if (result.equals("0")) {
                    //注册失败
                    Looper.prepare();
                    Looper.loop();
                } else {
                    //注册成功
                    RegisterOther();
                    Looper.prepare();
                    Looper.loop();
                }
            }
        });
    }


    //用户健康状态获取服务
    public void GetUserHealthyState(String Name)
    {
        //第一步创建OKHttpClient
        OkHttpClient client = new OkHttpClient.Builder().build();
        //第二步创建RequestBody（Form表达）
        Map m = new HashMap();
        m.put("name", Name);
        //创建json对象,储存上传的数据
        JSONObject jsonObject = new JSONObject(m);
        String jsonStr = jsonObject.toString();
        RequestBody requestBodyJson =
                RequestBody.create(MediaType.parse("application/json;charset=utf-8")
                        , jsonStr);
        //第三步创建Rquest
        Request request = new Request.Builder()
                .url(GovenUrl)
                .addHeader("contentType", "application/json;charset=UTF-8")
                .post(requestBodyJson)
                .build();
        //第四步创建call回调对象
        final Call call = client.newCall(request);
        //第五步发起请求
        call.enqueue(new Callback() {
            //接收失败
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            //接收成功
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                if (result.equals("1")) {
                    //阳性
                    Log.v("zj:","用户为阳性");
                    Looper.prepare();
                    UserHealthyState = true;
                    Looper.loop();
                } else if(result.equals("0")){
                    //阴性
                    Looper.prepare();
                    Log.v("zj:","用户为阴性");
                    UserHealthyState = false;
                    Looper.loop();
                }
                else
                {
                    Log.v("zj:","用户为未知");
                }
            }
        });

    }
}
