package com.architectica.socialcomponents.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.model.Article;
import com.architectica.socialcomponents.utils.NewsUtils;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {

    private List<Article> articles;
    private Context context;

    public NewsAdapter(List<Article> articles, Context context) {
        this.articles = articles;
        this.context = context;
    }

    @NonNull
    @Override
    public NewsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.item,viewGroup,false);
        return new NewsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.MyViewHolder viewHolder, int i) {

        //Picasso.with(context).load(articles.get(i).getUrlToImage()).into(viewHolder.imageView);

        Glide.with(context).load(articles.get(i).getUrlToImage()).into(viewHolder.imageView);

        viewHolder.title.setText(articles.get(i).getTitle());
        viewHolder.desc.setText(articles.get(i).getDescription());
        viewHolder.source.setText(articles.get(i).getSource().getName());
        viewHolder.time.setText(" \u2022 " + NewsUtils.DateToTimeFormat(articles.get(i).getPublishedAt()));
        viewHolder.published_at.setText(NewsUtils.DateFormat(articles.get(i).getPublishedAt()));
        viewHolder.author.setText(articles.get(i).getAuthor());

        viewHolder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView title,desc,author,published_at,source,time;
        ImageView imageView;
        ProgressBar progressBar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            author = itemView.findViewById(R.id.author);
            published_at = itemView.findViewById(R.id.publishedAt);
            source = itemView.findViewById(R.id.source);
            time = itemView.findViewById(R.id.time);

            imageView = itemView.findViewById(R.id.img);
            progressBar = itemView.findViewById(R.id.progress_load_photo);

        }
    }

}
