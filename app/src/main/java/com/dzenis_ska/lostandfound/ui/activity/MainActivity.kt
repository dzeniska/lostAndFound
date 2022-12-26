package com.dzenis_ska.lostandfound.ui.activity

import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.ActivityMainBinding
import com.dzenis_ska.lostandfound.ui.fragments.add_application.AddApplicationFragmentArguments
import com.dzenis_ska.lostandfound.ui.fragments.add_application.AddApplicationFragmentDirections
import com.dzenis_ska.lostandfound.ui.fragments.changeLocation.ChangeLocationFragmentArguments
import com.dzenis_ska.lostandfound.ui.fragments.map.MapFragmentDirections
import com.dzenis_ska.lostandfound.ui.fragments.showApplication.ShowApplicationFragmentDirections
import com.dzenis_ska.lostandfound.ui.fragments.showLocation.ShowLocationFragmentArguments
import com.dzenis_ska.lostandfound.ui.utils.*
import com.dzenis_ska.lostandfound.ui.utils.fragmentManagerUtils.FragmentManagerUtils
import com.dzenis_ska.lostandfound.ui.utils.fragmentManagerUtils.FragmentManagerUtils.currentFragment
import com.dzenis_ska.lostandfound.ui.utils.fragmentManagerUtils.FragmentManagerUtils.fragmentListener

class MainActivity : AppCompatActivity(), Navigator
{

    private lateinit var binding: ActivityMainBinding
    private var navController: NavController? = null
    private val viewModel: MainActivityViewModel by viewModels { factory() }

//    private var currentFragment: Fragment? = null
//    private val fragmentListener = object : FragmentManager.FragmentLifecycleCallbacks() {
//        override fun onFragmentViewCreated(
//            fm: FragmentManager,
//            f: Fragment,
//            v: View,
//            savedInstanceState: Bundle?
//        ) {
//            super.onFragmentViewCreated(fm, f, v, savedInstanceState)
//            if (f is NavHostFragment) return
//            currentFragment = f
//        }
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.LostAndFound)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentListener, true)
        navController = getRootNavController()
        binding.apply {

        }


//        Handler(Looper.getMainLooper()).postDelayed({
//            try {
//                hideStatusBar()
//            } catch (e: IllegalStateException) {
//                e.message?.let { Log.e("PixFragment", it) }
//            }
//        }, 2000)
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            try {
//                showStatusBar()
//            } catch (e: IllegalStateException) {
//                e.message?.let { Log.e("PixFragment", it) }
//            }
//        }, 2000)

    }
    private fun getRootNavController(): NavController {
        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        return navHost.navController
    }

    override fun onResume() {
        super.onResume()
        Log.d("!!!onResume", "${Build.VERSION.SDK_INT}")
        binding.fragmentContainer.postDelayed({
//            hideStatusBar()
//            hideSystemUI()
        }, 500L)
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentListener)
    }

    override fun onBackPressed() {
        val fragment = currentFragment
        if (fragment is OnBaskPressed) {
            fragment.onBackPressedInFragment().run()
        }
        super.onBackPressed()
    }

    override fun goToCameraFragment() {
        navController?.navigate(R.id.cameraFragment)
    }

    override fun goToAddApplicationFragment(addApplicationFragmentArguments: AddApplicationFragmentArguments) {
        val direction = MapFragmentDirections.actionMapFragmentToAddApplicationFragment(addApplicationFragmentArguments)
        navController?.navigate(direction)
    }

    override fun goToAddApplicationFragmentFromShowApplicationFragment(
        toAddApplicationFragmentArguments: AddApplicationFragmentArguments
    ) {
        val direction = ShowApplicationFragmentDirections.actionShowApplicationFragmentToAddApplicationFragment(toAddApplicationFragmentArguments)
        navController?.navigate(direction)
    }

    override fun goToChangeLocationFragment(changeLocationFragmentArguments: ChangeLocationFragmentArguments) {
        val direction = AddApplicationFragmentDirections.actionAddApplicationFragmentToChangeLocationFragment(changeLocationFragmentArguments)
        navController?.navigate(direction)
    }

    override fun goToShowApplicationFragment(id: String) {
        Log.d("!!!goToShowApplicationFragment", "${navController}")

        val direction = MapFragmentDirections.actionMapFragmentToShowApplicationFragment(id)
        navController?.navigate(direction)
    }

    override fun goToShowLocationFragment(showLocationFragmentArguments: ShowLocationFragmentArguments) {
        Log.d("!!!goToShowLocationFragment", "${navController?.context}")
        val direction = ShowApplicationFragmentDirections.actionShowApplicationFragmentToShowLocationFragment(showLocationFragmentArguments)
        navController?.navigate(direction)
    }

    override fun goToShowFullPhotoFragment(it: String) {
        Log.d("!!!goToShowFullPhotoFragment", "${navController?.context}")
        val direction = ShowApplicationFragmentDirections.actionShowApplicationFragmentToShowFullPhotoFragment(it)
        navController?.navigate(direction)
    }

    override fun popBackStack() {
//        onBackPressed()
        navController?.popBackStack()
    }

    override fun popBackStackToMapFragment() {
        Log.d("!!!requestFromBureauEntityisNotBlank11", "${navController?.context}")
        navController?.popBackStack(R.id.mapFragment, false)
    }


    override fun <T : Parcelable> publishResult(result: T) {
        supportFragmentManager.setFragmentResult(result.javaClass.name, bundleOf(KEY_RESULT to result))
    }

    override fun <T : Parcelable> listenResult(
        clazz: Class<T>,
        owner: LifecycleOwner,
        listener: ResultListener<T>
    ) {
        supportFragmentManager.setFragmentResultListener(clazz.name, owner, FragmentResultListener { _, bundle ->
            listener.invoke(bundle.getParcelable(KEY_RESULT)!!)
        })
    }

    companion object {
        const val KEY_RESULT = "KEY_RESULT"
    }
}