package com.example.dat.testsift;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getIDs();
        setEvents();

        photoActions = new PhotoActions(this);
        bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.bm1);
        bm2 = BitmapFactory.decodeResource(getResources(), R.drawable.bm1modified);

        //compare();
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
                //photoActions.dispatchTakePictureIntent();
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
                compare();
            }
        });

    }


    static final int REQUEST_TAKE_PHOTO = 999;
    static final int REQUEST_BROWSE_PHOTO = 1000;


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (photoActions.getmCurrentPhotoPath() != null) {
                        bm1 = getGreyscaleBitmap(imageView1);
                        photoActions.galleryAddPic();  //add image to Android Gallery
                        photoActions.setmCurrentPhotoPath(null);
                        imageView1.setImageBitmap(bm1);
                    }
                } else {
                    if (photoActions.getmCurrentPhotoPath() != null) {
                        File fileToDelte = new File(photoActions.getmCurrentPhotoPath());
                        fileToDelte.delete();
                        photoActions.setmCurrentPhotoPath(null);

                    }
                }
                break;
            case REQUEST_BROWSE_PHOTO:
                if (resultCode == RESULT_OK) {

                    Uri selectedimg = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedimg);
                        if(browse==1){
                            bm1 = photoActions.toGrayscale(bitmap);
                            imageView1.setImageBitmap(bm1);
                        }else if(browse==2) {
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

    public Bitmap getGreyscaleBitmap(ImageView imageView) {

		/* There isn't enough memory to open up more than a couple camera photos */
        /* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoActions.getmCurrentPhotoPath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(photoActions.getmCurrentPhotoPath(), bmOptions);


        Bitmap greyscale = photoActions.toGrayscale(bitmap);
        return greyscale;
    }

    public void compare() {
        Mat mat1 = new Mat();
        Utils.bitmapToMat(bm1, mat1);
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_RGBA2GRAY);
        detector.detect(mat1, keyPoints);

        Mat mat2 = new Mat();
        Utils.bitmapToMat(bm2, mat2);

        Mat descriptors = new Mat();
        Mat dupDescriptors = new Mat();

        MatOfKeyPoint keyPoints2 = new MatOfKeyPoint();
        Imgproc.cvtColor(mat2, mat2, Imgproc.COLOR_RGBA2GRAY);
        detector.detect(mat2, keyPoints2);

        extractor.compute(mat1, keyPoints, descriptors);
        extractor.compute(mat2, keyPoints2, dupDescriptors);

        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptors, dupDescriptors, matches);

        Mat mat3 = new Mat();

        // Log.d("LOG!", "Matches Size " + matches.size());
        // New method of finding best matches
        List<DMatch> matchesList = matches.toList();
        List<DMatch> matches_final = new ArrayList<>();
        for (int i = 0; i < matchesList.size(); i++) {
            if (matchesList.get(i).distance <= 10) {
                matches_final.add(matches.toList().get(i));
            }
        }

        MatOfDMatch matches_final_mat = new MatOfDMatch();
        matches_final_mat.fromList(matches_final);

        Scalar RED = new Scalar(255, 0, 0);
        Scalar GREEN = new Scalar(0, 255, 0);

        MatOfByte matOfByte = new MatOfByte();
        Features2d.drawMatches(mat1, keyPoints, mat2, keyPoints2, matches_final_mat, mat3, RED, GREEN, matOfByte, Features2d.DRAW_RICH_KEYPOINTS);
        Bitmap outputBm = Bitmap.createBitmap(mat3.cols(), mat3.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat3, outputBm);
        imageViewResult.setImageBitmap(outputBm);

        Log.d("LOG!", "Matches Size " + matches_final_mat.size());
    }

}