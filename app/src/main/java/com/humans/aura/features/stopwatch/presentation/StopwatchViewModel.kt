package com.humans.aura.features.stopwatch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.AppIntent
import com.humans.aura.features.day_closure.domain.HandleSleepIntentUseCase
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
    intentMediator: IntentMediator,
    private val handleSleepIntentUseCase: HandleSleepIntentUseCase,
) : ViewModel() {

    private val draftTitle = MutableStateFlow("")
    private val prediction = MutableStateFlow<com.humans.aura.features.stopwatch.domain.ActivityPrediction?>(null)
    private val isLogging = MutableStateFlow(false)

    val uiState: StateFlow<StopwatchUiState> = combine(
        observeCurrentActivityUseCase(),
        observeRecentActivitiesUseCase(),
        draftTitle,
        prediction,
        isLogging,
    ) { currentActivity, recentActivities, currentDraftTitle, currentPrediction, logging ->
        val effectiveDraft = if (currentDraftTitle.isBlank()) {
            currentPrediction?.title.orEmpty()
        } else {
            currentDraftTitle
        }

        StopwatchUiState(
            currentActivity = currentActivity,
            recentActivities = recentActivities,
            draftTitle = effectiveDraft,
            prediction = currentPrediction,
            runningDurationLabel = currentActivity?.let(::formatRunningDuration) ?: "00:00:00",
            isLoading = false,
            isLogging = logging,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StopwatchUiState(isLoading = true),
    )

    init {
        refreshPrediction()
        observeSleepEvents(intentMediator)
    }

    fun onDraftTitleChanged(value: String) {
        draftTitle.value = value
    }

    fun usePrediction() {
        draftTitle.value = prediction.value?.title.orEmpty()
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
        }
    }

    fun refreshPrediction() {
        viewModelScope.launch {
            prediction.value = predictNextActivityTitleUseCase()
        }
    }

    private fun updateStatus(status: ActivityStatus) {
        viewModelScope.launch {
            updateCurrentActivityStatusUseCase(status)
        }
    }

    private fun observeSleepEvents(intentMediator: IntentMediator) {
        viewModelScope.launch {
            intentMediator.intents.collect { intent ->
                if (intent is AppIntent.SleepLogged) {
                    handleSleepIntentUseCase(intent)
                }
            }
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
}
