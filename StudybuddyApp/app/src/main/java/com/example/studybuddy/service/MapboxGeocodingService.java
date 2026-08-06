package com.example.studybuddy.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MapboxGeocodingService
{
    @GET("geocoding/v5/mapbox.places/{searchText}.json")
    Call<MapboxGeocodingResponse> forwardGeocode(
            @Path("searchText") String searchText,
            @Query("access_token") String accessToken,
            @Query("limit") int limit,
            @Query("country") String country
    );
}
