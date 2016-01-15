package com.ifelse.posturometer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Bruno on 31/10/2015.
 */
public class MainTab extends Fragment implements View.OnClickListener {

    private Button mButtonStart;
    private Button mButtonStop;
    private MainTabCommunicator mMainTabCommunicator;

    private TextView mTextViewFrontAngle;
    private TextView mTextViewRightAngle;
    private TextView mTextViewBackAngle;
    private TextView mTextViewLeftAngle;

    private View mThisView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mThisView = inflater.inflate(R.layout.tab_main, container, false);
        setupUI(mThisView);
        return mThisView;
    }

    public void setupUI(View view) {
        inflate(view);
        setListeners();
    }

    private void inflate(View view) {
        mButtonStart = (Button) view.findViewById(R.id.buttonStart);
        mButtonStop = (Button) view.findViewById(R.id.buttonStop);
        mTextViewFrontAngle = (TextView) view.findViewById(R.id.textViewFrontAngle);
        mTextViewRightAngle = (TextView) view.findViewById(R.id.textViewRightAngle);
        mTextViewLeftAngle = (TextView) view.findViewById(R.id.textViewLeftAngle);
    }

    private void setListeners() {
        mButtonStart.setOnClickListener(this);
        mButtonStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //do what you want to do when button is clicked
        switch (v.getId()) {
            case R.id.buttonStart:
                mMainTabCommunicator.buttonStart();
                break;
            case R.id.buttonStop:
                mMainTabCommunicator.buttonStop();
                break;
        }
    }

    public interface MainTabCommunicator {
        void buttonStart();
        void buttonStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainTabCommunicator = (MainTabCommunicator) context;
    }

    public View getView(MainTabView mainTabView) {
        switch (mainTabView) {
            case TEXTVIEW_FRONT_ANGLE:
                return mTextViewFrontAngle;

            case TEXTVIEW_RIGHT_ANGLE:
                return mTextViewRightAngle;

            case TEXTVIEW_LEFT_ANGLE:
                return mTextViewLeftAngle;

            case BUTTON_START:
                return mButtonStart;

            case BUTTON_STOP:
                return mButtonStop;

            default:
                return null;
        }
    }

}