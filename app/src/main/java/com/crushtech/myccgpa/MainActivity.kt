package com.crushtech.myccgpa

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.crushtech.myccgpa.databinding.ActivityMainBinding
import com.crushtech.myccgpa.notification.OneSignalHelper
import com.crushtech.myccgpa.utils.Constants
import com.crushtech.myccgpa.utils.Constants.ACTION_SHOW_SEM_REQ_FRAGMENT
import com.crushtech.myccgpa.utils.Constants.viewBinding
import com.crushtech.myccgpa.utils.NetworkUtils
import com.crushtech.myccgpa.utils.SimpleCustomSnackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private var successBarShown = false
    private var errorBarShown = false
    private var appBarConfig: AppBarConfiguration? = null

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    val activityMainBinding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.mainLayoutToolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }
        val currentEmail = sharedPrefs.getString(
            Constants.KEY_LOGGED_IN_EMAIL,
            Constants.NO_EMAIL
        ) ?: Constants.NO_EMAIL
        OneSignalHelper.setUserExternalId(currentEmail)

        navController = Navigation.findNavController(this, R.id.gradesNavHostFragment)

        activityMainBinding.bottomNavigationView.setOnNavigationItemReselectedListener {
        }
        activityMainBinding.bottomNavigationView.setOnNavigationItemSelectedListener {
            if (it.itemId != activityMainBinding.bottomNavigationView.selectedItemId) {
                NavigationUI.onNavDestinationSelected(it, navController)
            }
            true
        }
        NavigationUI.setupWithNavController(activityMainBinding.bottomNavigationView, navController)


        appBarConfig = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.extrasFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfig!!)



        handleNetworkChanges(
            getString(R.string.no_connection),
            getString(R.string.network_available)
        )

        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { _request ->
            if (_request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = _request.result
                val flow =
                    manager.launchReviewFlow((this), reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToSemeReqFragmentIfNeeded(intent)
    }

    private fun navigateToSemeReqFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_SEM_REQ_FRAGMENT) {
            navController.navigate(
                R.id.action_global_semesterRequestFragment
            )
        }
    }


    fun hideAppBar() {
        supportActionBar?.hide()
        activityMainBinding.appBarLayout.visibility = View.GONE
    }

    fun showAppBar() {
        supportActionBar?.show()
        activityMainBinding.appBarLayout.visibility = View.VISIBLE
    }

    fun hideMainActivityUI() {
        activityMainBinding.bottomNavigationView.visibility = View.GONE
    }

    fun showMainActivityUI() {
        activityMainBinding.bottomNavigationView.visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfig!!) || super.onSupportNavigateUp()
    }

    private fun handleNetworkChanges(errorMessage: String, successMessage: String) {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this, {
            it?.let { event ->
                val isConnected = event.peekContent()
                if (isConnected && successBarShown) {
                    "".showSnackbar(
                        successMessage, null,
                        R.drawable.ic_baseline_wifi_24,
                        ContextCompat.getColor(this, R.color.progress_color)
                    )
                    successBarShown = false
                    errorBarShown = true
                } else if (!isConnected) {
                    "".showSnackbar(
                        errorMessage, null,
                        R.drawable.ic_baseline_wifi_off_24,
                        ContextCompat.getColor(this, android.R.color.holo_red_dark)
                    )
                    successBarShown = true
                    errorBarShown = false
                }
            }

        })
    }

    private fun String.showSnackbar(
        text: String, listener: View.OnClickListener?, iconId: Int,
        bgColor: Int
    ) {
        SimpleCustomSnackbar.make(
            activityMainBinding.root,
            text, LENGTH_LONG, listener,
            iconId, this,
            bgColor
        )?.show()
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        //listen to callbacks for fragments
//        for (fragments in supportFragmentManager.fragments) {
//            fragments.onActivityResult(requestCode, resultCode, data)
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }

}