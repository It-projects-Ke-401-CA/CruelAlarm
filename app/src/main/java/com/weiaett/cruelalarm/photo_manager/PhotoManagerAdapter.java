package com.weiaett.cruelalarm.photo_manager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.utils.DBHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


class PhotoManagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_EMPTY_LIST_PLACEHOLDER = 0;
    private static final int VIEW_TYPE_LIST_VIEW = 1;

    private final List<File> files;
    private final PhotoManagerFragment.OnFragmentInteractionListener listener;
    private Context context;
    private List<String> alarmImages = new ArrayList<>();
    private List<String> selectedImages = new ArrayList<>();

    PhotoManagerAdapter(List<File> files, Context context, PhotoManagerFragment.OnFragmentInteractionListener listener) {
        this.files = files;
        this.context = context;
        this.listener = listener;
    }

    PhotoManagerAdapter(List<File> files, Context context,
                        PhotoManagerFragment.OnFragmentInteractionListener listener, int alarmId) {
        this.files = files;
        this.context = context;
        this.listener = listener;
        if (alarmId > 0) {
            this.alarmImages = DBHelper.getInstance(context).getAlarm(context, alarmId).getImages();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return files.isEmpty() ? VIEW_TYPE_EMPTY_LIST_PLACEHOLDER : VIEW_TYPE_LIST_VIEW;
    }

    @Override
    public int getItemCount() {
        return files.isEmpty() ? 1 : files.size();
    }

    public List<String> getSelectedImages() {
        return selectedImages;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view;
        switch(viewType) {
            case VIEW_TYPE_EMPTY_LIST_PLACEHOLDER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.empty_photo_manager_placeholder, parent, false);
                return new EmptyViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.grid_photo_layout, parent, false);
                return new ListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch(getItemViewType(position)) {
            case VIEW_TYPE_EMPTY_LIST_PLACEHOLDER:
                break;
            case VIEW_TYPE_LIST_VIEW:
                String filepath = files.get(position).getAbsolutePath();
                ((ListViewHolder)holder).file = files.get(position);
                Glide.with(context)
                        .load(filepath)
                        .centerCrop()
                        .into(((ListViewHolder)holder).imgView);
                ((ListViewHolder) holder).checkBox.setChecked(alarmImages.contains(filepath));
        }
    }

    private class ListViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final ImageView imgView;
        final CheckBox checkBox;
        File file;

        ListViewHolder(View view) {
            super(view);
            this.view = view;
            imgView = (ImageView) view.findViewById(R.id.image);
            checkBox = (CheckBox) view.findViewById(R.id.checkBox);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    String path = file.getAbsolutePath();
                    if (b) {
                        selectedImages.add(path);
                    } else {
                        if (selectedImages.contains(path)) {
                            selectedImages.remove(path);
                        }
                    }
                }
            });
        }
    }

    private class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View view) {
            super(view);
        }
    }
}
