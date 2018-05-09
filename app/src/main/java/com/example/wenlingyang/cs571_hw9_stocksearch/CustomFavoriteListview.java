package com.example.wenlingyang.cs571_hw9_stocksearch;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by wenlingyang on 11/26/17.
 */

public class CustomFavoriteListview extends ArrayAdapter<String> {
    private final MainActivity context;
    private ArrayList<String> symbol = new ArrayList<>();
    private ArrayList<String> price = new ArrayList<>();
    private ArrayList<String> change = new ArrayList<>();

    public CustomFavoriteListview(MainActivity context, ArrayList<String> symbol, ArrayList<String> price, ArrayList<String> change) {
        super(context, R.layout.favorite_listview_layout, symbol);

        this.context = context;
        this.symbol = symbol;
        this.price = price;
        this.change = change;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        CustomFavoriteListview.ViewHolder viewHolder = null;
        if(rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.favorite_listview_layout,null, true);
            viewHolder = new CustomFavoriteListview.ViewHolder(rowView);
            rowView.setTag(viewHolder);
        }
        else {
            viewHolder = (CustomFavoriteListview.ViewHolder) rowView.getTag();

        }
        viewHolder.symbol_txtv.setText(symbol.get(position));
        viewHolder.price_txtv.setText(price.get(position));
        viewHolder.change_txtv.setText(change.get(position));
        //set change color
        if(Double.parseDouble(change.get(position).substring(0,4)) > 0) {
            viewHolder.change_txtv.setTextColor(Color.parseColor("#00FF00")); //green
        }
        else {
            viewHolder.change_txtv.setTextColor(Color.parseColor("#FF0000")); //red
        }

        return rowView;
    }

    class ViewHolder {
        TextView symbol_txtv;
        TextView price_txtv;
        TextView change_txtv;

        ViewHolder(View v) {
            symbol_txtv = v.findViewById(R.id.favorite_symbol);
            price_txtv = v.findViewById(R.id.favorite_price);
            change_txtv = v.findViewById(R.id.favorite_change);
        }
    }
}
