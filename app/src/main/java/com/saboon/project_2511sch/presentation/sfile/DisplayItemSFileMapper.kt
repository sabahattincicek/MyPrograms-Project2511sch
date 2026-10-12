package com.saboon.project_2511sch.presentation.sfile

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.CourseFiles
import com.saboon.project_2511sch.domain.model.SFile

/**
 * Builds the rows of the global file list: for every course a header with its title followed
 * by its files, then a footer with the total file count.
 * Returns an empty list when there is no file, so the screen shows its empty state.
 */
fun List<CourseFiles>.toSFileDisplayItems(): List<DisplayItemSFile> {
    val fileCount = sumOf { it.files.size }
    if (fileCount == 0) return emptyList()

    val items = mutableListOf<DisplayItemSFile>()
    forEach { group ->
        items.add(DisplayItemSFile.HeaderSFile(group.course.title))
        group.files.forEach { sFile ->
            items.add(DisplayItemSFile.ContentSFile(course = group.course, sFile = sFile))
        }
    }
    items.add(DisplayItemSFile.FooterSFile(fileCount))
    return items
}

/**
 * Builds the rows of the file preview of a single [course]: its files followed by a footer.
 * The footer is always present, even without files, because the preview uses it as the
 * "add file" tile.
 */
fun List<SFile>.toSFileDisplayItems(course: Course): List<DisplayItemSFile> {
    return map<SFile, DisplayItemSFile> { DisplayItemSFile.ContentSFile(course = course, sFile = it) } +
        DisplayItemSFile.FooterSFile(size)
}
