package com.dzenis_ska.lostandfound.ui.fragments.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.FragmentChatBinding
import com.dzenis_ska.lostandfound.ui.utils.factory

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private val viewModel: ChatViewModel by viewModels { factory() }
    private var binding: FragmentChatBinding? = null
    private var state: InstanceStateChat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = savedInstanceState?.getParcelable(STATE_KEY) ?: InstanceStateChat()
                ?: throw IllegalArgumentException("!!!There is not getting instance InstanceStateChat")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_KEY, state)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }
}