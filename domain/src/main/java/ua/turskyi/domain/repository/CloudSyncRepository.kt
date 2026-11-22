package ua.turskyi.domain.repository

import ua.turskyi.domain.model.PreflightSummaryModel

interface CloudSyncRepository {
    suspend fun performPreflightCheck(): Result<PreflightSummaryModel>
    suspend fun authorizeMigration(): Result<Boolean>
    suspend fun uploadMigrationData(onProgress: (Int, Int) -> Unit): Result<Unit>
    suspend fun confirmMigration(confirmed: Boolean): Result<Unit>
    suspend fun discardStagedMigration(): Result<Unit>
    suspend fun performBidirectionalSync(): Result<Unit>
    suspend fun restoreDataFromCloud(): Result<Unit>
    suspend fun syncLocalChanges(): Result<Unit>
}
