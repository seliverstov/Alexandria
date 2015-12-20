package it.jaschke.alexandria;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.sync.SyncAdapter;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Callback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String BOOK_FRAGMENT_TAG = "it.jaschke.alexandria.BOOK_FRAGMENT_TAG";

    private DrawerLayout mDrawerLayout;

    public static boolean IS_TABLET = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar =  getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        IS_TABLET = findViewById(R.id.book_container)!=null;
        if (IS_TABLET) {
            if (savedInstanceState == null || getSupportFragmentManager().findFragmentByTag(BOOK_FRAGMENT_TAG) == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.book_container, new BookDetail(), BOOK_FRAGMENT_TAG).commit();
            }
        }

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                startActivityForResult(intent,ListOfBooks.SCAN_REQUEST);
            }
        });

        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        SyncAdapter.syncNow(this);

   }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!IS_TABLET){
            menu.removeItem(R.id.action_share);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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
    public void onItemSelected(String ean) {
        Log.i(TAG, "EAN: " + ean);
        if (!IS_TABLET){
            Intent intent = new Intent(this,DetailsActivity.class);
            intent.putExtra(BookDetail.EAN_KEY,ean);
            startActivity(intent);

        }else{
            Bundle args = new Bundle();
            args.putString(BookDetail.EAN_KEY, ean);
            BookDetail fragment = new BookDetail();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.book_container,fragment,BOOK_FRAGMENT_TAG).commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.navigation_item_scan: {
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                startActivityForResult(intent, 0);
                break;
            }
            case R.id.navigation_item_settings: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.navigation_item_about: {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getSupportFragmentManager().findFragmentById(R.id.list).onActivityResult(requestCode,resultCode,data);
    }
}