package com.example.studybuddy.service;

import static com.example.studybuddy.utils.MyConstants.*;

import android.content.Context;
import android.util.Log;

import com.example.studybuddy.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class MapboxGeocodingRepository
{
    private final MapboxGeocodingService service;
    private final String accessToken;

    public interface GeocodingCallback
    {
        void onSuccess(double latitude, double longitude, String placeName);
        void onError(String errorMessage);
    }

    public MapboxGeocodingRepository(Context context)
    {
        this.accessToken = context.getString(R.string.mapbox_access_token);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MAPBOX_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.service = retrofit.create(MapboxGeocodingService.class);
    }

    public void geocode(String address, GeocodingCallback callback)
    {
        if (callback == null)
        {
            return;
        }
        if (address == null || address.trim().isEmpty())
        {
            callback.onError(EMPTY_ADDRESS_ERROR);
            return;
        }

        service.forwardGeocode(address.trim(), accessToken, ONE, MAPBOX_COUNTRY_FILTER)
                .enqueue(new Callback<MapboxGeocodingResponse>()
                {
                    @Override
                    public void onResponse(Call<MapboxGeocodingResponse> call, Response<MapboxGeocodingResponse> response)
                    {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<MapboxGeocodingResponse> call, Throwable t)
                    {
                        Log.e(MAPBOX_TAG, "Network error while geocoding", t);
                        callback.onError(t.getMessage() != null ? t.getMessage() : NETWORK_ERROR);
                    }
                });
    }

    private void handleResponse(Response<MapboxGeocodingResponse> response, GeocodingCallback callback)
    {
        if (!response.isSuccessful() || response.body() == null)
        {
            String message = String.format(HTTP_ERROR_TEMPLATE, response.code());
            Log.w(MAPBOX_TAG, message);
            callback.onError(message);
            return;
        }

        MapboxGeocodingResponse.Feature best = response.body().getBestMatch();
        if (best == null || !best.hasCoordinates())
        {
            callback.onError(NO_MATCH_ERROR);
            return;
        }

        callback.onSuccess(best.getLatitude(), best.getLongitude(), best.getPlaceName());
    }
}
