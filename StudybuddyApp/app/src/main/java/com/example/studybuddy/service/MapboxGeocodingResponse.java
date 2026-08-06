package com.example.studybuddy.service;

import static com.example.studybuddy.utils.MyConstants.*;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class MapboxGeocodingResponse
{
    @SerializedName("features")
    private List<Feature> features;

    public List<Feature> getFeatures()
    {
        return features != null ? features : Collections.emptyList();
    }

    public Feature getBestMatch()
    {
        List<Feature> all = getFeatures();
        return all.isEmpty() ? null : all.get(ZERO);
    }

    public static class Feature
    {
        @SerializedName("place_name")
        private String placeName;

        @SerializedName("center")
        private List<Double> center;

        public String getPlaceName()
        {
            return placeName;
        }

        public Double getLongitude()
        {
            return hasUsableCenter() ? center.get(ZERO) : null;
        }

        public Double getLatitude()
        {
            return hasUsableCenter() ? center.get(ONE) : null;
        }

        public boolean hasCoordinates()
        {
            return getLongitude() != null && getLatitude() != null;
        }

        private boolean hasUsableCenter()
        {
            return center != null && center.size() >= TWO;
        }
    }
}
