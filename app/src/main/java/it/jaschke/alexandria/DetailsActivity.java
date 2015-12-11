package it.jaschke.alexandria;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by a.g.seliverstov on 11.12.2015.
 */
public class DetailsActivity  extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        if (savedInstanceState==null){
            Bundle args = new Bundle();
            args.putString(BookDetail.EAN_KEY, getIntent().getStringExtra(BookDetail.EAN_KEY));
            BookDetail fragment = new BookDetail();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.details_fragment_container,fragment).commit();
        }

    }
}
