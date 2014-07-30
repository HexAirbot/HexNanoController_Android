package com.hexairbot.hexmini.ipc.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

@SuppressLint("ValidFragment")
public class IpcAlertDialog extends DialogFragment {
    private int title = -1;
    private int message = -1;
    private int pbtn_text = -1;
    private int nbtn_text = -1;
    private IpcAlertDialogHandler handler = null;
    private int icon = -1;
    private int theme = -1;

    public IpcAlertDialog(int title, int message, int pButton_content,
	    int nButton_content, IpcAlertDialogHandler handler) {
	this.title = title;
	this.message = message;
	this.pbtn_text = pButton_content;
	this.nbtn_text = nButton_content;
	this.handler = handler;
    }
    
    public IpcAlertDialog() {
	super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if(theme != -1) {
        	setStyle(DialogFragment.STYLE_NORMAL,theme);
	}
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	if (icon != -1) {
	    builder.setIcon(icon);
	}
	if (title != -1)
	    builder.setTitle(title);
	if (message != -1) {
	    builder.setMessage(message);
	}
	if (pbtn_text != -1) {
	    builder.setPositiveButton(pbtn_text,
		    new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,
				int whichButton) {
			    if (handler != null) {
				handler.positive();
			    }
			}
		    });
	}
	if (nbtn_text != -1) {
	    builder.setNegativeButton(nbtn_text,
		    new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,
				int whichButton) {
			    if (handler != null) {
				handler.negtive();
			    }
			}
		    });
	}
	return builder.create();
    }

    public void setTitle(int title) {
	this.title = title;
    }

    public void setMessage(int message) {
	this.message = message;
    }

    public void setPbtn_text(int pbtn_text) {
	this.pbtn_text = pbtn_text;
    }

    public void setNbtn_text(int nbtn_text) {
	this.nbtn_text = nbtn_text;
    }

    public void setHandler(IpcAlertDialogHandler handler) {
	this.handler = handler;
    }

    public void setIcon(int icon) {
	this.icon = icon;
    }

    public void setTheme(int mtheme) {
	theme = mtheme;
    }
}
