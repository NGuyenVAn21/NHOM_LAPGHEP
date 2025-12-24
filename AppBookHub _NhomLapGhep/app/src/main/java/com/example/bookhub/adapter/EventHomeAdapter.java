package com.example.bookhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bookhub.R;
import com.example.bookhub.activity.EventDetailActivity;
import com.example.bookhub.model.Event;
import java.util.List;

public class EventHomeAdapter extends RecyclerView.Adapter<EventHomeAdapter.ViewHolder> {

    private List<Event> events;
    private Context context;

    public EventHomeAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.date.setText(event.getStartDate() + " - " + event.getEndDate());

        // Xử lý ảnh (Ghép link server)
        String imageUrl = event.getImageUrl();
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            imageUrl = "http://10.0.2.2:5280/images/" + imageUrl; // Thay 5177 bằng cổng máy bạn
        }

        Glide.with(context).load(imageUrl).placeholder(R.drawable.gradient_header).into(holder.image);

        // --- SỰ KIỆN CLICK: Mở trang chi tiết ---
        String finalImageUrl = imageUrl;
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            intent.putExtra("EVENT_TITLE", event.getTitle());
            intent.putExtra("EVENT_DESC", event.getDescription());
            intent.putExtra("EVENT_START", event.getStartDate());
            intent.putExtra("EVENT_END", event.getEndDate());
            intent.putExtra("EVENT_IMAGE", finalImageUrl);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return events.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_event_home);
            title = itemView.findViewById(R.id.tv_event_home_title);
            date = itemView.findViewById(R.id.tv_event_home_date);
        }
    }
}