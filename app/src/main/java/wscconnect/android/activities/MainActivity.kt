package wscconnect.android.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.text.method.LinkMovementMethod
import android.util.Log

import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.google.gson.JsonObject
import wscconnect.android.R
import wscconnect.android.Utils
import wscconnect.android.fragments.AppsFragment
import wscconnect.android.fragments.MyAppsFragment
import wscconnect.android.listeners.OnBackPressedListener
import java.util.*

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https:></https:>//opensource.org/licenses/LGPL-3.0>
 */
class MainActivity : AppCompatActivity() {
    var toolbar: Toolbar? = null
    private var currentFragment: Fragment? = null
    private var navigation: BottomNavigationView? = null
    private var appsFragmentTag: String? = null
    private var myAppsFragmentTag: String? = null
    private var fManager: FragmentManager? = null
    private var notificationAppID: String? = null
    private var onBackPressedListener: OnBackPressedListener? = null
    private lateinit var queue: RequestQueue
    private val TAG = "Volley"
    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_apps -> {
                    if (currentFragment !is AppsFragment) {
                        currentFragment = changeFragment(0)
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_my_apps -> {
                    if (currentFragment !is MyAppsFragment) {
                        currentFragment = changeFragment(1)
                    }
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onBackPressed() {
        if (onBackPressedListener != null) {
            if (!onBackPressedListener!!.onBackPressed()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    fun updateAppsFragment() {
        val fragment = fManager!!.findFragmentByTag(appsFragmentTag) as AppsFragment?
        fragment?.updateAdapter()
    }

    fun updateMyAppsFragment() {
        val fragment = fManager!!.findFragmentByTag(myAppsFragmentTag) as MyAppsFragment?
        fragment?.updateData()
    }

    fun updateAllFragments() {
        updateAppsFragment()
        updateMyAppsFragment()
    }

    fun setNotificationAppID(notificationAppID: String?) {
        this.notificationAppID = notificationAppID
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onStop() {
        super.onStop()
        IS_VISIBLE = false
    }

    override fun onStart() {
        super.onStart()
        IS_VISIBLE = true
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun setOnBackPressedListener(callback: OnBackPressedListener?) {
        onBackPressedListener = callback
    }

    private fun changeFragment(position: Int): Fragment? {
        var newFragment: Fragment? = null
        when (position) {
            0 -> {
                newFragment = fManager!!.findFragmentByTag(appsFragmentTag)
                if (newFragment != null) {
                    if (!newFragment.isStateSaved) {
                        fManager!!.beginTransaction().show(newFragment).commit()
                    }
                } else {
                    newFragment = AppsFragment()
                    fManager!!.beginTransaction()
                        .add(R.id.activity_main_content, newFragment, appsFragmentTag).commit()
                }
                hideFragments(myAppsFragmentTag!!)
                Objects.requireNonNull(supportActionBar)!!.setTitle(R.string.app_name)
            }
            1 -> {
                newFragment = fManager!!.findFragmentByTag(myAppsFragmentTag)
                if (newFragment != null) {
                    if (!newFragment.isStateSaved) {
                        fManager!!.beginTransaction().show(newFragment).commitNow()
                    }
                } else {
                    newFragment = MyAppsFragment()
                    fManager!!.beginTransaction()
                        .add(R.id.activity_main_content, newFragment, myAppsFragmentTag).commitNow()
                }
                hideFragments(appsFragmentTag!!)
                Objects.requireNonNull(supportActionBar)!!.setTitle(R.string.title_my_apps)
                supportActionBar?.setSubtitle(null)
            }
        }
        return newFragment
    }

    private fun hideFragments(vararg tags: String) {
        for (tag in tags) {
            val f = fManager!!.findFragmentByTag(tag)
            if (f != null) {
                fManager!!.beginTransaction().hide(f).commit()
            }
        }
    }

    public override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        notificationAppID = newIntent.getStringExtra(EXTRA_NOTIFICATION)
        if (notificationAppID != null) {
            navigation!!.selectedItemId = R.id.navigation_my_apps
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
        queue = Volley.newRequestQueue(this)
        toolbar = findViewById(R.id.activity_main_toolbar)
        setSupportActionBar(toolbar)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        fManager = supportFragmentManager
        appsFragmentTag = AppsFragment::class.java.simpleName
        myAppsFragmentTag = MyAppsFragment::class.java.simpleName
        navigation = findViewById(R.id.activity_main_navigation)
        navigation?.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        notificationAppID = intent.getStringExtra(EXTRA_NOTIFICATION)
        showPrivacyDialog()
        if (notificationAppID != null) {
            navigation?.selectedItemId = R.id.navigation_my_apps
        } else {
            if (Utils.getAllAccessTokens(this).isNotEmpty()) {
                navigation?.selectedItemId = R.id.navigation_my_apps
            } else {
                navigation?.selectedItemId = R.id.navigation_apps
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({ checkForUpdates(this) }, 3000)
    }

    private fun showPrivacyDialog() {
        val prefs = getSharedPreferences(Utils.SHARED_PREF_KEY, MODE_PRIVATE)
        // not logged in anywhere
        if (Utils.getAllAccessTokens(this).isEmpty() && !prefs.getBoolean(
                "privacyDialogShown",
                false
            )
        ) {
            prefs.edit().putBoolean("privacyDialogShown", true).apply()
            return
        }
        if (!prefs.getBoolean("privacyDialogShown", false)) {
            // log out of all apps
            Utils.removeAllAccessTokens(this)

            // don't show this dialog again
            prefs.edit().putBoolean("privacyDialogShown", true).apply()
            val builder = AlertDialog.Builder(
                this,
                android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen
            )
            builder.setTitle(R.string.dialog_privacy_title)
            builder.setPositiveButton(R.string.dialog_privacy_accept, null)
            builder.setCancelable(false)
            val dialogView = layoutInflater.inflate(R.layout.dialog_privacy, null)
            builder.setView(dialogView)
            val dialog = builder.show()
            (Objects.requireNonNull<Any?>(dialog.findViewById(R.id.privacy_part_1)) as TextView).movementMethod =
                LinkMovementMethod.getInstance()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.textColor))
        }
    }

    fun setActiveMenuItem(id: Int) {
        navigation!!.selectedItemId = id
    }

    override fun onDestroy() {
        queue.cancelAll(TAG)
        super.onDestroy()
    }

    /**
     * Check for update on github
     */
    @Suppress("NAME_SHADOWING")
    private fun checkForUpdates(context: Context) {
        val url = getString(R.string.github_update_check_url)
        val request = StringRequest(Request.Method.GET, url, { reply ->
            val latestVersion = Gson().fromJson(reply, JsonObject::class.java).get("tag_name").asString
            val current = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            if (latestVersion != current) {
                // We have an update available, tell our user about it
                Snackbar.make(findViewById(R.id.activity_main_content), getString(R.string.app_name) + " " + latestVersion + " " + getString(R.string.update_available), 10000)
                    .setAction(R.string.show) {
                        val url = getString(R.string.url_app_home_page)
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        // Not sure that does anything
                        i.putExtra("SOURCE", "SELF")
                        startActivity(i)
                    }.show()
            }
        }, { error ->
            Log.w(TAG, "Update check failed", error)
        })

        request.tag = TAG
        queue.add(request)
    }

    companion object {
        const val EXTRA_NOTIFICATION = "extraNotification"
        const val EXTRA_OPTION_TYPE = "extraOptionType"
        var IS_VISIBLE = true
    }
}