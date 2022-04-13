package com.example.hikingapp.ui.profile.saved

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
import com.example.hikingapp.RouteActivity
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.users.PhotoItem
import com.example.hikingapp.ui.adapters.*
import com.example.hikingapp.ui.route.cultureInfo.SightDetailsActivity
import com.example.hikingapp.ui.route.photos.PhotoActivity
import com.example.hikingapp.viewModels.ProfileViewModel
import com.example.hikingapp.viewModels.UserViewModel
import com.google.firebase.auth.FirebaseUser
import java.util.stream.Collectors

class SavedFragment : Fragment(), OnItemClickedListener, OnItemLongClickedListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var routes: MutableList<Route>
    private lateinit var sights: MutableList<Sight>
    private lateinit var photos: MutableList<PhotoItem>


    private lateinit var routesLayoutManager: LinearLayoutManager
    private lateinit var sightsLayoutManager: LinearLayoutManager
    private lateinit var photosLayoutManager: LinearLayoutManager



    private lateinit var routesRecyclerView: RecyclerView
    private lateinit var sightsRecyclerView: RecyclerView
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var routesAdapter: RoutesProfileAdapter
    private lateinit var sightsAdapter: SightsProfileAdapter
    private lateinit var photosAdapter: PhotosProfileAdapter


    private var allRouteItemsSelected = false
    private var allSightItemsSelected = false
    private var allPhotosItemsSelected = false

    private var user: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        val selectAllRoutesTextView = view.findViewById(R.id.select_all_route_items) as TextView
        val selectAllSightsTextView = view.findViewById(R.id.select_all_sight_items) as TextView
        val selectAllPhotosTextView = view.findViewById(R.id.select_all_photo_items) as TextView



        userViewModel.user.observe(viewLifecycleOwner, {
            user = it
        })


        profileViewModel.isRoutesLongClickPressed.observe(viewLifecycleOwner, {

            if (it == true) {
                selectAllRoutesTextView.visibility = View.VISIBLE
                selectAllRoutesTextView.text = getString(R.string.select_all)
            } else {
                selectAllRoutesTextView.visibility = View.GONE
            }
        })

        profileViewModel.isSightsLongClickPressed.observe(viewLifecycleOwner, {

            if (it == true) {
                selectAllSightsTextView.visibility = View.VISIBLE
                if (profileViewModel.savedRoutes.value?.size == profileViewModel.selectedRouteItems.value?.size) {
                    selectAllSightsTextView.text = getString(R.string.un_select_all)
                } else {
                    selectAllSightsTextView.text = getString(R.string.select_all)
                }

            } else {
                selectAllSightsTextView.visibility = View.GONE
            }
        })

        profileViewModel.isPhotosLongClickPressed.observe(viewLifecycleOwner, {
            if (it == true) {
                selectAllPhotosTextView.visibility = View.VISIBLE
                selectAllPhotosTextView.text = getString(R.string.select_all)
            } else {
                selectAllPhotosTextView.visibility = View.GONE
            }
        })

        profileViewModel.selectedRouteItems.observe(viewLifecycleOwner, {
            allRouteItemsSelected =
                profileViewModel.selectedRouteItems.value?.size == profileViewModel.savedRoutes.value?.size
            if (allRouteItemsSelected) {
                selectAllRoutesTextView.text = getString(R.string.un_select_all)
            }
        })

        profileViewModel.selectedSightItems.observe(viewLifecycleOwner, {
            allSightItemsSelected =
                profileViewModel.selectedSightItems.value?.size == profileViewModel.savedSights.value?.size
            if (allSightItemsSelected) {
                selectAllSightsTextView.text = getString(R.string.un_select_all)
            }
        })

        profileViewModel.selectedPhotoItems.observe(viewLifecycleOwner, {
            allPhotosItemsSelected =
                profileViewModel.selectedPhotoItems.value?.size == profileViewModel.savedPhotos.value?.size
            if (allPhotosItemsSelected) {
                selectAllPhotosTextView.text = getString(R.string.un_select_all)
            }
        })


        selectAllRoutesTextView.setOnClickListener {
            if (!allRouteItemsSelected) {
                routesAdapter.selectAllItems()
                selectAllRoutesTextView.text = getString(R.string.un_select_all)
                allRouteItemsSelected = true
            } else {
                routesAdapter.clearAllItems()
                selectAllRoutesTextView.text = getString(R.string.select_all)
                selectAllRoutesTextView.visibility = View.GONE
                allRouteItemsSelected = false
            }

        }

        selectAllSightsTextView.setOnClickListener {
            if (!allSightItemsSelected) {
                sightsAdapter.selectAllItems()
                selectAllSightsTextView.text = getString(R.string.un_select_all)
                allSightItemsSelected = true
            } else {
                sightsAdapter.clearAllItems()
                selectAllSightsTextView.text = getString(R.string.select_all)
                selectAllSightsTextView.visibility = View.GONE
                allSightItemsSelected = false
            }

        }


        selectAllPhotosTextView.setOnClickListener {
            if (!allPhotosItemsSelected) {
                photosAdapter.selectAllItems()
                selectAllPhotosTextView.text = getString(R.string.un_select_all)
                allPhotosItemsSelected = true
            } else {
                photosAdapter.clearAllItems()
                selectAllPhotosTextView.text = getString(R.string.select_all)
                selectAllPhotosTextView.visibility = View.GONE
                allPhotosItemsSelected = false
            }

        }

        initializeSavedRoutes(view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initializeSavedSights(view)
        }

        initializeSavedPhotos(view)

        return view
    }


    override fun onResume() {
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeSavedSights(view: View) {
        sightsRecyclerView = view.findViewById(R.id.favoritesSightsRView)
        sightsLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        sightsRecyclerView.layoutManager = sightsLayoutManager

        // TODO initialize sights list with sights

        profileViewModel.savedSights.observe(viewLifecycleOwner, { sightsList ->

            sights = sightsList
            profileViewModel.user.value?.profileInfo?.savedSights =
                sights.stream().map { it.sightId }.collect(Collectors.toList())
            sightsAdapter = SightsProfileAdapter(context, sights, this, this)
            sightsRecyclerView.adapter = sightsAdapter
            profileViewModel.savedSightsEnabled.observe(viewLifecycleOwner, {
                sightsAdapter.setEnabled(it)
            })
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeSavedRoutes(view: View) {
        routesRecyclerView = view.findViewById(R.id.favoritesRoutesRView)
        routesLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        routesRecyclerView.layoutManager = routesLayoutManager

        profileViewModel.savedRoutes.observe(viewLifecycleOwner, {
            routes = it as MutableList<Route>

            profileViewModel.user.value?.profileInfo?.savedRoutes =
                routes.stream().map { it.routeId }.collect(Collectors.toList())
            routesAdapter = RoutesProfileAdapter(context, routes, this, this)
            routesRecyclerView.adapter = routesAdapter

            profileViewModel.savedRoutesEnabled.observe(viewLifecycleOwner, {
                routesAdapter.setEnabled(it)
            })
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeSavedPhotos(view: View) {
        photosRecyclerView = view.findViewById(R.id.favoritesPhotosView)
        photosLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        photosRecyclerView.layoutManager = photosLayoutManager

        profileViewModel.savedPhotos.observe(viewLifecycleOwner, {
            photos = it as MutableList<PhotoItem>

            /*profileViewModel.user.value?.profileInfo?.savedRoutes =
                routes.stream().map { it.routeId }.collect(Collectors.toList())*/

            photosAdapter = PhotosProfileAdapter(context, photos, this, this)
            photosRecyclerView.adapter = photosAdapter

            profileViewModel.savedPhotosEnabled.observe(viewLifecycleOwner, {
                photosAdapter.setEnabled(it)
            })
        })
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        if (bundle.containsKey("class")) {
            when (bundle.get("class")) {
                Route::class.java.simpleName -> {
                    val intent = Intent(context, RouteActivity::class.java)
                    // TODO replace with Global Utils ID
                    routes[position].mainPhotoBitmap = null
                    intent.putExtra("route", routes[position])
                    intent.putExtra("action", "saved")
                    intent.putExtra("authInfo", user)
                    startActivity(intent)
                }
                Sight::class.java.simpleName -> {
                    val intent = Intent(context, SightDetailsActivity::class.java)
                    // TODO replace with Global Utils ID
                    intent.putExtra("sightInfo", sights[position])
                    intent.putExtra("authInfo", user)
                    intent.putExtra("action", "saved")
                    startActivity(intent)
                }
                PhotoItem::class.java.simpleName -> {
                    val intent = Intent(context, PhotoActivity::class.java)
                    // TODO replace with Global Utils ID
                    intent.putExtra("photo_item", photos[position]!!.imageName)
//                    intent.putExtra("itemId", "S${sightInfo.sightId}")
                    intent.putExtra("authInfo", user)
                    intent.putExtra("action", "saved")
                    startActivity(intent)
                }
            }
        }
    }

    override fun onItemLongClicked(position: Int, bundle: Bundle) {
        if (bundle.containsKey("routeLongClick")) {
            if (bundle.getBoolean("routeLongClick")) {
                profileViewModel.isRoutesLongClickPressed.postValue(true)
            } else {
                profileViewModel.isRoutesLongClickPressed.postValue(false)
            }
        }

        if (bundle.containsKey("sightLongClick")) {

            if (bundle.getBoolean("sightLongClick")) {
                profileViewModel.isSightsLongClickPressed.postValue(true)
            } else {
                profileViewModel.isSightsLongClickPressed.postValue(false)
            }
        }

        if (bundle.containsKey("photoLongClick")) {

            if (bundle.getBoolean("photoLongClick")) {
                profileViewModel.isPhotosLongClickPressed.postValue(true)
            } else {
                profileViewModel.isPhotosLongClickPressed.postValue(false)
            }
        }

        if (bundle.containsKey("sightSelectedItems")) {
            val selectedItemsWrapper =
                bundle.getSerializable("sightSelectedItems") as SelectedItemsWrapper
            profileViewModel.selectedSightItems.postValue(selectedItemsWrapper.selectedItems)
        }

        if (bundle.containsKey("routeSelectedItems")) {
            val selectedItemsWrapper =
                bundle.getSerializable("routeSelectedItems") as SelectedItemsWrapper
            profileViewModel.selectedRouteItems.postValue(selectedItemsWrapper.selectedItems)
        }

        if (bundle.containsKey("photoSelectedItems")) {
            val selectedItemsWrapper =
                bundle.getSerializable("photoSelectedItems") as SelectedItemsWrapper
            profileViewModel.selectedPhotoItems.postValue(selectedItemsWrapper.selectedItems)
        }
    }

}