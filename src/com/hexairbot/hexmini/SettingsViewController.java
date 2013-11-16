
package com.hexairbot.hexmini;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
//import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.hexairbot.hexmini.R;
//import com.hexairbot.hexmini.drone.DroneConfig;
//import com.hexairbot.hexmini.drone.DroneConfig.EDroneVersion;
import com.hexairbot.hexmini.adapter.SettingsViewAdapter;
import com.hexairbot.hexmini.modal.ApplicationSettings;
//import  com.hexairbot.hexmini.ui.adapters.SettingsViewAdapter;
//import  com.hexairbot.hexmini.ui.controls.ViewPagerIndicator;
//import  com.hexairbot.hexmini.ui.filters.NetworkNameFilter;
//import  com.hexairbot.hexmini.ui.listeners.OnSeekChangedListener;
//import  com.hexairbot.hexmini.utils.FontUtils;
import com.hexairbot.hexmini.ui.control.ViewPagerIndicator;

public class SettingsViewController extends ViewController
        implements OnPageChangeListener,
        OnClickListener
{

    private static final String TAG = "SettingsViewController";
    
    private SettingsViewControllerDelegate delegate;

    private List<View> settingsViews;
    
    private TextView titleTextView;
    
    private ViewPager viewPager;
    private ImageButton preBtn;
    private ImageButton nextBtn;
    
    private Button backBtn;
    
    private Button defaultSettingsBtn;
    private Button accCalibrateBtn;
    private Button magCalibrateBtn;
    
    private Button scanBtn;
    private Button upTrimBtn;
    private Button downTrimBtn;
    private Button leftTrimBtn;
    private Button rightTrimBtn;
    
    private CheckBox isLeftHandedCheckBox;
    private CheckBox isAccModeCheckBox;
    private CheckBox isHeadfreeModeCheckBox;
    
    private TextView isLeftHandedTitleTextView;
    private TextView isAccModeTextTitleView;
    private TextView isHeadfreeTitleTextView;
    
    private TextView interfaceOpacityValueTextView;
    private TextView takeOffThrottleValueTextView;
    private TextView aileronAndElevatorDeadBandValueTextView;
    private TextView rudderDeadBandValueTextView;
    
    private SeekBar interfaceOpacitySeekBar;
    private SeekBar takeOffThrottleSeekBar;
    private SeekBar aileronAndElevatorDeadBandSeekBar;
    private SeekBar rudderDeadBandSeekBar;
    
    private OnSeekBarChangeListener interfaceOpacitySeekBarListener;
    private OnSeekBarChangeListener takeOffThrottleSeekBarListener;
    private OnSeekBarChangeListener aileronAndElevatorDeadBandSeekBarListener;
    private OnSeekBarChangeListener rudderDeadBandSeekBarListener;

    private Resources res;

    private int[] titles;

    public SettingsViewController(Context context, LayoutInflater inflater, ViewGroup container, SettingsViewControllerDelegate delegate)
    {
    	res = context.getResources();
    	
    	this.delegate = delegate;
    	
    	titleTextView = (TextView) container.findViewById(R.id.titleTextView);
    	
        preBtn = (ImageButton)container.findViewById(R.id.preBtn);
        preBtn.setOnClickListener(this);
        
        nextBtn = (ImageButton)container.findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(this);
        
        defaultSettingsBtn = (Button)container.findViewById(R.id.defaultSettingsBtn);
        accCalibrateBtn = (Button)container.findViewById(R.id.accCalibrateBtn);
        magCalibrateBtn = (Button)container.findViewById(R.id.magCalibrateBtn);

        titles = new int[] {
                R.string.settings_title_connection,
                R.string.settings_title_personal,
                R.string.settings_title_angel_trim,
                R.string.settings_title_mode,
                R.string.settings_title_about
        };
    	
        backBtn = (Button)container.findViewById(R.id.backBtn);
        
        int[] pageIds = new int[]{
        		R.layout.settings_page_connection,
        		R.layout.settings_page_personal,
        		R.layout.settings_page_angel_trim,
        		R.layout.settings_page_mode,
        		R.layout.settings_page_about
        };
        
        settingsViews = initPages(inflater, pageIds);
        
        viewPager = (ViewPager) container.findViewById(R.id.viewPager);
        viewPager.setAdapter(new SettingsViewAdapter(settingsViews));
        
        
        ViewPagerIndicator viewPagerIndicator = (ViewPagerIndicator) container.findViewById(R.id.pageIndicator);
        viewPagerIndicator.setViewPager(viewPager);
        viewPagerIndicator.setOnPageChangeListener(this);
        
        final int connectionPageIdx = 0;
        final int interfacePageIdx  = 1;
        final int angelTrimPageIdx  = 2;
        final int modePageIdx       = 3;
        final int aboutPageIdx      = 4;
        
        scanBtn = (Button)settingsViews.get(connectionPageIdx).findViewById(R.id.scanBtn);
        
        upTrimBtn   = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.upTrimBtn);
        downTrimBtn = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.downTrimBtn);
        leftTrimBtn = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.leftTrimBtn);
        rightTrimBtn = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.rightTrimBtn);
        
        scanBtn.setText(R.string.btn_title_scan);
        upTrimBtn.setText(R.string.btn_title_up_trim);
        downTrimBtn.setText(R.string.btn_title_down_trim);
        leftTrimBtn.setText(R.string.btn_title_left_trim);
        rightTrimBtn.setText(R.string.btn_title_right_trim);
        
        isLeftHandedCheckBox   = (CheckBox)settingsViews.get(interfacePageIdx).findViewById(R.id.isLeftHandedCheckBox);
        isAccModeCheckBox      = (CheckBox)settingsViews.get(interfacePageIdx).findViewById(R.id.isAccModeCheckBox);
        isHeadfreeModeCheckBox = (CheckBox)settingsViews.get(interfacePageIdx).findViewById(R.id.isHeadfreeModeCheckBox);
        
        isLeftHandedTitleTextView = (TextView)settingsViews.get(interfacePageIdx).findViewById(R.id.isLeftHandedTitleTextView);
        isAccModeTextTitleView    = (TextView)settingsViews.get(interfacePageIdx).findViewById(R.id.isAccModeTitleTextView);
        isHeadfreeTitleTextView   = (TextView)settingsViews.get(interfacePageIdx).findViewById(R.id.isHeadfreeModeTitleTextView);
        
        isLeftHandedTitleTextView.setText(R.string.settings_item_left_handed);
        isAccModeTextTitleView.setText(R.string.settings_item_acc_mode);
        isHeadfreeTitleTextView.setText(R.string.settings_item_headfree_mode);
        
        interfaceOpacityValueTextView =  (TextView)settingsViews.get(interfacePageIdx).findViewById(R.id.interfaceOpacityValueTextView);
        takeOffThrottleValueTextView = (TextView)settingsViews.get(modePageIdx).findViewById(R.id.takeOffThrottleValueTextView);
        aileronAndElevatorDeadBandValueTextView = (TextView)settingsViews.get(modePageIdx).findViewById(R.id.aileronAndElevatorDeadBandValueTextView);
        rudderDeadBandValueTextView = (TextView)settingsViews.get(modePageIdx).findViewById(R.id.rudderDeadBandValueTextView);
        
        interfaceOpacitySeekBar = (SeekBar)settingsViews.get(interfacePageIdx).findViewById(R.id.interfaceOpacitySeekBar);
        takeOffThrottleSeekBar = (SeekBar)settingsViews.get(modePageIdx).findViewById(R.id.takeOffThrottleSeekBar);
        aileronAndElevatorDeadBandSeekBar = (SeekBar)settingsViews.get(modePageIdx).findViewById(R.id.aileronAndElevatorDeadBandSeekBar);
        rudderDeadBandSeekBar = (SeekBar)settingsViews.get(modePageIdx).findViewById(R.id.rudderDeadBandSeekBar);
        
        interfaceOpacitySeekBar.setMax(100);
        takeOffThrottleSeekBar.setMax(1000);
        aileronAndElevatorDeadBandSeekBar.setMax(20);
        rudderDeadBandSeekBar.setMax(20);
        
        WebView aboutWebView = (WebView)settingsViews.get(aboutPageIdx).findViewById(R.id.aboutWebView);
        aboutWebView.getSettings().setJavaScriptEnabled(true);  
        aboutWebView.loadUrl("file:///android_asset/About.html");
        
        initListeners();
        
        updateSettingsUI();
    }
    
    public void setBackBtnOnClickListner(OnClickListener listener) {
		backBtn.setOnClickListener(listener);
	}
    
    private void updateSettingsUI(){
        ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
      
        isLeftHandedCheckBox.setChecked(settings.isLeftHanded());
        isAccModeCheckBox.setChecked(settings.isAccMode());
        isHeadfreeModeCheckBox.setChecked(settings.isHeadFreeMode());
        
        interfaceOpacitySeekBar.setProgress((int)(settings.getInterfaceOpacity() * 100));
        safeSetText(interfaceOpacityValueTextView, interfaceOpacitySeekBar.getProgress() + "%");
        
        takeOffThrottleSeekBar.setProgress((int)(settings.getTakeOffThrottle() - 1000));
        safeSetText(takeOffThrottleValueTextView, (takeOffThrottleSeekBar.getProgress() + 1000) + "us");
        
        aileronAndElevatorDeadBandSeekBar.setProgress((int)(settings.getAileronDeadBand() * 100));
        safeSetText(aileronAndElevatorDeadBandValueTextView, aileronAndElevatorDeadBandSeekBar.getProgress() + "%");
        
        rudderDeadBandSeekBar.setProgress((int)(settings.getRudderDeadBand() * 100));
        safeSetText(rudderDeadBandValueTextView, rudderDeadBandSeekBar.getProgress() + "%");  	
    }

    private List<View> initPages(LayoutInflater inflater, int[] pageIds)
    {
        ArrayList<View> pageList = new ArrayList<View>(pageIds.length);

        for (int i = 0; i < pageIds.length; ++i) {
            View view = inflater.inflate(pageIds[i], null);
            //FontUtils.applyFont(inflater.getContext(), (ViewGroup) view);
            pageList.add(view);
        }

        return pageList;
    }


    private void initListeners()
    {
    	scanBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	upTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	downTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	leftTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	rightTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	magCalibrateBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	accCalibrateBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	defaultSettingsBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();

				if (delegate != null) {
					delegate.leftHandedValueDidChange(settings.isLeftHanded());
					delegate.accModeValueDidChange(settings.isAccMode());
					delegate.headfreeModeValueDidChange(settings.isHeadFreeMode());
				}
				
			}
		});
    	
  
        isLeftHandedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(
        		) {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isLeftHanded) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setLeftHanded(isLeftHanded);
				settings.save();
				if (delegate != null) {
					delegate.leftHandedValueDidChange(isLeftHanded);
				}
				
			}
		});
        
        isAccModeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isAccMode) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setIsAccMode(isAccMode);
				settings.save();
				if (delegate != null) {
					delegate.leftHandedValueDidChange(isAccMode);
				}
			}
		});
        
        isHeadfreeModeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isHeadfree) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setIsHeadFreeMode(isHeadfree);
				settings.save();
				if (delegate != null) {
					delegate.headfreeModeValueDidChange(isHeadfree);
				}
			}
		});
    	
    	interfaceOpacitySeekBarListener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setInterfaceOpacity(seekBar.getProgress() / 100.0f);
				settings.save();
				
				if (delegate != null) {
					delegate.interfaceOpacityValueDidChange(settings.getInterfaceOpacity() * 100);
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
              safeSetText(interfaceOpacityValueTextView, progress + "%");
			}
		};
		interfaceOpacitySeekBar.setOnSeekBarChangeListener(interfaceOpacitySeekBarListener);
    	
    	takeOffThrottleSeekBarListener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setTakeOffThrottle(seekBar.getProgress() + 1000);
				settings.save();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
              safeSetText(takeOffThrottleValueTextView, (progress + 1000) + "us");
			}
		};
		takeOffThrottleSeekBar.setOnSeekBarChangeListener(takeOffThrottleSeekBarListener);
		
    	aileronAndElevatorDeadBandSeekBarListener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setAileronDeadBand(seekBar.getProgress() / 100.f);
				settings.setElevatorDeadBand(settings.getAileronDeadBand());
				settings.save();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
              safeSetText(aileronAndElevatorDeadBandValueTextView, progress + "%");
			}
		};
		aileronAndElevatorDeadBandSeekBar.setOnSeekBarChangeListener(aileronAndElevatorDeadBandSeekBarListener);
		
    	rudderDeadBandSeekBarListener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setRudderDeadBand(seekBar.getProgress() / 100.f);
				settings.save();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
              safeSetText(rudderDeadBandValueTextView, progress + "%");
			}
		};
		rudderDeadBandSeekBar.setOnSeekBarChangeListener(rudderDeadBandSeekBarListener);
    }
    

    private void safeSetText(final TextView view, final String text)
    {
        if (view != null) {
            view.setText(text);
        }
    }


    public void onPageScrollStateChanged(int state)
    {
        // Left unimplemented
    }


    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
        // Left unimplemented
    }


    public void onPageSelected(int position)
    {
        if (position == 0 && preBtn.getVisibility() != View.INVISIBLE) {
            preBtn.setVisibility(View.INVISIBLE);
        } else if (preBtn.getVisibility() != View.VISIBLE) {
            preBtn.setVisibility(View.VISIBLE);
        }

        if (nextBtn.getVisibility() != View.INVISIBLE && position == (viewPager.getAdapter().getCount() - 1)) {
            nextBtn.setVisibility(View.INVISIBLE);
        } else if (nextBtn.getVisibility() != View.VISIBLE) {
            nextBtn.setVisibility(View.VISIBLE);
        }

        if(titleTextView == null){
        	Log.d("Debug", "titleTextView is null");
        }
        
        titleTextView.setText(res.getString(titles[position]));
    }


    public void onClick(View v)
    {	
        switch (v.getId()) {
        case R.id.preBtn:
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
            break;
        case R.id.nextBtn:
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            break;
        }
    }
}
