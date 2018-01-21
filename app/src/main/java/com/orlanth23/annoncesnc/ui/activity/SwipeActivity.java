package com.orlanth23.annoncesnc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.database.DictionaryDAO;
import com.orlanth23.annoncesnc.ui.fragment.DemoFragmentAnnonces;
import com.orlanth23.annoncesnc.ui.fragment.DemoFragmentFavorites;
import com.orlanth23.annoncesnc.ui.fragment.DemoFragmentProfile;
import com.orlanth23.annoncesnc.utility.ZoomOutPageTransformer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SwipeActivity extends AppCompatActivity {
    SectionsPagerAdapter mSectionsPagerAdapter;
    @BindView(R.id.swipe_viewpager)
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_swipe);
        ButterKnife.bind(this);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab_annonces)
    public void onNextButton() {
        if (mViewPager.getCurrentItem() < 2) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        } else {
            DictionaryDAO.insertInto(this, DictionaryDAO.Dictionary.DB_CLEF_FIRST_TIME, "N");

            Intent intent = new Intent();
            intent.setClass(this, LoginFirebaseActivity.class);
            startActivity(intent);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            position = position + 1;
            Fragment retour = null;
            switch (position) {
                case 1:
                    retour = DemoFragmentAnnonces.newInstance();
                    break;
                case 2:
                    retour = DemoFragmentFavorites.newInstance();
                    break;
                case 3:
                    retour = DemoFragmentProfile.newInstance();
                    break;
            }
            return retour;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ANNONCES";
                case 1:
                    return "FAVORITES";
                case 2:
                    return "PROFILE";
            }
            return null;
        }
    }
}
