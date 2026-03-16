package com.humans.aura.features.stopwatch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.core.domain.interfaces.CurrentTimeTicker
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.ActivityPrediction
import com.humans.aura.features.stopwatch.domain.EnsureInitialActivityUseCase
import com.humans.aura.features.stopwatch.domain.LogNewActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveCurrentActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveRecentActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.PredictNextActivityTitleUseCase
import com.humans.aura.features.stopwatch.domain.UpdateCurrentActivityTitleUseCase
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
    private val ensureInitialActivityUseCase: EnsureInitialActivityUseCase,
    private val logNewActivityUseCase: LogNewActivityUseCase,
    private val predictNextActivityTitleUseCase: PredictNextActivityTitleUseCase,
    private val updateCurrentActivityTitleUseCase: UpdateCurrentActivityTitleUseCase,
    private val updateCurrentActivityStatusUseCase: UpdateCurrentActivityStatusUseCase,
    timeProvider: TimeProvider,
    currentTimeTicker: CurrentTimeTicker,
) : ViewModel() {

    private val draftTitle = MutableStateFlow("")
    private val prediction = MutableStateFlow<ActivityPrediction?>(null)
    private val isPredictionAutofilled = MutableStateFlow(false)
    private val editingTitle = MutableStateFlow("")
    private val isTitleEditorVisible = MutableStateFlow(false)
    private val isLogging = MutableStateFlow(false)
    private val nowEpochMillis = currentTimeTicker.tickEvery(RUNNING_DURATION_TICK_MILLIS).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = timeProvider.currentTimeMillis(),
    )
    private val draftInputState = combine(
        draftTitle,
        prediction,
        isPredictionAutofilled,
    ) { currentDraftTitle, currentPrediction, predictionAutofilled ->
        DraftInputState(
            draftTitle = currentDraftTitle,
            prediction = currentPrediction,
            isPredictionAutofilled = predictionAutofilled,
        )
    }
    private val titleEditorState = combine(
        editingTitle,
        isTitleEditorVisible,
    ) { currentEditingTitle, titleEditorVisible ->
        TitleEditorState(
            editingTitle = currentEditingTitle,
            isTitleEditorVisible = titleEditorVisible,
        )
    }
    private val draftState = combine(
        draftInputState,
        titleEditorState,
        isLogging,
    ) { inputState, editorState, logging ->
        DraftState(
            draftTitle = inputState.draftTitle,
            prediction = inputState.prediction,
            isPredictionAutofilled = inputState.isPredictionAutofilled,
            editingTitle = editorState.editingTitle,
            isTitleEditorVisible = editorState.isTitleEditorVisible,
            isLogging = logging,
        )
    }

    val uiState: StateFlow<StopwatchUiState> = combine(
        observeCurrentActivityUseCase(),
        observeRecentActivitiesUseCase(),
        draftState,
        nowEpochMillis,
    ) { currentActivity, recentActivities, currentDraftState, currentNowEpochMillis ->
        StopwatchUiState(
            currentActivity = currentActivity,
            recentActivities = recentActivities,
            draftTitle = currentDraftState.draftTitle,
            prediction = currentDraftState.prediction,
            isPredictionAutofilled = currentDraftState.isPredictionAutofilled,
            runningDurationLabel = currentActivity?.let { activity ->
                formatRunningDuration(activity, currentNowEpochMillis)
            } ?: EMPTY_DURATION_LABEL,
            editingTitle = currentDraftState.editingTitle,
            isTitleEditorVisible = currentDraftState.isTitleEditorVisible,
            isLoading = false,
            isLogging = currentDraftState.isLogging,
            isVoiceLoggingEnabled = !currentDraftState.isLogging,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = StopwatchUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            ensureInitialActivityUseCase()
            applyPrediction(predictNextActivityTitleUseCase())
        }
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
        logNewActivityWithTitle(draftTitle.value.ifBlank {
            prediction.value?.title.orEmpty()
        })
    }

    fun logVoiceActivity(title: String) {
        logNewActivityWithTitle(title)
    }

    fun openTitleEditor() {
        val currentTitle = uiState.value.currentActivity?.title.orEmpty()
        if (currentTitle.isBlank()) return

        editingTitle.value = currentTitle
        isTitleEditorVisible.value = true
    }

    fun onEditingTitleChanged(value: String) {
        editingTitle.value = value
    }

    fun saveCurrentActivityTitle() {
        if (isLogging.value) return

        viewModelScope.launch {
            isLogging.value = true
            runCatching {
                updateCurrentActivityTitleUseCase(editingTitle.value)
            }.onSuccess {
                editingTitle.value = ""
                isTitleEditorVisible.value = false
            }.also {
                isLogging.value = false
            }
        }
    }

    fun dismissTitleEditor() {
        isTitleEditorVisible.value = false
        editingTitle.value = ""
    }

    private fun logNewActivityWithTitle(rawTitle: String) {
        if (isLogging.value) return

        viewModelScope.launch {
            isLogging.value = true
            val titleToLog = rawTitle.trim()
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

    fun clearHonestyLabel() {
        updateStatus(ActivityStatus.ACTIVE)
    }

    fun refreshPrediction() {
        viewModelScope.launch {
            applyPrediction(predictNextActivityTitleUseCase())
        }
    }

    private fun applyPrediction(
        nextPrediction: ActivityPrediction?,
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

    private fun formatRunningDuration(
        activity: com.humans.aura.core.domain.models.Activity,
        currentNowEpochMillis: Long,
    ): String {
        val endMillis = activity.endTimeEpochMillis ?: currentNowEpochMillis
        val totalSeconds = ((endMillis - activity.startTimeEpochMillis).coerceAtLeast(0) / 1_000L)
        val hours = totalSeconds / 3_600L
        val minutes = (totalSeconds % 3_600L) / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private data class DraftState(
        val draftTitle: String,
        val prediction: ActivityPrediction?,
        val isPredictionAutofilled: Boolean,
        val editingTitle: String,
        val isTitleEditorVisible: Boolean,
        val isLogging: Boolean,
    )

    private data class DraftInputState(
        val draftTitle: String,
        val prediction: ActivityPrediction?,
        val isPredictionAutofilled: Boolean,
    )

    private data class TitleEditorState(
        val editingTitle: String,
        val isTitleEditorVisible: Boolean,
    )

    private companion object {
        private const val RUNNING_DURATION_TICK_MILLIS = 1_000L
        private const val EMPTY_DURATION_LABEL = "00:00:00"
    }
}
