package com.lionel.stickynote.itemhelper;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.lionel.stickynote.adapter.RecyclerContentListAdapter;

import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;

public class SimpleItemTouchHelper extends ItemTouchHelper.Callback {

    private ItemMoveDismissInterface mRecyclerAdapter;

    public interface ItemMoveDismissInterface {
        void onItemMove(int from, int to);
        void onItemDismiss(int position);
    }

    public SimpleItemTouchHelper(RecyclerContentListAdapter adapter) {
        mRecyclerAdapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(DOWN | UP | RIGHT | LEFT, LEFT | RIGHT);
    }

    // call this method as item is dragging
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mRecyclerAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    // call this method as item is swiping
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mRecyclerAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }
}
