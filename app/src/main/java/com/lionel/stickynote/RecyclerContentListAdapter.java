package com.lionel.stickynote;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerContentListAdapter extends RecyclerView.Adapter<RecyclerContentListAdapter.MyViewHolder> {

    private ArrayList<String> mContentItemList;

    RecyclerContentListAdapter(ArrayList<String> contentItemList) {
        mContentItemList = contentItemList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mTxtContentItemIndex;
        private EditText mEdtContentItem;

        MyViewHolder(View itemView) {
            super(itemView);
            mTxtContentItemIndex = itemView.findViewById(R.id.txtContentItemIndex);
            mEdtContentItem = itemView.findViewById(R.id.edtContentItem);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View viewItem = layoutInflater.inflate(R.layout.content_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(viewItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mTxtContentItemIndex.setText((position + 1) + ". ");


        // kill the old TextWatcher!
        // if don't kill, the old TextWatcher will active as the ViewHolder reuse,
        // and the position inside the old TextWatcher was wrong.
        // so we have to build a new TextWatcher by passing a correct position.
        // find the old TextWatcher which was added before by getTag()
        TextWatcher oldTextWatcher  = (TextWatcher)holder.mEdtContentItem.getTag();
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
}
