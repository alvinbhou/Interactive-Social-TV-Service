package com.flavienlaurent.discrollview.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.technalt.serverlessCafe.R;
import com.flavienlaurent.discrollview.lib.Discrollvable;

/**
 *
 */
public class DiscrollvableControllerLayout extends FrameLayout implements Discrollvable {

    private static final String TAG = "DiscrollvableRedLayout";

    private View controller1;
    private View controller2;

    private float controller1TranslationY;

    public DiscrollvableControllerLayout(Context context) {
        super(context);
    }

    public DiscrollvableControllerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DiscrollvableControllerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        controller1 = findViewById(R.id.controllerText);
        controller1TranslationY = controller1.getTranslationY();
        controller2 = findViewById(R.id.controllerImage);
    }

    @Override
    public void onResetDiscrollve() {

    }

    @Override
    public void onDiscrollve(float ratio) {
        if(ratio <= 0.65f) {
            controller1.setTranslationY(-1 * (controller1.getHeight()/1.5f) * (ratio / 0.65f));
        } else {
            float rratio = (ratio - 0.65f) / 0.35f;
            rratio = Math.min(rratio, 1.0f);
            controller1.setTranslationY(-1 * (controller1.getHeight()/1.5f));
            controller2.setAlpha(1.0f * rratio);
            controller2.setScaleX(1.0f * rratio);
            controller2.setScaleY(1.0f * rratio);
        }
    }
}
