package com.example.hikingapp.ui.route

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.hikingapp.R
import com.example.hikingapp.ui.route.viewModels.RouteViewModel
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_route_info.view.*
import java.time.LocalDate

class RouteInfoFragment : Fragment() {

    private val viewModel: RouteViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_route_info, container, false)

        //TODO Retrieve current Route Map information
        var routeName = savedInstanceState?.get("RouteName")
        val route = viewModel.route.value
        println(route)

        val routeType = view.type_content
        val difficultyLevel = view.difficulty_content
        val rating = view.rating_content

        val distance = view.distance_content
        val elevation = view.elevation_content
        val estimatedTime = view.time_est_content

        val temperatureDay1 = view.temp_1
        val temperatureDay2 = view.temp_2
        val temperatureDay3 = view.temp_3

        val conditionsDay1 = view.conditions_1
        val conditionsDay2 = view.conditions_2
        val conditionsDay3 = view.conditions_3

        val dayOfWeek1 = view.dom_1
        val dayOfWeek2 = view.dom_2
        val dayOfWeek3 = view.dom_3



        viewModel.route.observe(viewLifecycleOwner, { it ->

            println("ViewModel observes...")

            routeType.text = it.routeInfo?.routeType!!.type
            difficultyLevel.text = it.routeInfo?.difficultyLevel!!.difficultyLevel
            rating.text = it.routeInfo?.rating.toString()
            distance.text = it.routeInfo?.distance.toString()
            elevation.text = "0"
            estimatedTime.text = it.routeInfo?.timeEstimation.toString()

            it.weatherForecast?.weatherForecast?.withIndex()?.forEach { weatherData ->
                when (weatherData.index) {
                    0 -> {
//                        println(Date(it.value.time!! * 1000))
                        dayOfWeek1.text = setDayName(weatherData.value.time!!)
                        temperatureDay1.text = getString(R.string.celsius_symbol, weatherData.value.temperatureHigh.toString())
//                            weatherData.value.temperatureHigh.toString() + " \u2103" // TODO check when condition for Fahrenheit is applied
                        setImage(conditionsDay1, weatherData.value.icon!!)
                    }
                    1 -> {
//                        println(Date(it.value.time!! * 1000))
                        dayOfWeek2.text = setDayName(weatherData.value.time!!)
                        temperatureDay2.text = getString(R.string.celsius_symbol, weatherData.value.temperatureHigh.toString())
//                            weatherData.value.temperatureHigh.toString() + " \u2103"
                        setImage(conditionsDay2, weatherData.value.icon!!)
                    }
                    2 -> {
//                        println(Date(it.value.time!! * 1000))
                        dayOfWeek3.text = setDayName(weatherData.value.time!!)
                        temperatureDay3.text = getString(R.string.celsius_symbol, weatherData.value.temperatureHigh.toString())
//                            weatherData.value.temperatureHigh.toString() + getString(R.string.celsius_symbol)
                        setImage(conditionsDay3, weatherData.value.icon!!)
                    }
                }
            }
        })

        viewModel.elevationData.observe(viewLifecycleOwner, {
            drawGraph(it as MutableList<Int>?, view.elevation_graph)
        })



        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDayName(time: Long): String {
        return LocalDate
            .ofEpochDay(time / 86400)
            .dayOfWeek
            .name
            .lowercase()
            .replaceFirstChar { firstChar -> firstChar.uppercase() }
    }

    private fun setImage(conditionsImage: ImageView?, icon: String) {

        if (icon.contains("rain")) {
            conditionsImage?.setImageResource(R.drawable.rainy)
        }
        if (icon == "clear-day") {
            conditionsImage?.setImageResource(R.drawable.sunny)
        }
        if (icon.contains("snow")) {
            conditionsImage?.setImageResource(R.drawable.snowy)
        }
        if (icon.contains("cloud")) {
            conditionsImage?.setImageResource(R.drawable.cloudy)
        }
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