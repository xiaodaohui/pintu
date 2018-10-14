package com.example.dell.pintu;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

/**
 * 拼图主界面数据适配器
 *
 */
public class PuzzleGridViewAdapter extends BaseAdapter {

    // 映射List
    private List<Bitmap> bitmapList;
    private Context context;

    public PuzzleGridViewAdapter(Context context, List<Bitmap> picList) {
        this.context = context;
        this.bitmapList = picList;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = null;
        if (convertView == null) {
            imageView = new ImageView(context);
            // 设置布局 图片
            imageView.setLayoutParams(new GridView.LayoutParams(
                    bitmapList.get(position).getWidth(),
                    bitmapList.get(position).getHeight()));
            // 设置显示比例类型
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageBitmap(bitmapList.get(position));
        return imageView;
    }
}
