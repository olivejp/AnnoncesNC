package com.orlanth23.annoncesnc.activity;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.orlanth23.annoncesnc.BuildConfig;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.adapter.ListCategorieAdapter;
import com.orlanth23.annoncesnc.database.DictionaryDAO;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.dto.Categorie;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.dto.Utilisateur;
import com.orlanth23.annoncesnc.fragment.CardViewFragment;
import com.orlanth23.annoncesnc.fragment.HomeFragment;
import com.orlanth23.annoncesnc.fragment.MyProfileFragment;
import com.orlanth23.annoncesnc.fragment.SearchFragment;
import com.orlanth23.annoncesnc.interfaces.CustomActivityInterface;
import com.orlanth23.annoncesnc.interfaces.CustomUserSignCallback;
import com.orlanth23.annoncesnc.list.ListeCategories;
import com.orlanth23.annoncesnc.list.ListeStats;
import com.orlanth23.annoncesnc.receiver.AnnoncesReceiver;
import com.orlanth23.annoncesnc.service.UserService;
import com.orlanth23.annoncesnc.sync.AnnoncesAuthenticatorService;
import com.orlanth23.annoncesnc.sync.SyncUtils;
import com.orlanth23.annoncesnc.utility.Constants;
import com.orlanth23.annoncesnc.utility.PasswordEncryptionService;
import com.orlanth23.annoncesnc.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByActivity;

public class MainActivity extends CustomCompatActivity implements NoticeDialogFragment.NoticeDialogListener, CustomActivityInterface, CustomUserSignCallback {

    public final static int CODE_POST_ANNONCE = 100;
    public final static int CODE_CONNECT_USER = 200;
    public final static int CODE_SETTINGS = 300;
    public final static int CODE_POST_NOT_LOGGED = 500;
    public final static String PARAM_REQUEST_CODE = "REQUEST_CODE";
    private static final String TAG = MainActivity.class.getName();
    private static final String DIALOG_TAG_EXIT = "EXIT";
    private static final String DIALOG_TAG_NO_ACCOUNT = "DIALOG_TAG_NO_ACCOUNT";
    private static final String PARAM_FRAGMENT = "FRAGMENT";

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.drawer_list_categorie)
    ListView mDrawerListCategorie;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private Menu menu;
    private HomeFragment homeFragment = new HomeFragment();
    private Fragment searchFragment = new SearchFragment();
    private ColorDrawable colorDrawable = new ColorDrawable();
    private ListeCategories listeCategories;
    private Fragment mContent;
    private Activity mActivity = this;
    private AnnoncesReceiver annoncesReceiver;

    private void sendOkLoginToast() {
        Toast.makeText(mActivity, mActivity.getString(R.string.connected_with) + CurrentUser.getInstance().getEmailUTI() + " !", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Instanciation des singletons
        ListeStats.getInstance(this);
        listeCategories = ListeCategories.getInstance(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add);
        if (fab != null) {
            fab.setVisibility(View.VISIBLE);
        }

        mTitle = getString(R.string.app_name);  // Récupération du titre

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
        mDrawerListCategorie.setOnItemClickListener(new DrawerItemClickListener());

        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        // Récupération de la liste des catégories
        mDrawerListCategorie.setAdapter(new ListCategorieAdapter(this, listeCategories.getListCategorie()));

        // Fermeture du Drawer
        mDrawerLayout.closeDrawer(mDrawerListCategorie);

        // Par défaut c'est le HomeFragment qu'on mettra
        mContent = homeFragment;

        // Si il y avait déjà un fragment dans la sauvegarde c'est celui là qu'on va récupérer
        if (savedInstanceState != null) {
            mContent = getFragmentManager().getFragment(savedInstanceState, PARAM_FRAGMENT);
        }

        // Lancement du service SyncAdapter
        SyncUtils.CreateSyncAccount(this);
        getContentResolver();
        ContentResolver.addPeriodicSync(AnnoncesAuthenticatorService.getAccount(), SyncUtils.CONTENT_AUTHORITY, Bundle.EMPTY, SyncUtils.SYNC_FREQUENCY);

        // Tentative de connexion avec l'utilisateur par défaut
        tryRemoteConnection();

        getFragmentManager().beginTransaction().replace(R.id.frame_container, mContent, null).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Création d'un broadcast pour écouter si la connectivité à changer et appeler le syncAdapter
        annoncesReceiver = new AnnoncesReceiver();
        IntentFilter ifilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(annoncesReceiver, ifilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(annoncesReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mContent = getFragmentManager().findFragmentById(R.id.frame_container);
        getFragmentManager().putFragment(outState, PARAM_FRAGMENT, mContent);
    }

    public void refreshProfileMenu() {
        if (menu != null) {
            menu.findItem(R.id.action_connect).setVisible(!CurrentUser.getInstance().isConnected());

            // Gestion du profil accessible uniquement si l'user est connecté
            menu.findItem(R.id.action_my_profile).setVisible(CurrentUser.getInstance().isConnected());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // On inflate le menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // On récupère le menu qui a été créé, pour pouvoir le modifier ultérieurement
        this.menu = menu;

        refreshProfileMenu();

        return true;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_NO_ACCOUNT:
                break;
            case DIALOG_TAG_EXIT:
                finish();
                break;
            case Utility.DIALOG_TAG_UNREGISTER:
                mFirebaseUser.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    CurrentUser cu = CurrentUser.getInstance();
                                    cu.setTelephoneUTI("");
                                    cu.setEmailUTI(null);
                                    cu.setIdUTI("");
                                    CurrentUser.getInstance().setConnected(false);
                                    Toast.makeText(getApplicationContext(), "Votre profil a été dévalidé", Toast.LENGTH_LONG).show();
                                    refreshProfileMenu();
                                    getFragmentManager().popBackStackImmediate();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Impossible de supprimer cet utilisateur.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                break;
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_NO_ACCOUNT:
                break;
            case DIALOG_TAG_EXIT:
                break;
        }
    }

    public void tryRemoteConnection() {
        refreshProfileMenu();

        if (CurrentUser.getInstance().isConnected()) {
            return;
        }

        // Vérification que l'utilisateur a demandé la connexion automatique, sinon on sort tout de suite.
        String connexion_auto = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_AUTO_CONNECT);
        if (connexion_auto == null || !connexion_auto.equals("O")) {
            return;
        }

        // Si on a une connexion
        if (Utility.checkWifiAndMobileData(this)) {
            // Récupération des données dans le dictionnaire
            String email = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_EMAIL);
            String passwordEncrypted = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_MOT_PASSE);
            String password = PasswordEncryptionService.desDecryptIt(passwordEncrypted);

            // Si les données d'identification ont été saisies
            if (email != null && password != null && !email.isEmpty() && !password.isEmpty()) {
                UserService.sign(mAuth, mDatabase, mActivity, email, password, this);
            }
        } else {
            // Si pas de connexion, on récupère l'utilisateur enregistré
            CurrentUser.getInstance().getUserFromDictionary(this);
            sendOkLoginToast();
        }
    }

    /**
     * On a sélectionné un élément dans la liste des catégories
     */
    private void onSelectCategorie(int position) {
        // Récupération de la catégorie
        mDrawerListCategorie.setItemChecked(position, true);
        Categorie cat = listeCategories.getListCategorie().get(position);

        // Changement du nom dans l'ActionBar
        setTitle(cat.getNameCAT());
        changeColorToolBar(Color.parseColor(cat.getCouleurCAT()));

        // On ferme le drawer latéral
        mDrawerLayout.closeDrawer(mDrawerListCategorie);

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
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerListCategorie);
        menu.findItem(R.id.action_refresh).setVisible(!drawerOpen);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        menu.findItem(R.id.action_post).setVisible(!drawerOpen);
        menu.findItem(R.id.action_suggestion).setVisible(!drawerOpen);
        menu.findItem(R.id.action_connect).setVisible(!drawerOpen && !CurrentUser.getInstance().isConnected());
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        menu.findItem(R.id.action_my_profile).setVisible(!drawerOpen && CurrentUser.getInstance().isConnected());
        menu.findItem(R.id.action_leave).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    public void mainPost(View view) {
        Intent intent = new Intent();
        Bundle b = new Bundle();

        // Vérification que l'utilisateur est connecté
        if (!CurrentUser.getInstance().isConnected()) {
            // Ouverture de l'activity pour connecter l'utilisateur
            intent.setClass(this, LoginFirebaseActivity.class);
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

    private boolean changeToSearchFragment() {
        // On va rechercher le fragment qui est en cours d'utilisation
        if (!(getFragmentManager().findFragmentById(R.id.frame_container) instanceof SearchFragment)) {

            // On met le Fragment par défaut (SearchFragment) sauf si c'est déjà lui qui est en cours
            if (searchFragment != null) {
                setTitle(getString(R.string.searchTitle));
                mContent = searchFragment;
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_container, searchFragment, SearchFragment.TAG).addToBackStack(null).commit();
                mDrawerLayout.closeDrawer(mDrawerListCategorie);
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

    public void onClickChangeToSearchFragment(View view) {
        changeToSearchFragment();
    }

    public void onClickManageMesAnnonces(View view) {
        // On va rechercher le fragment qui est en cours d'utilisation
        if (!(getFragmentManager().findFragmentById(R.id.frame_container) instanceof CardViewFragment) || (!getFragmentManager().findFragmentById(R.id.frame_container).getTag().equals(CardViewFragment.ACTION_ANNONCE_BY_USER))) {
            if (CurrentUser.getInstance().isConnected()) {
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

            case R.id.action_refresh:
                ContentResolver.requestSync(AnnoncesAuthenticatorService.getAccount(), SyncUtils.CONTENT_AUTHORITY, Bundle.EMPTY);
                return true;

            case R.id.action_leave:
                SendDialogByActivity(this, getString(R.string.dialog_want_to_quit), NoticeDialogFragment.TYPE_BOUTON_OK, 0, DIALOG_TAG_EXIT);
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
                    Log.e("NotFoundException", ex.getMessage(), ex);
                }
                return true;

            case R.id.action_my_profile:
                if (!(getFragmentManager().findFragmentById(R.id.frame_container) instanceof MyProfileFragment)) {
                    if (CurrentUser.getInstance().isConnected()) {
                        // Gestion de mes annonces
                        MyProfileFragment myProfileFragment = MyProfileFragment.newInstance();

                        // On va remplacer le fragment par celui de la liste d'annonce
                        getFragmentManager().beginTransaction().replace(R.id.frame_container, myProfileFragment, MyProfileFragment.TAG).addToBackStack(null).commit();
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }

            case R.id.action_search:
                return changeToSearchFragment();

            case R.id.action_connect:
                if (!CurrentUser.getInstance().isConnected()) {
                    // Ouverture de l'activity pour connecter l'utilisateur
                    Intent intent = new Intent();
                    intent.setClass(this, LoginFirebaseActivity.class);
                    startActivityForResult(intent, CODE_CONNECT_USER);
                    return true;
                } else {
                    // On a cliqué sur le l'option Déconnexion
                    // On se déconnecte
                    if (mAuth != null) {
                        mAuth.signOut();
                    }
                    CurrentUser.getInstance().setConnected(false);
                    refreshProfileMenu();
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Utility.hideKeyboard(this);

        // On a tenté de poster, mais nous n'étions pas connecté...
        switch (requestCode) {
            case CODE_POST_NOT_LOGGED:
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
                break;
            case CODE_CONNECT_USER:
                if (resultCode == RESULT_OK)
                    refreshProfileMenu();
                break;
        }
    }

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
    public void changeColorToolBar(int color) {
        if (color != 0) {
            try {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    colorDrawable.setColor(color);
                    actionBar.setBackgroundDrawable(colorDrawable);
                }
            } catch (NullPointerException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void onCompleteUserSign(Utilisateur user) {
        refreshProfileMenu();
        CurrentUser.getInstance().setUser(user);
    }

    @Override
    public void onFailureUserSign() {

    }

    @Override
    public void onCancelledUserSign() {    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            onSelectCategorie(position);
        }
    }
}
