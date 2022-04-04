package com.example.hikingapp.ui.route.ratings

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.users.User
import com.example.hikingapp.domain.users.reviews.Review
import com.example.hikingapp.persistence.local.LocalDatabase
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.ReviewsAdapter
import com.example.hikingapp.utils.GlobalUtils
import com.example.hikingapp.viewModels.RouteViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.android.synthetic.main.fragment_reviews_list.view.*

class ReviewsListFragment : Fragment(), OnItemClickedListener {

    private var progressBar: ProgressBar? = null
    private val viewModel: RouteViewModel by activityViewModels()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewsAdapter: ReviewsAdapter
    private lateinit var itemClickedListener: OnItemClickedListener
    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private var currentRoute: Route? = null
    private var reviews: MutableList<Review> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.itemClickedListener = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reviews_list, container, false)

        progressBar = view.findViewById(R.id.progress_bar) as ProgressBar
        progressBar!!.visibility = View.VISIBLE

        val noResultsText = view.findViewById<TextView>(R.id.no_results_text)

        linearLayoutManager = LinearLayoutManager(context)
        reviewsRecyclerView = view.findViewById(R.id.reviews_recycler_view)
        reviewsRecyclerView.layoutManager = linearLayoutManager

        viewModel.route.observe(viewLifecycleOwner, {

            currentRoute = it

            reviews = LocalDatabase.getReviewsForRoute(currentRoute!!.routeId)

            if (reviews.isEmpty()) {
                database.getReference("reviews").child(currentRoute!!.routeId.toString())
                    .addValueEventListener(object : ValueEventListener {

                        @RequiresApi(Build.VERSION_CODES.N)
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {

                                val reviewData =
                                    snapshot.value as HashMap<String, HashMap<String, *>>

                                reviewData.entries.forEach { reviewDataEntry ->

                                    val review = Review()
                                    review.userName = reviewDataEntry.key
                                    val user = User()
                                    reviewDataEntry.value.entries.forEach {
                                        when (it.key) {
                                            "review" -> review.review = it.value as String
                                            "rating" -> review.rating =
                                                (it.value as Number).toFloat()
                                        }
                                    }
                                    setUser(
                                        review.userName!!,
                                        user,
                                        review,
                                        reviewData.entries.size
                                    )
                                }
                            } else {
                                reviewsRecyclerView.visibility = View.GONE
                                noResultsText.visibility = View.VISIBLE
                                progressBar!!.visibility = View.GONE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
            } else {
                reviewsAdapter = ReviewsAdapter(requireContext(), reviews, this)
                reviewsRecyclerView.adapter = reviewsAdapter
                progressBar!!.visibility = View.GONE
            }


        })

        return view
    }

    private fun setUser(uid: String, user: User, review: Review, size: Int) {

        FirebaseDatabase.getInstance().getReference("users").child("user$uid")
            .addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userData = snapshot.value as HashMap<String, *>
                        user.uId = uid
                        user.userName = userData["userName"] as String
                        user.mail = userData["mail"] as String
                        user.password = userData["password"] as String
                        user.profileInfo = null

                        FirebaseStorage.getInstance().getReference("users").child(user.userName)
                            .child("${user.userName}_icon.png")
                            .getBytes(GlobalUtils.MEGABYTE * 5).addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                                review.userImage = bitmap
                                review.userName = user.userName
                                reviews.add(review)
                                if (reviews.size == size) {
                                    LocalDatabase.setReviewsForRoute(
                                        currentRoute!!.routeId,
                                        reviews
                                    )
                                    reviewsAdapter =
                                        ReviewsAdapter(context!!, reviews, itemClickedListener)
                                    reviewsRecyclerView.adapter = reviewsAdapter
                                    progressBar!!.visibility = View.GONE
                                }
                            }.addOnFailureListener {
                                if (it is StorageException) {
                                    when (it.httpResultCode) {
                                        404 -> Log.e(
                                            ReviewsListFragment::class.java.simpleName,
                                            "Image not found for user: ${user.userName}",
                                            it
                                        )
                                    }
                                } else {
                                    Log.e(
                                        ReviewsListFragment::class.java.simpleName,
                                        "Exception occured during loading of user Image for Review.",
                                        it
                                    )
                                }

                                review.userName = user.userName
                                reviews.add(review)
                                if (reviews.size == size) {
                                    LocalDatabase.setReviewsForRoute(
                                        currentRoute!!.routeId,
                                        reviews
                                    )
                                    reviewsAdapter =
                                        ReviewsAdapter(context!!, reviews, itemClickedListener)
                                    reviewsRecyclerView.adapter = reviewsAdapter
                                    progressBar!!.visibility = View.GONE
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(requireActivity(), ReviewItemActivity::class.java)
        intent.putExtra("review", reviews[position])
        startActivity(intent)
    }

}