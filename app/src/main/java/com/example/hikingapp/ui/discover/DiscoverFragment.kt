package com.example.hikingapp.ui.discover

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.RouteActivity
import com.example.hikingapp.databinding.FragmentDiscoverBinding
import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.enums.RouteType
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.persistence.mock.db.MockDatabase
import com.example.hikingapp.search.SearchFiltersWrapper
import com.example.hikingapp.search.SearchType
import com.example.hikingapp.search.SearchUtils
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.RouteListAdapter
import com.example.hikingapp.ui.search.results.SearchResultsActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.geojson.Point
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
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

    private lateinit var discoverViewModel: DiscoverViewModel
    private var _binding: FragmentDiscoverBinding? = null

    private val locationManager by lazy {
        activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val searchType = SearchType.BY_PLACE
    private lateinit var searchTerm: String
    private lateinit var searchView: AutoCompleteTextView


    private lateinit var searchEngine: SearchEngine
    private lateinit var searchRequestTask: SearchRequestTask

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

    private val searchCallback = object : SearchSelectionCallback {

        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {
            if (suggestions.isEmpty()) {
                Log.i("SearchApiExample", "No suggestions found")
            } else {
                Log.i(
                    "SearchApiExample",
                    "Search suggestions: $suggestions.\nSelecting first suggestion..."
                )
                searchRequestTask = searchEngine.select(suggestions.first(), this)
            }
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            Log.i("SearchApiExample", "Search result: $result")
        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            Log.i("SearchApiExample", "Category search results: $results")
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Search error", e)
        }
    }

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
            return _binding!!.root
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

        // TODO Populate with database Data
        currentRoutes =
            MockDatabase.mockSearchResults.stream().map { it.third }.collect(Collectors.toList())

        categories = mutableListOf("Top Rated", "Popular", "Easy")

        routeListAdapter =
            RouteListAdapter(categories, currentRoutes, requireContext(), itemClickedListener)
        routesRecyclerView.adapter = routeListAdapter
        routesRecyclerView.setHasFixedSize(true)

        discoverViewModel =
            ViewModelProvider(this)[DiscoverViewModel::class.java]

        val root: View = _binding!!.root


        MapboxSearchSdk.initialize(
            application = requireActivity().application,
            accessToken = getString(R.string.mapbox_access_token),
            locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
        )
        searchEngine = MapboxSearchSdk.getSearchEngine()

        setButtonListeners(root)

        setFiltersScreenListeners(root)

        searchView = root.findViewById(R.id.search_bar) as AutoCompleteTextView

        val countries = MockDatabase.mockSearchResults.stream().map { it.third.routeName }
            .collect(Collectors.toList())

        val countriesAdapter =
            ArrayAdapter<String>(requireContext(), R.layout.simple_item, countries)

        searchView.setAdapter(countriesAdapter)

        searchView.addTextChangedListener {
            searchTerm = it.toString()
        }

        searchView.setOnItemClickListener { _, view, _, _ ->

            searchRoutes(view.searchItem.text.toString())
        }

        root.search_icon.setOnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
                if (StringUtils.isNotBlank(searchTerm)) {
                    searchRoutes(searchTerm)
                }
            }
            true
        }

        root.search_menu_icon.setOnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
                val searchOptionsFrame = root.findViewById(R.id.search_options_frame) as FrameLayout
                if (searchOptionsFrame.visibility == View.VISIBLE) {
                    searchOptionsFrame.visibility = View.GONE
                } else {
                    searchOptionsFrame.visibility = View.VISIBLE
                }
            }
            true
        }


        searchView.setOnKeyListener { _, keyCode, _ ->

            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> {
                    if (StringUtils.isNotBlank(searchTerm)) {
                        searchRoutes(searchTerm)
                    }
                }
            }
            true
        }

        return root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun searchRoutes(searchValue: String) {
        if (searchValue.length >= 4 && searchType == SearchType.BY_PLACE) {
            routeSearchResults = SearchUtils.searchByPlace(searchValue)

            if (routeSearchResults.isNullOrEmpty()) {
                timer.cancel()
                timer = Timer()
                timer.schedule(500) {
                    GlobalScope.launch {

                        routeSearchResults = SearchUtils.performGeocodingAPICall(
                            userLocation,
                            searchValue
                        )
                    }
                }
            } else {
                navigateToSearchResults()
            }
        }
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


        root.search_by_position.setOnClickListener {

            if (root.search_bar.visibility == View.VISIBLE) {
                root.search_bar.visibility = View.GONE
                routeSearchResults = SearchUtils.searchByPosition(userLocation)
                navigateToSearchResults()
            } else {
                root.search_bar.visibility = View.VISIBLE
            }
        }

        root.search_place.setOnClickListener {

            if (root.search_bar.visibility == View.GONE || root.search_bar.visibility == View.INVISIBLE) {
                root.search_bar.visibility = View.VISIBLE
            } else {
                root.search_bar.visibility = View.VISIBLE
            }
        }

        root.search_by_filters.setOnClickListener {
            BottomSheetBehavior.from(_binding!!.filterSheet).apply {
                this.state = BottomSheetBehavior.STATE_EXPANDED
                searchFiltersWrapperBuilder = SearchFiltersWrapper.Builder()
            }
        }

        root.btn_filters_ok.setOnClickListener {
            BottomSheetBehavior.from(_binding!!.filterSheet).apply {

                this.state = BottomSheetBehavior.STATE_COLLAPSED
                val searchFilters = searchFiltersWrapperBuilder.build()
                routeSearchResults = SearchUtils.searchByFilters(searchFilters).toMutableList()
                navigateToSearchResults()
            }
        }
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
//        searchRequestTask.cancel()
        super.onDestroy()
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {

        val intent = Intent(context, RouteActivity::class.java)
        intent.putExtra("route", currentRoutes[position])
        startActivity(intent)
    }

    override fun onLocationChanged(location: Location) {
        userLocation = Point.fromLngLat(location.longitude, location.latitude)
    }
}