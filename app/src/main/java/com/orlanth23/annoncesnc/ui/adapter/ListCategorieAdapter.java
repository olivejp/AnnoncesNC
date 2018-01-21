package com.orlanth23.annoncesnc.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dto.Categorie;

import java.util.ArrayList;

public class ListCategorieAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Categorie> navCategorieItems;

    public ListCategorieAdapter(Context context, ArrayList<Categorie> navCategorieItems) {
        this.context = context;
        this.navCategorieItems = navCategorieItems;
    }

    @Override
    public int getCount() {
        return navCategorieItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navCategorieItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_categorie, null);
        }

        TextView txtColorCategory = (TextView) convertView.findViewById(R.id.colorCategory);
        TextView txtidCategory = (TextView) convertView.findViewById(R.id.idCategory);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.titleCategory);
        TextView txtCount = (TextView) convertView.findViewById(R.id.counterCategory);
        txtidCategory.setText(String.valueOf(navCategorieItems.get(position).getIdCAT()));

        int color = Color.parseColor(navCategorieItems.get(position).getCouleurCAT());
        txtColorCategory.setBackgroundColor(color);
        txtTitle.setText(navCategorieItems.get(position).getNameCAT());
        txtCount.setText(String.valueOf(navCategorieItems.get(position).getNbAnnonceCAT()));

        return convertView;
    }

}