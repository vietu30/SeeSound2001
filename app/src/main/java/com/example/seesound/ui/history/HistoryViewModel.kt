package com.example.seesound.ui.history

import androidx.lifecycle.ViewModel
import com.example.seesound.data.local.entity.NoiseRecord
import com.example.seesound.data.repository.NoiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: NoiseRepository
) : ViewModel() {

    val history: Flow<List<NoiseRecord>> = repository.allRecords
}
