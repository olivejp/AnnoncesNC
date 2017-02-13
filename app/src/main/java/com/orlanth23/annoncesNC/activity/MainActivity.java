package com.orlanth23.annoncesNC.activity;


import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orlanth23.annoncesNC.BuildConfig;
import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.adapter.ListCategorieAdapter;
import com.orlanth23.annoncesNC.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesNC.dto.Categorie;
import com.orlanth23.annoncesNC.dto.CurrentUser;
import com.orlanth23.annoncesNC.fragment.CardViewFragment;
import com.orlanth23.annoncesNC.fragment.HomeFragment;
import com.orlanth23.annoncesNC.fragment.MyProfileFragment;
import com.orlanth23.annoncesNC.fragment.SearchFragment;
import com.orlanth23.annoncesNC.interfaces.CustomActivityInterface;
import com.orlanth23.annoncesNC.list.ListeCategories;
import com.orlanth23.annoncesNC.list.ListeStats;
import com.orlanth23.annoncesNC.utility.Constants;
import com.orlanth23.annoncesNC.utility.Utility;
import com.orlanth23.annoncesNC.webservice.AccessPoint;
import com.orlanth23.annoncesNC.webservice.RetrofitService;
import com.orlanth23.annoncesNC.webservice.ReturnWS;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements NoticeDialogFragment.NoticeDialogListener, CustomActivityInterface {

    public final static int CODE_POST_ANNONCE = 100;
    public final static int CODE_CONNECT_USER = 200;
    public final static int CODE_SETTINGS = 300;
    public final static int CODE_POST_NOT_LOGGED = 500;


    public final static String PARAM_REQUEST_CODE = "REQUEST_CODE";

    public static final long NOTIFY_INTERVAL = 15 * 1000; // toutes les 10 seconds on va récupérer la liste des catégories

    private static final String TAG = MainActivity.class.getName();
    private static final String DIALOG_TAG_EXIT = "EXIT";
    private static final String PARAM_FRAGMENT = "FRAGMENT";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private Menu menu;
    private HomeFragment homeFragment = new HomeFragment();
    private Fragment searchFragment = new SearchFragment();
    private ColorDrawable colorDrawable = new ColorDrawable();
    private ListeCategories listeCategories = ListeCategories.getInstance();
    private Fragment mContent;
    private Handler mHandler = new Handler();
    private Runnable runnable;
    private Timer mTimer;
    private RetrofitService retrofitService;
    private TimerTask timerTask;

    // Ce runnable va nous permettre de lancer le rafraichissement du menu
    private Runnable runnableMenu = new Runnable() {
        @Override
        public void run() {
            refreshMenu();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add);
        if (fab != null) {
            if (fab.getVisibility() == View.INVISIBLE) {
                fab.setVisibility(View.VISIBLE);
            }
        }

        mTitle = getString(R.string.app_name);  // Récupération du titre
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout); // Récupération du layout latéral
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu); // Récuparation de la liste latérale

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                new Toolbar(this),
                R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "ActionBar is NullPointerException");
        }

        retrofitService = new RestAdapter.Builder().setEndpoint(AccessPoint.getInstance().getServerEndpoint()).build().create(RetrofitService.class);

        // Création d'un exécutable qui va récupérer les informations sur le serveur
        runnable = new Runnable() {
            @Override
            public void run() {
                // ---------------------------------------
                // RECUPERATION de la liste des catégories
                // ---------------------------------------
                retrofitService.getListCategory(new retrofit.Callback<ReturnWS>() {
                    @Override
                    public void success(ReturnWS rs, Response response) {
                        if (rs.statusValid()) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<ArrayList<Categorie>>() {
                            }.getType();
                            ArrayList<Categorie> categories = gson.fromJson(rs.getMsg(), listType);

                            // On réceptionne la liste des catégories dans l'instance ListeCategories
                            listeCategories = ListeCategories.getInstance();
                            ListeCategories.setMyArrayList(categories);

                            // Création de l'adapter de la liste catégorie
                            ListCategorieAdapter adapter = new ListCategorieAdapter(getApplicationContext(), categories);

                            // J'affecte l'adapter à ma listView
                            mDrawerList.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.dialog_failed_webservice), Toast.LENGTH_LONG).show();
                    }
                });

                // Récupération du nombre d'annonces
                retrofitService.getNbAnnonce(new retrofit.Callback<ReturnWS>() {
                    @Override
                    public void success(ReturnWS rs, Response response) {
                        if (rs.statusValid()) {
                            ListeStats.setNbAnnonces(Integer.valueOf(rs.getMsg()));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });

                // Récupération du nombre d'utilisateur
                retrofitService.getNbUser(new retrofit.Callback<ReturnWS>() {
                    @Override
                    public void success(ReturnWS rs, Response response) {
                        if (rs.statusValid()) {
                            ListeStats.setNbUsers(Integer.valueOf(rs.getMsg()));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });

                refreshMenu();
            }
        };

        // Fermeture du Drawer
        mDrawerLayout.closeDrawer(mDrawerList);

        // Par défaut c'est le HomeFragment qu'on mettra
        mContent = homeFragment;

        // Si il y avait déjà un fragment dans la sauvegarde c'est celui là qu'on va récupérer
        if (savedInstanceState != null) {
            mContent = getFragmentManager().getFragment(savedInstanceState, PARAM_FRAGMENT);
        }


        // Tentative de connexion avec l'utilisateur par défaut
        CurrentUser.getInstance();
        CurrentUser.retrieveConnection(this, runnableMenu);

        getFragmentManager().beginTransaction().replace(R.id.frame_container, mContent, null).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        mContent = getFragmentManager().findFragmentById(R.id.frame_container);
        getFragmentManager().putFragment(outState, PARAM_FRAGMENT, mContent);
    }

    /**
     * Méthode pour rafraichir le menu
     * Si l'utilisateur est connecté on propose l'option de Déconnexion
     * Sinon on propose l'option de Connexion
     */
    public void refreshMenu() {
        if (menu != null) {
            menu.findItem(R.id.action_connect).setVisible(!CurrentUser.isConnected());

            // Gestion du profil accessible uniquement si l'user est connecté
            menu.findItem(R.id.action_my_profile).setVisible(CurrentUser.isConnected());
        }
    }


    /*
     * Création du menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // On inflate le menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // On récupère le menu qui a été créé, pour pouvoir le modifier ultérieurement
        this.menu = menu;

        refreshMenu();

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTimer.cancel();
        timerTask.cancel();
        mHandler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
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
        mTimer.scheduleAtFixedRate(timerTask, 0, NOTIFY_INTERVAL);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_EXIT :
                timerTask.cancel();
                mHandler.removeCallbacks(runnable);
                finish();
                break;
            case Utility.DIALOG_TAG_UNREGISTER :
                // On lance le webservice pour se désinscrire
                retrofitService.unregisterUser(CurrentUser.getInstance().getIdUTI(), new retrofit.Callback<ReturnWS>() {
                    @Override
                    public void success(ReturnWS rs, Response response) {
                        if (rs.statusValid()) {
                            CurrentUser cu = CurrentUser.getInstance();
                            cu.setTelephoneUTI(0);
                            cu.setEmailUTI(null);
                            cu.setIdUTI(0);
                            CurrentUser.setConnected(false);
                            Toast.makeText(getApplicationContext(), "Votre profil a été dévalidé", Toast.LENGTH_LONG).show();
                            refreshMenu();
                            getFragmentManager().popBackStackImmediate();
                        }else{
                            Toast.makeText(getApplicationContext(), rs.getMsg(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.dialog_failed_webservice), Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_EXIT :
                break;
        }
    }

    /**
     * On a sélectionné un élément dans la liste des catégories
     */
    private void selectItem(int position) {
        // Récupération de la catégorie
        mDrawerList.setItemChecked(position, true);
        Categorie cat = listeCategories.getListCategorie().get(position);

        // Changement du nom dans l'ActionBar
        setTitle(cat.getNameCAT());
        changeColorToolBar(Color.parseColor(cat.getCouleurCAT()));

        // On ferme le drawer latéral
        mDrawerLayout.closeDrawer(mDrawerList);

        CardViewFragment cardViewFragment = CardViewFragment.newInstance(CardViewFragment.ACTION_ANNONCE_BY_CATEGORY, null, cat, null, null, null, false);

        getFragmentManager().beginTransaction().replace(R.id.frame_container, cardViewFragment, CardViewFragment.ACTION_ANNONCE_BY_CATEGORY).addToBackStack(null).commit();
    }

    @Override
    public void setTitle(CharSequence title) {
        if (title != null) {
            mTitle = title;
            try {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(mTitle);
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "ActionBar is NullPointerException");
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        menu.findItem(R.id.action_post).setVisible(!drawerOpen);
        menu.findItem(R.id.action_suggestion).setVisible(!drawerOpen);
        menu.findItem(R.id.action_connect).setVisible(!drawerOpen && !CurrentUser.isConnected());
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        menu.findItem(R.id.action_my_profile).setVisible(!drawerOpen && CurrentUser.isConnected());
        menu.findItem(R.id.action_leave).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     *
     * @param view
     */
    public void mainSearch(View view) {
        changeToSearch();
    }

    /**
     * Méthode privée qui permet de lancer un post d'annonce
     * @param view
     * @return
     */
    public void mainPost(View view){
        Intent intent = new Intent();
        Bundle b = new Bundle();

        // Vérification que l'utilisateur est connecté
        if (!CurrentUser.isConnected()) {
            // Ouverture de l'activity pour connecter l'utilisateur
            intent.setClass(this, LoginActivityRetrofit.class);
            b.putInt(PARAM_REQUEST_CODE, CODE_POST_NOT_LOGGED); //Your id
            intent.putExtras(b);
            startActivityForResult(intent, CODE_POST_NOT_LOGGED);
        } else {
            // On est déjà connecté
            // Ouverture de l'activity pour créer une nouvelle annonce

            // Passage d'un paramètre Création
            Bundle bd = new Bundle();
            bd.putString(PostAnnonceActivity.BUNDLE_KEY_MODE, Constants.PARAM_CRE);

            intent.setClass(this, PostAnnonceActivity.class).putExtras(bd);
            startActivityForResult(intent, CODE_POST_ANNONCE);
        }
    }

    /**
     * Méthode pour changer le fragment actuel, en SearchFragment
     *
     * @return
     */
    private boolean changeToSearch() {
        // On va rechercher le fragment qui est en cours d'utilisation
        if (!(getFragmentManager().findFragmentById(R.id.frame_container) instanceof SearchFragment)) {

            // On met le Fragment par défaut (SearchFragment) sauf si c'est déjà lui qui est en cours
            if (searchFragment != null) {
                setTitle(getString(R.string.searchTitle));
                mContent = searchFragment;
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_container, searchFragment, SearchFragment.TAG).addToBackStack(null).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                return true;
            } else {
                // error in creating fragment
                Log.e(TAG, getString(R.string.error_creating_fragment));
                return false;
            }
        } else {
            return true;
        }
    }


    /**
     *
     * @param view
     */
    public void manageAds(View view) {
        // On va rechercher le fragment qui est en cours d'utilisation
        if (!(getFragmentManager().findFragmentById(R.id.frame_container) instanceof CardViewFragment) || (!getFragmentManager().findFragmentById(R.id.frame_container).getTag().equals(CardViewFragment.ACTION_ANNONCE_BY_USER))) {
            if (CurrentUser.isConnected()) {
                // Gestion de mes annonces
                CardViewFragment cardViewFragment = CardViewFragment.newInstance(CardViewFragment.ACTION_ANNONCE_BY_USER, null, null, CurrentUser.getInstance().getIdUTI(), null, null, false);

                // On va remplacer le fragment par celui de la liste d'annonce
                getFragmentManager().beginTransaction().replace(R.id.frame_container, cardViewFragment, CardViewFragment.ACTION_ANNONCE_BY_USER).addToBackStack(null).commit();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // On cache le clavier
        Utility.hideKeyboard(this);

        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {

            case R.id.action_leave:
                timerTask.cancel();
                mHandler.removeCallbacks(runnable);
                finish();
                return true;

            case R.id.action_suggestion:
                String[] TO = {BuildConfig.ADMIN_EMAIL};
                String[] CC = {""};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_CC, CC);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion d'amélioration");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");

                try {
                    startActivityForResult(Intent.createChooser(emailIntent, "Envoyer un email d'amélioration"), 0);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "There is no email client installed.", Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.action_my_profile:
                if (!(getFragmentManager().findFragmentById(R.id.frame_container) instanceof MyProfileFragment)) {
                    if (CurrentUser.isConnected()) {
                        // Gestion de mes annonces
                        MyProfileFragment myProfileFragment = MyProfileFragment.newInstance();

                        // On va remplacer le fragment par celui de la liste d'annonce
                        getFragmentManager().beginTransaction().replace(R.id.frame_container, myProfileFragment, MyProfileFragment.tag).addToBackStack(null).commit();
                        return true;
                    }else{
                        return false;
                    }
                }else{
                    return false;
                }

            case R.id.action_search:
                return changeToSearch();

            case R.id.action_connect:
                if (!CurrentUser.isConnected()) {
                    // Ouverture de l'activity pour connecter l'utilisateur
                    Intent intent = new Intent();
                    intent.setClass(this, LoginActivityRetrofit.class);
                    startActivityForResult(intent, CODE_CONNECT_USER);
                    return true;
                } else {
                    // On a cliqué sur le l'option Déconnexion
                    // On se déconnecte
                    CurrentUser.setConnected(false);
                    refreshMenu();
                    return false;
                }
            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(this, SettingsActivity.class);
                startActivityForResult(intent, CODE_SETTINGS);
                return true;
            case R.id.action_post:
                mainPost(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Méthode qui récupère les résultats des activités filles
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Utility.hideKeyboard(this);

        // On a tenté de poster, mais nous n'étions pas connecté...
        if (requestCode == CODE_POST_NOT_LOGGED) {
            if (resultCode == RESULT_OK) {
                // Maintenant nous sommes connecté et on va poster
                // Ouverture de l'activity pour créer une nouvelle annonce
                // Passage d'un paramètre Création
                Bundle bd = new Bundle();
                bd.putString(PostAnnonceActivity.BUNDLE_KEY_MODE, Constants.PARAM_CRE);

                Intent intent = new Intent();
                intent.setClass(this, PostAnnonceActivity.class).putExtras(bd);
                startActivityForResult(intent, CODE_POST_ANNONCE);
            }
        }
    }

    /**
     * Permet de revenir en arrière sur des fragments
     */
    @Override
    public void onBackPressed() {
        // Fermeture du drawer latéral s'il est ouvert
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                } else {
                    // on demande avant de quitter l'application
                    Utility.SendDialogByActivity(this, getString(R.string.dialog_want_to_quit), NoticeDialogFragment.TYPE_BOUTON_YESNO, NoticeDialogFragment.TYPE_IMAGE_INFORMATION, DIALOG_TAG_EXIT);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(runnable);
    }

    @Override
    public void changeColorToolBar(int color) {
        if (color != 0) {
            try {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    colorDrawable.setColor(color);
                    actionBar.setBackgroundDrawable(colorDrawable);
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "ActionBar is NullPointerException");
            }
        }
    }

    /**
     * On a sélectionné une valeur dans le Drawer
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}