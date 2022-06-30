package com.example.hikingapp.ui.route

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.hikingapp.*
import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.domain.map.ExtendedMapPoint
import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.domain.map.MapPoint
import com.example.hikingapp.domain.navigation.SerializableMapPoint
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.users.PhotoItem
import com.example.hikingapp.domain.users.settings.UserSettings
import com.example.hikingapp.domain.weather.WeatherForecast
import com.example.hikingapp.persistence.entities.RouteMapEntity
import com.example.hikingapp.persistence.local.LocalDatabase
import com.example.hikingapp.persistence.utils.DBUtils
import com.example.hikingapp.services.map.MapService
import com.example.hikingapp.services.map.MapServiceImpl
import com.example.hikingapp.services.weather.WeatherService
import com.example.hikingapp.services.weather.WeatherServiceImpl
import com.example.hikingapp.ui.adapters.ViewPagerAdapter
import com.example.hikingapp.utils.GlobalUtils
import com.example.hikingapp.viewModels.RouteViewModel
import com.example.hikingapp.viewModels.UserViewModel
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
import kotlinx.android.synthetic.main.custom_toolbar.view.*
import kotlinx.android.synthetic.main.route_fragment.view.*
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
import kotlin.Comparator
import kotlin.collections.HashMap

class RouteFragment : Fragment(), BackButtonListener {

    private var userSettings: UserSettings? = null
    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private lateinit var routeMap: String

    private val viewModel: RouteViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var mapService: MapService

    private lateinit var weatherService: WeatherService

    private lateinit var route: Route

    private var authInfo: FirebaseUser? = null

    private var mainPhotoName: String? = null

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private val sightRetrieveLimit: Int? = null

    private lateinit var routeView: View

    private var viewPagerAdapter: ViewPagerAdapter? = null

    private var viewPager: ViewPager2? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        routeView = inflater.inflate(R.layout.route_fragment, container, false)

        //TODO Retrieve current Route Map information
        route = arguments?.get("route") as Route

        authInfo = requireArguments()["authInfo"] as FirebaseUser?
        userViewModel.user.postValue(authInfo)

        userSettings = requireArguments()["userSettings"] as UserSettings?
        userViewModel.userSettings.postValue(userSettings)

        routeView.toolbarContainer.action_bar_title.text = route.routeName

        val actionBarUser = routeView.toolbarContainer.action_bar_user as TextView
        val accountIcon = routeView.toolbarContainer.account_icon as ImageView
        if (authInfo != null) {
            actionBarUser.visibility = View.GONE
            accountIcon.visibility = View.VISIBLE
            (accountIcon).setImageResource(R.drawable.account_icon_foreground)

        } else {
            actionBarUser.visibility = View.VISIBLE
            accountIcon.visibility = View.GONE
        }

        actionBarUser.setOnClickListener {
            if (authInfo == null) {
                val loginIntent = Intent(context, LoginActivity::class.java)
                loginIntent.putExtra("route", route)
                loginIntent.putExtra("action", "discover")
                loginIntent.putExtra(GlobalUtils.LAST_PAGE, RouteFragment::class.java.simpleName)
                startActivity(loginIntent)
            }
        }

        setBackButtonListener()

        val prefs =
            requireActivity().applicationContext.getSharedPreferences("mainPhotoPrefs", 0)
        mainPhotoName = prefs.getString("${route.routeId}", null)

        mapService = MapServiceImpl()
        weatherService = WeatherServiceImpl()

        database.getReference("navDataWithElevation")
            .child(route.routeId.toString())
            .child("serializedMapPoints")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        configureRouteNavigationMapData(snapshot)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        var routeMapEntity = LocalDatabase.getRouteMapContent(route.routeId)

        if (Objects.isNull(routeMapEntity)) {
            database.getReference("routeMaps")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        routeMap = (snapshot.value as HashMap<String, *>).entries
                            .stream()
                            .filter { routeMapEntry -> routeMapEntry.key.split("_")[1].toLong() == route.routeId }
                            .map { it.value as String }
                            .findFirst().orElse(null)



                        storage.getReference("routeMaps/").child(routeMap)
                            .getBytes(GlobalUtils.MEGABYTE * 5)
                            .addOnSuccessListener { routeMapBytes ->

                                route.mapInfo =
                                    mapService.getMapInformation(
                                        String(routeMapBytes),
                                        routeMap
                                    )
                                LocalDatabase.saveRouteMapContent(
                                    route.routeId,
                                    RouteMapEntity(routeMap, String(routeMapBytes))
                                )

                                GlobalScope.launch {

                                    if (viewModel.route.value?.routeInfo?.elevationData.isNullOrEmpty()) {
                                        route.routeInfo!!.elevationData =
                                            setRouteElevationData(route)
                                    }

                                    var persistedSightsFound = false

                                    val sights = sightRetrieveLimit?.let {
                                        LocalDatabase.getSightsOfRoute(route.routeId)
                                            ?.sortedBy { comparator -> comparator.rating }
                                            ?.reversed()
                                            ?.subList(0, it)
                                            ?.toMutableList()
                                    } ?: LocalDatabase.getSightsOfRoute(route.routeId)
                                        ?.sortedBy { comparator -> comparator.rating }
                                        ?.reversed()
                                        ?.toMutableList()

                                    if (Objects.nonNull(sights)) {
                                        route.cultureInfo = CultureInfo(sights)
                                        loadSightsMainPhotos()
                                    } else {
                                        database.getReference("route_sights")
                                            .child("${route.routeId}")
                                            .addValueEventListener(
                                                object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        if (Objects.nonNull(snapshot) && Objects.nonNull(
                                                                snapshot.value
                                                            ) && !(snapshot.value as ArrayList<*>).isNullOrEmpty()
                                                        ) {
                                                            persistedSightsFound = true

                                                            val sights =
                                                                (snapshot.value as ArrayList<HashMap<String, *>>)
                                                                    .stream()
                                                                    .map { sightEntry ->
                                                                        val sightId =
                                                                            sightEntry["sightId"] as Long
                                                                        val sight = Sight(
                                                                            sightId,
                                                                            DBUtils.loadLocation(
                                                                                sightEntry["point"] as HashMap<String, *>?
                                                                            ),
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
                                                                    .sorted(compareBy {
                                                                        it.rating
                                                                    })
                                                                    .collect(Collectors.toList())


                                                            val persistedCultureInfo =
                                                                CultureInfo(sightRetrieveLimit?.let {
                                                                    sights.reversed().subList(
                                                                        0,
                                                                        sightRetrieveLimit
                                                                    ).toMutableList()
                                                                } ?: sights.reversed()
                                                                    .toMutableList())
                                                            route.cultureInfo =
                                                                persistedCultureInfo
                                                            loadSightsMainPhotos()
//                                        viewModel.cultureInfo.postValue(persistedCultureInfo)
                                                        } else {
                                                            viewModel.cultureInfo.postValue(
                                                                CultureInfo(mutableListOf())
                                                            )
                                                        }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {
                                                        TODO("Not yet implemented")
                                                    }

                                                })
                                    }


                                    /* val cultureInfoJob = if (!persistedSightsFound) {
                                 if (viewModel.route.value?.cultureInfo?.sights.isNullOrEmpty()) {

                                     GlobalScope.launch {

                                         if (Objects.isNull(viewModel.cultureInfo.value)) {
                                             route.cultureInfo =
                                                 CultureUtils.retrieveSightInformation(route.mapInfo!!.origin)

                                             loadSightsMainPhotos()
                                             viewModel.cultureInfo.postValue(route.cultureInfo)
                                         }
                                     }
                                 } else {
                                     null
                                 }
                             } else {
                                 null
                             }*/


                                    val weatherInfoJob =
                                        if (viewModel.route.value?.weatherForecast?.weatherForecast.isNullOrEmpty()) {
                                            GlobalScope.launch {
                                                val weatherForecast = WeatherForecast()
                                                weatherForecast.weatherForecast =
                                                    weatherService.getForecastForDays(
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
                            }.addOnFailureListener {
                                if (it is StorageException) {
                                    when (it.httpResultCode) {
                                        404 -> throw IllegalArgumentException(
                                            "[${it.httpResultCode}]: No RouteMap \"$routeMap\" was found in Storage.",
                                            it.fillInStackTrace()
                                        )
                                    }
                                } else {
                                    throw it
                                }
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        } else {
            routeMap = routeMapEntity!!.routeMapName
            route.mapInfo =
                mapService.getMapInformation(routeMapEntity!!.routeMapContent, routeMap)

            GlobalScope.launch {

                if (viewModel.route.value?.routeInfo?.elevationData.isNullOrEmpty()) {
                    route.routeInfo!!.elevationData = setRouteElevationData(route)
                }

                var persistedSightsFound = false

                val sights = sightRetrieveLimit?.let {
                    LocalDatabase.getSightsOfRoute(route.routeId)
                        ?.sortedBy { comparator -> comparator.rating }?.reversed()
                        ?.subList(0, it)
                        ?.toMutableList()
                } ?: LocalDatabase.getSightsOfRoute(route.routeId)
                    ?.sortedBy { comparator -> comparator.rating }?.reversed()
                    ?.toMutableList()

                if (Objects.nonNull(sights)) {
                    route.cultureInfo = CultureInfo(sights)
                    loadSightsMainPhotos()
                } else {
                    database.getReference("route_sights").child("${route.routeId}")
                        .addValueEventListener(
                            object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (Objects.nonNull(snapshot) && Objects.nonNull(
                                            snapshot.value
                                        ) && !(snapshot.value as ArrayList<*>).isNullOrEmpty()
                                    ) {
                                        persistedSightsFound = true

                                        val sights =
                                            (snapshot.value as ArrayList<HashMap<String, *>>)
                                                .stream()
                                                .map { sightEntry ->
                                                    val sightId =
                                                        sightEntry["sightId"] as Long
                                                    val sight = Sight(
                                                        sightId,
                                                        DBUtils.loadLocation(sightEntry["point"] as HashMap<String, *>?),
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
                                                .sorted(compareBy {
                                                    it.rating
                                                })
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
//                                        viewModel.cultureInfo.postValue(persistedCultureInfo)
                                    } else {
                                        viewModel.cultureInfo.postValue(CultureInfo(mutableListOf()))
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
                            weatherForecast.weatherForecast =
                                weatherService.getForecastForDays(
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



        initializeNavigationComponents(routeView)

        initializeButtonListeners(routeView)

        /*val mainPhoto = LocalDatabase.getMainImage(route.routeId, Route::class.java.simpleName)
        if (mainPhoto != null) {
            routeView.route_info_image.setImageDrawable(BitmapDrawable(resources, mainPhoto))
        } else {
            FirebaseStorage.getInstance().getReference("routes/mainPhotos/$mainPhotoName")
                .getBytes(1024 * 1024).addOnSuccessListener {

                    val photoBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    routeView.route_info_image.setImageDrawable(
                        BitmapDrawable(
                            resources,
                            photoBitmap
                        )
                    )
                }
        }*/


        routeView.progress_bar.visibility = View.VISIBLE
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

                            val photoItem =
                                PhotoItem(photoReference.path.split("/").last(), bitmap)
                            route.photos!!.add(photoItem)
                            LocalDatabase.saveImage(
                                route.routeId,
                                Route::class.java.simpleName,
                                photoReference.path.split("/").last(),
                                photoItem,
                                false
                            )
                            if (route?.photos?.size == routePhotosFolder.items.size) {
                                viewModel.photos.postValue(route.photos)
                                configureViewPager()
                            }
                        }
                    }
                }
        } else {
            configureViewPager()
        }

        routeView.routeName.text = route.routeName
        routeView.stateName.text = route.stateName
        routeView.routeRating.rating = route.routeInfo!!.rating!!

        return routeView
    }

    private fun configureViewPager() {

        if (viewPager == null) {
            viewPager = routeView.findViewById(R.id.viewPager)
        }

        viewPagerAdapter = ViewPagerAdapter(route.photos)
        viewPager!!.adapter = viewPagerAdapter
        viewPager!!.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        routeView.progress_bar.visibility = View.GONE

        val handler = Handler()
        var currentPage = 0
        val update = Runnable {
            if (currentPage == route.photos!!.size) {
                currentPage = 0
            }

            //The second parameter ensures smooth scrolling
            viewPager!!.setCurrentItem(currentPage++, true)
        }

        Timer().schedule(object : TimerTask() {
            // task to be scheduled
            override fun run() {
                handler.post(update)
            }
        }, 3500, 3500)

    }

    private fun configureRouteNavigationMapData(snapshot: DataSnapshot) {

        val data = snapshot.value as MutableList<HashMap<String, *>>

        data.forEach {

            val point = SerializableMapPoint(
                it["pointId"] as String,
                it["index"] as Long,
                it["longitude"] as Double,
                it["latitude"] as Double,
                it["elevation"] as Long
            )
            if (route.routeInfo!!.navigationData.isNullOrEmpty()) {
                route.routeInfo!!.navigationData = mutableMapOf()
            }
            route.routeInfo!!.navigationData!![point.pointId] = point
        }

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
                            PhotoItem("sight_${sight.sightId}_main.jpg", bitmap),
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
                viewModel.cultureInfo.postValue(route.cultureInfo)
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeButtonListeners(view: View?) {

        val bookmarkButton = view?.route_bookmark

        if (Objects.nonNull(authInfo)) {
            database.getReference("savedRouteAssociations").child(authInfo!!.uid)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (Objects.nonNull(snapshot) && Objects.nonNull(snapshot.value)) {
                            val savedRouteIds = snapshot.value as MutableList<Long>
                            if (savedRouteIds.contains(route.routeId)) {
                                bookmarkButton?.setImageResource(R.drawable.remove_bookmark_icon_foreground)
                            } else {
                                bookmarkButton?.setImageResource(R.drawable.bookmark_outlined_icon_foreground)
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        } else {
            bookmarkButton?.setImageResource(R.drawable.bookmark_outlined_icon_foreground)
        }


        if (requireArguments().containsKey("action")) {
            when (requireArguments()["action"] as String) {
                "discover" -> {
                    bookmarkButton!!.setOnClickListener {
                        if (Objects.nonNull(authInfo)) {
                            database.getReference("savedRouteAssociations").child(authInfo!!.uid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (Objects.nonNull(snapshot) && Objects.nonNull(snapshot.value)) {
                                            val savedUserRoutes =
                                                snapshot.value as MutableList<Long>
                                            if (!savedUserRoutes.contains(route.routeId)) {
                                                savedUserRoutes.add(route.routeId)
                                                bookmarkButton.setImageResource(R.drawable.remove_bookmark_icon_foreground)
                                            } else {
                                                savedUserRoutes.remove(route.routeId)
                                                bookmarkButton.setImageResource(R.drawable.bookmark_outlined_icon_foreground)
                                            }
                                            database.getReference("savedRouteAssociations")
                                                .child(authInfo!!.uid).setValue(savedUserRoutes)
                                        } else {
                                            database.getReference("savedRouteAssociations")
                                                .child(authInfo!!.uid).setValue(
                                                    mutableListOf(route.routeId)
                                                )
                                            bookmarkButton.setImageResource(R.drawable.remove_bookmark_icon_foreground)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                })
                        } else {
                            val redirectIntent = Intent(context, LoginActivity::class.java)
                            redirectIntent.putExtra(GlobalUtils.LAST_PAGE, "RouteFragment")
                            redirectIntent.putExtra("route", route)
                            startActivity(redirectIntent)
                        }
                    }
                }
                "saved" -> {
                    bookmarkButton!!.setOnClickListener {
                        if (Objects.nonNull(authInfo)) {
                            database.getReference("savedRouteAssociations")
                                .child(authInfo!!.uid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (Objects.nonNull(snapshot) && Objects.nonNull(
                                                snapshot.value
                                            )
                                        ) {
                                            val savedUserRoutes =
                                                snapshot.value as MutableList<Long>
                                            savedUserRoutes.remove(route.routeId)
                                            database.getReference("savedRouteAssociations")
                                                .child(authInfo!!.uid).setValue(savedUserRoutes)
                                            bookmarkButton.setImageResource(R.drawable.bookmark_outlined_icon_foreground)
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

        val showMapButton = view?.show_map
        showMapButton?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, MapActivity::class.java)
                intent.putExtra("route", route)
                it.startActivity(intent)
            }
        }

        val showNavButton = view?.navigate
        showNavButton?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, NavigationActivity::class.java)
                intent.putExtra("route", route)
                intent.putExtra("authInfo", authInfo)
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
                // TODO Make a query to DB when implemented

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
                    viewModel.elevationData.postValue(elevationData)
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
        val extendedMapPoints = filterRoutePoints(mapInfo.mapPoints!!, 1)


        extendedMapPoints.withIndex().forEach {
//            if (it.index in 900..999) {
            GlobalScope.launch {
                pointIndexMap[it.value.point.longitude()
                    .toString() + "," + it.value.point.latitude()
                    .toString()] =
                    it.index
                callElevationDataAPI(it.value, mapInfo, pointIndexMap, elevationData)
            }
//            }

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
            delay(10000)
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
                                database.getReference("elevationData")
                                    .child(route.routeId.toString()).child(index.toString())
                                    .setValue(max)
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

    override fun setBackButtonListener() {
        routeView.toolbarContainer.back_btn.setOnClickListener {
            val redirectIntent = Intent(context, MainActivity::class.java)
            redirectIntent.putExtra("authInfo", authInfo)
            startActivity(redirectIntent)
        }
    }

}