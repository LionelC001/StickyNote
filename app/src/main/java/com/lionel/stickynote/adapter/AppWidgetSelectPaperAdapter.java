package com.lionel.stickynote.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lionel.stickynote.R;
import com.lionel.stickynote.fieldclass.PaperProperty;
import com.lionel.stickynote.preference.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class AppWidgetSelectPaperAdapter extends RecyclerView.Adapter<AppWidgetSelectPaperAdapter.CustomViewHolder> {

    private final Context context;
    private List<PaperProperty> data = new ArrayList<>();

    private int selectedPageId = 0;

    public AppWidgetSelectPaperAdapter(Context context) {
        this.context = context;

        int selectedPageId = PreferencesUtil.getAppWidgetPageId();
        if (selectedPageId != -1) this.selectedPageId = selectedPageId;
    }

    public void setData(List<PaperProperty> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        public final View rootView;
        public final ImageView imgChecked;
        public final TextView txtTitle;

        public CustomViewHolder(View view) {
            super(view);
            rootView = view.findViewById(R.id.rootView);
            imgChecked = view.findViewById(R.id.imgChecked);
            txtTitle = view.findViewById(R.id.txtTitle);
        }
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_widget_select_paper, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.txtTitle.setText(!TextUtils.isEmpty(data.get(position).getTitle()) ? data.get(position).getTitle() : "No Title");

        if (selectedPageId == data.get(position).getPaperId()) holder.imgChecked.setVisibility(View.VISIBLE);
        else holder.imgChecked.setVisibility(View.INVISIBLE);

        holder.rootView.setOnClickListener(v -> {
            selectedPageId = data.get(position).getPaperId();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public int getSelectedPosition() {
        return selectedPageId;
    }
}
