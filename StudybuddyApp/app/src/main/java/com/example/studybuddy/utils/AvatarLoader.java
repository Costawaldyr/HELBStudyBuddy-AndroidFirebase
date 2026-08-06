package com.example.studybuddy.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper used to display a user's profile picture inside any ImageView of
 * the application. Handles a process-wide cache so scrolling the messages,
 * matches and buddies lists never hits Firestore once per row.
 */
public final class AvatarLoader
{
    private static final Map<String, String> URL_CACHE = new HashMap<>();

    private AvatarLoader()
    {
    }

    /**
     * Loads the avatar for a given buddy. Tries the supplied image URL first,
     * then renders the course emoji on a coloured circle, then falls back to
     * the AI placeholder icon.
     */
    public static void loadBuddyAvatar(Context context, String imageUrl, String emoji, String hexColor, ImageView target)
    {
        if (target == null)
        {
            return;
        }

        if (imageUrl != null && !imageUrl.isEmpty())
        {
            displayUrl(context, imageUrl, target);
        }
        else if (emoji != null && !emoji.isEmpty())
        {
            target.setImageBitmap(renderEmojiOnCircle(context, emoji, hexColor));
        }
        else
        {
            displayAiPlaceholder(target);
        }
    }

    /**
     * Renders a circular bitmap with an emoji centered on a coloured background.
     */
    public static Bitmap renderEmojiOnCircle(Context context, String emoji, String hexColor)
    {
        float density = context.getResources().getDisplayMetrics().density;
        int sizePx = (int) (MyConstants.DEFAULT_AVATAR_DP * density);
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
        background.setColor(parseColorOrDefault(hexColor));
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, background);

        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setTextSize(sizePx * MyConstants.EMOJI_TEXT_SIZE_RATIO);
        text.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = text.getFontMetrics();
        float baseline = sizePx / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(emoji, sizePx / 2f, baseline, text);

        return bitmap;
    }

    /**
     * Loads the avatar for a Firestore user. Uses the supplied URL if known,
     * otherwise looks it up from Firestore and caches the result.
     */
    public static void loadInto(Context context, String userId, String knownUrl, ImageView target)
    {
        if (target == null)
        {
            return;
        }

        if (knownUrl != null && !knownUrl.isEmpty())
        {
            displayUrl(context, knownUrl, target);
            return;
        }

        if (userId == null || userId.isEmpty())
        {
            displayPlaceholder(target);
            return;
        }

        String cached = URL_CACHE.get(userId);
        if (cached != null)
        {
            applyCached(context, cached, target);
            return;
        }

        displayPlaceholder(target);
        target.setTag(R.id.avatar_loader_tag, userId);

        FirebaseFirestore.getInstance()
                .collection(MyConstants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(doc ->
                {
                    String url = doc.exists() ? doc.getString(MyConstants.FIELD_PROFILE_IMAGE_URL) : null;
                    URL_CACHE.put(userId, (url != null && !url.isEmpty()) ? url : MyConstants.EMPTY_URL_SENTINEL);

                    Object stillSameRow = target.getTag(R.id.avatar_loader_tag);
                    if (!userId.equals(stillSameRow))
                    {
                        return;
                    }

                    if (url != null && !url.isEmpty())
                    {
                        displayUrl(context, url, target);
                    }
                    else
                    {
                        displayPlaceholder(target);
                    }
                })
                .addOnFailureListener(e -> URL_CACHE.put(userId, MyConstants.EMPTY_URL_SENTINEL));
    }

    private static void applyCached(Context context, String cached, ImageView target)
    {
        if (MyConstants.EMPTY_URL_SENTINEL.equals(cached))
        {
            displayPlaceholder(target);
        }
        else
        {
            displayUrl(context, cached, target);
        }
    }

    private static void displayUrl(Context context, String url, ImageView target)
    {
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(target);
    }

    private static void displayAiPlaceholder(ImageView target)
    {
        Glide.with(target.getContext())
                .load(R.drawable.ic_robot)
                .circleCrop()
                .into(target);
    }

    private static void displayPlaceholder(ImageView target)
    {
        target.setImageResource(R.drawable.ic_profile);
    }

    private static int parseColorOrDefault(String hexColor)
    {
        if (hexColor == null || hexColor.isEmpty())
        {
            return MyConstants.DEFAULT_BG_COLOR;
        }
        try
        {
            return Color.parseColor(hexColor);
        }
        catch (IllegalArgumentException ignored)
        {
            return MyConstants.DEFAULT_BG_COLOR;
        }
    }
}
