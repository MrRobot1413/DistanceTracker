package ru.mrrobot1413.distancetracker.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.mrrobot1413.distancetracker.R
import ru.mrrobot1413.distancetracker.databinding.FragmentResultBinding

class ResultFragment : BottomSheetDialogFragment() {
    private val args: ResultFragmentArgs by navArgs()

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            txtDistanceValue.text = args.result.distance + "km"
            txtTimeElapsed.text = args.result.time + " hours"
            btnShare.setOnClickListener {
                openShareIntent()
            }
        }
    }

    private fun openShareIntent() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.extra_subject_header))
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            getString(R.string.i_passed) + " ${args.result.distance}km ${getString(R.string.`in`)} ${args.result.time}"
        )
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}