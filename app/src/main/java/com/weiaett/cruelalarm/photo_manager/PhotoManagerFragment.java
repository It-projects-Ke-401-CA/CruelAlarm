package com.weiaett.cruelalarm.photo_manager;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.graphics.AutofitRecyclerView;
import com.weiaett.cruelalarm.utils.ImageLoader;

import java.io.File;
import java.util.List;


public class PhotoManagerFragment extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private int alarmId = -1;
    private List<String> photos;
    private PhotoManagerAdapter photoManagerAdapter;
    private AutofitRecyclerView recyclerView;

    public PhotoManagerFragment() {}

    public static PhotoManagerFragment newInstance(int alarm_id) {
        PhotoManagerFragment fragment = new PhotoManagerFragment();
        Bundle args = new Bundle();
        args.putInt("alarmId", alarm_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            alarmId = getArguments().getInt("alarmId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_manager, container, false);

        Context context = view.getContext();
        List<File> files = ImageLoader.getImages(context);

        recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerViewPhotoGrid);
//        if (files.isEmpty()) {
//            recyclerView.setEmptyLayoutManager();
//        }

        if (alarmId > 0) {
            photoManagerAdapter = new PhotoManagerAdapter(files, getContext(), mListener, alarmId);
            recyclerView.setAdapter(photoManagerAdapter);
        } else {
            photoManagerAdapter = new PhotoManagerAdapter(files, getContext(), mListener);
            recyclerView.setAdapter(photoManagerAdapter);
        }

        return view;
    }

    RecyclerView getRecyclerView() {
        return recyclerView;
    }

    PhotoManagerAdapter getPhotoManagerAdapter() {
        return photoManagerAdapter;
    }

    void addPhoto(File file) {
        photoManagerAdapter.addItem(file);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_photo_manager, null);
        AutofitRecyclerView recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerViewPhotoGrid);
        Context context = view.getContext();
        List<File> files = ImageLoader.getImages(context);
        if (files.isEmpty()) {
            recyclerView.setEmptyLayoutManager();
        }
        final PhotoManagerAdapter photoManagerAdapter = new PhotoManagerAdapter(files, getContext(),
                mListener, alarmId);
        recyclerView.setAdapter(photoManagerAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DrakDialog);
        builder.setMessage("Выбор фото")
                .setView(view)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        photos = photoManagerAdapter.getSelectedImages();
                        mListener.onFragmentInteraction(photos, alarmId);
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(List<String> photos, int alarmId);
    }
}
