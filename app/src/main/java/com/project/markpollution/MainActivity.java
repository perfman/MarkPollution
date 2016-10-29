package com.project.markpollution;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.project.markpollution.CustomAdapter.ViewPagerAdapter;
import com.project.markpollution.Fragments.MapsFragment;
import com.project.markpollution.Fragments.NewsFeedFragment;
import com.project.markpollution.Objects.PollutionPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ViewPagerAdapter adapter;
    private Toolbar toolbar;
    private TabLayout tabs;
    public ViewPager viewPager;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    public static ArrayList<PollutionPoint> listPo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();

        setupViewPager(viewPager);
        tabs.setupWithViewPager(viewPager);
        setNavigationHeader();
        fetchData();
    }

    private void initView() {
        tabs = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new NewsFeedFragment(), "NEWS FEED");
        adapter.addFragment(new MapsFragment(), "MAPS");
        viewPager.setAdapter(adapter);
        viewPager.requestTransparentRegion(viewPager);
    }

//    private void setupTabIcons() {
//        int[] tabIcons = {
//                R.mipmap.ic_launcher,
//                R.mipmap.ic_launcher,
//        };
//
//        tabs.getTabAt(0).setIcon(tabIcons[0]);
//        tabs.getTabAt(1).setIcon(tabIcons[1]);
//    }

    private void setNavigationHeader(){
        View view = navigationView.getHeaderView(0);
        TextView tvNavName = (TextView) view.findViewById(R.id.username);
        TextView tvNavEmail = (TextView) view.findViewById(R.id.email);
        ImageView ivNavAvatar = (ImageView) view.findViewById(R.id.profile_image);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String avatar = intent.getStringExtra("avatar");
        tvNavName.setText(name);
        tvNavEmail.setText(email);
        new getAvatar(ivNavAvatar).execute(avatar);
    }

    private class getAvatar extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public getAvatar(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap bitmap = null;
            try {
                InputStream inputStream = new URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

    private void fetchData(){
        Intent intent = getIntent();
        String po_data = intent.getStringExtra("po_data");
        try {
            JSONObject jObj = new JSONObject(po_data);
            JSONArray arr = jObj.getJSONArray("result");
            listPo = new ArrayList<>();
            for(int i=0; i<arr.length(); i++){
                JSONObject po = arr.getJSONObject(i);
                String id_po = po.getString("id_po");
                String id_cate = po.getString("id_cate");
                String id_user = po.getString("id_user");
                double lat = po.getDouble("lat");
                double lng = po.getDouble("lng");
                String title = po.getString("title");
                String desc = po.getString("desc");
                String image = po.getString("image");
                String time = po.getString("time");

                listPo.add(new PollutionPoint(id_po, id_cate, id_user, lat, lng, title, desc, image, time));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        drawerLayout.closeDrawer(GravityCompat.START);

        switch (id) {
            //Replacing the main content with ContentFragment Which is our Inbox View;
            case R.id.reportmanagement:
                startActivity(new Intent(this, ReportManagementActivity.class));
                return true;
            case R.id.seriousreport:
                Toast.makeText(getApplicationContext(), "Seriousreport Selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.rencentlyreport:
                Toast.makeText(getApplicationContext(), "Rencentlyreport Selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.nearbyreport:
                Toast.makeText(getApplicationContext(), "Nearbyreport Selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.allreport:
                Toast.makeText(getApplicationContext(), "Allreport Selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.nav_share:
                Toast.makeText(getApplicationContext(), "Share Selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_logout:
                Toast.makeText(getApplicationContext(), "Logout Selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
