package com.example.wenlingyang.cs571_hw9_stocksearch;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wenlingyang on 11/24/17.
 */

public class tab2historical extends Fragment {
    private static final String DEBUG_TAG = "tab2historicalPart";
    private View rootView;
    private WebView webView;
    private ProgressBar progressbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment2_historical, container, false);
        webView = (WebView) rootView.findViewById(R.id.stockView);
        /********************* Progress Bar setting **********************/
        progressbar = (ProgressBar) rootView.findViewById(R.id.pro_bar);
        if(!StockResultsActivity.alldata.containsKey("allStockValue")) {
            //draw loading icon
            progressbar.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }
        else {
            progressbar.setVisibility(View.GONE);
            plotHistoricalChart();
        }

        return rootView;
    }
    public void updateChartsView() {
        progressbar.setVisibility(View.GONE);
        plotHistoricalChart();
    }

    private void plotHistoricalChart() {

        // data got from AWS, can plot associative highCharts
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("file:///android_asset/price_chart.html");
        //if(available == true) {
            //plot historical highChart
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    JSONObject objJSON = new JSONObject();
                    String historicaldata = StockResultsActivity.alldata.get("allStockValue");
                    try{
                        JsonParser parser = new JsonParser();
                        JsonObject historical_json = parser.parse(historicaldata).getAsJsonObject();
                        JSONObject resp = new JSONObject(historical_json.toString());
                        objJSON.put("symbol", StockResultsActivity.symbol);
                        objJSON.put("data", resp);
                        webView.loadUrl("javascript:plotHistoricalChart('" + objJSON + "')");
                    } catch (JSONException err) {
                        Log.d(DEBUG_TAG, "price json error");
                    }
                }
            });
        //}
        webView.setVisibility(View.VISIBLE);
    }

}
