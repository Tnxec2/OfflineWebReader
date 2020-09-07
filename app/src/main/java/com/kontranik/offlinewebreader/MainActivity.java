package com.kontranik.offlinewebreader;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<OfflinePage> pages = new ArrayList<>();
    private ListView pageList;
    private OfflinePageAdapter pagesAdapter;

    private boolean fabExpanded = false;
    private FloatingActionButton fabAdd;
    private LinearLayout layoutFabClipboard;
    private LinearLayout layoutFabEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabAdd = (FloatingActionButton) this.findViewById(R.id.fabAdd);

        layoutFabClipboard = (LinearLayout) this.findViewById(R.id.layoutFabClipboard);
        layoutFabEdit = (LinearLayout) this.findViewById(R.id.layoutFabEdit);

        //When main Fab (Settings) is clicked, it expands if not expanded already.
        //Collapses if main FAB was open already.
        //This gives FAB (Settings) open/close behavior
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded == true){
                    closeSubMenusFab();
                } else {
                    openSubMenusFab();
                }
            }
        });

        FloatingActionButton fabClipboard = this.findViewById(R.id.fabClipboard);
        fabClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasteFromClipboard();
            }
        });

        FloatingActionButton fabEdit = this.findViewById(R.id.fabEdit);
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editAddress(null);
            }
        });

        //Only main FAB is visible in the beginning
        closeSubMenusFab();

        // получаем элемент ListView
        pageList = findViewById(R.id.offlinepageList);

        // создаем адаптер
        pagesAdapter = new OfflinePageAdapter(this, R.layout.offlinepage_listitem, pages);
        // устанавливаем адаптер
        pageList.setAdapter(pagesAdapter);
        // слушатель выбора в списке
        AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                OfflinePage selectedEntry = (OfflinePage) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Selected " + selectedEntry.getName(),
                        Toast.LENGTH_SHORT).show();
                loadPage(selectedEntry);
            }
        };
        pageList.setOnItemClickListener(itemListener);

    }


    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    //closes FAB submenus
    private void closeSubMenusFab(){
        layoutFabClipboard.setVisibility(View.INVISIBLE);
        layoutFabEdit.setVisibility(View.INVISIBLE);
        fabAdd.setImageResource(R.drawable.ic_add_black_24dp);
        fabExpanded = false;
    }

    //Opens FAB submenus
    private void openSubMenusFab(){
        layoutFabClipboard.setVisibility(View.VISIBLE);
        layoutFabEdit.setVisibility(View.VISIBLE);
        //Change settings icon to 'X' icon
        fabAdd.setImageResource(R.drawable.ic_close_black_24dp);
        fabExpanded = true;
    }

    private void loadData() {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
        dbAdapter.open();

        pages.clear();
        pages.addAll(dbAdapter.getPages());
        pagesAdapter.notifyDataSetChanged();
        dbAdapter.close();
    }

    private  void editAddress(String text) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_dialog, null);

        final EditText editText = (EditText) dialogView.findViewById(R.id.edtDialog_Address);
        if ( text != null) editText.setText(text);
        ImageButton btnSend = dialogView.findViewById(R.id.btnDialog_Send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadUrl(editText.getText().toString());
                dialogBuilder.dismiss();
            }
        });

        editText.setOnKeyListener(new View.OnKeyListener()   {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    loadUrl(editText.getText().toString());
                    dialogBuilder.dismiss();
                    return true;
                }
                return false;
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void loadUrl(String url) {
        Lt.d(url);
        closeSubMenusFab();
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_KEY_URL, url);
        startActivity(intent);
    }

    private void loadPage(OfflinePage page) {
        Lt.d(page.getOrigin());
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra( OfflinePage.class.getSimpleName(), page);
        startActivity(intent);
    }



    private void pasteFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String pasteData = "";

        // If it does contain data, decide if you can handle the data.
        if (!(clipboard.hasPrimaryClip())) {

        } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))) {

            // since the clipboard has data but it is not plain text

        } else {

            //since the clipboard contains plain text.
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

            // Gets the clipboard as text.
            pasteData = item.getText().toString();
            editAddress(pasteData);
        }
    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_delete_archive :
                deleteArchive();
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    }*/
}