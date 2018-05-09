package com.example.wenlingyang.cs571_hw9_stocksearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.View.OnClickListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wenlingyang on 11/24/17.
 */

public class tab1current extends Fragment implements View.OnClickListener {
    private static final String DEBUG_TAG = "tab1currentPart";
    private ListView stock_list;
    private String price_data;
    private View rootView;
    private WebView webView;
    private JsonObject price_json;
    private Spinner spinner;
    private Button change_btn;
    private View favorites_btn;
    private View fb_btn;
    private HashMap<String, String> info_map = new HashMap <String, String> ();
    private HashMap<String, String> highcharts_options = new HashMap <String, String> ();
    private String lastPriceStr;
    private String changeStr;
    private ImageView starimg;
    private String current_chart = "";
    private ProgressBar list_spinner;
    //private ProgressBar chart_spinner;
    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d(DEBUG_TAG, "Facebook Dialog Callback success");
            Log.d(DEBUG_TAG, "Post id: " + result.getPostId());
            Toast.makeText(getContext(), "Shared successfully!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Log.d(DEBUG_TAG, "Facebook Dialog Callback cancel");
            Toast.makeText(getContext(), "Shared cancelled!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(FacebookException error) {
            Log.d(DEBUG_TAG, "Facebook Dialog Callback ERROR");
            Toast.makeText(getContext(), "Post NOT shared", Toast.LENGTH_SHORT).show();
        }
    };

    private CallbackManager callbackManager;
    //create data source
    String [] left_column = {"Stock Symbol", "Last Price", "Change", "Timestamp", "Open", "Close", "Day's Range", "Volume"};
    ArrayList<String> right_column = new ArrayList<>();
    Integer[] imgid = {R.drawable.up, R.drawable.down};
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment1_results, container, false);
        stock_list = rootView.findViewById(R.id.listview_stock);
        webView = (WebView) rootView.findViewById(R.id.chartsView);


        /********************* indicator_map setting **********************/
        info_map.put("Price", "Time Series (Daily)");
        info_map.put("SMA", "Technical Analysis: SMA&Simple Moving Average (SMA)");
        info_map.put("EMA", "Technical Analysis: EMA&Exponential Moving Average (EMA)");
        info_map.put("STOCH","Technical Analysis: STOCH&Stochastic Oscillator (STOCH)");
        info_map.put("RSI","Technical Analysis: RSI&Relative Strength Index (RSI)");
        info_map.put("ADX","Technical Analysis: ADX&Average Directional movement indeX (ADX)");
        info_map.put("CCI","Technical Analysis: CCI&Commodity Channel Index (CCI)");
        info_map.put("BBANDS","Technical Analysis: BBANDS&Bollinger Bands (BBANDS)");
        info_map.put("MACD","Technical Analysis: MACD&Moving Average Convergence/Divergence (MACD)");

        /********************* favorite button setting **********************/
        favorites_btn = rootView.findViewById(R.id.favorites_btn);
        favorites_btn.setOnClickListener(this);

        if(FavoritesStorage.symbolMap.containsKey(StockResultsActivity.symbol)) {
            //change star to fill
            changeFavoriteIcon(false);
        }

        /********************* fb button setting **********************/
        fb_btn = rootView.findViewById(R.id.fb_btn);
        fb_btn.setOnClickListener(this);

        /********************* indicator spinner setting **********************/
        spinner = (Spinner) rootView.findViewById(R.id.indicator_spinner);
        // Spinner click listener
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String indicator = parent.getItemAtPosition(position).toString();
                if(!current_chart.isEmpty()) {
                    if(indicator.equals(current_chart)) {
                        change_btn.setEnabled(false);
                    }
                    else {
                        change_btn.setEnabled(true);
                    }
                }
                else {
                    change_btn.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /********************* change button setting **********************/
        change_btn = (Button) rootView.findViewById(R.id.change_btn);
        change_btn.setOnClickListener(this);

        /********************* Progress Bar setting **********************/
        list_spinner = (ProgressBar) rootView.findViewById(R.id.list_proBar);
        //chart_spinner = (ProgressBar) rootView.findViewById(R.id.chart_proBar);

        /* ---------------- STOCK LIST PART ------------------- */
        //if(!StockResultsActivity.alldata.containsKey(info_map.get("Price")) || !available.get("Price")) {
        if(!StockResultsActivity.alldata.containsKey(info_map.get("Price"))) {
            //draw loading icon
            list_spinner.setVisibility(View.VISIBLE);
        }
        else {
            list_spinner.setVisibility(View.GONE);
            //draw list view
            if(right_column.isEmpty()) {
                showStocklist(StockResultsActivity.alldata.get("Time Series (Daily)"));
            }
            else {
                CustomStockListview stock_customlist = new CustomStockListview(getActivity(), left_column, right_column, imgid);
                stock_list.setAdapter(stock_customlist);
            }
        }

        /********************* Facebook SDK setting **********************/
        FacebookSdk.sdkInitialize(getContext());
        callbackManager = CallbackManager.Factory.create();

        return rootView;
    }

    public void updateListView() {
        list_spinner.setVisibility(View.GONE);
        showStocklist(StockResultsActivity.alldata.get("Time Series (Daily)"));
    }
    public void updateChartsView(String index) {
        //available.put(index, true); //modify its avaiable state
        //parseChartsData(index);
    }

    @SuppressLint("JavascriptInterface")
    private void showStocklist(String data) {
        //hide progress bar
        price_data = data;
        Log.d(DEBUG_TAG, "parse price data...");
        try {
            Gson g = new Gson();
            JsonParser parser = new JsonParser();
            price_json = parser.parse(data).getAsJsonObject();
            ArrayList <String> date = new ArrayList<>(), open_stock = new ArrayList<>(), high = new ArrayList<>(), low = new ArrayList<>(), close = new ArrayList<>(), volume=new ArrayList<>();
            for(Map.Entry<String, JsonElement> item : price_json.entrySet()) {
                date.add(item.getKey());
                open_stock.add(g.fromJson(item.getValue().getAsJsonObject().get("1. open"), String.class));
                high.add(g.fromJson(item.getValue().getAsJsonObject().get("2. high"), String.class));
                low.add(g.fromJson(item.getValue().getAsJsonObject().get("3. low"), String.class));
                close.add(g.fromJson(item.getValue().getAsJsonObject().get("4. close"), String.class));
                volume.add(g.fromJson(item.getValue().getAsJsonObject().get("5. volume"), String.class));
            }

            //var last_refresh = Object.keys(resp)[131];

            //modify the table content
            double prev_close = Double.parseDouble(close.get(close.size() - 2));
            //today's info
            double cur_open = Double.parseDouble(open_stock.get(open_stock.size() - 1));
            double cur_close = Double.parseDouble(close.get(close.size() - 1));
            double cur_low = Double.parseDouble(low.get(low.size() - 1));
            double cur_high = Double.parseDouble(high.get(high.size() - 1));
            int cur_vol = Integer.parseInt(volume.get(volume.size() - 1));
            double change = cur_close - prev_close;
            double change_percent = 100 * change/prev_close;

            lastPriceStr = String.format("%.2f", cur_close);
            changeStr = String.format("%.2f", change) + " (" + String.format("%.2f", change_percent) + "%)";
            String openStr = String.format("%.2f", cur_open);
            String closeStr = String.format("%.2f", prev_close);
            String rangeStr =  String.format("%.2f", cur_low) + " - " +  String.format("%.2f", cur_high);
            String volumeStr =  Integer.toString(cur_vol);
            right_column.add(StockResultsActivity.symbol);
            right_column.add(lastPriceStr);
            right_column.add(changeStr);
            right_column.add("TODO!!");
            right_column.add(openStr);
            right_column.add(closeStr);
            right_column.add(rangeStr);
            right_column.add(volumeStr);

            CustomStockListview stock_customlist = new CustomStockListview(getActivity(),left_column,right_column,imgid);
            stock_list.setAdapter(stock_customlist);

        } catch (JsonParseException err) {
            Log.d(DEBUG_TAG, "parsing price data error");
        }
    }


    private void plotCharts() {
        Toast.makeText(getContext(),
                "change button clicked: " +
                        "\nindicator: "+ String.valueOf(spinner.getSelectedItem()),
                Toast.LENGTH_SHORT).show();

        String indicator = String.valueOf(spinner.getSelectedItem());
        //if(StockResultsActivity.alldata.containsKey(info_map.get(indicator)) == true || available.get(indicator) == true) {
        HashMap<String,String> test = StockResultsActivity.alldata;
        Boolean isready;
        if(indicator.equals("Price")) {
            isready = StockResultsActivity.alldata.containsKey(info_map.get("Price"));
        }
        else {
            String mapval = info_map.get(indicator);
            isready = StockResultsActivity.alldata.containsKey(mapval.substring(0, mapval.indexOf('&')));
        }
        if(isready) {
            //hide progress bar
            //chart_spinner.setVisibility(View.GONE);
            //webView.setVisibility(View.VISIBLE);   //TODO

            // data got from AWS, can plot associative highCharts
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebChromeClient(new WebChromeClient());
            webView.loadUrl("file:///android_asset/price_chart.html");
            //if(indicator.equals("Price") && available.get("Price") == true) {
            if(indicator.equals("Price")) {
                //plot price highChart
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        JSONObject objJSON = new JSONObject();
                        try{
                            JSONObject resp = new JSONObject(price_json.toString());
                            objJSON.put("symbol", StockResultsActivity.symbol);
                            objJSON.put("data", resp);
                            webView.loadUrl("javascript:plotPriceChart('" + objJSON + "')");

                            // retrieve options
                            webView.evaluateJavascript("javascript:plotPriceChart('" + objJSON + "')", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String options) {
                                    Log.d(DEBUG_TAG, "highcharts options: "+options); // Returns the value from the function
                                    highcharts_options.put("Price", options);
                                }
                            });
                        } catch (JSONException err) {
                            Log.d(DEBUG_TAG, "price json error");
                        }
                    }
                });

            }
            else {
                //plot API highCharts
                webView.setWebViewClient(new WebViewClient() {
                    JSONObject objJSON = new JSONObject();
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        final String indicator = String.valueOf(spinner.getSelectedItem());
                        String mapval = info_map.get(indicator);
                        int pos = mapval.indexOf('&');
                        String key = mapval.substring(0,pos);
                        String title = mapval.substring(pos+1, mapval.length());
                        String apidata = StockResultsActivity.alldata.get(key);
                        //("MACD","Technical Analysis: MACD&Moving Average Convergence/Divergence (MACD)");
                        try{
                            JsonParser parser = new JsonParser();
                            JsonObject apidata_json = parser.parse(apidata).getAsJsonObject();
                            //symbol, indicator, title
                            JSONObject resp = new JSONObject(apidata_json.toString());
                            objJSON.put("symbol", StockResultsActivity.symbol);
                            objJSON.put("indicator", indicator);
                            objJSON.put("title", title);
                            objJSON.put("data", resp);
                            webView.loadUrl("javascript:plotApiChart('" + objJSON + "')");

                            // retrieve options
                            webView.evaluateJavascript("javascript:plotApiChart('" + objJSON + "')", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String options) {
                                    Log.d(DEBUG_TAG, "highcharts options: "+options); // Returns the value from the function
                                    highcharts_options.put(indicator, options);
                                }
                            });

                        } catch (JSONException err) {
                            Log.d(DEBUG_TAG, "price json error");
                        }
                    }
                });


            }
            change_btn.setEnabled(false);
            current_chart = indicator;
        }
        else { //TODO progress bar not show ?????????!!!!!!!!!!!!
            //otherwise, show loading icon
            //chart_spinner.setVisibility(View.VISIBLE);
            //webView.setVisibility(View.GONE);
        }
    }

    private void changeFavoriteIcon(Boolean isFav) {
        starimg = rootView.findViewById(R.id.favorites_btn);
        if(isFav) {
            starimg.setImageResource(R.drawable.empty);
        }
        else {
            starimg.setImageResource(R.drawable.filled);
        }
    }

    /**/
    private void starClicked() {

        JSONObject fv_json = new JSONObject();
        try {
            fv_json.put("symbol", StockResultsActivity.symbol);
            fv_json.put("price", lastPriceStr);
            fv_json.put("change", changeStr);
            String fvdata = fv_json.toString(); //encode into json string
            //Context ctxt = getContext();
            Context ctxt = MainActivity.ctxt;
            //change star img
            if(!FavoritesStorage.isFavorite(ctxt, fvdata)) {
                //add favorites
                FavoritesStorage.addFavoriteItem(ctxt, fvdata);
                changeFavoriteIcon(false);
                //write into listview TODO
            }
            else {
                //remove favorites
                FavoritesStorage.removeFavoriteItem(ctxt, fvdata);
                changeFavoriteIcon(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**/
    private void shareOnFacebook() {
        Log.d(DEBUG_TAG, "facebook share button is clicked");
        String cur_options = highcharts_options.get(current_chart);
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        String exportUrl = "http://export.highcharts.com/";
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("async", "true");
            jsonBody.put("type", "image/png");
            jsonBody.put("options", cur_options);
            final String requestBody = jsonBody.toString();

            StringRequest postRequest = new StringRequest(Request.Method.POST, exportUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("VOLLEY", "post request response is " + response);
                    ShareDialog dialog = new ShareDialog(getActivity());
                    dialog.registerCallback(callbackManager, shareCallback);
                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                .setContentUrl(Uri.parse("http://export.highcharts.com/"+ response))
                                .build();
                        dialog.setShouldFailOnDataError(true);
                        dialog.show(linkContent, ShareDialog.Mode.FEED);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

            };
            requestQueue.add(postRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.favorites_btn:
                starClicked();
                break;
            case R.id.fb_btn:
                shareOnFacebook();
                break;
            case R.id.change_btn:
                plotCharts();
        }
    }

}
