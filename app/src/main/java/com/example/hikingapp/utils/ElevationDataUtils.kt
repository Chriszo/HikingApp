package com.example.hikingapp.utils

import android.os.Build
import android.util.Log
import com.example.hikingapp.domain.map.ExtendedMapPoint
import com.mapbox.api.tilequery.MapboxTilequery
import com.mapbox.geojson.FeatureCollection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ElevationDataUtils {

    suspend fun callElevationDataAPI(
        extendedPoint: ExtendedMapPoint,
        accessToken: String
    ) {

        val elevationQuery = formElevationRequestQuery(extendedPoint, accessToken)

        elevationQuery.enqueueCall(object : Callback<FeatureCollection> {

            override fun onResponse(
                call: Call<FeatureCollection>,
                response: Response<FeatureCollection>
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    if (response.isSuccessful) {

                        response.body()?.features()
                            ?.stream()
                            ?.mapToLong { feature ->
                                feature.properties()?.get("ele")?.asLong!!
                            }
                            ?.max()
                            ?.ifPresent { max ->
//                                currentElevation = max
                            }
                        call.cancel()
                    }
                } else {
                    //TODO add implementation for backwards compatibility
                }
            }

            override fun onFailure(call: Call<FeatureCollection>, t: Throwable) {
                Log.e(Log.ERROR.toString(), "An error occured " + t.message)
                elevationQuery.cancelCall()
                return
            }
        })
    }

    private fun formElevationRequestQuery(
        extendedPoint: ExtendedMapPoint,
        accessToken: String
    ): MapboxTilequery {
        return MapboxTilequery.builder()
            .accessToken(accessToken)
            .tilesetIds(GlobalUtils.TERRAIN_ID)
            .limit(50)
            .layers(GlobalUtils.TILEQUERY_ATTRIBUTE_REQUESTED_ID)
            .query(extendedPoint.point)
            .build()
    }
}