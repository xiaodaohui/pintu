package com.example.dell.pintu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.fansunion.puzzle.R;
import cn.fansunion.puzzle.adapter.MainGridViewAdapter;
import cn.fansunion.puzzle.util.ScreenUtil;

/**
 * 程序主界面：显示默认图片列表、自选图片按钮
 */
public class MainActivity extends Activity implements OnClickListener {

    // 返回码：系统图库
    private static final int RESULT_IMAGE = 100;
    // 返回码：相机
    private static final int RESULT_CAMERA = 200;
    // IMAGE TYPE
    private static final String IMAGE_TYPE = "image/*";
    // GridView 显示图片
    private GridView gridView;
    private List<Bitmap> bitmapList;
    // 主页图片资源ID
    private int[] photoResourceIdArray;
    // 显示Type
    private TextView selectedTypeTextView;
    private LayoutInflater layoutInflater;
    private PopupWindow popupWindow;
    private View popupView;
    private TextView typeSecondTextView;
    private TextView typeThirdTextView;
    private TextView typeFourthTextView;
    // 游戏类型N*N
    private int type = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xpuzzle_main);

        // 初始化Views
        initViews();
        // 数据适配器
        gridView.setAdapter(new MainGridViewAdapter(
                MainActivity.this, bitmapList));
        // Item点击监听
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long arg3) {
                if (position == photoResourceIdArray.length - 1) {
                    // 选择本地图库 相机
                    showDialogCustom();
                } else {
                    // 选择默认图片
                    Intent intent = new Intent(
                            MainActivity.this,
                            PuzzleMain.class);
                    intent.putExtra(GlobalConst.SELECT_PHOTO_ID, photoResourceIdArray[position]);
                    intent.putExtra(GlobalConst.TYPE, type);
                    startActivity(intent);
                }
            }
        });

        /**
         * 显示难度Type
         */
        selectedTypeTextView.setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 弹出popup window
                        popupShow(v);
                    }
                });
    }

    // 显示选择系统图库 相机对话框
    private void showDialogCustom() {
        // 本地图册、相机选择
        String[] customItemArray = new String[]{"本地图册", "相机拍照"};
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                MainActivity.this);
        dialogBuilder.setTitle("选择：");
        dialogBuilder.setItems(customItemArray,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0) {
                            // 本地图册
                            Intent intent = new Intent(
                                    Intent.ACTION_PICK, null);
                            intent.setDataAndType(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    IMAGE_TYPE);
                            startActivityForResult(intent, RESULT_IMAGE);
                        } else if (which==1) {
                            // 系统相机
                            Intent intent = new Intent(
                                    MediaStore.ACTION_IMAGE_CAPTURE);
                            Uri photoUri = Uri.fromFile(
                                    new File(GlobalConst.TEMP_IMAGE_PATH));
                            intent.putExtra(
                                    MediaStore.EXTRA_OUTPUT,
                                    photoUri);
                            startActivityForResult(intent, RESULT_CAMERA);
                        }
                    }
                });
        dialogBuilder.create().show();
    }

    /**
     * 调用图库相机回调方法
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_IMAGE && data != null) {
                // 相册
                Cursor cursor = this.getContentResolver().query(
                        data.getData(), null, null, null, null);
                cursor.moveToFirst();
                String imagePath = cursor.getString(
                        cursor.getColumnIndex("_data"));
                Intent intent = new Intent(
                        MainActivity.this,
                        PuzzleMain.class);
                intent.putExtra(GlobalConst.PHOTO_PATH, imagePath);
                intent.putExtra(GlobalConst.TYPE, type);
                cursor.close();
                startActivity(intent);
            } else if (requestCode == RESULT_CAMERA) {
                // 相机
                Intent intent = new Intent(
                        MainActivity.this,
                        PuzzleMain.class);
                intent.putExtra("photoPath", GlobalConst.TEMP_IMAGE_PATH);
                intent.putExtra(GlobalConst.TYPE, type);
                startActivity(intent);
            }
        }
    }

    /**
     * 显示popup window
     *
     * @param view popup window
     */
    private void popupShow(View view) {
        int density = (int) ScreenUtil.getDeviceDensity(this);
        // 显示popup window
        popupWindow = new PopupWindow(popupView,
                200 * density, 50 * density);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        // 透明背景
        Drawable transpent = new ColorDrawable(Color.TRANSPARENT);
        popupWindow.setBackgroundDrawable(transpent);
        // 获取位置
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        popupWindow.showAtLocation(
                view,
                Gravity.NO_GRAVITY,
                location[0] - 40 * density,
                location[1] + 30 * density);
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        bitmapList = new ArrayList<Bitmap>();
        gridView = (GridView) findViewById(
                R.id.gv_xpuzzle_main_pic_list);
        // 初始化Bitmap数据
        photoResourceIdArray = new int[]{
                R.drawable.pic1, R.drawable.pic2, R.drawable.pic3,
                R.drawable.pic4, R.drawable.pic5, R.drawable.pic6,
                R.drawable.pic7, R.drawable.pic8, R.drawable.pic9,
                R.drawable.pic10, R.drawable.pic11, R.drawable.pic12,
                R.drawable.pic13, R.drawable.pic14,
                R.drawable.pic15, R.mipmap.ic_launcher};
        Bitmap[] bitmapArray = new Bitmap[photoResourceIdArray.length];
        for (int index = 0; index < bitmapArray.length; index++) {
            bitmapArray[index] = BitmapFactory.decodeResource(
                    getResources(), photoResourceIdArray[index]);
            bitmapList.add(bitmapArray[index]);
        }
        // 显示type
        selectedTypeTextView = (TextView) findViewById(
                R.id.tv_puzzle_main_type_selected);
        layoutInflater = (LayoutInflater) getSystemService(
                LAYOUT_INFLATER_SERVICE);
        // mType view
        popupView = layoutInflater.inflate(
                R.layout.xpuzzle_main_type_selected, null);
        typeSecondTextView = (TextView) popupView.findViewById(R.id.tv_main_type_2);
        typeThirdTextView = (TextView) popupView.findViewById(R.id.tv_main_type_3);
        typeFourthTextView = (TextView) popupView.findViewById(R.id.tv_main_type_4);
        // 监听事件
        typeSecondTextView.setOnClickListener(this);
        typeThirdTextView.setOnClickListener(this);
        typeFourthTextView.setOnClickListener(this);
    }

    /**
     * popup window item点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Type
            case R.id.tv_main_type_2:
                type = 2;
                selectedTypeTextView.setText("2 X 2");
                break;
            case R.id.tv_main_type_3:
                type = 3;
                selectedTypeTextView.setText("3 X 3");
                break;
            case R.id.tv_main_type_4:
                type = 4;
                selectedTypeTextView.setText("4 X 4");
                break;
            default:
                break;
        }
        popupWindow.dismiss();
    }
}
