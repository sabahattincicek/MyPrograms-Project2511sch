package com.saboon.project_2511sch.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ISettingsRepository
import com.saboon.project_2511sch.domain.usecase.home.GetHomeDisplayItemsUseCase
import com.saboon.project_2511sch.presentation.home.DisplayItemHome
import com.saboon.project_2511sch.presentation.home.DisplayRange
import com.saboon.project_2511sch.presentation.settings.SettingsConstants
import com.saboon.project_2511sch.presentation.task.FilterTask
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.toFormattedString
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.Calendar
import androidx.glance.appwidget.lazy.items
import androidx.glance.color.ColorProvider
import androidx.glance.layout.fillMaxHeight
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.util.ModelColorConstats
import java.util.concurrent.TimeUnit

class WidgetHome : GlanceAppWidget(){


    private lateinit var getHomeDisplayItemsUseCase: GetHomeDisplayItemsUseCase
    private lateinit var settingsRepository: ISettingsRepository
    private var isColorEnabled: Boolean = true
    private lateinit var colorSource: String
    private lateinit var darkMode: String
    private val LocalWidgetTextColor = staticCompositionLocalOf {
        ColorProvider(Color.Black) //default
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint{
        fun getHomeDisplayItemsUseCase(): GetHomeDisplayItemsUseCase
        fun settingsRepository(): ISettingsRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        getHomeDisplayItemsUseCase = entryPoint.getHomeDisplayItemsUseCase()
        settingsRepository = entryPoint.settingsRepository()

        val filter = FilterTask(lesson = true, exam = true, homework = true)
        val dateRange = getInitialRange()

        val result = getHomeDisplayItemsUseCase(filter, dateRange.start, dateRange.end).first()

        val displayItems = if (result is Resource.Success){
            result.data?.filter { it !is DisplayItemHome.FooterItemHome } ?: emptyList()
        }else emptyList()

        isColorEnabled = settingsRepository.getHomeListItemColorEnabled().first()
        colorSource = settingsRepository.getHomeListItemColorSource().first()
        darkMode = settingsRepository.getDarkMode().first()

        provideContent {
            WidgetViewHome(displayItems)
        }

    }

    private suspend fun getInitialRange(): DisplayRange {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        val viewRange = settingsRepository.getHomeViewRange().first()

        return when (viewRange) {
            SettingsConstants.HomeViewRange.WEEK -> {
                // Pazartesiye git
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val start = getDayStartMillis(calendar.timeInMillis)

                // Pazartesi üzerine 6 gün ekle (Pazar)
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                val end = getDayEndMillis(calendar.timeInMillis)

                DisplayRange(start, end)
            }

            SettingsConstants.HomeViewRange.MONTH -> {
                // Ayın 1'ine git
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = getDayStartMillis(calendar.timeInMillis)

                // Ayın son gününe git
                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                val end = getDayEndMillis(calendar.timeInMillis)

                DisplayRange(start, end)
            }

            else -> {
                // Pazartesiye git
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val start = getDayStartMillis(calendar.timeInMillis)

                // Pazartesi üzerine 6 gün ekle (Pazar)
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                val end = getDayEndMillis(calendar.timeInMillis)

                DisplayRange(start, end)
            }
        }
    }
    private fun getDayStartMillis(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    private fun getDayEndMillis(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    @Composable
    private fun WidgetViewHome(items: List<DisplayItemHome>){
        val context = LocalContext.current
        val bgColorProvider = GlanceTheme.colors.surface
        val textColorProvider = GlanceTheme.colors.onSurface
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColorProvider)
                .cornerRadius(16.dp)
                .padding(8.dp)
        ) {
            Text(
                text = context.getString(R.string.app_name),
                style = TextStyle(
                    fontSize = 16.sp,
                    color = textColorProvider
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (items.isEmpty()){
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Ders Bulunamadi", style = TextStyle(color = ColorProvider(Color.Gray)))
                }
            }else{
                LazyColumn(
                    modifier = GlanceModifier
                        .fillMaxSize(),
                ) {
                    items(items){ item ->
                        when(item){
                            is DisplayItemHome.HeaderItemHome -> RowWidgetViewHeader(item)
                            is DisplayItemHome.ContentItemHome -> RowWidgetViewContent(item)
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RowWidgetViewHeader(item: DisplayItemHome.HeaderItemHome){
        val context = LocalContext.current
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val diffMillis = item.date - today
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
        val text = when {
            diffDays == 0L -> context.getString(R.string.today)
            diffDays == 1L -> context.getString(R.string.tomorrow)
            diffDays == -1L -> context.getString(R.string.yesterday)
            diffDays > 0 -> context.getString(R.string.in_n_days, diffDays.toInt())
            else -> context.getString(R.string.n_days_ago, kotlin.math.abs(diffDays).toInt())
        }
        val alpha = if(item.date < today) 0.3f else 1.0f
        val textColorProvider = ColorProvider(day = Color.Black.copy(alpha = alpha), night = Color.White.copy(alpha = alpha))
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.date.toFormattedString("EEEE"),
                style = TextStyle(
                    fontSize = 14.sp,
                    color = textColorProvider
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = textColorProvider
                ),
            )
        }
    }
    @Composable
    private fun RowWidgetViewContent(item: DisplayItemHome.ContentItemHome){
        val context = LocalContext.current
        val tag = item.tag
        val course = item.course
        val task = item.task
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val date1 = when(task){
            is Task.Lesson -> task.timeStart.toFormattedString("HH:mm")
            is Task.Exam -> task.timeStart.toFormattedString("HH:mm")
            is Task.Homework -> task.dueTime.toFormattedString("HH:mm")
        }
        val date2 = when(task){
            is Task.Lesson -> task.timeEnd.toFormattedString("HH:mm")
            is Task.Exam -> task.timeEnd.toFormattedString("HH:mm")
            is Task.Homework -> ""
        }
        val content1 = "${course.title}, ${task.title}"
        val content1Sub = when(task){
            is Task.Lesson -> {
                if (task.description != ""){ task.description }
                else {
                    if (course.description != "") course.description else course.people
                }
            }
            is Task.Exam -> {
                if (task.date > System.currentTimeMillis()){
                    "${context.getString(R.string.achieved_score)}: ${task.achievedScore}"
                }else{
                    if (task.description != "") task.description else "${context.getString(R.string.target_score)}: ${task.targetScore}"
                }
            }
            is Task.Homework -> task.description
        }
        val content2 = when(task){
            is Task.Lesson -> task.place
            is Task.Exam -> task.date.toFormattedString("dd.MM.yyyy")
            is Task.Homework -> ""
        }
        val content2Sub = when (task){
            is Task.Lesson -> "${context.getString(R.string.absence)}: ${task.absence.size}"
            is Task.Exam -> task.place
            is Task.Homework -> ""
        }
        val taskDate = when (task) {
            is Task.Lesson -> task.date
            is Task.Exam -> task.date
            is Task.Homework -> task.dueDate
        }
        val alpha = if (taskDate < today) 0.3f else 1.0f
        val textColorProvider = ColorProvider(day = Color.Black.copy(alpha = alpha), night = Color.White.copy(alpha = alpha))
        val containerColorProvider: ColorProvider
        if (isColorEnabled){
            val color = if (colorSource == SettingsConstants.HomeListItemColorSource.FROM_TAG){
                tag?.color
            }else{
                course.color
            }
            if (color != null){
                containerColorProvider = ColorProvider(Color( color.getContainerColor(context)).copy(alpha = alpha))
            } else {
                containerColorProvider = ColorProvider(Color.Transparent.copy(alpha = alpha))
            }
        }else{
            containerColorProvider = ColorProvider(Color.Transparent.copy(alpha = alpha))
        }
        val dividerColorProvider = when(task){
            is Task.Lesson -> ColorProvider(Color(ModelColorConstats.LESSON.toColorInt()).copy(alpha = alpha))
            is Task.Exam -> ColorProvider(Color(ModelColorConstats.EXAM.toColorInt()).copy(alpha = alpha))
            is Task.Homework -> ColorProvider(Color(ModelColorConstats.HOMEWORK.toColorInt()).copy(alpha = alpha))
        }
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                    .cornerRadius(1000.dp)
                    .background(containerColorProvider)
            ) {

                // LEFT: Date Section
                Column(
                    modifier = GlanceModifier.padding(end = 8.dp),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = date1,
                        style = TextStyle(fontSize = 14.sp, color = textColorProvider),
                        maxLines = 1
                    )
                    Text(
                        text = date2,
                        style = TextStyle(fontSize = 10.sp, color = textColorProvider),
                        maxLines = 1
                    )
                }

                // Divider
                Spacer(
                    modifier = GlanceModifier
                        .width(8.dp)
                        .padding(end = 8.dp)
                        .fillMaxHeight()
                        .cornerRadius(1000.dp)
                        .background(dividerColorProvider)
                )

                // RIGHT: Content Section
                Column(
                    modifier = GlanceModifier
                        .padding(start = 8.dp)
                        .fillMaxWidth()
                ) {

                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = content1,
                            style = TextStyle(fontSize = 14.sp, color = textColorProvider),
                            modifier = GlanceModifier.defaultWeight(),
                            maxLines = 1,
                        )
                        Text(
                            text = content2,
                            style = TextStyle(fontSize = 14.sp, color = textColorProvider),
                            maxLines = 1
                        )
                    }

                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = content1Sub,
                            style = TextStyle(fontSize = 10.sp, color = textColorProvider),
                            modifier = GlanceModifier.defaultWeight(),
                            maxLines = 1,
                        )
                        Text(
                            text = content2Sub,
                            style = TextStyle(fontSize = 10.sp, color = textColorProvider),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}