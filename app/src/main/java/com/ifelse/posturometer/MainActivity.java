package com.ifelse.posturometer;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements SettingsTab.SettingsTabCommunicator, MainTab.MainTabCommunicator, SensorEventListener {

    /*
    ATTRIBUTES
     */

    // Tabs
    private ViewPagerAdapter mViewPagerAdapter;
    private CharSequence mTabTitles[] = {"INICIO", "AJUSTES"};
    private int mTotalTabs = 2;

    // Accelerometer
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private double mSensorSamplingPeriod = 0.5;
    private float[] mCurrentAcceleration;
    private float[] mPreviousAcceleration;

    // Vibration
    private Vibrator mVibrator;
    private long[] mFrontVibrationPattern;
    private long[] mLateralVibrationPattern;
    private boolean mIsVibratingFront = false;
    private boolean mIsVibratingLateral = false;

    // Angles
    private int mFrontAngleThreshold;
    private int mLateralAngleThreshold;
    private int mInitialFrontAngle = 0;
    private int mInitialLateralAngle = 0;
    private int mCurrentFrontAngle = 0;
    private int mCurrentLateralAngle = 0;

    // Delay
    private DelayThread mDelayThread;
    private int mDelay;

    /*
    ANGLE AND VIBRATION METHODS
     */
    @Override
    public void buttonStart() {
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mDelayThread = new DelayThread();
        mDelayThread.setPaused(true);
        mDelayThread.start();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, (int) (mSensorSamplingPeriod * 100000));

        mCurrentAcceleration = new float[3];
        mPreviousAcceleration = new float[3];
    }

    @Override
    public void buttonStop() {
        if(mSensorManager != null) mSensorManager.unregisterListener(this);
        if(mVibrator != null) mVibrator.cancel();
        updateFrontAngleTextView(0);
        updateRightAngleTextView(0);
        updateLeftAngleTextView(0);
        //if(mButtonStop != null) mButtonStop.setEnabled(false);
        //if(mButtonStart != null) mButtonStart.setEnabled(true);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //if sensor is unreliable, return void
        if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        // Get acceleration vector
        mCurrentAcceleration[0] = sensorEvent.values[0];
        mCurrentAcceleration[1] = sensorEvent.values[1];
        mCurrentAcceleration[2] = sensorEvent.values[2];

        // Calculate norm
        double norm = Math.sqrt((double) (mCurrentAcceleration[0] * mCurrentAcceleration[0]
                + mCurrentAcceleration[1] * mCurrentAcceleration[1]
                + mCurrentAcceleration[2] * mCurrentAcceleration[2]));

        // Normalize acceleration vector
        mCurrentAcceleration[0] = (float) (mCurrentAcceleration[0] / norm);
        mCurrentAcceleration[1] = (float) (mCurrentAcceleration[1] / norm);
        mCurrentAcceleration[2] = (float) (mCurrentAcceleration[2] / norm);

        checkAngleThresholds();
        updateAngleTextViews();

        mPreviousAcceleration = mCurrentAcceleration;
    }

    private void checkAngleThresholds() {
        // Get angles
        mCurrentFrontAngle = (int) (-mCurrentAcceleration[2] * 90) - mInitialFrontAngle;
        mCurrentLateralAngle = (int) (-mCurrentAcceleration[0] * 90) - mInitialLateralAngle;

        if(mCurrentFrontAngle > mFrontAngleThreshold && mCurrentFrontAngle < 90 && !mIsVibratingFront) {
            //mVibrator.vibrate(mFrontVibrationPattern, 0);
            mIsVibratingFront = true;
        }

        if(mCurrentFrontAngle < mFrontAngleThreshold && mIsVibratingFront) {
            //mVibrator.cancel();
            mIsVibratingFront = false;
        }

        /*
        // Check Lateral Angle
        if(Math.abs(mCurrentLateralAngle) > mLateralAngleThreshold &&Math.abs(mCurrentLateralAngle) < 90
                && !mIsVibratingFront && !mIsVibratingLateral) {
            mVibrator.vibrate(mLateralVibrationPattern, 0);
            mIsVibratingLateral = true;
        }

        if(Math.abs(mCurrentLateralAngle) < mLateralAngleThreshold && !mIsVibratingFront && mIsVibratingLateral) {
            mVibrator.cancel();
            mIsVibratingLateral = false;
        }
        */
    }

    private void updateAngleTextViews() {
        // Front-Left Inclination
        if(mCurrentFrontAngle > 0 && mCurrentLateralAngle > 0) {
            updateFrontAngleTextView(mCurrentFrontAngle);
            updateRightAngleTextView(0);
            updateLeftAngleTextView(mCurrentLateralAngle);
            return;
        }

        // Front-Right Inclination
        if(mCurrentFrontAngle > 0 && mCurrentLateralAngle < 0) {
            updateFrontAngleTextView(mCurrentFrontAngle);
            updateRightAngleTextView(-mCurrentLateralAngle);
            updateLeftAngleTextView(0);
            return;
        }

        // Back-Left Inclination
        if(mCurrentFrontAngle < 0 && mCurrentLateralAngle > 0) {
            updateFrontAngleTextView(0);
            updateRightAngleTextView(0);
            updateLeftAngleTextView(mCurrentLateralAngle);
            return;
        }

        // Back-Right Inclination
        if(mCurrentFrontAngle < 0 && mCurrentLateralAngle < 0) {
            updateFrontAngleTextView(0);
            updateRightAngleTextView(-mCurrentLateralAngle);
            updateLeftAngleTextView(0);
            return;
        }
    }

    private void updateFrontAngleTextView(int frontAngle) {
        TextView textView = (TextView) ((MainTab) (mViewPagerAdapter.getItem(0))).getView(MainTabView.TEXTVIEW_FRONT_ANGLE);

        if (frontAngle == 0) {
            textView.setText("0°");
        }

        if (frontAngle > mFrontAngleThreshold) {
            textView.setTextColor(Color.RED);
            textView.setText(String.valueOf(frontAngle) + "°");
        }

        if (frontAngle < mFrontAngleThreshold) {
            textView.setTextColor(Color.BLACK);
            textView.setText(String.valueOf(frontAngle) + "°");
        }
    }

    private void updateRightAngleTextView(int rightAngle) {
        TextView textView = (TextView) ((MainTab) (mViewPagerAdapter.getItem(0))).getView(MainTabView.TEXTVIEW_RIGHT_ANGLE);

        if (rightAngle == 0) {
            textView.setText("0°");
        }

        if (rightAngle > mLateralAngleThreshold) {
            textView.setTextColor(Color.RED);
            textView.setText(String.valueOf(rightAngle) + "°");
        }

        if (rightAngle < mLateralAngleThreshold) {
            textView.setTextColor(Color.BLACK);
            textView.setText(String.valueOf(rightAngle) + "°");
        }
    }

    private void updateLeftAngleTextView(int leftAngle) {
        TextView textView = (TextView)((MainTab) (mViewPagerAdapter.getItem(0))).getView(MainTabView.TEXTVIEW_LEFT_ANGLE);

        if (leftAngle == 0) {
            textView.setText("0°");
        }

        if (leftAngle > mLateralAngleThreshold) {
            textView.setTextColor(Color.RED);
            textView.setText(String.valueOf(leftAngle) + "°");
        }

        if (leftAngle < mLateralAngleThreshold) {
            textView.setTextColor(Color.BLACK);
            textView.setText(String.valueOf(leftAngle) + "°");
        }
    }

    /*
    UI METHODS
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        setupSlidingUI();
    }

    private void setupSlidingUI() {
        setupActionBar();
        setupSlidingTabs();
        inflate();
    }

    private void setupActionBar() {
        // Inflate Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);


        // Get the ActionBar to configure the way it behaves
        final ActionBar actionBar = getSupportActionBar();
        // Enable/Disable overriding the default toolbar layout
        actionBar.setDisplayShowCustomEnabled(true);
        // Enable/Disable Title
        //actionBar.setDisplayShowTitleEnabled(true);
        // Enable/Disable Home Button
        //actionBar.setDisplayShowHomeEnabled(false);
        // Enable/Disable Home Button as Up Button
        //actionBar.setDisplayHomeAsUpEnabled(false);
        // ?
        //actionBar.setDisplayUseLogoEnabled(true);

    }

    private void setupSlidingTabs() {
        // Inflate ViewPagerAdapter
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mTabTitles, mTotalTabs);

        // Inflate ViewPager and attach Adapter
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(mViewPagerAdapter);

        // Inflate Tabs
        SlidingTabLayout slidingTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        slidingTabs.setDistributeEvenly(true);
        // Set Indicator Color
        slidingTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getSelectedTextColor(int position) {
                return ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLighter70);
            }

            @Override
            public int getUnselectedTextColor(int position) {
                return ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLighter30);
            }
        });

        // Attach the ViewPager to the SlidingTabsLayout
        slidingTabs.setViewPager(viewPager);
    }

    private void inflate() {
        mFrontVibrationPattern = new long[3];
        mLateralVibrationPattern = new long[3];
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            return true;
        }

        if (id == R.id.action_help) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void seekBarProgressChanged(SeekBarType seekBarType, int progress) {
        switch(seekBarType) {
            case SEEKBAR_FRONT_ANGLE:
                onFrontAngleChanged(progress);
                break;

            case SEEKBAR_FRONT_VIBRATION:
                onFrontVibrationChanged(progress);
                break;

            case SEEKBAR_LATERAL_ANGLE:
                onLateralAngleChanged(progress);
                break;

            case SEEKBAR_LATERAL_VIBRATION:
                onLateralVibrationChanged(progress);
                break;

            case SEEKBAR_DELAY:
                onDelayChanged(progress);
                break;

            default:
                break;
        }
    }

    private void onFrontAngleChanged(int seekBarProgress) {
        mFrontAngleThreshold = seekBarProgress;
    }

    private void onFrontVibrationChanged(int seekBarProgress) {
        if(mFrontVibrationPattern == null) return;

        if(seekBarProgress == 0) {
            mFrontVibrationPattern[0] = 0;
            mFrontVibrationPattern[1] = 0;
            mFrontVibrationPattern[2] = 0;
        } else {
            mFrontVibrationPattern[0] = 0;
            mFrontVibrationPattern[1] = (long)( ((1 / (double)seekBarProgress)) * 1000 );
            mFrontVibrationPattern[2] = (long)( ((1 / (double)seekBarProgress)) * 1000 );
        }
    }

    private void onLateralAngleChanged(int seekBarProgress) {
        mLateralAngleThreshold = seekBarProgress;
    }

    private void onLateralVibrationChanged(int seekBarProgress) {
        if(mLateralVibrationPattern == null) return;

        if(seekBarProgress == 0) {
            mLateralVibrationPattern[0] = 0;
            mLateralVibrationPattern[1] = 0;
            mLateralVibrationPattern[2] = 0;
        } else {
            mLateralVibrationPattern[0] = 0;
            mLateralVibrationPattern[1] = (long)( ((1 / (double)seekBarProgress)) * 1000 );
            mLateralVibrationPattern[2] = (long)( ((1 / (double)seekBarProgress)) * 1000 );
        }
    }

    private void onDelayChanged(int seekBarProgress) {
        mDelay = seekBarProgress;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}