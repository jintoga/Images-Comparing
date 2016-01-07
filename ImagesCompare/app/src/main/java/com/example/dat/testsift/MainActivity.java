package com.example.dat.testsift;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.dat.testsift.PhotoAlbum.PhotoActions;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("opencv_java");
        /*System.loadLibrary("nonfree");
        System.loadLibrary("nonfree_jni");*/
    }

    private ImageView imageViewResult, imageView1, imageView2;
    private Bitmap bm1, bm2; // make bitmap from image resource
    private FeatureDetector detector = FeatureDetector.create(FeatureDetector.FAST);
    private DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
    private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    private PhotoActions photoActions;
    private Button buttonCompare;

    Scalar RED = new Scalar(255, 0, 0);
    Scalar GREEN = new Scalar(0, 255, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getIDs();
        setEvents();

        photoActions = new PhotoActions(this);
       /* bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.bm1);
        bm2 = BitmapFactory.decodeResource(getResources(), R.drawable.bm1modified);*/

    }

    private void getIDs() {
        imageViewResult = (ImageView) this.findViewById(R.id.imageViewResult);
        imageView1 = (ImageView) this.findViewById(R.id.imageView1);
        imageView2 = (ImageView) this.findViewById(R.id.imageView2);
        buttonCompare = (Button) findViewById(R.id.buttonCompare);
    }

    int browse = 0;

    private void setEvents() {
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoActions.processBrowsePicture();
                browse = 1;
            }
        });
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoActions.processBrowsePicture();
                browse = 2;
            }
        });

        buttonCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //compare();
                if (bm1 != null && bm2 != null) {
                    MyAsyncTask myAsyncTask = new MyAsyncTask(MainActivity.this, bm1, bm2);
                    myAsyncTask.execute();
                }
            }
        });

    }


    static final int REQUEST_BROWSE_PHOTO = 1000;


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode) {
            case REQUEST_BROWSE_PHOTO:
                if (resultCode == RESULT_OK) {

                    Uri selectedimg = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedimg);
                        if (browse == 1) {
                            bm1 = photoActions.toGrayscale(bitmap);
                            imageView1.setImageBitmap(bm1);
                        } else if (browse == 2) {
                            bm2 = photoActions.toGrayscale(bitmap);
                            imageView2.setImageBitmap(bm2);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
                break;
            default:
                break;
        }

    }


    public void setImages(Bitmap res1, Bitmap res2, Bitmap res) {
        imageView1.setImageBitmap(res1);
        imageView2.setImageBitmap(res2);
        imageViewResult.setImageBitmap(res);
    }
}