package com.example.apptest2;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.apptest.R;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ImageViewBitmap extends AppCompatActivity implements ActivityView {
    private com.example.apptest2.presenter presenter;
    private ImageView imageView;
    private TextToSpeech textToSpeech;
    private TextView loadingText;
    private TextView textClockView;
    private Uri outputFileDi;
    ImageToText imageToText;
    private TextView textTessaractView;

    private static int count;
    final Timer T = new Timer();

    final String textWaiting = "Waiting Processing!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_bitmap);
        //Khởi tạo Presenter
        initPresenter();

        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        this.loadingText=(TextView)this.findViewById(R.id.loadingText);
        imageView = (ImageView) findViewById(R.id.imageView);
        textClockView = (TextView)findViewById(R.id.textClock);
        textTessaractView = (TextView)findViewById(R.id.textViewTessaract);
        //Gợi đến phương thức để load và gửi dữ liệu lên Google Cloud Vision
        presenter.captureImage();
        //Thiết lập đồng hồ đo thời gian gửi
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        textClockView.setText("count="+count);
                        loadingText.setText(textWaiting);
                        textToSpeech.speak(textWaiting, TextToSpeech.QUEUE_FLUSH, null);
                        count++;

                    }
                });
            }
        }, 1000, 1000);
    }
    //Phương thức khởi tạo Class presenter
    private void initPresenter(){
        presenter = new presenter(this);
    }
    //Phương thức hiển thị ảnh crop được
    @Override
    public void displayImage(Bitmap bitmapConvert) {
        imageView.setImageBitmap(bitmapConvert);
        imageToText=new ImageToText(this,textTessaractView);
        imageToText.execute(bitmapConvert);
    }
    //Phương thức hiển thị nội dung bức ảnh sau khi xử lý
    @Override
    public void displayData(String data) {
        T.cancel();
        count = 0;
        loadingText.setText(data);
        textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null);

    }
}
