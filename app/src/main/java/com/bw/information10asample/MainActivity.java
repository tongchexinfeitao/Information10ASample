package com.bw.information10asample;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.img);
        listView = findViewById(R.id.lv);


        //构造联网工具类
        final NetUtil netUtil = new NetUtil();

        // TODO: 2020/2/21 判断是否有网
        if (netUtil.hasNet(this)) {
            //有网
            //去做get请求
            netUtil.doGet("http://blog.zhaoliang5156.cn/api/news/lawyer.json", new NetUtil.MyCallback() {
                @Override
                public void onDoGetSuccess(final String json) {

                    //利用Gson，将 json 转换成 bean
                    LawyerBean lawyerBean = new Gson().fromJson(json, LawyerBean.class);

                    // TODO: 2020/2/21 拿到图片地址
                    List<LawyerBean.BannerdataBean> bannerdata = lawyerBean.getBannerdata();
                    LawyerBean.BannerdataBean bannerdataBean = bannerdata.get(0);
                    String imageUrl = bannerdataBean.getImageUrl();

                    // TODO: 2020/2/21 请求图片
                    netUtil.doGetPhoto(imageUrl, imageView);

                    List<LawyerBean.ListdataBean> listdata = lawyerBean.getListdata();
                    MyAdapter myAdapter = new MyAdapter(listdata);
                    listView.setAdapter(myAdapter);
                }

                @Override
                public void onDoGetError() {
                    Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //无网
            Toast.makeText(MainActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
        }
    }

}
