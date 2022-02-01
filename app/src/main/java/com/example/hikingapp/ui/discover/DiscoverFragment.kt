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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.hikingapp.R
import com.example.hikingapp.databinding.FragmentDiscoverBinding
import com.example.hikingapp.domain.Route
import com.example.hikingapp.search.SearchType
import com.example.hikingapp.search.SearchUtils
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import kotlinx.android.synthetic.main.fragment_discover.view.*


class DiscoverFragment : Fragment() {

    private lateinit var discoverViewModel: DiscoverViewModel
    private var _binding: FragmentDiscoverBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var searchEngine: SearchEngine
    private lateinit var searchRequestTask: SearchRequestTask

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        discoverViewModel =
            ViewModelProvider(this).get(DiscoverViewModel::class.java)

        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val searchType = SearchType.BY_PLACE

        val searchQueryOptions = SearchUtils.defineSearchQueryOptions(searchType)


        MapboxSearchSdk.initialize(
            application = requireActivity().application,
            accessToken = getString(R.string.mapbox_access_token),
            locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
        )
        searchEngine = MapboxSearchSdk.getSearchEngine()

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
//                println("Made a geocoding API call")
                return true
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText?.length!! < 4) {
                    return false
                }

                if (searchType == SearchType.BY_PLACE) {

                    val routesFound = SearchUtils.searchDatabaseByPlace(newText)

                    if (!routesFound.isNullOrEmpty()) {

                        routesFound.forEach { route ->
                            root.text_discover.text = route.routeName
                        }
                    } else {

                        // Make Geocoding API Call
                        SearchUtils.performGeocodingAPICall(
                            searchEngine,
                            newText,
                            searchQueryOptions,
                            searchCallback
                        )

                    }
                }
                return true
            }

        })

        val textView: TextView = binding.textDiscover
        discoverViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })

        return root
    }


    override fun onDestroy() {
        searchRequestTask.cancel()
        super.onDestroy()
    }
}