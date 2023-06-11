package com.example.hope;

//用户HTTP操作类
import com.example.HttpServer.HttpOperate;
//用户本地数据管理类
import com.example.DataServer.*;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;


public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,View.OnClickListener
        , CompoundButton.OnCheckedChangeListener {

    //Debug
    private static final String TAG = "MainActivity";
    private static final String UserSQLName = "UserSQL";
    private final String UserTableName = "T_UserInfo";

    //UI assembly
    private Spinner UserLanguageList;
    private Button SignInButton;
    private Button RegisterButton;
    private ImageView SoftName;
    private AutoCompleteTextView UserName;
    private EditText UserCode;
    private CheckBox UserShowCode;
    private TextView RegisterAPPName;
    private TextView UserNameTip;
    private TextView UserCodeTip;

    //HTTP操作类
    private HttpOperate MyHttpObject;
    //数据库管理类
    public DataOperate MyDataObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.v("zj:","开始登录");
        //HTTP操作类
        MyHttpObject = new HttpOperate(MainActivity.this,this);
        //用户数据管理
        MyDataObject = new DataOperate();
//        Jump2ServerActivity();
        //登录界面UI初始化
        SignInUIInit();
    }


    //修改语言
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //获取当前选中的内容
        String content = parent.getItemAtPosition(position).toString();

        switch (content) {
            case "中文(Chinese)":
//                Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
                break;
            case "英文(English)":
//                Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //初始化登录UI界面
    @SuppressLint("ResourceType")
    public void SignInUIInit() {
        //登录按键初始化
        SignInButton = findViewById(R.id.UserSignInButton1);
        SignInButton.setOnClickListener(this);
        //注册按键初始化
        RegisterButton = findViewById(R.id.UserRegisterButton2);
        RegisterButton.setOnClickListener(this);
        //标题初始化
        SoftName = findViewById(R.id.SoftName);
//        SoftName.setTypeface(Typeface.createFromAsset(getAssets(), "font/soname.ttf"));
        //显示密码可见
        UserShowCode = findViewById(R.id.UserShowCode);
        UserShowCode.setOnCheckedChangeListener(this::onCheckedChanged);
        //密码框
        UserCode = findViewById(R.id.UserCode);
        //获取用户名组件
        UserName = findViewById(R.id.UserName);
        //禁止输入空格
        UserName.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            if (source.equals(" ")) return "";
            else return null;
        }});


        UserNameTip = findViewById(R.id.UserNameTip);
        UserCodeTip = findViewById(R.id.UserCodeTip);
        //设置字体
        UserNameTip.setTypeface(Typeface.createFromAsset(getAssets(), "font/font11.ttf"));
        UserCodeTip.setTypeface(Typeface.createFromAsset(getAssets(), "font/font11.ttf"));


    }


    //登录按键回调
    @SuppressLint("ResourceType")
    @Override
    public void onClick(View v) {

        if (v.getId() == SignInButton.getId()) {
            //发送登录请求
            LoginServer();

        }
        //发送注册请求
        else if (v.getId() == RegisterButton.getId()) {
            RegisterServer();
        }

    }//登录按键回调

    /*显示密码 或 不显示密码*/
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
//            显示密码或者不显示
            case R.id.UserShowCode: {
                if (isChecked)
                    //设置密码可见
                    UserCode.setInputType(0x90);
                else
                    //设置密码不可见
                    UserCode.setInputType(0x81);
                break;
            }
        }
    }//显示密码或不显示密码


    public void Jump2ServerActivity() {
        //切换界面
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, ServerActivity.class);

        //新功能测试界面
//        intent.setClass(MainActivity.this, testMainActivity.class);
        this.finish();
        startActivity(intent);
    }


    boolean NameFirst = false, CodeFirst = false, CodeSureFirst = false;

    @SuppressLint({"ResourceType", "ClickableViewAccessibility"})
    public void RegisterServer() {
        //弹出注册框
        View view = LayoutInflater.from(this).inflate(R.layout.register_dialog, null);
        //自定义dialog
        AlertDialog.Builder RegisterDialogBuild = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false);
        AlertDialog RegisterDialog = RegisterDialogBuild.create();
        RegisterDialogBuild.getContext();
        RegisterDialog.show();

        //组件设置
        Button RegisterSure = view.findViewById(R.id.RegisterSure);
        EditText RegisterUserName = view.findViewById(R.id.editTextTextPersonName);
        RegisterUserName.setTypeface(Typeface.createFromAsset(getAssets(), "font/font4.ttf"));
        RegisterUserName.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            if (source.equals(" ")) return "";
            else return null;
        }});
        EditText RegisterUserCode = view.findViewById(R.id.editTextTextPassword);
        EditText RegisterUserCodeSure = view.findViewById(R.id.editTextTextPasswordSure);
        RegisterUserCode.setTypeface(Typeface.createFromAsset(getAssets(), "font/font4.ttf"));
        RegisterUserCode.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            if (source.equals(" ")) return "";
            else return null;
        }});
        RegisterUserCodeSure.setTypeface(Typeface.createFromAsset(getAssets(), "font/font4.ttf"));
        RegisterUserCodeSure.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            if (source.equals(" ")) return "";
            else return null;
        }});
        TextView RegisterTip = view.findViewById(R.id.RegisterTip);
        //监听编辑框触摸事件
        RegisterUserName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!NameFirst) {
                    RegisterUserName.setText("");
                    RegisterUserName.setTextColor(0xFF000000);
                    NameFirst = true;
                }
                return false;
            }
        });

        RegisterUserCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!CodeFirst) {
                    RegisterUserCode.setText("");
                    RegisterUserCode.setInputType(0x81);
                    RegisterUserCode.setTextColor(0xFF000000);
                    CodeFirst = true;
                }
                return false;
            }
        });

        RegisterUserCodeSure.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!CodeSureFirst) {
                    RegisterUserCodeSure.setText("");
                    RegisterUserCodeSure.setInputType(0x81);
                    RegisterUserCodeSure.setTextColor(0xFF000000);
                    CodeSureFirst = true;
                }
                return false;
            }
        });

        //文本框内容发生改变实时监测
        RegisterUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
//                if(!NameFirst)
//                    return ;
                //检测用户名是否重复
                if(RegisterUserName.getText().toString().equals(""))
                {
                    RegisterTip.setText("");
                    RegisterTip.setText("请输入用户名！");
                    RegisterUserName.setCompoundDrawables(null,null,
                            null, null);
                    return ;
                }
                if(MyDataObject.CheckRepeat(UserSQLName, "T_UserInfo",
                        RegisterUserName.getText().toString(), MainActivity.this))
                {
                    RegisterTip.setText("");
                    RegisterTip.setText("用户名已经存在");
                    RegisterUserName.setCompoundDrawables(null,null,
                            null, null);
                }
                else
                {
                    RegisterTip.setText("");
                    RegisterUserName.setCompoundDrawables(null,null,
                            null, Drawable.createFromPath("D://hope//app//src//main//res//drawable//correct.png"));
                }
            }
        });

        //文本框内容发生改变实时监测
        RegisterUserCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                if(!CodeFirst)
//                    return ;
                //检测密码是否合格
                if (RegisterUserCode.getText().toString().equals("") || RegisterUserCode.getText().length() < 6)
                {
                    RegisterTip.setText("");
                    RegisterTip.setText("密码必须大于5位！");
                    RegisterUserCode.setCompoundDrawables(null,null,
                            null, null);
                }
                else
                {
                    RegisterTip.setText("");
                    RegisterUserCode.setCompoundDrawables(null,null,
                            null, Drawable.createFromPath("D://hope//app//src//main//res//drawable//correct.png"));
                }
            }
        });


        //点击确定注册按钮事件之后
        RegisterSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断用户名是否为空
                if (RegisterUserName.getText().toString().equals("") || (!NameFirst)) {
                    RegisterTip.setText("");
                    RegisterTip.setText("用户名不可以为空！");
                    return;
                }
                //判断密码是否为空且大于6位
                if (RegisterUserCode.getText().toString().equals("") || RegisterUserCode.getText().length() < 6 || (!CodeFirst)) {
                    RegisterTip.setText("");
                    RegisterTip.setText("密码必须大于5位！");
                    return;
                }
                //判断用户名是否存在
                if (MyDataObject.CheckRepeat(UserSQLName, "T_UserInfo",
                        RegisterUserName.getText().toString(), MainActivity.this)) {
                    RegisterTip.setText("");
                    RegisterTip.setText("用户名已经存在");
                    return;
                }
                //判断两次输入的密码是否正确
                if (!RegisterUserCode.getText().toString().equals(RegisterUserCodeSure.getText().toString())) {
                    RegisterTip.setText("");
                    RegisterTip.setText("密码不一致！");
                    return;
                }

                //向服务器发送注册请求
                MyHttpObject.HttpRegisterServer(RegisterUserName.getText().toString(),
                        RegisterUserCode.getText().toString(),RegisterDialog);


                    CodeSureFirst = false;
                    CodeFirst = false;
                    NameFirst = false;
//                    RegisterDialog.cancel();

            }
        });
        //按下取消按键
        Button RegisterAbort = view.findViewById(R.id.RegisterAbort);
        RegisterAbort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CodeSureFirst = false;
                CodeFirst = false;
                NameFirst = false;
                RegisterDialog.cancel();
            }
        });
    }

    public void LoginServer() {
        //检查密码格式正确与否
        if (UserCode.getText().toString().equals("")) {
            AlertDialog textTips = new AlertDialog.Builder(this)
                    .setTitle("警告：请输入有效密码！")
                    .setMessage("")
                    .create();
            textTips.show();
            return;
        }
        //检查用户名是否合法
        if (UserName.getText().toString().equals("")) {
            AlertDialog textTips = new AlertDialog.Builder(this)
                    .setTitle("警告：请输入有效用户名！")
                    .setMessage("")
                    .create();
            textTips.show();
            return;
        }

        Vector<String> Name = new Vector<String>();
        Name = MyDataObject.GetTotalName(UserSQLName, UserTableName, this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_main, Name);
        UserName.setAdapter(adapter);

        Log.v("zj:","开始登录");

        MyHttpObject.HttpLoginServer(UserName.getText().toString(),
                                        UserCode.getText().toString());


//            if(!MyHttpObject.Usersession.equals("接收失败"))
//            {
                //进入注册界面
//                Jump2ServerActivity();


//            }

    }



}