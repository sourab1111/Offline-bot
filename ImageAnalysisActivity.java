package com.offlineai.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.offlineai.app.R;
import com.offlineai.app.engine.SearchEngine;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringBuilder;

public class ImageAnalysisActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 200;
    private static final int REQUEST_GALLERY = 201;
    private static final int REQUEST_CAMERA_PERMISSION = 202;

    private ImageView imageView;
    private TextView tvResult, tvHint;
    private ProgressBar progressBar;
    private Button btnCamera, btnGallery, btnAskAI;
    private ScrollView scrollResult;

    private Uri currentImageUri;
    private String detectedLabels = "";
    private SearchEngine engine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_analysis);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Image Analysis");

        engine = SearchEngine.getInstance(this);

        imageView = findViewById(R.id.image_preview);
        tvResult = findViewById(R.id.tv_result);
        tvHint = findViewById(R.id.tv_hint);
        progressBar = findViewById(R.id.progress_bar);
        btnCamera = findViewById(R.id.btn_camera);
        btnGallery = findViewById(R.id.btn_gallery);
        btnAskAI = findViewById(R.id.btn_ask_ai);
        scrollResult = findViewById(R.id.scroll_result);

        btnCamera.setOnClickListener(v -> checkCameraAndOpen());
        btnGallery.setOnClickListener(v -> openGallery());
        btnAskAI.setOnClickListener(v -> askAIAboutImage());
    }

    private void checkCameraAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile();
        if (photoFile != null) {
            currentImageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private File createImageFile() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(null);
        try {
            return File.createTempFile("IMG_" + timestamp, ".jpg", storageDir);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null) {
                currentImageUri = data.getData();
            }
            if (currentImageUri != null) {
                displayAndAnalyze(currentImageUri);
            }
        }
    }

    private void displayAndAnalyze(Uri imageUri) {
        Glide.with(this).load(imageUri).into(imageView);
        imageView.setVisibility(View.VISIBLE);
        tvHint.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        tvResult.setText("Analyzing image...");
        scrollResult.setVisibility(View.VISIBLE);
        btnAskAI.setVisibility(View.GONE);

        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            ImageLabeler labeler = ImageLabeling.getClient(
                    new ImageLabelerOptions.Builder().setConfidenceThreshold(0.65f).build());

            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        progressBar.setVisibility(View.GONE);
                        if (labels.isEmpty()) {
                            tvResult.setText("Could not identify objects in this image. Try a clearer photo.");
                            return;
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("🔍 I can see:\n\n");
                        for (ImageLabel label : labels) {
                            int confidence = (int) (label.getConfidence() * 100);
                            sb.append("• ").append(label.getText())
                              .append("  (").append(confidence).append("% confidence)\n");
                        }

                        detectedLabels = labels.get(0).getText();
                        tvResult.setText(sb.toString());
                        btnAskAI.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        tvResult.setText("Analysis failed: " + e.getMessage());
                    });
        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
            tvResult.setText("Could not load image.");
        }
    }

    private void askAIAboutImage() {
        if (detectedLabels.isEmpty()) return;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("category_prompt", "Tell me about " + detectedLabels);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
