package com.fleetshipdigitalboard.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.fleetshipdigitalboard.BuildConfig;
import com.fleetshipdigitalboard.R;
import com.fleetshipdigitalboard.interfaces.DeviceResponse;
import com.fleetshipdigitalboard.model.AddDevice;
import com.fleetshipdigitalboard.model.AddDeviceResponse;
import com.fleetshipdigitalboard.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressBar = findViewById(R.id.progress);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                insertDevice();
            }
        }, 1000);
    }

    private void insertDevice() {
        progressBar.setVisibility(View.VISIBLE);
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG)  // not applicable for release version
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
//http://18.190.0.112/fleet/Api/rest/
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(Constants.base_url +"Api/rest/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClientBuilder.build());
        Retrofit retrofit = builder.build();
        final String sharingCode = System.currentTimeMillis() + "";  // generating the sharing code
        DeviceResponse deviceResponse = retrofit.create(DeviceResponse.class);
        AddDevice addDevice = new AddDevice(Constants.api_secret_key,
                androidId,
                sharingCode);
        Call<AddDeviceResponse> call = deviceResponse.addDevice(addDevice);


        call.enqueue(new Callback<AddDeviceResponse>() {
            @Override
            public void onResponse(Call<AddDeviceResponse> call, Response<AddDeviceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String msg = response.body().getMessage();
                    if (msg.equals("not connected")) {
                        //save locally
                        SharedPreferences.Editor spe = getSharedPreferences("device", Context.MODE_PRIVATE).edit();
                        spe.clear();
                        spe.commit();
                        spe.putString("status", "yes");
                        spe.putString("device_id", response.body().getDevice_id());
                        spe.putString("sharing_code", sharingCode);
                        spe.commit();
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    } else if (msg.equals("Device Already Exists.")) {
                        SharedPreferences.Editor spe = getSharedPreferences("device", Context.MODE_PRIVATE).edit();
                        spe.clear();
                        spe.commit();
                        spe.putString("status", "yes");
                        spe.putString("device_id", response.body().getDevice_id());
                        spe.putString("sharing_code", sharingCode);
                        spe.commit();
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                }
            }

            @Override
            public void onFailure(Call<AddDeviceResponse> call, Throwable t) {
                //Check if device id available locally or not. If not then print error message otherwise navigate to MainActivity.
                SharedPreferences spf = getSharedPreferences("device", MODE_PRIVATE);
                String device_id = spf.getString("device_id", "no");
                if (device_id.equals("no")) {
                    Toast.makeText(SplashActivity.this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        });
    }
}
