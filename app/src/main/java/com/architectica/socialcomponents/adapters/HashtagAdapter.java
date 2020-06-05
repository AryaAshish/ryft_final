package com.architectica.socialcomponents.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.model.hashtag;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.SocialArrayAdapter;

import androidx.annotation.NonNull;

public class HashtagAdapter extends SocialArrayAdapter<hashtag> {

    public HashtagAdapter(@NonNull Context context) {
        super(context, R.layout.hashtag_row, R.id.textViewName);
    }

    @Override
    public String convertToString(hashtag $receiver) {
        return $receiver.name;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.hashtag_row, parent, false);

            holder = new ViewHolder(convertView);

            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();

        }

        hashtag item = getItem(position);

        if (item != null) holder.textView.setText(item.name);

        return convertView;

    }

    private static class ViewHolder {

        TextView textView;

        ViewHolder(@NonNull View view) {

            textView = view.findViewById(R.id.textViewName);

        }

    }


}
