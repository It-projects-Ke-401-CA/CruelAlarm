package com.weiaett.cruelalarm.photo_manager;

import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.img_proc.ImgProcessor;
import com.weiaett.cruelalarm.utils.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PhotoManagerActivity extends AppCompatActivity implements
        PhotoManagerFragment.OnFragmentInteractionListener,
        RecyclerView.OnItemTouchListener,
        SurfaceHolder.Callback {

    private ActionMode actionMode;
    private GestureDetectorCompat gestureDetector;
    private PhotoManagerFragment photoManagerFragment;
    private SurfaceHolder surfaceHolder;
    private PhotoManagerAdapter photoManagerAdapter;
    private RecyclerView recyclerView;
    private View viewCamera;
    private View viewWakeUp;
    private TextView tvProcessing;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_manager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewWakeUp.setVisibility(View.GONE);
                viewCamera.setVisibility(View.VISIBLE);

                if (camera != null) {
                    try {
                        camera.setPreviewDisplay(surfaceHolder);
                        camera.startPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        RecyclerViewOnGestureListener recyclerViewOnGestureListener = new RecyclerViewOnGestureListener();
        gestureDetector = new GestureDetectorCompat(this, recyclerViewOnGestureListener);

        viewCamera = findViewById(R.id.incCamera);
        viewWakeUp = findViewById(R.id.incWakeUp);
        tvProcessing = (TextView) findViewById(R.id.tvProcessing);

        photoManagerFragment = (PhotoManagerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentPhotoManager);

        photoManagerAdapter = photoManagerFragment.getPhotoManagerAdapter();
        recyclerView = photoManagerFragment.getRecyclerView();
        recyclerView.addOnItemTouchListener(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (actionMode != null && fab.isShown()) {
                    fab.hide();
                }
                if (dy > 0 && fab.isShown()) {
                    fab.hide();
                }
                if (dy <= 0 && !fab.isShown() && actionMode == null) {
                    fab.show();
                }
            }
        });

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        camera = getCameraInstance();

        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();

        Camera.Size bestSize = supportedSizes.get(0);
        for(int i = 1; i < supportedSizes.size(); i++){
            if((supportedSizes.get(i).width * supportedSizes.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = supportedSizes.get(i);
            }
        }

        params.setPictureSize(bestSize.width, bestSize.height);
        camera.setParameters(params);

        findViewById(R.id.ivPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(null, null, PhotoManagerActivity.this.onPictureTaken);
                viewCamera.setVisibility(View.GONE);
                tvProcessing.setVisibility(View.VISIBLE);
            }
        });
    }

    private Camera.PictureCallback onPictureTaken = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File lastPhoto = ImageLoader.prepareFile(PhotoManagerActivity.this);
            if (lastPhoto == null){
                Log.d("Camera", "Error creating media file, check storage permissions");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(lastPhoto);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("Camera", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("Camera", "Error accessing file: " + e.getMessage());
            }

            Uri fileUri = Uri.fromFile(lastPhoto);
            Uri newUri = ImgProcessor.Companion.process(getApplicationContext(), fileUri, true);
            if(newUri == null){
                Toast.makeText(getApplicationContext(), "На снимке не хватает\n контрастных участков", Toast.LENGTH_SHORT).show();
                tvProcessing.setVisibility(View.GONE);
                viewCamera.setVisibility(View.VISIBLE);
            } else {
                tvProcessing.setVisibility(View.GONE);
                viewCamera.setVisibility(View.GONE);
                viewWakeUp.setVisibility(View.VISIBLE);
                photoManagerFragment.addPhoto(new File(newUri.getPath()));
            }
        }
    };

    private void toggleSelection(int position) {
        photoManagerAdapter.toggleSelection(position);
        int count = photoManagerAdapter.getSelectedItemCount();
        if (count > 0) {
            actionMode.setTitle(getString(R.string.selection_count) + photoManagerAdapter.getSelectedItemCount());
        } else {
            actionMode.finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null){
            camera.release();
            camera = null;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (camera == null) {
                camera = getCameraInstance();
            }
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d("Camera", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (surfaceHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (Exception e){
            Log.d("Camera", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(List<String> photos, int alarmId) {

    }

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        List<Integer> selectedItemPositions;

        @Override
        public boolean onDown(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (actionMode != null) {
                            toggleSelection(recyclerView.getChildAdapterPosition(view));
                        }
                    }
                });
            }
            return super.onDown(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (photoManagerAdapter.getPhotosCount() == 0 || actionMode != null) {
                return;
            }
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            actionMode = startSupportActionMode(new ActionMode.Callback(){
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    if (fab.isShown()) {
                        fab.hide();
                    }
                    MenuInflater inflater = actionMode.getMenuInflater();
                    inflater.inflate(R.menu.action_mode_photo_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    PhotoManagerActivity.this.actionMode = null;
                    photoManagerAdapter.clearSelections();
                    photoManagerAdapter.setCheckboxVisible(false);
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    if (!fab.isShown()) {
                        fab.show();
                    }
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    selectedItemPositions = photoManagerAdapter.getSelectedItems();
                    switch (menuItem.getItemId()) {
                        case R.id.action_delete:
                            for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                                photoManagerAdapter.deleteItem(selectedItemPositions.get(i));
                            }
                            actionMode.finish();
                            return true;
                        case R.id.action_select:
                            if (selectedItemPositions.size() < photoManagerAdapter.getPhotosCount()) {
                                photoManagerAdapter.selectAll();
                                actionMode.setTitle(getString(R.string.selection_count) +
                                        photoManagerAdapter.getSelectedItemCount());
                            } else {
                                photoManagerAdapter.clearSelections();
                                actionMode.finish();
                            }
                            return true;
                        default:
                            return false;
                    }
                }
            });
            toggleSelection(recyclerView.getChildAdapterPosition(view));
            photoManagerAdapter.setCheckboxVisible(true);
            photoManagerAdapter.notifyDataSetChanged();
            super.onLongPress(e);
        }
    }

    public Camera getCameraInstance(){
        if (camera == null) {
            try {
                camera = Camera.open(); // attempt to get a Camera instance
            } catch (Exception e) {
                e.printStackTrace();
                // Camera is not available (in use or does not exist)
            }
        }
        camera.setDisplayOrientation(90);
        return camera; // returns null if camera is unavailable
    }
}