package com.example.apptest2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by LOC on 10/29/2017.
 */

public class LoadImage {
    private CallbackModel listener;
    public LoadImage(CallbackModel listener){
        this.listener = listener;
    }
    public void loadImageFromStorage(String path)
    {
        try {
            File f = new File(path, "profile.jpg");
            Bitmap imageSend = BitmapFactory.decodeStream(new FileInputStream(f));
            listener.sendImageView(imageSend);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }
}
