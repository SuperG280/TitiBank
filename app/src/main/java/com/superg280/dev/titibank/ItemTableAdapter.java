package com.superg280.dev.titibank;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemTableAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<TitiItem> items;
    public ItemTableAdapter(Activity activity, ArrayList<TitiItem> items){
        this.activity = activity;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;

        if (view == null) {
            LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inf != null;
            v = inf.inflate(R.layout.item_table_layout, null);
        }

        TitiItem item = items.get( i);
        TextView fecha = v.findViewById(R.id.textView_itemtable_fecha);
        fecha.setText(item.formatFecha());

        TextView importe = v.findViewById(R.id.textView_itemtable_importe);
        String strImporte = "";
        if( item.getItemType() == TitiItem.ITEM_TYPE_PRESTAMO) {
            strImporte = String.format("-%s", item.formatImporte());
            importe.setTextColor( activity.getColor(R.color.colorNegativo));
        } else {
            strImporte = String.format("%s", item.formatImporte());
            importe.setTextColor( activity.getColor(R.color.colorPositivo));
        }
        importe.setText( strImporte);

        return v;
    }
}
