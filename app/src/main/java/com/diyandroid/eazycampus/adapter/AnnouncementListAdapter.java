package com.diyandroid.eazycampus.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.diyandroid.eazycampus.Notification;
import com.diyandroid.eazycampus.R;

import java.util.ArrayList;

public class AnnouncementListAdapter extends ArrayAdapter<Notification> {

    private static final String TAG = "AnnouncementListAdapter";
    private Context mContext;
    private int mResource;
    private ArrayList<Notification> announcementList;

    public AnnouncementListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Notification> list) {
        super(context, resource, list);
        mContext = context;
        mResource = resource;
        announcementList = list;
    }

    static class ViewHolder {
        TextView headerTextView, ann_message, ann_title, ann_ldate, ann_rdate;
        TextView ann_link1_title, ann_link2_title, ann_link3_title;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        position = getCount() - position - 1;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.headerTextView = convertView.findViewById(R.id.ann_monthtext);
            viewHolder.ann_link1_title = convertView.findViewById(R.id.ann_link1_title);
            viewHolder.ann_link2_title = convertView.findViewById(R.id.ann_link2_title);
            viewHolder.ann_link3_title = convertView.findViewById(R.id.ann_link3_title);

            viewHolder.ann_message = convertView.findViewById(R.id.ann_message);
            viewHolder.ann_title = convertView.findViewById(R.id.ann_title);
            viewHolder.ann_ldate = convertView.findViewById(R.id.ann_ldate);
            viewHolder.ann_rdate = convertView.findViewById(R.id.ann_rdate);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Notification notification = (Notification) getItem(position);

        if (notification.getLink1().length() != 0 && notification.getLink1().length() != 0 && notification.getLink3().length() != 0) {
            viewHolder.ann_link1_title.setText(notification.getLink1_title());
            viewHolder.ann_link2_title.setText(notification.getLink2_title());
            viewHolder.ann_link3_title.setText(notification.getLink3_title());
        } else if (notification.getLink1().length() != 0 && notification.getLink3().length() != 0) {
            viewHolder.ann_link1_title.setText(notification.getLink1_title());
            viewHolder.ann_link2_title.setText(notification.getLink2_title());
            viewHolder.ann_link3_title.setVisibility(View.GONE);
        } else if (notification.getLink1().length() != 0) {
            viewHolder.ann_link1_title.setText(notification.getLink1_title());
            viewHolder.ann_link2_title.setVisibility(View.GONE);
            viewHolder.ann_link3_title.setVisibility(View.GONE);
        } else {
            viewHolder.ann_link1_title.setVisibility(View.GONE);
            viewHolder.ann_link2_title.setVisibility(View.GONE);
            viewHolder.ann_link3_title.setVisibility(View.GONE);
        }

        // if not first item check if item above has the same header
        if (position < getCount() - 1 && announcementList.get(position + 1).getMonth_year().equals(notification.getMonth_year())) {
            viewHolder.headerTextView.setVisibility(View.GONE);
        } else {
            viewHolder.headerTextView.setText(notification.getMonth_year());
            viewHolder.headerTextView.setVisibility(View.VISIBLE);
        }

        if (notification.getMessage().length() == 0) {
            viewHolder.ann_message.setVisibility(View.GONE);
        } else {
            viewHolder.ann_message.setText(notification.getMessage().replaceAll("\\s{2,}", " ").trim());
        }

        viewHolder.ann_title.setText(notification.getTitle());
        viewHolder.ann_ldate.setText(notification.getTimestamp().substring(0, 2));
        viewHolder.ann_rdate.setText(notification.getDate());

        return convertView;
    }
}
