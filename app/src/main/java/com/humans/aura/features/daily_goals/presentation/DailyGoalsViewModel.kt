package com.humans.aura.features.daily_goals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.core.domain.models.GoalSubtaskDraft
import com.humans.aura.features.daily_goals.domain.ClearTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayActivitiesUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.SaveTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.ToggleGoalSubtaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DailyGoalsViewModel(
    observeTodayGoalUseCase: ObserveTodayGoalUseCase,
    observeTodayActivitiesUseCase: ObserveTodayActivitiesUseCase,
    private val saveTodayGoalUseCase: SaveTodayGoalUseCase,
    private val toggleGoalSubtaskUseCase: ToggleGoalSubtaskUseCase,
    private val clearTodayGoalUseCase: ClearTodayGoalUseCase,
) : ViewModel() {

    private val mainTitleInput = MutableStateFlow("")
    private val subtaskInputs = MutableStateFlow(listOf("", "", ""))
    private val isSaving = MutableStateFlow(false)
    private val isTogglingSubtask = MutableStateFlow(false)
    private val formState = combine(
        mainTitleInput,
        subtaskInputs,
        isSaving,
        isTogglingSubtask,
    ) { mainTitle, subtasks, saving, toggling ->
        GoalFormState(
            mainTitle = mainTitle,
            subtasks = subtasks,
            isSaving = saving,
            isToggling = toggling,
        )
    }

    val uiState: StateFlow<DailyGoalsUiState> = combine(
        observeTodayGoalUseCase(),
        observeTodayActivitiesUseCase(),
        formState,
    ) { goal, todayActivities, formState ->
        DailyGoalsUiState(
            goal = goal,
            mainTitleInput = if (formState.mainTitle.isBlank()) goal?.mainTitle.orEmpty() else formState.mainTitle,
            subtaskInputs = mergeSubtaskInputs(goal, formState.subtasks),
            todayActivities = todayActivities,
            isLoading = false,
            isSaving = formState.isSaving,
            isTogglingSubtask = formState.isToggling,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DailyGoalsUiState(isLoading = true),
    )

    fun onMainTitleChanged(value: String) {
        mainTitleInput.value = value
    }

    fun onSubtaskChanged(index: Int, value: String) {
        val mutable = subtaskInputs.value.toMutableList()
        while (mutable.size <= index) {
            mutable += ""
        }
        mutable[index] = value
        subtaskInputs.value = mutable
    }

    fun saveTodayGoal() {
        if (isSaving.value) return

        viewModelScope.launch {
            isSaving.value = true
            val titleToSave = mainTitleInput.value.ifBlank {
                uiState.value.goal?.mainTitle.orEmpty()
            }
            runCatching {
                saveTodayGoalUseCase(
                    mainTitle = titleToSave,
                    subtasks = subtaskInputs.value.map { title ->
                        GoalSubtaskDraft(title = title, isCompleted = false)
                    },
                )
            }.onSuccess {
                mainTitleInput.value = ""
                subtaskInputs.value = listOf("", "", "")
            }.also {
                isSaving.value = false
            }
        }
    }

    fun toggleSubtask(subtaskId: Long, isCompleted: Boolean) {
        if (isTogglingSubtask.value) return

        viewModelScope.launch {
            isTogglingSubtask.value = true
            runCatching {
                toggleGoalSubtaskUseCase(subtaskId, isCompleted)
            }.also {
                isTogglingSubtask.value = false
            }
        }
    }

    fun clearTodayGoal() {
        viewModelScope.launch {
            clearTodayGoalUseCase()
            mainTitleInput.value = ""
            subtaskInputs.value = listOf("", "", "")
        }
    }

    private fun mergeSubtaskInputs(
        goal: com.humans.aura.core.domain.models.DailyGoal?,
        inputs: List<String>,
    ): List<String> {
        if (inputs.any { it.isNotBlank() }) {
            return inputs
        }

        val goalInputs = goal?.subtasks?.map { it.title }.orEmpty()
        return if (goalInputs.isEmpty()) listOf("", "", "") else goalInputs + List((3 - goalInputs.size).coerceAtLeast(0)) { "" }
    }

    private data class GoalFormState(
        val mainTitle: String,
        val subtasks: List<String>,
        val isSaving: Boolean,
        val isToggling: Boolean,
    )
}
