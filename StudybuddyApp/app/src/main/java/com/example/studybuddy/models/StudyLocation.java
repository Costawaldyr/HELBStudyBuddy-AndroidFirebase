package com.example.studybuddy.models;

public class StudyLocation
{
    private final String name;
    private final String address;
    private final double lat;
    private final double lng;

    public StudyLocation(String name, String address, double lat, double lng)
    {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName()
    {
        return name;
    }

    public String getAddress()
    {
        return address;
    }

    public double getLat()
    {
        return lat;
    }

    public double getLng()
    {
        return lng;
    }
}
