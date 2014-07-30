package com.hexairbot.hexmini.ipc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hexairbot.hexmini.R;

public class SquareRelativeLayout extends RelativeLayout implements Checkable{

    private boolean checked = false;
    ImageView mCheckBox = null;
    Context mContext;
    
    public SquareRelativeLayout(Context context) {
	this(context, null,0);
    }
    
    public SquareRelativeLayout(Context context, AttributeSet attrs) {
	this(context, attrs,0);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs,
	    int defStyle) {
	super(context, attrs, defStyle);
	// TODO Auto-generated constructor stub
	mContext = context;
	init();
    }

    private void init() {
	LayoutInflater.from(mContext).inflate(R.layout.media_item_layout, this);  
	mCheckBox = (ImageView)this.findViewById(R.id.select_check);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// TODO Auto-generated method stub
	super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }


    @Override
    public boolean isChecked() {
	// TODO Auto-generated method stub
	return checked;
    }

    @Override
    public void setChecked(boolean checked) {
	// TODO Auto-generated method stub
	this.checked = checked;
	if(mCheckBox != null ) {
	    mCheckBox.setVisibility(checked?View.VISIBLE:View.INVISIBLE);
	}
    }

    @Override
    public void toggle() {
	// TODO Auto-generated method stub
	this.setChecked(!checked);
    }
}
