package com.example.hikingapp.ui.profile.statistics

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
import com.example.hikingapp.LoginActivity
import com.example.hikingapp.R
import com.example.hikingapp.domain.enums.DistanceUnitType
import com.example.hikingapp.domain.navigation.UserNavigationData
import com.example.hikingapp.utils.GlobalUtils
import com.example.hikingapp.viewModels.UserViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StatisticsFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()
    private var userAuthInfo: FirebaseUser? = null
    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)


        userViewModel.user.observe(viewLifecycleOwner, {

            userAuthInfo = it
            if (userAuthInfo != null) {
                val distanceCovered = view.findViewById<TextView>(R.id.stats_distance_content)
                val totalTime = view.findViewById<TextView>(R.id.stats_time_content)
                val averageElevation =
                    view.findViewById<TextView>(R.id.stats_average_elevation_content)

                database.getReference("completedUserNavigationData").child(userAuthInfo!!.uid)
                    .addValueEventListener(object : ValueEventListener {
                        @RequiresApi(Build.VERSION_CODES.N)
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {

                                var totalDistance = 0.0
                                var totalTimeSpent = 0L
                                var elevationSum = 0L
                                var elevationCount = 0L

                                val userDataList = snapshot.value as List<HashMap<String, *>>

                                userDataList.forEach { navData ->
                                    val userData = UserNavigationData(
                                        navData["routeId"] as Long,
                                        navData["distanceCovered"] as Double,
                                        navData["timeSpent"] as Long,
                                        navData["currentElevation"] as MutableList<Long>
                                    )


                                    userData.currentElevation.apply {
                                        elevationSum+= this.stream().mapToLong { it }.sum()
                                        elevationCount+=this.size
                                    }

                                    totalDistance += userData.distanceCovered
                                    totalTimeSpent += userData.timeSpent
                                }

                                userViewModel.userSettings.observe(viewLifecycleOwner,{

                                    distanceCovered.text = GlobalUtils.getMetric(totalDistance, it.distanceUnit)
                                    totalTime.text = GlobalUtils.getTime(totalTimeSpent.toDouble(), it.timeUnit)
                                    averageElevation.text = GlobalUtils.getMetric((elevationSum / elevationCount).toDouble(),it.heightUnit)
                                })

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })

            } else {
                val redirectIntent = Intent(context,LoginActivity::class.java)
                redirectIntent.putExtra(GlobalUtils.LAST_PAGE, StatisticsFragment::class.java.simpleName)
                startActivity(redirectIntent)
            }
        })


        return view
    }
}