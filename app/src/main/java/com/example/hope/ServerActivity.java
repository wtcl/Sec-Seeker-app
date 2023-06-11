package com.example.hope;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.BackstageServer.MyWork;
import com.example.HealthyUpdate.HealthyUpdateThread;
import com.example.HttpServer.HttpOperate;
import com.example.MapServer.MapOperateObject;
import com.example.NewsAdapter.Bean;
import com.example.NewsAdapter.MyNewsAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.michaldrabik.tapbarmenulib.TapBarMenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;


public class ServerActivity extends AppCompatActivity
        implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private final int GreenColor = 0XFF0EAD69;
    private final int RedColor = 0XFFF94144;
    private final int YelloColor = 0XFFFF7B00;
    private String CurrentPage = "code";

    GestureDetector MyGestureDetector;


    ImageView dimensional_barcode;
    private TapBarMenu tapBarMenu;
    ImageView ServerCode;
    ImageView ServerLocation;
    ImageView ServerNews;
    TextView ServerTime;
    ImageButton FreshTimeButton;
//    WebView ServerWeb;

    private DrawerLayout mDrawerLayout;//侧边菜单视图
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private Switch LocationServer;


    HttpOperate myHttpObject;
    MapOperateObject ServerMapOperator;
    private int CodeColor = GreenColor;
    private Bitmap erweima;
    public Handler handler;
    private ViewPager vp;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"ClickableViewAccessibility", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userserver_main);


        mDrawerLayout = findViewById(R.id.ServerWindow);

        ActionBar actionBar = getSupportActionBar();//获取当前升级后标题栏
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);//让Actionbar去初始化箭头按钮

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.open, R.string.close);
        mActionBarDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);


        //HTTP操作类
        myHttpObject = new HttpOperate(ServerActivity.this, this);
        View view = findViewById(R.id.Menu);
        ServerCode = view.findViewById(R.id.ServerCode);
        ServerLocation = view.findViewById(R.id.ServerLocation);
        ServerNews = view.findViewById(R.id.ServerNews);
        ServerTime = findViewById(R.id.ServerTime);
        FreshTimeButton = findViewById(R.id.FreshTimeButton);
        FreshTimeButton.setOnTouchListener(this);
        dimensional_barcode = findViewById(R.id.dimensional_barcode);
        tapBarMenu = findViewById(R.id.ServerMenu);
//        ServerWeb = findViewById(R.id.ServerWeb);
        LocationServer = findViewById(R.id.LocationServer);
        LocationServer.setTypeface(Typeface.createFromAsset(getAssets(), "font/font4.ttf"));
        vp = (ViewPager) findViewById(R.id.ServerViewPage);

        LocationServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    //打开定位服务
                    startWork();
                    Toast.makeText(ServerActivity.this, "上传服务开启", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    WorkManager.getInstance(ServerActivity.this).cancelAllWork();
                    Toast.makeText(ServerActivity.this, "上传服务关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        ServerWeb.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return false;
//            }
//        });

        tapBarMenu.performClick();
        tapBarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapBarMenu.toggle();
            }
        });

        //创建手势监测器
        MyGestureDetector = new GestureDetector(ServerActivity.this, this);
        //创建地图管理器
        ServerMapOperator = new MapOperateObject(ServerActivity.this, savedInstanceState, findViewById(R.id.ServerMap));
        //展示二维码界面
        ShowHealth();

        //监听显示切换按键
        ServerCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowHealth();
            }
        });
        ServerLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowLocation();
            }
        });
        ServerNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowNews();
            }
        });

//        WebSettings webSettings = ServerWeb.getSettings();
//        webSettings.setUseWideViewPort(true);
//        webSettings.setLoadWithOverviewMode(true);

//        try {
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //获取用户健康状态
        GetUserHealthyState();

        //刷新时间
        ServerInTime();

        //设置新闻显示界面
        setVp();
        GetWebNews();


    }


    //展示定位信息
    public void ShowLocation() {
        vp.setVisibility(View.GONE);
//        ServerWeb.setVisibility(View.GONE);
        ServerMapOperator.SetMapVisiuability();
        CurrentPage = "Location";
        //取消二维码的显示
        dimensional_barcode.setVisibility(View.GONE);
        ServerTime.setVisibility(View.GONE);
        FreshTimeButton.setVisibility(View.GONE);
        ServerNews.setBackgroundColor(0xFFFEFAE0);
        ServerLocation.setBackgroundColor(0XFFE0E1DD);
        ServerCode.setBackgroundColor(0xFFFEFAE0);
    }

    //展示健康信息
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void ShowHealth() {
        ServerTime.setVisibility(View.VISIBLE);
//        ServerWeb.setVisibility(View.GONE);
        vp.setVisibility(View.GONE);
        ServerMapOperator.SetMapInVisiuability();
        //判断健康状况
        FreshTime();
        CurrentPage = "code";
        ServerNews.setBackgroundColor(0xFFFEFAE0);
        ServerLocation.setBackgroundColor(0xFFFEFAE0);
        ServerCode.setBackgroundColor(0XFFE0E1DD);
        dimensional_barcode.setVisibility(View.VISIBLE);
//        erweima = createQRCodeBitmap("安全", 800, 800, "UTF-8",
//                "H", "1", CodeColor, Color.WHITE);
//        dimensional_barcode.setImageBitmap(erweima);
    }

    //展示新闻资讯
    public void ShowNews() {
//        ServerWeb.loadUrl("http://yqfk.sdau.edu.cn/");//加载url
//        ServerWeb.setVisibility(View.VISIBLE);
        vp.setVisibility(View.VISIBLE);
        ServerMapOperator.SetMapInVisiuability();
        CurrentPage = "news";
        ServerTime.setVisibility(View.GONE);
        dimensional_barcode.setVisibility(View.GONE);
        FreshTimeButton.setVisibility(View.GONE);
        ServerNews.setBackgroundColor(0XFFE0E1DD);
        ServerLocation.setBackgroundColor(0xFFFEFAE0);
        ServerCode.setBackgroundColor(0xFFFEFAE0);


    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    public void FreshTime() {

        FreshTimeButton.setVisibility(View.VISIBLE);

    }

    /**
     * 生成简单二维码
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param character_set          编码方式（一般使用UTF-8）
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @return BitMap
     */
    public static Bitmap createQRCodeBitmap(String content, int width, int height,
                                            String character_set, String error_correction_level,
                                            String margin, int color_black, int color_white) {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null;
        }
        try {
            /** 1.设置二维码相关配置 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            // 字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set);
            }
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin);
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象 */
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black;//黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white;// 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }


    //监听屏幕滑动事件
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if (velocityX > 0) {
            //判断当前是那个界面
            if (CurrentPage == "code") {
                ShowLocation();
            } else if (CurrentPage == "Location") {
                ShowNews();
            }
        } else if (velocityX < 0) {
            if (CurrentPage == "Location") {
                ShowHealth();
            } else if (CurrentPage == "news") {
                ShowLocation();
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return MyGestureDetector.onTouchEvent(event);
    }


    //按键按压事件更新
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == FreshTimeButton.getId()) {
            ShowHealth();
        }
        return false;
    }


    @SuppressLint("RestrictedApi")
    public void startWork() {
        //15分钟更新一次
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(MyWork.class,
                15, TimeUnit.MINUTES).addTag("GetLocationWork").build();
        WorkManager.getInstance(ServerActivity.this).enqueue(periodicWorkRequest);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //处理ActionBar元素的点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Ctrl+D复制行
        switch (item.getItemId()) {
            case android.R.id.home:
                View menu = findViewById(R.id.left_layout);
                menu.getBackground().setAlpha(200);
                if (mDrawerLayout.isDrawerOpen(menu)) {
                    mDrawerLayout.closeDrawer(menu);
                } else {

                    mDrawerLayout.openDrawer(menu);
                }
                break;
            case R.id.OpenService:
//                startWork();
//                Toast.makeText(this, "开启服务", Toast.LENGTH_SHORT).show();
                break;
            case R.id.CloseService:
//                WorkManager.getInstance(ServerActivity.this).cancelAllWork();
//                Toast.makeText(this, "关闭服务", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void GetUserHealthyState()
    {
        HealthyUpdateThread.runInThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                while (true) {
//                    Log.v("zj:", "执行线程！");
                    myHttpObject.GetUserHealthyState("zydzyd");
                    //改变用户码色
                    if(myHttpObject.UserHealthyState)
                    {
                        CodeColor = RedColor;
                    }
                    else
                    {
                        CodeColor = GreenColor;
                    }
                    //向handle发送信号，修改UI
                    handler.sendEmptyMessage(1);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //在定时操作UI组件的任务
    @SuppressLint({"SetTextI18n", "HandlerLeak"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void ServerInTime()
    {

        //创建消息处理器 接受子线程发送的消息  根据它做出处理，跟新主界面的值
        handler = new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@SuppressLint("HandlerLeak") Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1){
//                    Log.v("zj:","时间刷新任务");
                    Calendar c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);
                    int Hour = c.get(Calendar.HOUR_OF_DAY);
                    int Minute = c.get(Calendar.MINUTE);
                    int Second = c.get(Calendar.SECOND);

                    ServerTime.setText(mYear + "-" + mMonth + "-" + mDay + "  " + Hour
                            + ":" + Minute + ":" + Second);

                    erweima = createQRCodeBitmap("安全", 800, 800, "UTF-8",
                            "H", "1", CodeColor, Color.WHITE);
                    dimensional_barcode.setImageBitmap(erweima);
                }
            }
        };

    }


    private View news1 = null;
    private View news2 = null;
    private View news3 = null;


    //ViewPage适配器子类
    public class MyPagerAdapter extends PagerAdapter {
        private Context mContext;
        private List<View> viewList;


        public MyPagerAdapter(Context context ) {
            mContext = context;
            viewList = new ArrayList<>();

            news1 = View.inflate(mContext,R.layout.news_1,null);
            news2 = View.inflate(mContext,R.layout.news_2,null);
            news3 = View.inflate(mContext,R.layout.news_3,null);

            viewList.add(news1);
            viewList.add(news2);
            viewList.add(news3);
        }


        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            container.addView(viewList.get(position));
            return viewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(viewList.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }


    //设置ViewPage
    private void setVp() {
        vp.setAdapter(new MyPagerAdapter(ServerActivity.this));
    }



    //爬取网页新闻
    @SuppressLint("ResourceType")
    private void GetWebNews() {

        //设置新闻界面
        SetNewsPage1();
        SetNewsPage2();
//        SetNewsPage3();

    }

    private void SetNewsPage3() {
        //新闻内容设置
        List<String> news_3_data = new ArrayList<>();
        news_3_data.add("新闻1");
        news_3_data.add("新闻2");
        news_3_data.add("新闻3");
        news_3_data.add("新闻1");
        news_3_data.add("新闻2");
        news_3_data.add("新闻3");
        ListView news3_list;
        news3_list = news3.findViewById(R.id.new_list);
        news3_list.setAdapter(new MyNewsAdapter(ServerActivity.this,news_3_data));

    }

    private void SetNewsPage2() {
        //新闻内容设置
        List<String> news_2_data = new ArrayList<>();
        news_2_data.add("全国新冠核酸检测机构");
        news_2_data.add("陕西新冠核酸检测机构");
        news_2_data.add("卫生健康标准");
        news_2_data.add("国家卫生城镇");
        news_2_data.add("基本药物目录");
        news_2_data.add("卫生健康标准网");
        ListView news2_list = news2.findViewById(R.id.new_list);

        MyNewsAdapter news2Adapter =new MyNewsAdapter(ServerActivity.this,news_2_data,false);
        news2_list.setAdapter(news2Adapter);
        news2_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i=0;i<news2Adapter.news_list.size();i++)
                {
                    news2Adapter.myTextView.get(i).setBackgroundColor(0XFFE0E1DD);
                }
                news2Adapter.myTextView.get(position).setBackgroundColor(0XFFFFFFFF);
                TextView news2_content1 = news2.findViewById(R.id.news2_1);
                TextView news2_content2 = news2.findViewById(R.id.news2_2);
                TextView news2_content3 = news2.findViewById(R.id.news2_3);

                if(position == 0)
                {
                    news2_content1.setText("1、旬阳县疾病预防控制中心\n\n   电话：0915-7200567");
                    news2_content2.setText("2、榆林市疾病预防控制中心\n\n   电话：0912-2232514");
                    news2_content3.setText("3、防城港市中医医院\n\n   电话：0770-3276003");
                    news2_content1.setTextColor(0xff000000);
                    news2_content2.setTextColor(0xff000000);
                    news2_content3.setTextColor(0xff000000);

                }
                else if(position == 1)
                {
//                    news2_content.setText("\n1、更新《经批准开展人类辅助生殖技术和设置人类精子库的医疗机构名单》");
                }
                else if(position == 3)
                {

                }
//                news2_content.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG ); //下划线

//                Toast.makeText(ServerActivity.this,
//                        news2Adapter.news_list.get(position),Toast.LENGTH_SHORT).show();

            }
        });

    }

    //设置新闻界面1
    private void SetNewsPage1() {
        //新闻
        List<String> news_1_data = new ArrayList<>();
        news_1_data.add("新冠病毒疫苗接种情况");
        news_1_data.add("截至5月13日24时新型冠状病毒肺炎疫情最新情况");
        news_1_data.add("国务院联防联控机制2022年5月13日新闻发布会文字实录");
        news_1_data.add("新冠病毒疫苗接种情况");
        news_1_data.add("截至5月12日24时新型冠状病毒肺炎疫情最新情况");
        news_1_data.add("新冠病毒疫苗接种情况");
        ListView news1_list;
        news1_list = news1.findViewById(R.id.new_list);
        news1_list.setAdapter(new MyNewsAdapter(ServerActivity.this,news_1_data,true));

    }





}



