package com.flavienlaurent.discrollview.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.technalt.serverlessCafe.R;
import com.flavienlaurent.discrollview.lib.Discrollvable;


/**
 *
 */
public class DiscrollvableHelpLayout extends LinearLayout implements Discrollvable {

    private static final String TAG = "DiscrollvablePurpleLayout";

    private View settingView;
     private View helpView;

    private float settingViewTranslationX;
    private float helpViewTranslationX;

    public DiscrollvableHelpLayout(Context context) {
        super(context);
    }

    public DiscrollvableHelpLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DiscrollvableHelpLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        settingView = findViewById(R.id.settingsImage);
        settingViewTranslationX = settingView.getTranslationX();
        helpView = findViewById(R.id.helpImage);
        helpViewTranslationX = helpView.getTranslationX();
    }

    @Override
    public void onResetDiscrollve() {
        settingView.setAlpha(0);
        helpView.setAlpha(0);
        settingView.setTranslationX(settingViewTranslationX);
        helpView.setTranslationX(helpViewTranslationX);
    }

    @Override
    public void onDiscrollve(float ratio) {
        if(ratio <= 0.5f) {
            helpView.setTranslationX(0);
            helpView.setAlpha(0.0f);
            float rratio = ratio / 0.5f;
            settingView.setTranslationX(settingViewTranslationX * (1 - rratio));
            settingView.setAlpha(rratio);
        } else {
            settingView.setTranslationX(0);
            settingView.setAlpha(1.0f);
            float rratio = (ratio - 0.5f) / 0.5f;
            helpView.setTranslationX(helpViewTranslationX * (1 - rratio));
            helpView.setAlpha(rratio);
        }
    }
}
