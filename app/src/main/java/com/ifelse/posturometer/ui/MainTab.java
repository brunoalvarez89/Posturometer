package com.ifelse.posturometer.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ifelse.posturometer.R;

/**
 * Created by Bruno on 31/10/2015.
 */
public class MainTab extends Fragment implements View.OnClickListener {

    private Button mButtonStart;
    private boolean mStart;

    private MainTabInterface mMainTabInterface;

    private TextView mTextViewFrontAngle;
    private TextView mTextViewRightAngle;
    private TextView mTextViewLeftAngle;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_main, container, false);
        setupUI(view);
        return view;
    }

    public void setupUI(View view) {
        inflate(view);
        setListeners();
    }

    private void inflate(View view) {
        mButtonStart = (Button) view.findViewById(R.id.buttonStart);
        mTextViewFrontAngle = (TextView) view.findViewById(R.id.textViewFrontAngle);
        mTextViewRightAngle = (TextView) view.findViewById(R.id.textViewRightAngle);
        mTextViewLeftAngle = (TextView) view.findViewById(R.id.textViewLeftAngle);
    }

    private void setListeners() {
        mButtonStart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //do what you want to do when button is clicked
        switch (v.getId()) {
            case R.id.buttonStart:
                mStart = !mStart;

                if (mStart) {
                    mMainTabInterface.buttonStart(true);
                    mButtonStart.setText("Parar");
                }
                else {
                    mMainTabInterface.buttonStart(false);
                    mButtonStart.setText("Empezar");
                }
                break;
        }
    }

    public interface MainTabInterface {
        void buttonStart(boolean start);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainTabInterface = (MainTabInterface) context;
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

            default:
                return null;
        }
    }

}