package com.crushtech.mycollegecgpa

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.crushtech.mycollegecgpa.utils.NetworkUtils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
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

        handleNetworkChanges()
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

    fun handleNetworkChanges() {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this, Observer { isConnected ->
            if (!isConnected) {
                getSnackBar(
                    getString(R.string.no_connection),
                    Color.RED
                ).show()

            } else {
                getSnackBar(
                    getString(R.string.network_available),
                    Color.parseColor("#00C853")
                ).show()
            }
        })
    }

    private fun getSnackBar(message: String, backgroundColor: Int): Snackbar {
        val snackbar = Snackbar.make(parent_layout, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(backgroundColor)
        val view = snackbar.view
        val snackBarText = view.findViewById<TextView>(R.id.snackbar_text)
        val typeface = ResourcesCompat.getFont(this, R.font.averia_libre_bold)
        snackBarText.apply {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTypeface(typeface)
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.snackbar_textsize)
            )
        }

        return snackbar
    }
}