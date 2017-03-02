package works.com.hellovision2;

import java.util.Vector;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;



import java.util.ArrayList;

public class VisionActivity extends ActionBarActivity implements CameraBridgeViewBase.CvCameraViewListener2
{
    private final String TAG = "APP";
    private Tutorial3View mOpenCvCameraView;
    private SubMenu mColorEffectsMenu;
    long prevTs;
    TextView rgbVal;
    float lastTouchY=0;
    int cannyThreshold=50;

    ArrayList<Float> greenValues;
    int bufferCounter = 0;
    int bufferSize = 100;
    float sum=0;
    float halfWaySum = 0;
    Float ave = new Float(0);
    ArrayList<Double> bufferValues;

    ArrayList<Float> testArray;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

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

    public float calculateMean(ArrayList<Float> values)
    {
        float mean= (float) 0.0;
       for (int i=0;i<values.size();i++)
       {
           Log.d(TAG, values.get(i) + "");
           mean += values.get(i);

       }
        mean /= values.size();
        return mean;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat currentFrame = inputFrame.rgba();




        //CANNY Edge Detection
        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGB2HSV);

        Scalar HSVMeans = Core.mean(currentFrame);

        double valueMean = HSVMeans.val[2];

//        bufferValues.add(valueMean);

        String valueMeanString = Double.toString(valueMean);

        Log.d("means", valueMeanString);

        return currentFrame;
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
}