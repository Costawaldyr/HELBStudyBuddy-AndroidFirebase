package com.example.studybuddy.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mapbox.geojson.Point;

/**
 * Holds the camera state of ChatMapActivity so the map keeps its centre
 * and zoom level across configuration changes (rotation, theme switch,
 * multi-window resize…).
 */
public class ChatMapViewModel extends ViewModel
{
    private static final double DEFAULT_ZOOM = 12.0;

    private final MutableLiveData<Point> cameraCenter = new MutableLiveData<>();
    private final MutableLiveData<Double> zoomLevel = new MutableLiveData<>(DEFAULT_ZOOM);

    public LiveData<Point> getCameraCenter()
    {
        return cameraCenter;
    }

    public void setCameraCenter(Point point)
    {
        cameraCenter.setValue(point);
    }

    public LiveData<Double> getZoomLevel()
    {
        return zoomLevel;
    }

    public void setZoomLevel(Double zoom)
    {
        zoomLevel.setValue(zoom);
    }
}
