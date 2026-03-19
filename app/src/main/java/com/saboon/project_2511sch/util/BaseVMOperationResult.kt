package com.saboon.project_2511sch.util

data class BaseVMOperationResult<T>(
    val data: T,
    val operationType: OperationType
)

enum class OperationType{
    INSERT, UPDATE, DELETE
}