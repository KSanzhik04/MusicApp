package com.example.musicapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.musicapp.R;
import com.example.musicapp.adapters.ViewPagerAdapter;
import com.example.musicapp.fragments.PlaylistFragment;
import com.example.musicapp.fragments.ProfileFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUser();
        setupViewPager();
    }

    private void initUser() {
        SharedPreferences prefs = getSharedPreferences("musicapp_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        setupFragments(adapter);

        viewPager.setAdapter(adapter);
        setupTabLayout();
    }

    private void setupFragments(ViewPagerAdapter adapter) {
        adapter.addFragment(ProfileFragment.newInstance(userId), getString(R.string.tab_profile));
        adapter.addFragment(PlaylistFragment.newInstance(userId), getString(R.string.tab_playlist));
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(((ViewPagerAdapter) viewPager.getAdapter()).getPageTitle(position))
        ).attach();
    }
}