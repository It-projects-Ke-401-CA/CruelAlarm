package com.weiaett.cruelalarm.photo_manager;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.SparseBooleanArray;
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
    private boolean checkboxVisible = false;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    PhotoManagerAdapter(List<File> files, Context context, PhotoManagerFragment.OnFragmentInteractionListener listener) {
        this.files = files;
        this.context = context;
        this.listener = listener;
        this.checkboxVisible = false;
    }

    PhotoManagerAdapter(List<File> files, Context context,
                        PhotoManagerFragment.OnFragmentInteractionListener listener, int alarmId) {
        this.files = files;
        this.context = context;
        this.listener = listener;
        if (alarmId > 0) {
            this.alarmImages = DBHelper.getInstance(context).getAlarm(context, alarmId).getImages();
        }
        this.checkboxVisible = true;
    }

    @Override
    public int getItemViewType(int position) {
        return files.isEmpty() ? VIEW_TYPE_EMPTY_LIST_PLACEHOLDER : VIEW_TYPE_LIST_VIEW;
    }

    @Override
    public int getItemCount() {
        return files.isEmpty() ? 1 : files.size();
    }

    List<String> getSelectedImages() {
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
                final String filepath = files.get(position).getAbsolutePath();
                ((ListViewHolder)holder).imgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        Uri uri = Uri.parse("file://" + filepath);
                        intent.setDataAndType(uri, "image/*");
                        Intent chooser = Intent.createChooser(intent, "Choose");
                        context.startActivity(chooser);
                    }
                });
                ((ListViewHolder)holder).file = files.get(position);
                Glide.with(context)
                        .load(filepath)
                        .centerCrop()
                        .into(((ListViewHolder) holder).imgView);
                if (checkboxVisible) {
                    ((ListViewHolder) holder).checkBox.setVisibility(View.VISIBLE);
                    ((ListViewHolder) holder).checkBox.setChecked(alarmImages.contains(filepath) ||
                            selectedItems.get(holder.getAdapterPosition(), false));
                } else {
                    ((ListViewHolder) holder).checkBox.setVisibility(View.GONE);
                }
        }
    }

    void setCheckboxVisible(boolean isVisible) {
        checkboxVisible = isVisible;
    }

    void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        }
        else if (position >= 0) {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    void selectAll() {
        for (int i = 0; i < files.size(); i++) {
            selectedItems.put(i, true);
        }
        notifyDataSetChanged();
    }

    int getSelectedItemCount() {
        return selectedItems.size();
    }

    int getPhotosCount() {
        return files.size();
    }

    private boolean isSelectionMode() {
        return selectedItems.size() > 0;
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
            checkBox.setClickable(!isSelectionMode());

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    String path = file.getAbsolutePath();
                    if (!isSelectionMode()) {
                        if (b) {
                            selectedImages.add(path);
                        } else {
                            if (selectedImages.contains(path)) {
                                selectedImages.remove(path);
                            }
                        }
                    }
                }
            });
        }
    }

    void addItem(File file) {
        files.add(file);
        notifyItemChanged(files.size() - 1);
    }

    void deleteItem(int pos) {
        DBHelper.getInstance(context).deleteImageAlarm(files.get(pos).getAbsolutePath());
        files.get(pos).delete();
        files.remove(pos);
        notifyItemRemoved(pos);
    }

    List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    private class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View view) {
            super(view);
        }
    }
}
