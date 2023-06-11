package com.example.NewsAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hope.R;

import java.util.ArrayList;
import java.util.List;

public class MyNewsAdapter extends BaseAdapter {

        Context context;
        public List<String> news_list;
        public List<TextView> myTextView;
        boolean NewsType;
        int LastItem = 0;




    public MyNewsAdapter(Context Activity_This, List<String> Pass_Data)
    {
        context   = Activity_This;
        news_list = Pass_Data;
        myTextView = new ArrayList<>();

    }

    public MyNewsAdapter(Context Activity_This, List<String> Pass_Data,boolean NewsType)
    {
        context   = Activity_This;
        news_list = Pass_Data;
        this.NewsType = NewsType;
        myTextView = new ArrayList<>();
    }


    @Override
    public int getCount() {
//        Log.v("zj:长度为", String.valueOf(news_list.size()));
        if(NewsType)
            return 5;
        else
            return news_list.size();
    }



    @Override
    public Object getItem(int position) {
        return news_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(NewsType)
        {
            //防止view不停的新建
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.item_news,parent,false);
            }
            News1_deal(convertView,position);
        }
        else
        {
            //防止view不停的新建
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.item_news2,parent,false);
            }
            News2_deal(convertView,position);
        }
        return convertView;
    }


    public void News1_deal(View convertView,int position)
    {
        ImageView imageView = convertView.findViewById(R.id.Left_image);
        if(position == 0)
            imageView.setImageResource(R.drawable.news1_head1);
        else if(position == 1)
            imageView.setImageResource(R.drawable.news1_2);
        else if(position == 2)
            imageView.setImageResource(R.drawable.news1_3);
        else if(position == 3)
            imageView.setImageResource(R.drawable.news1_4);
        else if(position == 4)
            imageView.setImageResource(R.drawable.news1_5);
    }

    @SuppressLint("SetTextI18n")
    public void News2_deal(View convertView, int position)
    {
        TextView textView = convertView.findViewById(R.id.FunName);
        textView.setText(news_list.get(position));
        myTextView.add(textView);
    }

}
