package wscconnect.android.fragments.myApps.appOptions;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import wscconnect.android.R;
import wscconnect.android.listeners.OnBackPressedListener;
import wscconnect.android.listeners.OnFragmentUpdateListener;
import wscconnect.android.models.AccessTokenModel;

import static android.app.Activity.RESULT_OK;

/**
 * Created by chris on 18.07.17.
 */

public class AppWebviewFragment extends Fragment implements OnBackPressedListener, OnFragmentUpdateListener {
    public final static String USER_AGENT = "WSC-Connect Mobile Browser 1.0";
    public final static String URL = "webviewUrl";
    private static final int PICKFILE_REQUEST_CODE = 1337;
    private AccessTokenModel token;
    private WebView webview;
    private String webviewUrl;
    private SwipeRefreshLayout refreshView;
    private ValueCallback<Uri[]> mFilePathCallback;

    public AppWebviewFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            token = getArguments().getParcelable(AccessTokenModel.EXTRA);
            webviewUrl = getArguments().getString(URL);
        }

        prepareWebview();
    }

    public void prepareWebview() {
        final WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUserAgentString(USER_AGENT);
        webSettings.setAllowFileAccess(true);

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mFilePathCallback = filePathCallback;
                // Launch Intent for picking file
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                startActivityForResult(intent, PICKFILE_REQUEST_CODE);

                return true;
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                refreshView.setRefreshing(true);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                refreshView.setRefreshing(false);
            }
        });

        refreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webview.reload();
            }
        });

        loadWebview();
    }

    public void loadWebview() {
        refreshView.setRefreshing(true);
        webview.loadUrl(getWebViewUrl());
    }

    private String getWebViewUrl() {
        return (webviewUrl == null) ? token.getAppUrl() : webviewUrl;
    }

    public boolean goBackWebview() {
        if (webview != null && webview.canGoBack()) {
            webview.goBack();
            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {
        if (requestCode == PICKFILE_REQUEST_CODE && intent != null && resultCode == RESULT_OK) {
            if (intent.getData() != null) {
                //If uploaded with Android Gallery (max 1 image)
                Uri selectedFile = intent.getData();
                Uri[] uris = new Uri[1];
                uris[0] = selectedFile;
                mFilePathCallback.onReceiveValue(uris);
            } else {
                ClipData clipData = intent.getClipData();
                int count = clipData.getItemCount();
                Uri[] uris = new Uri[count];

                for (int i = 0; i < count; i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    uris[i] = item.getUri();
                }

                mFilePathCallback.onReceiveValue(uris);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (webview != null) {
            webview.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (webview != null) {
            webview.onPause();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_app_webview, container, false);

        webview = view.findViewById(R.id.fragment_app_webview_webview);
        refreshView = view.findViewById(R.id.fragment_app_webview_refresh);

        return view;
    }

    public void setUrl(String webviewUrl) {
        this.webviewUrl = webviewUrl;
    }

    @Override
    public boolean onBackPressed() {
        if (webview != null && webview.canGoBack()) {
            webview.goBack();
            return true;
        }

        return false;
    }

    @Override
    public void onUpdate(Bundle bundle) {
        String url = bundle.getString(URL);
        if (url != null) {
            setUrl(url);
            loadWebview();
        }
    }
}
