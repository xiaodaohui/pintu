package com.example.dell.pintu;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GlobalConst {
    public static final String TYPE = "type";
    public static final String SELECT_PHOTO_ID = "selectPhotoId";
    public static final String PHOTO_PATH = "photoPath";
    // Temp照片路径
    public static String TEMP_IMAGE_PATH  =
            Environment.getExternalStorageDirectory().getPath() +
                    "/temp.png";
}