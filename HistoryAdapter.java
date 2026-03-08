package com.offlineai.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.offlineai.app.R;
import com.offlineai.app.models.HistorySession;
import com.offlineai.app.utils.ResponseFormatter;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(HistorySession session);
        void onSessionDelete(HistorySession session);
    }

    private List<HistorySession> sessions;
    private Context context;
    private OnSessionClickListener listener;

    public HistoryAdapter(List<HistorySession> sessions, Context context, OnSessionClickListener listener) {
        this.sessions = sessions;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistorySession session = sessions.get(position);
        holder.tvTitle.setText(session.getTitle());
        holder.tvPreview.setText(session.getPreview());
        holder.tvDate.setText(ResponseFormatter.formatDate(session.getTimestamp()));
        holder.itemView.setOnClickListener(v -> listener.onSessionClick(session));
        holder.btnDelete.setOnClickListener(v -> listener.onSessionDelete(session));
    }

    @Override
    public int getItemCount() { return sessions.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPreview, tvDate;
        ImageButton btnDelete;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvPreview = v.findViewById(R.id.tv_preview);
            tvDate = v.findViewById(R.id.tv_date);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
