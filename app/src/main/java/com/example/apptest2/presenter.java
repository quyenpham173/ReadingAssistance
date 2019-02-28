package com.example.apptest2;

import android.graphics.Bitmap;
import android.os.Environment;

import com.google.api.services.vision.v1.model.Feature;

/**
 * Created by LOC on 10/24/2017.
 */

public class presenter implements  CallbackModel{

    private ActivityView activityView;
    private LoadImage loadImage;
    private ModelInternet modelInternet;
    public presenter(ActivityView activityView) {
        this.activityView = activityView;
        loadImage = new LoadImage(this);
        modelInternet =new ModelInternet(this);
    }
    private Feature feature;

    public void captureImage(){
        loadImage.loadImageFromStorage(Environment.getExternalStorageDirectory().getPath());//path dan den file luu anh

    }

    @Override
    public void sendImageView(Bitmap bitmapConvert) {
        feature = new Feature();
        feature.setType("TEXT_DETECTION");
        feature.setMaxResults(10);
        activityView.displayImage(bitmapConvert);
        modelInternet.callCloudVision(bitmapConvert, feature );
    }

    @Override
    public void sendDataView(String data) {
        activityView.displayData(data);
    }

}
