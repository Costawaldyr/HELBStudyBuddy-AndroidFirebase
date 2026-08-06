package com.example.studybuddy.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.studybuddy.R;
import com.example.studybuddy.data.StudyLocationData;
import com.example.studybuddy.data.local.AppDatabase;
import com.example.studybuddy.data.local.StudySpotEntity;
import com.example.studybuddy.models.StudyLocation;
import com.example.studybuddy.utils.MapNavigation;
import com.example.studybuddy.utils.MyConstants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * MapFragment — Shows fixed study spots as blue markers, and other students
 * as orange markers based on the "studyLocation" field in their Firestore profile.
 * No real-time GPS: just the study place the student set in their profile.
 *
 * AI-assisted (Claude) — reviewed by Waldyr Costa Dos Santos Lima
 */
public class MapFragment extends Fragment
{
    private static final String TAG = "MapFragment";

    private static final double RANDOM_CENTER_OFFSET = 0.5;
    private static final double STUDY_SPOT_TEXT_SIZE = 13.0;
    private static final double STUDY_SPOT_TEXT_OFFSET_Y = 0.8;
    private static final double STUDENT_TEXT_OFFSET_Y = -1.0;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;

    private View panelSpotInfo;
    private TextView tvSpotName, tvSpotAddress;
    private MaterialButton btnStudyHere;
    private StudyLocation selectedSpot;

    private FirebaseFirestore db;
    private String currentUid;
    private PointAnnotationManager studySpotAnnotationManager;
    private PointAnnotationManager studentAnnotationManager;
    private AppDatabase localDb;

    private Double savedLatitude;
    private Double savedLongitude;
    private Double savedZoom;

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted ->
                    {
                        if (isGranted)
                        {
                            enableUserLocationOnMap();
                        }
                        else
                        {
                            Toast.makeText(requireContext(), "Location permission denied. Map features may be limited.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(root, savedInstanceState);

        boolean hasSavedState = (savedInstanceState != null);
        if (hasSavedState)
        {
            if (savedInstanceState.containsKey(MyConstants.KEY_CAMERA_LAT))
            {
                savedLatitude = savedInstanceState.getDouble(MyConstants.KEY_CAMERA_LAT);
                savedLongitude = savedInstanceState.getDouble(MyConstants.KEY_CAMERA_LON);
            }
            if (savedInstanceState.containsKey(MyConstants.KEY_ZOOM))
            {
                savedZoom = savedInstanceState.getDouble(MyConstants.KEY_ZOOM);
            }
        }

        localDb = AppDatabase.getDatabase(requireContext());
        db = FirebaseFirestore.getInstance();

        boolean isUserLoggedIn = (FirebaseAuth.getInstance().getCurrentUser() != null);
        if (isUserLoggedIn)
        {
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        mapView = root.findViewById(R.id.map_view);
        panelSpotInfo = root.findViewById(R.id.panel_spot_info);
        tvSpotName = root.findViewById(R.id.tv_spot_name);
        tvSpotAddress = root.findViewById(R.id.tv_spot_address);
        btnStudyHere = root.findViewById(R.id.btn_study_here);

        root.findViewById(R.id.btn_close_panel).setOnClickListener(v -> panelSpotInfo.setVisibility(View.GONE));

        btnStudyHere.setOnClickListener(v ->
        {
            if (selectedSpot != null)
            {
                launchNavigation(selectedSpot);
                persistSpotToLocalDatabase(selectedSpot);
            }
        });

        root.findViewById(R.id.fab_my_location).setOnClickListener(v -> requestFocusOnUserLocation());

        View btnRecenter = root.findViewById(R.id.btn_recenter);
        if (btnRecenter != null)
        {
            btnRecenter.setOnClickListener(v -> requestFocusOnUserLocation());
        }

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style ->
        {
            boolean isRestoringCamera = (savedLatitude != null && savedLongitude != null && savedZoom != null);
            if (isRestoringCamera)
            {
                updateCameraPosition(savedLatitude, savedLongitude, savedZoom);
            }
            else
            {
                updateCameraPosition(MyConstants.BRUSSELS_LAT, MyConstants.BRUSSELS_LNG, MyConstants.INITIAL_ZOOM);
            }

            style.addImage(MyConstants.KEY_MARKER_STUDY_SPOT, createMarkerBitmap(MyConstants.HEX_COLOR_STUDY_SPOT));
            style.addImage(MyConstants.KEY_MARKER_STUDENT, createMarkerBitmap(MyConstants.HEX_COLOR_STUDENT));

            initializeAnnotationManagers();
            displayFixedStudySpots();
            loadStudentLocationsFromFirestore();
            verifyLocationPermissions();
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        boolean isMapReady = (mapView != null && mapView.getMapboxMap() != null);
        if (isMapReady)
        {
            Point center = mapView.getMapboxMap().getCameraState().getCenter();
            outState.putDouble(MyConstants.KEY_CAMERA_LAT, center.latitude());
            outState.putDouble(MyConstants.KEY_CAMERA_LON, center.longitude());
            outState.putDouble(MyConstants.KEY_ZOOM, mapView.getMapboxMap().getCameraState().getZoom());
        }
    }

    private void initializeAnnotationManagers()
    {
        AnnotationPlugin plugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        if (plugin == null)
        {
            return;
        }

        studySpotAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(plugin, new AnnotationConfig());
        studentAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(plugin, new AnnotationConfig());
    }

    private void updateCameraPosition(double latitude, double longitude, double zoom)
    {
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(Point.fromLngLat(longitude, latitude))
                .zoom(zoom)
                .build());
    }

    private void verifyLocationPermissions()
    {
        boolean hasFineLocationPermission = (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        if (hasFineLocationPermission)
        {
            enableUserLocationOnMap();
        }
        else
        {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void enableUserLocationOnMap()
    {
        LocationComponentPlugin locationPlugin = mapView.getPlugin(Plugin.MAPBOX_LOCATION_COMPONENT_PLUGIN_ID);
        if (locationPlugin != null)
        {
            locationPlugin.setEnabled(true);
            locationPlugin.setPulsingEnabled(true);
            locationPlugin.setLocationPuck(new LocationPuck2D());
        }
    }

    @SuppressLint("MissingPermission")
    private void requestFocusOnUserLocation()
    {
        boolean hasFineLocationPermission = (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        if (!hasFineLocationPermission)
        {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location ->
        {
            if (location != null)
            {
                updateCameraPosition(location.getLatitude(), location.getLongitude(), MyConstants.USER_ZOOM);
            }
            else
            {
                updateCameraPosition(MyConstants.BRUSSELS_LAT, MyConstants.BRUSSELS_LNG, MyConstants.INITIAL_ZOOM);
            }
        });
    }

    private void displayFixedStudySpots()
    {
        if (studySpotAnnotationManager == null)
        {
            return;
        }

        List<StudyLocation> spots = StudyLocationData.getStudyLocations();

        for (StudyLocation spot : spots)
        {
            PointAnnotationOptions options = new PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(spot.getLng(), spot.getLat()))
                    .withIconImage(MyConstants.KEY_MARKER_STUDY_SPOT)
                    .withIconAnchor(IconAnchor.BOTTOM)
                    .withIconSize(1.0)
                    .withTextField(spot.getName())
                    .withTextSize( STUDY_SPOT_TEXT_SIZE)
                    .withTextColor(MyConstants.HEX_COLOR_TEXT_PRIMARY)
                    .withTextHaloColor(MyConstants.HEX_COLOR_WHITE)
                    .withTextHaloWidth(2.0)
                    .withTextAnchor(TextAnchor.TOP)
                    .withTextOffset(Arrays.asList(0.0, STUDY_SPOT_TEXT_OFFSET_Y));

            studySpotAnnotationManager.create(options);
        }

        studySpotAnnotationManager.addClickListener(annotation ->
        {
            String clickedSpotName = annotation.getTextField();
            for (StudyLocation spot : spots)
            {
                if (clickedSpotName != null && clickedSpotName.equals(spot.getName()))
                {
                    showSpotDetailsPanel(spot);
                    updateCameraPosition(spot.getLat(), spot.getLng(), MyConstants.USER_ZOOM);
                    break;
                }
            }
            return true;
        });
    }

    private void loadStudentLocationsFromFirestore()
    {
        if (db == null || studentAnnotationManager == null)
        {
            return;
        }

        db.collection(MyConstants.COLLECTION_USERS).get()
                .addOnSuccessListener(querySnapshot ->
                {
                    studentAnnotationManager.deleteAll();

                    for (QueryDocumentSnapshot document : querySnapshot)
                    {
                        processStudentDocument(document);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading student locations", e));

        studentAnnotationManager.addClickListener(annotation ->
        {
            Point p = annotation.getPoint();
            String label = annotation.getTextField();
            if (p != null)
            {
                MapNavigation.promptForNavigation(requireContext(), p.latitude(), p.longitude(), label);
            }
            return true;
        });
    }

    private void processStudentDocument(QueryDocumentSnapshot document)
    {
        String userId = document.getId();
        if (userId.equals(currentUid))
        {
            return;
        }

        String studentName = document.getString(MyConstants.FIELD_NAME);
        String displayName = (studentName != null && !studentName.isEmpty())
                ? studentName : MyConstants.DEFAULT_STUDENT_NAME;

        Double lat = readDouble(document, MyConstants.FIELD_STUDY_LOCATION_LAT);
        Double lng = readDouble(document, MyConstants.FIELD_STUDY_LOCATION_LNG);
        if (lat != null && lng != null)
        {
            addStudentMarkerOnMap(lat, lng, displayName);
            return;
        }

        Double legacyLat = readDouble(document, MyConstants.FIELD_LATITUDE_LEGACY);
        Double legacyLng = readDouble(document, MyConstants.FIELD_LONGITUDE_LEGACY);
        if (legacyLat != null && legacyLng != null)
        {
            addStudentMarkerOnMap(legacyLat, legacyLng, displayName);
            return;
        }

        String locationText = document.getString(MyConstants.FIELD_STUDY_LOCATION);
        if (locationText == null || locationText.trim().isEmpty())
        {
            return;
        }

        StudyLocation matchedSpot = findKnownSpotByName(locationText.trim());
        if (matchedSpot != null)
        {
            addStudentMarkerOnMap(matchedSpot.getLat(), matchedSpot.getLng(), displayName);
        }
    }

    private Double readDouble(QueryDocumentSnapshot doc, String field)
    {
        Object raw = doc.get(field);
        if (raw instanceof Number)
        {
            return ((Number) raw).doubleValue();
        }
        return null;
    }

    private StudyLocation findKnownSpotByName(String inputLocationName)
    {
        if (inputLocationName == null || inputLocationName.isEmpty())
        {
            return null;
        }
        String needle = normalize(inputLocationName);

        for (StudyLocation spot : StudyLocationData.getStudyLocations())
        {
            String hay = normalize(spot.getName());
            if (hay.equals(needle) || needle.contains(hay) || hay.contains(needle))
            {
                return spot;
            }
        }

        String[] needleTokens = needle.split("\\s+");
        for (StudyLocation spot : StudyLocationData.getStudyLocations())
        {
            String[] hayTokens = normalize(spot.getName()).split("\\s+");
            int shared = MyConstants.ZERO;
            for (String n : needleTokens)
            {
                if (n.length() < MyConstants.THREE)
                {
                    continue;
                }
                for (String h : hayTokens)
                {
                    if (h.equals(n))
                    {
                        shared++;
                        break;
                    }
                }
            }
            if (shared >= MyConstants.TWO)
            {
                return spot;
            }
        }
        return null;
    }

    private String normalize(String s)
    {
        return s.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    private void addStudentMarkerOnMap(double latitude, double longitude, String studentName)
    {
        double jitteredLatitude = latitude + (Math.random() - RANDOM_CENTER_OFFSET) * MyConstants.MARKER_OVERLAP_OFFSET;
        double jitteredLongitude = longitude + (Math.random() - RANDOM_CENTER_OFFSET) * MyConstants.MARKER_OVERLAP_OFFSET;

        PointAnnotationOptions studentMarkerOptions = new PointAnnotationOptions()
                .withPoint(Point.fromLngLat(jitteredLongitude, jitteredLatitude))
                .withIconImage(MyConstants.KEY_MARKER_STUDENT)
                .withTextField(studentName)
                .withTextSize(10.0)
                .withTextColor(MyConstants.HEX_COLOR_STUDENT)
                .withTextAnchor(TextAnchor.BOTTOM)
                .withTextOffset(Arrays.asList(0.0, STUDENT_TEXT_OFFSET_Y));

        studentAnnotationManager.create(studentMarkerOptions);
    }

    private Bitmap createMarkerBitmap(String colorHexCode)
    {
        Bitmap bitmap = Bitmap.createBitmap(MyConstants.MARKER_WIDTH_PX, MyConstants.MARKER_HEIGHT_PX, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.parseColor(colorHexCode));
        android.graphics.Path pinPath = new android.graphics.Path();
        pinPath.moveTo(MyConstants.MARKER_WIDTH_PX / 2f, MyConstants.MARKER_HEIGHT_PX);
        pinPath.cubicTo(MyConstants.ZERO, MyConstants.MARKER_HEIGHT_PX * 0.4f, MyConstants.ZERO, MyConstants.ZERO, MyConstants.MARKER_WIDTH_PX / 2f, MyConstants.ZERO);
        pinPath.cubicTo(MyConstants.MARKER_WIDTH_PX, MyConstants.ZERO, MyConstants.MARKER_WIDTH_PX, MyConstants.MARKER_HEIGHT_PX * 0.4f, MyConstants.MARKER_WIDTH_PX / 2f, MyConstants.MARKER_HEIGHT_PX);
        canvas.drawPath(pinPath, paint);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(MyConstants.MARKER_WIDTH_PX / 2f, MyConstants.MARKER_HEIGHT_PX * 0.35f, MyConstants.MARKER_WIDTH_PX * 0.2f, paint);

        return bitmap;
    }

    private void showSpotDetailsPanel(StudyLocation spot)
    {
        selectedSpot = spot;
        tvSpotName.setText(spot.getName());
        tvSpotAddress.setText(spot.getAddress());
        panelSpotInfo.setVisibility(View.VISIBLE);
    }

    private void launchNavigation(StudyLocation destinationSpot)
    {
        MapNavigation.openInMaps(
                requireContext(),
                destinationSpot.getLat(),
                destinationSpot.getLng(),
                destinationSpot.getName());
    }

    private void persistSpotToLocalDatabase(StudyLocation spot)
    {
        new Thread(() ->
        {
            StudySpotEntity entity = new StudySpotEntity(
                    spot.getName(), spot.getAddress(), spot.getLat(), spot.getLng());
            localDb.studySpotDao().insert(entity);

            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Study spot saved to history.", Toast.LENGTH_SHORT).show());
        }).start();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (mapView != null)
        {
            mapView.onStart();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mapView != null)
        {
            mapView.onStop();
        }
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        if (mapView != null)
        {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (mapView != null)
        {
            mapView.onDestroy();
            mapView = null;
        }
    }
}
