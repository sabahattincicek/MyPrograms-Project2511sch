package com.saboon.project_2511sch.presentation.tag

import com.saboon.project_2511sch.domain.model.Tag

/**
 * Builds the rows of the tag list: one row per tag followed by a footer with the tag count.
 * Returns an empty list when there is no tag, so the screen shows its empty state.
 */
fun List<Tag>.toTagDisplayItems(): List<DisplayItemTag> {
    if (isEmpty()) return emptyList()
    return map { DisplayItemTag.ContentTag(it) } + DisplayItemTag.FooterTag(size)
}
