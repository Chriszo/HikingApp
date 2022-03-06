package com.example.hikingapp.ui.profile.completed

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.PhotoAdapter
import com.example.hikingapp.ui.profile.saved.CompletedViewModel
import com.example.hikingapp.ui.route.photos.PhotoActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_completed_route_info.view.*

class CompletedRouteInfoFragment : Fragment(), OnItemClickedListener {

    private val viewModel: CompletedViewModel by activityViewModels()

    private lateinit var route: Route
    private lateinit var completedRoutePhotos: MutableList<Int>
    private lateinit var elevationData: MutableList<Int>

    private lateinit var completedPhotosRecyclerView: RecyclerView
    private lateinit var photosAdapter: PhotoAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var elevationGraph: GraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_completed_route_info, container, false)

        initializeRouteStatistics(view)

        drawElevationGraph(view)

        addRoutePhotos(view)


        return view
    }

    private fun initializeRouteStatistics(view: View) {

        val distanceCovered = view.findViewById(R.id.distance_content) as TextView
        val routeType = view.findViewById(R.id.type_content) as TextView
        val time = view.findViewById(R.id.time_content) as TextView

        // TODO need a NavigationProcess and NavigationResult object to record navigation data

        viewModel.route.observe(viewLifecycleOwner, {
            route = it
            // TODO replace these values with navigation results values for the specific user
            distanceCovered.text = getString(
                R.string.completed_distance_content,
                route.routeInfo!!.distance.toString()
            )
            routeType.text = route.routeInfo?.difficultyLevel?.difficultyLevel
            time.text = getString(
                R.string.completed_time_content,
                route.routeInfo?.timeEstimation.toString()
            )
        })
    }

    private fun addRoutePhotos(view: View) {
        completedPhotosRecyclerView = view.findViewById(R.id.sight_photos_recycler_view)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        completedPhotosRecyclerView.layoutManager = layoutManager

        viewModel.photos.observe(viewLifecycleOwner, {
            completedRoutePhotos = it.toMutableList()
            photosAdapter = PhotoAdapter(completedRoutePhotos, this)
            completedPhotosRecyclerView.adapter = photosAdapter
        })


    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun drawElevationGraph(view: View) {
        elevationGraph = view.elevation_graph as GraphView

        val averageElevation = view.findViewById(R.id.elevation_content) as TextView
        val series = LineGraphSeries<DataPoint>()
        // TODO will need to implement a DB retrieve mechanism
        if (viewModel.elevationData.value.isNullOrEmpty()) {

            viewModel.elevationData.observe(viewLifecycleOwner, {
                elevationData = it.toMutableList()

                elevationData.withIndex().forEach {
                    series.appendData(
                        DataPoint(it.index.toDouble(), it.value.toDouble()),
                        false,
                        elevationData.size
                    )
                }
                averageElevation.text = getString(
                    R.string.completed_elevation_content,
                    String.format("%.2f", elevationData.average())
                )
            })
        }
        elevationGraph.addSeries(series)
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val photoIntent = Intent(context, PhotoActivity::class.java)
        photoIntent.putExtra("photo_item", completedRoutePhotos[position])
        startActivity(photoIntent)
    }
}