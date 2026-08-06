package com.example.studybuddy.service;

import retrofit2.Call;
import retrofit2.http.GET;

public interface QuoteApiService
{
    @GET("api/random")
    Call<QuoteResponse[]> getRandomQuote();
}
