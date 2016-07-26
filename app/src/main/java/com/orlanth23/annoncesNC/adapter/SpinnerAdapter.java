package com.orlanth23.annoncesNC.adapter;

/**
 * Created by olivejp on 19/04/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.dto.Categorie;
import com.orlanth23.annoncesNC.utility.Utility;

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

        TextView txtColorCategory = (TextView) convertView.findViewById(R.id.colorCategory);
        TextView txtidCategory = (TextView) convertView.findViewById(R.id.idCategory);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.titleCategory);

        // Récupération de la couleur
        int color = Utility.getColorFromString(navCategorieItems.get(position).getImageCAT());

        txtidCategory.setText(String.valueOf(navCategorieItems.get(position).getIdCAT()));
        txtColorCategory.setBackgroundColor(color);
        txtTitle.setText(navCategorieItems.get(position).getNameCAT());

        return convertView;
    }

}
