package com.weiaett.cruelalarm.photo_manager;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
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
    List<String> photos;

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

        AutofitRecyclerView recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerViewPhotoGrid);

        Context context = view.getContext();
        List<File> files = ImageLoader.getImages(context);
        if (alarmId > 0) {
            recyclerView.setAdapter(new PhotoManagerAdapter(files, getContext(), mListener));
        } else {
            recyclerView.setAdapter(new PhotoManagerAdapter(files, getContext(), mListener, alarmId));
        }

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_photo_manager, null);
        AutofitRecyclerView recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerViewPhotoGrid);
        Context context = view.getContext();
        List<File> files = ImageLoader.getImages(context);
        final PhotoManagerAdapter photoManagerAdapter = new PhotoManagerAdapter(files, getContext(), mListener, alarmId);
        recyclerView.setAdapter(photoManagerAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DrakDialog);
        builder.setMessage("Выбор фото")
                .setView(view)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        photos = photoManagerAdapter.getSelectedImages();
                        mListener.onFragmentInteraction(photos, alarmId);
//                        Intent intent = new Intent();
//                        intent.putStringArrayListExtra("photos", (ArrayList<String>) photos);
//                        getTargetFragment().onActivityResult(getTargetRequestCode(), 0, intent);
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
