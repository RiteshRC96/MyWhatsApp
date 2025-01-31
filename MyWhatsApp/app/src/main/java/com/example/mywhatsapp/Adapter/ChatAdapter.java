package com.example.mywhatsapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mywhatsapp.Models.MessageModel;
import com.example.mywhatsapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter {

    ArrayList<MessageModel> messageModels;
    Context context;
    int sender_view_type = 1;
    int receiver_view_type = 2;
    String recId;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, String recId) {
        this.messageModels = messageModels;
        this.context = context;
        this.recId = recId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == sender_view_type) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Ensure both FirebaseAuth UID and messageModel UID are not null
        if (messageModels.get(position).getuId() != null &&
                messageModels.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())) {
            return sender_view_type;
        } else {
            return receiver_view_type;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel = messageModels.get(position);

        if (holder instanceof SenderViewHolder) {
            ((SenderViewHolder) holder).senderMsg.setText(messageModel.getMessage());
            ((SenderViewHolder) holder).senderTime.setText(formatTime(messageModel.getTimestamp())); // Set time if needed
        } else {
            ((ReceiverViewHolder) holder).receiverMsg.setText(messageModel.getMessage());
            ((ReceiverViewHolder) holder).receiverTime.setText(formatTime(messageModel.getTimestamp())); // Set time if needed
        }
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    // ViewHolder for Sender
    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg, senderTime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.sender_text);
            senderTime = itemView.findViewById(R.id.senderTime);
        }
    }

    // ViewHolder for Receiver
    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiverMsg, receiverTime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTime);
        }
    }

    // Helper method to format the timestamp
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
