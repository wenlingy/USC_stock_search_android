package com.example.wenlingyang.cs571_hw9_stocksearch;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by wenlingyang on 11/23/17.
 */

public class interactOnAWS {
    public static String DEBUG_TAG = "get_request_from_AWS";
    // not catch error here
    public static String httpRequest(String url_query) throws IOException {
        InputStream in = null;
        String queryResult = "";
        try {
            URL url = new URL(url_query);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            //httpConn.setAllowUserInteraction(false);
            httpConn.setRequestMethod("GET");

            httpConn.connect();
            in = httpConn.getInputStream();
            int res_status = httpConn.getResponseCode();
            if(res_status == 200 && in != null) {
                Scanner input = new Scanner(in).useDelimiter("\\A");
                while(input.hasNext()) {
                    queryResult = input.next();
                }
                input.close();
            }
            return queryResult;
        } finally {
            if(in!=null)
                in.close();
        }
    }
}
