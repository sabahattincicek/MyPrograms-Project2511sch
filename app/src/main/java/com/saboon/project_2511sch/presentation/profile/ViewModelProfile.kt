package com.saboon.project_2511sch.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.usecase.settings.ExportDataUseCase
import com.saboon.project_2511sch.domain.usecase.settings.ImportDataUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ViewModelProfile @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
): ViewModel() {

    private val _exportEvent = Channel<Resource<File>>(Channel.BUFFERED)
    val exportEvent = _exportEvent.receiveAsFlow()

    private val _importEvent = Channel<Resource<Unit>>(Channel.BUFFERED)
    val importEvent = _importEvent.receiveAsFlow()


    fun exportData() {
        viewModelScope.launch {
            _exportEvent.send(Resource.Loading())
            _exportEvent.send(exportDataUseCase.execute())
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _importEvent.send(Resource.Loading())
            _importEvent.send(importDataUseCase.execute(uri))
        }
    }
}