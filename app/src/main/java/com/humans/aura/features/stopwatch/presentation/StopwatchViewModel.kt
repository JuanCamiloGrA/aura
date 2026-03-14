package com.humans.aura.features.stopwatch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.features.stopwatch.domain.ClearActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.LogNewActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveCurrentActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveRecentActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.PredictNextActivityTitleUseCase
import com.humans.aura.features.stopwatch.domain.UpdateCurrentActivityStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class StopwatchViewModel(
    observeCurrentActivityUseCase: ObserveCurrentActivityUseCase,
    observeRecentActivitiesUseCase: ObserveRecentActivitiesUseCase,
    private val logNewActivityUseCase: LogNewActivityUseCase,
    private val predictNextActivityTitleUseCase: PredictNextActivityTitleUseCase,
    private val updateCurrentActivityStatusUseCase: UpdateCurrentActivityStatusUseCase,
    private val clearActivitiesUseCase: ClearActivitiesUseCase,
) : ViewModel() {

    private val draftTitle = MutableStateFlow("")
    private val prediction = MutableStateFlow<com.humans.aura.features.stopwatch.domain.ActivityPrediction?>(null)
    private val isPredictionAutofilled = MutableStateFlow(false)
    private val isLogging = MutableStateFlow(false)
    private val draftState = combine(
        draftTitle,
        prediction,
        isPredictionAutofilled,
        isLogging,
    ) { currentDraftTitle, currentPrediction, predictionAutofilled, logging ->
        DraftState(
            draftTitle = currentDraftTitle,
            prediction = currentPrediction,
            isPredictionAutofilled = predictionAutofilled,
            isLogging = logging,
        )
    }

    val uiState: StateFlow<StopwatchUiState> = combine(
        observeCurrentActivityUseCase(),
        observeRecentActivitiesUseCase(),
        draftState,
    ) { currentActivity, recentActivities, currentDraftState ->
        StopwatchUiState(
            currentActivity = currentActivity,
            recentActivities = recentActivities,
            draftTitle = currentDraftState.draftTitle,
            prediction = currentDraftState.prediction,
            isPredictionAutofilled = currentDraftState.isPredictionAutofilled,
            runningDurationLabel = currentActivity?.let(::formatRunningDuration) ?: "00:00:00",
            isLoading = false,
            isLogging = currentDraftState.isLogging,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StopwatchUiState(isLoading = true),
    )

    init {
        refreshPrediction()
    }

    fun onDraftTitleChanged(value: String) {
        draftTitle.value = value
        prediction.value = null
        isPredictionAutofilled.value = false
    }

    fun usePrediction() {
        applyPrediction(prediction.value, forceAutofill = true)
    }

    fun logNewActivity() {
        if (isLogging.value) return

        viewModelScope.launch {
            isLogging.value = true
            val titleToLog = draftTitle.value.ifBlank {
                prediction.value?.title.orEmpty()
            }
            runCatching {
                logNewActivityUseCase(titleToLog)
            }.onSuccess {
                draftTitle.value = ""
                isPredictionAutofilled.value = false
                refreshPrediction()
            }.also {
                isLogging.value = false
            }
        }
    }

    fun markInaccurate() {
        updateStatus(ActivityStatus.INACCURATE)
    }

    fun markLost() {
        updateStatus(ActivityStatus.LOST)
    }

    fun clearAll() {
        viewModelScope.launch {
            clearActivitiesUseCase()
            draftTitle.value = ""
            prediction.value = null
            isPredictionAutofilled.value = false
        }
    }

    fun refreshPrediction() {
        viewModelScope.launch {
            applyPrediction(predictNextActivityTitleUseCase())
        }
    }

    private fun applyPrediction(
        nextPrediction: com.humans.aura.features.stopwatch.domain.ActivityPrediction?,
        forceAutofill: Boolean = false,
    ) {
        prediction.value = nextPrediction
        if (forceAutofill || draftTitle.value.isBlank() || isPredictionAutofilled.value) {
            draftTitle.value = nextPrediction?.title.orEmpty()
            isPredictionAutofilled.value = nextPrediction != null
        }
    }

    private fun updateStatus(status: ActivityStatus) {
        viewModelScope.launch {
            updateCurrentActivityStatusUseCase(status)
        }
    }

    private fun formatRunningDuration(activity: com.humans.aura.core.domain.models.Activity): String {
        val endMillis = activity.endTimeEpochMillis ?: System.currentTimeMillis()
        val totalSeconds = ((endMillis - activity.startTimeEpochMillis).coerceAtLeast(0) / 1000L)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private data class DraftState(
        val draftTitle: String,
        val prediction: com.humans.aura.features.stopwatch.domain.ActivityPrediction?,
        val isPredictionAutofilled: Boolean,
        val isLogging: Boolean,
    )
}
