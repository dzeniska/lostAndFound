package com.dzenis_ska.lostandfound.ui.fragments.showApplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.CustomProgressBarBinding
import com.dzenis_ska.lostandfound.databinding.FragmentShowApplicationBinding
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestFromBureau
import com.dzenis_ska.lostandfound.ui.fragments.add_application.Category
import com.dzenis_ska.lostandfound.ui.fragments.add_application.DefaultTelNum.defaultTelNum
import com.dzenis_ska.lostandfound.ui.fragments.map.MapFragment
import com.dzenis_ska.lostandfound.ui.fragments.showLocation.ShowLocationFragmentArguments
import com.dzenis_ska.lostandfound.ui.utils.*
import com.dzenis_ska.lostandfound.ui.utils.permissionsUtils.CALL_PHONE_PERMISSION
import com.dzenis_ska.lostandfound.ui.utils.permissionsUtils.allCallPhonePermissionsGranted
import com.dzenis_ska.lostandfound.ui.utils.permissionsUtils.gotCallPhonePermissions
import com.dzenis_ska.lostandfound.ui.utils.showApplication.callTelNum
import com.dzenis_ska.lostandfound.ui.utils.showApplication.showData
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.tabs.TabLayoutMediator


class ShowApplicationFragment: Fragment(R.layout.fragment_show_application) {
    private var runBlock: Runnable? = null
    private val argsSAF: ShowApplicationFragmentArgs by navArgs()
    private val viewModel: ShowApplicationViewModel by viewModels { factory(id = argsSAF.id) }

    var binding: FragmentShowApplicationBinding? = null
    var customProgressBarBinding: CustomProgressBarBinding? = null

    var showPhotoAdapter: ShowPhotoAdapter? = null

    var state: InstanceStateShowApplication? = null

    private val requestCallPhonePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            Log.d("!!!requestLocationPermissions", "${it.values}_ requestLocationPermissions")
            getCallPhonePermissions(it)
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.whenContextActive.contextAction = context
    }
    override fun onDetach() {
        super.onDetach()
        viewModel.whenContextActive.contextAction = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        customProgressBarBinding = null
        showPhotoAdapter = null
    }

    private fun getCallPhonePermissions(map: Map<String, @JvmSuppressWildcards Boolean>) {
        gotCallPhonePermissions(map) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("!!!onCreateViewSAF", "${argsSAF.id}")
        setTranslucentStatusAndNavigation(true)
        binding = FragmentShowApplicationBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = savedInstanceState?.getParcelable(STATE_KEY) ?: /*viewModel.savedInstanceState.value ?:*/ InstanceStateShowApplication()
                ?: throw IllegalArgumentException("!!!There is not getting instance InstanceStateShowApplication")
        Log.d("!!!onCreateSAF", "${state}")

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("!!!onViewCreatedSAF", "${state}")
        initViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("!!!onSaveInstanceStateSAF", "${state}")
        outState.putParcelable(STATE_KEY, state)
    }

    private fun initViewModel() {
        viewModel.uploadState.observe(viewLifecycleOwner){
            binding!!.pbUpdate.isVisible = it
        }
        viewModel.requestForInfo.observe(viewLifecycleOwner) {
            initTvData(views = it.views.toString())
        }
        viewModel.requestFromBureauAndMyUid.observe(viewLifecycleOwner) {
            Log.d("!!!requestToBureauObserve", "${it}")
            if (it.requestFromBureau != null)
                initUI(it)
            else
                navigator().popBackStack()
        }

        viewModel.dialogBlockedAppEvent.observe(viewLifecycleOwner) {
            it.getValue()?.let { simpleDialog->
                Dialog.createDialogAppWasDisabled(requireContext(), simpleDialog.header, simpleDialog.message){ _, run->
                    runBlock = run
                }
            }
        }

        viewModel.snackBarBase.observe(viewLifecycleOwner) {simpleSnackBar->
            Log.d("!!!createSnackBar01", "${customProgressBarBinding} _ ${simpleSnackBar?.countDownTimer}")

            if (simpleSnackBar != null){
                if (customProgressBarBinding == null)
                    customProgressBarBinding = CustomProgressBarBinding.inflate(layoutInflater)

                CustomSnackBar.createSnackBar(requireContext(), binding!!.root, customProgressBarBinding!!, simpleSnackBar.info, simpleSnackBar.countDownTimer){
                    if(it) {
                        Log.d("!!!snackBarBase", "_ super")
                        when (simpleSnackBar.clazz) {
                            is RequestFromBureau -> viewModel.blockAppStepOne(simpleSnackBar.clazz, simpleSnackBar.mess!!)
                        }
//                        viewModel.clearJob()
                        customProgressBarBinding = null
                    }
                    else {
                        Log.d("!!!snackBarBase", "_ super stop")
                        Log.d("!!!clearJob", "clearJob2")
                        viewModel.clearJob()
                        customProgressBarBinding = null
                    }
                }
            }
        }

        viewModel.popBackStackToMapFragment.observe(viewLifecycleOwner) {
            it.getValue()?.let {
                navigator().popBackStackToMapFragment()
            }
        }
        viewModel.popBackStack.observe(viewLifecycleOwner) {
            it.getValue()?.let {
                navigator().popBackStack()
            }
        }

        viewModel.listPreloadPhotoAdapter.observe(viewLifecycleOwner) {
            it.getValue()?.let { spp->
                Log.d("!!!listLoadPhotoAdapter", "${spp.bitmap?.height} _ ${spp.listUri.size}")
                binding!!.showPhotoViewPagerPB?.isVisible = false
                showPhotoAdapter?.bitmap = spp.bitmap
                if (spp.listUri.isNotEmpty()) showPhotoAdapter?.photos = spp.listUri
                else showPhotoAdapter?.photos = listOf("".toUri())
                binding!!.showPhotoViewPager.setCurrentItem(state?.currentNumPageAdapterPhoto ?: 0, false)
            }
        }
        viewModel.onBlocked.observe(viewLifecycleOwner) {event->
            event.getValue()?.let {
                Log.d("!!!onBlocked", "${it}_ ${runBlock}")
                if (it) {
                    runBlock?.run()
                    viewModel.clearJob()
                    binding!!.tvNoRules.text = resources.getString(R.string.ann_blocked)
                    binding!!.tvNoRules.setTextColor(Color.RED)
                    binding!!.tvNoRules.textSize = 24.5f
                    binding!!.btnNoRules.hide()
                    //todo when rotation btnNoRules show else time
                } else {
                    Log.d("!!!clearJob", "clearJob3")

                    runBlock?.run()
                }
                runBlock = null
            }
        }

        viewModel.toastEvent.observe(viewLifecycleOwner) {
            it.getValue()?.let { mess->
                Log.d("!!!toastEventSAF", "${mess}")
                toastL(mess)
            }
        }
    }

    private fun initUI(requestToBureauAndMyUid: RequestFromBureauAndMyUid) = with(binding!!) {
        Log.d("!!!initUISAF", "$requestToBureauAndMyUid")
        val requestFromBureau = requestToBureauAndMyUid.requestFromBureau!!

        initPhotoAdapter()
        initTvData(showData = showData(requestFromBureau.time?.toLong() ?: 0L))

        when (requestFromBureau.category) {
            Category.FOUND.toString() -> {
                ivSelectCat.setImageResource(R.drawable.ic_found)
                tvSetCat.text = getString(R.string.item_found)
            }
            else -> {
                ivSelectCat.setImageResource(R.drawable.ic_lost)
                tvSetCat.text = getString(R.string.item_lost)
            }
        }
        tvShowTelNum.setText(requestFromBureau.telNum, getString(R.string._375_45))
        tvMessage.setText(requestFromBureau.additionalInfo, getString(R.string.no_additional_info))
        Log.d("!!!isNotBlank", "${requestFromBureau.messBlocked} _ ${requestToBureauAndMyUid.uid == requestFromBureau.uid}")
        (requestToBureauAndMyUid.uid == requestFromBureau.uid).let {
            groupNoRules.isVisible = !it
            groupButtonsEditAndDelete.isVisible = it
            if (requestFromBureau.messBlocked.isNotBlank()){
                groupWarning.isVisible = it
                tvWarning.text = getString(R.string.blocked_add, requestFromBureau.messBlocked)
            } else {
                groupWarning.isVisible = false
            }
        }
        initClick(requestFromBureau)
    }

    private fun initClick(requestFromBureau: RequestFromBureau) = with(binding!!) {

        btnLoc.setOnClickListener {
            navigator().goToShowLocationFragment(ShowLocationFragmentArguments(LatLng(requestFromBureau.latitude, requestFromBureau.longitude)))
        }
        btnInfo.setOnClickListener {
            Dialog.createInfoDialog(requireContext()){}
        }
        btnTelNum.setOnClickListener{
            if(!allCallPhonePermissionsGranted()) {
                requestCallPhonePermission.launch(CALL_PHONE_PERMISSION)
            } else {
                if (tvShowTelNum.text != defaultTelNum)
                    callTelNum(tvShowTelNum.text.toString())
                else
                    toastL(getString(R.string.no_tel_num))
            }

        }

        tvNoRules.setOnClickListener {
            //todo #h3
            Dialog.createInfoDialog(requireContext()){}
        }
        btnNoRules.setOnClickListener {
            Dialog.createInfoDialog(requireContext(), getString(R.string.next)) {
                Dialog.createDialogNoRules(requireContext()) { mess, run ->
//                runBlock = run
                    viewModel.createSnackBar(requestFromBureau, getString(R.string.app_will_be_block), mess)
                }
            }
        }
        btnEdit.setOnClickListener {
            navigator().goToAddApplicationFragmentFromShowApplicationFragment(requestFromBureau.toAddApplicationFragmentArguments(isEditApplication = true))
        }
        btnDelete.setOnClickListener {
            Dialog.createDialog(
                requireContext(),
                cancelable = true,
                getString(R.string.warning),
                getString(R.string.are_you_sure_delete_application),
                getString(R.string.yes_sure),
                getString(R.string.no_no_no),
                { viewModel.deleteApplication(requestFromBureau) },
                {}
            ) { it.run() }

        }
    }

    private fun initPhotoAdapter() = with(binding!!) {
        Log.d("!!!initPhotoAdapter", "${showPhotoAdapter} ")

        showPhotoViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    Log.d("!!!onPageSelected", "${position} ")
                    state = state?.copy(currentNumPageAdapterPhoto = position)
                }
            }
        )

        showPhotoAdapter = ShowPhotoAdapter {
            Log.d("!!!goToShowFullPhotoFragmentSAF", "$it")
            navigator().goToShowFullPhotoFragment(it)
        }
        showPhotoViewPager.adapter = showPhotoAdapter
        TabLayoutMediator(showPhotoTabLayout, showPhotoViewPager) { _, _ -> }.attach()
        showPhotoViewPager.setCurrentItem(state?.currentNumPageAdapterPhoto ?: 0 , false)
        Log.d("!!!getPreloadImageBitMap1", "${state?.currentNumPageAdapterPhoto} ")
        viewModel.showPreloadPhoto()




    }

    private fun initTvData(
        showData: String = binding!!.tvData.text.toString().substringBefore(getString(R.string.eye)).filter{ !it.isWhitespace()} ,
        views: String = binding!!.tvData.text.toString().substringAfter(getString(R.string.eye))
    ) {
        binding!!.tvData.text = "${showData}   ${getString(R.string.eye)} ${views}"
    }

    companion object {
        private const val STATE_KEY = "STATE_KEY"
    }
}