package com.example.mywhatsapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mywhatsapp.ChatDetail;
import com.example.mywhatsapp.Models.UsersDatabase;
import com.example.mywhatsapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder> {

    ArrayList <UsersDatabase> list;

    public UserAdapter(ArrayList<UsersDatabase> list, Context context) {
        this.list = list;
        this.context = context;
    }

    Context context;

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
        return new viewHolder(view);



    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
    UsersDatabase users = list.get(position);
    Picasso.get().load(users.getProfilepic()).placeholder(R.drawable.avatar3).into(holder.image);
    holder.userName.setText(users.getUserName());

    holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ChatDetail.class);
            intent.putExtra("prifilePic", users.getProfilepic());
            intent.putExtra("userId", users.getUserId());
            intent.putExtra("userName", users.getUserName());
            context.startActivity(intent);
        }
    });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView userName, LastMsg;


        public viewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.user_names);
            LastMsg = itemView.findViewById(R.id.last_msg);
        }
    }
}

