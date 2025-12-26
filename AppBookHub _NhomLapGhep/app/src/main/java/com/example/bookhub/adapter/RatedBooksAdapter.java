package com.example.bookhub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bookhub.R;
import com.example.bookhub.models.UserReview;
import java.util.List;

public class RatedBooksAdapter extends RecyclerView.Adapter<RatedBooksAdapter.ViewHolder> {

    private Context context;
    private List<UserReview> list;

    public RatedBooksAdapter(Context context, List<UserReview> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rated_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserReview item = list.get(position);
        holder.tvTitle.setText(item.getBookTitle());
        holder.ratingBar.setRating(item.getRating());
        holder.tvComment.setText(item.getComment());
        holder.tvDate.setText(item.getDate());

        Glide.with(context).load(item.getImage()).placeholder(R.drawable.ic_menu_book_round).into(holder.imgCover);
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvComment, tvDate;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgRatedCover);
            tvTitle = itemView.findViewById(R.id.tvRatedTitle);
            tvComment = itemView.findViewById(R.id.tvRatedComment);
            tvDate = itemView.findViewById(R.id.tvRatedDate);
            ratingBar = itemView.findViewById(R.id.rbUserRating);
        }
    }
}