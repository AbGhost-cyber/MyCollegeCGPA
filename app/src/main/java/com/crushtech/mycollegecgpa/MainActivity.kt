package com.crushtech.mycollegecgpa

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.crushtech.mycollegecgpa.R.font.averia_libre_bold
import com.crushtech.mycollegecgpa.utils.NetworkUtils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar.make
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
                    getSnackBar(
                        successMessage,
                        Color.parseColor("#00C853")
                    ).show()
                    successBarShown = false
                    errorBarShown = true
                } else if (!isConnected) {
                    getSnackBar(
                        errorMessage,
                        Color.RED
                    ).show()
                    successBarShown = true
                    errorBarShown = false
                }
            }

        })
    }

    private fun getSnackBar(message: String, backgroundColor: Int): Snackbar {
        val snackbar = make(parent_layout, message, LENGTH_LONG)
            .setBackgroundTint(backgroundColor)
        val view = snackbar.view
        val snackBarText = view.findViewById<TextView>(R.id.snackbar_text)
        val typeface = getFont(this, averia_libre_bold)
        snackBarText.apply {
            textAlignment = TEXT_ALIGNMENT_CENTER
            setTypeface(typeface)
            setTextSize(
                COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.snackbar_textsize)
            )
        }

        return snackbar
    }
}