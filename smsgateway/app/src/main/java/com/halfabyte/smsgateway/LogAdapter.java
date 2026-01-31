package com.halfabyte.smsgateway;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.halfabyte.smsgateway.models.LogEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying activity log entries.
 */
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private final List<LogEntry> logEntries = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private final Context context;

    public LogAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry entry = logEntries.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return logEntries.size();
    }

    public void addEntry(LogEntry entry) {
        logEntries.add(0, entry); // Add to top
        notifyItemInserted(0);

        // Keep list manageable
        if (logEntries.size() > 100) {
            logEntries.remove(logEntries.size() - 1);
            notifyItemRemoved(logEntries.size());
        }
    }

    public void clear() {
        int size = logEntries.size();
        logEntries.clear();
        notifyItemRangeRemoved(0, size);
    }

    class LogViewHolder extends RecyclerView.ViewHolder {
        private final View logTypeIndicator;
        private final TextView logMessage;
        private final TextView logTimestamp;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            logTypeIndicator = itemView.findViewById(R.id.logTypeIndicator);
            logMessage = itemView.findViewById(R.id.logMessage);
            logTimestamp = itemView.findViewById(R.id.logTimestamp);
        }

        public void bind(LogEntry entry) {
            logMessage.setText(entry.getMessage());
            logTimestamp.setText(timeFormat.format(new Date(entry.getTimestamp())));

            int indicatorColor;
            switch (entry.getType()) {
                case INCOMING:
                    indicatorColor = R.color.log_incoming;
                    break;
                case OUTGOING:
                    indicatorColor = R.color.log_outgoing;
                    break;
                case ERROR:
                    indicatorColor = R.color.log_error;
                    break;
                case SYSTEM:
                default:
                    indicatorColor = R.color.log_system;
                    break;
            }

            logTypeIndicator.setBackgroundColor(ContextCompat.getColor(context, indicatorColor));
        }
    }
}
