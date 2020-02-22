package com.bw.information10asample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends BaseAdapter {


    private List<LawyerBean.ListdataBean> listdata;

    public MyAdapter(List<LawyerBean.ListdataBean> listdata) {

        this.listdata = listdata;
    }

    @Override
    public int getCount() {
        return listdata.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler viewHodler = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lawyer, parent, false);

            viewHodler = new ViewHodler();

            viewHodler.checkBox = convertView.findViewById(R.id.cb);
            viewHodler.name = convertView.findViewById(R.id.tv_name);
            viewHodler.info = convertView.findViewById(R.id.tv_info);

            convertView.setTag(viewHodler);
        } else {
            viewHodler = (ViewHodler) convertView.getTag();
        }

        //获取数据
        LawyerBean.ListdataBean listdataBean = listdata.get(position);

        //给控件绑定数据
        viewHodler.name.setText(listdataBean.getName());
        viewHodler.info.setText(listdataBean.getInfo());

        //给checkbox设置一个 boolean 值
        viewHodler.checkBox.setChecked(listdataBean.getType() == 1);

        return convertView;
    }


    class ViewHodler {
        CheckBox checkBox;
        TextView name;
        TextView info;
    }
}
