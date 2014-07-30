package com.hexairbot.hexmini.ipc.view;

import java.util.ArrayList;

import android.content.Context;

public interface OnGalleryItemClick {
    public void onClick(int position,Context context);
    public void delete(ArrayList<Long> selects);
    public void destroy();
}
