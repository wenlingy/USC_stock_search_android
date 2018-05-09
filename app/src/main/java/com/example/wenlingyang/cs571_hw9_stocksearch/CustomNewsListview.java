package com.example.wenlingyang.cs571_hw9_stocksearch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by wenlingyang on 11/26/17.
 */

public class CustomNewsListview extends ArrayAdapter<String> {
    private final Activity context;
    private ArrayList<String> title = new ArrayList<>();
    private ArrayList<String> author = new ArrayList<>();
    private ArrayList<String> date = new ArrayList<>();
    private ArrayList<String> link = new ArrayList<>();

    //public CustomNewsListview(Activity context, ArrayList<String> t, ArrayList<String> a, ArrayList<String> d, ArrayList<String> l) {
    public CustomNewsListview(Activity context, ArrayList<String> t, ArrayList<String> a, ArrayList<String> d) {
        super(context, R.layout.news_listview_layout, t);

        this.context = context;
        this.title = t;
        this.author = a;
        this.date = d;
        //this.link = l;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        CustomNewsListview.ViewHolder viewHolder = null;
        if(rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.news_listview_layout,null, true);
            viewHolder = new CustomNewsListview.ViewHolder(rowView);
            rowView.setTag(viewHolder);
        }
        else {
            viewHolder = (CustomNewsListview.ViewHolder) rowView.getTag();

        }
        viewHolder.title_txtv.setText(title.get(position));
        viewHolder.author_txtv.setText(author.get(position));
        viewHolder.date_txtv.setText(date.get(position));

        return rowView;
    }

    class ViewHolder {
        TextView title_txtv;
        TextView author_txtv;
        TextView date_txtv;

        ViewHolder(View v) {
            title_txtv = v.findViewById(R.id.news_title);
            author_txtv = v.findViewById(R.id.news_author);
            date_txtv = v.findViewById(R.id.news_date);
        }
    }
}
