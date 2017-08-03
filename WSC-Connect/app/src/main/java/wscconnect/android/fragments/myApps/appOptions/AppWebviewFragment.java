package wscconnect.android.fragments.myApps.appOptions;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import wscconnect.android.R;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.models.AccessTokenModel;

/**
 * Created by chris on 18.07.17.
 */

public class AppWebviewFragment extends Fragment {
    private MainActivity activity;
    private AccessTokenModel token;
    private WebView webview;
    private String webviewUrl;
    private SwipeRefreshLayout refreshView;

    public AppWebviewFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        token = getArguments().getParcelable(AccessTokenModel.EXTRA);
        webviewUrl = getArguments().getString("webviewUrl");

        activity = (MainActivity) getActivity();
        prepareWebview();
    }

    public void prepareWebview() {
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
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
        webview.loadUrl((webviewUrl == null) ? token.getAppUrl() : webviewUrl);
    }

    public boolean goBackWebview() {
        if (webview != null && webview.canGoBack()) {
            webview.goBack();
            return true;
        }

        return false;
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

        webview = (WebView) view.findViewById(R.id.fragment_app_webview_webview);
        refreshView = (SwipeRefreshLayout) view.findViewById(R.id.fragment_app_webview_refresh);

        return view;
    }

    public void setUrl(String webviewUrl) {
        this.webviewUrl = webviewUrl;
    }
}
