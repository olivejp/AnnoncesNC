package com.orlanth23.annoncesNC.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.activity.MainActivity;
import com.orlanth23.annoncesNC.interfaces.CustomActivityInterface;
import com.orlanth23.annoncesNC.lists.ListeStats;
import com.orlanth23.annoncesNC.utility.Constants;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;


public class HomeFragment extends Fragment {

    public static final String tag = HomeFragment.class.getName();
    @Bind(R.id.textHomeNbUser)
    TextView textHomeNbUser;
    @Bind(R.id.textHomeNbAnnonce)
    TextView textHomeNbAnnonce;
    @Bind(R.id.imageHomeLogo)
    ImageView imageHomeLogo;

    private View rootView;
    private Handler mHandler = new Handler();
    private Runnable runnable;
    private Timer mTimer;
    private TimerTask timerTask;

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
            myCustomActivity.changeColorToolBar(Constants.colorPrimary);
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                textHomeNbUser.setText(getString(R.string.textNbUser).concat(String.valueOf(ListeStats.getNbUsers())));
                textHomeNbAnnonce.setText(getString(R.string.textNbAnnonces).concat(String.valueOf(ListeStats.getNbAnnonces())));
            }
        };

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimer.cancel();
        timerTask.cancel();
        mHandler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();

        mTimer = new Timer();

        // Création d'un timerTask pour aller récupérer la liste des catégories toutes les 20 secondes
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // run on another thread
                mHandler.post(runnable);
            }
        };

        // On va récupérer les catégories toutes les 20 secondes.
        mTimer.scheduleAtFixedRate(timerTask, 0, MainActivity.NOTIFY_INTERVAL);
    }
}
