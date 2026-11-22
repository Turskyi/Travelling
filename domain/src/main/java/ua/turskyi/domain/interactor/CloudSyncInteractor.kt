package ua.turskyi.domain.interactor

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.domain.repository.CloudSyncRepository

@Suppress("unused")
class CloudSyncInteractor : KoinComponent {
    private val cloudSyncRepository: CloudSyncRepository by inject()

    suspend fun performPreflightCheck() = cloudSyncRepository.performPreflightCheck()

    suspend fun authorizeMigration() = cloudSyncRepository.authorizeMigration()

    suspend fun uploadMigrationData(onProgress: (Int, Int) -> Unit) =
        cloudSyncRepository.uploadMigrationData(onProgress)

    suspend fun confirmMigration(confirmed: Boolean) =
        cloudSyncRepository.confirmMigration(confirmed)

    suspend fun discardStagedMigration() = cloudSyncRepository.discardStagedMigration()

    suspend fun performBidirectionalSync() = cloudSyncRepository.performBidirectionalSync()

    suspend fun restoreDataFromCloud() = cloudSyncRepository.restoreDataFromCloud()
}
