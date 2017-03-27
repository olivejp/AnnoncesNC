package com.orlanth23.annoncesnc.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.adapter.CardViewDataAdapter;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.dto.Annonce;
import com.orlanth23.annoncesnc.dto.AnnonceFirebase;
import com.orlanth23.annoncesnc.dto.Categorie;
import com.orlanth23.annoncesnc.interfaces.CustomActivityInterface;
import com.orlanth23.annoncesnc.listener.EndlessRecyclerOnScrollListener;
import com.orlanth23.annoncesnc.provider.ProviderContract;
import com.orlanth23.annoncesnc.provider.contract.AnnonceContract;
import com.orlanth23.annoncesnc.utility.Constants;
import com.orlanth23.annoncesnc.utility.Utility;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.ReturnWS;
import com.orlanth23.annoncesnc.webservice.ServiceAnnonce;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByActivity;

public class CardViewFragment extends Fragment implements Callback<ReturnWS> {

    public static final String tag = CardViewFragment.class.getName();

    public static final String PARAM_ACTION = "ACTION";
    public static final String PARAM_KEYWORD = "KEYWORD";
    public static final String PARAM_CATEGORY = "ID_CATEGORY";
    public static final String PARAM_ID_USER = "ID_USER";
    public static final String PARAM_MIN_PRICE = "MIN_PRICE";
    public static final String PARAM_MAX_PRICE = "MAX_PRICE";
    public static final String PARAM_PHOTO = "PHOTO";
    public static final String PARAM_LIST_ANNONCE = "LIST_ANNONCE";
    public static final String ACTION_ANNONCE_BY_KEYWORD = "ACTION_ANNONCE_BY_KEYWORD";
    public static final String ACTION_ANNONCE_BY_CATEGORY = "ACTION_ANNONCE_BY_CATEGORY";
    public static final String ACTION_ANNONCE_BY_USER = "ACTION_ANNONCE_BY_USER";
    public static final String ACTION_MULTI_PARAM = "ACTION_MULTI_PARAM";
    @BindView(R.id.textEmpty)
    TextView textEmpty;
    @BindView(R.id.linearContent)
    LinearLayout linearContent;
    @BindView(R.id.linearEmpty)
    LinearLayout linearEmpty;
    @BindView(R.id.my_recycler_view)
    RecyclerView mRecyclerView;
    private int current_page = 1;
    private CardViewDataAdapter mAdapter;
    private String action;
    private String mode;
    private ArrayList<Annonce> mListAnnonces;
    private Categorie category;
    private String keyword;
    private String idUser;
    private Integer pMinPrice;
    private Integer pMaxPrice;
    private boolean pPhoto;
    private Gson gson;
    private Context mContext;
    private LinearLayoutManager mLinearLayoutManager;

    public CardViewFragment() {
        // Required empty public constructor
    }

    public static CardViewFragment newInstance(String action, String keyword, Categorie categorie, String idUser,
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
            args.putString(PARAM_ID_USER, idUser);
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

        mListAnnonces = new ArrayList<>();
        gson = new Gson();
        if (getArguments() != null) {
            action = getArguments().getString(PARAM_ACTION);
            if (action != null) {
                switch (action) {
                    case ACTION_ANNONCE_BY_USER:
                        mode = Constants.PARAM_MAJ;
                        idUser = getArguments().getString(PARAM_ID_USER);
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
        mAdapter = new CardViewDataAdapter(mContext, mListAnnonces, mode);
        current_page = 1;
        loadData(current_page);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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
            outState.putString(PARAM_ID_USER, idUser);
        }
        if (pMinPrice != null) {
            outState.putInt(PARAM_MIN_PRICE, pMinPrice);
        }
        if (pMaxPrice != null) {
            outState.putInt(PARAM_MAX_PRICE, pMaxPrice);
        }
        if (mListAnnonces != null) {
            outState.putParcelableArrayList(PARAM_LIST_ANNONCE, mListAnnonces);
        }

        outState.putBoolean(PARAM_PHOTO, pPhoto);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // On inflate la vue
        View rootView = inflater.inflate(R.layout.fragment_card_view, container, false);
        ButterKnife.bind(this, rootView);

        // Récupération des paramètres
        if (savedInstanceState != null) {
            action = savedInstanceState.getString(PARAM_ACTION);
            mListAnnonces = savedInstanceState.getParcelableArrayList(PARAM_LIST_ANNONCE);
            if (action != null) {
                switch (action) {
                    case ACTION_ANNONCE_BY_USER:
                        mode = Constants.PARAM_MAJ;
                        idUser = savedInstanceState.getString(PARAM_ID_USER);
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

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);

            mLinearLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLinearLayoutManager);

            // Ajout d'un divider entre les éléments
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                    mLinearLayoutManager.getOrientation());
            mRecyclerView.addItemDecoration(dividerItemDecoration);

            // set the adapter object to the Recyclerview
            mRecyclerView.setAdapter(mAdapter);

            mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(
                    mLinearLayoutManager) {
                @Override
                public void onLoadMore() {
                    loadData(current_page);
                }
            });
        }

        //  Changement du titre et de la couleur de l'activité selon les cas
        int color = ContextCompat.getColor(getActivity(), R.color.ColorPrimary);
        String title = null;

        switch (action) {
            case ACTION_ANNONCE_BY_USER:
                title = getString(R.string.action_my_annonces);
                break;
            case ACTION_ANNONCE_BY_CATEGORY:
                if (category != null) {
                    title = category.getNameCAT();
                    color = Color.parseColor(category.getCouleurCAT());
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
            CustomActivityInterface customActivityInterface = (CustomActivityInterface) myActivity;
            customActivityInterface.changeColorToolBar(color);
        }

        // On cache le clavier
        Utility.hideKeyboard(getActivity());

        changeVisibility();

        return rootView;
    }


    private void loadData(int currentPage) {
        // Création d'un RestAdapter pour le futur appel de mon RestService
        ServiceAnnonce serviceAnnonce = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(ServiceAnnonce.class);
        Call<ReturnWS> call = null;

        switch (action) {
            case ACTION_ANNONCE_BY_CATEGORY:
                // Appel du service RETROFIT
                if (category != null) {
                    // S'il y a du réseau on va recuperer la liste des annonces sur le net
                    if (Utility.checkWifiAndMobileData(mContext)) {
                        Query query = FirebaseDatabase.getInstance().getReference("annonces").orderByChild("idCategory").equalTo(category.getIdCAT());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    AnnonceFirebase annonceFirebase = postSnapshot.getValue(AnnonceFirebase.class);
                                    annonceFirebase.getIdAnnonce();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
                break;
            case ACTION_ANNONCE_BY_KEYWORD:
                // Appel du service RETROFIT
                call = serviceAnnonce.searchAnnonceWithPage(keyword, currentPage);
                break;
            case ACTION_ANNONCE_BY_USER:
                // S'il y a du réseau on va recuperer la liste des annonces sur le net
                if (Utility.checkWifiAndMobileData(mContext)) {
                    Query query = FirebaseDatabase.getInstance().getReference("annonces").orderByChild("idUtilisateur").equalTo(idUser);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                AnnonceFirebase annonceFirebase = postSnapshot.getValue(AnnonceFirebase.class);
                                annonceFirebase.getIdAnnonce();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    // Tentative de récupération dans le contentProvider
                    String where = AnnonceContract.COL_ID_UTILISATEUR + "=?";
                    String[] args = new String[]{idUser};
                    Cursor cursor = getActivity().getContentResolver().query(ProviderContract.AnnonceEntry.CONTENT_URI, null, where, args, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            AnnonceFirebase annonceFirebase = new AnnonceFirebase();
                            annonceFirebase.setIdAnnonce(cursor.getString(cursor.getColumnIndex(AnnonceContract.COL_UUID_ANNONCE)));

                        }
                        cursor.close();
                    }
                }
                break;
            case ACTION_MULTI_PARAM:
                // Appel du service RETROFIT multiparamètre
                call = serviceAnnonce.searchAnnonceWithMultiparam(category.getIdCAT(), pMinPrice, pMaxPrice, keyword, pPhoto, currentPage);
                break;
        }
        if (call != null) {
            call.enqueue(this);
        }
    }

    private void changeVisibility() {
        if (linearContent != null && linearEmpty != null) {
            if (mListAnnonces.isEmpty()) {
                linearContent.setVisibility(View.GONE);
                linearEmpty.setVisibility(View.VISIBLE);
                textEmpty.setVisibility(View.VISIBLE);
            } else {
                linearContent.setVisibility(View.VISIBLE);
                linearEmpty.setVisibility(View.GONE);
                textEmpty.setVisibility(View.GONE);
            }
        }
    }

    private void onPostWebservice(ArrayList<Annonce> listAnnonces) {
        if (listAnnonces != null) {
            for (Annonce annonce : listAnnonces) {
                mListAnnonces.add(annonce);
                if (!mRecyclerView.isComputingLayout()) {
                    mAdapter.notifyItemInserted(mListAnnonces.indexOf(annonce));
                }
            }
        }

        changeVisibility();

        current_page++;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void onFailureWs() {
        Activity activity = getActivity();
        if (activity != null) {
            SendDialogByActivity(activity, getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, tag);
        }
    }

    @Override
    public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
        if (response.isSuccessful()) {
            // Récupération de notre liste locale dans la liste globale
            ReturnWS rs = response.body();
            if (rs.statusValid()) {
                Type listType = new TypeToken<ArrayList<Annonce>>() {
                }.getType();
                ArrayList<Annonce> mListAnnonces = gson.fromJson(rs.getMsg(), listType);
                onPostWebservice(mListAnnonces);
            } else {
                onFailureWs();
            }
        } else {
            onFailureWs();
        }
    }

    @Override
    public void onFailure(Call<ReturnWS> call, Throwable t) {
        onFailureWs();
    }
}
