package com.example.studybuddy.adapters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.models.Buddy;
import com.example.studybuddy.utils.AvatarLoader;
import com.example.studybuddy.utils.MyConstants;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of AI Study Buddies with swipe-to-edit and swipe-to-delete support.
 */
public class IABuddyAdapter extends RecyclerView.Adapter<IABuddyAdapter.BuddyViewHolder>
{
    private static final double ONLINE_PROBABILITY = 0.3;
    private static final int COLOR_CHIP_TEXT = 0xFF3949AB;
    private static final int COLOR_STATUS_ONLINE = 0xFF4CAF50;
    private static final int COLOR_STATUS_OFFLINE = 0xFF9E9E9E;

    private List<Buddy> buddies = new ArrayList<>();
    private final OnBuddyClickListener listener;
    private final OnBuddySwipeListener swipeListener;

    /**
     * Interface for handling buddy click events (e.g., starting a chat).
     */
    public interface OnBuddyClickListener
    {
        void onChatClick(Buddy buddy);
    }

    /**
     * Interface for handling swipe actions on buddy items.
     */
    public interface OnBuddySwipeListener
    {
        void onDelete(Buddy buddy);
        void onEdit(Buddy buddy);
    }

    /**
     * Initializes the adapter with click and swipe listeners.
     *
     * @param listener      Handles chat button clicks.
     * @param swipeListener Handles swipe-to-edit and swipe-to-delete.
     */
    public IABuddyAdapter(OnBuddyClickListener listener, OnBuddySwipeListener swipeListener)
    {
        this.listener = listener;
        this.swipeListener = swipeListener;
    }

    /**
     * Updates the full list of buddies.
     *
     * @param buddies The new list of buddies.
     */
    public void setBuddies(List<Buddy> buddies)
    {
        this.buddies = buddies;
        notifyDataSetChanged();
    }

    /**
     * Removes a buddy from the list at a specific position.
     *
     * @param position The position of the buddy to remove.
     */
    public void removeBuddy(int position)
    {
        if (position >= MyConstants.ZERO && position < buddies.size())
        {
            buddies.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public BuddyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ia_buddy, parent, false);
        return new BuddyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuddyViewHolder holder, int position)
    {
        Buddy buddy = buddies.get(position);
        holder.bind(buddy);
    }

    @Override
    public int getItemCount()
    {
        return buddies.size();
    }

    /**
     * ViewHolder for AI Buddy items.
     */
    class BuddyViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView tvName, tvSpecialty, tvStatus;
        private final android.widget.ImageView ivRealImage;
        private final FlexboxLayout layoutSubjects;
        private final View btnChat;

        BuddyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            ivRealImage = itemView.findViewById(R.id.iv_buddy_real_image);
            tvName = itemView.findViewById(R.id.tv_buddy_name);
            tvSpecialty = itemView.findViewById(R.id.tv_buddy_specialty);
            tvStatus = itemView.findViewById(R.id.tv_buddy_status);
            layoutSubjects = itemView.findViewById(R.id.layout_subjects);
            btnChat = itemView.findViewById(R.id.btn_chat);
        }

        /**
         * Binds buddy data to the view components.
         *
         * @param buddy The AI Buddy model.
         */
        void bind(Buddy buddy)
        {
            AvatarLoader.loadBuddyAvatar(
                    itemView.getContext(),
                    buddy.getImageUrl(),
                    buddy.getCourseEmoji(),
                    buddy.getCourseColor(),
                    ivRealImage
            );

            tvName.setText(buddy.getName());
            tvSpecialty.setText(String.format("Specialized in: %s", buddy.getCourseName()));

            boolean online = Math.random() > ONLINE_PROBABILITY;
            tvStatus.setText(online ? "Online" : "Offline");
            tvStatus.setTextColor(online ? COLOR_STATUS_ONLINE : COLOR_STATUS_OFFLINE);
            tvStatus.setBackgroundResource(online ?
                    R.drawable.bg_status_online : R.drawable.bg_status_offline);

            layoutSubjects.removeAllViews();
            String courseName = buddy.getCourseName();
            if (courseName != null && !courseName.isEmpty())
            {
                String[] subjects = courseName.split(" ");
                for (String subject : subjects)
                {
                    if (!subject.isEmpty())
                    {
                        TextView chip = createSubjectChip(subject);
                        layoutSubjects.addView(chip);
                    }
                }
            }

            btnChat.setOnClickListener(v ->
            {
                if (listener != null)
                {
                    listener.onChatClick(buddy);
                }
            });
        }

        private TextView createSubjectChip(String subject)
        {
            TextView chip = new TextView(itemView.getContext());
            chip.setText(subject);
            chip.setTextSize(11);
            chip.setTextColor(COLOR_CHIP_TEXT);
            chip.setBackgroundResource(R.drawable.bg_chip);
            chip.setPadding(12, MyConstants.FOUR, 12, MyConstants.FOUR);
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(MyConstants.ZERO, MyConstants.ZERO, MyConstants.FOUR, MyConstants.FOUR);
            chip.setLayoutParams(params);
            return chip;
        }
    }

    /**
     * ItemTouchHelper callback for swipe actions on RecyclerView items.
     */
    public static class SwipeCallback extends ItemTouchHelper.SimpleCallback
    {
        private static final String DELETE_COLOR = "#FF4757";
        private static final String EDIT_COLOR = "#3949AB";
        private static final String TEXT_EDIT = "EDIT";
        private static final String TEXT_DELETE = "DELETE";

        private final IABuddyAdapter adapter;
        private final Paint paint;
        private final Paint deletePaint;
        private final Paint editPaint;
        private final RectF background;

        public SwipeCallback(IABuddyAdapter adapter)
        {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.adapter = adapter;

            paint = new Paint();
            paint.setAntiAlias(true);

            deletePaint = new Paint();
            deletePaint.setColor(Color.parseColor(DELETE_COLOR));

            editPaint = new Paint();
            editPaint.setColor(Color.parseColor(EDIT_COLOR));

            background = new RectF();
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target)
        {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
        {
            int position = viewHolder.getAdapterPosition();
            if (position == RecyclerView.NO_POSITION)
            {
                return;
            }

            Buddy buddy = adapter.buddies.get(position);
            if (direction == ItemTouchHelper.LEFT)
            {
                adapter.swipeListener.onDelete(buddy);
            }
            else if (direction == ItemTouchHelper.RIGHT)
            {
                adapter.swipeListener.onEdit(buddy);
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive)
        {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
            {
                View itemView = viewHolder.itemView;
                float height = (float) itemView.getBottom() - (float) itemView.getTop();
                float width = height / MyConstants.THREE;

                if (dX > MyConstants.ZERO)
                {
                    background.set(
                            (float) itemView.getLeft(),
                            (float) itemView.getTop(),
                            (float) itemView.getLeft() + dX,
                            (float) itemView.getBottom()
                    );
                    c.drawRect(background, editPaint);

                    paint.setColor(Color.WHITE);
                    paint.setTextSize(30);
                    paint.setTextAlign(Paint.Align.CENTER);
                    c.drawText(TEXT_EDIT,
                            (float) itemView.getLeft() + width * MyConstants.TWO,
                            (float) itemView.getTop() + height / MyConstants.TWO + MyConstants.TEN,
                            paint);
                }
                else if (dX < MyConstants.ZERO)
                {
                    background.set(
                            (float) itemView.getRight() + dX,
                            (float) itemView.getTop(),
                            (float) itemView.getRight(),
                            (float) itemView.getBottom()
                    );
                    c.drawRect(background, deletePaint);

                    paint.setColor(Color.WHITE);
                    paint.setTextSize(30);
                    paint.setTextAlign(Paint.Align.CENTER);
                    c.drawText(TEXT_DELETE,
                            (float) itemView.getRight() - width * MyConstants.TWO,
                            (float) itemView.getTop() + height / MyConstants.TWO + MyConstants.TEN,
                            paint);
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
