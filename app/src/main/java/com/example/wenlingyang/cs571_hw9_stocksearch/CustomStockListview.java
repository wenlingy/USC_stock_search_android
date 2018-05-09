package com.example.wenlingyang.cs571_hw9_stocksearch;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import java.util.ArrayList;

/**
 * Created by wenlingyang on 11/24/17.
 */

public class CustomStockListview extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] left_column;
    private ArrayList<String> right_column = new ArrayList<>();
    private final Integer[] imgid;

    public CustomStockListview(Activity context, String[] left_column, ArrayList<String> right_column, Integer[] imgid) {
        super(context, R.layout.listview_layout, left_column);

        this.context = context;
        this.left_column = left_column;
        this.right_column = right_column;
        this.imgid = imgid;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder viewHolder = null;
        if(rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.listview_layout,null, true);
            viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) rowView.getTag();

        }
        viewHolder.left_txtv.setText(left_column[position]);
        viewHolder.right_txtv.setText(right_column.get(position));
        if(position == 2) {
            if(Double.parseDouble(right_column.get(position).substring(0,4)) > 0) {
                viewHolder.imgv.setImageResource(imgid[0]);
            }
            else {
                viewHolder.imgv.setImageResource(imgid[1]);
            }
        }
        return rowView;
    }

    class ViewHolder {
        TextView left_txtv;
        TextView right_txtv;
        ImageView imgv;

        ViewHolder(View v) {
            left_txtv = v.findViewById(R.id.left_col);
            right_txtv = v.findViewById(R.id.right_col);
            imgv = v.findViewById(R.id.updown_img);
        }
    }
}
