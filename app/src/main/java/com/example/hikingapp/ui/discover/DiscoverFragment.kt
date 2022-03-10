package com.example.hikingapp.ui.discover

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.RouteActivity
import com.example.hikingapp.databinding.FragmentDiscoverBinding
import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.enums.RouteType
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.route.RouteInfo
import com.example.hikingapp.persistence.utils.DBUtils
import com.example.hikingapp.search.SearchFiltersWrapper
import com.example.hikingapp.search.SearchType
import com.example.hikingapp.search.SearchUtils
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.RouteListAdapter
import com.example.hikingapp.ui.search.results.SearchResultsActivity
import com.example.hikingapp.ui.viewModels.RouteViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.geojson.Point
import kotlinx.android.synthetic.main.fragment_discover.view.*
import kotlinx.android.synthetic.main.simple_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import java.io.Serializable
import java.util.*
import java.util.stream.Collectors
import kotlin.concurrent.schedule


class DiscoverFragment : Fragment(), OnItemClickedListener, LocationListener {

    private var _binding: FragmentDiscoverBinding? = null

    private val locationManager by lazy {
        activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val searchType = SearchType.BY_PLACE
    private lateinit var searchTerm: String
    private lateinit var searchView: AutoCompleteTextView
    private lateinit var searchOptionsFrame: LinearLayout

    private lateinit var searchFiltersWrapperBuilder: SearchFiltersWrapper.Builder

    private lateinit var userLocation: Point

    private var timer = Timer()

    private lateinit var routesRecyclerView: RecyclerView
    private lateinit var routeListAdapter: RouteListAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var currentRoutes: MutableList<Route>
    private lateinit var routeSearchResults: MutableList<Route>
    private lateinit var categories: MutableList<String>

    private lateinit var itemClickedListener: OnItemClickedListener

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private val routeViewModel: RouteViewModel by activityViewModels()

    private lateinit var progressDialog: ProgressDialog


    override fun onAttach(context: Context) {
        super.onAttach(context)
        itemClickedListener = this
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 234
            )
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)

        BottomSheetBehavior.from(_binding!!.filterSheet).apply {
            this.peekHeight = 0
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        currentRoutes = mutableListOf()

        layoutManager = LinearLayoutManager(context)

        routesRecyclerView = _binding!!.searchResultsList
        routesRecyclerView.layoutManager = layoutManager

        val root: View = _binding!!.root

        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Please wait...")
        progressDialog.setMessage("Loading Routes...")
        progressDialog.setCanceledOnTouchOutside(false)


        searchView = root.findViewById(R.id.search_bar) as AutoCompleteTextView
        searchOptionsFrame = root.findViewById(R.id.search_options_layout) as LinearLayout


        // TODO Populate with database Data
        searchView.addTextChangedListener {
            searchTerm = it.toString()
        }

        searchView.setOnItemClickListener { _, view, _, _ ->

            if (searchOptionsFrame.visibility == View.VISIBLE) {
                searchOptionsFrame.visibility = View.GONE
            }
            searchRoutes(view.searchItem.text.toString(), currentRoutes)
        }

        root.search_icon.setOnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
                if (StringUtils.isNotBlank(searchTerm)) {
                    if (searchOptionsFrame.visibility == View.VISIBLE) {
                        searchOptionsFrame.visibility = View.GONE
                    }
                    searchRoutes(searchTerm, currentRoutes)
                }
            }
            true
        }

        root.search_menu_icon.setOnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
                if (searchOptionsFrame.visibility == View.VISIBLE) {
                    searchOptionsFrame.visibility = View.GONE
                    routesRecyclerView.alpha = 1f
                } else {
                    searchOptionsFrame.visibility = View.VISIBLE
                    routesRecyclerView.alpha = 0.4f
                }
            }
            true
        }


        searchView.setOnKeyListener { _, keyCode, _ ->

            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> {
                    if (StringUtils.isNotBlank(searchTerm)) {
                        if (searchOptionsFrame.visibility == View.VISIBLE) {
                            searchOptionsFrame.visibility = View.GONE
                        }
                        searchRoutes(searchTerm, currentRoutes)
                    }
                }
            }
            true
        }


        database.getReference("routes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.show()
                currentRoutes = (snapshot.value as HashMap<String, *>)
                    .entries
                    .map { DBUtils.mapToRouteEntity(it.value as HashMap<String, String>) }
                    .map {
                        Route(
                            it.routeId, it.routeName, it.stateName, it.mainPhoto,
                            RouteInfo(
                                it.distance,
                                it.timeEstimation,
                                it.routeType,
                                it.difficultyLevel,
                                it.rating,
                                mutableListOf()
                            ), null, null, null, mutableListOf()
                        )
                    }.stream()
                    .sorted(Comparator.comparing(Route::routeId))
                    .collect(Collectors.toList())

                routeViewModel.currentRoutes.postValue(currentRoutes)

                categories = mutableListOf("Top Rated", "Popular", "Easy")

                routeListAdapter =
                    RouteListAdapter(
                        categories,
                        currentRoutes,
                        requireContext(),
                        itemClickedListener
                    )
                routesRecyclerView.adapter = routeListAdapter
                routesRecyclerView.setHasFixedSize(true)

                setButtonListeners(root)

                setFiltersScreenListeners(root)

                val routeNames =
                    currentRoutes.stream().map { it.routeName }.collect(Collectors.toList())

                val routeNamesAdapter =
                    ArrayAdapter<String>(requireContext(), R.layout.simple_item, routeNames)

                searchView.setAdapter(routeNamesAdapter)
                progressDialog.dismiss()

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return root

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun searchRoutes(searchValue: String, routes: List<Route>) {

        database.getReference("keywords").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val keywords = (snapshot.value as HashMap<String, Long>)

                if (searchValue.length >= 4 && searchType == SearchType.BY_PLACE) {
                    routeSearchResults = SearchUtils.searchByPlace(searchValue, routes, keywords)

                    if (routeSearchResults.isNullOrEmpty()) {

                        database.getReference("mapData")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    timer.cancel()
                                    timer = Timer()
                                    timer.schedule(500) {

                                        val originsMap = mutableMapOf<String, Point>()

                                        (snapshot.value as HashMap<String, *>).entries
                                            .forEach { routeEntry ->

                                                val originPoint =
                                                    (routeEntry.value as HashMap<String, *>).entries
                                                        .stream()
                                                        .filter { it.key == "origin" }
                                                        .map {
                                                            val point =
                                                                it.value as HashMap<String, *>
                                                            Point.fromLngLat(
                                                                point["longitude"] as Double,
                                                                point["latitude"] as Double
                                                            )
                                                        }
                                                        .findFirst()
                                                        .orElse(null)
                                                originsMap.putIfAbsent(routeEntry.key, originPoint)
                                            }


                                        GlobalScope.launch {

                                            routeSearchResults =
                                                SearchUtils.performGeocodingAPICall(
                                                    userLocation,
                                                    searchValue,
                                                    routes,
                                                    originsMap
                                                )
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })


                    } else {
                        navigateToSearchResults()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun navigateToSearchResults() {
        val intent = Intent(context, SearchResultsActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("routes", routeSearchResults as Serializable)
        intent.putExtra("routesBundle", bundle)
        searchTerm = ""
        searchView.setText(searchTerm)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setButtonListeners(root: View) {


        database.getReference("mapData").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val originsMap = mutableMapOf<String, Point>()

                (snapshot.value as HashMap<String, *>).entries
                    .forEach { routeEntry ->

                        val originPoint = (routeEntry.value as HashMap<String, *>).entries
                            .stream()
                            .filter { it.key == "origin" }
                            .map {
                                val point = it.value as HashMap<String, *>
                                Point.fromLngLat(
                                    point["longitude"] as Double,
                                    point["latitude"] as Double
                                )
                            }
                            .findFirst()
                            .orElse(null)
                        originsMap.putIfAbsent(routeEntry.key, originPoint)
                    }


                root.search_by_position.setOnClickListener {

                    if (root.search_bar.visibility == View.VISIBLE) {
                        routeSearchResults =
                            SearchUtils.searchByPosition(userLocation, currentRoutes, originsMap)
                        navigateToSearchResults()
                        searchOptionsFrame.visibility = View.GONE
                        routesRecyclerView.alpha = 1f
                    } else {
                        root.search_bar.visibility = View.VISIBLE
                    }
                }

                root.search_by_filters.setOnClickListener {
                    BottomSheetBehavior.from(_binding!!.filterSheet).apply {
                        searchOptionsFrame.visibility = View.GONE
                        this.state = BottomSheetBehavior.STATE_EXPANDED
                        searchFiltersWrapperBuilder = SearchFiltersWrapper.Builder()
                    }
                }

                root.btn_filters_ok.setOnClickListener {
                    BottomSheetBehavior.from(_binding!!.filterSheet).apply {

                        this.state = BottomSheetBehavior.STATE_COLLAPSED
                        val searchFilters = searchFiltersWrapperBuilder.build()
                        routeSearchResults =
                            SearchUtils.searchByFilters(searchFilters, currentRoutes)
                                .toMutableList()
                        navigateToSearchResults()
                        routesRecyclerView.alpha = 1f
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun setFiltersScreenListeners(view: View) {
        view.btn_linear.setOnClickListener {

            searchFiltersWrapperBuilder.withType(RouteType.LINEAR)
        }

        view.btn_cyclic.setOnClickListener {
            searchFiltersWrapperBuilder.withType(RouteType.CYCLIC)
        }

        view.btn_easy.setOnClickListener {
            searchFiltersWrapperBuilder.withDifficulty(DifficultyLevel.EASY)
        }

        view.btn_moderate.setOnClickListener {
            searchFiltersWrapperBuilder.withDifficulty(DifficultyLevel.MODERATE)
        }

        view.btn_hard.setOnClickListener {
            searchFiltersWrapperBuilder.withDifficulty(DifficultyLevel.HARD)
        }

        view.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            searchFiltersWrapperBuilder.withRating(rating)
        }

        view.distance_slider.addOnChangeListener { _, value, _ ->
            println(value)
            searchFiltersWrapperBuilder.withDistance(value.toDouble())
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {

        val intent = Intent(context, RouteActivity::class.java)
        intent.putExtra("route", currentRoutes[position])
        intent.putExtra("action", "normal")
        startActivity(intent)
    }

    override fun onLocationChanged(location: Location) {
        userLocation = Point.fromLngLat(location.longitude, location.latitude)
    }
}