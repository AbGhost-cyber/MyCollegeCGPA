package com.crushtech.mycollegecgpa

import android.content.Intent
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
import com.crushtech.mycollegecgpa.databinding.ActivityMainBinding
import com.crushtech.mycollegecgpa.utils.Constants.viewBinding
import com.crushtech.mycollegecgpa.utils.NetworkUtils
import com.crushtech.mycollegecgpa.utils.SimpleCustomSnackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private var successBarShown = false
    private var errorBarShown = false
    private var appBarConfig: AppBarConfiguration? = null

    val activityMainBinding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.mainLayoutToolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //listen to callbacks for fragments
        for (fragments in supportFragmentManager.fragments) {
            fragments.onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}