package com.example.MapServer;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.ServiceSettings;

public class MapOperateObject extends AppCompatActivity {

    //AMap是地图对象
    MapView mMapView = null;
    AMap aMap = null;
    MyLocationStyle myLocationStyle = null;
    //构造函数
    public MapOperateObject(Context ActivityThis, Bundle MapBundle, MapView ThisMapView)
    {
        //请求APP使用规范（必须，不然显示不正常）
        ServiceSettings.updatePrivacyShow(ActivityThis, true, true);
        ServiceSettings.updatePrivacyAgree(ActivityThis,true);

        mMapView = ThisMapView;
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(MapBundle);
        SetMapInVisiuability();
        //初始化地图控制器对象
        aMap = mMapView.getMap();
        //定位小蓝点
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
        aMap.setMyLocationStyle(myLocationStyle);

    }

    public void SetMapVisiuability()
    {
        //显示地图
        mMapView.setVisibility(View.VISIBLE);
        SetMarkShow(10,11);
    }
    public void SetMapInVisiuability()
    {
        //不显示地图
        mMapView.setVisibility(View.GONE);
        SetMarkInShow();
    }


    //显示当前定位标记
    public void SetMarkShow(double lon,double lat)
    {
        LatLng latLng = new LatLng(39.906901,116.397972);
        aMap.addMarker(new MarkerOptions().position(latLng).title("当前位置").snippet("DefaultMarker"));
    }

    //不显示当前定位标记
    public void SetMarkInShow()
    {

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



}
