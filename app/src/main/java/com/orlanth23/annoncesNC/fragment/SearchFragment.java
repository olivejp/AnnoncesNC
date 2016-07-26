package com.orlanth23.annoncesNC.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.adapter.SpinnerAdapter;
import com.orlanth23.annoncesNC.dto.Categorie;
import com.orlanth23.annoncesNC.interfaces.CustomActivityInterface;
import com.orlanth23.annoncesNC.lists.ListeCategories;
import com.orlanth23.annoncesNC.utility.Constants;
import com.orlanth23.annoncesNC.utility.Utility;
import com.orlanth23.annoncesNC.webservices.AccessPoint;
import com.orlanth23.annoncesNC.webservices.RetrofitService;
import com.orlanth23.annoncesNC.webservices.ReturnClass;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by olivejp on 21/10/2015.
 */
public class SearchFragment extends Fragment implements OnClickListener {

    public static final String TAG = SearchFragment.class.getName();
    private static View.OnKeyListener spinnerOnKey = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                Utility.hideKeyboard(v.getContext());
                return true;
            } else {
                return false;
            }
        }
    };
    @Bind(R.id.buttonSearch)
    Button btnSearch;
    @Bind(R.id.editTextSearch)
    EditText editKeyword;
    @Bind(R.id.checkBoxPhoto)
    CheckBox checkBoxPhoto;
    @Bind(R.id.editPrixMin)
    EditText editPrixMin;
    @Bind(R.id.editPrixMax)
    EditText editPrixMax;
    @Bind(R.id.spinnerCategory)
    Spinner spinnerCategory;
    @Bind(R.id.txtCheckBox)
    TextView txtCheckBox;
    private ArrayList<Categorie> listCat;
    /**
     *
     */
    private View.OnClickListener textCheckBoxclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            checkBoxPhoto.setChecked(!checkBoxPhoto.isChecked());
        }
    };
    private View.OnTouchListener spinnerOnTouch = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Utility.hideKeyboard(v.getContext());
            }
            return false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        ButterKnife.bind(this, rootView);

        btnSearch.setOnClickListener(this);
        txtCheckBox.setOnClickListener(textCheckBoxclickListener);
        spinnerCategory.setOnTouchListener(spinnerOnTouch);
        spinnerCategory.setOnKeyListener(spinnerOnKey);


        // ---------------------------------------
        // RECUPERATION de la liste des catégories
        // ---------------------------------------
        if (ListeCategories.getInstance().getListCategorie() == null) {
            RetrofitService retrofitService = new RestAdapter.Builder().setEndpoint(AccessPoint.getDefaultServerEndpoint()).build().create(RetrofitService.class);
            retrofitService.listcategorie(new retrofit.Callback<ReturnClass>() {
                @Override
                public void success(ReturnClass rs, Response response) {
                    if (rs.isStatus()) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<Categorie>>() {
                        }.getType();
                        listCat = gson.fromJson(rs.getMsg(), listType);

                        // On réceptionne la liste des catégories dans l'instance ListeCategories
                        ListeCategories.setMyArrayList(listCat);

                        loadSpinner(listCat);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getActivity(), getString(R.string.dialog_failed_webservice), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            loadSpinner(ListeCategories.getInstance().getListCategorie());
        }

        // Permet d'appeler la création du menu dans l'action bar
        setHasOptionsMenu(true);

        // Changement du titre dans l'action bar
        getActivity().setTitle(getString(R.string.action_search));

        // Changement de couleur de l'action bar
        Activity myActivity = getActivity();
        if (myActivity instanceof CustomActivityInterface) {
            CustomActivityInterface myCustomActivity = (CustomActivityInterface) myActivity;
            myCustomActivity.changeColorToolBar(Constants.colorPrimary);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    /**
     *
     */
    private void loadSpinner(ArrayList<Categorie> listCat) {

        ArrayList<Categorie> listCat1 = new ArrayList<>(listCat);

        // On rajoute la catégorie "Toutes les catégories" à la liste des catégories
        Categorie allCat = new Categorie(Constants.ID_ALL_CAT, getString(R.string.text_all_category), getString(R.string.color_black), 0);
        listCat1.add(0, allCat);

        // Création de l'adapter de la liste catégorie
        SpinnerAdapter adapter = new SpinnerAdapter(getActivity().getApplicationContext(), R.layout.drawer_list_category, listCat1);

        // J'affecte l'adapter à ma listView
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setSelection(0);
    }

    /**
     * Méthode de vérification des données, avant d'envoyer la recherche
     *
     * @return
     */
    private boolean checkSearch() {
        boolean retour = true;
        Integer min;
        Integer max;
        View focus = null;
        String keyword = editKeyword.getText().toString();

        if (!editPrixMin.getText().toString().isEmpty()) {
            min = Integer.valueOf(editPrixMin.getText().toString());
        } else {
            min = 0;
        }

        if (!editPrixMax.getText().toString().isEmpty()) {
            max = Integer.valueOf(editPrixMax.getText().toString());
        } else {
            max = 0;
        }

        Categorie cat = (Categorie) spinnerCategory.getSelectedItem();
        boolean photo = checkBoxPhoto.isChecked();

        // Aucun paramètre défini
        if (keyword.isEmpty() && min == 0 && max == 0 && cat.getIdCAT() == Constants.ID_ALL_CAT && !photo) {
            Toast.makeText(getActivity(), R.string.error_no_parameter, Toast.LENGTH_LONG).show();
            focus = editKeyword;
            retour = false;
        }

        if (min > max) {
            Toast.makeText(getActivity(), R.string.error_min_sup_max, Toast.LENGTH_LONG).show();
            focus = editPrixMin;
            retour = false;
        }

        if (focus != null) {
            focus.requestFocus();
        }

        return retour;
    }

    @Override
    public void onClick(View v) {
        CardViewFragment cardViewFragment;
        Integer min;
        Integer max;

        if (checkSearch()) {

            String keyword = editKeyword.getText().toString();
            if (!editPrixMin.getText().toString().isEmpty()) {
                min = Integer.valueOf(editPrixMin.getText().toString());
            } else {
                min = 0;
            }

            if (!editPrixMax.getText().toString().isEmpty()) {
                max = Integer.valueOf(editPrixMax.getText().toString());
            } else {
                max = 0;
            }
            Categorie cat = (Categorie) spinnerCategory.getSelectedItem();
            boolean photo = checkBoxPhoto.isChecked();

            if (!keyword.isEmpty()) {
                keyword = keyword.replace("'", "''");
            }

            // Si on a renseigné que le mot clef, on ne prend que le mot clef
            if (!keyword.isEmpty() && min == 0 && max == 0 && !photo && cat.getIdCAT() == Constants.ID_ALL_CAT) {
                cardViewFragment = CardViewFragment.newInstance(CardViewFragment.ACTION_ANNONCE_BY_KEYWORD, keyword, null, null, null, null, false);
            } else {
                // Si on a mis que la catégorie, on fait une recherche sur la catégorie
                if (keyword.isEmpty() && min == 0 && max == 0 && !photo && cat.getIdCAT() != Constants.ID_ALL_CAT) {
                    cardViewFragment = CardViewFragment.newInstance(CardViewFragment.ACTION_ANNONCE_BY_CATEGORY, null, cat, null, null, null, false);
                } else {
                    // Dans les autres cas c'est une recherche multi paramètres
                    cardViewFragment = CardViewFragment.newInstance(CardViewFragment.ACTION_MULTI_PARAM, keyword, cat, null, min, max, photo);
                }
            }

            // On va remplacer le fragment par celui de la liste d'annonce
            getFragmentManager().beginTransaction().replace(R.id.frame_container, cardViewFragment, CardViewFragment.tag).addToBackStack(null).commit();
        }
    }
}