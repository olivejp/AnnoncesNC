package com.orlanth23.annoncesnc.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.adapter.SpinnerAdapter;
import com.orlanth23.annoncesnc.dto.Categorie;
import com.orlanth23.annoncesnc.interfaces.CustomActivityInterface;
import com.orlanth23.annoncesnc.list.ListeCategories;
import com.orlanth23.annoncesnc.utility.Constants;
import com.orlanth23.annoncesnc.utility.Utility;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.RetrofitService;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    @BindView(R.id.buttonSearch)
    Button btnSearch;
    @BindView(R.id.editTextSearch)
    EditText editKeyword;
    @BindView(R.id.checkBoxPhoto)
    CheckBox checkBoxPhoto;
    @BindView(R.id.editPrixMin)
    EditText editPrixMin;
    @BindView(R.id.editPrixMax)
    EditText editPrixMax;
    @BindView(R.id.spinnerCategory)
    Spinner spinnerCategory;
    @BindView(R.id.txtCheckBox)
    TextView txtCheckBox;
    private ListeCategories listCategorie;


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

    private Callback<ReturnWS> callbackListCategory = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS retour = response.body();
                if (retour.statusValid()) {
                    // On réceptionne la liste des catégories dans l'instance ListeCategories
                    ListeCategories.setNbAnnonceFromJson(retour.getMsg());
                    loadSpinner(listCategorie.getListCategorie());
                }
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            Toast.makeText(getActivity(), getString(R.string.dialog_failed_webservice), Toast.LENGTH_LONG).show();
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

        listCategorie = ListeCategories.getInstance(getActivity());

        if (listCategorie.getListCategorie() == null) {
            RetrofitService retrofitService = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitService.class);
            Call<ReturnWS> call = retrofitService.getListCategory();
            call.enqueue(callbackListCategory);
        } else {
            loadSpinner(listCategorie.getListCategorie());
        }

        setHasOptionsMenu(true);
        getActivity().setTitle(getString(R.string.action_search));
        Activity myActivity = getActivity();
        if (myActivity instanceof CustomActivityInterface) {
            CustomActivityInterface myCustomActivity = (CustomActivityInterface) myActivity;
            myCustomActivity.changeColorToolBar(ContextCompat.getColor(getActivity(), R.color.ColorPrimary));
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void loadSpinner(ArrayList<Categorie> listCat) {

        ArrayList<Categorie> listCat1 = new ArrayList<>(listCat);

        // On rajoute la catégorie "Toutes les catégories" à la liste des catégories
        Categorie allCat = new Categorie(Constants.ID_ALL_CAT, getString(R.string.text_all_category), getString(R.string.color_black), 0);
        listCat1.add(0, allCat);

        // Création de l'adapter de la liste catégorie
        SpinnerAdapter adapter = new SpinnerAdapter(getActivity().getApplicationContext(), R.layout.drawer_list_categorie, listCat1);

        // J'affecte l'adapter à ma listView
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setSelection(0);
    }

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
