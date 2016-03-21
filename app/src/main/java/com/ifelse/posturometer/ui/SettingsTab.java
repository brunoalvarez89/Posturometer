package com.ifelse.posturometer.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ifelse.posturometer.R;

/**
 * Created by Bruno on 31/10/2015.
 */
public class SettingsTab extends Fragment {

    // TextViews
    private TextView mTextViewFrontAngle;
    private TextView mTextViewFrontVibration;
    private TextView mTextViewLateralAngle;
    private TextView mTextViewLateralVibration;
    private TextView mTextViewDelay;

    // SeekBars
    private SeekBar mSeekBarFrontAngle;
    private SeekBar mSeekBarFrontVibration;
    private SeekBar mSeekBarLateralAngle;
    private SeekBar mSeekBarLateralVibration;
    private SeekBar mSeekBarDelay;

    // SeekBar Interface
    private SettingsTabCommunicator mSettingsTabCommunicator;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.tab_settings,container,false);
        setupUI(view);
        return view;
    }

    private void setupUI(View view) {
        inflate(view);
        setListeners();
    }

    private void inflate(View view) {
        // TextViews
        mTextViewFrontAngle = (TextView) view.findViewById(R.id.textViewFrontAngle);
        mTextViewFrontVibration = (TextView) view.findViewById(R.id.textViewFrontVibration);
        mTextViewLateralAngle = (TextView) view.findViewById(R.id.textViewLateralAngle);
        mTextViewLateralVibration = (TextView) view.findViewById(R.id.textViewLateralVibration);
        mTextViewDelay = (TextView) view.findViewById(R.id.textViewDelay);

        // SeekBars
        mSeekBarFrontAngle = (SeekBar) view.findViewById(R.id.seekBarFrontAngle);
        mSeekBarFrontAngle.setProgress(45);

        mSeekBarFrontVibration = (SeekBar) view.findViewById(R.id.seekBarFrontVibration);
        mSeekBarFrontVibration.setProgress(10);

        mSeekBarLateralAngle = (SeekBar) view.findViewById(R.id.seekBarLateralAngle);
        mSeekBarLateralAngle.setProgress(45);

        mSeekBarLateralVibration = (SeekBar) view.findViewById(R.id.seekBarLateralVibration);
        mSeekBarLateralVibration.setProgress(10);

        mSeekBarDelay = (SeekBar) view.findViewById(R.id.seekBarDelay);
        mSeekBarDelay.setProgress(5);
    }

    private void setListeners() {
        mSeekBarFrontAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextViewFrontAngle.setText("Ángulo Frontal: " + progress + "°");
                mSettingsTabCommunicator.seekBarProgressChanged(SeekBarType.SEEKBAR_FRONT_ANGLE, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarFrontVibration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextViewFrontVibration.setText("Vibración Frontal: " + progress);
                mSettingsTabCommunicator.seekBarProgressChanged(SeekBarType.SEEKBAR_FRONT_VIBRATION, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarLateralAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextViewLateralAngle.setText("Ángulo Lateral: " + progress + "°");
                mSettingsTabCommunicator.seekBarProgressChanged(SeekBarType.SEEKBAR_LATERAL_ANGLE, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarLateralVibration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextViewLateralVibration.setText("Vibración Lateral: " + progress);
                mSettingsTabCommunicator.seekBarProgressChanged(SeekBarType.SEEKBAR_LATERAL_VIBRATION, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextViewDelay.setText("Tiempo de Espera: " + progress + " s");
                mSettingsTabCommunicator.seekBarProgressChanged(SeekBarType.SEEKBAR_DELAY, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public interface SettingsTabCommunicator {
        void seekBarProgressChanged(SeekBarType seekBarType, int progress);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSettingsTabCommunicator = (SettingsTabCommunicator) context;
    }
}