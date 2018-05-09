package com.example.wenlingyang.cs571_hw9_stocksearch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_MESSAGE = "com.project.stocksearch.MESSAGE";
    public static Context ctxt;
    public static final String PRICE_MSG = "price_msg";
    public static final String API_MSG = "api_msg";
    public static final String HISTORICAL_MSG = "historical_msg";
    public static final String NEWS_MSG = "news_msg";
    private static final String DEBUG_TAG = "MainActivityPart";
    private View submit_btn;
    private View clear_btn;
    private View refresh_btn;
    private Spinner sort_spinner;
    private Spinner order_spinner;
    private AutoCompleteTextView autocomplete_text;
    private String input = "";
    private AsyncTask autoComplete_request = null;
    private ListView favorite_list;
    private CustomFavoriteListview favorite_customlist;
    private ArrayList<String> favorite_symbol = new ArrayList<>();
    private ArrayList<String> favorite_price = new ArrayList<>();
    private ArrayList<String> favorite_change = new ArrayList<>();
    private Switch refresh_switch;
    private basicTimer.TaskHandle refresh_handle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctxt = getApplicationContext();

        /********************* auto complete text view setting **********************/
        autocomplete_text = findViewById(R.id.autoComplete);
        // adapter will be set when text changed
        TextWatcher autocomplete_watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // request autocomplete data here
                Log.d(DEBUG_TAG, "start request auto complete information...");
                // Check a Device's Network Connection
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                //input validation
                String symbol = autocomplete_text.getText().toString().trim();
                if(networkInfo != null && networkInfo.isConnected() && !symbol.isEmpty()) {
                    //network is ok && input is valid, then start request
                    String url_autocomplete = "http://androidapp-env.us-east-2.elasticbeanstalk.com/autocomplete?input=" + symbol;
                    //Log.d(DEBUG_TAG, url_autocomplete);
                    autoComplete_request = new autocompleteRequest().execute(url_autocomplete);
                }
                else {
                    Log.d(DEBUG_TAG, "input symbol is invalid or network status is bad");
                    // if symbol is invalid  //TODO

                }
            }
        };
        AdapterView.OnItemClickListener autocomplete_click = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                String company = p.getItemAtPosition(pos).toString();
                Log.d(DEBUG_TAG, "clicked on company is" + company);
                autocomplete_text.setText(company);
                int position = company.length();
                Editable select_text = autocomplete_text.getEditableText();
                input = select_text.toString();
                Selection.setSelection(select_text, position);
            }
        };

        /*View.OnTouchListener touch_listener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (autocomplete_text.getText().toString().trim().length() != 0) { //TODO > 3
                    autocomplete_text.showDropDown();
                }
                return false;
            }
        };*/
        autocomplete_text.addTextChangedListener(autocomplete_watcher);
        autocomplete_text.setOnItemClickListener(autocomplete_click);
        //autocomplete_text.setOnTouchListener(touch_listener);


        /********************* get quote button setting **********************/
        submit_btn = findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(this);

        /********************* clear button setting **********************/
        clear_btn = findViewById(R.id.clear_btn);
        clear_btn.setOnClickListener(this);

        /********************* sortby spinner setting **********************/

        // Get reference of widgets from XML layout
        sort_spinner = (Spinner) findViewById(R.id.sort_spinner);

        ArrayList <String> sortbyList = new ArrayList<>();
        sortbyList.add("Sort By");
        sortbyList.add("Default");
        sortbyList.add("Symbol");
        sortbyList.add("Price");
        sortbyList.add("Change");
        sortbyList.add("Change Percent");

        // Initializing an ArrayAdapter
        ArrayAdapter<String> sortArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, sortbyList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0) {
                    // Disable the first item from Spinner
                    return false;
                }
                else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0) {
                    // Set the disable item text color
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        sortArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        sort_spinner.setAdapter(sortArrayAdapter);
        sort_spinner.setOnItemSelectedListener(spinner_listener);


        /********************* order spinner setting **********************/
        // Get reference of widgets from XML layout
        order_spinner = (Spinner) findViewById(R.id.order_spinner);

        ArrayList <String> orderList = new ArrayList<>();
        orderList.add("Order");
        orderList.add("Ascending");
        orderList.add("Descending");

        // Initializing an ArrayAdapter
        ArrayAdapter<String> orderArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, orderList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0) {
                    // Disable the first item from Spinner
                    return false;
                }
                else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0) {
                    // Set the disable item text color
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        orderArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        order_spinner.setAdapter(orderArrayAdapter);
        order_spinner.setOnItemSelectedListener(spinner_listener);



        //String order = String.valueOf(order_spinner.getSelectedItem());
        //String sort = String.valueOf(sort_spinner.getSelectedItem());

        /********************* favorite list view setting **********************/

        HashMap<String, String> fvmap = FavoritesStorage.symbolMap;
        if(!fvmap.isEmpty()) {
            drawFavoriteList(fvmap);
        }
        else {
            //local hashmap is empty, need to update it
            FavoritesStorage.updateLocalMap(ctxt);
            //draw them on favorite list and add listener
            fvmap = FavoritesStorage.symbolMap;
            if(!fvmap.isEmpty())
                drawFavoriteList(fvmap);
        }
        if(favorite_list != null)
            registerForContextMenu(favorite_list);


        /********************* refresh button setting **********************/
        refresh_btn = findViewById(R.id.refresh_btn);
        refresh_btn.setOnClickListener(this);

        /********************* autorefresh switch setting **********************/
        refresh_switch = findViewById(R.id.autorefresh_btn);
        refresh_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    // every 5s refreshList();
                    refresh_handle = basicTimer.setInterval(new Runnable() {
                        public void run() {
                            Log.d(DEBUG_TAG, "auto refresh executed every 5000 ms!");
                            refreshList();;
                        }
                    }, 5000);//TODO
                }
                else {
                    refresh_handle.invalidate();
                }
            }
        });
    }



    /******************************* helper classes and methods **************************************/

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listview_favorite) {
            menu.setHeaderTitle("Remove from Favorites?"); //TODO change header color
            menu.add(0, v.getId(), 0, "No");//groupId, itemId, order, title
            menu.add(0, v.getId(), 0, "Yes");

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals("No")) {
            //do nothing
        }
        else if(item.getTitle().equals("Yes")) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            String company = favorite_list.getItemAtPosition(info.position).toString();
            FavoritesStorage.removeFavoriteItemBySymbol(ctxt, company);
            int index = favorite_symbol.indexOf(company);
            favorite_symbol.remove(index);
            favorite_change.remove(index);
            favorite_price.remove(index);
            favorite_customlist.notifyDataSetChanged();
        }
        else {
            return false;
        }
        return true;

    }

    AdapterView.OnItemSelectedListener spinner_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(parent.getContext(),
                    "Selected: " + parent.getItemAtPosition(position).toString(),
                    Toast.LENGTH_SHORT).show();
            // sort favorite list
            String order = String.valueOf(order_spinner.getSelectedItem());
            String sort = String.valueOf(sort_spinner.getSelectedItem());
            Log.d(DEBUG_TAG, "list information: \n" + favorite_symbol + "\n" + favorite_price + "\n" + favorite_change);
            if(sort.equals("Sort By") || order.equals("Order") || favorite_symbol.isEmpty()) {
                return;
            }

            if(sort.equals("Symbol")) {
                HashMap<String, Integer> index = new HashMap<>();
                for(int i=0; i < favorite_symbol.size(); i++) {
                    index.put(favorite_symbol.get(i), i);
                }
                if(order.equals("Ascending")) {
                    Collections.sort(favorite_symbol);
                }
                else if(order.equals("Descending")) {
                    Collections.sort(favorite_symbol, new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            return o2.compareTo(o1);
                        }
                    });
                }
                ArrayList<String> tmp_price = new ArrayList<>(favorite_price);
                ArrayList<String> tmp_change = new ArrayList<>(favorite_change);
                for(int new_idx=0; new_idx < favorite_symbol.size(); new_idx++) {
                    int old_idx = index.get(favorite_symbol.get(new_idx));
                    if(new_idx != old_idx) {
                        favorite_price.set(new_idx, tmp_price.get(old_idx));
                        favorite_change.set(new_idx, tmp_change.get(old_idx));
                    }
                }
                favorite_customlist.notifyDataSetChanged();
            }
            else if(sort.equals("Price")) {
                HashMap<String, Integer> index = new HashMap<>();
                for(int i=0; i < favorite_price.size(); i++) {
                    index.put(favorite_price.get(i), i);
                }
                if(order.equals("Ascending")) {
                    Collections.sort(favorite_price, new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            Double oo1 = Double.parseDouble(o1);
                            Double oo2 = Double.parseDouble(o2);
                            return oo1.compareTo(oo2);
                        }
                    });
                }
                else if(order.equals("Descending")) {
                    Collections.sort(favorite_price, new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            Double oo1 = Double.parseDouble(o1);
                            Double oo2 = Double.parseDouble(o2);
                            return oo2.compareTo(oo1);
                        }
                    });
                }
                ArrayList<String> tmp_symbol = new ArrayList<>(favorite_symbol);
                ArrayList<String> tmp_change = new ArrayList<>(favorite_change);
                for(int new_idx=0; new_idx < favorite_price.size(); new_idx++) {
                    int old_idx = index.get(favorite_price.get(new_idx));
                    if(new_idx != old_idx) {
                        favorite_symbol.set(new_idx, tmp_symbol.get(old_idx));
                        favorite_change.set(new_idx, tmp_change.get(old_idx));
                    }
                }
                favorite_customlist.notifyDataSetChanged();
            }
            else if(sort.equals("Change")) {
                HashMap<String, Integer> index = new HashMap<>();
                for(int i=0; i < favorite_change.size(); i++) {
                    index.put(favorite_change.get(i), i);
                }
                if(order.equals("Ascending")) {
                    Collections.sort(favorite_change, new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            Double oo1 = Double.parseDouble(o1.substring(0, o1.indexOf("(")-1));
                            Double oo2 = Double.parseDouble(o2.substring(0, o2.indexOf("(")-1));
                            return oo1.compareTo(oo2);
                        }
                    });
                }
                else if(order.equals("Descending")) {
                    Collections.sort(favorite_change, new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            Double oo1 = Double.parseDouble(o1.substring(0, o1.indexOf("(")-1));
                            Double oo2 = Double.parseDouble(o2.substring(0, o2.indexOf("(")-1));
                            return oo2.compareTo(oo1);
                        }
                    });
                }
                ArrayList<String> tmp_symbol = new ArrayList<>(favorite_symbol);
                ArrayList<String> tmp_price = new ArrayList<>(favorite_price);
                for(int new_idx=0; new_idx < favorite_change.size(); new_idx++) {
                    int old_idx = index.get(favorite_change.get(new_idx));
                    if(new_idx != old_idx) {
                        favorite_symbol.set(new_idx, tmp_symbol.get(old_idx));
                        favorite_price.set(new_idx, tmp_price.get(old_idx));
                    }
                }
                favorite_customlist.notifyDataSetChanged();
            }
            else if(sort.equals("Change Percent")){
                HashMap<String, Integer> index = new HashMap<>();
                for(int i=0; i < favorite_change.size(); i++) {
                    index.put(favorite_change.get(i), i);
                }
                if(order.equals("Ascending")) {
                    Collections.sort(favorite_change, new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            Double oo1 = Double.parseDouble(o1.substring(o1.indexOf("(")+1, o1.indexOf("%")));
                            Double oo2 = Double.parseDouble(o2.substring(o2.indexOf("(")+1, o2.indexOf("%")));
                            return oo1.compareTo(oo2);
                        }
                    });
                }
                else if(order.equals("Descending")) {
                    Collections.sort(favorite_change, new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            Double oo1 = Double.parseDouble(o1.substring(o1.indexOf("(")+1, o1.indexOf("%")));
                            Double oo2 = Double.parseDouble(o2.substring(o2.indexOf("(")+1, o2.indexOf("%")));
                            return oo2.compareTo(oo1);
                        }
                    });
                }
                ArrayList<String> tmp_symbol = new ArrayList<>(favorite_symbol);
                ArrayList<String> tmp_price = new ArrayList<>(favorite_price);
                for(int new_idx=0; new_idx < favorite_change.size(); new_idx++) {
                    int old_idx = index.get(favorite_change.get(new_idx));
                    if(new_idx != old_idx) {
                        favorite_symbol.set(new_idx, tmp_symbol.get(old_idx));
                        favorite_price.set(new_idx, tmp_price.get(old_idx));
                    }
                }
                favorite_customlist.notifyDataSetChanged();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void drawFavoriteList(HashMap<String, String> fvmap) {
        // click listener
        AdapterView.OnItemClickListener favoritelist_click = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                String company = p.getItemAtPosition(pos).toString();
                Log.d(DEBUG_TAG, "on favorite list, clicked on company is: " + company);
                //TODO
                search_quote(company);
                //change to StockResultsActivity now
                Intent i = new Intent(MainActivity.this, StockResultsActivity.class);
                i.putExtra(EXTRA_MESSAGE, company);
                startActivity(i);
            }
        };

        // long press listener
        /*AdapterView.OnItemLongClickListener favoritelist_long_click = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(DEBUG_TAG,"long clicked on favorite list pos: " + position);
                String company = parent.getItemAtPosition(position).toString();
                FavoritesStorage.removeFavoriteItemBySymbol(ctxt, company);
                int index = favorite_symbol.indexOf(company);
                favorite_symbol.remove(index);
                favorite_change.remove(index);
                favorite_price.remove(index);
                favorite_customlist.notifyDataSetChanged();
                return true;
            }
        };*/

        //parse fvmap data
        Iterator<Map.Entry<String, String>> itr = fvmap.entrySet().iterator();
        while(itr.hasNext()) {
            String data = itr.next().getValue();
            try {
                JSONObject json = new JSONObject(data);
                favorite_symbol.add((String)json.get("symbol"));
                favorite_price.add((String)json.get("price"));
                favorite_change.add((String)json.get("change"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //show favorite list view
        //CustomFavoriteListview favorite_customlist = new CustomFavoriteListview(this, favorite_symbol, favorite_price, favorite_change);
        favorite_customlist = new CustomFavoriteListview(this, favorite_symbol, favorite_price, favorite_change);
        favorite_list = findViewById(R.id.listview_favorite);
        favorite_list.setAdapter(favorite_customlist);
        favorite_list.setOnItemClickListener(favoritelist_click);
        //favorite_list.setOnItemLongClickListener(favoritelist_long_click);
    }


    private class autocompleteRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                // request info from AWS
                return interactOnAWS.httpRequest(urls[0]);
            } catch (IOException err) {
                Log.d(DEBUG_TAG, "autocomplete http request error");
                return "http request error";
            }
        }
        @Override
        protected void onPostExecute(String autocomplete_info) {
            Log.d(DEBUG_TAG, "autocomplete result: " + autocomplete_info);
            try {
                //parse result
                JsonParser parser = new JsonParser();
                JsonArray companies_json = parser.parse(autocomplete_info).getAsJsonArray();
                ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<String> (MainActivity.this, android.R.layout.simple_dropdown_item_1line);
                Gson g = new Gson();
                int test = companies_json.size();
                for (int i = 0; i < 5 && i < companies_json.size(); i++) {
                    String company_symbol = g.fromJson(companies_json.get(i).getAsJsonObject().get("Symbol"), String.class); //Json to string
                    String company_name = g.fromJson(companies_json.get(i).getAsJsonObject().get("Name"), String.class); //Json to string
                    String company_exchange = g.fromJson(companies_json.get(i).getAsJsonObject().get("Exchange"), String.class); //Json to string
                    String company = company_symbol + " - " + company_name + " (" +company_exchange + ")";
                    //Log.d(DEBUG_TAG, "autocomplete result: " + company);
                    autocompleteAdapter.add(company);
                }
                autocomplete_text.setAdapter(autocompleteAdapter);
                autocomplete_text.showDropDown();

            } catch (JsonParseException err) {
                Log.d(DEBUG_TAG, "autocomplete JASON parse error");
            }
        }

    }

    private class getquoteRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                // request info from AWS
                return interactOnAWS.httpRequest(urls[0]);
            } catch (IOException err) {
                Log.d(DEBUG_TAG, "get quote http request error");
                return "http request error";
            }
        }
        @Override
        protected void onPostExecute(String sucessinfo) {
            Log.d(DEBUG_TAG, "submit btn click's result is" + sucessinfo);
        }
    }

    private class updateFavoriteRequest extends AsyncTask<String, Void, String> {
        private String symbol = "";
        @Override
        protected String doInBackground(String... urls) {
            try {
                // request info from AWS
                symbol = urls[0].substring(urls[0].indexOf("=")+1, urls[0].length());
                return interactOnAWS.httpRequest(urls[0]);
            } catch (IOException err) {
                Log.d(DEBUG_TAG, "update favorites http request error");
                return "http request error";
            }
        }
        @Override
        protected void onPostExecute(String resp) {
            if(resp.equals("favorite data not ready yet")) {
                //if(req_times < 100) {
                Log.d(DEBUG_TAG, "Executed get request after 1000 ms!");
                //setTimeout
                basicTimer.setTimeout(new Runnable() {
                    @Override
                    public void run() {
                        updateFavoriteItem(symbol);
                    }
                }, 1000);
                //}
                //else {
                //request too many times, need to show error messages
                //}
            }
            else {
                Log.d(DEBUG_TAG, "refresh btn click's result is" + resp);
                try {
                    Gson g = new Gson();
                    JsonParser parser = new JsonParser();
                    JsonObject favorites_json = parser.parse(resp).getAsJsonObject();
                    ArrayList <String> close = new ArrayList<>();
                    for(Map.Entry<String, JsonElement> item : favorites_json.entrySet()) {
                        close.add(g.fromJson(item.getValue().getAsJsonObject().get("4. close"), String.class));
                    }
                    double prev_close = Double.parseDouble(close.get(1));
                    //today's info
                    double cur_close = Double.parseDouble(close.get(0));
                    double change = cur_close - prev_close;
                    double change_percent = 100 * change/prev_close;

                    String lastPriceStr = String.format("%.2f", cur_close);
                    String changeStr = String.format("%.2f", change) + " (" + String.format("%.2f", change_percent) + "%)";
                    int index = favorite_symbol.indexOf(symbol);
                    favorite_price.set(index,lastPriceStr);
                    favorite_change.set(index,changeStr);
                    favorite_customlist.notifyDataSetChanged();

                    // update favorite storage
                    JSONObject update_json = new JSONObject();
                    update_json.put("symbol", symbol);
                    update_json.put("price", lastPriceStr);
                    update_json.put("change", changeStr);
                    String updates = update_json.toString(); //encode into json string
                    FavoritesStorage.updateContent(ctxt, updates);

                } catch (JsonParseException err) {
                    Log.d(DEBUG_TAG, "parsing updating favorites data error");
                } catch (JSONException err) {
                    Log.d(DEBUG_TAG, "parsing updating favorites data error");
                }
            }
        }
    }
    /**/
    private void search_quote(String company_symbol) {
        Log.d(DEBUG_TAG, "get quote button was clicked");

        // TODO symbol is empty or not?

        String url_submit = "http://androidapp-env.us-east-2.elasticbeanstalk.com/stock?symbol=" + company_symbol;
        new getquoteRequest().execute(url_submit);
        //updatedata();
    }


    /* clear autocomplete input */
    private void clear_all() {
        autocomplete_text.setText("");
        autocomplete_text.setAdapter(null);
    }

    /**/
    private void refreshList() {
        HashMap<String, String> local_list = FavoritesStorage.symbolMap;
        //parse local_list data
        Iterator<Map.Entry<String, String>> itr = local_list.entrySet().iterator();
        while(itr.hasNext()) {
            String company_symbol = itr.next().getKey();
            updateFavoriteItem(company_symbol);
        }
    }

    private void updateFavoriteItem(String symbol) {
        String url_refresh = "http://androidapp-env.us-east-2.elasticbeanstalk.com/updatefavorite?symbol=" + symbol;
        new updateFavoriteRequest().execute(url_refresh);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.submit_btn:
                // input validation
                if(autocomplete_text.getText().toString().trim().length() != 0) {
                    String company_symbol = input.substring(0, input.indexOf('-')-1);
                    search_quote(company_symbol);
                    //change to StockResultsActivity now
                    Intent i = new Intent(MainActivity.this, StockResultsActivity.class);
                    i.putExtra(EXTRA_MESSAGE, company_symbol);
                    startActivity(i);
                }
                else {
                    //show alert
                    Toast.makeText(ctxt,
                            "Please enter a stock name or symbol",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.clear_btn:
                clear_all();
                break;
            case R.id.refresh_btn:
                refreshList();
                break;
        }
    }

}
