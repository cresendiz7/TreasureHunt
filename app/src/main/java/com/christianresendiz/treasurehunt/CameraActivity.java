package com.christianresendiz.treasurehunt;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
import com.google.common.io.BaseEncoding;

public class CameraActivity extends AppCompatActivity {

    private static final String CLOUD_VISION_API_KEY = "AIzaSyCMX6GPdOFCaftyjkoE5d8GUGOyJBD1ezg";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final String TAG = CameraActivity.class.getSimpleName();
    public static final int CAMERA_IMAGE_REQUEST = 3;
    private Random rand = new Random();

    TreasureListFragment treasureListFragment;
    TextView instruct;
    TextView challengeTitle;
    TextView t1;
    TextView t2;
    TextView t3;
    TextView shots;
    TextView resetText;
    ImageView preview;
    ImageView emoji;
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
        t1 = treasureListFragment.t1;
        t2 = treasureListFragment.t2;
        t3 = treasureListFragment.t3;

        shots = (TextView) findViewById(R.id.shots);
        instruct = (TextView) findViewById(R.id.instruct);
        challengeTitle = (TextView) findViewById(R.id.newChallenge);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnList = (ImageButton) findViewById(R.id.newList);
        emoji = (ImageView) findViewById(R.id.happy);
        background = (RelativeLayout) findViewById(R.id.background);
        preview = (ImageView) findViewById(R.id.preview);
        resetText = (TextView) findViewById(R.id.resetText);

        r = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r.setDuration((long) 500);
        r.setRepeatCount(0);

        r2 = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r2.setInterpolator(new LinearInterpolator());
        r2.setDuration((long) 500);
        r2.setRepeatCount(Animation.INFINITE);

        treasureListFragment.getFortunes(difficulty);
        treasureListFragment.colorText();
        instruct.setText(R.string.instructGame);
        resetText.setText(R.string.reset);
        shots.setText("Remaining Shots: " + tries + "");
        btnCamera.setClickable(true);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnList.setBackgroundDrawable(getDrawable(R.drawable.camera));
                challengeTitle.setText(R.string.happy_hunting);
                btnList.setClickable(false);
                resetText.setText("");
                startCamera();
            }
        });

        btnCamera.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                btnList.performClick();
                return true;
            }
        });
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emoji.setImageResource(android.R.color.transparent);
                challengeTitle.setText(R.string.new_challenge);
                background.setBackgroundColor(getResources().getColor(R.color.background));
                tries = 5;
                btnList.setClickable(true);
                treasureListFragment.getFortunes(difficulty);
                treasureListFragment.colorText();
                resetText.setText(R.string.reset);
                preview.setImageBitmap(null);
                btnList.setBackgroundDrawable(getDrawable(R.drawable.reload));
                instruct.setText(R.string.instructGame);
                shots.setText("Remaining Shots: " + tries + "");
                treasureListFragment.resetFlags();
                btnCamera.setClickable(true);
                btnCamera.setText(R.string.begin_hunt);
                btnList.startAnimation(r);
            }
        });
    }

    public void startCamera(){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
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

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

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

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Bitmap darkenBitMap(Bitmap bm) {
        Canvas canvas = new Canvas(bm);
        Paint p = new Paint(Color.RED);
        ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);    // darken
        p.setColorFilter(filter);
        canvas.drawBitmap(bm, new Matrix(), p);
        return bm;
    }

    public void uploadImage(Uri uri) {
            try {
                Bitmap bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri), 1200);
                bitmap = rotateImage(bitmap, 90);
                bitmap = darkenBitMap(bitmap);
                preview.setImageBitmap(bitmap);
                callCloudVision(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public static String getSignature(@NonNull PackageManager pm, @NonNull String packageName) {
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            if (packageInfo == null
                    || packageInfo.signatures == null
                    || packageInfo.signatures.length == 0
                    || packageInfo.signatures[0] == null) {
                return null;
            }
            return signatureDigest(packageInfo.signatures[0]);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static String signatureDigest(Signature sig) {
        byte[] signature = sig.toByteArray();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] digest = md.digest(signature);
            return BaseEncoding.base16().lowerCase().encode(digest);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private void onStartCall(){
        btnList.startAnimation(r2);
        String[] scanningStrings = getResources().getStringArray(R.array.scanning);
        String randString = scanningStrings[rand.nextInt(scanningStrings.length)];
        instruct.setText(randString);
        shots.setText("");
        btnList.setBackgroundDrawable(getDrawable(R.drawable.reload));
        challengeTitle.setText(R.string.scanning);
        btnCamera.setText("");
        btnCamera.setClickable(false);
        btnCamera.setLongClickable(false);
    }

    private void onFinishCall(){
        showResults();
        r2.cancel();
        btnList.setBackgroundDrawable(getDrawable(R.drawable.checkmark));
        challengeTitle.setText(R.string.finished_scanning);
        tries--;
        crossOut();
        if(!checkWin()) {
            if (tries == 0) {
                instruct.setText(R.string.lose);
                btnCamera.setText("");
                btnCamera.setLongClickable(false);
                shots.setText("");
                btnList.setClickable(true);
                btnList.setBackgroundDrawable(getDrawable(R.drawable.reload));
                preview.setImageBitmap(null);
                background.setBackgroundColor(getResources().getColor(R.color.lose));
                t1.setTextColor(getResources().getColor(R.color.white));
                t2.setTextColor(getResources().getColor(R.color.white));
                t3.setTextColor(getResources().getColor(R.color.white));
                emoji.setImageResource(R.drawable.sad);
                challengeTitle.setText(R.string.new_challenge);
            } else {
                btnCamera.setClickable(true);
                btnCamera.setLongClickable(true);
                btnCamera.setText(R.string.continue_hunt);
            }
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        onStartCall();
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

                                    String sig = getSignature(getPackageManager(), packageName);

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
                onFinishCall();
            }
        }.execute();
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        message = "\nFound:\n\n";
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format(Locale.US, "%.2f%%:  ", label.getScore() * 100);
                message += label.getDescription().toUpperCase() + "\n";
            }
        } else
            message = "\nAbsolutely nothing.";
        return message;
    }

    public void crossOut(){
        if(message.contains(t1.getText().toString()))
            t1.setPaintFlags(t1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        if(message.contains(t2.getText().toString()))
            t2.setPaintFlags(t2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        if(message.contains(t3.getText().toString()))
            t3.setPaintFlags(t3.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public boolean checkWin(){
        if (       ((t1.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)
                && ((t2.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)
                && ((t3.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)) {
            tries = 1;
            instruct.setText(R.string.won);
            challengeTitle.setText(R.string.congrats);
            shots.setText("");
            btnList.setClickable(true);
            btnList.setBackgroundDrawable(getDrawable(R.drawable.reload));
            btnCamera.setText("");
            preview.setImageBitmap(null);
            emoji.setImageResource(R.drawable.happy);
            background.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            t1.setTextColor(getResources().getColor(R.color.white));
            t2.setTextColor(getResources().getColor(R.color.white));
            t3.setTextColor(getResources().getColor(R.color.white));
            return true;
        }
        else if(   (((t1.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) && ((t2.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0))
                || (((t1.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) && ((t3.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0))
                || (((t2.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) && ((t3.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0))) {
            instruct.setText(R.string.oneMore);
            shots.setText("Remaining Shots: " + tries + "");
            return false;
        }
        else if(   ((t1.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)
                || ((t2.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)
                || ((t3.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)) {
            instruct.setText(R.string.twoMore);
            shots.setText("Remaining Shots: " + tries + "");
            return false;

        }
        else {
            instruct.setText(R.string.try_again);
            shots.setText("Remaining Shots: " + tries + "");
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
