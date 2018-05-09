package com.example.wenlingyang.cs571_hw9_stocksearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by wenlingyang on 11/26/17.
 */

public class FavoritesStorage {
    private static final String DEBUG_TAG = "FavoriteStoragepart";
    private static final String KEY = "KEY";
    public static HashMap<String, String> symbolMap = new HashMap<>(); //local space to store, remember to change it when reload preferences !!!!

    public static String getFavoritesItems(Context ctxt) {
        SharedPreferences spre = PreferenceManager.getDefaultSharedPreferences(ctxt);
        return spre.getString(KEY, ""); //dummy
    }

    @NonNull
    public static Boolean isFavorite(Context ctxt, String content) {
        // parse content
        return symbolMap.containsKey(getSymbol(content));
    }

    public static void addFavoriteItem(Context ctxt, String content) {
        SharedPreferences spre = PreferenceManager.getDefaultSharedPreferences(ctxt);
        String data = spre.getString(KEY, "");
        data += content + "&";
        spre.edit().putString(KEY, data).apply();
        Log.d(DEBUG_TAG,"add favorite data result " + data);

        symbolMap.put(getSymbol(content), content);
    }

    public static void removeFavoriteItemBySymbol(Context ctxt, String symbol) {
        String rmdata = symbolMap.get(symbol);
        symbolMap.remove(symbol);

        String cur_storage = getFavoritesItems(ctxt);
        String newdata = cur_storage.replace(rmdata+"&", "");

        SharedPreferences spre = PreferenceManager.getDefaultSharedPreferences(ctxt);
        spre.edit().putString(KEY, newdata).apply();
        Log.d(DEBUG_TAG,"remove favorite by symbol data result: " + newdata);
    }

    public static void removeFavoriteItem(Context ctxt, String content) {
        String symbol = getSymbol(content);
        String rmdata = symbolMap.get(symbol);
        symbolMap.remove(symbol);


        String cur_storage = getFavoritesItems(ctxt);
        String newdata = cur_storage.replace(rmdata+"&", "");

        SharedPreferences spre = PreferenceManager.getDefaultSharedPreferences(ctxt);
        spre.edit().putString(KEY, newdata).apply();
        Log.d(DEBUG_TAG,"remove favorite data result: " + newdata);

    }

    private static String getSymbol(String json_str) {
        String symbol = "";
        try {
            JSONObject json = new JSONObject(json_str);
            symbol = (String) json.get("symbol");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return symbol;
    }

    public static void updateLocalMap(Context ctxt) {
        /*SharedPreferences spre = PreferenceManager.getDefaultSharedPreferences(ctxt);
        spre.edit().remove("KEY").commit();*/
        String local_data = getFavoritesItems(ctxt);
        //parse local data to update symbolMap
        if(!local_data.isEmpty()) {
            String[] arr = local_data.split("&");
            for(int i=0; i < arr.length; i++) {
                symbolMap.put(getSymbol(arr[i]), arr[i]);
            }
        }
    }
    public static void updateContent(Context ctxt, String content) {
        String symbol = getSymbol(content);
        String raplcedata = symbolMap.get(symbol);
        symbolMap.put(symbol, content);

        String cur_storage = getFavoritesItems(ctxt);
        String newdata = cur_storage.replace(raplcedata, content);
        SharedPreferences spre = PreferenceManager.getDefaultSharedPreferences(ctxt);
        spre.edit().putString(KEY, newdata).apply();
        Log.d(DEBUG_TAG,"update favorites data result: " + newdata);
    }

}
