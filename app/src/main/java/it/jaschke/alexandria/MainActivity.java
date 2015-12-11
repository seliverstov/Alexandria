package it.jaschke.alexandria;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import it.jaschke.alexandria.api.Callback;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Callback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SCAN_REQUEST = 0;
    private DrawerLayout mDrawerLayout;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #()}.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReciever;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IS_TABLET = isTablet();
        if(IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
        }else {
            setContentView(R.layout.activity_main);
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar =  getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                startActivityForResult(intent,SCAN_REQUEST);
            }
        });

        title = getTitle();

        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever,filter);

        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==SCAN_REQUEST && resultCode == Activity.RESULT_OK){
            String query = data.getStringExtra(AddBook.SCAN_CONTENTS);
            Log.i(TAG, query);
            mSearchView.setQuery(query, false);
            mSearchView.onActionViewExpanded();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem!=null){
            mSearchView  = (SearchView)searchItem.getActionView();
            //mSearchView.setIconifiedByDefault(false);
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.i(TAG, query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            /*case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String ean) {
        Log.i(TAG,"EAN: "+ean);
        if (!IS_TABLET){
            Intent intent = new Intent(this,DetailsActivity.class);
            intent.putExtra(BookDetail.EAN_KEY,ean);
            startActivity(intent);

        }else{
            Bundle args = new Bundle();
            args.putString(BookDetail.EAN_KEY, getIntent().getStringExtra(BookDetail.EAN_KEY));
            BookDetail fragment = new BookDetail();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.details_fragment_container,fragment).commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(true);
        mDrawerLayout.closeDrawers();

       /* FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;

        switch (item.getItemId()){
            case R.id.navigation_item_books:
                nextFragment = new ListOfBooks();
                break;
            case R.id.navigation_item_scan:
                nextFragment = new AddBook();
                break;
            case R.id.navigation_item_about:
                nextFragment = new About();
                break;
            default:
                return true;

        }*/

       /* fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment)
                .addToBackStack((String) title)
                .commit();*/
        return true;
    }

    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY)!=null){
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void goBack(View view){
        getSupportFragmentManager().popBackStack();
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()<2){
            finish();
        }
        super.onBackPressed();
    }


}