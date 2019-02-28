package com.example.apptest2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.apptest.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;

public class ImageCapture extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG = "ImageCapture";
    private static final Double MIN_VAL = (double) 30;
    private static Mat cropImage;
    Bitmap bitmapConvert;
    Mat mRgba, imgGray, imgCanny;
    JavaCameraView javaCameraView;
    TextView txtView;
    double maxContourArea;
    Boolean bool = true;
    Mat mRgbaT;
    //    private presenter presenter;
    Point[] Edge1 = new Point[2];
    int count1 = 0;
    Point[] Edge2 = new Point[2];
    int count2 = 0;
    Point[] Edge3 = new Point[2];
    int count3 = 0;
    Point[] Edge4 = new Point[2];
    int count4 = 0;
    String message;
    BaseLoaderCallback myLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV loaded successfully!");
        } else {
            Log.d(TAG, "OpenCV loaded failed");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Check", "Show me1!!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_capture);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        txtView = (TextView) findViewById(R.id.txtView);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV loaded successfully!");
            myLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "OpenCV loaded failed");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, myLoaderCallBack);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(width, height, CvType.CV_8UC4);
        imgGray = new Mat(width, height, CvType.CV_8UC1);
        imgCanny = new Mat(width, height, CvType.CV_8UC1);
        mRgbaT = new Mat(width, height, CvType.CV_8UC4);
        maxContourArea = -1;
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mRgbaT.release();
        imgCanny.release();
        imgGray.release();
    }


    private Double calDistance(Point start, Point Point1, Point Point2) {
        double temp1;
        Point normal = new Point(Point2.y - Point1.y, Point1.x - Point2.x);
        temp1 = (Math.abs(normal.x * (start.x - Point1.x) + normal.y * (start.y - Point1.y))) / (Math.sqrt(normal.x * normal.x + normal.y * normal.y));
        return temp1;
    }

    private Double CalAngle(Point Point11, Point Point12, Point Point21, Point Point22) {
        double temp;
        Point nomal1 = new Point(Point12.x - Point11.x, Point12.y - Point11.y);
        Point nomal2 = new Point(Point22.x - Point21.x, Point22.y - Point21.y);
        temp = Math.acos((nomal1.x * nomal2.x + nomal1.y * nomal2.y) / (Math.sqrt(nomal1.x * nomal1.x + nomal1.y * nomal1.y) * Math.sqrt(nomal2.x * nomal2.x + nomal2.y * nomal2.y)));
        return temp;
    }

    private Boolean belongTo(Point contoutPoint, Point Point1, Point Point2) {
        Point normal = new Point(Point2.y - Point1.y, Point1.x - Point2.x);
        return ((normal.x * (contoutPoint.x - Point1.x) + normal.y * (contoutPoint.y - Point1.y)) == 0);
    }

    private Double calDistanceBW2Point(Point point1, Point point2) {
        return (Math.sqrt((point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y)));
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //System.sleep();
        Log.i("Check", "Show me2!!");
        message = "";
        mRgba = inputFrame.rgba();
        mRgbaT = mRgba.t();
        int contourIdx = -1;
        Core.flip(mRgba.t(), mRgbaT, 1);
        Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());
        maxContourArea = -1;
        Imgproc.cvtColor(mRgba.clone(), imgGray, Imgproc.COLOR_RGB2GRAY, 0);
        Imgproc.GaussianBlur(imgGray, imgGray, new Size(3, 3), 0);
        Imgproc.Canny(imgGray, imgCanny, 100, 200);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imgCanny, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        int x = 0, y = 0, w = 0, h = 0;
        Rect rect = new Rect();
        for (int idx = 0; idx < contours.size(); idx++) {
            MatOfPoint contour = contours.get(idx);
            rect = Imgproc.boundingRect(contour);
            double contourarea = rect.width * rect.height;
            if (contourarea > maxContourArea) {
                contourIdx = idx;
                maxContourArea = contourarea;
                x = rect.x;
                y = rect.y;
                w = rect.width;
                h = rect.height;
            }
        }
        Imgproc.rectangle(mRgba, new Point(x, y), new Point(x + w, y + h), new Scalar(255, 0, 0, 255), 3);
        Mat overlay = new Mat(imgCanny.size(), CvType.CV_8UC3);

        if (Math.abs(w - mRgba.width()) < 5) {
            message = "Dua camera ra xa";
        } else if (Math.abs(x - 1) == 0) {
            if (Math.abs(y - 1) == 0) {
                Log.i("MESSAGE", "Dua camera cheo sang trai len tren 45 do");
                message = "Dua camera cheo sang trai len tren 45 do";
            } else if (Math.abs(y + h - 1 - mRgba.height()) < 5) {
                Log.i("MESSAGE", "Dua camera cheo sang trai xuong duoi 45 do");
                message = "Dua camera cheo sang trai xuong duoi 45 do";
            } else {
                Log.i("MESSAGE", "Dua camera sang trai");
                message = "Dua camera sang trai";
            }
        } else if (Math.abs(x + w - 1 - mRgba.width()) < 5) {
            if (Math.abs(y - 1) == 0) {
                Log.i("MESSAGE", "Dua camera cheo sang phai len tren 45 do");
                message = "Dua camera cheo sang phai len tren 45 do";
            } else if (Math.abs(y + h - 1 - mRgba.height()) < 5) {
                Log.i("MESSAGE", "Dua camera cheo sang phai xuong duoi 45 do");
                message = "Dua camera cheo sang phai xuong duoi 45 do";
            } else {
                Log.i("MESSAGE", "Dua camera sang phai");
                message = "Dua camera sang phai";
            }
        } else if (Math.abs(y - 1) == 0) {
            Log.i("MESSAGE", "Dua camera len tren");
            message = "Dua camera len tren";
        } else if (Math.abs(y + h - 1 - mRgba.height()) < 5) {
            Log.i("MESSAGE", "Dua camera xuong duoi");
            message = "Dua camera xuong duoi";
        } else {
            Log.i("MESSAGE", "Camera da dung vi tri");
            message = "Camera da dung vi tri";


            Log.i(TAG, "" + x + " " + y + " " + w + " " + h);

            //final Mat cropImage = new Mat(overlay, roi);
            if (contourIdx != -1) {
                List<Point> contour = new ArrayList<Point>();
                contour = contours.get(contourIdx).toList();
                Point tl = new Point(x, y);
                Point tr = new Point(x + w, y);
                Point bl = new Point(x, y + h);
                Point br = new Point(x + w, y + h);
                double minBR = MAX_VALUE, minTL = MAX_VALUE, minTR = MAX_VALUE, minBL = MAX_VALUE;
                int point1 = -1, point2 = -1, point3 = -1, point4 = -1;
                double temp = 0;
                int point11 = -1, point22 = -1, point33 = -1, point44 = -1;
                for (int i = 0; i < contour.size(); i++) {
                    if (calDistance(contour.get(i), tl, tr) < 3) {
                        if (contour.get(i).x < x + w / 2) {
                            temp = calDistanceBW2Point(contour.get(i), tl);
                            if (minTL > temp) {
                                minTL = temp;
                                point1 = i;
                            }
                        } else {
                            temp = calDistanceBW2Point(contour.get(i), tr);
                            if (minTL > temp) {
                                minTL = temp;
                                point1 = i;
                            }
                        }
                    }
                    if (calDistance(contour.get(i), tr, br) < 3) {
                        if (contour.get(i).y < y + h / 2) {
                            temp = calDistanceBW2Point(contour.get(i), tr);
                            if (minTR > temp) {
                                minTR = temp;
                                point2 = i;
                            }
                        } else {
                            temp = calDistanceBW2Point(contour.get(i), br);
                            if (minTR > temp) {
                                minTR = temp;
                                point2 = i;
                            }
                        }
                    }
                    if (calDistance(contour.get(i), br, bl) < 3) {
                        if (contour.get(i).x > x + w / 2) {
                            temp = calDistanceBW2Point(contour.get(i), br);
                            if (minBR > temp) {
                                minBR = temp;
                                point3 = i;
                            }
                        } else {
                            temp = calDistanceBW2Point(contour.get(i), bl);
                            if (minBR > temp) {
                                minBR = temp;
                                point3 = i;
                            }
                        }
                    }
                    if (calDistance(contour.get(i), bl, tl) < 3) {
                        if (contour.get(i).y > y + h / 2) {
                            temp = calDistanceBW2Point(contour.get(i), bl);
                            if (minBL > temp) {
                                minBL = temp;
                                point4 = i;
                            }
                        } else {
                            temp = calDistanceBW2Point(contour.get(i), tl);
                            if (minBL > temp) {
                                minBL = temp;
                                point4 = i;
                            }
                        }
                    }
                }
                if (point1 != -1 && point2 != -1 && point3 != -1 && point4 != -1) {
                    if (calDistanceBW2Point(contour.get(point3), contour.get(point4)) < 5 || calDistanceBW2Point(contour.get(point3), contour.get(point2)) < 5
                            || calDistance(contour.get(point3), contour.get(point2), contour.get(point4)) < 5) {

                        double maxSum = -1, maxSub1 = -1, maxSub2 = -1, minSum = MAX_VALUE;
                        for (int i = 0; i < contour.size(); i++) {
                            if (maxSum < contour.get(i).x + contour.get(i).y) {
                                maxSum = contour.get(i).x + contour.get(i).y;
                                point33 = i;
                            }
                            if (minSum > contour.get(i).x + contour.get(i).y) {
                                minSum = contour.get(i).x + contour.get(i).y;
                                point11 = i;
                            }
                            double realx = contour.get(i).x - x, realy = contour.get(i).y - y;
                            if (contour.get(i).x > x + w / 2 && contour.get(i).y < y + h / 2) {
                                if (maxSub1 <= Math.abs(contour.get(i).x - contour.get(i).y)) {
                                    maxSub1 = Math.abs(contour.get(i).x - contour.get(i).y);
                                    point22 = i;
                                }
                            }
                            if (contour.get(i).x < x + w / 2 && contour.get(i).y > y + h / 2) {
                                if (maxSub2 <= Math.abs(realx - realy)) {
                                    maxSub2 = Math.abs(realx - realy);
                                    point44 = i;
                                }
                            }
                        }
                        if (point11 != -1) {
                            Imgproc.circle(mRgba, contour.get(point11), 5, new Scalar(66, 100, 80), -5); // luc sang
                        }
                        if (point22 != -1) {
                            Imgproc.circle(mRgba, contour.get(point22), 5, new Scalar(252, 245, 76), -5); // vang
                        }
                        if (point33 != -1) {
                            Imgproc.circle(mRgba, contour.get(point33), 5, new Scalar(162, 0, 124), -5);// do tia
                        }
                        if (point44 != -1) {
                            Imgproc.circle(mRgba, contour.get(point44), 5, new Scalar(0, 255, 0), -5);
                        }
                        if (point11 != -1 && point22 != -1 && point33 != -1 && point44 != -1) {

                        }
                        if (point11 != -1 && point22 != -1 && point33 != -1 && point44 != -1) {
                            if (calDistanceBW2Point(contour.get(point44), bl) > 15) {
                                message = "Xoay dien thoai cung chieu kim dong ho";
                            } else if (calDistanceBW2Point(contour.get(point33), br) > 15) {
                                message = "Xoay dien thoai nguoc chieu kim dong ho";
                            } else if ((calDistanceBW2Point(contour.get(point11), tl) > 15) || (calDistanceBW2Point(contour.get(point22), tr) > 15)) {
                                message = "Di chuyen camera len tren dong thoi nghieng dt ve truoc";
                            } else {
                                message = "Crop anh";
                                Rect roi = new Rect(x, y, w, h);
                                Log.i(TAG, "" + x + " " + y + " " + w + " " + h);

                                cropImage = new Mat(mRgba, roi);

                                //Log.i("PICTURE_STATE", "" + cropImage.width() + " " + cropImage.height());

                                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                String filename = "barry.png";
                                File file = new File(path, filename);

                                filename = file.toString();
                                Log.i("PATH", filename);
                                Boolean bool2 = Imgcodecs.imwrite(filename, cropImage);
                                captureBitmap();
                                saveToInternalStorage(bitmapConvert);
                                changeActivity();
                            }
                        }
                    } else {
                        double temp1 = 180 * CalAngle(contour.get(point3), contour.get(point4), br, bl) / Math.PI;
                        Log.i("MESSAGER", "" + temp1);
                        if (temp1 > 45) {
                            message = "Xoay dien thoai nguoc chieu kim dong ho";
                        } else message = "Xoay dien thoai cung chieu kim dong ho";
                    }
                }
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtView.setText("");
                txtView.setText(message.toString().trim());
            }
        });
        return mRgba;
    }

    private void captureBitmap() {
        bitmapConvert = Bitmap.createBitmap(javaCameraView.getWidth() / 4, javaCameraView.getHeight() / 4, Bitmap.Config.ARGB_8888);
        try {
            bitmapConvert = Bitmap.createBitmap(cropImage.cols(), cropImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cropImage, bitmapConvert);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        String sdcard= Environment
                .getExternalStorageDirectory()
                .getAbsolutePath()+"/profile.jpg";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(sdcard);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sdcard;
    }
    public void changeActivity()
    {
        Intent intent1= new Intent(ImageCapture.this,ImageViewBitmap.class);
        startActivity(intent1);
    }
}

