package com.dzenis_ska.lostandfound.ui.fragments.camera

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.fragment.app.Fragment
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.FragmentCameraBinding
import com.dzenis_ska.lostandfound.ui.fragments.camera.InstanceStateCameraFragment.Companion.FAILURE
import com.dzenis_ska.lostandfound.ui.fragments.camera.InstanceStateCameraFragment.Companion.SUCCESS
import com.dzenis_ska.lostandfound.ui.utils.*
import com.dzenis_ska.lostandfound.ui.utils.permissionsUtils.REQUIRED_CAMERA_PERMISSIONS
import com.dzenis_ska.lostandfound.ui.utils.permissionsUtils.allCameraPermissionsGranted
import com.dzenis_ska.lostandfound.ui.utils.permissionsUtils.gotCamRecAudWESPermissions
import com.dzenis_ska.lostandfound.ui.utils.photoUtils.ImagePicker
import com.dzenis_ska.lostandfound.ui.utils.photoUtils.startCamera
import com.dzenis_ska.lostandfound.ui.utils.photoUtils.takePhoto
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//todo https://developer.android.com/codelabs/camerax-getting-started#1


class CameraFragment: Fragment(R.layout.fragment_camera) {

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var imagePicker: ImagePicker? = null

    private var binding: FragmentCameraBinding? = null
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            getPerm(it)
    }

    private fun getPerm(map: Map<String, Boolean>) {
        Log.d("!!!getPerm", "getPerm1")
        gotCamRecAudWESPermissions(map) {
            Log.d("!!!getPerm", "getPerm2")
            requestPermissions.launch(REQUIRED_CAMERA_PERMISSIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("!!!$TAG", "onCreate")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("!!!$TAG", "onCreateView $savedInstanceState")
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        initImagePicker()
        return binding!!.root
    }

    override fun onResume() {
        Log.d("!!!$TAG", "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.d("!!!$TAG", "onPause")
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initClick()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun initClick() = with(binding!!) {
        ibPhoto.setOnClickListener {
            takePhoto(imageCapture) {
                Log.d("!!!ibImage1", "${it}")
                publishResult(it)
            }
        }
        ibImage.setOnClickListener {
            imagePicker?.selectImage()
        }
    }

    private fun initImagePicker() {
        Log.d("!!!$TAG", "initImagePicker")
        imagePicker = ImagePicker(activity?.activityResultRegistry!!, viewLifecycleOwner) {
            publishResult(it)
        }
    }

    private fun publishResult(uri: Uri?){
        if (uri != null) navigator().publishResult(InstanceStateCameraFragment(uri, SUCCESS))
        else navigator().publishResult(InstanceStateCameraFragment(uri, FAILURE))
        navigator().popBackStack()
    }

    private fun initUI(){
//        if (!isPortrait())
            setTranslucentStatusAndNavigation(true)
        Log.d("!!!initUI", "${!allCameraPermissionsGranted()}")
        if (!allCameraPermissionsGranted())
            requestPermissions.launch(REQUIRED_CAMERA_PERMISSIONS)

        startCamera(binding!!.viewFinder.surfaceProvider) { imageCapture ->
            this.imageCapture = imageCapture
            this.imageCapture!!
        }
    }

    override fun onDestroyView() {
        cameraExecutor.shutdown()
        binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("!!!$TAG", "onDestroy")
    }

    companion object {
            val TAG = CameraFragment::class.java.name.substringAfterLast('.')
    }
}