package com.orlanth23.annoncesnc.listener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessRecyclerOnScrollListener extends
        RecyclerView.OnScrollListener {
    private int previousTotal = 0;
    private boolean loading = true;
    private int current_page = 1;
    private LinearLayoutManager mLinearLayoutManager;

    protected EndlessRecyclerOnScrollListener(
            LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;


    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
    }


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = mLinearLayoutManager.getChildCount();
        int totalItemCount = mLinearLayoutManager.getItemCount();
        int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        int visibleThreshold = 5;
        if (!loading
                && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {

            current_page++;

            onLoadMore();

            loading = true;
        }
    }

    public abstract void onLoadMore();
}
