package com.example.hikingapp.ui.profile.completed

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.hikingapp.R
import com.example.hikingapp.ReviewActivity
import com.example.hikingapp.ShareActivity
import com.example.hikingapp.domain.map.ExtendedMapPoint
import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.domain.map.MapPoint
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.persistence.entities.RouteMapEntity
import com.example.hikingapp.persistence.local.LocalDatabase
import com.example.hikingapp.services.map.MapService
import com.example.hikingapp.services.map.MapServiceImpl
import com.example.hikingapp.ui.profile.saved.CompletedViewModel
import com.example.hikingapp.ui.viewModels.RouteViewModel
import com.example.hikingapp.utils.GlobalUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.api.tilequery.MapboxTilequery
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import kotlinx.android.synthetic.main.fragment_completed_route.view.*
import kotlinx.android.synthetic.main.route_fragment.view.*
import kotlinx.android.synthetic.main.route_fragment.view.routeName
import kotlinx.android.synthetic.main.route_fragment.view.routeRating
import kotlinx.android.synthetic.main.route_fragment.view.route_info_image
import kotlinx.android.synthetic.main.route_fragment.view.stateName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashMap

class CompletedRouteFragment : Fragment() {

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val viewModel: RouteViewModel by activityViewModels()
    private lateinit var routeMap: String

    private val completedViewModel: CompletedViewModel by activityViewModels()

    private lateinit var route: Route

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private val mapService: MapService by lazy {
        MapServiceImpl()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_completed_route, container, false)

        route = arguments?.get("route") as Route

        if (completedViewModel.route.value == null) {
            completedViewModel.route.postValue(route)
        }

        val routeMapEntity = LocalDatabase.getRouteMapContent(route.routeId)

        if (Objects.isNull(routeMapEntity)) {
            database.getReference("routeMaps").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    routeMap = (snapshot.value as HashMap<String, *>).entries
                        .stream()
                        .filter { routeMapEntry -> routeMapEntry.key.split("_")[1].toLong() == route.routeId }
                        .map { it.value as String }
                        .findFirst().orElse(null)

                    storage.getReference("routeMaps/").child(routeMap)
                        .getBytes(GlobalUtils.MEGABYTE * 5).addOnSuccessListener {

                            val routeMapEntity = RouteMapEntity(routeMap, String(it))

                            GlobalScope.launch {

                                if (completedViewModel.elevationData.value.isNullOrEmpty()) {

                                    if (route.mapInfo == null) {
                                        route.mapInfo = mapService.getMapInformation(
                                            routeMapEntity.routeMapContent,
                                            routeMap
                                        )
                                        LocalDatabase.saveRouteMapContent(
                                            route.routeId,
                                            routeMapEntity
                                        )
                                    }

                                    route.routeInfo!!.elevationData = setRouteElevationData(route)

                                    completedViewModel.route.postValue(route)
                                    completedViewModel.elevationData.postValue(route.routeInfo!!.elevationData)
                                }
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        } else {
            routeMap = routeMapEntity!!.routeMapName
            GlobalScope.launch {

                if (completedViewModel.elevationData.value.isNullOrEmpty()) {

                    if (route.mapInfo == null) {
                        route.mapInfo =
                            mapService.getMapInformation(routeMapEntity!!.routeMapContent, routeMap)
                    }

                    route.routeInfo!!.elevationData = setRouteElevationData(route)

                    completedViewModel.route.postValue(route)
                    completedViewModel.elevationData.postValue(route.routeInfo!!.elevationData)
                }
            }

        }


        initializeNavigationComponents(view)

        initializeButtonListeners(view)


        val mainPhotoBitmap =
            LocalDatabase.getMainImage(route.routeId, Route::class.java.simpleName)
        if (mainPhotoBitmap != null) {
            view.route_info_image.setImageDrawable(BitmapDrawable(resources, mainPhotoBitmap))
        } else {
            FirebaseStorage.getInstance()
                .getReference("routes/mainPhotos/route_${route.routeId}_main.jpg")
                .getBytes(1024 * 1024).addOnSuccessListener {

                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    view.route_info_image.setImageDrawable(
                        BitmapDrawable(
                            resources,
                            bitmap
                        )
                    )
                }
        }



        route.photos = LocalDatabase.getImages(route.routeId, Route::class.java.simpleName)

        if (route.photos.isNullOrEmpty()) {
            storage?.reference?.child("routes/${route.routeId}/photos")?.listAll()!!
                .addOnSuccessListener { routePhotosFolder ->

                    routePhotosFolder.items.forEach { photoReference ->
                        photoReference.getBytes(1024 * 1024).addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

                            if (route.photos.isNullOrEmpty()) {
                                route.photos = mutableListOf()
                            }
                            route.photos!!.add(bitmap)
                            LocalDatabase.saveImage(
                                route.routeId,
                                Route::class.java.simpleName,
                                photoReference.path.split("/").last(),
                                bitmap,
                                false
                            )
                            if (route?.photos?.size == routePhotosFolder.items.size) {
                                completedViewModel.photos.postValue(route.photos)
                            }
                        }
                    }
                }
        } else {
            completedViewModel.photos.postValue(route.photos)
        }

        view.routeName.text = route.routeName
        view.stateName.text = route.stateName
        view.routeRating.rating = route.routeInfo!!.rating!!
        return view
    }


    private fun initializeButtonListeners(view: View?) {
        val rateButton = view?.rate_button
        rateButton?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, ReviewActivity::class.java)
//                intent.putExtra("routeName", route.routeName)
                it.startActivity(intent)
            }
        }

        val shareButton = view?.share_button
        shareButton?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, ShareActivity::class.java)
//                intent.putExtra("routeName", route.routeName)
                it.startActivity(intent)
            }
        }
    }

    private fun initializeNavigationComponents(view: View) {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val navView = view.info_nav_view
        navView?.setupWithNavController(navController)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    // METHODS FOR RETRIEVING ELEVATION DATA
    private fun filterRoutePoints(
        coordinates: List<MapPoint>,
        modulo: Int
    ): MutableList<ExtendedMapPoint> {

        return coordinates.filterIndexed { index, _ -> index % modulo == 0 }
            .map { mapPoint ->
                ExtendedMapPoint(mapPoint.point, mapPoint.elevation, coordinates.indexOf(mapPoint))
            }
            .toMutableList()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setRouteElevationData(
        route: Route// TODO Remove it when you must. It is used in order to bypass execution during test., graph: com.jjoe64.graphview.GraphView){}, graph: com.jjoe64.graphview.GraphView){}, graph: com.jjoe64.graphview.GraphView){}
    ): MutableList<Long> {
        var elevationData = mutableListOf<Long>()

        if (getString(R.string.prodMode).toBooleanStrict()) { // TODO Remove. Only for test
            if (route.mapInfo!!.elevationDataLoaded) { // Means that these data may be stored in db and can be retrieved from there
                elevationData = (route.mapInfo!!.mapPoints?.stream()?.map { it.elevation }
                    ?.collect(Collectors.toList())?.toMutableList()
                    ?: emptyList<Long>()) as MutableList<Long>
                route.routeInfo?.elevationData = elevationData
            } else {
                // TODO Make a query to DB when implemented
                database.getReference("elevationData").child(route.routeId.toString())
                    .addValueEventListener(object : ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (Objects.nonNull(snapshot) && Objects.nonNull(snapshot.value) && (snapshot.value as ArrayList<Long>).size > 0) {
                                elevationData = snapshot.value as MutableList<Long>
                                route.routeInfo?.elevationData = elevationData
                                completedViewModel.elevationData.postValue(elevationData)
                            } else {
                                GlobalScope.launch {

                                    collectionElevData(route.mapInfo!!).collect { elevationDataList ->
                                        elevationData =
                                            elevationDataList.stream().map { it.elevation }
                                                .collect(Collectors.toList())
                                        route.routeInfo?.elevationData = elevationData
                                    }
                                    completedViewModel.elevationData.postValue(elevationData)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            GlobalScope.launch {

                                collectionElevData(route.mapInfo!!).collect { elevationDataList ->
                                    elevationData =
                                        elevationDataList.stream().map { it.elevation }
                                            .collect(Collectors.toList())
                                    route.routeInfo?.elevationData = elevationData
                                }
                                completedViewModel.elevationData.postValue(elevationData)
                            }
                        }

                    })
                // Data have not been loaded so need Tilequery async API calls to populate data.
                /*GlobalScope.launch {

                    collectionElevData(route.mapInfo!!).collect { elevationDataList ->
                        elevationData =
                            elevationDataList.stream().map { it.elevation }
                                .collect(Collectors.toList())
                        route.routeInfo?.elevationData = elevationData
                    }
                    completedViewModel.elevationData.postValue(elevationData)
                }*/
            }
        }
        return elevationData
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun collectionElevData(
        mapInfo: MapInfo
    ): Flow<MutableList<ExtendedMapPoint>> = flow {

        val pointIndexMap = HashMap<String, Int>()
        var elevationData = mutableListOf<ExtendedMapPoint>()
        val extendedMapPoints = filterRoutePoints(mapInfo.mapPoints!!, 3)


        extendedMapPoints.stream().forEach {

            GlobalScope.launch {
                pointIndexMap[it.point.longitude().toString() + "," + it.point.latitude()
                    .toString()] =
                    it.index
                callElevationDataAPI(it, mapInfo, pointIndexMap, elevationData)
            }

        }
        while (elevationData.size != extendedMapPoints.size) {
            // wait
        }
        elevationData = elevationData
            .stream()
            .filter { it.elevation != -10000L }
            .sorted(Comparator.comparing(ExtendedMapPoint::index))
            .collect(Collectors.toList()).toMutableList()

        emit(elevationData)
    }


    private suspend fun callElevationDataAPI(
        extendedPoint: ExtendedMapPoint,
        mapInfo: MapInfo,
        pointIndexMap: HashMap<String, Int>,
        elevationData: MutableList<ExtendedMapPoint>
    ) {

        if (extendedPoint.index % 50 == 0) {
            delay(3000)
            println("WAITING")
        }

        val elevationQuery = formElevationRequestQuery(extendedPoint)

        elevationQuery.enqueueCall(object : Callback<FeatureCollection> {

            override fun onResponse(
                call: Call<FeatureCollection>,
                response: Response<FeatureCollection>
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    if (response.isSuccessful) {
                        val point =
                            (response.body()?.features()?.get(0)?.geometry() as Point)
                        val pointsMapKey =
                            point.longitude().toString() + "," + point.latitude().toString()


                        response.body()?.features()
                            ?.stream()
                            ?.mapToLong { feature ->
                                feature.properties()?.get("ele")?.asLong!!
                            }
                            ?.max()
                            ?.ifPresent { max ->
                                val index = pointIndexMap[pointsMapKey]
                                mapInfo.mapPoints?.get(index!!)?.elevation = max
                                extendedPoint.elevation = max
                                elevationData.add(extendedPoint)
                            }
                        Log.d(
                            "R",
                            "" + elevationData.indexOf(extendedPoint) + ", " + extendedPoint.elevation
                        )
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

    private fun formElevationRequestQuery(extendedPoint: ExtendedMapPoint): MapboxTilequery {
        return MapboxTilequery.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .tilesetIds(GlobalUtils.TERRAIN_ID)
            .limit(50)
            .layers(GlobalUtils.TILEQUERY_ATTRIBUTE_REQUESTED_ID)
            .query(extendedPoint.point)
            .build()
    }
}