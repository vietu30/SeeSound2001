package com.example.seesound.ui.home

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.seesound.R
import com.example.seesound.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
        viewModel.checkPermission()
    }

    private fun setupClickListeners() {
        binding.btnToggleMonitor.setOnClickListener {
            if (viewModel.hasPermission.value) {
                viewModel.toggleMonitoring()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveCurrentReading()
        }

        binding.btnHistory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dbLevel.collect { db ->
                        binding.tvDbValue.text = String.format(Locale.getDefault(), "%.1f", db)
                    }
                }

                launch {
                    viewModel.isMonitoring.collect { isMonitoring ->
                        binding.btnToggleMonitor.text = if (isMonitoring) {
                            getString(R.string.stop_monitoring)
                        } else {
                            getString(R.string.start_monitoring)
                        }
                    }
                }

                launch {
                    viewModel.hasPermission.collect { hasPermission ->
                        binding.tvPermissionWarning.isVisible = !hasPermission
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
