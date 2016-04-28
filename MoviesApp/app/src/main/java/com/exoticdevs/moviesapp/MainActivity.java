package com.exoticdevs.moviesapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.exoticdevs.Fragments.DetailFragment;
import com.exoticdevs.Fragments.MoviesFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean mTwoPane;
    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportFragmentManager().findFragmentById(R.id.fragmentDetail) != null) {
            mTwoPane = true;
        }

        if(mTwoPane) {
            MoviesFragment moviesFragment = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMovies);
            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentDetail);
            // notifier
            moviesFragment.setFragData(detailFragment);

        }else{
            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
            if (viewPager != null) {
                setupViewPager(viewPager);
            }
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(MoviesFragment.newInstance(getString(R.string.pref_sort_popularity)), getString(R.string.pref_sort_label_popularity));
        adapter.addFragment(MoviesFragment.newInstance(getString(R.string.pref_sort_highestRated)), getString(R.string.pref_sort_label_highestRated));
        adapter.addFragment(MoviesFragment.newInstance(getString(R.string.pref_sort_favorite)), getString(R.string.pref_sort_label_favorite));
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_mostPopular);
        MenuItem item2 = menu.findItem(R.id.action_highestRated);
        MenuItem item3 = menu.findItem(R.id.action_favourit);

        if(mTwoPane){
            item.setVisible(true);
            item2.setVisible(true);
            item3.setVisible(true);

        }else {
            item.setVisible(false);
            item2.setVisible(false);
            item3.setVisible(false);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        MoviesFragment moviesFragment = (MoviesFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentMovies);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_mostPopular) {
            editor.putString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity));
            editor.commit();
            if ( null != moviesFragment ) {
                moviesFragment.onSortChanged();
            }

            return true;
        }else  if (id == R.id.action_highestRated) {
            editor.putString(getString(R.string.pref_sort_key),getString(R.string.pref_sort_highestRated));
            editor.commit();
            if ( null != moviesFragment ) {
                moviesFragment.onSortChanged();
            }
            return true;
        }else  if (id == R.id.action_favourit) {
            editor.putString(getString(R.string.pref_sort_key),getString(R.string.pref_sort_favorite));
            editor.commit();
            if ( null != moviesFragment ) {
                moviesFragment.onSortChanged();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
