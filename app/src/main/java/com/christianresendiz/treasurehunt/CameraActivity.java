package com.christianresendiz.treasurehunt;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

public class CameraActivity extends AppCompatActivity {

    private static final String CLOUD_VISION_API_KEY = "AIzaSyCMX6GPdOFCaftyjkoE5d8GUGOyJBD1ezg";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final String TAG = CameraActivity.class.getSimpleName();
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    TreasureListFragment treasureListFragment;
    TextView instruct;
    TextView challengeTitle;
    ImageView happyFace;
    ImageButton btnList;
    Button btnCamera;
    String message;
    RelativeLayout background;
    int tries = 5;
    RotateAnimation r;
    RotateAnimation r2;
    int difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        difficulty = getIntent().getIntExtra("difficulty", 0);
        treasureListFragment = (TreasureListFragment) getSupportFragmentManager().findFragmentById(R.id.listFrag);
        instruct = (TextView) findViewById(R.id.instruct);
        challengeTitle = (TextView) findViewById(R.id.newChallenge);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnList = (ImageButton) findViewById(R.id.newList);
        happyFace = (ImageView) findViewById(R.id.happy);
        background = (RelativeLayout) findViewById(R.id.background);

        r = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r.setDuration((long) 500);
        r.setRepeatCount(0);

        r2 = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r2.setInterpolator(new LinearInterpolator());
        r2.setDuration((long) 500);
        r2.setRepeatCount(Animation.INFINITE);

        treasureListFragment.getFortunes(difficulty);
        treasureListFragment.colorText();
        instruct.setText("Find each treasure below\nTap New Challenge for another list\nRemaining Shots: " + tries + "");
        btnCamera.setClickable(true);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                challengeTitle.setText(R.string.happy_hunting);
                btnList.setClickable(false);
                startCamera();
            }
        });
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                happyFace.setVisibility(View.INVISIBLE);
                challengeTitle.setText(R.string.new_challenge);
                background.setBackgroundColor(getResources().getColor(R.color.background));
                tries = 5;
                treasureListFragment.getFortunes(difficulty);
                treasureListFragment.colorText();
                instruct.setText("Find each treasure below\nTap New Challenge for another list\nRemaining Shots: " + tries + "");
                treasureListFragment.resetFlags();
                btnCamera.setClickable(true);
                btnCamera.setText(R.string.begin_hunt);
                btnList.startAnimation(r);
            }
        });
    }

    public void startCamera(){
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                1200);
                callCloudVision(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        btnList.startAnimation(r2);
        instruct.setText(R.string.instruct_loading);
        btnList.setBackgroundDrawable(getDrawable(R.drawable.reload));
        challengeTitle.setText(R.string.scanning);
        btnCamera.setText("");
        btnCamera.setClickable(false);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                r2.cancel();
                btnList.setBackgroundDrawable(getDrawable(R.drawable.checkmark));
                challengeTitle.setText(R.string.finished_scanning);
                tries--;
                showResults();
                crossOut();
                if(!checkWin()) {
                    if (tries == 0) {
                        instruct.setText(R.string.lose);
                        btnCamera.setText("");
                        btnList.setClickable(true);
                        btnList.setBackgroundDrawable(getDrawable(R.drawable.reload));
                        challengeTitle.setText(R.string.new_challenge);
                    } else {
                        btnCamera.setClickable(true);
                        btnCamera.setText(R.string.continue_hunt);
                    }
                }
            }
        }.execute();
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        message = "Found:\n\n";
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format(Locale.US, "%.2f%%:  ", label.getScore() * 100);
                message += label.getDescription().toUpperCase() + "\n";
            }
        } else
            message = "I found nothing interesting.";
        return message;
    }

    public void crossOut(){
        if(message.contains(treasureListFragment.t1.getText().toString()))
            treasureListFragment.t1.setPaintFlags(treasureListFragment.t1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        if(message.contains(treasureListFragment.t2.getText().toString()))
            treasureListFragment.t2.setPaintFlags(treasureListFragment.t2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        if(message.contains(treasureListFragment.t3.getText().toString()))
            treasureListFragment.t3.setPaintFlags(treasureListFragment.t3.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public boolean checkWin(){
        if (       ((treasureListFragment.t1.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)
                && ((treasureListFragment.t2.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)
                && ((treasureListFragment.t3.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)) {
            tries = 1;
            instruct.setText(R.string.won);
            challengeTitle.setText(R.string.congrats);
            btnList.setClickable(true);
            btnList.setBackgroundDrawable(getDrawable(R.drawable.reload));
            btnCamera.setText("");
            btnCamera.setClickable(false);
            happyFace.setVisibility(View.VISIBLE);
            background.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            treasureListFragment.t1.setTextColor(getResources().getColor(R.color.white));
            treasureListFragment.t2.setTextColor(getResources().getColor(R.color.white));
            treasureListFragment.t3.setTextColor(getResources().getColor(R.color.white));
            return true;
        }
        else if(   (((treasureListFragment.t1.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) && ((treasureListFragment.t2.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0))
                || (((treasureListFragment.t1.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) && ((treasureListFragment.t3.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0))
                || (((treasureListFragment.t2.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) && ((treasureListFragment.t3.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0))) {
            instruct.setText("One more treasure!\nRemaining Shots: " + tries + "");
            return false;
        }
        else if(   ((treasureListFragment.t1.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)
                || ((treasureListFragment.t2.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)
                || ((treasureListFragment.t3.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)) {
            instruct.setText("Two more treasures!\nRemaining Shots: " + tries + "");
            return false;

        }
        else {
            instruct.setText("No luck. Try harder!\nRemaining Shots: " + tries + "");
            return false;

        }
    }

    public void showResults(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView results = new TextView(this);
        results.setText(message);
        results.setGravity(Gravity.CENTER_HORIZONTAL);
        results.setTextSize(18);
        builder.setView(results)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
