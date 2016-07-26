package com.orlanth23.annoncesNC.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.dialogs.NoticeDialogFragment;
import com.orlanth23.annoncesNC.dto.Categorie;
import com.orlanth23.annoncesNC.dto.CurrentUser;
import com.orlanth23.annoncesNC.dto.Message;
import com.orlanth23.annoncesNC.fragment.MessageByAnnoncesFragment;
import com.orlanth23.annoncesNC.utility.Utility;
import com.orlanth23.annoncesNC.webservices.AccessPoint;
import com.orlanth23.annoncesNC.webservices.RetrofitService;
import com.orlanth23.annoncesNC.webservices.ReturnClass;

import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.orlanth23.annoncesNC.utility.Utility.SendDialogByFragmentManager;

public class MessageActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private ProgressDialog prgDialog;
    private AppCompatActivity mActivity = this;
    private ArrayList<Message> messages;
    private Fragment messageByAnnoncesFragment;
    private Fragment messageByReceiversFragment;
    private static String tag = MessageActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_message);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // ToDo - Création des deux fragments avec la liste de message
        // messageByAnnoncesFragment(messages);
        // messageByReceiversFragment(messages);

        /**
         * Appel du RestService qui va nous ramener tous nos messages
         */
        // Création d'une progress bar
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage(getString(R.string.dialog_msg_patience));
        prgDialog.setCancelable(false);

        retrofit.Callback<ReturnClass> myCallback = new retrofit.Callback<ReturnClass>() {
            @Override
            public void success(ReturnClass rs, Response response) {
                prgDialog.hide();
                if (rs.isStatus()) {
                    // Récupération des messages dans notre liste mémoire
                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<Message>>() {
                    }.getType();
                    messages = gson.fromJson(rs.getMsg(), listType);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                prgDialog.hide();
                SendDialogByFragmentManager(getFragmentManager(), getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, tag);
            }
        };

        RetrofitService retrofitService = new RestAdapter.Builder().setEndpoint(AccessPoint.getENDPOINT()).build().create(RetrofitService.class);
        retrofitService.getListMessage(CurrentUser.getInstance().getIdUTI(), myCallback);

    }

    /**
     * Méthode appelée par le Floating Button
     * @param view
     */
    public void mainPostMessage(View view){
        /** Todo appel à l'intent qui permettra de créer un nouveau message
         *
         */
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabLayout/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position){
                case 0:
                    /**
                     * Par Annonce - On va appeler le fragment dédié aux annonces
                     */
                    // fragment = MessageByAnnoncesFragment.newInstance();
                    break;
                case 1:
                    /**
                     * Par Annonce - On va appeler le fragment dédié aux correspondants
                     */
                    // fragment = messageByReceiversFragment;
                    break;
            }
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Par Annonce";
                case 1:
                    return "Par Correspondant";
            }
            return null;
        }
    }
}
