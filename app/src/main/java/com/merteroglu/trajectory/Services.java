package com.merteroglu.trajectory;

import com.merteroglu.trajectory.Model.Coordinates;
import com.merteroglu.trajectory.Model.ReducedResponse;
import com.merteroglu.trajectory.Model.SearchingBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Mert on 26.02.2018.
 */

public interface Services {


    @POST("reduction")
    Call<ReducedResponse> reduceCoordinates(@Body Coordinates request);

    @POST("search")
    Call<Coordinates> searchCoordinates(@Body SearchingBody request);

}
