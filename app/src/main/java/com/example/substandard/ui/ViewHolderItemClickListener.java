package com.example.substandard.ui;

/**
 * This UI uses many RecyclerViews, and each View in the ViewHolder is clickable. This
 * interface should be used to register a Fragment as a Listener in any Adapter class.
 * @param <T>
 */
public interface ViewHolderItemClickListener<T> {
    void onItemClick(T obj);
}
