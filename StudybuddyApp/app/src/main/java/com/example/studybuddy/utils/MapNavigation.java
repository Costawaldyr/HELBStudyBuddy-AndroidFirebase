package com.example.studybuddy.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * Utility for opening locations in Google Maps.
 */
public final class MapNavigation
{
    private MapNavigation()
    {
    }

    /**
     * Prompts the user to open a specific location in Google Maps.
     */
    public static void promptForNavigation(Context context, double latitude, double longitude, String label)
    {
        String safeLabel = resolveLabel(label);
        new AlertDialog.Builder(context)
                .setTitle(MyConstants.MAP_DIALOG_TITLE)
                .setMessage(String.format(MyConstants.MAP_DIALOG_MESSAGE_TEMPLATE, safeLabel))
                .setPositiveButton(MyConstants.MAP_DIALOG_POSITIVE, (dialog, which) -> openInMaps(context, latitude, longitude, safeLabel))
                .setNegativeButton(MyConstants.MAP_DIALOG_NEGATIVE, null)
                .show();
    }

    /**
     * Launches the Google Maps application for navigation.
     */
    public static void openInMaps(Context context, double latitude, double longitude, String label)
    {
        String safeLabel = resolveLabel(label);
        Intent navigationIntent = buildNavigationIntent(latitude, longitude);
        if (navigationIntent.resolveActivity(context.getPackageManager()) != null)
        {
            context.startActivity(navigationIntent);
            return;
        }

        Intent webIntent = buildWebIntent(latitude, longitude);
        if (webIntent.resolveActivity(context.getPackageManager()) != null)
        {
            context.startActivity(webIntent);
            return;
        }

        Toast.makeText(context, String.format(MyConstants.NO_MAPS_MESSAGE_TEMPLATE, safeLabel), Toast.LENGTH_SHORT).show();
    }

    /**
     * Constructs an intent for the Google Maps app.
     */
    private static Intent buildNavigationIntent(double latitude, double longitude)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(MyConstants.NAV_URI_TEMPLATE, latitude, longitude)));
        intent.setPackage(MyConstants.GOOGLE_MAPS_PACKAGE);
        return intent;
    }

    /**
     * Constructs a web fallback intent for Google Maps.
     */
    private static Intent buildWebIntent(double latitude, double longitude)
    {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(MyConstants.WEB_URI_TEMPLATE, latitude, longitude)));
    }

    /**
     * Ensures the location label is never null or empty.
     */
    private static String resolveLabel(String label)
    {
        return (label != null && !label.isEmpty()) ? label : MyConstants.MAP_DEFAULT_LABEL;
    }
}
