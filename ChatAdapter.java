package com.offlineai.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.offlineai.app.R;
import com.offlineai.app.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_USER = 0;
    private static final int VIEW_AI = 1;

    private List<ChatMessage> messages;
    private Context context;

    public ChatAdapter(List<ChatMessage> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType() == ChatMessage.TYPE_USER ? VIEW_USER : VIEW_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_USER) {
            return new UserViewHolder(inflater.inflate(R.layout.item_message_user, parent, false));
        } else {
            return new AiViewHolder(inflater.inflate(R.layout.item_message_ai, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(msg);
        } else {
            ((AiViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        ImageView ivImage;

        UserViewHolder(View v) {
            super(v);
            tvText = v.findViewById(R.id.tv_message);
            ivImage = v.findViewById(R.id.iv_image);
        }

        void bind(ChatMessage msg) {
            if (msg.hasImage()) {
                ivImage.setVisibility(View.VISIBLE);
                tvText.setVisibility(msg.getText().isEmpty() ? View.GONE : View.VISIBLE);
                Glide.with(itemView.getContext()).load(msg.getImagePath()).into(ivImage);
            } else {
                ivImage.setVisibility(View.GONE);
                tvText.setVisibility(View.VISIBLE);
            }
            tvText.setText(msg.getText());
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;

        AiViewHolder(View v) {
            super(v);
            tvText = v.findViewById(R.id.tv_message);
        }

        void bind(ChatMessage msg) {
            tvText.setText(msg.getText());
        }
    }
}
