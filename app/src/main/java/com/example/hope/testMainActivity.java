package com.example.hope;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.services.core.ServiceSettings;
import com.example.NewsAdapter.MyNewsAdapter;
import com.github.mzule.fantasyslide.SideBar;
import com.github.mzule.fantasyslide.Transformer;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class testMainActivity extends AppCompatActivity {

    //AMap是地图对象
    MapView mMapView = null;
    ListView news1_list;

    private ImageButton MenuButton;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_test);

        MenuButton = (ImageButton) findViewById(R.id.MenuButton);
        registerForContextMenu(MenuButton);


//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        ServiceSettings.updatePrivacyShow(this, true, true);
        ServiceSettings.updatePrivacyAgree(this,true);

//        获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
//        在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
//        初始化地图控制器对象
        AMap aMap = mMapView.getMap();


        MenuButton.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        //新闻内容设置
        List<String> news_1_data = new ArrayList<>();
        //设置新闻界面
        news_1_data.add("新闻1");
        news_1_data.add("新闻2");
        news_1_data.add("新闻3");

        news1_list = findViewById(R.id.ListTest);
        news1_list.setAdapter(new MyNewsAdapter(this,news_1_data));

        //爬取网页内容
        String path="http://www.nhc.gov.";
        try {
            URL url = new URL(path);
            //获取连接对象
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //设置请求方式
            httpURLConnection.setRequestMethod("POST");
            //设置连接超时
            httpURLConnection.setConnectTimeout(5000);
            //获取响应码
            int code = httpURLConnection.getResponseCode();
            if(code == 200) {
                //响应成功,获取服务器返回过来的数据
                final InputStream is = httpURLConnection.getInputStream();
                //测试数据
                StringBuffer stringBuffer = new StringBuffer();
                String str = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((str = br.readLine()) != null) {
                    stringBuffer.append(str);
                }
                Log.v("Json数据", "Json数据: "+stringBuffer.toString() );

            }


        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }



    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.OpenService:
                Toast.makeText( this, "开启服务",Toast.LENGTH_SHORT).show();
                break;
            case R.id.CloseService:
                Toast.makeText( this,"关闭服务",Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }



}
