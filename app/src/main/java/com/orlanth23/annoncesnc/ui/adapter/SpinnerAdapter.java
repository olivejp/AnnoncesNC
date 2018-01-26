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
import com.orlanth23.annoncesnc.domain.Categorie;

import java.util.ArrayList;

public class SpinnerAdapter extends BaseAdapter {

    private Context context;
    private int res;
    private ArrayList<Categorie> navCategorieItems;

    public SpinnerAdapter(Context context, int resource, ArrayList<Categorie> navCategorieItems) {
        super();

        this.context = context;
        this.res = resource;
        this.navCategorieItems = navCategorieItems;
    }

    @Override
    public int getCount() {
        return navCategorieItems.size();
    }

    @Override
    public Categorie getItem(int position) {
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
            convertView = mInflater.inflate(res, null);
        }

        TextView txtColorCategory = convertView.findViewById(R.id.colorCategory);
        TextView txtidCategory = convertView.findViewById(R.id.idCategory);
        TextView txtTitle = convertView.findViewById(R.id.titleCategory);

        // Récupération de la couleur
        int color = Color.parseColor(navCategorieItems.get(position).getCouleurCAT());

        txtidCategory.setText(String.valueOf(navCategorieItems.get(position).getIdCAT()));
        txtColorCategory.setBackgroundColor(color);
        txtTitle.setText(navCategorieItems.get(position).getNameCAT());

        return convertView;
    }

}
