package com.exoticdevs.helper;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by bassant on 5/27/17.
 */

public class PreCachingGridLayoutManager extends GridLayoutManager {
    private int extraLayoutSpace;

    public PreCachingGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public void setExtraLayoutSpace(int extraLayoutSpace) {
        this.extraLayoutSpace = extraLayoutSpace;
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        return extraLayoutSpace;
    }
}
