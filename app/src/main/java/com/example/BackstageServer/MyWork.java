package com.example.BackstageServer;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.DataServer.DataOperate;
import com.example.hope.MainActivity;
import com.example.hope.R;
import com.example.hope.ServerActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyWork extends Worker {
    public int i=0,tree_len=4;
    String a="",b="",c="",d="",url0="http://10.198.170.205:8080",url1="http://10.198.170.205:8081";
    String name, password, session,uuid;
    Context context=this.getApplicationContext();
    private static final String UserSQLName = "UserSQL";
    private final String UserTableName = "T_UserInfo";
    //用户数据管理
    DataOperate MyDataObject;

    public MyWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        MyDataObject = new DataOperate();
    }

    @NonNull
    @Override
    public Result doWork() {
        init();
        d=StorageLocation(this.context);
        dataSender(d);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService
                (Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "chat";
        //适配8.0
        if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "聊天信息",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        /**
         * 注意写上 channel_id，适配8.0，不用担心8.0以下的，找不到 channel_id 不影响程序
         */
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("location-track")
                .setContentText("I am a notice!"+d)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_user)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));;

        //通过 builder.build() 拿到 notification
        mNotificationManager.notify(i, mBuilder.build());
        i=i+1;
        Log.e("调试_临时_log", "this_doWork");
        return Result.success();//结果返回为成功
    }

    //    实现wgs84坐标转墨卡托坐标
    public int[] wgs842mercator(double lng,double lat){
        int b[]=new int[2];
        double x=lng*20037508.34/180;
        double y=Math.log(Math.tan((90+lat)*Math.PI/360))/(Math.PI/180)*20037508.34/180;
        b[0]=(int)x;
        b[1]=(int)y;
        return b;
    }

    //    获取公钥函数
    public String[] get_pubkey(){
        try{
            String u=url0+"/pubkey";
            URL url=new URL(u);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line= bufferedReader.readLine();
            System.out.println(line);
            connection.disconnect();

            String[] split=line.split(",");
            BigInteger b[]=new BigInteger[2];
            b[0] =new BigInteger(split[0],16);
            b[1] =new BigInteger(split[1],16);
            Log.e("16",b[1].toString());
            String bb[]=new String[2];
            bb[0]=b[0].toString();
            bb[1]=b[1].toString();
            return bb;
        } catch (IOException e){
            Log.e("error",e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    //  获取坐标所在四叉树节点信息
    public void xy_to_id(int x1,int y1,int x2,int y2,int xx,int yy,StringBuilder tree_id){
        int x_core = x1 / 2 + x2 / 2;
        int y_core = y1 / 2 + y2 / 2;
        if (judge_part(x1,y1,x_core,y_core,xx,yy) ){
            tree_id.append("1");
            Log.e(String.valueOf(tree_len), "1");
            if(tree_len>1){
                tree_len = tree_len -1;
                xy_to_id(x1,y1,x_core,y_core,xx,yy,tree_id);
            }
        }
        if (judge_part(x_core,y_core,x2,y2,xx,yy) ){
            tree_id.append("4");
            Log.e(String.valueOf(tree_len), "4");
            if(tree_len>1){
                tree_len = tree_len -1;
                xy_to_id(x_core,y_core,x2,y2,xx,yy,tree_id);
            }
        }
        if (judge_part(x1,y_core,x_core,y2,xx,yy) ){
            tree_id.append("3");
            Log.e(String.valueOf(tree_len), "3");
            if(tree_len>1){
                tree_len = tree_len -1;
                xy_to_id(x1,y_core,x_core,y2,xx,yy,tree_id);
            }
        }
        if (judge_part(x_core,y1,x2,y_core,xx,yy) ){
            tree_id.append("2");
            Log.e(String.valueOf(tree_len), "2");
            if(tree_len>1){
                tree_len = tree_len -1;
                xy_to_id(x_core,y1,x2,y_core,xx,yy,tree_id);
            }
        }
//        return tree_id;
    }
    public boolean judge_part(int upx,int upy,int dwx,int dwy,int x,int y){
        if((upx - x) * (dwx - x) <= 0 && (upy - y) * (dwy - y) <= 0){
            return true;
        }
        else return false;
    }

    public int strid_to_inum(StringBuilder s){
        int res;
        res = ((int)(s.charAt(0))-(int)('0')-1)*256+
                ((int)(s.charAt(1))-(int)('0')-1)*64+
                ((int)(s.charAt(2))-(int)('0')-1)*16+
                ((int)(s.charAt(3))-(int)('0')-1)*4+
                ((int)(s.charAt(4))-(int)('0')-1);
        return res;
    }

    //    用于发送数据
    private void dataSender(String content) {
        //第一步创建OKHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        //第二步创建RequestBody（Form表达）
        Map m = new HashMap();
        m.put("msg", content);
//        m.put("password", "sdf");
        JSONObject jsonObject = new JSONObject(m);
        String jsonStr = jsonObject.toString();
        RequestBody requestBodyJson = RequestBody.create(MediaType.parse("application/json;charset=utf-8")
                , jsonStr);
        //第三步创建Rquest
        Request request = new Request.Builder()
                .url(url1+"/api")
                .addHeader("contentType", "application/json;charset=UTF-8")
                .post(requestBodyJson)
                .build();
        //第四步创建call回调对象
        final Call call = client.newCall(request);
        //第五步发起请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("onFailure", e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                Log.e("result", result);
            }

        });
    }

    //    PSHE加密,输入墨卡托整数坐标，返回加密的【x,y】
    public String[] PSHE_en(int[] xy){
        String[] c1=get_pubkey();
        Log.e("pubkey",c1[0]+","+c1[1]);
        BigInteger aa=new BigInteger(c1[0]);
        BigInteger bb=new BigInteger(c1[1]);
        Random r=new Random(1000);
        Random rr=new Random(12);
        BigInteger  rl=new BigInteger(String.valueOf(r.nextInt((int)Math.pow(2,99)))+(int)Math.pow(2,99)).abs();
        BigInteger  rrl=new BigInteger(String.valueOf(rr.nextInt((int)Math.pow(2,100)))+(int)Math.pow(2,99)).abs();
        BigInteger xxyy[]= new BigInteger[2];
        xxyy[0]=BigInteger.valueOf(xy[0]);
        xxyy[1]=BigInteger.valueOf(xy[1]);
        String cc[]=new String[2];
        cc[0]=xxyy[0].add(rl.multiply(aa)).add(rrl.multiply(bb)).toString();
        cc[1]=xxyy[1].add(rl.multiply(aa)).add(rrl.multiply(bb)).toString();
        return cc;
    }

    //    PSHE加密,输入墨卡托整数坐标，返回加密的【x,y】
    public String tree_en(String treeid, String[] treeids){
        String[] c1=get_pubkey();
        BigInteger aa=new BigInteger(c1[0]);
        BigInteger bb=new BigInteger(c1[1]);
        Random r=new Random(1000);
        Random rr=new Random(12);
        BigInteger rl=new BigInteger(String.valueOf(r.nextInt((int)Math.pow(2,99)))+(int)Math.pow(2,99)).abs();
        BigInteger rrl=new BigInteger(String.valueOf(rr.nextInt((int)Math.pow(2,100)))+(int)Math.pow(2,99)).abs();

        BigInteger id=new BigInteger(treeid);
        StringBuilder ret_content=new StringBuilder();
        treeid=id.add(rl.multiply(aa)).add(rrl.multiply(bb)).toString();
        ret_content.append(treeid+",");
        for(int k=0;k<treeids.length;k++){
            BigInteger id_4 = new BigInteger(treeids[k]);
            ret_content.append(id_4.add(rl.multiply(aa)).add(rrl.multiply(bb)).toString());
            ret_content.append(";");
        }
        return ret_content.toString();
    }

    DataOperate MyDataOperate;
    //    初始化函数,此处获取登录后获取的凭证，比如name，password和session，用于后续上传数据
    private void init() {
        MyDataOperate = new DataOperate();
        DataOperate.DBHelper test = MyDataOperate.new DBHelper(getApplicationContext(),
                "UserSQL", null, 1);
        SQLiteDatabase db = test.getWritableDatabase();
        Cursor cursor = db.query("T_UserInfo", new String[]{"name","password","uuid","session"}, null, null, null, null, null);
        if (cursor.moveToNext()) {
            name = cursor.getString(0);
            password = cursor.getString(1);
            uuid    = cursor.getString(2);
            session = cursor.getString(3);
        }
        db.close();
    }
    @SuppressLint("MissingPermission")
    public String StorageLocation(Context context){
        String locationProvider;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);


        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
            Log.e("back","GPS");
        }
        else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
            Log.e("back","NETWORK");
        }
        else if (providers.contains(LocationManager.PASSIVE_PROVIDER)){
            locationProvider = LocationManager.PASSIVE_PROVIDER;
            Log.e("back","PASSIVE");
        }
        else {
            Log.e("back","NO");
            return "没位置权限";
        }
        if (Looper.myLooper() ==null){
            Looper.prepare();}
        locationManager.requestLocationUpdates(
                locationProvider,
                1000,
                1,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        LocationListener.super.onStatusChanged(provider, status, extras);
                    }

                    @Override
                    public void onProviderEnabled(@NonNull String provider) {
                        LocationListener.super.onProviderEnabled(provider);
                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {
                        LocationListener.super.onProviderDisabled(provider);
                    }
                }
        );
        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(locationProvider);


        String mes = locationUpdates(location);
        return mes;
    }

    public String locationUpdates(Location location){
        if (location!=null){
            double wgs84x = location.getLongitude();
            double wgs84y = location.getLatitude();

            //add by zj
            Log.v("zj:", "经度"+ wgs84x);
            Log.v("zj:", "纬度"+ wgs84y);
            //新建数据库管理对象
//            DataOperate.DBHelper test = MyDataObject.new DBHelper(context,UserSQLName,
//                    null,1);
            //获取写入数据库的权限
//            SQLiteDatabase db = test.getWritableDatabase();
//            //获取Table光标
//            Cursor MyCursor = db.query(UserTableName,null,null,null,
//                    null,null,null);
            //获取当前用户的UUID
//            @SuppressLint("Range") String uuid = MyCursor.getString(MyCursor.getColumnIndex("uuid"));
            Log.v("zj:",uuid);

//            下面这条将GPS坐标转换成墨卡托整数
            int[] mercator_loaction=wgs842mercator(wgs84x,wgs84y);
            String x_y[]=PSHE_en(mercator_loaction);

//            获取当前位置的子树信息
//            下面两行删除
            mercator_loaction[0] = 11966845+300;
            mercator_loaction[1] = 4096139-300;

            StringBuilder tree0 = new StringBuilder();
            tree_len = 4;
            xy_to_id(11966845, 4096139, 12078164, 4028802,
                    mercator_loaction[0],mercator_loaction[1],tree0);
            int id0=strid_to_inum(tree0);

            String[] treeids = new String[4];
            StringBuilder tree1 = new StringBuilder();
            tree_len = 4;
            xy_to_id(11966845, 4096139, 12078164, 4028802,
                    mercator_loaction[0]-200,mercator_loaction[1]-200,tree1);
            int id1 = strid_to_inum(tree1);
            treeids[0]=String.valueOf(id1);

            StringBuilder tree2 = new StringBuilder();
            tree_len = 4;
            xy_to_id(11966845, 4096139, 12078164, 4028802,
                    mercator_loaction[0]-200,mercator_loaction[1]+200,tree2);
            int id2 = strid_to_inum(tree2);
            treeids[1]=String.valueOf(id2);

            StringBuilder tree3 = new StringBuilder();
            tree_len = 4;
            xy_to_id(11966845, 4096139, 12078164, 4028802,
                    mercator_loaction[0]+200,mercator_loaction[1]-200,tree3);
            int id3 =strid_to_inum(tree3);
            treeids[2]=String.valueOf(id3);

            StringBuilder tree4 = new StringBuilder();
            tree_len = 4;
            xy_to_id(11966845, 4096139, 12078164, 4028802,
                    mercator_loaction[0]+200,mercator_loaction[1]-200,tree4);
            int id4 = strid_to_inum(tree4);
            treeids[3]=String.valueOf(id4);
            Log.e("trees",treeids.toString());

            StringBuilder stbuild = new StringBuilder();
            stbuild.append((int)(System.currentTimeMillis()/1000));
            stbuild.append(",");
            stbuild.append(uuid);
            stbuild.append(",");
            stbuild.append(x_y[0]);
            stbuild.append(",");
            stbuild.append(x_y[1]);
            stbuild.append(",");
            stbuild.append(tree_en(String.valueOf(id0), treeids));
            Log.e("msg",stbuild.toString());
            return stbuild.toString();
        }
        else{
            return "save error";
        }
    }
}
