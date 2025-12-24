package com.example.bookhub.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.bookhub.R;
import com.example.bookhub.model.UserRank;
import java.util.List;

public class UserRankAdapter extends RecyclerView.Adapter<UserRankAdapter.ViewHolder> {
    private List<UserRank> users;
    private Context context;

    public UserRankAdapter(Context context, List<UserRank> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng layout mới
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_rank, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserRank user = users.get(position);
        int rank = position + 1; // Thứ hạng bắt đầu từ 1

        holder.name.setText(user.getName());
        holder.count.setText(user.getBorrowCount() + " sách đã mượn");
        holder.rankNumber.setText(String.valueOf(rank));
        holder.badge.setText("Top " + rank);

        // Load ảnh Avatar
        String avatarUrl = user.getAvatar();
        if (avatarUrl != null && !avatarUrl.startsWith("http")) {
            avatarUrl = "http://10.0.2.2:5280/images/" + avatarUrl; // Sửa cổng API của bạn
        }

        Glide.with(context)
                .load(avatarUrl)
                .transform(new CircleCrop())
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(holder.avatar);

        // Đổi màu badge cho đẹp (Top 1 màu khác, Top 2,3 màu khác)
        // Đây là code ví dụ đổi màu nền badge bằng code java nếu muốn
        // holder.badge.setBackgroundResource(rank == 1 ? R.drawable.bg_badge_purple : R.drawable.bg_badge_blue);
    }

    @Override
    public int getItemCount() { return users.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name, count, rankNumber, badge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.img_user_avatar);
            name = itemView.findViewById(R.id.tv_user_name);
            count = itemView.findViewById(R.id.tv_borrow_count);
            rankNumber = itemView.findViewById(R.id.tv_rank_number);
            badge = itemView.findViewById(R.id.tv_top_badge);
        }
    }
}