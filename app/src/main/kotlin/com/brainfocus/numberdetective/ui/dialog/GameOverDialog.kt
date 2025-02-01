package com.brainfocus.numberdetective.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.databinding.DialogGameOverBinding

class GameOverDialog : DialogFragment() {
    private var _binding: DialogGameOverBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_IS_WIN = "is_win"

        fun newInstance(title: String, message: String, isWin: Boolean): GameOverDialog {
            return GameOverDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_MESSAGE, message)
                    putBoolean(ARG_IS_WIN, isWin)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogGameOverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = requireArguments().getString(ARG_TITLE)
        val message = requireArguments().getString(ARG_MESSAGE)
        val isWin = requireArguments().getBoolean(ARG_IS_WIN)

        binding.titleText.text = title
        binding.messageText.text = message
        
        // Kazanma/kaybetme durumuna göre arka plan rengini ayarla
        val backgroundColor = if (isWin) {
            R.color.colorWin
        } else {
            R.color.colorLose
        }
        binding.root.setBackgroundResource(backgroundColor)

        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.playAgainButton.setOnClickListener {
            dismiss()
            activity?.recreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
