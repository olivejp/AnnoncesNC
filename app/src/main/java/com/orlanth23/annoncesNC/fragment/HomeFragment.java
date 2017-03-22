package com.orlanth23.annoncesnc.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.interfaces.CustomActivityInterface;
import com.orlanth23.annoncesnc.list.ListeStats;
import com.orlanth23.annoncesnc.provider.AnnoncesProvider;
import com.orlanth23.annoncesnc.provider.ProviderContract;
import com.orlanth23.annoncesnc.provider.contract.InfosServerContract;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomeFragment extends Fragment {

    public static final String tag = HomeFragment.class.getName();
    @BindView(R.id.textHomeNbUtilisateur)
    TextView textHomeNbUtilisateur;
    @BindView(R.id.textHomeNbAnnonce)
    TextView textHomeNbAnnonce;
    @BindView(R.id.imageHomeLogo)
    ImageView imageHomeLogo;

    private View rootView;
    // This observer will update textView on the layout when ListeStats change.
    private Observer listeInfosServerObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            updateInfosServer();
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootView);

        View.OnClickListener clickListenerImage = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gestion d'une animation
                ImageView image = (ImageView) rootView.findViewById(R.id.imageHomeLogo);
                Animation animation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.myanimation);
                image.startAnimation(animation);
            }
        };

        imageHomeLogo.setOnClickListener(clickListenerImage);

        Activity myActivity = getActivity();
        myActivity.setTitle(getString(R.string.app_name));
        if (myActivity instanceof CustomActivityInterface) {
            CustomActivityInterface myCustomActivity = (CustomActivityInterface) myActivity;
            int color = ContextCompat.getColor(myActivity,R.color.ColorPrimary);
            myCustomActivity.changeColorToolBar(color);
        }

        // On ajoute l'observer à notre liste de stats. Quand elle bougera, on mettra à jour les views
        ListeStats listStats = ListeStats.getInstance(getActivity());
        listStats.addObserver(listeInfosServerObserver);

        updateInfosServer();

        return rootView;
    }

    private void updateInfosServer(){
        Cursor cursor = getActivity().getContentResolver().query(ProviderContract.InfosServerEntry.CONTENT_URI, null, AnnoncesProvider.sSelectionInfosServer, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int nbAnnonce = cursor.getInt(cursor.getColumnIndex(InfosServerContract.COL_NB_ANNONCE));
                int nbUtilisateur = cursor.getInt(cursor.getColumnIndex(InfosServerContract.COL_NB_UTILISATEUR));
                textHomeNbUtilisateur.setText(getString(R.string.textNbUser).concat(String.valueOf(nbAnnonce)));
                textHomeNbAnnonce.setText(getString(R.string.textNbAnnonces).concat(String.valueOf(nbUtilisateur)));
            }
            cursor.close();
        }
    }
}
