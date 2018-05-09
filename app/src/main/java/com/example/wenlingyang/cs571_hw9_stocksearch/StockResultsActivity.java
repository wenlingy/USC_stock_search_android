package com.example.wenlingyang.cs571_hw9_stocksearch;

import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;

public class StockResultsActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "StockActivityPart";
    public static String symbol = "";
    public static HashMap<String, String> alldata = new HashMap <> ();


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_results);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        /***************************** action bar setting ***************************************/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        symbol = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
        getSupportActionBar().setTitle(symbol);

        updatedata(); // GET data from AWS
    }
    private class getstockinfoRequest extends AsyncTask<String, Void, String> {
        private String key = "";

        @Override
        protected String doInBackground(String... urls) {
            try {
                // request info from AWS
                key = urls[0].substring(urls[0].indexOf('=')+1, urls[0].indexOf('&'));
                return interactOnAWS.httpRequest(urls[0]);
            } catch (IOException err) {
                Log.d(DEBUG_TAG, "get quote http request error");
                return "http request error";
            }
        }
        @Override
        protected void onPostExecute(String resp) {
            Log.d(DEBUG_TAG, resp);
            Log.d("APIdata_key", key);
            if(resp.equals("data not ready yet")) {
                //if(req_times < 100) {
                Log.d(DEBUG_TAG, "Executed get request after 3000 ms!");
                //setTimeout
                basicTimer.setTimeout(new Runnable() {
                    @Override
                    public void run() {
                        //getApiData(key, req_times+1);
                        getApiData(key);
                    }
                }, 3000);
                //}
                //else {
                //request too many times, need to show error messages
                //}
            }
            else {
                alldata.put(key, resp);
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                tab1current current = null;
                tab2historical historical = null;
                for(int i=0; i<fragments.size(); i++) {
                    Fragment f = fragments.get(i);
                    String classname = f.getClass().getName();
                    if(classname.equals("com.project.stocksearch.tab1current")) {
                        current = (tab1current) f;
                    }
                    else if(classname.equals("com.project.stocksearch.tab2historical")) {
                        historical = (tab2historical) f;
                    }
                }

                if (key.equals("Time Series (Daily)") && current != null) {
                    current.updateListView();
                    current.updateChartsView("Price");
                } else if (key.equals("Technical Analysis: SMA") && current != null) {
                    current.updateChartsView("SMA");
                } else if (key.equals("Technical Analysis: EMA") && current != null) {
                    current.updateChartsView("EMA");
                } else if (key.equals("Technical Analysis: STOCH") && current != null) {
                    current.updateChartsView("STOCH");
                } else if (key.equals("Technical Analysis: RSI") && current != null) {
                    current.updateChartsView("RSI");
                } else if (key.equals("Technical Analysis: ADX") && current != null) {
                    current.updateChartsView("ADX");
                } else if (key.equals("Technical Analysis: CCI") && current != null) {
                    current.updateChartsView("CCI");
                } else if (key.equals("Technical Analysis: BBANDS") && current != null) {
                    current.updateChartsView("BBANDS");
                } else if (key.equals("Technical Analysis: MACD") && current != null) {
                    current.updateChartsView("MACD");
                } else if (key.equals("allStockValue") && historical != null) {
                    historical.updateChartsView();
                } /*else if (key.equals("news")) {
                    //parseNewsData(resp);
                }*/
            }
        }
    }

    private void updatedata() {
        getApiData("Time Series (Daily)");
        getApiData("Time Series (Daily)");
        getApiData("Technical Analysis: SMA");
        getApiData("Technical Analysis: EMA");
        getApiData("Technical Analysis: STOCH");
        getApiData("Technical Analysis: RSI");
        getApiData("Technical Analysis: ADX");
        getApiData("Technical Analysis: CCI");
        getApiData("Technical Analysis: BBANDS");
        getApiData("Technical Analysis: MACD");
        getApiData("allStockValue");
        //getApiData("news");
        /*
        getApiData("Time Series (Daily)", 0);
        getApiData("Technical Analysis: SMA", 0);
        getApiData("Technical Analysis: EMA", 0);
        getApiData("Technical Analysis: STOCH", 0);
        getApiData("Technical Analysis: RSI", 0);
        getApiData("Technical Analysis: ADX", 0);
        getApiData("Technical Analysis: CCI", 0);
        getApiData("Technical Analysis: BBANDS", 0);
        getApiData("Technical Analysis: MACD", 0);
        getApiData("allStockValue", 0);
        getApiData("news", 0);*/
    }

    private void getApiData(String key) {
        String url_getdata = "http://androidapp-env.us-east-2.elasticbeanstalk.com/updatedata?key=" + Html.escapeHtml(key) + "&symbol=" + StockResultsActivity.symbol;
        new getstockinfoRequest().execute(url_getdata);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stock_results, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        } */
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    //public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        /*
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        } */

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        /*
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }*/
        /*
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment1_results, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }*/

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case 0:
                    tab1current tab1 = new tab1current();
                    return tab1;
                case 1:
                    tab2historical tab2 = new tab2historical();
                    return tab2;
                case 2:
                    tab3news tab3 = new tab3news();
                    return tab3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
