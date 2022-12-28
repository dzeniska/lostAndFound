package com.dzenis_ska.lostandfound.ui.fragments.chat

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.FragmentChatBinding
import com.dzenis_ska.lostandfound.ui.utils.factory
import com.dzenis_ska.lostandfound.ui.utils.setTranslucentStatusAndNavigation
import com.dzenis_ska.lostandfound.ui.utils.toastS

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val argsCF: ChatFragmentArgs by navArgs()
    private val viewModel: ChatViewModel by viewModels { factory() }
    private var binding: FragmentChatBinding? = null
    private var state: InstanceStateChat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = savedInstanceState?.getParcelable(STATE_KEY) ?: InstanceStateChat()
                ?: throw IllegalArgumentException("!!!There is not getting instance InstanceStateChat")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentChatBinding.inflate(inflater, container, false).also { binding->
        setTranslucentStatusAndNavigation(false)
        this.binding = binding
//        binding.etToSendMessage.setText(argsCF.args.additionalInfo)
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClick()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.toastEvent.observe(viewLifecycleOwner) {
            it.getValue()?.let { mess ->
                Log.d("!!!toastEvent", "${mess}")
                toastS(mess)
            }
        }
    }

    private fun initClick() = with(binding!!) {
        ibSendMessage.setOnClickListener {
            viewModel.sendOnlyMessage(etToSendMessage.text.toString())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_KEY, state)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.whenContextActive.contextAction = context
    }
    override fun onDetach() {
        super.onDetach()
        viewModel.whenContextActive.contextAction = null
    }

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }
}