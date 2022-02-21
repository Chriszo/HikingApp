package com.example.hikingapp.ui.route

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.hikingapp.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_route_info.view.*

class RouteInfoFragment : Fragment() {

    private val viewModel: RouteViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_route_info, container, false)

        //TODO Retrieve current Route Map information
        var routeName = savedInstanceState?.get("RouteName")
        val route = viewModel.route.value
        println(route)

        viewModel.route.observe(viewLifecycleOwner, Observer {
            println(it)
            println("ViewModel observes...")
        })

        viewModel.elevationData.observe(viewLifecycleOwner, Observer {
            drawGraph(it as MutableList<Int>?, view.graph)
        })

        return view
    }

    private fun drawGraph(elevationData: MutableList<Int>?, graph: GraphView?) {
        val series = LineGraphSeries<DataPoint>()

        elevationData?.withIndex()?.forEach {
            series.appendData(
                DataPoint(it.index.toDouble(), it.value.toDouble()),
                false,
                elevationData.size
            )
        }
        graph?.addSeries(series)
    }

}