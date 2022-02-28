package com.example.hikingapp.ui.discover

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hikingapp.R
import com.example.hikingapp.databinding.FragmentDiscoverBinding
import com.example.hikingapp.domain.DifficultyLevel
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.route.enums.RouteType
import com.example.hikingapp.search.SearchFiltersWrapper
import com.example.hikingapp.search.SearchType
import com.example.hikingapp.search.SearchUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.geojson.Point
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import kotlinx.android.synthetic.main.fragment_discover.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.concurrent.schedule


class DiscoverFragment : Fragment() {

    private lateinit var discoverViewModel: DiscoverViewModel
    private var _binding: FragmentDiscoverBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
//    private val binding get() = _binding!!

    private lateinit var searchEngine: SearchEngine
    private lateinit var searchRequestTask: SearchRequestTask

    private lateinit var searchFiltersWrapperBuilder: SearchFiltersWrapper.Builder

    private lateinit var userLocation: Point

    private var timer = Timer()

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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)

        BottomSheetBehavior.from(_binding!!.filterSheet).apply {
            this.peekHeight = 0
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        discoverViewModel =
            ViewModelProvider(this).get(DiscoverViewModel::class.java)

        val root: View = _binding!!.root

        val searchType = SearchType.BY_PLACE

        userLocation = Point.fromLngLat(
            21.942563928989884,
            39.23945243147539  // TODO Replace with current location of user
        )

        var routes: List<Route>

        MapboxSearchSdk.initialize(
            application = requireActivity().application,
            accessToken = getString(R.string.mapbox_access_token),
            locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
        )
        searchEngine = MapboxSearchSdk.getSearchEngine()

        root.search_position.setOnClickListener {

            if (root.search_bar.visibility == View.VISIBLE) {
                root.search_bar.visibility = View.GONE
                routes = SearchUtils.searchByPosition(userLocation)
                root.text_discover.text = routes[0].routeName
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

        root.search_filters.setOnClickListener {

            BottomSheetBehavior.from(_binding!!.filterSheet).apply {
                this.state = BottomSheetBehavior.STATE_EXPANDED
                searchFiltersWrapperBuilder = SearchFiltersWrapper.Builder()
            }
        }

        root.btn_filters_ok.setOnClickListener {
            BottomSheetBehavior.from(_binding!!.filterSheet).apply {

                this.state = BottomSheetBehavior.STATE_COLLAPSED
                val searchFilters = searchFiltersWrapperBuilder.build()
                routes = SearchUtils.searchByFilters(searchFilters)
                root.text_discover.text =
                    if (!routes.isNullOrEmpty()) routes[0].routeName else "No results found..."
                routes.forEach {
                    println(it)
                }
            }
        }

        setFiltersScreenListeners(root)


        root.search_bar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
//                println("Made a geocoding API call")
                return true
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onQueryTextChange(keyword: String?): Boolean {

                if (keyword?.length!! < 4) {
                    return true
                }

                var routesFound: MutableList<Route>
                if (searchType == SearchType.BY_PLACE) {

                    routesFound = SearchUtils.searchByPlace(keyword)

                    if (!routesFound.isNullOrEmpty()) {

                        routesFound.forEach { route ->
                            root.text_discover.text = route.routeName
                        }
                    } else {

                        // Make Geocoding API Call (MAPBOX API)
                        /*SearchUtils.performGeocodingAPICall(
                            Point.fromLngLat(23.74986906294603,37.99658992267283 ),
                            searchEngine,
                            newText,
                            SearchUtils.defineSearchQueryOptions(searchType),
                            searchCallback
                        )*/

                        //OpenStreetMap API

                        timer.cancel()
                        timer = Timer()
                        timer.schedule(500) {
                            val job = GlobalScope.launch {

                                routesFound = SearchUtils.performGeocodingAPICall(
                                    userLocation,
                                    keyword
                                ) // TODO Change with user's lccation
                            }

                            runBlocking {
                                job.join()
                                if (!routesFound.isNullOrEmpty()) {
                                    root.text_discover.text = routesFound[0].routeName
                                } else {
                                    root.text_discover.text = getString(R.string.route_not_found)
                                }
                            }
                        }

                    }
                }
                return true
            }

        })

        val textView: TextView = root.text_discover
        discoverViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })

        return root
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
        searchRequestTask.cancel()
        super.onDestroy()
    }
}