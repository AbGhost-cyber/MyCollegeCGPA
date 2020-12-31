package com.crushtech.mycollegecgpa

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.crushtech.mycollegecgpa.utils.NetworkUtils
import com.crushtech.mycollegecgpa.utils.SimpleCustomSnackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private var successBarShown = false
    private var errorBarShown = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainLayoutToolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }


        navController = Navigation.findNavController(this, R.id.gradesNavHostFragment)

        bottomNavigationView.setOnNavigationItemReselectedListener {
        }
        bottomNavigationView.setOnNavigationItemSelectedListener {
            if (it.itemId != bottomNavigationView.selectedItemId) {
                NavigationUI.onNavDestinationSelected(it, navController)
            }
            true
        }
        NavigationUI.setupWithNavController(bottomNavigationView, navController)


        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.extrasFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfig)



        handleNetworkChanges(
            getString(R.string.no_connection),
            getString(R.string.network_available)
        )

    }


    fun hideAppBar() {
        supportActionBar?.hide()
        appBarLayout.visibility = View.GONE
    }

    fun showAppBar() {
        supportActionBar?.show()
        appBarLayout.visibility = View.VISIBLE
    }

    fun hideMainActivityUI() {
        bottomNavigationView.visibility = View.GONE
    }

    fun showMainActivityUI() {
        bottomNavigationView.visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    private fun handleNetworkChanges(errorMessage: String, successMessage: String) {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this, Observer {
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
            parent_layout,
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