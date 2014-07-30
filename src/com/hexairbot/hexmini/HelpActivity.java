/**
 * 
 */
package com.hexairbot.hexmini;

import java.util.ArrayList;
import java.util.List;

import com.hexairbot.hexmini.ui.control.ViewPagerIndicator;

import com.hexairbot.hexmini.R;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author koupoo
 *
 */
public class HelpActivity extends Activity implements OnPageChangeListener{
    private List<View> helpViews;
    private ViewPager viewPager;    
    private Button closeBtn;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
		
		setContentView(R.layout.help_screen);
		
		closeBtn = (Button)findViewById(R.id.closeBtn);
		
		closeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        
        int[] pageIds = new int[]{
        		R.layout.help_page_01,
        		R.layout.help_page_02,
        		R.layout.help_page_03,
        		R.layout.help_page_04,
        		R.layout.help_page_05,
        		R.layout.help_page_06,
        };
        
        
       LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       helpViews = initPages(inflater, pageIds);
        
       viewPager = (ViewPager)findViewById(R.id.helpViewPager);
       viewPager.setAdapter(new HelpViewAdapter(helpViews));
        
       ViewPagerIndicator viewPagerIndicator = (ViewPagerIndicator)findViewById(R.id.helpPagerIndicator);
       viewPagerIndicator.setViewPager(viewPager);
       viewPagerIndicator.setOnPageChangeListener(this);
	}
	
    private List<View> initPages(LayoutInflater inflater, int[] pageIds)
    {
        ArrayList<View> pageList = new ArrayList<View>(pageIds.length);

        for (int i = 0; i < pageIds.length; ++i) {
            View view = inflater.inflate(pageIds[i], null);
            pageList.add(view);
        }

        return pageList;
    }

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
	}
}


class HelpViewAdapter extends PagerAdapter 
{

	private List<View> views;
	
	public HelpViewAdapter(List<View> views)
	{
		this.views = views;
	}
	

	@Override
	public Object instantiateItem(ViewGroup container, int position) 
	{	
		View view = views.get(position);
		((ViewPager)container).addView(view, 0);
		
		return view;
	}
	

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) 
	{
		((ViewPager)container).removeView((View)object);
	}


	@Override
	public int getCount() 
	{
		return views.size();
	}


	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0.equals(arg1);
	}
}

