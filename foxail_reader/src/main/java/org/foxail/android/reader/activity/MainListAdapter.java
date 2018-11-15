package org.foxail.android.reader.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.foxail.android.reader.R;
import org.foxail.android.reader.model.News;

import java.util.List;

public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.MainListItemHolder> {
    private Context context;
    private List<News> newsList;

    public MainListAdapter(Context context, List<News> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @Override
    public MainListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.mainlist_item,
                parent, false);
        MainListAdapter.MainListItemHolder holder = new MainListAdapter.MainListItemHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MainListItemHolder itemHolder, int position) {
        News news = newsList.get(position);
        itemHolder.bind(news);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void clearItems() {
        newsList.clear();
    }

    public void addItems(List<News> items) {
        if(newsList == null || newsList.isEmpty()) {
            newsList = items;
            notifyDataSetChanged();
        } else {
            int positionStart = newsList.size();
            newsList.addAll(items);
            notifyItemRangeInserted(positionStart, items.size());
        }
    }

    public class MainListItemHolder extends RecyclerView.ViewHolder {
        TextView itemId;
        TextView itemTitle;

        public MainListItemHolder(@NonNull View itemView) {
            super(itemView);
            itemId = itemView.findViewById(R.id.mainlist_item_id);
            itemTitle = itemView.findViewById(R.id.mainlist_item_title);
        }

        public void bind(final News news) {
            itemId.setText(news.getId());
            itemTitle.setText(news.getTitle());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), NewsActivity.class);
                    intent.putExtra("news", news);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}
