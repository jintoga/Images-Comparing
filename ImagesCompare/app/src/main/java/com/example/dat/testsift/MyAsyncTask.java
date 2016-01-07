package com.example.dat.testsift;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DAT on 07-Jan-16.
 */
public class MyAsyncTask extends AsyncTask<Void, Void, ArrayList<Bitmap>> {

    private FeatureDetector detector = FeatureDetector.create(FeatureDetector.FAST);
    private DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
    private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

    private Scalar RED = new Scalar(255, 0, 0);
    private Scalar GREEN = new Scalar(0, 255, 0);

    private MainActivity activity;
    private Bitmap bm1, bm2;

    private int matchingPercentage = 50;
    private boolean isMatched = false;
    private static ProgressDialog pd;

    public MyAsyncTask(MainActivity activity, Bitmap bm1, Bitmap bm2) {
        this.activity = activity;
        this.bm1 = bm1;
        this.bm2 = bm2;
    }

    @Override
    protected void onPreExecute() {
        pd = new ProgressDialog(activity);
        pd.setIndeterminate(true);
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Processing...");
        pd.show();
    }

    @Override
    protected ArrayList<Bitmap> doInBackground(Void... voids) {
        return compare();
    }

    @Override
    protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
        pd.dismiss();
        if (bitmaps != null)
            activity.setImages(bitmaps.get(0), bitmaps.get(1), bitmaps.get(2));
        if (isMatched)
            Toast.makeText(activity, "Matched", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(activity, "Not matched", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        Toast.makeText(activity, "No keypoints detected", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<Bitmap> compare() {

        try {
            ArrayList<Bitmap> bitmaps = new ArrayList<>();
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

            Mat matX = new Mat();
            Features2d.drawKeypoints(mat1, keyPoints, matX, GREEN, Features2d.NOT_DRAW_SINGLE_POINTS);
            Bitmap outputBm1 = Bitmap.createBitmap(matX.cols(), matX.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(matX, outputBm1);
            bitmaps.add(outputBm1);

            Mat matX2 = new Mat();
            Features2d.drawKeypoints(mat2, keyPoints2, matX2, GREEN, Features2d.NOT_DRAW_SINGLE_POINTS);
            Bitmap outputBm2 = Bitmap.createBitmap(matX2.cols(), matX2.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(matX2, outputBm2);
            bitmaps.add(outputBm2);

            Log.d("LOG!", "descriptors 1: " + descriptors.size());
            Log.d("LOG!", "descriptors 2: " + dupDescriptors.size());

            if (descriptors.size().height == 0 || dupDescriptors.size().height == 0
                    || descriptors.size().width == 0 || dupDescriptors.size().width == 0) {
                publishProgress();
                return null;
            }
            Log.d("size:", "width: " + descriptors.size().width + " height " + descriptors.size().height);
            double decider = descriptors.size().height < dupDescriptors.size().height ? descriptors.size().height : dupDescriptors.size().height;
            Log.d("decider:", decider + "");

            double deciderPercentage = decider * matchingPercentage / 100f;
            Log.d("deciderPercentage:", deciderPercentage + "");

            MatOfDMatch matches = new MatOfDMatch();
            matcher.match(descriptors, dupDescriptors, matches);

            Mat mat3 = new Mat();

            List<DMatch> matchesList = matches.toList();
            List<DMatch> matches_final = new ArrayList<>();
            for (int i = 0; i < matchesList.size(); i++) {
                if (matchesList.get(i).distance <= 15) {
                    matches_final.add(matches.toList().get(i));
                }
            }

            MatOfDMatch matches_final_mat = new MatOfDMatch();
            matches_final_mat.fromList(matches_final);

            MatOfByte matOfByte = new MatOfByte();
            Features2d.drawMatches(mat1, keyPoints, mat2, keyPoints2, matches_final_mat, mat3, RED, GREEN, matOfByte, Features2d.DRAW_RICH_KEYPOINTS);
            Bitmap outputBm = Bitmap.createBitmap(mat3.cols(), mat3.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat3, outputBm);
            bitmaps.add(outputBm);

            Log.d("LOG!", "Matches Size " + matches_final_mat.size());
            isMatched = matches_final_mat.size().height >= deciderPercentage ? true : false;
            return bitmaps;
        } catch (CvException e) {
            Log.e("CvException", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return null;
    }
}
