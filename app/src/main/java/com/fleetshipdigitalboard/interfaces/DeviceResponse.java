package com.fleetshipdigitalboard.interfaces;

import android.content.SharedPreferences;

import com.fleetshipdigitalboard.model.AddDevice;
import com.fleetshipdigitalboard.model.AddDeviceResponse;
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

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;



public interface DeviceResponse {

    @POST("addDevice")
    Call<AddDeviceResponse> addDevice(@Body AddDevice device);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl);

    @GET
    Call<List<ImageResponse>> getImages(@Url String url);

    @GET
    Call<List<SliderResponse>> getSlider(@Url String url);

    @POST("getsharingcode")
    Call<StatusModel> check(@Body StatusRequest request);

    @POST("check_content_status")
    Call<ContentStatusResponse> content(@Body ContentStatusRequest request);

    @POST("update_content_status")
    Call<UpdateStatusResponse> update(@Body UpdateStatusRequest request);

    @POST("settings")
    Call<SliderDurationResponse> duration(@Body SliderDurationRequest request);
}
