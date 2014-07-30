/**
 * 
 */
/**
 * @author Administrator
 *
 */
package com.hexairbot.hexmini.ipc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.hexairbot.hexmini.R;
import com.vmc.ipc.util.DebugHandler;

public class MainActivityLayout extends RelativeLayout {

    private static final String TAG = "MainActivityLayout";

    private static final int DEFAULT_SQUARE_SIZE = 480;
    private static final int DEFAULT_BUTTON_SIZE = 215;
    private static final int DEFAULT_TEXT_GAP = 50;

    private ImageButton leftTop;
    private ImageButton rightTop;
    private ImageButton leftBottom;
    private ImageButton rightBottom;
    private ImageButton center;

    public MainActivityLayout(Context context, AttributeSet attrs) {
	super(context, attrs);

	setupViews();
    }

    private void setupViews() {
	leftTop = new ImageButton(this.getContext());
	RelativeLayout.LayoutParams lplt = new RelativeLayout.LayoutParams(
		android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
		android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	lplt.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	lplt.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	leftTop.setLayoutParams(lplt);
	leftTop.setScaleType(ImageView.ScaleType.FIT_XY);
	leftTop.setPadding(0, 0, 0, 0);
	leftTop.setImageResource(R.drawable.left_top);
	leftTop.setBackgroundDrawable(null);
	leftTop.setVisibility(View.INVISIBLE);
	this.addView(leftTop);

	rightTop = new ImageButton(this.getContext());
	RelativeLayout.LayoutParams lprt = new RelativeLayout.LayoutParams(
		android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
		android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	lprt.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	lprt.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	rightTop.setLayoutParams(lprt);
	rightTop.setScaleType(ImageView.ScaleType.FIT_XY);
	rightTop.setPadding(0, 0, 0, 0);
	rightTop.setImageResource(R.drawable.right_top);
	rightTop.setBackgroundDrawable(null);
	this.addView(rightTop);

	leftBottom = new ImageButton(this.getContext());
	RelativeLayout.LayoutParams lplb = new RelativeLayout.LayoutParams(
		android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
		android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	lplb.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	lplb.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	leftBottom.setLayoutParams(lplb);
	leftBottom.setScaleType(ImageView.ScaleType.FIT_XY);
	leftBottom.setPadding(0, 0, 0, 0);
	leftBottom.setImageResource(R.drawable.left_bottom);
	leftBottom.setBackgroundDrawable(null);
	leftBottom.setVisibility(View.INVISIBLE);
	this.addView(leftBottom);

	rightBottom = new ImageButton(this.getContext());
	RelativeLayout.LayoutParams lprb = new RelativeLayout.LayoutParams(
		android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
		android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	lprb.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	lprb.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	rightBottom.setLayoutParams(lprb);
	rightBottom.setScaleType(ImageView.ScaleType.FIT_XY);
	rightBottom.setPadding(0, 0, 0, 0);
	rightBottom.setImageResource(R.drawable.right_bottom);
	rightBottom.setBackgroundDrawable(null);
	this.addView(rightBottom);

	center = new ImageButton(this.getContext());
	RelativeLayout.LayoutParams lpc = new RelativeLayout.LayoutParams(
		android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
		android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	lpc.addRule(RelativeLayout.CENTER_IN_PARENT);
	center.setLayoutParams(lpc);
	center.setScaleType(ImageView.ScaleType.FIT_XY);
	center.setPadding(0, 0, 0, 0);
	center.setImageResource(R.drawable.center);
	center.setBackgroundDrawable(null);
	this.addView(center);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// TODO Auto-generated method stub

	int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
	int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
	int square = Math.min(w, h);
	DebugHandler.logd(TAG, String.format("onmeasure(%1$d,%2$d)", w, h));

	ViewGroup.LayoutParams lpft = leftTop.getLayoutParams();
	lpft.width = DEFAULT_BUTTON_SIZE * square / DEFAULT_SQUARE_SIZE;
	lpft.height = DEFAULT_BUTTON_SIZE * square / DEFAULT_SQUARE_SIZE;
	leftTop.setLayoutParams(lpft);

	ViewGroup.LayoutParams lprt = rightTop.getLayoutParams();
	lprt.width = DEFAULT_BUTTON_SIZE * square / DEFAULT_SQUARE_SIZE;
	lprt.height = DEFAULT_BUTTON_SIZE * square / DEFAULT_SQUARE_SIZE;
	rightTop.setLayoutParams(lprt);

	ViewGroup.LayoutParams lplb = leftBottom.getLayoutParams();
	lplb.width = DEFAULT_BUTTON_SIZE * square / DEFAULT_SQUARE_SIZE;
	lplb.height = DEFAULT_BUTTON_SIZE * square / DEFAULT_SQUARE_SIZE;
	leftBottom.setLayoutParams(lplb);

	ViewGroup.LayoutParams lprb = rightBottom.getLayoutParams();
	lprb.width = DEFAULT_BUTTON_SIZE * square / DEFAULT_SQUARE_SIZE;
	lprb.height = DEFAULT_BUTTON_SIZE * square / DEFAULT_SQUARE_SIZE;
	rightBottom.setLayoutParams(lprb);

	ViewGroup.LayoutParams lpc = center.getLayoutParams();
	lpc.width = 100 * square / DEFAULT_SQUARE_SIZE;
	lpc.height = 100 * square / DEFAULT_SQUARE_SIZE;
	center.setLayoutParams(lpc);

	boolean isP = w <= h;
	int spec = isP ? widthMeasureSpec : heightMeasureSpec;
	super.onMeasure(spec, spec);
	// this.setMeasuredDimension(square,square);
    }

    public ImageButton getLeftTop() {
	return leftTop;
    }

    public ImageButton getRightTop() {
	return rightTop;
    }

    public ImageButton getLeftBottom() {
	return leftBottom;
    }

    public ImageButton getRightBottom() {
	return rightBottom;
    }

    public ImageButton getCenter() {
	return center;
    }

}