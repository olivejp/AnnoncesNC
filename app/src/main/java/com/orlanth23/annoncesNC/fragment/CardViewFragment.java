package com.orlanth23.annoncesNC.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.adapter.CardViewDataAdapter;
import com.orlanth23.annoncesNC.dialogs.NoticeDialogFragment;
import com.orlanth23.annoncesNC.dto.Annonce;
import com.orlanth23.annoncesNC.dto.Categorie;
import com.orlanth23.annoncesNC.interfaces.CustomActivityInterface;
import com.orlanth23.annoncesNC.listeners.EndlessRecyclerOnScrollListener;
import com.orlanth23.annoncesNC.utility.Constants;
import com.orlanth23.annoncesNC.utility.Utility;
import com.orlanth23.annoncesNC.webservices.AccessPoint;
import com.orlanth23.annoncesNC.webservices.RetrofitService;

import java.util.ArrayList;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.orlanth23.annoncesNC.utility.Utility.SendDialogByActivity;

public class CardViewFragment extends Fragment {

    public static final String tag = CardViewFragment.class.getName();

    public static final String PARAM_ACTION = "ACTION";
    public static final String PARAM_KEYWORD = "KEYWORD";
    public static final String PARAM_CATEGORY = "ID_CATEGORY";
    public static final String PARAM_ID_USER = "ID_USER";
    public static final String PARAM_MIN_PRICE = "MIN_PRICE";
    public static final String PARAM_MAX_PRICE = "MAX_PRICE";
    public static final String PARAM_PHOTO = "PHOTO";
    public static final String ACTION_ANNONCE_BY_KEYWORD = "ACTION_ANNONCE_BY_KEYWORD";
    public static final String ACTION_ANNONCE_BY_CATEGORY = "ACTION_ANNONCE_BY_CATEGORY";
    public static final String ACTION_ANNONCE_BY_USER = "ACTION_ANNONCE_BY_USER";
    public static final String ACTION_MULTI_PARAM = "ACTION_MULTI_PARAM";

    private int current_page = 1;
    private RecyclerView mRecyclerView;
    private CardViewDataAdapter mAdapter;
    private String action;
    private String mode;
    private ProgressDialog prgDialog;
    private ArrayList<Annonce> gbListAnnonces;
    private Categorie category;
    private String keyword;
    private Integer idUser;
    private Integer pMinPrice;
    private Integer pMaxPrice;
    private boolean pPhoto;
    private View rootView;

    public CardViewFragment() {
        // Required empty public constructor
    }

    public static CardViewFragment newInstance(String action, String keyword, Categorie categorie, Integer idUser,
                                               Integer minPrice, Integer maxPrice, boolean photo) {
        CardViewFragment fragment = new CardViewFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_ACTION, action);
        if (categorie != null) {
            args.putParcelable(PARAM_CATEGORY, categorie);
        }
        if (keyword != null) {
            args.putString(PARAM_KEYWORD, keyword);
        }
        if (idUser != null) {
            args.putInt(PARAM_ID_USER, idUser);
        }
        if (minPrice != null) {
            args.putInt(PARAM_MIN_PRICE, minPrice);
        }
        if (maxPrice != null) {
            args.putInt(PARAM_MAX_PRICE, maxPrice);
        }
        args.putBoolean(PARAM_PHOTO, photo);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            action = getArguments().getString(PARAM_ACTION);
            if (action != null) {
                switch (action) {
                    case ACTION_ANNONCE_BY_USER:
                        mode = Constants.PARAM_MAJ;
                        idUser = getArguments().getInt(PARAM_ID_USER);
                        break;
                    case ACTION_ANNONCE_BY_CATEGORY:
                        mode = Constants.PARAM_VIS;
                        category = getArguments().getParcelable(PARAM_CATEGORY);
                        break;
                    case ACTION_ANNONCE_BY_KEYWORD:
                        mode = Constants.PARAM_VIS;
                        keyword = getArguments().getString(PARAM_KEYWORD);
                        break;
                    case ACTION_MULTI_PARAM:
                        mode = Constants.PARAM_VIS;
                        keyword = getArguments().getString(PARAM_KEYWORD);
                        category = getArguments().getParcelable(PARAM_CATEGORY);
                        pMaxPrice = getArguments().getInt(PARAM_MAX_PRICE);
                        pMinPrice = getArguments().getInt(PARAM_MIN_PRICE);
                        pPhoto = getArguments().getBoolean(PARAM_PHOTO);
                        break;
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PARAM_ACTION, action);
        if (category != null) {
            outState.putParcelable(PARAM_CATEGORY, category);
        }
        if (keyword != null) {
            outState.putString(PARAM_KEYWORD, keyword);
        }
        if (idUser != null) {
            outState.putInt(PARAM_ID_USER, idUser);
        }
        if (pMinPrice != null) {
            outState.putInt(PARAM_MIN_PRICE, pMinPrice);
        }
        if (pMaxPrice != null) {
            outState.putInt(PARAM_MAX_PRICE, pMaxPrice);
        }
        outState.putBoolean(PARAM_PHOTO, pPhoto);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // On inflate la vue
        rootView = inflater.inflate(R.layout.fragement_card_view, container, false);

        prgDialog = new ProgressDialog(getActivity());

        gbListAnnonces = new ArrayList<>();

        // Récupération des paramètres
        if (savedInstanceState != null) {
            action = savedInstanceState.getString(PARAM_ACTION);
            if (action != null) {
                switch (action) {
                    case ACTION_ANNONCE_BY_USER:
                        mode = Constants.PARAM_MAJ;
                        idUser = savedInstanceState.getInt(PARAM_ID_USER);
                        break;
                    case ACTION_ANNONCE_BY_CATEGORY:
                        mode = Constants.PARAM_VIS;
                        category = savedInstanceState.getParcelable(PARAM_CATEGORY);
                        break;
                    case ACTION_ANNONCE_BY_KEYWORD:
                        mode = Constants.PARAM_VIS;
                        keyword = savedInstanceState.getString(PARAM_KEYWORD);
                        break;
                    case ACTION_MULTI_PARAM:
                        mode = Constants.PARAM_VIS;
                        keyword = savedInstanceState.getString(PARAM_KEYWORD);
                        category = savedInstanceState.getParcelable(PARAM_CATEGORY);
                        pMaxPrice = savedInstanceState.getInt(PARAM_MAX_PRICE);
                        pMinPrice = savedInstanceState.getInt(PARAM_MIN_PRICE);
                        pPhoto = savedInstanceState.getBoolean(PARAM_PHOTO);
                        break;
                }
            }
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

        // use a linear layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Changement du titre et de la couleur de l'activité selon les cas
        String color = Constants.colorPrimary;
        String title = null;

        switch (action) {
            case ACTION_ANNONCE_BY_USER:
                title = getString(R.string.action_my_annonces);
                break;
            case ACTION_ANNONCE_BY_CATEGORY:
                if (category != null) {
                    title = category.getNameCAT();
                    color = category.getImageCAT();
                }
                break;
            case ACTION_ANNONCE_BY_KEYWORD:
                title = getString(R.string.action_search) + " : " + keyword;
                break;
            case ACTION_MULTI_PARAM:
                title = getString(R.string.action_search);
                break;
        }

        Activity myActivity = getActivity();
        myActivity.setTitle(title);
        if (myActivity instanceof CustomActivityInterface) {
            CustomActivityInterface myCustomActivity = (CustomActivityInterface) myActivity;
            myCustomActivity.changeColorToolBar(color);
        }

        mAdapter = new CardViewDataAdapter(getActivity(), gbListAnnonces, mode);

        current_page = 1;

        // set the adapter object to the Recyclerview
        mRecyclerView.setAdapter(mAdapter);

        loadData(current_page);

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(
                mLayoutManager) {
            @Override
            public void onLoadMore() {
                loadData(current_page);
            }
        });

        // On cache le clavier
        Utility.hideKeyboard(getActivity());

        return rootView;
    }


    private void loadData(int currentPage) {

        // Définition d'un nouveau callback
        retrofit.Callback<ArrayList<Annonce>> myCallback = new retrofit.Callback<ArrayList<Annonce>>() {
            @Override
            public void success(ArrayList<Annonce> annonces, Response response) {
                // Récupération de notre liste locale dans la liste globale
                prgDialog.hide();

                onPostWebservice(annonces);
            }

            @Override
            public void failure(RetrofitError error) {
                prgDialog.hide();
                Activity activity = getActivity();
                if (activity != null) {
                    SendDialogByActivity(activity, getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, tag);
                }
            }
        };

        // Création d'un RestAdapter pour le futur appel de mon RestService
        RetrofitService retrofitService = new RestAdapter.Builder().setEndpoint(AccessPoint.getInstance().getServerEndpoint()).build().create(RetrofitService.class);

        prgDialog.setMessage(getString(R.string.dialog_msg_patience));
        prgDialog.show();
        switch (action) {
            case ACTION_ANNONCE_BY_CATEGORY:
                // Appel du service RETROFIT
                if (category != null) {
                    retrofitService.getListAnnonceByCategoryWithPage(category.getIdCAT(), currentPage, myCallback);
                }
                break;
            case ACTION_ANNONCE_BY_KEYWORD:
                // Appel du service RETROFIT
                retrofitService.searchAnnonceWithPage(keyword, currentPage, myCallback);
                break;
            case ACTION_ANNONCE_BY_USER:
                // Appel du service RETROFIT
                retrofitService.getListAnnonceByUser(idUser, currentPage, myCallback);
                break;
            case ACTION_MULTI_PARAM:
                // Appel du service RETROFIT multiparamètre
                retrofitService.searchAnnonceWithMultiparam(category.getIdCAT(), pMinPrice, pMaxPrice, keyword, pPhoto, currentPage, myCallback);
                break;
        }
    }

    private void onPostWebservice(ArrayList<Annonce> listAnnonces) {

        if (listAnnonces != null) {
            for (Annonce annonce : listAnnonces) {
                gbListAnnonces.add(annonce);
                if (!mRecyclerView.isComputingLayout()) {
                    mAdapter.notifyItemInserted(gbListAnnonces.indexOf(annonce));
                }
            }
        }

        // Si la liste des annonces est vide, on va afficher un message comme quoi, la requête n'a ramené aucun résultat
        LinearLayout linearContent = (LinearLayout) rootView.findViewById(R.id.linearContent);
        LinearLayout linearEmpty = (LinearLayout) rootView.findViewById(R.id.linearEmpty);

        if (linearContent != null && linearEmpty != null) {
            if (gbListAnnonces.isEmpty()) {
                linearContent.setVisibility(View.INVISIBLE);
                linearEmpty.setVisibility(View.VISIBLE);
            } else {
                linearContent.setVisibility(View.VISIBLE);
                linearEmpty.setVisibility(View.INVISIBLE);
            }
        }

        current_page++;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }
}
