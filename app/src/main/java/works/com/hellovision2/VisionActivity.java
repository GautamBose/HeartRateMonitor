package works.com.hellovision2;



import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.ViewDebug;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.util.concurrent.TimeUnit;




import java.util.Random;

import android.app.Activity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;




import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collections;

public class VisionActivity extends ActionBarActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private final String TAG = "APP";
    private Tutorial3View mOpenCvCameraView;
    private SubMenu mColorEffectsMenu;
    long prevTs;
    TextView rgbVal;
    float lastTouchY = 0;
    int cannyThreshold = 50;
    long startTime = 0;

    ArrayList<Float> greenValues;
    int bufferCounter = 0;
    int bufferSize = 100;
    float sum = 0;
    float halfWaySum = 0;
    Float ave = new Float(0);
    ArrayList<Double> bufferValues = new ArrayList<>();
    private LineGraphSeries<DataPoint> m2series;
    private int lastX = 0;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "called onCreate");
        super.onCreate(savedInstanceState);



        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_vision);
        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        rgbVal = (TextView) findViewById(R.id.rgbVal);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> m2series = new LineGraphSeries<>();
        graph.addSeries(m2series);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(10);
        viewport.setScrollable(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mColorEffectsMenu = menu.addSubMenu("Flash On");
        mColorEffectsMenu = menu.addSubMenu("Flash Off");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item.toString() == "Flash On") {
            mOpenCvCameraView.setFlashOn();
        } else if (item.toString() == "Flash Off") {
            mOpenCvCameraView.setFlashOff();
        }
        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    public float calculateMean(ArrayList<Float> values) {
        float mean = (float) 0.0;
        for (int i = 0; i < values.size(); i++) {
            Log.d(TAG, values.get(i) + "");
            mean += values.get(i);

        }
        mean /= values.size();
        return mean;
    }

    public Double sumArrayList(ArrayList<Double> array) {

        Double sum = new Double(0);
        for (Double wef : array) {
            sum = sum + wef;
        }
        return sum;
    }

    public ArrayList<Double> deMeanList(ArrayList<Double> array) {
        Double sumArrayList = sumArrayList(array);
        Double arrayMean = sumArrayList / array.size();
        ArrayList deMeanedArray = new ArrayList();

        for (int i = 0; i < array.size(); i++) {
            Double currentValue = array.get(i);
            Double deMeanedValue = currentValue - arrayMean;

            deMeanedArray.add(deMeanedValue);

        }

        return deMeanedArray;
    }

    public Double findMedian(ArrayList<Double> a) {

        Collections.sort(a);
        if (a.size() == 0) {
            return 0.0;
        } else if (a.size() % 2 == 1) {
            return a.get(a.size() / 2);
        } else {
            return (a.get(a.size() / 2) + a.get(a.size() / 2 - 1)) / 2.0;
        }
    }

    public ArrayList<Double> lowPassFilter(ArrayList<Double> deArray) {
        ArrayList<Double> filteredArray = new ArrayList<>();

        for (int index = 0; index < deArray.size(); index++) {
            if (index > 5) {
                ArrayList<Double> subListed = new ArrayList<Double>(deArray.subList(index - 5, index));
                Double medianValue = findMedian(subListed);
                filteredArray.add(medianValue);

            }

        }
        return filteredArray;
    }

    public int getBeats(ArrayList<Double> bufferValues) {
        int beats = 0;
        ArrayList<Double> bufferDeMeaned = deMeanList(bufferValues);
        ArrayList<Double> filteredArray = lowPassFilter(bufferDeMeaned);

        for (int filterIndex = 0; (filterIndex < filteredArray.size() - 1); filterIndex++) {
            Double currentVal = filteredArray.get(filterIndex);
            if (filterIndex > 2) {
                Double prevVal = filteredArray.get(filterIndex - 1);
                Double nextVal = filteredArray.get(filterIndex + 1);

                if (prevVal < 0 && nextVal > 0) {
                    beats++;

                }
            }
        }
        return beats;

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat currentFrame = inputFrame.rgba();


        //CANNY Edge Detection
        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGB2HSV);

        Scalar HSVMeans = Core.mean(currentFrame);

        Double valueMean = HSVMeans.val[2];


        bufferValues.add(valueMean);
//        m2series.appendData(new DataPoint(lastX++, 10), true, 100);

//        String valueMeanString = Double.toString(valueMean);
        if (bufferValues.size() % 50 == 0) {
            int currentBeats = getBeats(bufferValues);

            if(startTime != 0) {
                long timeDifference = System.nanoTime() - startTime;

                double seconds = (double) timeDifference / 1000000000.0;
                double minutes = (double) seconds / 60.0;

                double bpm = currentBeats / minutes;

//                String timeElapsed = Double.toString(seconds);

//                String beatsString = Integer.toString(currentBeats);
//            String bufferValString = android.text.TextUtils.join(", ", bufferValues);
//            String bufferValSize = Integer.toString(bufferValues.size());
//            Log.d("wewef", bufferValSize);
//            Log.d("wewef", bufferValString);
//                Log.d("wewef", beatsString);
//                Log.d("wewef", timeElapsed);
                TextView wefView = (TextView) findViewById(R.id.bpm);
                String bpmString = Double.toString(bpm);
                setText(wefView, bpmString);

            }

//            for(int remover = 0; remover < 5; remover++){
//                bufferValues.remove(remover);
//            }
            startTime = System.nanoTime();




        }


        return currentFrame;
    }
    private void setText(final TextView text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        float y = e.getY();
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            if (lastTouchY > y)
                cannyThreshold += 5;
            else
                cannyThreshold -= 5;
            lastTouchY = y;
        }

        if (e.getAction() == MotionEvent.ACTION_UP)
            lastTouchY = 0;
        return true;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Vision Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}