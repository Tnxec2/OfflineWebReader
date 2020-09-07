package com.kontranik.offlinewebreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class WebViewActivity extends AppCompatActivity {

    public static final int STORAGE_PERMISSION_CODE = 101;
    public static final String INTENT_KEY_URL = "url";
    public static final String PREFIX_ARCHIVFILE = "archiv_";
    public static final String EXT_ARCHIVFILE = ".mht";

    private WebView webView;
    private FloatingActionButton saveButton;

    private ProgressDialog progDailog;

    private DatabaseAdapter adapter;

    private OfflinePage page;

    private MenuItem miUpdateArchive;

    // Sample WebViewClient in case it was needed...
    // See continueWhenLoaded() sample function for the best place to set it on our webView
    private class MyWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            progDailog.show();
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            progDailog.dismiss();
            Lt.d("Web page loaded: " + url);

            if ( page == null ) saveButton.show();
            if ( page != null && webView.getUrl().equals("file://" + page.getFilename()) ) webView.scrollTo(0, (int) page.getPosition() );
            miUpdateArchive.setEnabled(page != null && !webView.getUrl().startsWith("file://"));
            super.onPageFinished(view, url);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new DatabaseAdapter(this);

        progDailog = ProgressDialog.show(this, "Loading","Please wait...", true);
        progDailog.setCancelable(false);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(false);
        webView.getSettings().setUserAgentString("Android");
        webView.setWebViewClient(new MyWebClient());

        saveButton = findViewById(R.id.btn_Save);
        saveButton.hide();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveArchive();
            }
        });

        // test load url
        //webView.loadUrl("https://librusec.pro/");

        // test load archiv
        //testLoadArchiv("SavedArchive.mht");

        Bundle arguments = getIntent().getExtras();
        if(arguments!=null) {
            if ( arguments.containsKey(INTENT_KEY_URL)) {
                String url = arguments.get(INTENT_KEY_URL).toString();
                page = null;
                webView.loadUrl(url);
            } else if ( arguments.containsKey(OfflinePage.class.getSimpleName())) {
                page = (OfflinePage) arguments.getSerializable(OfflinePage.class.getSimpleName());
                loadPage(page);
            }
        }

    }

    @Override
    protected void onStop() {
        updatePosition();
        super.onStop();
    }

    private void loadPage(OfflinePage page) {
        if ( page.getFilename() != null) {
            String fileName = page.getFilename();

            File f = new File(fileName);
            if (!f.exists()) {
                Toast.makeText(WebViewActivity.this,
                        "Archive file not exist. Load Origin Url",
                        Toast.LENGTH_SHORT)
                        .show();
                webView.loadUrl(page.getOrigin());
            } else {
                webView.loadUrl("file://" + f.getAbsolutePath());
            }
        } else {
            Toast.makeText(WebViewActivity.this,
                    "Filename is empty",
                    Toast.LENGTH_LONG)
                    .show();
        }

    }

    public void saveArchive(){
        if ( page != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Page update");
            alertDialogBuilder.setPositiveButton("Update",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            updateArchive();
                        }
                    });
            alertDialogBuilder.setNegativeButton("Save new",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            saveNewPage();
                        }
                    });
            alertDialogBuilder.create();
        } else {
            saveNewPage();
        }
    }

    private void updateArchive() {
        page.setOrigin(webView.getUrl());
        page.setName(webView.getTitle());
        page.setPosition(webView.getScrollY());
        saveDB(page);
        saveArchiveFile(page.getFilename());
        saveButton.hide();
    }

    private void updatePosition() {
        if ( page != null && page.getPosition() != webView.getScrollY() ) {
            adapter.open();
            page.setPosition(webView.getScrollY());
            adapter.update(page);
            adapter.close();
        }
    }

    private void saveNewPage() {
        String origin = webView.getUrl();
        String title = webView.getTitle();
        Date now = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");

        File dir = getFilesDir();
        String filename = dir.getAbsolutePath() + File.separator + PREFIX_ARCHIVFILE + df.format(now) + EXT_ARCHIVFILE;

        OfflinePage page = new OfflinePage(origin, title, filename, null, 0);
        saveDB(page);
        saveArchiveFile(filename);
        saveButton.hide();
    }

    private void saveDB(OfflinePage page) {
        adapter.open();
        if (page.getId() > 0) {
            adapter.update(page);
        } else {
            adapter.insert(page);
        }
        List<OfflinePage> pages = adapter.getPages();
        adapter.close();
    }

    public void saveArchiveFile(String fileName){
        Lt.d("Save archive");
        try {
            webView.saveWebArchive(fileName, false, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    if (value != null) {
                        Lt.d("save finish. " + value);
                        Toast.makeText(WebViewActivity.this,
                                "File saved. " + value,
                                Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Lt.d("saving the archive is failed");
                        Toast.makeText(WebViewActivity.this,
                                "saving the archive is failed.",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(WebViewActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(WebViewActivity.this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            Toast.makeText(WebViewActivity.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(WebViewActivity.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(WebViewActivity.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.webview_menu, menu);

        miUpdateArchive = menu.findItem(R.id.action_update_archive).setEnabled(false);
        miUpdateArchive.setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_update_archive :
                updateArchive();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
