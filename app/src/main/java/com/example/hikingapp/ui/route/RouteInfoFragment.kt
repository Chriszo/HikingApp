package com.example.hikingapp.ui.route

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hikingapp.R
import com.example.hikingapp.domain.users.settings.UserSettings
import com.example.hikingapp.utils.GlobalUtils
import com.example.hikingapp.viewModels.RouteViewModel
import com.example.hikingapp.viewModels.UserViewModel
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_route_info.view.*
import java.time.LocalDate

class RouteInfoFragment : Fragment() {

    private val viewModel: RouteViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private var userSettings: UserSettings? = null

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
        savedInstanceState?.get("RouteName")
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

        val elevationProgressBar = view.findViewById(R.id.elevation_progress_bar) as ProgressBar
        val weatherProgressBar = view.findViewById(R.id.weather_progress_bar) as ProgressBar

        viewModel.route.observe(viewLifecycleOwner, { it ->

            println("ViewModel observes...")

            routeType.text = it.routeInfo?.routeType!!.type
            difficultyLevel.text = it.routeInfo?.difficultyLevel!!.difficultyLevel
            rating.text = it.routeInfo?.rating.toString()


            weatherProgressBar.visibility = View.VISIBLE

            userViewModel.userSettings.observe(viewLifecycleOwner, { us ->
                userSettings = us

                distance.text = GlobalUtils.getMetric(it.routeInfo?.distance?:0.0, us?.distanceUnit)
                elevation.text = GlobalUtils.getMetric(it.routeInfo?.distance?:0.0, us?.heightUnit)
                estimatedTime.text = GlobalUtils.getTime(it.routeInfo?.timeEstimation, us?.timeUnit)

                it.weatherForecast?.weatherForecast?.withIndex()?.forEach { weatherData ->
                    when (weatherData.index) {
                        0 -> {
//                        println(Date(it.value.time!! * 1000))
                            dayOfWeek1.text = setDayName(weatherData.value.time!!)

                            temperatureDay1.text = getString(
                                getTemperatureUnit(),
                                "%.2f".format(getTemperatureValue(weatherData.value.temperatureHigh))
                            )
//                            weatherData.value.temperatureHigh.toString() + " \u2103" // TODO check when condition for Fahrenheit is applied
                            setImage(conditionsDay1, weatherData.value.icon!!)
                        }
                        1 -> {
//                        println(Date(it.value.time!! * 1000))
                            dayOfWeek2.text = setDayName(weatherData.value.time!!)
                            temperatureDay2.text = getString(
                                getTemperatureUnit(),
                                "%.2f".format(getTemperatureValue(weatherData.value.temperatureHigh))
                            )
//                            weatherData.value.temperatureHigh.toString() + " \u2103"
                            setImage(conditionsDay2, weatherData.value.icon!!)
                        }
                        2 -> {
//                        println(Date(it.value.time!! * 1000))
                            dayOfWeek3.text = setDayName(weatherData.value.time!!)
                            temperatureDay3.text = getString(
                                getTemperatureUnit(),
                                "%.2f".format(getTemperatureValue(weatherData.value.temperatureHigh))
                            )
//                            weatherData.value.temperatureHigh.toString() + getString(R.string.celsius_symbol)
                            setImage(conditionsDay3, weatherData.value.icon!!)
                        }
                    }
                }
                weatherProgressBar.visibility = View.GONE
                view.weather_info.visibility = View.VISIBLE
            })
        })

        elevationProgressBar.visibility = View.VISIBLE
        viewModel.elevationData.observe(viewLifecycleOwner, {
            drawGraph(it as MutableList<Long>?, view.elevation_graph)
            elevationProgressBar.visibility = View.GONE
            view.elevation_graph.visibility = View.VISIBLE
        })



        return view
    }

    private fun getTemperatureUnit(): Int {
        userSettings?.let {
            return if (it.temperatureUnit.trim() == "°F") R.string.fahrenheit_symbol else R.string.celsius_symbol
        }

        return R.string.celsius_symbol
    }

    private fun getTemperatureValue(temperatureHigh: Double?): Double {
        userSettings?.let {
            return if (it.temperatureUnit.trim() == "°F") GlobalUtils.convertToFahrenheit(temperatureHigh!!) else temperatureHigh!!
        }
        return temperatureHigh!!
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

}