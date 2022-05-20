package com.example.hikingapp.ui.profile.completed

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.users.PhotoItem
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.PhotoAdapter
import com.example.hikingapp.ui.profile.saved.CompletedViewModel
import com.example.hikingapp.ui.route.photos.PhotoActivity
import com.example.hikingapp.utils.PhotoItemDecorator
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_completed_route_info.view.*
import kotlinx.android.synthetic.main.fragment_completed_route_info.view.progress_bar
import kotlinx.android.synthetic.main.fragment_photos.view.*

class CompletedRouteInfoFragment : Fragment(), OnItemClickedListener {

    private val completedViewModel: CompletedViewModel by activityViewModels()

    private lateinit var route: Route
    private lateinit var completedRoutePhotos: MutableList<PhotoItem?>
    private lateinit var elevationData: MutableList<Long>

    private lateinit var completedPhotosRecyclerView: RecyclerView
    private lateinit var photosAdapter: PhotoAdapter
    private lateinit var layoutManager: GridLayoutManager

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

        val elevationProgressBar = view.progress_bar

        elevationProgressBar.visibility = View.VISIBLE
        completedViewModel.elevationData.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                drawGraph(it as MutableList<Long>?, view.elevation_graph)
                view.elevation_graph.visibility = View.VISIBLE
            }
            elevationProgressBar.visibility = View.GONE
        })

        addRoutePhotos(view)


        return view
    }

    private fun initializeRouteStatistics(view: View) {

        val distanceCovered = view.findViewById(R.id.distance_content) as TextView
        val routeType = view.findViewById(R.id.type_content) as TextView
        val time = view.findViewById(R.id.time_content) as TextView

        // TODO need a NavigationProcess and UserNavigationData object to record navigation data

        completedViewModel.route.observe(viewLifecycleOwner, {
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

        val progressBar = view.findViewById(R.id.progress_bar) as ProgressBar
        progressBar.visibility = View.VISIBLE

        completedPhotosRecyclerView = view.findViewById(R.id.sight_photos_recycler_view)
        layoutManager = GridLayoutManager(context, 5, GridLayoutManager.VERTICAL, false)
        completedPhotosRecyclerView.layoutManager = layoutManager

        completedViewModel.photos.observe(viewLifecycleOwner, {

            val photosLayout = view.findViewById<FrameLayout>(R.id.photos_layout)

            if (it.isNullOrEmpty()) {
                completedPhotosRecyclerView.visibility = View.GONE
                photosLayout.no_photos_found.visibility = View.VISIBLE
            } else {

                completedPhotosRecyclerView.visibility = View.VISIBLE
                photosLayout.no_photos_found.visibility = View.GONE
                completedRoutePhotos = it.toMutableList()
                photosAdapter = PhotoAdapter(context, completedRoutePhotos, this)
                completedPhotosRecyclerView.adapter = photosAdapter

                val photoItemSpacing = PhotoItemDecorator(5)
                completedPhotosRecyclerView.addItemDecoration(photoItemSpacing)
                progressBar.visibility = View.GONE
            }
        })


    }

    private fun drawGraph(elevationData: MutableList<Long>?, graph: GraphView?) {
        val series = LineGraphSeries<DataPoint>()

        elevationData!!.withIndex().filter { it.value != null }.forEach {
            series.appendData(
                DataPoint(it.index.toDouble(), it.value.toDouble()),
                false,
                elevationData.size
            )
        }
        graph?.addSeries(series)
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val photoIntent = Intent(context, PhotoActivity::class.java)
        photoIntent.putExtra("photo_item", completedRoutePhotos[position]?.imageName)
        startActivity(photoIntent)
    }
}