package br.com.lrdsilva.tvmazeapp.activities;

import java.util.ArrayList;
import java.util.List;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;



import br.com.lrdsilva.tvmazeapp.R;
import br.com.lrdsilva.tvmazeapp.adapters.GridAdapter;
import br.com.lrdsilva.tvmazeapp.helpers.HawkOperations;
import br.com.lrdsilva.tvmazeapp.helpers.NetworkingStatus;
import br.com.lrdsilva.tvmazeapp.interfaces.TvMazeApiEndPoints;
import br.com.lrdsilva.tvmazeapp.models.Show;
import br.com.lrdsilva.tvmazeapp.models.Shows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class ActivityAllShows extends AppCompatActivity {

    RecyclerView tvShowsGalleryView;
    DrawerLayout rootDrawer;
    ActionBarDrawerToggle rootDrawerToogle;
    NavigationView menuDrawer;
    TextView noConnection;
    Button retry;


    boolean isFirstTime = true;
    boolean hitBottom =false;
    int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_shows);
        initGallery();
        initDrawerMenu();

        if(new NetworkingStatus(this).isNetworkAvailable()){
           getMoreItens();

        }
        else{
            noConnection.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(GridAdapter.adapter != null && GridAdapter.adapter.changeToggle)
        {
            GridAdapter.adapter.changeToggleNow();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(rootDrawerToogle.onOptionsItemSelected(item)){

           return true;

        }


        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
    }


    private void searchByName(String name)
    {
        initGallery();
        Retrofit retrofit = initRetrofit();
        if(!new NetworkingStatus(this).isNetworkAvailable())
        {
            Toast.makeText(ActivityAllShows.this, "Network not available!", Toast.LENGTH_SHORT).show();
            return;
        }

        TvMazeApiEndPoints tvMazeApiEndPoints = retrofit.create(TvMazeApiEndPoints.class);

        Call<List<Shows>> call = tvMazeApiEndPoints.getShows(name);

        call.enqueue(new Callback<List<Shows>>() {

            @Override
            public void onResponse(Call<List<Shows>> call, Response<List<Shows>> response) {

                ArrayList<Show> onlyShow = new ArrayList<Show>();
                if(response.isSuccessful())
                    for(Shows s : response.body())
                    {

                        onlyShow.add(s.getShow());
                        if(onlyShow.get(0) != null&& tvShowsGalleryView.getAdapter() != null) {

                            GridAdapter gridAdapter = new GridAdapter(getApplicationContext(), (ArrayList<Show>) onlyShow, initFavoritos());
                            tvShowsGalleryView.setAdapter(gridAdapter);
                        }
                    }
                else
                {
                    switch(response.code())
                    {
                        case 404:

                            Toast.makeText(ActivityAllShows.this, "Page not Found", Toast.LENGTH_SHORT).show();
                            break;

                        case 408:

                            Toast.makeText(ActivityAllShows.this, "Timeout Request", Toast.LENGTH_SHORT).show();
                            break;

                        case 500:

                            Toast.makeText(ActivityAllShows.this, "Internal Server Error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Shows>> call, Throwable t) {

            }
        });
    }

    protected void getPage(int page)
    {

        Retrofit retrofit = initRetrofit();
        TvMazeApiEndPoints tvMazeApiEndPoints = retrofit.create(TvMazeApiEndPoints.class);
        if(!new NetworkingStatus(this).isNetworkAvailable())
        {
            Toast.makeText(ActivityAllShows.this, "Network not available!", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<List<Show>> call = tvMazeApiEndPoints.getAllShows(page);
        call.enqueue(new Callback<List<Show>>() {

            @Override
            public void onResponse(Call<List<Show>> call, Response<List<Show>> response) {

                Log.d("Retrofit", ""+response.code());
                if(response.isSuccessful()){

                    List<Show> shows = response.body();
                    initGridAdapter((ArrayList<Show>) shows);
                    hitBottom = false;

                }else
                {
                    switch(response.code())
                    {

                        case 404:

                            Toast.makeText(ActivityAllShows.this, "Page not Found", Toast.LENGTH_SHORT).show();
                            break;

                        case 408:

                            Toast.makeText(ActivityAllShows.this, "Timeout Request", Toast.LENGTH_SHORT).show();
                            break;

                        case 500:

                            Toast.makeText(ActivityAllShows.this, "Internal Server Error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Show>> call, Throwable t) {
                Toast.makeText(ActivityAllShows.this,""+t.getMessage(),Toast.LENGTH_SHORT);
            }
        });


    }

    protected void initGallery()
    {

        tvShowsGalleryView = (RecyclerView) findViewById(R.id.tvshowsgallery);
        retry = (Button) findViewById(R.id.retryButton);
        noConnection = (TextView) findViewById(R.id.No_Conection_Text);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("test", "Retry");
                if(new NetworkingStatus(getApplicationContext()).isNetworkAvailable()){
                    getMoreItens();
                    noConnection.setVisibility(View.GONE);
                    retry.setVisibility(View.GONE);
                }
            }
        });
        noConnection.setVisibility(View.GONE);
        retry.setVisibility(View.GONE);
        tvShowsGalleryView.setHasFixedSize(false);
        tvShowsGalleryView.setNestedScrollingEnabled(false);

        RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        tvShowsGalleryView.setLayoutManager(gridLayoutManager);

        if(tvShowsGalleryView.getAdapter() == null){
            tvShowsGalleryView.setAdapter(new GridAdapter(getApplicationContext(),new ArrayList<Show>(),new ArrayList<Integer>()));
        }

    }
    protected void getMoreItens() {

        if (isFirstTime)
        {
            getPage(page);
        }
        tvShowsGalleryView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);

                if (!tvShowsGalleryView.canScrollVertically(1 )&& !isFirstTime && !hitBottom){
                    page += 1;
                    getPage(page);
                    hitBottom = true;

                }

            }
        });
    }

    protected Retrofit initRetrofit()
    {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TvMazeApiEndPoints.BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build();

        return retrofit;
    }

    protected void initDrawerMenu()
    {

        //DrawerCode
        rootDrawer = (DrawerLayout) findViewById(R.id.rootdrawer);
        rootDrawerToogle = new ActionBarDrawerToggle(this, rootDrawer,R.string.openDrawer,R.string.closeDrawer);

        menuDrawer = (NavigationView) findViewById(R.id.menudrawer) ;
        rootDrawer.addDrawerListener(rootDrawerToogle);

        menuDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
           int menuItemId =menuItem.getItemId();
           switch (menuItemId)
           {
                case R.id.showsdrawer:
                    //Intent allShows = new Intent(this, ActivityFavoriteShows.class);
                    //startActivity(favorite);
                    break;
                case R.id.favoritesdrawer:
                    //Intent favorite = new Intent(this, ActivityFavoriteShows.class);
                    //startActivity(favorite);
                    break;
                case R.id.leavedrawer:
                    finish();
                    System.exit(0);
                    break;
                default:
                    return false;
            }
            return false;
            }
        });
        rootDrawerToogle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        menuDrawer.setItemIconTintList(null);

    }

    protected void initGridAdapter(ArrayList<Show> show)
    {
        if(show.get(0) != null && isFirstTime) {

            GridAdapter gridAdapter = new GridAdapter(getApplicationContext(),  show, initFavoritos());
            tvShowsGalleryView.setAdapter(gridAdapter);
            isFirstTime=false;

        }else
        {

            GridAdapter gridAdapter= (GridAdapter) tvShowsGalleryView.getAdapter();
            gridAdapter.appendContentToList(show);

        }

    }

    protected ArrayList<Integer> initFavoritos()
    {
        return new HawkOperations(getApplicationContext()).getFavorites("favorites");
    }


}

