package com.crushtech.mycollegecgpa

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
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
    private var successBarShown = false
    private var errorBarShown = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = Navigation.findNavController(this, R.id.gradesNavHostFragment)

        customBottomBar.setOnNavigationItemSelectedListener {
            if (it.itemId != customBottomBar.selectedItemId) {
                NavigationUI.onNavDestinationSelected(it, navController)
            }
            true
        }
        customBottomBar.setOnNavigationItemReselectedListener {}
        NavigationUI.setupWithNavController(customBottomBar, navController)

        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.othersFragment
            )
        )
        setupActionBarWithNavController(gradesNavHostFragment.findNavController(), appBarConfig)


        handleNetworkChanges(
            getString(R.string.no_connection),
            getString(R.string.network_available)
        )

    }

    fun hideAppBar() {
        supportActionBar?.hide()
    }

    fun showAppBar() {
        supportActionBar?.show()
    }

    fun hideMainActivityUI() {
        customBottomBar.visibility = View.GONE
        addSemester.visibility = View.GONE
    }

    fun showMainActivityUI() {
        customBottomBar.visibility = View.VISIBLE
        addSemester.visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = gradesNavHostFragment.findNavController()
        return navController.navigateUp()
    }

    private fun handleNetworkChanges(errorMessage: String, successMessage: String) {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this, Observer {
            it?.let { event ->
                val isConnected = event.peekContent()
                if (isConnected && successBarShown) {
                    showSnackbar(
                        successMessage, null,
                        R.drawable.ic_baseline_wifi_24, "",
                        ContextCompat.getColor(this, R.color.progress_color)
                    )
                    successBarShown = false
                    errorBarShown = true
                } else if (!isConnected) {
                    showSnackbar(
                        errorMessage, null,
                        R.drawable.ic_baseline_wifi_off_24, "",
                        ContextCompat.getColor(this, android.R.color.holo_red_dark)
                    )
                    successBarShown = true
                    errorBarShown = false
                }
            }

        })
    }

    fun showSnackbar(
        text: String, listener: View.OnClickListener?, iconId: Int,
        actionLabel: String, bgColor: Int
    ) {
        SimpleCustomSnackbar.make(
            parent_layout,
            text, LENGTH_LONG, listener,
            iconId, actionLabel,
            bgColor
        )?.show()
    }
}