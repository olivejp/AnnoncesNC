package com.orlanth23.annoncesnc.activity;


import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
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
import com.orlanth23.annoncesnc.fragment.MyProfileFragment;
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

import static android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByActivity;

public class MainActivity extends CustomCompatActivity implements InterfaceProfileActivity, NoticeDialogFragment.NoticeDialogListener, CustomUserSignCallback, CustomUnregisterCallback {

    public final static int CODE_POST_ANNONCE = 100;
    public final static int CODE_CONNECT_USER = 200;
    public final static int CODE_SETTINGS = 300;
    public final static int CODE_POST_NOT_LOGGED = 500;
    public final static String PARAM_REQUEST_CODE = "REQUEST_CODE";
    private static final String TAG = MainActivity.class.getName();
    private static final String DIALOG_TAG_EXIT = "EXIT";
    private static final String DIALOG_TAG_NO_ACCOUNT = "DIALOG_TAG_NO_ACCOUNT";
    private static final String PARAM_FRAGMENT = "FRAGMENT";
    private static final String PARAM_TAG = "PARAM_TAG";

    private static final String TAG_MES_ANNONCES = "mes_annonces";
    private static final String TAG_RECENT_ANNONCES = "annonces_recentes";
    private static final String TAG_PROFILE = "profile";
    private static String CURRENT_TAG = TAG_MES_ANNONCES;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private View navHeader;
    private TextView displayNameTv;
    private TextView emailTv;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private Fragment mContent;
    private Toolbar toolbar;

    @Override
    protected void onStart() {
        super.onStart();

        // Tentative de connexion avec l'utilisateur par défaut
        tryAuthenticateUser();

        // Lancement du service SyncAdapter
        SyncUtils.CreateSyncAccount(this);
        getContentResolver();
        ContentResolver.addPeriodicSync(AnnoncesAuthenticatorService.getAccount(), SyncUtils.CONTENT_AUTHORITY, Bundle.EMPTY, SyncUtils.SYNC_FREQUENCY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        navHeader = navigationView.getHeaderView(0);
        displayNameTv = (TextView) navHeader.findViewById(R.id.nav_header_display_name);
        emailTv = (TextView) navHeader.findViewById(R.id.nav_header_email);

        mTitle = getString(R.string.app_name);  // Récupération du titre

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getColor(R.color.white));
        setSupportActionBar(toolbar);

        setUpNavigationView();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar,
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

        // Si il y avait déjà un fragment dans la sauvegarde c'est celui là qu'on va récupérer
        if (savedInstanceState != null) {
            CURRENT_TAG = savedInstanceState.getString(PARAM_TAG);
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, PARAM_FRAGMENT);
        } else {
            CURRENT_TAG = TAG_RECENT_ANNONCES;
            mContent = getHomeFragment();
        }

        loadHomeFragment();
    }

    private void loadHomeFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, mContent, CURRENT_TAG).commit();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (CURRENT_TAG) {
            case TAG_MES_ANNONCES:
                // Mes annonces
                CardViewFragment myAnnoncesFragment = CardViewFragment.newInstance(CardViewFragment.ACTION_ANNONCE_BY_USER, null, null, CurrentUser.getInstance().getIdUTI(), null, null, false);
                return myAnnoncesFragment;
            case TAG_PROFILE:
                // My Profile Fragment
                MyProfileFragment myProfileFragment = MyProfileFragment.newInstance();
                return myProfileFragment;
            default:
                // The most recent annonces first
                CardViewFragment recentAnnonces = CardViewFragment.newInstance(CardViewFragment.ACTION_RECENT_ANNONCES, null, null, null, null, null, false);
                return recentAnnonces;
        }
    }

    private void setUpNavigationView() {
        OnNavigationItemSelectedListener onNavigationItemSelectedListener = new OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem selectedItemMenu) {

                // Passage de tous les menuItem a checked false
                for (int i = 0; i == navigationView.getMenu().size(); i++) {
                    MenuItem menuItem = navigationView.getMenu().getItem(i);
                    menuItem.setChecked(false);
                }

                switch (selectedItemMenu.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_my_annonces:
                        CURRENT_TAG = TAG_MES_ANNONCES;
                        break;
                    case R.id.nav_profile:
                        CURRENT_TAG = TAG_PROFILE;
                        break;
                    default:
                        CURRENT_TAG = TAG_RECENT_ANNONCES;
                }

                selectedItemMenu.setChecked(true);

                mContent = getHomeFragment();

                loadHomeFragment();

                mDrawerLayout.closeDrawers();

                return true;
            }
        };

        navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mContent = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        getSupportFragmentManager().putFragment(outState, PARAM_FRAGMENT, mContent);
        outState.putString(PARAM_TAG, CURRENT_TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // On inflate le mMenu
        getMenuInflater().inflate(R.menu.menu_main, menu);

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
    public void refreshProfileMenu() {
        if (CurrentUser.getInstance().isConnected()) {

            displayNameTv.setText(CurrentUser.getInstance().getDisplayNameUTI());
            emailTv.setText(CurrentUser.getInstance().getEmailUTI());

            try {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_login_inverse);
                }
            } catch (NullPointerException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public void tryAuthenticateUser() {
        // Si on a une connexion
        if (Utility.checkWifiAndMobileData(this)) {
            String password = null;
            // Récupération des données dans le dictionnaire
            String email = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_EMAIL);
            String passwordEncrypted = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_MOT_PASSE);
            if (passwordEncrypted != null) {
                password = PasswordEncryptionService.desDecryptIt(passwordEncrypted);
            }
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
    public void onCompleteUserSign(Utilisateur user) {
        CurrentUser.getInstance().setUser(user);
        refreshProfileMenu();
    }

    @Override
    public void onFailureUserSign(Exception e) {
        Intent intent = new Intent();
        intent.setClass(this, LoginFirebaseActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCancelledUserSign() {
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

}
