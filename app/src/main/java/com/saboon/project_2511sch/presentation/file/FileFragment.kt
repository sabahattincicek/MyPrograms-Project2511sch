package com.saboon.project_2511sch.presentation.file

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentFileBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.File as JavaFile

@AndroidEntryPoint
class FileFragment : Fragment() {

    private var _binding: FragmentFileBinding?=null
    private val binding get() = _binding!!

    private val args : FileFragmentArgs by navArgs()

    private lateinit var course: Course

    private val viewModelFile : ViewModelFile by viewModels()

    private lateinit var recyclerAdapter: RecyclerAdapterFile

    private val selectFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()){uri ->
            if (uri != null){
                saveFileFromUri(uri)
            }
            else{

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        course = args.course

        setupRecyclerAdapter()
        observeFilesState()

//        viewModelFile.getAllFilesByCourseId(course.id)

        binding.toolbar.subtitle = course.title

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.fabAddNewFile.setOnClickListener { anchorView ->
            val popup = PopupMenu(requireContext(), anchorView)
            popup.menuInflater.inflate(R.menu.add_file_menu, popup.menu)
            popup.setOnMenuItemClickListener { item -> 
                when(item.itemId){
                    R.id.action_add_file -> {
                        selectFileLauncher.launch(arrayOf("*/*"))
                        true
                    }
                    R.id.action_add_note -> {
                        // TODO: add necessary code for "add note" option
                        true
                    }
                    R.id.action_add_link -> {
                        // TODO: add necessary code for "add link" option
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun setupRecyclerAdapter(){
        recyclerAdapter = RecyclerAdapterFile()
        binding.programRecyclerView.apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun saveFileFromUri(uri: Uri){
        val contentResolver = requireContext().contentResolver
        var fileName = "unknown_file"
        var fileSize = 0L

        // 1. Dosyanın adını ve boyutunu ContentResolver ile sorgula.
        contentResolver.query(uri, null, null, null).use { cursor ->
            if (cursor!!.moveToFirst()){
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
            }
        }

        try {
            // 2. Seçilen dosyanın içeriğini okumak için bir InputStream aç.
            val inputStream = contentResolver.openInputStream(uri)

            // 3. Dosyayı kopyalamak için kendi özel depolama alanımızda yeni bir dosya oluştur.
            val newFileName = "${System.currentTimeMillis()}_${fileName}"
            val newFile = JavaFile(requireContext().filesDir, newFileName)
            val outputStream = FileOutputStream(newFile)

            // 4. İçeriği byte byte kopyala.
            inputStream?.copyTo(outputStream)

            // 5. Kaynakları serbest bırakmak için akışları kapat.
            inputStream?.close()
            outputStream.close()

            // 6. Kopyalama başarılı. Şimdi veritabanına kaydedilecek File nesnesini oluştur.
            val newFileObject = File(
                id = IdGenerator.generateFileId(newFile.name),
                programTableId = course.programTableId,
                courseId = course.id,
                title = fileName,
                description = null,
                fileType = contentResolver.getType(uri)?: "application/octet-stream",
                filePath = newFile.absolutePath,
                sizeInBytes = fileSize
            )

            viewModelFile.insertNewFile(newFileObject)
        }catch (e: Exception){

        }
    }

    private fun observeFilesState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.filesState.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            recyclerAdapter.submitList(resource.data)
                        }
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