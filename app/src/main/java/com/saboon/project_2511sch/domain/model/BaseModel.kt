package com.saboon.project_2511sch.domain.model

interface BaseModel {
    /**
     * Unique identifier for the record.
     * Use UUID strings to prevent primary key collisions during distributed synchronization.
     */
    val id: String

    /**
     * Unix timestamp (milliseconds) indicating when the record was first persisted.
     */
    val createdAt: Long

    /**
     * The unique identifier of the user or system actor who created this record for auditing purposes.
     */
    val createdBy: String

    /**
     * The application version string at the time of creation.
     * Useful for tracking legacy data issues or schema-specific bugs.
     */
    val appVersionAtCreation: String

    /**
     * Unix timestamp (milliseconds) of the last modification.
     * Critical for Delta Sync operations to fetch only modified records.
     */
    val updatedAt: Long

    /**
     * Local version counter for Optimistic Concurrency Control.
     * Prevents "lost updates" during simultaneous local transactions.
     */
    val version: Int

    /**
     * Flag indicating if the record is functionally active within the business logic.
     */
    val isActive: Boolean

    /**
     * Implementation of the 'Soft Delete' pattern.
     * Required for remote synchronization to notify the server of deletions.
     */
    val isDeleted: Boolean

    /**
     * Unix timestamp of the soft deletion. Used for TTL (Time-To-Live) cleanup and audit trails.
     */
    val deletedAt: Long

    /**
     * Current synchronization state (e.g., SYNCED, PENDING_INSERT, PENDING_UPDATE).
     * Guides the Sync Engine on how to handle the record during network availability.
     * 0: SYNCED, 1: PENDING_INSERT, 2: PENDING_UPDATE, 3: PENDING_DELETE, 4: FAILED
     */
    val syncStatus: Int

    /**
     * A cryptographic hash of the record's payload.
     * Used to detect meaningful data changes and avoid redundant I/O or network calls.
     */
    val contentHash: String

    /**
     * The authoritative version index provided by the remote server.
     * Used to resolve conflicts when the server state is ahead of the local state.
     */
    val serverVersion: Int
}