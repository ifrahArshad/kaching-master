package com.devclear.kaching;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> implements Filterable {
    private ArrayList<SearchItem> mUserList;
    private ArrayList<SearchItem> mUserListFull;
    private OnItemClickedListener mListener;

    public interface OnItemClickedListener {
        void onItemClick(int position);
    }

    public void setOnItemClickedListener(OnItemClickedListener listener) {
        mListener = listener;
    }

    public static class SearchViewHolder extends  RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;

        public SearchViewHolder(@NonNull View itemView, final OnItemClickedListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.card_imageView);
            mTextView1 = itemView.findViewById(R.id.card_line1);
            mTextView2 = itemView.findViewById(R.id.card_line2);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION)
                            listener.onItemClick(position);
                    }
                }
            });
        }
    }

    public SearchAdapter(ArrayList<SearchItem> userList) {
        mUserList = userList;
        mUserListFull = new ArrayList<>(mUserList);
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        SearchViewHolder svh = new SearchViewHolder(v, mListener);
        return svh;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchItem currentItem = mUserList.get(position);

        if(currentItem.getImageResource() != null)
            Glide.with(holder.itemView.getContext()).load(currentItem.getImageResource()).into(holder.mImageView);

        holder.mTextView1.setText(currentItem.getUsername());
        holder.mTextView2.setText(currentItem.getTags());
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    public Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<SearchItem> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length() == 0) {
                filteredList.addAll(mUserListFull);
            }
            else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(SearchItem item : mUserListFull) {
                    if(item.getUsername().toLowerCase().contains(filterPattern)
                            || item.getTags().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mUserList.clear();
            mUserList.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };
}
