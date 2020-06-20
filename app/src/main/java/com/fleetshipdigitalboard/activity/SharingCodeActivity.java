package com.fleetshipdigitalboard.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fleetshipdigitalboard.BuildConfig;
import com.fleetshipdigitalboard.R;
import com.fleetshipdigitalboard.interfaces.DeviceResponse;
import com.fleetshipdigitalboard.model.StatusModel;
import com.fleetshipdigitalboard.model.StatusRequest;
import com.fleetshipdigitalboard.utils.Constants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SharingCodeActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing_code);
        textView = findViewById(R.id.code);
        try {
            String code = getIntent().getStringExtra("sharing_code");
            textView.setText(code);
        } catch (NullPointerException ex) {

        }
    }

    // on Button click this method is called to check whether the sharing code is verified or not
    // if verified then will navigate to Main Activity

    public void checkStatus(View v) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Checking Status");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG)
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
//http://18.190.0.112/fleet/Api/rest/
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(Constants.base_url+"staging/Api/rest/")
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
                        SharedPreferences.Editor spe = getSharedPreferences("decvice", MODE_PRIVATE).edit();
                        spe.putString("sharing_code_status", "done");
                        spe.commit();
                        dialog.dismiss();
                        Toast.makeText(SharingCodeActivity.this, "Sharing Code verified Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    } else if (response.body().getConnection_status().equalsIgnoreCase("false")) {
                        Toast.makeText(SharingCodeActivity.this, "Please verify your sharing code", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {

            }
        });
    }
}
