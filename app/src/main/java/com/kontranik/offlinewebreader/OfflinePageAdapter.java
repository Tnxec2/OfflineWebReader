package com.kontranik.offlinewebreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder viewHolder;
        if(convertView==null){
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final OfflinePage entry = entrys.get(position);

        viewHolder.nameView.setText(entry.getName());
        viewHolder.originView.setText( entry.getOrigin());

        float pagePosition = entry.getPosition();
        viewHolder.positionView.setText( String.format(Locale.getDefault(),"%d %%", (int) (pagePosition * 100))  );

        byte[] imgByte = entry.getImage();
        if ( imgByte == null) {
            viewHolder.coverView.setImageDrawable( parent.getResources().getDrawable(R.drawable.ic_insert_drive_file_black_24dp));
        } else {
            viewHolder.coverView.setImageBitmap(BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length));
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm", Locale.getDefault());

        Long created = entry.getCreated();
        if ( created != null ) {
            viewHolder.createdView.setText(dateFormat.format(new Date(entry.getCreated())));
        }

        viewHolder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(getContext(), viewHolder.menuButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.pagelist_popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_open_archive_from_list:
                                openArchive(entry);
                                break;
                            case R.id.action_delete_archive_from_list:
                                deleteEntry(entry, position);
                                break;
                        }

                        return true;
                    }
                });

                popup.show();//showing popup menu
            }
        });//closing the setOnClickListener method

        viewHolder.menuButton.setFocusable(false);

        return convertView;
    }

    private class ViewHolder {
        final ImageView coverView;
        final ImageButton menuButton;
        final TextView nameView;
        final TextView originView;
        final TextView positionView;
        final TextView createdView;

        ViewHolder(View view){
            coverView = view.findViewById(R.id.cover);
            nameView = view.findViewById(R.id.name);
            originView = view.findViewById(R.id.origin);
            positionView = view.findViewById(R.id.position);
            createdView = view.findViewById(R.id.created);
            menuButton = view.findViewById(R.id.btnMenu);
        }
    }

    private void openArchive(OfflinePage entry) {
        Lt.d(entry.getOrigin());
        Intent intent = new Intent(getContext(), WebViewActivity.class);
        intent.putExtra( OfflinePage.class.getSimpleName(), entry);
        getContext().startActivity(intent);
    }

    private void deleteEntry(OfflinePage entry, int position) {
        entrys.remove(position);
        notifyDataSetChanged();
        DatabaseAdapter dbAdapter = new DatabaseAdapter(getContext());
        dbAdapter.open();
        dbAdapter.delete(entry.getId());
        dbAdapter.close();
    }
}
