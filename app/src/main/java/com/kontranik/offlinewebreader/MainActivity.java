package com.kontranik.offlinewebreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<OfflinePage> pages = new ArrayList<>();
    private ListView pageList;
    private OfflinePageAdapter pagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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

                // получаем выбранный пункт
                OfflinePage selectedState = (OfflinePage) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Был выбран пункт " + selectedState.getName(),
                        Toast.LENGTH_SHORT).show();
            }
        };
        pageList.setOnItemClickListener(itemListener);

        loadData();
    }


    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
        dbAdapter.open();

        pages.clear();
        pages.addAll(dbAdapter.getPages());
        pagesAdapter.notifyDataSetChanged();
        dbAdapter.close();
    }
}