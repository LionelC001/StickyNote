package com.lionel.stickynote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class RecyclerContentListAdapter extends RecyclerView.Adapter<RecyclerContentListAdapter.MyViewHolder>
        implements SimpleItemTouchHelper.ItemMoveDismissInterface {

    private String mItemBGColor, mItemIndexColor, mItemTextColor;
    private ArrayList<String> mContentItemList;
    private Context mContext;

    RecyclerContentListAdapter(ArrayList<String> contentItemList, String itemBG, String itemIndex, String itemText) {
        mContentItemList = contentItemList;
        mItemBGColor = itemBG;
        mItemIndexColor = itemIndex;
        mItemTextColor = itemText;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mTxtContentItemIndex;
        private EditText mEdtContentItem;
        private CardView mContentItemCardView;

        MyViewHolder(View itemView) {
            super(itemView);
            mTxtContentItemIndex = itemView.findViewById(R.id.txtContentItemIndex);
            mEdtContentItem = itemView.findViewById(R.id.edtContentItem);
            mContentItemCardView = itemView.findViewById(R.id.contentItemCardView);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View viewItem = layoutInflater.inflate(R.layout.content_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(viewItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // set color
        holder.mContentItemCardView.setCardBackgroundColor(Color.parseColor(mItemBGColor));
        holder.mTxtContentItemIndex.setTextColor(Color.parseColor(mItemIndexColor));
        holder.mEdtContentItem.setTextColor(Color.parseColor(mItemTextColor));

        holder.mTxtContentItemIndex.setText((position + 1) + ". ");

        // kill the old TextWatcher!
        // if don't kill, the old TextWatcher will active as the ViewHolder reuse,
        // and the position inside the old TextWatcher was wrong.
        // so we have to build a new TextWatcher by passing a correct position.
        // find the old TextWatcher which was added before by getTag()
        TextWatcher oldTextWatcher = (TextWatcher) holder.mEdtContentItem.getTag();
        if (oldTextWatcher != null)
            holder.mEdtContentItem.removeTextChangedListener(oldTextWatcher);

        TextWatcher newTextWatcher = new MyTextChangedListener(position);
        holder.mEdtContentItem.setTag(newTextWatcher);
        holder.mEdtContentItem.addTextChangedListener(newTextWatcher);

        // have to setText after killed the old TextWatcher
        holder.mEdtContentItem.setText(mContentItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return mContentItemList.size();
    }

    // to listen if the edittext of the content item change or not
    class MyTextChangedListener implements TextWatcher {

        private int mPosition;

        MyTextChangedListener(int position) {
            mPosition = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mContentItemList.set(mPosition, s.toString());
        }
    }


    // to swap position as item drag to another's position
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mContentItemList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        notifyItemChanged(toPosition);
        notifyItemChanged(fromPosition);
    }

    // to delete item as swiping away
    @Override
    public void onItemDismiss(final int position) {
        new AlertDialog.Builder(mContext)
                .setMessage("Are you sure for deleting this item?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContentItemList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, mContentItemList.size() - position);
                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(true)
                .show();
        notifyDataSetChanged();
    }
}
