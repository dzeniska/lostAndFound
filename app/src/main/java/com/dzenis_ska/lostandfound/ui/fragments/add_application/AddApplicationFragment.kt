package com.dzenis_ska.lostandfound.ui.fragments.add_application

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.DialogLoadApplicationBinding
import com.dzenis_ska.lostandfound.databinding.FragmentAddApplicationBinding
import com.dzenis_ska.lostandfound.ui.db.firebase.FBDatabase
import com.dzenis_ska.lostandfound.ui.fragments.add_application.DefaultTelNum.defaultTelNum
import com.dzenis_ska.lostandfound.ui.fragments.camera.InstanceStateCameraFragment
import com.dzenis_ska.lostandfound.ui.fragments.camera.InstanceStateCameraFragment.Companion.SUCCESS
import com.dzenis_ska.lostandfound.ui.fragments.changeLocation.ChangeLocationFragmentArguments
import com.dzenis_ska.lostandfound.ui.utils.*
import com.dzenis_ska.lostandfound.ui.utils.addApplication.createUploadDialog
import com.dzenis_ska.lostandfound.ui.utils.photoUtils.ImageManager
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*

class AddApplicationFragment : Fragment(R.layout.fragment_add_application) {

    private val args: AddApplicationFragmentArgs by navArgs()
    var choosePhotoAdapter: ChoosePhotoAdapter? = null
    var smileAdapter: ChooseSmileAdapter? = null

    private var binding: FragmentAddApplicationBinding? = null
    var dialogBinding: DialogLoadApplicationBinding? = null
    var dialog: AlertDialog? = null
    val viewModel: AddApplicationViewModel by viewModels { factory() }

    var state: InstanceStateAddApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("!!!onCreateAAF", "savedInstanceState = ${InstanceStateAddApplication()}")
        state = savedInstanceState?.getParcelable(KEY_STATE)
            ?: InstanceStateAddApplication.fromAddApplicationFragmentArgs(args.argsAAF)
                    ?: throw IllegalArgumentException("!!!There is not getting instance InstanceStateAddApplication")
        Log.d("!!!onCreateAAF", "savedInstanceState = ${savedInstanceState}")

        Log.d("!!!onCreateAAF", "savedInstanceState = ${state}")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("!!!onSaveInstanceState", "savedInstanceState = ${state}")
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_STATE, state)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddApplicationBinding.inflate(inflater, container, false)
        initNavigator()
        initClick()
//        initArgs()
        initUI()
        initViewModel()
        return binding!!.root
    }

    private fun initViewModel() {
        viewModel.viewModelStateAA.observe(viewLifecycleOwner) { stateVM ->
            Log.d(

                "!!!viewModelStateAAObserve",
                "${stateVM.isShowLoadApplicationDialog}_${stateVM.displayLoadApplicationDialog?.mess}_${stateVM.displayLoadApplicationDialog?.numPhoto}"
            )

            stateVM.isShowLoadApplicationDialog?.let {
                Log.d("!!!viewModelStateAA1", "$it")
                Log.d("!!!viewModelStateAA11", "$dialog")
                Log.d("!!!viewModelStateAA111", "$dialogBinding")
                if (it) {
                    if (dialog == null) createUploadDialog()
                } else {
                    dialog?.dismiss()
                    dialog = null
                }

            }
            stateVM.displayLoadApplicationDialog?.let {
                Log.d("!!!viewModelStateAA2", "${it.mess}_${it.numPhoto}")
//                if (dialog == null) createUploadDialog()
                if (it.numPhoto != AAViewModelState.DIALOG_DISMISSED) {
                    displayUploadDialog(it)
                } else {
                    stateCopy(imageIndex = -1)
                    if (it.mess != null) toastL(getString(it.mess!!))
                }
            }
        }

        viewModel.toastEvent.observe(viewLifecycleOwner) {
            it.getValue()?.let { mess ->
                Log.d("!!!toastEvent", "${mess}")
                if (mess == FBDatabase.ADD_TO_DB_FAILURE) {
                    Dialog.createDialog(
                        context = requireContext(),
                        cancelable = true,
                        header = getString(R.string.something_went_wrong),
                        mess = getString(R.string.maybe_application_do_not_upload),
                        btn1 = null,
                        btn2 = null,
                        {}, {}
                    ) {}
                    toastL(getString(R.string.something_went_wrong))
                    return@observe
                }
            }
        }

        viewModel.goToChangeLocationFragment.observe(viewLifecycleOwner) {
            it.getValue()?.let {
                Log.d("!!!OnMapReadyCallback2", "${getState()?.location}")
                navigator().goToChangeLocationFragment(ChangeLocationFragmentArguments(getState()?.location))
            }
        }
        viewModel.goToCameraFragment.observe(viewLifecycleOwner) {
            it.getValue()?.let {
                navigator().goToCameraFragment()
            }
        }
        viewModel.popBackStack.observe(viewLifecycleOwner) {
            it.getValue()?.let {
//                clearState()
                navigator().popBackStack()
            }
        }
        viewModel.popBackToMap.observe(viewLifecycleOwner) {
            it.getValue()?.let {
//                clearState()
                navigator().popBackStackToMapFragment()
            }
        }
        viewModel.getBiteArrayEvent.observe(viewLifecycleOwner) {
            it.getValue()?.let {
                Log.d("!!!listPhotoUriSize", "${getState()?.listPhotoUri?.size}")
                Log.d("!!!listPhotoUriSize", "${getState()?.imageIndex}")
                getByteArrayFromPhoto()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initClick() = with(binding!!) {
        btnLoc.setOnClickListener {
            setTranslucentStatusAndNavigation(true)
            viewModel.goToChangeLocationFragment()
        }

        btnSelectCat.setOnClickListener {
            setTranslucentStatusAndNavigation(true)
            when (getState()?.category) {
                null -> stateCopy(chooseCategory = Category.LOST.toString())
                Category.LOST.toString() -> stateCopy(chooseCategory = Category.FOUND.toString())
                Category.FOUND.toString() -> stateCopy(chooseCategory = Category.LOST.toString())
            }
            initButtonChooseCategory()
        }
        etShowTelNum.addTextChangedListener {
            if (it.toString().isNotEmpty())
                btnShowTelNum.setTint(R.color.white)
            else
                btnShowTelNum.setTint(R.color.white_38)
            stateCopy(enterTelNum = it.toString())
        }
        etShowTelNum.setOnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    setTranslucentStatusAndNavigation(true)
                    stateCopy(enterTelNum = v.text.toString())
                    hideSoftKeyboards()
//                    btnShowTelNum.setTint(R.color.white)
//                    initButtonShowTelNum()
                }
            }
            return@setOnEditorActionListener false
        }
        etAdditionalInfo.setOnTouchListener { v, event ->
            Log.d("!!!setOnTouchListener", " MotionEvent = ${event}")
            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d("!!!setOnTouchLis2tener", " MotionEvent = ${event}")
                setTranslucentStatusAndNavigation(false)
            }
            false
        }

        etAdditionalInfo.addTextChangedListener {
            Log.d("!!!addTextChangedListener", "Build.VERSION.SDK_INT = ${Build.VERSION.SDK_INT}")
            stateCopy(enterAdditionalInfo = it.toString())
        }

        btnInfo.setOnClickListener {
            setTranslucentStatusAndNavigation(true)
            Dialog.createInfoDialog(requireContext()) {
                stateCopy(isRulesRead = true)
            }
        }
        btnPublish.setOnClickListener {
            setTranslucentStatusAndNavigation(true)
            if (getState()?.isRulesRead == true || getState()?.isEditApplication == true) {
                prepareToPublishApplication()
            } else {
                Dialog.createInfoDialog(requireContext()) {
                    stateCopy(isRulesRead = true)
                    prepareToPublishApplication()
                }
            }
        }
        smile.setOnClickListener {
            rvSmile?.showAndHide()
        }
    }

    private fun initUI() {
        setTranslucentStatusAndNavigation(true)
        initSmilesAdapter()
        initViewPager()
        initBtnLocation()
        initButtonChooseCategory()
        initButtonShowTelNum()
        initAdditionalInformation()
    }

    private fun initBtnLocation() = with(binding!!) {
        val location = getState()?.location ?: args.argsAAF?.location
        if (getState()?.location == null) stateCopy(location = location)
        else btnLoc.setTint(R.color.white)
    }

    private fun initButtonChooseCategory() = with(binding!!) {
        when (getState()?.category) {
            null -> {}
            Category.LOST.toString() -> {
                btnSelectCat.setImageResource(R.drawable.ic_lost)
                tvSelectCat.text = getString(R.string.tv_select_cat_lost)
            }
            Category.FOUND.toString() -> {
                btnSelectCat.setImageResource(R.drawable.ic_found)
                tvSelectCat.text = getString(R.string.tv_select_cat_found)
            }
        }
    }

    private fun initButtonShowTelNum() = with(binding!!) {
        if (getState()?.telNum.isNullOrBlank()) return@with
        etShowTelNum.setText(getState()?.telNum)
        btnShowTelNum.setTint(R.color.white)
    }

    private fun initAdditionalInformation() = with(binding!!) {
        if (getState()?.additionalInfo.isNullOrBlank()) return@with
        etAdditionalInfo.setText(getState()?.additionalInfo)
    }

    private fun initNavigator() {
        navigator().listenResult(InstanceStateCameraFragment::class.java, viewLifecycleOwner) {
            if (it.message == SUCCESS) {
                if (getState()?.numPageVP!! < getState()?.listPhotoUri!!.size) {
                    stateCopy(
                        removePhotoUriPosition = getState()?.numPageVP,
                        setPhotoUriToRemovedPosition = it.uri!!
                    )
                } else {
                    stateCopy(photoUri = it.uri!!)
                }
                Log.d("!!!initNavigator", "${getState()}")
                notifyAdapter()
            }
        }
        navigator().listenResult(ChangeLocationFragmentArguments::class.java, viewLifecycleOwner) {
            stateCopy(location = it.location)
            initBtnLocation()
        }
    }

    private fun initSmilesAdapter() = with(binding!!) {
        smileAdapter = ChooseSmileAdapter { numEmoji->
            val listEmoji = resources.getStringArray(R.array.emoji_array).map { it }
            val selectedEmoji = listEmoji[numEmoji]

            Log.d("!!!listEmoji", "${listEmoji}")
            val start = etAdditionalInfo.selectionStart
            val startText = etAdditionalInfo.text.toString().substring(0, start)
            val endText = etAdditionalInfo.text.toString()
                .substring(start, etAdditionalInfo.text.toString().length)
            etAdditionalInfo.setText(startText + selectedEmoji + endText)
            etAdditionalInfo.setSelection((startText + selectedEmoji).length)
        }
        rvSmile.adapter = smileAdapter
        smileAdapter?.smiles = resources.getStringArray(R.array.emoji_array).map { it }
        rvSmile.layoutManager = GridLayoutManager(requireContext(), 5)
    }

    private fun initViewPager() = with(binding!!) {
        choosePhotoAdapter = ChoosePhotoAdapter(object : ChoosePhotoListener {
            override fun choosePhoto(position: Int) {
                viewModel.goToCameraFragment()
            }

            override fun deletePhoto(position: Int) {
                stateCopy(removePhotoUriPosition = position)
                notifyAdapter()
            }

            override fun showScalablePhoto(position: Int) {
//                TODO("Not yet implemented")
            }
        })
        choosePhotoViewPager.adapter = choosePhotoAdapter
        TabLayoutMediator(choosePhotoTabLayout, choosePhotoViewPager) { _, _ -> }.attach()
        notifyAdapter()

        choosePhotoViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    stateCopy(numPageVP = position)
                }
            }
        )
    }

    private fun notifyAdapter() = with(binding) {
        Log.d("!!!notifyAdapter", "${getState()}")
        val stateListUri = getState()?.listPhotoUri!!
        val listUri = arrayListOf<Uri>()
        if (stateListUri.isNotEmpty()) listUri.addAll(stateListUri)
        if (listUri.size < MAX_COUNT_IMAGE) listUri.add("".toUri())
        choosePhotoAdapter?.photos = listUri.toList()
    }

    private fun prepareToPublishApplication() = with(binding!!) {
        val currentState = getState() ?: return@with
        currentState.let { state ->
            if (state.listPhotoUri.isNullOrEmpty()) {
                toastL(getString(R.string.no_change_photo))
                return@with
            }
            if (state.location == null) {
                toastL(getString(R.string.no_change_location))
                return@with
            }
            if (state.category.isNullOrBlank()) {
                toastL(getString(R.string.no_change_category))
                return@with
            }

            if (state.telNum.isNullOrBlank()) {
                stateCopy(enterTelNum = defaultTelNum)
            }

            if (state.additionalInfo.isNullOrBlank()) {
                toastL(getString(R.string.no_change_additional_info))
                return@with
            }
        }
        viewModel.launchPreparePhotoToPublish(
            LoadDialogDisplayClass(
                R.string.preparing_images_for_uploading,
                getString(R.string.one_1)
            )
        )
    }

    private fun getByteArrayForMainPhoto() {
        getState()?.listPhotoUri ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val uri = getState()?.listPhotoUri!![0]
            if (uri.toString().startsWith("content"))
                ImageManager.imageResize(SIZE_150, uri, requireActivity()) { byteArray150 ->
                    stateCopy(imageIndex = getState()?.imageIndex!!.plus(1))
                    viewModel.putByteArrayToList(byteArray150)
                    Log.d("!!!getByteArrayForMainPhoto", "${getState()?.imageIndex!!} _ }")
                    viewModel.launchPreparePhotoToPublish(
                        LoadDialogDisplayClass(
                            mess = R.string.preparing_images_for_uploading,
                            numPhoto = getState()?.imageIndex!!.plus(1).toString()
                        )
                    )
                }
            else
                ImageManager.firebaseImageResize(uri, requireActivity()) { biteArrayFromFirebase ->
                    stateCopy(imageIndex = getState()?.imageIndex!!.plus(1))
                    viewModel.putByteArrayToList(biteArrayFromFirebase)
                    Log.d("!!!getByteArrayForMainPhotoFB", "${getState()?.imageIndex!!} _ }")
                    viewModel.launchPreparePhotoToPublish(
                        LoadDialogDisplayClass(
                            mess = R.string.preparing_images_for_uploading,
                            numPhoto = getState()?.imageIndex!!.plus(1).toString()
                        )
                    )
                }
        }
    }

    private fun getByteArrayFromPhoto() {
        Log.d("!!!getByteArrayFromPhoto0", "${getState()?.imageIndex!!} _ }")
        getState()?.listPhotoUri ?: return
        val index = getState()?.imageIndex!!
        if (index == -1) {
            getByteArrayForMainPhoto()
            return
        }
        val uri = getState()?.listPhotoUri!![index]
        Log.d("!!!getByteArrayFromPhoto0", "${index} _ ${uri}")

        if (uri.toString().startsWith("content")) {
            CoroutineScope(Dispatchers.IO).launch {
                ImageManager.imageResize(SIZE_1000, uri, requireActivity()) { byteArray1000 ->
                    Log.d("!!!getByteArrayFromPhoto0", "${getState()?.imageIndex!!} _ ${uri}")
                    stateCopy(imageIndex = index.plus(1))
                    viewModel.putByteArrayToList(byteArray1000)
                    Log.d(
                        "!!!getByteArrayFromPhoto1",
                        "${getState()?.imageIndex!!} _ ${getState()?.listPhotoUri!!.size}"
                    )
                    if (getState()?.imageIndex!! == getState()?.listPhotoUri!!.size) {
                        viewModel.publishPhotos(getState()!!, args.argsAAF)
                    } else {
                        viewModel.launchPreparePhotoToPublish(
                            LoadDialogDisplayClass(
                                mess = R.string.preparing_images_for_uploading,
                                numPhoto = getState()?.imageIndex!!.plus(1).toString()
                            )
                        )
                    }
                }
            }
        } else {
            stateCopy(imageIndex = index.plus(1))
            viewModel.addPhotoUriToListWithIndex(getState()!!.imageIndex!!, uri)
            if (getState()?.imageIndex!! == getState()?.listPhotoUri!!.size) {
                viewModel.publishPhotos(getState()!!, args.argsAAF)
            } else {
                viewModel.launchPreparePhotoToPublish(
                    LoadDialogDisplayClass(
                        mess = R.string.preparing_images_for_uploading,
                        numPhoto = getState()?.imageIndex!!.plus(1).toString()
                    )
                )
            }
        }
    }

    private fun displayUploadDialog(textInfo: LoadDialogDisplayClass) {
        Log.d("!!!dialogBinding2", "${dialogBinding}")
        dialogBinding?.tvInPB?.text = textInfo.numPhoto
        dialogBinding?.tvHelper?.text = if (textInfo.mess == R.string.images_uploading)
            getString(textInfo.mess!!, textInfo.numPhoto, textInfo.percentage)
        else
            getString(textInfo.mess!!, textInfo.percentage)
        Log.d("!!!displayUploadDialog3", "${textInfo.mess}")
    }

    override fun onResume() {
        super.onResume()
        Log.d("!!!onResumeAAF", "")
    }

    override fun onPause() {
        super.onPause()
        Log.d("!!!onPauseAAF", "")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        dialog?.dismiss()
        dialog = null
        dialogBinding = null
    }


    companion object {
        val TAG = AddApplicationFragment::class.java.name
        const val KEY_STATE = "KEY_STATE"
        const val MAX_COUNT_IMAGE = 3
        const val SIZE_1000 = 1000
        const val SIZE_150 = 150
    }
}