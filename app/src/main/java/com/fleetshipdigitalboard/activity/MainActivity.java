package com.fleetshipdigitalboard.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fleetshipdigitalboard.BuildConfig;
import com.fleetshipdigitalboard.R;
import com.fleetshipdigitalboard.interfaces.DeviceResponse;
import com.fleetshipdigitalboard.model.ConectionDetector;
import com.fleetshipdigitalboard.model.ContentStatusRequest;
import com.fleetshipdigitalboard.model.ContentStatusResponse;
import com.fleetshipdigitalboard.model.ImageResponse;
import com.fleetshipdigitalboard.model.SliderDurationRequest;
import com.fleetshipdigitalboard.model.SliderDurationResponse;
import com.fleetshipdigitalboard.model.SliderResponse;
import com.fleetshipdigitalboard.model.StatusModel;
import com.fleetshipdigitalboard.model.StatusRequest;
import com.fleetshipdigitalboard.model.UpdateStatusRequest;
import com.fleetshipdigitalboard.model.UpdateStatusResponse;
import com.fleetshipdigitalboard.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 100;
    private static final String TAG = "Console Message";
    private ArrayList<SliderResponse> f;
    // private ViewFlipper viewFlipper;
    private ViewFlipper viewFlipper;
    private String deviceId;
    private int globalI = 0;
    private boolean fromserver, fromserver_special;
    private int click = 0;
    private Button pauseBtn;
    private int sliderDuration = 10000;
    private int flag = 0;
    private ProgressDialog mProgressDialog;
    private ConectionDetector conectionDetector;
    private AlertDialog alertDialog;
    private ArrayList<String> fileTokeepSave;
    private Handler handler;
    private Runnable runnable;
    private int progress;
    private int updateFlag = 0;
    private int errorCount = 0;
    private TextView position;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        position = findViewById(R.id.position);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  //Keep screen on
        viewFlipper = findViewById(R.id.viewfilpper);
        setAnimationToViewFlipper();  // Custom Fade in  and Fade out animation
        //prepare progress dialog to show while downloading the contents
        viewFlipper.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                position.setText((viewFlipper.getDisplayedChild()+1) + "");
            }
        });
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle("");
        mProgressDialog.setMessage("Downloading..");

        pauseBtn = findViewById(R.id.pause); // Play/pause button
        pauseBtn.setVisibility(View.GONE);  // By default visibilty is Gone. Only visible if image is shown

        // define handler to check content status in every 10 seconds
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                checkContentStatus();
            }
        };
        pauseBtn.setBackgroundColor(getResources().getColor(R.color.trans));   // set background to transparent or tranlucent
        SharedPreferences spf = getSharedPreferences("device", MODE_PRIVATE);
        deviceId = spf.getString("device_id", "not found");
        sliderDuration = spf.getInt("slider_duration", 0);
        Log.d(TAG, "DEVICE ID " + deviceId);
        String sharingCode = spf.getString("sharing_code_status", "no");
        Log.d(TAG, "Sharing Code " + sharingCode);
        conectionDetector = new ConectionDetector(getApplicationContext());
        if (conectionDetector.isConnection()) {
            if (sliderDuration == 0) {
                getSliderDuration();
            } else {
                viewFlipper.setFlipInterval(sliderDuration);
            }
            if (sharingCode.equals("no")) {
                checkStatus();
            } else {
                checkContentStatus();
            }
        } else {
            fromserver = false;
            flag = 1;
            checkPermission();
            checkStatusContinously();
            if (sliderDuration == 0)
                sliderDuration = 10000;
        }
        Log.d(TAG, "Slider Duration " + sliderDuration);
    }

    private void setAnimationToViewFlipper() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1500); //time in milliseconds
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(500);
        fadeOut.setDuration(1000); //time in milliseconds
        viewFlipper.setInAnimation(fadeIn);
        viewFlipper.setOutAnimation(fadeOut);
    }

    private void checkStatusContinously() {
        handler.postDelayed(runnable, 10000);  // handler and runnable is defined in oncreate ethod.
    }

    private void checkContentStatus() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG)
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
//http://<domainname>/fleet/Api/rest/
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(Constants.base_url+"Api/rest/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClientBuilder.build());
        Retrofit retrofit = builder.build();
        ContentStatusRequest request = new ContentStatusRequest(Constants.api_secret_key, deviceId);
        DeviceResponse deviceResponse = retrofit.create(DeviceResponse.class);
        Call<ContentStatusResponse> call = deviceResponse.content(request);
        call.enqueue(new Callback<ContentStatusResponse>() {
            @Override
            public void onResponse(Call<ContentStatusResponse> call, Response<ContentStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, response.body().getDevice_id());
                    Log.d(TAG, response.body().getMessage());
                    Log.d(TAG, response.body().getStatus());
                    if (response.body().getMessage().equals("No content available")) {  // nothing uploaded
                        fromserver = false;           // boolean to tell whether to hit the server for content or not
                        fromserver_special = false;   // don't fetch even if no content is available locally
                        checkPermission();
                    } else if (response.body().getMessage().equals("content available.") && response.body().getStatus().equalsIgnoreCase("true")) {
                        getSliderDuration();
                        handler.removeCallbacks(runnable);  // stop checking content status untill the download is completed.
                        updateFlag = 1;
                        fromserver = true;                  // fetch from the server
                        fromserver_special = false;         // don't fetch even if no content is available locally
                        flag = 1;
                        checkPermission();
                    } else if (response.body().getMessage().equals("content available.") && response.body().getStatus().equalsIgnoreCase("false")) {
                        fromserver = false;              // don't fetch from server
                        fromserver_special = true;
                        updateFlag = 0;// fetch if no content is available locally
                        if (flag == 0) {
                            checkPermission();
                            flag = 1;
                        }
                    }
                } else {
                    Log.d(TAG, "UNSUCCESSFULL");
                }
            }

            @Override
            public void onFailure(Call<ContentStatusResponse> call, Throwable t) {
                if (errorCount == 0) {
                    errorCount = 1;
                    fromserver = false;
                    flag = 1;
                    checkPermission();
                }
            }
        });
        Log.d(TAG, "update Flag = " + updateFlag);
        if (updateFlag == 0) {
            checkStatusContinously();
        }
        //
    }


    private void checkStatus() {    // Method to check sharing code status
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG)
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        //http://18.190.0.112/fleet/Api/rest/
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(Constants.base_url+"Api/rest/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClientBuilder.build());
        Retrofit retrofit = builder.build();
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        StatusRequest request = new StatusRequest(Constants.api_secret_key, androidId);
        DeviceResponse deviceResponse = retrofit.create(DeviceResponse.class);
        Call<StatusModel> call = deviceResponse.check(request);
        call.enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getConnection_status().equalsIgnoreCase("true")) {
                        SharedPreferences.Editor spe = getSharedPreferences("device", MODE_PRIVATE).edit();
                        spe.putString("sharing_code_status", "done");
                        spe.putString("device_id", response.body().getDevice_id());
                        spe.commit();
                        checkContentStatus();
                    } else {
                        finish();
                        Log.d(TAG, "Sharing Code " + response.body().getSharing_code());
                        Intent i = new Intent(getApplicationContext(), SharingCodeActivity.class);
                        i.putExtra("sharing_code", response.body().getSharing_code());
                        startActivity(i);
                    }
                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {

            }
        });
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            }
        } else {
            Log.d(TAG, "FROMSERVER IN CHECK PERMISSION " + fromserver);
            if (fromserver)
                getFromserver();
            else
                getFromSdcard(null);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "FROMSERVER IN onRequestPermissionsResult " + fromserver);
                    if (fromserver)
                        getFromserver();
                    else
                        getFromSdcard(null);
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void getFromserver() {
        pauseBtn.setVisibility(View.GONE);
        viewFlipper.removeAllViews();


        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG)
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        //http://18.190.0.112/fleet/public/site/uploads/content/
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(Constants.asset_url+"public/site/uploads/content/" + deviceId + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClientBuilder.build());
        Retrofit retrofit = builder.build();
        DeviceResponse deviceResponse = retrofit.create(DeviceResponse.class);
        Call<List<ImageResponse>> call = deviceResponse.getImages(deviceId + ".json");
        call.enqueue(new Callback<List<ImageResponse>>() {
            @Override
            public void onResponse(Call<List<ImageResponse>> call, Response<List<ImageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.code() != 404) {
                        fromserver_special = false;
                        ArrayList<String> list = checkForUseFullFiles(response.body());
                        downloadFiles(list);
                    } else {
                        showAlertMessege("No content available For this device id");
                    }
                } else {
                    showAlertMessege("No content available For this device id");
                }

            }

            @Override
            public void onFailure(Call<List<ImageResponse>> call, Throwable t) {

            }
        });
    }

    private ArrayList<String> checkForUseFullFiles(final List<ImageResponse> body) {
        ArrayList<String> downloadListFiles = new ArrayList<>();
        // File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Fleetshipdigitalboard");
        File folder = new File(getExternalFilesDir("Fleetshipdigitalboard") + "");
        fileTokeepSave = new ArrayList<>();
        for (int i = 0; i < body.size(); i++) {
            File f = new File(folder, body.get(i).getFilename());
            if (f.exists())
                fileTokeepSave.add(body.get(i).getFilename());
            else
                downloadListFiles.add(body.get(i).getFilename());

        }
        DeleteUseLessFile();
        return downloadListFiles;
    }

    private void DeleteUseLessFile() {
        // File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Fleetshipdigitalboard");
        File folder = new File(getExternalFilesDir("Fleetshipdigitalboard") + "");
        File[] files = folder.listFiles();
        for (File file : files) {
            if (!fileTokeepSave.contains(file.getName())) {
                if (file.delete())
                    Log.d(TAG, "FILE DELETED " + file.getName());
            }

        }
    }

    private void downloadFiles(final ArrayList<String> body) {
        if (body != null && body.size() > 0) {
            mProgressDialog.show();
            Log.d(TAG, body.size() + " NEW FILES FOUND");
            Toast.makeText(this, body.size() + " NEW FILES FOUND", Toast.LENGTH_SHORT).show();
            for (int i = 0; i < body.size(); i++) {
                final String filename = body.get(i);
                OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                        .connectTimeout(300, TimeUnit.SECONDS)
                        .readTimeout(3600, TimeUnit.SECONDS)
                        .writeTimeout(3600, TimeUnit.SECONDS)
                        .build();
//http://18.190.0.112/fleet/public/site/uploads/content/
                Retrofit.Builder builder = new Retrofit.Builder().baseUrl(Constants.asset_url+"public/site/uploads/content/" + deviceId + "/")
                        .client(okHttpClient);

                Retrofit retrofit = builder.build();
                DeviceResponse deviceResponse = retrofit.create(DeviceResponse.class);
                Call<ResponseBody> call = deviceResponse.downloadFileWithDynamicUrlAsync(filename);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "server contacted and has file");

                            new AsyncTask<Void, Integer, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    boolean writtenToDisk = writeResponseBodyToDisk(response.body(), filename);
                                    //Log.d(TAG, "file download was a success? " + writtenToDisk);
                                    if (writtenToDisk) {
                                        Log.d(TAG, "file download was a success? " + writtenToDisk);
                                    } else {

                                        Log.d(TAG, "file download was a success? " + writtenToDisk);
                                    }
                                    publishProgress(progress);
                                    return null;
                                }

                                @Override
                                protected void onProgressUpdate(Integer... values) {
                                    super.onProgressUpdate(values);
                                    mProgressDialog.setProgress(0);
                                    if (!mProgressDialog.isShowing())
                                        mProgressDialog.show();
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    super.onPostExecute(aVoid);
                                    globalI++;
                                    // Toast.makeText(MainActivity.this, (globalI+1) + "File downloaded", Toast.LENGTH_SHORT).show();
                                    // mProgressDialog.show();
                                    Log.d(TAG, "ON POST EXECUTE CALLED " + globalI);
                                    Log.d(TAG, "Body Size " + body.size());
                                    if (globalI == body.size()) {
                                        Log.d(TAG, "Body Size Equal");
                                        globalI = 0;
                                        mProgressDialog.dismiss();
                                        getFromSdcard(null);
                                    } else {
                                        // progress = 0;
                                        // mProgressDialog.setProgress(progress);
                                        // mProgressDialog.show();
                                    }
                                }
                            }.execute();
                        } else {
                            Log.d(TAG, "server contact failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if (t instanceof IOException) {
                            Log.d(TAG, "Content status updated 111");
                            globalI = 0;
                        }
                    }
                });
            }
        } else {
            Log.d(TAG, "No New File");
            getFromSdcard(null);
        }

    }

    private void renameFolder() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                File f = new File(Environment.getExternalStorageDirectory() + File.separator + "Fleetshipdigitalboard");
                if (f.exists()) {
                    cleanDirectory(f);
                    File newDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Fleetshipdigitalboard");
                    File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "tmp_fleetshipdigitalboard");
                    newDir.mkdir();
                    if (dir.renameTo(newDir)) {
                        cleanDirectory(dir);
                        getFromSdcard(null);
                    }
                } else {
                    Log.d(TAG, "File Not Found");
                    File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "tmp_fleetshipdigitalboard");
                    File newName = new File(Environment.getExternalStorageDirectory() + File.separator + "Fleetshipdigitalboard");
                    newName.mkdir();
                    if (dir.renameTo(newName)) {
                        cleanDirectory(dir);
                        getFromSdcard(null);
                    } else {
                        Log.d(TAG, "Could not Rename folder");
                    }
                }
            }
        });


    }

    private void cleanDirectory(File directory) {
        if (directory.isDirectory()) {
            String[] children = directory.list();
            for (int i = 0; i < children.length; i++) {
                new File(directory, children[i]).delete();
                Log.d(TAG, "Deleted File " + i);
            }
        }
    }

    private void updateContentStatus(String status) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG)
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        //http://18.190.0.112/fleet/Api/rest/
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(Constants.base_url+"Api/rest/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClientBuilder.build());
        Retrofit retrofit = builder.build();
        UpdateStatusRequest request = new UpdateStatusRequest(Constants.api_secret_key, deviceId, status);
        DeviceResponse deviceResponse = retrofit.create(DeviceResponse.class);
        Call<UpdateStatusResponse> call = deviceResponse.update(request);
        call.enqueue(new Callback<UpdateStatusResponse>() {
            @Override
            public void onResponse(Call<UpdateStatusResponse> call, Response<UpdateStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //Toast.makeText(MainActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    checkStatusContinously();
                }
            }

            @Override
            public void onFailure(Call<UpdateStatusResponse> call, Throwable t) {

            }
        });
    }

    private void setSlider() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG)
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        //http://18.190.0.112/fleet/public/site/uploads/content/
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(Constants.asset_url+"public/site/uploads/content/" + deviceId + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClientBuilder.build());
        Retrofit retrofit = builder.build();
        DeviceResponse deviceResponse = retrofit.create(DeviceResponse.class);
        Call<List<SliderResponse>> call = deviceResponse.getSlider(deviceId + "_slider.json");
        call.enqueue(new Callback<List<SliderResponse>>() {
            @Override
            public void onResponse(Call<List<SliderResponse>> call, Response<List<SliderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    getFromSdcard(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<SliderResponse>> call, Throwable t) {

            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String fileName) {
        try {
            // todo change the file location/name according to your needs
            // File f = new File(Environment.getExternalStorageDirectory() + File.separator + "Fleetshipdigitalboard");
            File f = new File(getExternalFilesDir("Fleetshipdigitalboard") + "");
            if (!f.exists()) {
                f.mkdir();
            }
            File currentlyDownloadingFile = new File(f + File.separator + fileName);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(currentlyDownloadingFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                    progress = (int) (fileSizeDownloaded * 100 / fileSize);
                    mProgressDialog.setProgress(progress);

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();
                return true;
            } catch (IOException e) {
                currentlyDownloadingFile.delete();
                checkStatusContinously();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void getFromSdcard(List<SliderResponse> body) {

        f = new ArrayList<>();
        // File file = new File(android.os.Environment.getExternalStorageDirectory(), "Fleetshipdigitalboard");
        File file = new File(getExternalFilesDir("Fleetshipdigitalboard") + "");
        if (!file.exists())
            file.mkdir();     // Make folder if not exists
        if (file.isDirectory()) {
            File[] listFile = file.listFiles();
            if (listFile.length == 0) {
                if (fromserver_special) {      // if no image avalable locally
                    getFromserver();           // download from server
                } else {
                    showAlertMessege("No Data available to display.");
                }

                pauseBtn.setVisibility(View.GONE);     //hide play/pause button
                return;
            }
            for (int i = 0; i < listFile.length; i++) {
                Log.d(TAG, "File Added " + i);
                if (getExt(listFile[i].getAbsolutePath()).equals("jpg") || getExt(listFile[i].getAbsolutePath()).equals("jpeg") || getExt(listFile[i].getAbsolutePath()).equals("png"))
                    f.add(new SliderResponse(listFile[i].getAbsolutePath(), "image"));
                else if (getExt(listFile[i].getAbsolutePath()).equals("mp4") || getExt(listFile[i].getAbsolutePath()).equals("mpeg"))
                    f.add(new SliderResponse(listFile[i].getAbsolutePath(), "video"));
            }

            setAdapter();   // set slider after download
        }
    }


    private int extractNumber(String name) {
        int i = 0;
        try {
            int s = name.lastIndexOf('/') + 1;
            int e = name.lastIndexOf('.');
            String number = name.substring(s, e);
            i = Integer.parseInt(number);
        } catch (Exception e) {
            i = 0; // if filename does not match the format
            // then default to 0
        }
        return i;
    }


    private void setAdapter() {
        Log.d(TAG, "Updating Slider");
        Log.d(TAG, "Slider Size " + viewFlipper.getChildCount());
        viewFlipper.removeAllViews();
        Log.d(TAG, "Setting Slider Afetr network call");
        Toast.makeText(this, f.size() + " Files Available", Toast.LENGTH_SHORT).show();
        pauseBtn.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        Collections.sort(f, new Comparator<SliderResponse>() {
            @Override
            public int compare(SliderResponse object1, SliderResponse object2) {
                int n1 = extractNumber(object1.getFilename());
                int n2 = extractNumber(object2.getFilename());
                return n1 - n2;

            }
        });
        for (int i = 0; i < f.size(); i++) {
            final int finalI = i;
            String ext = getExt(f.get(i).getFilename());
            if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png")) {
                if (imageView != null) {
                    imageView = null;
                }
                imageView = new ImageView(this);
                imageView.setLayoutParams(params);
                imageView.setKeepScreenOn(true);
                final File imageFile = new File(f.get(i).getFilename());
                Glide.with(this).asBitmap().load(imageFile).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                       // pauseBtn.setText(e.getMessage());
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(imageView);
                viewFlipper.addView(imageView);
                imageView.setKeepScreenOn(true);
                viewFlipper.setFlipInterval(sliderDuration);
                viewFlipper.startFlipping();
            } else if (ext.equals("mp4")) {
                final VideoView videoView = new VideoView(this);
                videoView.setLayoutParams(params);
                final File videoFile = new File(f.get(i).getFilename());
                videoView.setKeepScreenOn(true);
                videoView.setVideoURI(Uri.fromFile(videoFile));
                videoView.requestFocus();
                viewFlipper.addView(videoView);
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mp != null) {
                            mp.stop();
                            pauseBtn.setVisibility(View.VISIBLE);
                        }
                        viewFlipper.showNext();
                        viewFlipper.startFlipping();

                    }
                });
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {

                        if (mp != null) {
                            mp.start();
                            pauseBtn.setVisibility(View.GONE);
                            viewFlipper.stopFlipping();
                        }


                    }
                });

                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        videoFile.delete();
                        viewFlipper.showNext();
                        Log.d(TAG, "Moving Next");
                        return true;
                    }
                });

            }
        }
        Log.d(TAG, "Slider Size " + viewFlipper.getChildCount());
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        if (updateFlag == 1) {
            Log.d(TAG, "Content Status updated");
            updateContentStatus("false");
            updateFlag = 0;
        }

    }

    private String getExt(String filePath) {
        int strLength = filePath.lastIndexOf(".");
        if (strLength > 0)
            return filePath.substring(strLength + 1).toLowerCase();
        return null;
    }

    private void showAlertMessege(String msg) {
        alertDialog = new AlertDialog.Builder(
                MainActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle("Info");

        // Setting Dialog Message
        alertDialog.setMessage(msg);


        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //finish();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void pause(View view) {
        if (click % 2 == 0) {
            viewFlipper.stopFlipping();
            pauseBtn.setText("play");
            click++;
        } else {
            viewFlipper.showNext();
            viewFlipper.startFlipping();
            pauseBtn.setText("pause");
            click++;
        }
    }

    private void getSliderDuration() {
        Log.d(TAG, "Getting Slider Duration");
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG)
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        //http://18.190.0.112/fleet/Api/rest/
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(Constants.base_url+"Api/rest/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClientBuilder.build());
        Retrofit retrofit = builder.build();
        SliderDurationRequest request = new SliderDurationRequest(Constants.api_secret_key);
        DeviceResponse deviceResponse = retrofit.create(DeviceResponse.class);
        Call<SliderDurationResponse> call = deviceResponse.duration(request);
        call.enqueue(new Callback<SliderDurationResponse>() {
            @Override
            public void onResponse(Call<SliderDurationResponse> call, Response<SliderDurationResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.message().equals("404")) {
                    sliderDuration = Integer.parseInt(response.body().getSlider_duration()) * 1000;
                    //viewFlipper.setFlipInterval(sliderDuration);
                    Log.d(TAG, "Slider Duration response" + sliderDuration);
                    SharedPreferences.Editor spe = getSharedPreferences("device", MODE_PRIVATE).edit();
                    spe.putInt("slider_duration", sliderDuration);
                    spe.commit();
                }
            }

            @Override
            public void onFailure(Call<SliderDurationResponse> call, Throwable t) {

            }
        });

    }
}