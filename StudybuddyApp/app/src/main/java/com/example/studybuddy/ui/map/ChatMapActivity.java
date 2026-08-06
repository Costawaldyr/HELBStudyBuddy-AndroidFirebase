package com.example.studybuddy.ui.map;

import static com.example.studybuddy.utils.MyConstants.*;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studybuddy.R;
import com.example.studybuddy.data.StudyLocationData;
import com.example.studybuddy.models.StudyLocation;
import com.example.studybuddy.service.MapboxGeocodingRepository;
import com.example.studybuddy.utils.MapNavigation;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesUtils;
import com.mapbox.maps.plugin.gestures.OnMoveListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatMapActivity extends AppCompatActivity
{
    public static final String EXTRA_PARTICIPANT_UIDS = "participant_uids";
    public static final String EXTRA_CHAT_TITLE = "chat_title";

    private static final String TAG = "ChatMapActivity";
    private static final String MAP_UNAVAILABLE_TOAST = "Map annotations unavailable";

    private MapView mapView;
    private TextView tvHeaderTitle;
    private TextView tvParticipantsSummary;

    private FirebaseFirestore db;
    private MapboxGeocodingRepository geocodingRepository;
    private PointAnnotationManager participantAnnotationManager;
    private ChatMapViewModel viewModel;

    private final List<Point> plottedPoints = new ArrayList<>();
    private final Set<String> participantUidSet = new HashSet<>();
    private int participantsWithLocation;
    private int participantsExpected;

    public static Intent createIntent(android.content.Context context, String[] participantUids, String chatTitle)
    {
        Intent intent = new Intent(context, ChatMapActivity.class);
        intent.putExtra(EXTRA_PARTICIPANT_UIDS, participantUids);
        if (chatTitle != null && !chatTitle.isEmpty())
        {
            intent.putExtra(EXTRA_CHAT_TITLE, chatTitle);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_map);

        viewModel = new ViewModelProvider(this).get(ChatMapViewModel.class);
        db = FirebaseFirestore.getInstance();
        geocodingRepository = new MapboxGeocodingRepository(this);

        bindViews();
        applyHeader();
        loadMap();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (mapView != null)
        {
            mapView.onStart();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (mapView != null)
        {
            mapView.onStop();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mapView != null)
        {
            mapView.onDestroy();
        }
    }

    private void bindViews()
    {
        mapView = findViewById(R.id.chat_map_view);
        tvHeaderTitle = findViewById(R.id.tv_chat_map_title);
        tvParticipantsSummary = findViewById(R.id.tv_chat_map_summary);

        View btnBack = findViewById(R.id.btn_chat_map_back);
        if (btnBack != null)
        {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void applyHeader()
    {
        String title = getIntent().getStringExtra(EXTRA_CHAT_TITLE);
        if (title != null && !title.isEmpty() && tvHeaderTitle != null)
        {
            tvHeaderTitle.setText(title);
        }
        if (tvParticipantsSummary != null)
        {
            tvParticipantsSummary.setText(R.string.chat_map_loading);
        }
    }

    private void loadMap()
    {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style ->
        {
            style.addImage(MARKER_IMAGE_ID, createMarkerBitmap(MARKER_COLOR_HEX));
            style.addImage(OTHER_MARKER_IMAGE_ID, createMarkerBitmap(OTHER_MARKER_COLOR_HEX));
            setupAnnotationManager();
            setupCameraListeners();
            applyInitialCamera();
            loadParticipants();
        });
    }

    private void setupAnnotationManager()
    {
        AnnotationPlugin plugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        if (plugin == null)
        {
            plugin = mapView.getPlugin(MAP_ANNOTATIONS_PLUGIN);
        }
        if (plugin == null)
        {
            Log.e(TAG, "Annotation plugin missing.");
            return;
        }

        try
        {
            participantAnnotationManager = (PointAnnotationManager)
                    plugin.createAnnotationManager(AnnotationType.PointAnnotation, new AnnotationConfig());
        }
        catch (Throwable t)
        {
            participantAnnotationManager =
                    PointAnnotationManagerKt.createPointAnnotationManager(plugin, new AnnotationConfig());
        }

        if (participantAnnotationManager != null)
        {
            participantAnnotationManager.addClickListener(annotation ->
            {
                Point point = annotation.getPoint();
                String label = annotation.getTextField();
                if (point != null)
                {
                    MapNavigation.promptForNavigation(this, point.latitude(), point.longitude(), label);
                }
                return true;
            });
        }
    }

    private void setupCameraListeners()
    {
        GesturesUtils.getGestures(mapView).addOnMoveListener(new OnMoveListener()
        {
            @Override
            public void onMoveBegin(@NonNull com.mapbox.android.gestures.MoveGestureDetector detector)
            {
            }

            @Override
            public boolean onMove(@NonNull com.mapbox.android.gestures.MoveGestureDetector detector)
            {
                return false;
            }

            @Override
            public void onMoveEnd(@NonNull com.mapbox.android.gestures.MoveGestureDetector detector)
            {
                viewModel.setCameraCenter(mapView.getMapboxMap().getCameraState().getCenter());
                viewModel.setZoomLevel(mapView.getMapboxMap().getCameraState().getZoom());
            }
        });
    }

    private void applyInitialCamera()
    {
        Point savedCenter = viewModel.getCameraCenter().getValue();
        Double savedZoom = viewModel.getZoomLevel().getValue();

        if (savedCenter != null && savedZoom != null)
        {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .center(savedCenter)
                    .zoom(savedZoom)
                    .build());
            return;
        }

        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(Point.fromLngLat(BRUSSELS_LNG_DEFAULT, BRUSSELS_LAT_DEFAULT))
                .zoom(FALLBACK_ZOOM)
                .build());
    }

    private void loadParticipants()
    {
        String[] uids = getIntent().getStringArrayExtra(EXTRA_PARTICIPANT_UIDS);
        if (uids == null || uids.length == ZERO)
        {
            updateSummary();
            loadOtherStudents();
            return;
        }

        plottedPoints.clear();
        participantsWithLocation = ZERO;
        participantsExpected = uids.length;
        participantUidSet.clear();

        for (String uid : uids)
        {
            if (uid != null && !uid.isEmpty())
            {
                participantUidSet.add(uid);
            }
        }

        for (String uid : uids)
        {
            if (uid == null || uid.isEmpty())
            {
                participantsExpected--;
                continue;
            }
            db.collection(COLLECTION_USERS).document(uid)
                    .get()
                    .addOnSuccessListener(this::handleParticipantDocument)
                    .addOnFailureListener(error ->
                    {
                        markParticipantHandled();
                    });
        }

        loadOtherStudents();
    }

    private void loadOtherStudents()
    {
        db.collection(COLLECTION_USERS).get()
                .addOnSuccessListener(snapshots ->
                {
                    for (DocumentSnapshot doc : snapshots)
                    {
                        String uid = doc.getId();
                        if (!participantUidSet.contains(uid))
                        {
                            plotIfHasLocation(doc, false);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Could not load students", e));
    }

    private void handleParticipantDocument(DocumentSnapshot doc)
    {
        if (!doc.exists())
        {
            markParticipantHandled();
            return;
        }

        String displayName = pickDisplayName(doc);
        Double lat = readDouble(doc, FIELD_STUDY_LOCATION_LAT);
        Double lng = readDouble(doc, FIELD_STUDY_LOCATION_LNG);
        String address = readTrimmed(doc, FIELD_STUDY_LOCATION);

        if (lat != null && lng != null)
        {
            plotMarker(lat, lng, displayName, address, true);
            return;
        }

        Double legacyLat = readDouble(doc, FIELD_LATITUDE_LEGACY);
        Double legacyLng = readDouble(doc, FIELD_LONGITUDE_LEGACY);
        if (legacyLat != null && legacyLng != null)
        {
            plotMarker(legacyLat, legacyLng, displayName, address, true);
            return;
        }

        if (address == null || address.isEmpty())
        {
            markParticipantHandled();
            return;
        }

        StudyLocation hardcoded = matchHardcodedSpot(address);
        if (hardcoded != null)
        {
            plotMarker(hardcoded.getLat(), hardcoded.getLng(), displayName, hardcoded.getName(), true);
            return;
        }

        geocodeAndPlot(displayName, address);
    }

    private void geocodeAndPlot(String displayName, String address)
    {
        geocodingRepository.geocode(address, new MapboxGeocodingRepository.GeocodingCallback()
        {
            @Override
            public void onSuccess(double latitude, double longitude, String placeName)
            {
                plotMarker(latitude, longitude, displayName, address, true);
            }

            @Override
            public void onError(String errorMessage)
            {
                markParticipantHandled();
            }
        });
    }

    private void plotIfHasLocation(DocumentSnapshot doc, boolean isParticipant)
    {
        if (doc == null || !doc.exists())
        {
            return;
        }

        String displayName = pickDisplayName(doc);
        Double lat = readDouble(doc, FIELD_STUDY_LOCATION_LAT);
        Double lng = readDouble(doc, FIELD_STUDY_LOCATION_LNG);
        String address = readTrimmed(doc, FIELD_STUDY_LOCATION);

        if (lat != null && lng != null)
        {
            plotMarker(lat, lng, displayName, address, isParticipant);
            return;
        }

        Double legacyLat = readDouble(doc, FIELD_LATITUDE_LEGACY);
        Double legacyLng = readDouble(doc, FIELD_LONGITUDE_LEGACY);
        if (legacyLat != null && legacyLng != null)
        {
            plotMarker(legacyLat, legacyLng, displayName, address, isParticipant);
            return;
        }

        if (address != null && !address.isEmpty())
        {
            StudyLocation hardcoded = matchHardcodedSpot(address);
            if (hardcoded != null)
            {
                plotMarker(hardcoded.getLat(), hardcoded.getLng(), displayName, hardcoded.getName(), isParticipant);
            }
        }
    }

    private void plotMarker(Double latitude, Double longitude, String name, String address, boolean isParticipant)
    {
        if (latitude == null || longitude == null)
        {
            if (isParticipant)
            {
                markParticipantHandled();
            }
            return;
        }
        if (participantAnnotationManager == null)
        {
            if (isParticipant)
            {
                Toast.makeText(this, MAP_UNAVAILABLE_TOAST, Toast.LENGTH_SHORT).show();
                markParticipantHandled();
            }
            return;
        }

        Point base = Point.fromLngLat(longitude, latitude);
        Point spread = avoidOverlap(base);

        String label = (address == null || address.isEmpty()) ? name : name + "\n" + address;
        String iconId = isParticipant ? MARKER_IMAGE_ID : OTHER_MARKER_IMAGE_ID;

        PointAnnotationOptions options = new PointAnnotationOptions()
                .withPoint(spread)
                .withIconImage(iconId)
                .withIconAnchor(IconAnchor.BOTTOM)
                .withIconSize(isParticipant ? PARTICIPANT_ICON_SIZE : OTHER_ICON_SIZE)
                .withTextField(label)
                .withTextSize(isParticipant ? PARTICIPANT_TEXT_SIZE : OTHER_TEXT_SIZE)
                .withTextColor(Color.parseColor(MARKER_TEXT_COLOR_HEX))
                .withTextHaloColor(Color.parseColor(MARKER_TEXT_HALO_HEX))
                .withTextHaloWidth(TEXT_HALO_WIDTH)
                .withTextAnchor(TextAnchor.TOP)
                .withTextOffset(Arrays.asList(0.0, 0.8));

        participantAnnotationManager.create(options);
        plottedPoints.add(spread);

        if (isParticipant)
        {
            participantsWithLocation++;
            focusCameraOnFirstPlot(spread);
            markParticipantHandled();
        }
    }

    private Point avoidOverlap(Point base)
    {
        int collisions = ZERO;
        for (Point existing : plottedPoints)
        {
            double dLng = Math.abs(existing.longitude() - base.longitude());
            double dLat = Math.abs(existing.latitude() - base.latitude());
            if (dLng < DUPLICATE_OFFSET_DEGREES && dLat < DUPLICATE_OFFSET_DEGREES)
            {
                collisions++;
            }
        }
        if (collisions == ZERO)
        {
            return base;
        }
        double offset = DUPLICATE_OFFSET_DEGREES * collisions;
        return Point.fromLngLat(base.longitude() + offset, base.latitude() + offset);
    }

    private void focusCameraOnFirstPlot(Point point)
    {
        boolean firstMarker = plottedPoints.size() == ONE && viewModel.getCameraCenter().getValue() == null;
        if (firstMarker)
        {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .center(point)
                    .zoom(FOCUS_ZOOM)
                    .build());
        }
    }

    private void markParticipantHandled()
    {
        if (participantsExpected > ZERO)
        {
            participantsExpected--;
        }
        if (participantsExpected == ZERO)
        {
            updateSummary();
        }
    }

    private void updateSummary()
    {
        if (tvParticipantsSummary == null)
        {
            return;
        }
        if (participantsWithLocation == ZERO)
        {
            tvParticipantsSummary.setText(R.string.chat_map_no_locations);
        }
        else if (participantsWithLocation == ONE)
        {
            tvParticipantsSummary.setText(R.string.chat_map_one_location);
        }
        else
        {
            tvParticipantsSummary.setText(getString(R.string.chat_map_n_locations, participantsWithLocation));
        }
    }

    private Bitmap createMarkerBitmap(String hexColor)
    {
        Bitmap bitmap = Bitmap.createBitmap(MARKER_WIDTH_PX, MARKER_HEIGHT_PX, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.parseColor(hexColor));
        Path path = new Path();
        float centerX = MARKER_WIDTH_PX / MARKER_CENTER_X_RATIO;
        path.moveTo(centerX, MARKER_HEIGHT_PX);
        path.cubicTo(0, MARKER_HEIGHT_PX * MARKER_CUBIC_Y_RATIO, 0, 0, centerX, 0);
        path.cubicTo(MARKER_WIDTH_PX, 0, MARKER_WIDTH_PX, MARKER_HEIGHT_PX * MARKER_CUBIC_Y_RATIO, centerX, MARKER_HEIGHT_PX);
        canvas.drawPath(path, paint);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, MARKER_HEIGHT_PX * MARKER_CIRCLE_Y_RATIO, MARKER_WIDTH_PX * MARKER_CIRCLE_RADIUS_RATIO, paint);

        return bitmap;
    }

    private StudyLocation matchHardcodedSpot(String address)
    {
        if (address == null || address.isEmpty())
        {
            return null;
        }
        String needle = normalize(address);

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
            int shared = ZERO;
            for (String n : needleTokens)
            {
                if (n.length() < MIN_TOKEN_LENGTH)
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
            if (shared >= MIN_TOKEN_OVERLAP)
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

    private String pickDisplayName(DocumentSnapshot doc)
    {
        String name = doc.getString(FIELD_NAME);
        if (name == null || name.isEmpty())
        {
            return getString(R.string.chat_map_unknown_student);
        }
        return name;
    }

    private String readTrimmed(DocumentSnapshot doc, String field)
    {
        String raw = doc.getString(field);
        return (raw != null) ? raw.trim() : null;
    }

    private Double readDouble(DocumentSnapshot doc, String field)
    {
        Object raw = doc.get(field);
        if (raw instanceof Number)
        {
            return ((Number) raw).doubleValue();
        }
        return null;
    }
}
