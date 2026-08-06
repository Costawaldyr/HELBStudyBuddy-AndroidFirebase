package com.example.studybuddy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.data.local.NotificationEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying local notifications stored in the app's database.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>
{
    private static final String TIME_FORMAT_PATTERN = "HH:mm";

    private List<NotificationEntity> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;

    /**
     * Interface for handling click and delete events on notifications.
     */
    public interface OnNotificationClickListener
    {
        void onNotificationClick(NotificationEntity notification);
        void onNotificationDelete(NotificationEntity notification);
    }

    /**
     * Initializes the adapter with a listener for notification events.
     *
     * @param listener The listener to handle interactions.
     */
    public NotificationAdapter(OnNotificationClickListener listener)
    {
        this.listener = listener;
    }

    /**
     * Updates the list of notifications.
     *
     * @param notifications The new list of notifications.
     */
    public void setNotifications(List<NotificationEntity> notifications)
    {
        this.notifications = (notifications != null) ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        NotificationEntity notification = notifications.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());

        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_PATTERN, Locale.getDefault());
        String formattedTime = timeFormat.format(new Date(notification.getTimestamp()));
        holder.tvTime.setText(formattedTime);

        holder.itemView.setOnClickListener(v ->
        {
            if (listener != null)
            {
                listener.onNotificationClick(notification);
            }
        });

        holder.btnDelete.setOnClickListener(v ->
        {
            if (listener != null)
            {
                listener.onNotificationDelete(notification);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return notifications.size();
    }

    /**
     * ViewHolder for notification items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvTitle, tvMessage, tvTime;
        View btnDelete;

        ViewHolder(View itemView)
        {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            btnDelete = itemView.findViewById(R.id.btn_delete_notification);
        }
    }
}
