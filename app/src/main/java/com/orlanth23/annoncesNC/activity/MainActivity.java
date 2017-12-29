package com.orlanth23.annoncesnc.activity;


import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.orlanth23.annoncesnc.BuildConfig;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.database.DictionaryDAO;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.dto.Utilisateur;
import com.orlanth23.annoncesnc.fragment.CardViewFragment;
import com.orlanth23.annoncesnc.fragment.HomeFragment;
import com.orlanth23.annoncesnc.fragment.MyProfileFragment;
import com.orlanth23.annoncesnc.fragment.SearchFragment;
import com.orlanth23.annoncesnc.interfaces.CustomUnregisterCallback;
import com.orlanth23.annoncesnc.interfaces.CustomUserSignCallback;
import com.orlanth23.annoncesnc.interfaces.InterfaceProfileActivity;
import com.orlanth23.annoncesnc.service.UserService;
import com.orlanth23.annoncesnc.sync.AnnoncesAuthenticatorService;
import com.orlanth23.annoncesnc.sync.SyncUtils;
import com.orlanth23.annoncesnc.utility.Constants;
import com.orlanth23.annoncesnc.utility.PasswordEncryptionService;
import com.orlanth23.annoncesnc.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByActivity;

public class MainActivity extends CustomCompatActivity implements InterfaceProfileActivity, NoticeDialogFragment.NoticeDialogListener, CustomUserSignCallback, CustomUnregisterCallback {

    public static final int CODE_POST_ANNONCE = 100;
    public static final int CODE_CONNECT_USER = 200;
    public static final int CODE_SETTINGS = 300;
    public static final int CODE_POST_NOT_LOGGED = 500;
    public static final String PARAM_REQUEST_CODE = "REQUEST_CODE";
    private static final String DIALOG_TAG_EXIT = "EXIT";
    private static final String DIALOG_TAG_NO_ACCOUNT = "DIALOG_TAG_NO_ACCOUNT";
    private static final String PARAM_FRAGMENT = "FRAGMENT";

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private HomeFragment homeFragment = new HomeFragment();
    private Fragment searchFragment = new SearchFragment();
    private Fragment mContent;
    private Menu mMenu;

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
                mDrawerLayout.closeDrawer(navigationView);
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
        if (!(getFragmentManager().findFragmentById(R.id.frame_container) instanceof CardViewFragment)
                || (!getFragmentManager().findFragmentById(R.id.frame_container).getTag().equals(CardViewFragment.ACTION_ANNONCE_BY_USER))) {
            if (CurrentUser.getInstance().isConnected()) {
                // Gestion de mes annonces
                CardViewFragment cardViewFragment = CardViewFragment.newInstance(CardViewFragment.ACTION_ANNONCE_BY_USER, null, null, CurrentUser.getInstance().getIdUTI(), null, null, false);

                // On va remplacer le fragment par celui de la liste d'annonce
                getFragmentManager().beginTransaction().replace(R.id.frame_container, cardViewFragment, CardViewFragment.ACTION_ANNONCE_BY_USER).addToBackStack(null).commit();
            }
        }
    }

    private void sendEmailImprovement() {
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
    }

    public void tryAuthenticateUser() {

        // Si on a une connexion
        if (Utility.checkWifiAndMobileData(this)) {

            // Vérification que l'utilisateur a demandé la connexion automatique, sinon on sort tout de suite.


            // Récupération des données dans le dictionnaire
            String email = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_EMAIL);
            String passwordEncrypted = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_MOT_PASSE);
            String password = PasswordEncryptionService.desDecryptIt(passwordEncrypted);

            // Si les données d'identification ont été saisies
            if (email != null && password != null && !email.isEmpty() && !password.isEmpty()) {
                UserService.sign(FirebaseAuth.getInstance(), FirebaseDatabase.getInstance(), email, password, this);
            }
        } else {
            // Si pas de connexion, on récupère l'utilisateur enregistré
            CurrentUser.getInstance().getUserFromDictionary(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Tentative de connexion avec l'utilisateur par défaut
        tryAuthenticateUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        TAG = MainActivity.class.getName();

        FloatingActionButton fab = findViewById(R.id.fab_add);
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

        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage(), e);
        }

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

        getFragmentManager().beginTransaction().replace(R.id.frame_container, mContent, null).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mContent = getFragmentManager().findFragmentById(R.id.frame_container);
        getFragmentManager().putFragment(outState, PARAM_FRAGMENT, mContent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // On inflate le mMenu
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // On récupère le mMenu qui a été créé, pour pouvoir le modifier ultérieurement
        mMenu = menu;

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
                UserService.unregister(mAuth, this);
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
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(navigationView);
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
                sendEmailImprovement();
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

    @Override
    public void onBackPressed() {
        // Fermeture du drawer latéral s'il est ouvert
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
    public void refreshProfileMenu() {
        if (mMenu != null) {
            mMenu.findItem(R.id.action_connect).setVisible(!CurrentUser.getInstance().isConnected());

            // Gestion du profil accessible uniquement si l'user est connecté
            mMenu.findItem(R.id.action_my_profile).setVisible(CurrentUser.getInstance().isConnected());
        }
    }

    @Override
    public void onCompleteUserSign(Utilisateur user) {
        CurrentUser.getInstance().setUser(user);
        Toast.makeText(this, "Bienvenue " + CurrentUser.getInstance().getDisplayNameUTI(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFailureUserSign(Exception e) {
        Intent intent = new Intent();
        intent.setClass(this, LoginFirebaseActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCancelledUserSign() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onCompleteUnregister() {
        CurrentUser.getInstance().clear();
        Toast.makeText(getApplicationContext(), "Votre profil a été dévalidé", Toast.LENGTH_LONG).show();
        refreshProfileMenu();
        getFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onFailureUnregister() {
        Toast.makeText(getApplicationContext(), "Impossible de supprimer cet utilisateur.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Utility.hideKeyboard(this);

        // On a tenté de poster, mais nous n'étions pas connecté...
        switch (requestCode) {
            case CODE_POST_NOT_LOGGED:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Connecté avec le compte " + CurrentUser.getInstance().getDisplayNameUTI() + ".", Toast.LENGTH_LONG).show();

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
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Connecté avec le compte " + CurrentUser.getInstance().getDisplayNameUTI() + ".", Toast.LENGTH_LONG).show();
                    refreshProfileMenu();
                }
                break;
        }
    }
}
