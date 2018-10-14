package com.example.dell.pintu;

import android.R.color;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

import com.example.dell.pintu.ScreenUtil;

/**
 * 程序主界面数据适配器
 *
 */
public class GridViewAdapter extends BaseAdapter {

    // 映射List
    private List<Bitmap> bitmapList;
    private Context context;

    public GridViewAdapter(Context context, List<Bitmap> bitmapList) {
        this.context = context;
        this.bitmapList = bitmapList;
    }

    @Override
    public int getCount() {
        return bitmapList.size();
    }

    @Override
    public Object getItem(int position) {
        return bitmapList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ImageView imageView = null;
        int density = (int) ScreenUtil.getDeviceDensity(context);
        if (convertView == null) {
            imageView = new ImageView(context);
            // 设置布局 图片
            imageView.setLayoutParams(new GridView.LayoutParams(
                    80 * density,
                    100 * density));
            // 设置显示比例类型
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setBackgroundColor(color.black);
        imageView.setImageBitmap(bitmapList.get(position));
        return imageView;
    }
}
