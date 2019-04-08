package br.com.lrdsilva.tvmazeapp.interfaces;

import java.util.List;

import br.com.lrdsilva.tvmazeapp.models.Show;
import br.com.lrdsilva.tvmazeapp.models.Shows;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface TvMazeApiEndPoints {
    // Request method and URL specified in the annotation
    // Callback for the parsed response is the last parameter
    String  BASE_URL = "http://api.tvmaze.com";
    @GET("/search/shows")
    Call<List<Shows>> getShows(@Query("q") String name);
    @GET("/shows")
    Call<List<Show>> getAllShows(@Query("page")int page);

}
