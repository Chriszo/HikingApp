package com.example.hikingapp.ui.profile.saved

import android.content.Intent
import android.graphics.Bitmap
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
import com.example.hikingapp.LoginActivity
import com.example.hikingapp.R
import com.example.hikingapp.MapActivity
import com.example.hikingapp.NavigationActivity
import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.domain.map.ExtendedMapPoint
import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.domain.map.MapPoint
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.weather.WeatherForecast
import com.example.hikingapp.persistence.local.LocalDatabase
import com.example.hikingapp.services.map.MapService
import com.example.hikingapp.services.map.MapServiceImpl
import com.example.hikingapp.services.weather.WeatherService
import com.example.hikingapp.services.weather.WeatherServiceImpl
import com.example.hikingapp.ui.viewModels.RouteViewModel
import com.example.hikingapp.utils.GlobalUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.mapbox.api.tilequery.MapboxTilequery
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import io.ktor.http.*
import kotlinx.android.synthetic.main.fragment_saved_route.view.*
import kotlinx.android.synthetic.main.route_fragment.view.info_nav_view
import kotlinx.android.synthetic.main.route_fragment.view.navigate
import kotlinx.android.synthetic.main.route_fragment.view.routeName
import kotlinx.android.synthetic.main.route_fragment.view.routeRating
import kotlinx.android.synthetic.main.route_fragment.view.route_info_image
import kotlinx.android.synthetic.main.route_fragment.view.show_map
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

class SavedRouteFragment : Fragment() {

    private var authInfo: FirebaseUser? = null
    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val viewModel: RouteViewModel by activityViewModels()

    private lateinit var routeMap: String

    private lateinit var mapService: MapService

    private lateinit var weatherService: WeatherService

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private lateinit var route: Route

    private val sightRetrieveLimit: Int? = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_saved_route, container, false)

        route = arguments?.get("route") as Route
        authInfo = arguments?.get("authInfo") as FirebaseUser?

        mapService = MapServiceImpl()
        weatherService = WeatherServiceImpl()



        database.getReference("routeMaps").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                routeMap = (snapshot.value as HashMap<String, *>).entries
                    .stream()
                    .filter { routeMapEntry -> routeMapEntry.key.split("_")[1].toLong() == route.routeId }
                    .map { it.value as String }
                    .findFirst().orElse(null)

                route.mapInfo = mapService.getMapInformation(getJson(), routeMap)

                GlobalScope.launch {

                    if (viewModel.route.value?.routeInfo?.elevationData.isNullOrEmpty()) {
                        route.routeInfo!!.elevationData = setRouteElevationData(route)
                    }

                    /*val cultureInfoJob =
                        if (viewModel.route.value?.cultureInfo?.sights.isNullOrEmpty()) {
                            GlobalScope.launch {

                                if (Objects.isNull(viewModel.cultureInfo.value)) {
                                    route.cultureInfo =
                                        CultureUtils.retrieveSightInformation(route.mapInfo!!.origin)
                                    viewModel.cultureInfo.postValue(route.cultureInfo)
                                }
                            }
                        } else {
                            null
                        }*/

                    var persistedSightsFound = false

                    val sights = sightRetrieveLimit?.let {
                        LocalDatabase.getSightsOfRoute(route.routeId)
                            ?.sortedBy { comparator -> comparator.rating }?.reversed()
                            ?.subList(0, it)
                            ?.toMutableList()
                    } ?: LocalDatabase.getSightsOfRoute(route.routeId)
                        ?.sortedBy { comparator -> comparator.rating }?.reversed()?.toMutableList()

                    if (Objects.nonNull(sights)) {
                        route.cultureInfo = CultureInfo(sights)
                        loadSightsMainPhotos()
                    } else {
                        database.getReference("route_sights").child("${route.routeId}")
                            .addValueEventListener(
                                object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (Objects.nonNull(snapshot) && Objects.nonNull(snapshot.value) && !(snapshot.value as ArrayList<*>).isNullOrEmpty()) {
                                            persistedSightsFound = true

                                            val sights =
                                                (snapshot.value as ArrayList<HashMap<String, *>>)
                                                    .stream()
                                                    .map { sightEntry ->
                                                        val sightId = sightEntry["sightId"] as Long
                                                        val sight = Sight(
                                                            sightId,
                                                            null,
                                                            sightEntry["name"] as String,
                                                            sightEntry["description"] as String,
                                                            (sightEntry["rating"] as Double).toFloat(),
                                                            LocalDatabase.getMainImage(
                                                                sightId,
                                                                Sight::class.java.simpleName
                                                            ),
                                                            null
                                                        )
                                                        LocalDatabase.saveSight(
                                                            route.routeId,
                                                            sight
                                                        )
                                                        sight
                                                    }
                                                    .collect(Collectors.toList())

                                            val persistedCultureInfo =
                                                CultureInfo(sightRetrieveLimit?.let {
                                                    sights.reversed().subList(
                                                        0,
                                                        sightRetrieveLimit
                                                    ).toMutableList()
                                                } ?: sights.reversed().toMutableList())
                                            route.cultureInfo = persistedCultureInfo
                                            loadSightsMainPhotos()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                })
                    }


                    val weatherInfoJob =
                        if (viewModel.route.value?.weatherForecast?.weatherForecast.isNullOrEmpty()) {
                            GlobalScope.launch {
                                val weatherForecast = WeatherForecast()
                                weatherForecast.weatherForecast = weatherService.getForecastForDays(
                                    route.mapInfo!!.origin,
                                    4,
                                    getString(R.string.prodMode).toBooleanStrict()
                                ) //TODO remove this test flag when in PROD
                                route.weatherForecast = weatherForecast
                            }
                        } else {
                            null
                        }

//                    cultureInfoJob?.join()
                    weatherInfoJob?.join()

                    viewModel.route.postValue(route)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

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

                    val mainPhotoBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    view.route_info_image.setImageDrawable(
                        BitmapDrawable(
                            resources,
                            mainPhotoBitmap
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
                                viewModel.photos.postValue(route.photos)
                            }
                        }
                    }
                }
        } else {
            viewModel.photos.postValue(route.photos)
        }

        view.routeName.text = route.routeName
        view.stateName.text = route.stateName
        view.routeRating.rating = route.routeInfo!!.rating!!
        return view
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadSightsMainPhotos() {

        val sightMainPhotos = mutableListOf<Bitmap?>()
        route.cultureInfo?.sights?.forEach { sight ->

            sight.mainPhoto =
                LocalDatabase.getMainImage(sight.sightId, Sight::class.java.simpleName)
            if (sight.mainPhoto == null) {

                storage.reference.child("sights/mainPhotos/sight_${sight.sightId}_main.jpg")
                    .getBytes(1024 * 1024 * 5).addOnSuccessListener {

                        val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                        sight.mainPhoto = bitmap
                        sightMainPhotos.add(bitmap)
                        LocalDatabase.saveImage(
                            sight.sightId,
                            Sight::class.java.simpleName,
                            "sight_${sight.sightId}_main.jpg",
                            bitmap,
                            true
                        )
                        if (sightMainPhotos.size == route.cultureInfo!!.sights?.size ?: mutableListOf<Sight>()) {
                            viewModel.cultureInfo.postValue(route.cultureInfo)
                        }
                    }
                    .addOnFailureListener {
                        if (it is StorageException) {
                            if (it.httpResultCode == HttpStatusCode.NotFound.value) {
                                sightMainPhotos.add(null)
                            }
                        }
                        if (sightMainPhotos.size == route.cultureInfo!!.sights?.size ?: mutableListOf<Sight>()) {
                            viewModel.cultureInfo.postValue(route.cultureInfo)
                        }
                    }

            } else {
                sightMainPhotos.add(sight.mainPhoto)
                if (sightMainPhotos.size == route.cultureInfo!!.sights?.size ?: mutableListOf<Sight>()) {
                    viewModel.cultureInfo.postValue(route.cultureInfo)
                }
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        println("on destroy view")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("on destroy")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeButtonListeners(view: View) {

        val removeBookmarkButton = view.route_remove_bookmark

        if (requireArguments().containsKey("action")) {
            when (requireArguments()["action"] as String) {
                "discover" -> {
                    removeBookmarkButton!!.setImageResource(R.drawable.bookmark_outlined_icon_foreground)
                    removeBookmarkButton!!.setOnClickListener {
                        if (Objects.nonNull(authInfo)) {
                            database.getReference("savedRouteAssociations").child(authInfo!!.uid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (Objects.nonNull(snapshot) && Objects.nonNull(snapshot.value)) {
                                            val savedUserRoutes =
                                                snapshot.value as MutableList<Long>
                                            savedUserRoutes.add(route.routeId)
                                            database.getReference("savedRouteAssociations")
                                                .child(authInfo!!.uid).setValue(savedUserRoutes)
                                        } else {
                                            database.getReference("savedRouteAssociations")
                                                .child(authInfo!!.uid).setValue(
                                                    mutableListOf(route.routeId)
                                                )
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                })
                        } else {
                            startActivity(Intent(context, LoginActivity::class.java))
                        }
                    }
                }
                "saved" -> {
                    removeBookmarkButton!!.setImageResource(R.drawable.remove_bookmark_icon_foreground)
                    removeBookmarkButton!!.setOnClickListener {
                        if (Objects.nonNull(authInfo)) {
                            database.getReference("savedRouteAssociations").child(authInfo!!.uid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (Objects.nonNull(snapshot) && Objects.nonNull(snapshot.value)) {
                                            val savedUserRoutes =
                                                snapshot.value as MutableList<Long>
                                            savedUserRoutes.remove(route.routeId)
                                            database.getReference("savedRouteAssociations")
                                                .child(authInfo!!.uid).setValue(savedUserRoutes)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                })
                        } else {
                            startActivity(Intent(context, LoginActivity::class.java))
                        }
                    }
                }
            }
        }


        val showMapButton = view.show_map
        showMapButton?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, MapActivity::class.java)
                intent.putExtra("routeName", route.routeName)
                intent.putExtra("routeMap", routeMap)
                it.startActivity(intent)
            }
        }

        val showNavButton = view.navigate
        showNavButton?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, NavigationActivity::class.java)
                intent.putExtra("routeName", route.routeName)
                intent.putExtra("routeMap", routeMap)
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

    private fun getJson(): String {
        return requireContext().assets.open(routeMap).readBytes()
            .toString(Charsets.UTF_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel = ViewModelProvider(this)[RouteViewModel::class.java]
        // TODO: Use the ViewModel
//        viewModel.defineRoute(route)
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

                database.getReference("elevationData").child(route.routeId.toString())
                    .addValueEventListener(object : ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (Objects.nonNull(snapshot) && Objects.nonNull(snapshot.value) && (snapshot.value as ArrayList<Long>).size > 0) {
                                elevationData = snapshot.value as MutableList<Long>
                                route.routeInfo?.elevationData = elevationData
                                viewModel.elevationData.postValue(elevationData)
                            } else {
                                GlobalScope.launch {

                                    collectionElevData(route.mapInfo!!).collect { elevationDataList ->
                                        elevationData =
                                            elevationDataList.stream().map { it.elevation }
                                                .collect(Collectors.toList())
                                        route.routeInfo?.elevationData = elevationData
                                    }
                                    viewModel.elevationData.postValue(elevationData)
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
                                viewModel.elevationData.postValue(elevationData)
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
                    routeViewModel.elevationData.postValue(elevationData)
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