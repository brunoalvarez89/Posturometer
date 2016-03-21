package com.ifelse.posturometer;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.SeekBar;
import android.widget.TextView;

import com.ifelse.posturometer.ui.MainTab;
import com.ifelse.posturometer.ui.MainTabView;
import com.ifelse.posturometer.ui.SeekBarType;
import com.ifelse.posturometer.ui.SettingsTab;
import com.ifelse.posturometer.ui.SettingsTabView;
import com.ifelse.posturometer.slidingtabs.SlidingTabLayout;
import com.ifelse.posturometer.ui.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity
        implements SettingsTab.SettingsTabInterface
                    , MainTab.MainTabInterface
                    , SensorEventListener {

    //region Attributes

    // Tabs
    private ViewPagerAdapter mViewPagerAdapter;
    private CharSequence mTabTitles[] = {"INICIO", "AJUSTES"};
    private int mTotalTabs = 2;

    // Accelerometer
    private SensorManager mSensorManager;
    private double mSensorSamplingPeriod = 50; // in ms
    private float[] mAccelerationLP;
    private float[] mAcceleration2LP;
    private float[] mAcceleration3LP;
    private boolean mAcquiringOk;

    // Vibration
    private Vibrator mVibrator;
    private long[] mFrontVibrationPattern;
    private long[] mLateralVibrationPattern;
    private boolean mIsVibratingFrontAngleThreshold;
    private boolean mIsVibratingLateralAngleThreshold;

    // Angle Thresholds & Values
    private int mInitialLateralAngle;
    private int mInitialFrontAngle;
    private int mFrontAngleThreshold;
    private int mLateralAngleThreshold;
    private int mCurrentFrontAngle;
    private int mCurrentLateralAngle;
    private boolean mIsOverFrontAngleThreshold;
    private boolean mIsOverLateralAngleThreshold;
    private AngleType mLastAngleOverThreshold;

    // Initial Angles
    private int mInitialAngleAcquisitionDelay = 3000; // in ms
    private int mInitialAngleAcquisitionDelaySampleCount;
    private boolean mInitialAnglesOk;

    // Smartphone Positioning
    private int mPositioningDelay = 5000; // in ms
    private int mPositioningDelaySampleCount;
    private boolean mPositioningOk;
    private int mPositioningVibrationDelay = 1000;
    private int mPositioningVibrationDelaySampleCount;
    private boolean mPositioningVibrationOk = true;

    // User Response Delay
    private int mUserResponseDelay; // in ms
    private int mUserResponseDelaySampleCount;
    private boolean mUserDidNotRespond;

    // Preferences
    private boolean mPreferencesLoaded;

    //endregion

    //region Angle and Vibration Methods

    @Override
    public void buttonStart(boolean start) {
        if (start) {
            // Start Acquisition
            mAcquiringOk = true;

            // Initialize Vibrator
            mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            // Initialize Accelerometer
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, accelerometer, (int) (mSensorSamplingPeriod * 1000));

            // Initialize Sensor Data
            mAccelerationLP = new float[3];
            mAcceleration2LP = new float[3];
            mAcceleration3LP = new float[3];
        }
        else {
            // Put Flags & Counters to zero
            mAcquiringOk = false;
            mPositioningOk = false;
            mInitialAnglesOk = false;
            mUserDidNotRespond = false;
            mPositioningVibrationOk = false;
            mPositioningDelaySampleCount = 0;
            mInitialAngleAcquisitionDelaySampleCount = 0;
            mUserResponseDelaySampleCount = 0;
            mPositioningDelaySampleCount = 0;
            mPositioningVibrationDelaySampleCount = 0;
            mLastAngleOverThreshold = AngleType.NONE;

            // Dismiss Vibrator and Aceelerometer
            mVibrator.cancel();
            mSensorManager.unregisterListener(this);

            // Clear Angle TextViews
            updateLeftAngleTextView(0);
            updateFrontAngleTextView(0);
            updateRightAngleTextView(0);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Check Sampling Period
        //long currentTime = SystemClock.elapsedRealtime();
        //long deltaTime = currentTime - mOldTime;
        //Log.d("time", deltaTime + ", ");
        //mOldTime = currentTime;

        acquireAndFilter(sensorEvent.values.clone());

        if(mPositioningOk) {
            if (mAcquiringOk) {
                if (mInitialAnglesOk) {

                    checkAngleThresholds();

                    if (mIsOverFrontAngleThreshold || mIsOverLateralAngleThreshold) {
                        if (mUserDidNotRespond) {
                            vibrateUserResponse(true);
                        }
                        else {
                            delayUserResponse();
                        }
                    }
                    else {
                        vibrateUserResponse(false);
                        mUserDidNotRespond = false;
                    }

                    updateAngleTextViews();

                }
                else {
                    calculateInitialAngles();
                }
            }
        }
        else {
            delayPositioning();
        }
    }

    private void acquireAndFilter(float[] values) {
        mAccelerationLP = lowPass(values, mAccelerationLP);
        mAcceleration2LP = lowPass(mAccelerationLP.clone(), mAcceleration2LP);
        mAcceleration3LP = lowPass(mAcceleration2LP.clone(), mAcceleration3LP);

        // calculate norm
        double norm = Math.sqrt((double) (mAcceleration3LP[0] * mAcceleration3LP[0]
                + mAcceleration3LP[1] * mAcceleration3LP[1]
                + mAcceleration3LP[2] * mAcceleration3LP[2]));

        // Normalize acceleration vector
        mAcceleration3LP[0] = (float) (mAcceleration3LP[0] / norm);
        mAcceleration3LP[1] = (float) (mAcceleration3LP[1] / norm);
        mAcceleration3LP[2] = (float) (mAcceleration3LP[2] / norm);
    }

    private void checkAngleThresholds() {
        // Get angles
        mCurrentFrontAngle = (int) (-mAcceleration3LP[2] * 90) - mInitialFrontAngle;
        mCurrentLateralAngle = (int) (-mAcceleration3LP[0] * 90) - mInitialLateralAngle;

        // Check Front Angle
        if (mCurrentFrontAngle > mFrontAngleThreshold && mCurrentFrontAngle < 90 && !mIsOverFrontAngleThreshold) {
            mIsOverFrontAngleThreshold = true;
            mLastAngleOverThreshold = AngleType.FRONT_ANGLE;
        }// Check Lateral Angle

        if (Math.abs(mCurrentLateralAngle) > mLateralAngleThreshold && Math.abs(mCurrentLateralAngle) < 90
                && !mIsOverLateralAngleThreshold) {
            mIsOverLateralAngleThreshold = true;
            mLastAngleOverThreshold = AngleType.LATERAL_ANGLE;
        }

        if (mCurrentFrontAngle < mFrontAngleThreshold) {
            mIsOverFrontAngleThreshold = false;

            if (!mIsOverLateralAngleThreshold) {
                mLastAngleOverThreshold = AngleType.NONE;
            } else {
                mLastAngleOverThreshold = AngleType.LATERAL_ANGLE;
            }
        }

        if (Math.abs(mCurrentLateralAngle) < mLateralAngleThreshold) {
            mIsOverLateralAngleThreshold = false;

            if (!mIsOverFrontAngleThreshold) {
                mLastAngleOverThreshold = AngleType.NONE;
            } else {
                mLastAngleOverThreshold = AngleType.FRONT_ANGLE;
            }
        }
    }

    private void vibrateUserResponse(boolean vibrate) {
        if(vibrate) {
            if (mLastAngleOverThreshold == AngleType.FRONT_ANGLE && !mIsVibratingFrontAngleThreshold) {
                mVibrator.vibrate(mFrontVibrationPattern, 0);
                mIsVibratingFrontAngleThreshold = true;
                mIsVibratingLateralAngleThreshold = false;
            }

            if (mLastAngleOverThreshold == AngleType.LATERAL_ANGLE && !mIsVibratingLateralAngleThreshold) {
                mVibrator.vibrate(mLateralVibrationPattern, 0);
                mIsVibratingLateralAngleThreshold = true;
                mIsVibratingFrontAngleThreshold = false;
            }
        }
        else {
            mVibrator.cancel();
            mIsVibratingFrontAngleThreshold = false;
            mIsVibratingLateralAngleThreshold = false;
        }

    }

    private void delayUserResponse() {
        int totalSamples = (int) (mUserResponseDelay / mSensorSamplingPeriod);

        if(mUserResponseDelaySampleCount == totalSamples && !mUserDidNotRespond) {
            mUserDidNotRespond = true;
            mUserResponseDelaySampleCount = 0;
        }

        mUserResponseDelaySampleCount++;
    }

    private void calculateInitialAngles() {
        int totalSamples = (int) (mInitialAngleAcquisitionDelay / mSensorSamplingPeriod);

        if(mInitialAngleAcquisitionDelaySampleCount < totalSamples) {
            mCurrentFrontAngle = (int) (-mAcceleration3LP[2] * 90);
            mCurrentLateralAngle = (int) (-mAcceleration3LP[0] * 90);

            mInitialFrontAngle += mCurrentFrontAngle;
            mInitialLateralAngle += mCurrentLateralAngle;

            mInitialAngleAcquisitionDelaySampleCount++;
        } else {
            mInitialFrontAngle /= totalSamples;
            mInitialLateralAngle /= totalSamples;
            mInitialAnglesOk = true;
        }
    }

    private void delayPositioning() {

        int totalPositioningSamples = (int) (mPositioningDelay / mSensorSamplingPeriod);
        if(mPositioningDelaySampleCount == totalPositioningSamples && !mPositioningOk) {
            mPositioningOk = true;
        }
        mPositioningDelaySampleCount++;

        int totalVibrationSamples = (int) (mPositioningVibrationDelay / mSensorSamplingPeriod);
        if(mPositioningVibrationDelaySampleCount == totalVibrationSamples && !mPositioningOk) {
            mPositioningDelaySampleCount = 0;
            mVibrator.vibrate(300);
        }
        mPositioningVibrationDelaySampleCount++;
    }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;

        int fcut = 10;
        float dFactor = 1/(float)(2*Math.PI*mSensorSamplingPeriod*fcut);
        float alpha = 1/(1+dFactor);

        alpha = 0.2f;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + alpha*(input[i]-output[i]);
        }

        return output;
    }

    //endregion

    //region UI Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        setupSlidingUI();
        loadPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setupSlidingUI() {
        setupActionBar();
        setupSlidingTabs();
        inflate();
    }

    private void loadPreferences() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);

        // Front Angle Threshold
        mFrontAngleThreshold = settings.getInt("FRONT_ANGLE_THRESHOLD", -1);
        if (mFrontAngleThreshold == -1) mFrontAngleThreshold = 20;

        // Front Vibration Pattern
        mFrontVibrationPattern[1] = settings.getLong("FRONT_VIBRATION_PATTERN", -1);
        if (mFrontVibrationPattern[1] == -1) {
            mFrontVibrationPattern[0] = 0;
            mFrontVibrationPattern[1] = (long)( ((1 / (double)1)) * 1000 );
            mFrontVibrationPattern[2] = (long)( ((1 / (double)1)) * 1000 );
        }
        else {
            mFrontVibrationPattern[0] = 0;
            mFrontVibrationPattern[2] = mFrontVibrationPattern[1];
        }

        // Lateral Angle Threshold
        mLateralAngleThreshold = settings.getInt("LATERAL_ANGLE_THRESOLD", -1);
        if (mLateralAngleThreshold == -1) mLateralAngleThreshold = 20;

        // Lateral Vibration Pattern
        mLateralVibrationPattern[1] = settings.getLong("LATERAL_VIBRATION_PATTERN", -1);
        if (mLateralVibrationPattern[1] == -1) {
            mLateralVibrationPattern[0] = 0;
            mLateralVibrationPattern[1] = (long)( ((1 / (double)6)) * 1000 );
            mLateralVibrationPattern[2] = (long)( ((1 / (double)6)) * 1000 );
        }
        else {
            mLateralVibrationPattern[0] = 0;
            mLateralVibrationPattern[2] = mLateralVibrationPattern[1];
        }

        // User Delay
        mUserResponseDelay = settings.getInt("DELAY", -1);
        if (mUserResponseDelay == -1) mUserResponseDelay = 1000;
    }

    private void updateSettingsTab() {
        SeekBar seekBar;
        TextView textView;

        // Front Angle SeekBar & TextView
        seekBar = (SeekBar) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.SEEKBAR_FRONT_ANGLE_THRESHOLD);
        seekBar.setProgress(mFrontAngleThreshold);
        textView = (TextView) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.TEXTVIEW_FRONT_ANGLE_THRESHOLD);
        textView.setText("Ángulo Frontal: " + mFrontAngleThreshold + "°");

        // Front Vibration SeekBar & TextView
        seekBar = (SeekBar) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.SEEKBAR_FRONT_VIBRATION);
        seekBar.setProgress((int)((1000/mFrontVibrationPattern[1])));
        textView = (TextView) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.TEXTVIEW_FRONT_VIBRATION);
        textView.setText("Vibración Frontal: " + (1000/mFrontVibrationPattern[1]) + " Hz");

        // Lateral Angle SeekBar & TextView
        seekBar = (SeekBar) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.SEEKBAR_LATERAL_ANGLE_THRESHOLD);
        seekBar.setProgress(mLateralAngleThreshold);
        textView = (TextView) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.TEXTVIEW_LATERAL_ANGLE_THRESHOLD);
        textView.setText("Ángulo Lateral: " + mLateralAngleThreshold + "°");

        // Lateral Vibration SeekBar & TextView
        seekBar = (SeekBar) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.SEEKBAR_LATERAL_VIBRATION);
        seekBar.setProgress((int)((1000/mLateralVibrationPattern[1])));
        textView = (TextView) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.TEXTVIEW_LATERAL_VIBRATION);
        textView.setText("Vibración Lateral: " + (1000/mLateralVibrationPattern[1]) + " Hz");

        // User Delay SeekBar & TextView
        seekBar = (SeekBar) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.SEEKBAR_DELAY);
        seekBar.setProgress((int)(mUserResponseDelay /(1000*0.5)));
        textView = (TextView) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.TEXTVIEW_DELAY);
        textView.setText("Tiempo de Espera: " + (mUserResponseDelay /1000) + " s");

        // Do not load again
        mPreferencesLoaded = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        savePreferences();
    }

    private void savePreferences() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        SeekBar seekBar;

        seekBar = (SeekBar) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.SEEKBAR_FRONT_ANGLE_THRESHOLD);
        editor.putInt("FRONT_ANGLE_THRESHOLD", seekBar.getProgress());

        seekBar = (SeekBar) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.SEEKBAR_FRONT_VIBRATION);
        editor.putLong("FRONT_VIBRATION_PATTERN", 1000 / seekBar.getProgress());

        seekBar = (SeekBar) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.SEEKBAR_LATERAL_ANGLE_THRESHOLD);
        editor.putInt("LATERAL_ANGLE_THRESOLD", seekBar.getProgress());

        seekBar = (SeekBar) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.SEEKBAR_LATERAL_VIBRATION);
        editor.putLong("LATERAL_VIBRATION_PATTERN", 1000/seekBar.getProgress());

        seekBar = (SeekBar) ((SettingsTab) (mViewPagerAdapter.getItem(1))).getView(SettingsTabView.SEEKBAR_DELAY);
        editor.putInt("DELAY", (int)(seekBar.getProgress()*0.5*1000));

        editor.commit();
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
        // Inflate ViewPager
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Show saved preferences only the first the user swaps
                if(!mPreferencesLoaded && position == 1) {
                    updateSettingsTab();
                }
            }
        };

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
        slidingTabs.setOnPageChangeListener(pageChangeListener);

        // Inflate ViewPagerAdapter
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mTabTitles, mTotalTabs);

        // Attach to View Pager
        viewPager.setAdapter(mViewPagerAdapter);

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
        mUserResponseDelay = (int) (seekBarProgress * 0.5 * 1000);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

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

        if(mAcquiringOk) {
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
        else {
            textView.setTextColor(Color.BLACK);
            textView.setText("-");
        }
    }

    private void updateRightAngleTextView(int rightAngle) {
        TextView textView = (TextView) ((MainTab) (mViewPagerAdapter.getItem(0))).getView(MainTabView.TEXTVIEW_RIGHT_ANGLE);

        if(mAcquiringOk) {
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
        else {
            textView.setTextColor(Color.BLACK);
            textView.setText("-");
        }
    }

    private void updateLeftAngleTextView(int leftAngle) {
        TextView textView = (TextView)((MainTab) (mViewPagerAdapter.getItem(0))).getView(MainTabView.TEXTVIEW_LEFT_ANGLE);

        if(mAcquiringOk) {
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
        else {
            textView.setTextColor(Color.BLACK);
            textView.setText("-");
        }
    }

    //endregion

}