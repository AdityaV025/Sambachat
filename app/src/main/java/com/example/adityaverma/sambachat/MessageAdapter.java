package com.example.adityaverma.sambachat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;

    MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout ,parent, false);

        return new MessageViewHolder(v);

    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        ImageView mReceivedImg;
        private ImageView mSentImg;

        TextView messageText2;

        MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            messageText.setVisibility(View.GONE);

            messageText2 = (TextView) view.findViewById(R.id.message_text_layout_2);
            messageText2.setVisibility(View.GONE);

            mReceivedImg = (ImageView) view.findViewById(R.id.received_img);
            mReceivedImg.setVisibility(View.GONE);

            mSentImg = (ImageView) view.findViewById(R.id.sent_img);
            mSentImg.setVisibility(View.GONE);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder viewHolder, int i) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String mCurrentUserId = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(i);
        String from_user_id = c.getFrom();

        if (from_user_id.equals(mCurrentUserId)){

            viewHolder.messageText2.setBackgroundResource(R.drawable.gradient_text_background);
            viewHolder.messageText2.setTextColor(Color.WHITE);

        }else {

            viewHolder.messageText.setBackgroundResource(R.drawable.text_message_background);
            viewHolder.messageText.setTextColor(Color.BLACK);

        }

        if (from_user_id.equals(mCurrentUserId)){
            viewHolder.messageText2.setVisibility(View.VISIBLE);
            viewHolder.messageText2.setText(c.getMessage());

        } else {
            viewHolder.messageText.setVisibility(View.VISIBLE);
            viewHolder.messageText.setText(c.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}
