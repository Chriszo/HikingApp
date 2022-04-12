package com.example.hikingapp.ui.profile

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.LoginActivity
import com.example.hikingapp.R
import com.example.hikingapp.viewModels.AppViewModel
import com.example.hikingapp.databinding.FragmentProfileBinding
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.users.PhotoItem
import com.example.hikingapp.persistence.local.LocalDatabase
import com.example.hikingapp.utils.GlobalUtils
import com.example.hikingapp.viewModels.ProfileViewModel
import com.example.hikingapp.viewModels.RouteViewModel
import com.example.hikingapp.viewModels.UserViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import io.ktor.http.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import java.util.stream.Collectors

class ProfileFragment : Fragment() {


    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val routeViewModel: RouteViewModel by activityViewModels()
    private val applicationViewModel: AppViewModel by activityViewModels()

    private var _binding: FragmentProfileBinding? = null

    private lateinit var savedRoutes: MutableList<Route>
    private lateinit var completedRoutes: MutableList<Route>
    private lateinit var savedSights: MutableList<Sight>
    private lateinit var completedSights: MutableList<Sight>

    private var userAuthInfo: FirebaseUser? = null
    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private lateinit var userSavedRouteIds: MutableList<Long>
    private lateinit var userCompletedRouteIds: MutableList<Long>
    private lateinit var userSavedSightIds: MutableList<Long>
    private lateinit var userCompletedSightIds: MutableList<Long>

    private lateinit var progressDialog: ProgressDialog


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val inProdMode = getString(R.string.prodMode).toBooleanStrict()

        if (userViewModel.user.value == null && inProdMode) {
            val redirectIntent = Intent(context,LoginActivity::class.java)
            redirectIntent.putExtra(GlobalUtils.LAST_PAGE, ProfileFragment::class.java.simpleName)
            startActivity(redirectIntent)
        } else {

            userViewModel.user.observe(viewLifecycleOwner, {
                userAuthInfo = it
            })

            progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait...")
            progressDialog.setMessage("Loading Routes...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            _binding = FragmentProfileBinding.inflate(inflater, container, false)
            val root: View = binding.root

            /*if (inProdMode) {
                userAuthInfo = userViewModel.user.value!!
            }*/

            val navHost =
                childFragmentManager.findFragmentById(R.id.profileFragmentContainer) as NavHostFragment
            val navController = navHost.navController

            val navView = root.info_nav_view
            navView.setupWithNavController(navController)

            val deleteFrame = root.findViewById(R.id.delete_routes_layout) as LinearLayout

            profileViewModel.isRoutesLongClickPressed.observe(viewLifecycleOwner, {

                val deleteText = root.findViewById(R.id.removeText) as TextView
                val sightsView = root.findViewById(R.id.favoritesSightsRView) as RecyclerView
                if (it == true) {
                    deleteFrame.visibility = View.VISIBLE
                    sightsView.alpha = 0.5f
                    profileViewModel.savedSightsEnabled.postValue(false)
                    deleteText.text = getString(R.string.remove_routes)

                } else {

                    deleteFrame.visibility = View.GONE
                    sightsView.alpha = 1f
                    profileViewModel.savedSightsEnabled.postValue(true)
                }
            })

            profileViewModel.isSightsLongClickPressed.observe(viewLifecycleOwner, {

                val routesView = root.findViewById(R.id.favoritesRoutesRView) as RecyclerView
                val deleteText = root.findViewById(R.id.removeText) as TextView

                if (it == true) {

                    deleteFrame.visibility = View.VISIBLE
                    routesView.alpha = 0.5f
                    profileViewModel.savedRoutesEnabled.postValue(false)
                    deleteText.text = getString(R.string.remove_sights)

                } else {

                    deleteFrame.visibility = View.GONE
                    routesView.alpha = 1f
                    profileViewModel.savedRoutesEnabled.postValue(true)

                }
            })

            val removeImage = root.findViewById(R.id.removeImage) as ImageView

            removeImage.setOnClickListener {

                val selectedRouteItemsList = profileViewModel.selectedRouteItems.value
                val selectedSightItemsList = profileViewModel.selectedSightItems.value


                if (Objects.nonNull(userAuthInfo) && !selectedRouteItemsList.isNullOrEmpty()) {
                    handleRouteSelectedItems(selectedRouteItemsList)
                }

                if (Objects.nonNull(userAuthInfo) && !selectedSightItemsList.isNullOrEmpty()) {
                    handleSightSelectedItems(selectedSightItemsList)
                }
            }

            database.getReference("completedRouteAssociations")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        userSavedSightIds = (snapshot.value as HashMap<String, *>).entries
                            .stream()
                            .filter { it.key == userAuthInfo!!.uid }
                            .flatMap { (it.value as MutableList<Long>).stream() }
                            .collect(Collectors.toList())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            database.getReference("completedSightAssociations")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        userCompletedSightIds = (snapshot.value as HashMap<String, *>).entries
                            .stream()
                            .filter { it.key == userAuthInfo!!.uid }
                            .flatMap { (it.value as MutableList<Long>).stream() }
                            .collect(Collectors.toList())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            routeViewModel.currentRoutes.observe(viewLifecycleOwner, { currentRoutes ->

                database.getReference("savedRouteAssociations")
                    .addValueEventListener(object : ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {
                            userSavedRouteIds = (snapshot.value as HashMap<String, *>).entries
                                .stream()
                                .filter { it.key == userAuthInfo!!.uid }
                                .flatMap { (it.value as MutableList<Long>).stream() }
                                .collect(Collectors.toList())


                            savedRoutes =
                                currentRoutes.stream()
                                    .filter { userSavedRouteIds.contains(it.routeId) }
                                    .collect(Collectors.toList())
                            profileViewModel.savedRoutes.postValue(savedRoutes)

                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })

                database.getReference("completedRouteAssociations")
                    .addValueEventListener(object : ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {
                            userCompletedRouteIds = (snapshot.value as HashMap<String, *>).entries
                                .stream()
                                .filter { it.key == userAuthInfo!!.uid }
                                .flatMap { (it.value as MutableList<Long>).stream() }
                                .collect(Collectors.toList())

                            completedRoutes =
                                currentRoutes.stream()
                                    .filter { userCompletedRouteIds.contains(it.routeId) }
                                    .collect(Collectors.toList())
                            profileViewModel.completedRoutes.postValue(completedRoutes)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })



                database.getReference("savedSightAssociations")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            userSavedSightIds = (snapshot.value as HashMap<String, *>).entries
                                .stream()
                                .filter { it.key == userAuthInfo!!.uid }
                                .flatMap { (it.value as MutableList<Long>).stream() }
                                .collect(Collectors.toList())

                            savedSights =
                                userSavedSightIds.stream().map { LocalDatabase.getSight(it) }
                                    .filter { Objects.nonNull(it) }.collect(Collectors.toList())

                            if (!savedSights.isNullOrEmpty()) {
                                loadSightsMainPhotos(savedSights as MutableList<Sight>, "saved")
                            } else {
                                database.getReference("sights")
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {

                                            savedSights =
                                                (snapshot.value as HashMap<String, *>).entries
                                                    .stream()
                                                    .filter { sightEntry ->
                                                        sightEntry.key.split(
                                                            "_"
                                                        )[1].toLong() in userSavedSightIds
                                                    }
                                                    .map {
                                                        val sightInfo =
                                                            it.value as HashMap<String, *>
                                                        val sightId =
                                                            sightInfo["sightId"] as Long
                                                        Sight(
                                                            sightId,
                                                            null,
                                                            sightInfo["name"] as String,
                                                            sightInfo["description"] as String,
                                                            (sightInfo["rating"] as Double).toFloat(),
                                                            LocalDatabase.getMainImage(
                                                                sightId,
                                                                Sight::class.java.simpleName
                                                            ),
                                                            mutableListOf()
                                                        )
                                                    }
                                                    .collect(Collectors.toList())
                                            loadSightsMainPhotos(savedSights, "saved")
//                                            profileViewModel.savedSights.postValue(savedSights)
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                            }


                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })


                database.getReference("completedSightAssociations")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            userCompletedSightIds = (snapshot.value as HashMap<String, *>).entries
                                .stream()
                                .filter { it.key == userAuthInfo!!.uid }
                                .flatMap { (it.value as MutableList<Long>).stream() }
                                .collect(Collectors.toList())

                            completedSights =
                                userCompletedSightIds.stream().map { LocalDatabase.getSight(it) }
                                    .filter { Objects.nonNull(it) }.collect(Collectors.toList())

                            if (!completedSights.isNullOrEmpty()) {
                                loadSightsMainPhotos(completedSights, "completed")
                                progressDialog.dismiss()
                            } else {
                                database.getReference("sights")
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {

                                            completedSights =
                                                (snapshot.value as HashMap<String, *>).entries
                                                    .stream()
                                                    .filter { sightEntry -> sightEntry.key.split("_")[1].toLong() in userCompletedSightIds }
                                                    .map {
                                                        val sightInfo =
                                                            it.value as HashMap<String, *>
                                                        val sightId = sightInfo["sightId"] as Long
                                                        Sight(
                                                            sightId,
                                                            null,
                                                            sightInfo["name"] as String,
                                                            sightInfo["description"] as String,
                                                            (sightInfo["rating"] as Double).toFloat(),
                                                            LocalDatabase.getMainImage(
                                                                sightId,
                                                                Sight::class.java.simpleName
                                                            ),
                                                            mutableListOf()
                                                        )
                                                    }
                                                    .collect(Collectors.toList())
                                            loadSightsMainPhotos(completedSights, "completed")
                                            progressDialog.dismiss()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })

            })

            return root
        }
        return null
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadSightsMainPhotos(sights: MutableList<Sight>, profileItem: String) {

        val sightMainPhotos = mutableListOf<Bitmap?>()
        sights?.forEach { sight ->

            sight.mainPhoto =
                LocalDatabase.getMainImage(sight.sightId, Sight::class.java.simpleName)
            if (sight.mainPhoto == null) {

                storage.reference.child("sights/mainPhotos/sight_${sight.sightId}_main.jpg")
                    .getBytes(1024 * 1024 * 5).addOnSuccessListener {

                        val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                        sight.mainPhoto = bitmap
                        sightMainPhotos.add(bitmap)
                        LocalDatabase.saveImage(
                            sight.sightId,
                            Sight::class.java.simpleName,
                            "sight_${sight.sightId}_main.jpg",
                            PhotoItem("sight_${sight.sightId}_main.jpg", bitmap),
                            true
                        )
                        if (sightMainPhotos.size == sights?.size ?: mutableListOf<Sight>()) {
                            when (profileItem) {
                                "saved" -> profileViewModel.savedSights.postValue(sights)
                                "completed" -> profileViewModel.completedSights.postValue(sights)
                            }
                        }
                    }
                    .addOnFailureListener {
                        if (it is StorageException) {
                            if (it.httpResultCode == HttpStatusCode.NotFound.value) {
                                sightMainPhotos.add(null)
                            }
                        }
                        if (sightMainPhotos.size == sights?.size ?: mutableListOf<Sight>()) {
                            when (profileItem) {
                                "saved" -> profileViewModel.savedSights.postValue(sights)
                                "completed" -> profileViewModel.completedSights.postValue(sights)
                            }
                        }
                    }

            } else {
                when (profileItem) {
                    "saved" -> profileViewModel.savedSights.postValue(sights)
                    "completed" -> profileViewModel.completedSights.postValue(sights)
                }
            }
        }

    }


    private fun handleSightSelectedItems(selectedSightItemsList: MutableList<Int>) {

        val sightsCopy = mutableListOf<Sight>().apply { addAll(savedSights) }
        savedSights.clear()

        sightsCopy.withIndex().forEach {
            if (!selectedSightItemsList.contains(it.index)) {
                savedSights.add(it.value)
            }
        }

        selectedSightItemsList.clear()
        profileViewModel.selectedSightItems.postValue(selectedSightItemsList)
        profileViewModel.savedSights.postValue(savedSights)
        profileViewModel.isSightsLongClickPressed.postValue(false)
    }

    private fun handleRouteSelectedItems(selectedRouteItemsList: MutableList<Int>) {
        val routesCopy = mutableListOf<Route>().apply { addAll(savedRoutes) }
        savedRoutes.clear()

        routesCopy.withIndex().forEach {
            if (!selectedRouteItemsList.contains(it.index)) {
                savedRoutes.add(it.value)
            }
        }

        selectedRouteItemsList.clear()
        profileViewModel.selectedRouteItems.postValue(selectedRouteItemsList)
        profileViewModel.savedRoutes.postValue(savedRoutes)
        profileViewModel.isRoutesLongClickPressed.postValue(false)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onDestroyView() {
        super.onDestroyView()

        if (Objects.nonNull(userAuthInfo)) {

            persistSavedRoutes()
            persistCompletedRoutes()
            persistSavedSights()
            persistCompletedSights()

        }

        _binding = null
    }

    private fun persistCompletedSights() {
        database.getReference("completedSightAssociations").child(userAuthInfo!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onDataChange(snapshot: DataSnapshot) {
                    val completedSightIds =
                        completedSights.stream().map { it.sightId }.collect(Collectors.toList())
                    database.getReference("completedSightAssociations").child(userAuthInfo!!.uid)
                        .setValue(completedSightIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun persistSavedSights() {
        database.getReference("savedSightAssociations").child(userAuthInfo!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onDataChange(snapshot: DataSnapshot) {
                    val savedSightIds =
                        savedSights.stream().map { it.sightId }.collect(Collectors.toList())
                    database.getReference("savedSightAssociations").child(userAuthInfo!!.uid)
                        .setValue(savedSightIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun persistCompletedRoutes() {
        database.getReference("completedRouteAssociations").child(userAuthInfo!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onDataChange(snapshot: DataSnapshot) {
                    val completedRouteIds =
                        completedRoutes.stream().map { it.routeId }.collect(Collectors.toList())

                    database.getReference("completedRouteAssociations").child(userAuthInfo!!.uid)
                        .setValue(completedRouteIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun persistSavedRoutes() {
        database.getReference("savedRouteAssociations").child(userAuthInfo!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onDataChange(snapshot: DataSnapshot) {
                    val savedRouteIds =
                        savedRoutes.stream().map { it.routeId }.collect(Collectors.toList())
                    database.getReference("savedRouteAssociations").child(userAuthInfo!!.uid)
                        .setValue(savedRouteIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}