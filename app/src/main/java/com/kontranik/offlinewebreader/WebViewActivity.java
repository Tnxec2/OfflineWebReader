package com.kontranik.offlinewebreader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class WebViewActivity extends AppCompatActivity {

    public static final int STORAGE_PERMISSION_CODE = 101;
    public static final String INTENT_KEY_URL = "url";
    public static final String PREFIX_ARCHIVFILE = "archiv_";
    public static final String EXT_ARCHIVFILE = ".mht";

    private WebView myWebView;
    private FloatingActionButton saveButton;

    private ProgressDialog progDailog;

    private DatabaseAdapter adapter;

    private OfflinePage page;


    // Sample WebViewClient in case it was needed...
    // See continueWhenLoaded() sample function for the best place to set it on our myWebView
    private class MyWebClient extends WebViewClient {

        int color;

        MyWebClient(int color) {
            this.color = color;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
        {
            String url = myWebView.getUrl();
            if ( url.startsWith("http://")) {
                url = url.replace("http://", "https://");
                myWebView.loadUrl(url);
            } else {
                myWebView.loadUrl(String.format("https://www.google.de/search?q=%s", myWebView.getUrl()));
            }
        }

        @TargetApi(android.os.Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView inView, WebResourceRequest inReq, WebResourceError inError)
        {
            // do some stuff
            onReceivedError(inView, inError.getErrorCode(), inError.getDescription().toString(), inReq.getUrl().toString());
        }

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
            if ( page != null && myWebView.getUrl().equals("file://" + page.getFilename()) ) {
                myWebView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int positionY = calculateScrollPosition();
                        myWebView.scrollTo(0, positionY);
                    }
                    // Delay the scrollTo to make it work
                }, 300);
            }

            super.onPageFinished(myWebView, url);
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE);

        if ( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                updatePosition();
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);


        adapter = new DatabaseAdapter(this);

        progDailog = ProgressDialog.show(this, "Loading","Please wait...", true);
        progDailog.setCancelable(false);

        myWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = myWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(false);

        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);

        webSettings.setUserAgentString("Android");
        webSettings.setDefaultTextEncodingName("utf-8");

        myWebView.setWebViewClient(new MyWebClient( Color.rgb(229, 215, 204) ));

        saveButton = findViewById(R.id.btn_Save);
        saveButton.hide();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveArchive();
            }
        });

        // Get intent, action and MIME type
        Intent intent = getIntent();

        // Receive shared text from other Apps
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        }

        // test load url
        //myWebView.loadUrl("https://librusec.pro/");

        // test load archiv
        //testLoadArchiv("SavedArchive.mht");

        // Receive shared url or page object from MainActivity
        Bundle arguments = intent.getExtras();
        if(arguments!=null) {
            if ( arguments.containsKey(INTENT_KEY_URL)) {
                String url = arguments.get(INTENT_KEY_URL).toString();
                loadUrl(url);
            } else if ( arguments.containsKey(OfflinePage.class.getSimpleName())) {
                page = (OfflinePage) arguments.getSerializable(OfflinePage.class.getSimpleName());
                if ( page != null) {
                    loadPage(page);
                } else {
                    Toast.makeText(WebViewActivity.this,
                            "Can' get Page",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        }

    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Lt.d(sharedText);
            loadUrl(sharedText);
        }
    }

    @Override
    protected void onStop() {
        updatePosition();
        super.onStop();
    }

    private void loadUrl(String url) {
        page = null;
        if ( ! url.toLowerCase().startsWith("http")) {
            url = "http://" + url;
        }
        myWebView.loadUrl(url);
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
                myWebView.loadUrl(page.getOrigin());
            } else {
                myWebView.loadUrl("file://" + f.getAbsolutePath());
            }
        } else {
            Toast.makeText(WebViewActivity.this,
                    "Filename is empty",
                    Toast.LENGTH_LONG)
                    .show();
        }

    }

    /**
     * Calculate ScrollY of saved percent progress
     *
     */
    private int calculateScrollPosition() {
    /*
    float positionTopView = myWebView.getTop();
    float webviewsize = myWebView.getContentHeight() - positionTopView;
    float positionInWV = webviewsize * page.getPosition();
    int positionY = Math.round(positionTopView + positionInWV);
    */

        float contentHeight = myWebView.getContentHeight() * myWebView.getScaleY();
        float total = contentHeight * getResources().getDisplayMetrics().density - myWebView.getHeight();
        return Math.round(page.getPosition() * (total - getResources().getDisplayMetrics().density));
    }

    /**
     * Calculate the % of scroll progress in the actual web page content
      */
    private float calculateProgression() {

        /*
        float positionTopView = myWebView.getTop();
        float contentHeight = myWebView.getContentHeight();
        float currentScrollPosition = myWebView.getScrollY();
        float percentWebview = (currentScrollPosition - positionTopView) / contentHeight;
        */

        float contentHeight = myWebView.getContentHeight() * myWebView.getScaleY();
        float total = contentHeight * getResources().getDisplayMetrics().density - myWebView.getHeight();
        float percent = Math.min(myWebView.getScrollY() / (total - getResources().getDisplayMetrics().density), 1);

        Log.d("SCROLL", "Percentage: " + percent);

        return percent;
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
            alertDialogBuilder.show();
        } else {
            saveNewPage();
        }
    }

    private void updateArchive() {
        page.setOrigin(myWebView.getUrl());
        page.setName(myWebView.getTitle());
        page.setPosition(calculateProgression());
        saveDB(page);
        saveArchiveFile(page.getFilename());
        saveButton.hide();
    }

    private void updatePosition() {
        if ( page != null && page.getPosition() != calculateProgression() ) {
            adapter.open();
            page.setPosition(calculateProgression());
            adapter.update(page);
            adapter.close();
        }
    }

    private void saveNewPage() {
        String origin = myWebView.getUrl();
        String title = myWebView.getTitle();
        Date now = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");

        File dir = getFilesDir();
        String filename = dir.getAbsolutePath() + File.separator + PREFIX_ARCHIVFILE + df.format(now) + EXT_ARCHIVFILE;

        page = new OfflinePage(origin, title, filename, null, 0);
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
            myWebView.saveWebArchive(fileName, false, new ValueCallback<String>() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webview_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_go_back).setEnabled(myWebView.canGoBack());
        Lt.d(myWebView.getUrl());
        menu.findItem(R.id.action_update_archive).setEnabled(page != null && ! myWebView.getUrl().startsWith("file://") && ! page.getOrigin().equals(myWebView.getUrl()));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_go_back :
                myWebView.goBack();
                return true;
            case R.id.action_update_archive :
                //updateArchive();
                saveArchive();
                return true;
            case android.R.id.home:
                updatePosition();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
