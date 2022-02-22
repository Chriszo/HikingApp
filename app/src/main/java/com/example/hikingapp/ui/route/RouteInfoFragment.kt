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
import java.util.*

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

        var routeType = view.type_content
        var difficultyLevel = view.difficulty_content
        var rating = view.rating_content

        var distance = view.distance_content
        var elevation = view.elevation_content
        var estimatedTime = view.time_est_content

        var temperatureDay1 = view.temp_1
        var temperatureDay2 = view.temp_2
        var temperatureDay3 = view.temp_3

        var conditionsDay1 = view.conditions_1
        var conditionsDay2 = view.conditions_2
        var conditionsDay3 = view.conditions_3



        viewModel.route.observe(viewLifecycleOwner, Observer {

            println("ViewModel observes...")

            routeType.text = it.routeInfo?.routeType!!.type
            difficultyLevel.text = it.routeInfo?.difficultyLevel!!.difficultyLevel
            rating.text = it.routeInfo?.rating.toString()
            distance.text = it.routeInfo?.distance.toString()
            elevation.text = "0"
            estimatedTime.text = it.routeInfo?.timeEstimation.toString()

            it.weatherForecast?.weatherForecast?.withIndex()?.forEach {
                when (it.index) {
                    0 -> {
                        println(Date(it.value.time!! * 1000))
                        temperatureDay1.text = it.value.temperatureHigh.toString()
                        conditionsDay1.text = it.value.icon
                    }
                    1 -> {
                        println(Date(it.value.time!! * 1000))
                        temperatureDay2.text = it.value.temperatureHigh.toString()
                        conditionsDay2.text = it.value.icon
                    }
                    2 -> {
                        println(Date(it.value.time!! * 1000))
                        temperatureDay3.text = it.value.temperatureHigh.toString()
                        conditionsDay3.text = it.value.icon
                    }
                }
            }
        })

        viewModel.elevationData.observe(viewLifecycleOwner, Observer
        {
            drawGraph(it as MutableList<Int>?, view.elevation_graph)
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