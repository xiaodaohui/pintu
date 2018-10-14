package com.example.dell.pintu;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.dell.pintu.R;
import com.example.dell.pintu.PuzzleGridViewAdapter;
import com.example.dell.pintu.GridItem;
import com.example.dell.pintu.GameUtil;
import com.example.dell.pintu.ImageUtil;
import com.example.dell.pintu.ScreenUtil;

/**
 * 拼图逻辑主界面：面板显示
 */
public class PuzzleMain extends Activity implements OnClickListener {

    // 拼图完成时显示的最后一个图片
    public static Bitmap lastBitmap;
    // 设置为N*N显示
    public static int TYPE = 2;
    // 步数显示
    private  int stepCount = 0;
    // 计时显示
    private  int timerSeconds = 0;
    /**
     * UI更新Handler
     */
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    // 更新计时器
                    timerSeconds++;
                    timerTextView.setText("" + timerSeconds);
                    break;
                default:
                    break;
            }
        }
    };
    // 选择的图片
    private Bitmap photoSelected;
    // PuzzlePanel
    private GridView gridView;
    private int resourceId;
    private String photoPath;
    private ImageView imageView;
    // Button
    private Button backButton;
    private Button imageButton;
    private Button restartButton;
    // 显示步数
    private TextView stepCountTextView;
    // 计时器
    private TextView timerTextView;
    // 切图后的图片
    private List<Bitmap> bitmapItemList = new ArrayList<Bitmap>();
    // GridView适配器
    private PuzzleGridViewAdapter puzzleGridViewAdapter;
    // Flag 是否已显示原图
    private boolean showOriginalImage;
    // 计时器类
    private Timer timer;
    /**
     * 计时器线程
     */
    private TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xpuzzle_puzzle_detail_main);
        // 获取选择的图片
        Bitmap photoSelectTemp=null;
        // 选择默认图片还是自定义图片
        resourceId = getIntent().getExtras().getInt(GlobalConst.SELECT_PHOTO_ID);
        photoPath = getIntent().getExtras().getString(GlobalConst.PHOTO_PATH);
        if (resourceId != 0) {
            photoSelectTemp = BitmapFactory.decodeResource(
                    getResources(), resourceId);
        } else {
            photoSelectTemp = BitmapFactory.decodeFile(photoPath);
        }
        TYPE = getIntent().getExtras().getInt(GlobalConst.TYPE, 2);
        // 对图片处理
        photoSelected=selfAdaptionImage(photoSelectTemp);
        // 初始化Views
        initViews();
        // 生成游戏数据
        generateGame();
        // GridView点击事件
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long arg3) {
                // 判断是否可移动
                if (GameUtil.isMoveable(position)) {
                    // 交换点击Item与空格的位置
                    GameUtil.swapItems(
                            GameUtil.gridItemList.get(position),
                            GameUtil.blankGridItem);
                    // 重新获取图片
                    recreateData();
                    // 通知GridView更改UI
                    puzzleGridViewAdapter.notifyDataSetChanged();
                    // 更新步数
                    stepCount++;
                    stepCountTextView.setText("" + stepCount);
                    // 判断是否成功
                    if (GameUtil.isSuccess()) {
                        // 将最后一张图显示完整
                        recreateData();
                        bitmapItemList.remove(TYPE * TYPE - 1);
                        bitmapItemList.add(lastBitmap);
                        // 通知GridView更改UI
                        puzzleGridViewAdapter.notifyDataSetChanged();
                        Toast.makeText(PuzzleMain.this, "拼图成功!",
                                Toast.LENGTH_LONG).show();
                        gridView.setEnabled(false);
                        timer.cancel();
                        timerTask.cancel();
                    }
                }
            }
        });
        // 返回按钮点击事件
        backButton.setOnClickListener(this);
        // 显示原图按钮点击事件
        imageButton.setOnClickListener(this);
        // 重置按钮点击事件
        restartButton.setOnClickListener(this);
    }

    /**
     * Button点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 返回按钮点击事件
            case R.id.btn_puzzle_main_back:
                PuzzleMain.this.finish();
                break;
            // 显示原图按钮点击事件
            case R.id.btn_puzzle_main_img:
                Animation animShow = AnimationUtils.loadAnimation(
                        PuzzleMain.this, R.anim.image_show_anim);
                Animation animHide = AnimationUtils.loadAnimation(
                        PuzzleMain.this, R.anim.image_hide_anim);
                if (showOriginalImage) {
                    imageView.startAnimation(animHide);
                    imageView.setVisibility(View.GONE);
                    showOriginalImage = false;
                } else {
                    imageView.startAnimation(animShow);
                    imageView.setVisibility(View.VISIBLE);
                    showOriginalImage = true;
                }
                break;
            // 重置按钮点击事件
            case R.id.btn_puzzle_main_restart:
                cleanConfig();
                generateGame();
                recreateData();
                // 通知GridView更改UI
                stepCountTextView.setText("" + stepCount);
                puzzleGridViewAdapter.notifyDataSetChanged();
                gridView.setEnabled(true);
                break;
            default:
                break;
        }
    }

    /**
     * 生成游戏数据
     */
    private void generateGame() {
        // 切图 获取初始拼图数据 正常顺序
        ImageUtil.createInitBitmaps(
                TYPE, photoSelected, PuzzleMain.this);
        // 生成随机数据
        GameUtil.getPuzzleGenerator();
        // 获取Bitmap集合
        for (GridItem gridItem : GameUtil.gridItemList) {
            bitmapItemList.add(gridItem.getBitmap());
        }
        // 数据适配器
        puzzleGridViewAdapter = new PuzzleGridViewAdapter(this, bitmapItemList);
        gridView.setAdapter(puzzleGridViewAdapter);
        // 启用计时器
        timer = new Timer(true);
        // 计时器线程
        timerTask = new TimerTask() {

            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        };
        // 每1000ms执行 延迟0s
        timer.schedule(timerTask, 0, 1000);
    }

    /**
     * 添加显示原图的View
     */
    private void addShowOriginalPhotoView() {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(
                R.id.rl_puzzle_main_main_layout);
        imageView = new ImageView(PuzzleMain.this);
        imageView.setImageBitmap(photoSelected);
        int x = (int) (photoSelected.getWidth() * 0.9F);
        int y = (int) (photoSelected.getHeight() * 0.9F);
        LayoutParams params = new LayoutParams(x, y);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageView.setLayoutParams(params);
        relativeLayout.addView(imageView);
        imageView.setVisibility(View.GONE);
    }

    /**
     * 返回时调用
     */
    @Override
    protected void onStop() {
        super.onStop();
        // 清空相关参数设置
        cleanConfig();
        this.finish();
    }

    /**
     * 清空相关参数设置
     */
    private void cleanConfig() {
        // 清空相关参数设置
        GameUtil.gridItemList.clear();
        // 停止计时器
        timer.cancel();
        timerTask.cancel();
        stepCount = 0;
        timerSeconds = 0;
        // 清除拍摄的照片
        if (photoPath != null) {
            // 删除照片
            File file = new File(GlobalConst.TEMP_IMAGE_PATH);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 重新获取图片
     */
    private void recreateData() {
        bitmapItemList.clear();
        for (GridItem temp : GameUtil.gridItemList) {
            bitmapItemList.add(temp.getBitmap());
        }
    }

    /**
     * 对图片处理 自适应大小
     */
    private Bitmap selfAdaptionImage(Bitmap bitmap) {
        // 将图片放大到固定尺寸
        int screenWidth = ScreenUtil.getDisplayMetrics(this).widthPixels;
        int screenHeigt = ScreenUtil.getDisplayMetrics(this).heightPixels;
        return ImageUtil.resizeBitmap(
                screenWidth * 0.8f, screenHeigt * 0.6f, bitmap);
    }

    /**
     * 初始化Views
     */

    private void initViews() {
        // Button
        backButton = (Button) findViewById(R.id.btn_puzzle_main_back);
        imageButton = (Button) findViewById(R.id.btn_puzzle_main_img);
        restartButton = (Button) findViewById(R.id.btn_puzzle_main_restart);
        // Flag 是否已显示原图
        showOriginalImage = false;
        // GridView
        gridView = (GridView) findViewById(
                R.id.gv_puzzle_main_detail);
        // 设置为N*N显示
        gridView.setNumColumns(TYPE);
        LayoutParams layoutParams = new LayoutParams(
                photoSelected.getWidth(),
                photoSelected.getHeight());
        // 水平居中
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        // 其他格式属性
        layoutParams.addRule(
                RelativeLayout.BELOW,
                R.id.ll_puzzle_main_spinner);
        // Grid显示
        gridView.setLayoutParams(layoutParams);
        gridView.setHorizontalSpacing(0);
        gridView.setVerticalSpacing(0);
        // TV步数
        stepCountTextView = (TextView) findViewById(
                R.id.tv_puzzle_main_counts);
        stepCountTextView.setText(stepCount+"");

        // TV计时器
        timerTextView = (TextView) findViewById(R.id.tv_puzzle_main_time);
        timerTextView.setText("0秒");
        // 添加显示原图的View
        addShowOriginalPhotoView();
    }
}
