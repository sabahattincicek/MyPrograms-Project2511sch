package com.saboon.project_2511sch.presentation.tag

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentTagBinding
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.course.DialogFragmentCourse
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.presentation.widget.WidgetHelper
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.ModelColor
import com.saboon.project_2511sch.util.ModelColorConstats
import com.saboon.project_2511sch.util.OperationType
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class DialogFragmentTag: DialogFragment() {

    private val TAG = "CreateProgramTableDialog"

    private var _binding: DialogFragmentTagBinding? = null
    private val binding get() = _binding!!
    private val viewModelUser: ViewModelUser by activityViewModels()
    private val viewModelTag: ViewModelTag by viewModels()
    private lateinit var currentUser: User
    private var tag: Tag? = null
    private var selectedColor =  ModelColor()
    private var isActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: called")
        _binding = DialogFragmentTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sistemin çubuklarının (StatusBar ve NavBar) yüksekliğini al ve layout'a padding olarak ekle
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // v (yani senin root view'ın) sistem çubukları kadar içeri itilir
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,    // Üstteki bildirim çubuğunu kurtarır
                right = systemBars.right,
                bottom = systemBars.bottom // Alttaki navigasyon çubuğunu kurtarır
            )

            insets
        }

        arguments?.let {
            tag = BundleCompat.getParcelable(it,ARG_TAG, Tag::class.java)
        }

        setupColorCheckers()
        setupListeners()
        setupObservers()

        val isEditMode = tag != null
        if (isEditMode){
            binding.toolbar.title = getString(R.string.editTag)

            binding.etTitle.setText(tag!!.title)
            binding.etDescription.setText(tag!!.description)
            selectedColor = tag!!.color
            when(selectedColor.colorHex){
                ModelColorConstats.COLOR_1 -> {clearAllChecks(); binding.ivColorCk1.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_2 -> {clearAllChecks(); binding.ivColorCk2.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_3 -> {clearAllChecks(); binding.ivColorCk3.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_4 -> {clearAllChecks(); binding.ivColorCk4.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_5 -> {clearAllChecks(); binding.ivColorCk5.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_6 -> {clearAllChecks(); binding.ivColorCk6.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_7 -> {clearAllChecks(); binding.ivColorCk7.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_8 -> {clearAllChecks(); binding.ivColorCk8.visibility = View.VISIBLE}
            }
            isActive = tag!!.isActive
            binding.msActivation.isChecked = isActive
        }else{
            binding.toolbar.title = getString(R.string.createTag)

            binding.etTitle.requestFocus()
            binding.toolbar.menu.clear()
        }

        binding.btnSave.setOnClickListener {
            if(isEditMode){
                val updatedProgramTable = tag!!.copy(
                    isActive = isActive,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    color = selectedColor
                )
                viewModelTag.update(updatedProgramTable)
            }else{
                val newTag = Tag(
                    id = IdGenerator.generateId(binding.etTitle.text.toString()),
                    createdBy = currentUser.id,
                    appVersionAtCreation = getString(R.string.app_version),
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    color = selectedColor,
                )
                viewModelTag.insert(newTag)
            }
        }
        binding.mcvColor1.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_1)
            clearAllChecks()
            binding.ivColorCk1.visibility = View.VISIBLE
        }
        binding.mcvColor2.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_2)
            clearAllChecks()
            binding.ivColorCk2.visibility = View.VISIBLE
        }
        binding.mcvColor3.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_3)
            clearAllChecks()
            binding.ivColorCk3.visibility = View.VISIBLE
        }
        binding.mcvColor4.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_4)
            clearAllChecks()
            binding.ivColorCk4.visibility = View.VISIBLE
        }
        binding.mcvColor5.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_5)
            clearAllChecks()
            binding.ivColorCk5.visibility = View.VISIBLE
        }
        binding.mcvColor6.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_6)
            clearAllChecks()
            binding.ivColorCk6.visibility = View.VISIBLE
        }
        binding.mcvColor7.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_7)
            clearAllChecks()
            binding.ivColorCk7.visibility = View.VISIBLE
        }
        binding.mcvColor8.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_8)
            clearAllChecks()
            binding.ivColorCk8.visibility = View.VISIBLE
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.action_delete -> {
                    val dialog = DialogFragmentDeleteConfirmation.newInstance(binding.root.context.getString(R.string.delete), binding.root.context.getString(R.string.areYouSure_ifDeleteTagCouresWontDelete))
                    dialog.show(childFragmentManager, "Delete Course")
                    true
                }
                else -> {
                    false
                }
            }
        }
        binding.msActivation.setOnCheckedChangeListener { buttonView, isChecked ->
            isActive = isChecked
        }
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        Log.d(TAG, "onDestroy: called, binding set to null")
    }

    private fun setupColorCheckers(){
        binding.ivColorBg1.setBackgroundColor(ModelColorConstats.COLOR_1.toColorInt())
        binding.ivColorBg2.setBackgroundColor(ModelColorConstats.COLOR_2.toColorInt())
        binding.ivColorBg3.setBackgroundColor(ModelColorConstats.COLOR_3.toColorInt())
        binding.ivColorBg4.setBackgroundColor(ModelColorConstats.COLOR_4.toColorInt())
        binding.ivColorBg5.setBackgroundColor(ModelColorConstats.COLOR_5.toColorInt())
        binding.ivColorBg6.setBackgroundColor(ModelColorConstats.COLOR_6.toColorInt())
        binding.ivColorBg7.setBackgroundColor(ModelColorConstats.COLOR_7.toColorInt())
        binding.ivColorBg8.setBackgroundColor(ModelColorConstats.COLOR_8.toColorInt())
    }
    private fun setupListeners(){
        childFragmentManager.setFragmentResultListener(DialogFragmentDeleteConfirmation.REQUEST_KEY, this) { requestKey, result ->
            val isYes = result.getBoolean(DialogFragmentDeleteConfirmation.RESULT_KEY)
            if (isYes) {
                viewModelTag.delete(tag!!)
            }
        }
    }
    private fun setupObservers(){
        //USER STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelUser.currentUser.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            currentUser = resource.data!!
                        }
                    }
                }
            }
        }
        //TAG EVENT: INSERT, UPDATE, DELETE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTag.operationEvent.collect { event ->
                    when(event){
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading ->{}
                        is Resource.Success -> {
                            WidgetHelper.updateWidgetHome(requireContext())
                            val operationResult = event.data //BaseVMOperationResult<Tag>
                            val type = operationResult?.operationType
                            when(type) {
                                OperationType.INSERT -> {dismiss()}
                                OperationType.UPDATE -> {
                                    // eger update islemi yapildiysa ve activation degisitirildiyse bu taga
                                    // bagli butun derslerin altindaki tasklarin alarmlarini sync et
                                    if (tag != null){
                                        if (tag!!.isActive != operationResult.data.isActive){
                                            tag = operationResult.data
                                            viewModelTag.syncAlarms(tag!!){
                                                dismiss()
                                            }
                                        }else{
                                            dismiss()
                                        }
                                    }else{
                                        dismiss()
                                    }
                                }
                                OperationType.DELETE -> {dismiss()}
                                null -> {dismiss()}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun clearAllChecks(){
        binding.ivColorCk1.visibility = View.GONE
        binding.ivColorCk2.visibility = View.GONE
        binding.ivColorCk3.visibility = View.GONE
        binding.ivColorCk4.visibility = View.GONE
        binding.ivColorCk5.visibility = View.GONE
        binding.ivColorCk6.visibility = View.GONE
        binding.ivColorCk7.visibility = View.GONE
        binding.ivColorCk8.visibility = View.GONE
    }

    companion object {
        const val ARG_USER = "tag_dialog_fragment_arg_user"
        const val ARG_TAG = "tag_dialog_fragment_arg_tag"
        const val REQUEST_TAG = "tag_dialog_fragment_request_tag"
        const val RESULT_TAG = "tag_dialog_fragment_result_tag"

        fun newInstanceForCreate():DialogFragmentTag{
            val fragment = DialogFragmentTag()
            return fragment
        }
        fun newInstanceForUpdate(tag: Tag): DialogFragmentTag {
            val fragment = DialogFragmentTag()
            fragment.arguments = bundleOf(
                ARG_TAG to tag
            )
            return fragment
        }
    }
}