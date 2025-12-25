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
import com.example.bookhub.models.Event;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;
    private Context context;

    public EventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        // ... (set text giữ nguyên) ...

        // --- SỬA ĐOẠN LOAD ẢNH ---
        String imageUrl = event.getImageUrl();

        if (imageUrl != null && !imageUrl.startsWith("http")) {
            imageUrl = "http://10.0.2.2:5280/images/" + imageUrl;
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.gradient_header)
                    .into(holder.banner);
        }

        // Lưu ý: Khi click vào sự kiện, cũng phải gửi link đầy đủ sang trang chi tiết
        String finalImageUrl = imageUrl; // Biến tạm để dùng trong lambda
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            intent.putExtra("EVENT_TITLE", event.getTitle());
            intent.putExtra("EVENT_DESC", event.getDescription());
            intent.putExtra("EVENT_START", event.getStartDate());
            intent.putExtra("EVENT_END", event.getEndDate());
            intent.putExtra("EVENT_IMAGE", finalImageUrl); // Gửi link đã ghép
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, desc;
        ImageView banner;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_event_title);
            date = itemView.findViewById(R.id.tv_event_date);
            desc = itemView.findViewById(R.id.tv_event_desc);
            banner = itemView.findViewById(R.id.img_event_banner);
        }
    }
}