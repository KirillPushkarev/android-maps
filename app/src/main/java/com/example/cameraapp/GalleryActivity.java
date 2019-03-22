package com.example.cameraapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GalleryActivity extends AppCompatActivity {
    // Request codes
    static final int REQUEST_IMAGE_CAPTURE_CODE = 1;

    // Saving state keys
    static final String MARKER_KEY = "MARKER";
    static final String CURRENT_IMAGE_PATH_STATE_KEY = "CURRENT_IMAGE_PATH";
    static final String IMAGES_STATE_KEY = "IMAGES";

    private Button captureButton;
    private Button returnButton;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private String markerId;
    private String currentImagePath;
    private ArrayList<String> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreInstanceState(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        returnButton = findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToParentActivity();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(images);
        recyclerView.setAdapter(mAdapter);
    }

    protected void restoreInstanceState(Bundle savedInstanceState){
        if (savedInstanceState != null) {
            markerId = savedInstanceState.getString(MARKER_KEY);
            currentImagePath = savedInstanceState.getString(CURRENT_IMAGE_PATH_STATE_KEY);
            images = savedInstanceState.getStringArrayList(IMAGES_STATE_KEY);
        } else {
            Intent intent = getIntent();
            markerId = intent.getStringExtra(MapsActivity.MARKER_ID_KEY);
            images = intent.getStringArrayListExtra(MapsActivity.IMAGES_KEY);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentImagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            images.add(currentImagePath);
            mAdapter.notifyItemInserted(images.size() - 1);
        }
    }

    private void returnToParentActivity() {
        Intent data = new Intent();
        data.putExtra(MapsActivity.MARKER_ID_KEY, markerId);
        data.putStringArrayListExtra(MapsActivity.IMAGES_KEY, images);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(MARKER_KEY, markerId);
        savedInstanceState.putString(CURRENT_IMAGE_PATH_STATE_KEY, currentImagePath);
        savedInstanceState.putStringArrayList(IMAGES_STATE_KEY, images);
    }
}
