package com.example.wenlingyang.cs571_hw9_stocksearch;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by wenlingyang on 11/24/17.
 */

public class tab3news extends Fragment {
    private static final String DEBUG_TAG = "tab3newsPart";
    private Boolean available = false;
    private ListView news_list;
    private ProgressBar progressbar;
    private ArrayList<String> title = new ArrayList<>();
    private ArrayList<String> author = new ArrayList<>();
    private ArrayList<String> date = new ArrayList<>();
    private ArrayList<String> link = new ArrayList<>();
    private AdapterView.OnItemClickListener news_click;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment3_news, container, false);
        news_list = rootView.findViewById(R.id.listview_news);
        news_click = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String test = parent.getItemAtPosition(position).toString();
                //Log.d(DEBUG_TAG, "on favorite list, clicked on company is: " + company);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.get(position)));
                startActivity(browserIntent);
            }
        };

        /********************* Progress Bar setting **********************/
        progressbar = (ProgressBar) rootView.findViewById(R.id.pro_bar);
        if(!available) {
            //draw loading icon
            progressbar.setVisibility(View.VISIBLE);
            getNewsData("news");
        }
        else {
            progressbar.setVisibility(View.GONE);
            //show list view
            CustomNewsListview news_customlist = new CustomNewsListview(getActivity(), title, author, date);
            news_list.setAdapter(news_customlist);
            news_list.setOnItemClickListener(news_click);
        }

        return rootView;
    }

    private void getNewsData(String key) {
        //http://androidapp-env.us-east-2.elasticbeanstalk.com/updatedata?key=Time%20Series%20(Daily)&symbol=FB
        String url_getnewsdata = "http://androidapp-env.us-east-2.elasticbeanstalk.com/updatedata?key=" + Html.escapeHtml(key) + "&symbol=" + StockResultsActivity.symbol;
        new getNewsinfoRequest().execute(url_getnewsdata);
    }

    private class getNewsinfoRequest extends AsyncTask<String, Void, String> {
        private String key = "";

        @Override
        protected String doInBackground(String... urls) {
            try {
                // request info from AWS
                key = urls[0].substring(urls[0].indexOf('=')+1, urls[0].indexOf('&'));
                return interactOnAWS.httpRequest(urls[0]);
            } catch (IOException err) {
                Log.d(DEBUG_TAG, "get news http request error");
                return "http request error";
            }
        }
        @Override
        protected void onPostExecute(String resp) {
            Log.d(DEBUG_TAG, resp);
            if(resp.equals("data not ready yet")) {
                //if(req_times < 100) {
                Log.d(DEBUG_TAG, "Executed get request after 3000 ms!");
                //setTimeout
                basicTimer.setTimeout(new Runnable() {
                    @Override
                    public void run() {
                        //getApiData(key, req_times+1);
                        getNewsData(key);
                    }
                }, 3000);
                //}
                //else {
                //request too many times, need to show error messages
                //}
            }
            else {
                StockResultsActivity.alldata.put(key, resp);
                parseNewsData(resp);
                available = true;
            }
        }
    }
    private void parseNewsData(String data) {
        progressbar.setVisibility(View.GONE);
        try{
            //open_stock.add(g.fromJson(item.getValue().getAsJsonObject().get("1. open"), String.class));
            Gson g = new Gson();
            JsonParser parser = new JsonParser();
            JsonArray news_jsonarr = parser.parse(data).getAsJsonArray();
            for(int i=0; i<5 && news_jsonarr.get(i) != null; i++) {
                JsonObject news_ctn = news_jsonarr.get(i).getAsJsonObject();

                //String test = g.fromJson(news_ctn.get("title").getAsJsonArray().get(0),String.class);
                title.add(g.fromJson(news_ctn.get("title").getAsJsonArray().get(0),String.class));
                link.add(g.fromJson(news_ctn.get("link").getAsJsonArray().get(0), String.class));
                author.add("Author: " + g.fromJson(news_ctn.get("sa:author_name").getAsJsonArray().get(0), String.class));
                date.add("Date: " + g.fromJson(news_ctn.get("pubDate").getAsJsonArray().get(0), String.class));
            }
            CustomNewsListview news_customlist = new CustomNewsListview(getActivity(), title, author, date);
            news_list.setAdapter(news_customlist);
            news_list.setOnItemClickListener(news_click);
        } catch (JsonParseException err) {
            Log.d(DEBUG_TAG, "parsing news data error");
        }

    }
}
