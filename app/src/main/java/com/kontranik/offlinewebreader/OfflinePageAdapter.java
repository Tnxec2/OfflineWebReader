package com.kontranik.offlinewebreader;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OfflinePageAdapter extends ArrayAdapter<OfflinePage> {

        private LayoutInflater inflater;
        private int layout;
        private List<OfflinePage> entrys;

        public OfflinePageAdapter(Context context, int resource, List<OfflinePage> list) {
            super(context, resource, list);
            this.entrys = list;
            this.layout = resource;
            this.inflater = LayoutInflater.from(context);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View view=inflater.inflate(this.layout, parent, false);

            ImageView coverView = (ImageView) view.findViewById(R.id.cover);
            TextView nameView = (TextView) view.findViewById(R.id.name);
            TextView originView = (TextView) view.findViewById(R.id.origin);
            TextView positionView = (TextView) view.findViewById(R.id.position);
            TextView createdView = (TextView) view.findViewById(R.id.created);

            OfflinePage entry = entrys.get(position);

            byte[] imgByte = entry.getImage();
            if ( imgByte == null) {
                coverView.setImageDrawable( parent.getResources().getDrawable(R.drawable.file));
            } else {
                coverView.setImageBitmap(BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length));
            }
            nameView.setText(entry.getName());
            originView.setText(entry.getOrigin());

            Float pos = entry.getPosition();
            if ( pos != null ) {
                positionView.setText(String.format("%.0f", entry.getPosition()));
            } else {
                positionView.setText("0");
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm");

            Long created = entry.getCreated();
            if ( created != null ) {
                createdView.setText(dateFormat.format(new Date(entry.getCreated())));
            }

            return view;
        }
    }
