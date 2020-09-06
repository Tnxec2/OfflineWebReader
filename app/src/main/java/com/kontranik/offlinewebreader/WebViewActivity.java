package com.kontranik.offlinewebreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.*;


public class WebViewActivity extends AppCompatActivity {

    public static final int STORAGE_PERMISSION_CODE = 101;

    private WebView webView;
    private FloatingActionButton saveButton;

    Activity activity ;
    //private ProgressDialog progDailog;

    public float yPos;
    public float webViewHeight;
    public float scrollProzent;

    // Sample WebViewClient in case it was needed...
    // See continueWhenLoaded() sample function for the best place to set it on our webView
    private class MyWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //progDailog.show();
            view.loadUrl(url);

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            //progDailog.dismiss();
            Lt.d("Web page loaded: " + url);
            try {
                Thread.sleep(2000);
                saveButton.show();
                webView.scrollTo(0, 1000);
                super.onPageFinished(view, url);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        activity = this;

        checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE);

       // progDailog = ProgressDialog.show(activity, "Loading","Please wait...", true);
       // progDailog.setCancelable(false);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebViewClient(new MyWebClient());

        saveButton = findViewById(R.id.btn_Save);
        saveButton.hide();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveArchive("SavedArchive.mht");
            }
        });


        saveButton.hide();
        //webView.loadUrl("https://librusec.pro/");



        testLoadArchiv("SavedArchive.mht");
    }

    @Override
    protected void onStop() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("scroll", webView.getScrollY());
        editor.commit();
        super.onStop();
    }




    private void testLoadArchiv(String fileName) {


            File dir = getFilesDir();
            Lt.d("Read from: " + dir.getAbsolutePath() + File.separator + fileName);
            File f = new File(dir.getAbsolutePath() + File.separator + fileName);
            if (!f.exists()) {
                Toast.makeText(WebViewActivity.this,
                        "File not exist",
                        Toast.LENGTH_LONG)
                    .show();
            }

            webView.loadUrl("file://" + f.getAbsolutePath());
    }

    void continueWhenLoaded(WebView webView) {
        Lt.d("Page from WebArchive fully loaded.");
        // If you need to set your own WebViewClient, do it here,
        // after the WebArchive was fully loaded:
        webView.setWebViewClient(new MyWebClient());
        // Any other code we need to execute after loading a page from a WebArchive...
    }

    public void saveArchive(String fileName){
        Lt.d("Save archive");
        try {
            File dir = getFilesDir();

            Lt.d(dir.getAbsolutePath());

            webView.saveWebArchive(dir.getAbsolutePath() + File.separator + fileName);
            Lt.d("save finish. " + dir.getAbsolutePath() + File.separator + fileName);

        } catch (Exception e) {
            // TODO: handle exception
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

}
