package ua.turskyi.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.data.BuildConfig
import ua.turskyi.data.datasources.database.datasource.DatabaseSource
import ua.turskyi.data.entities.local.CityEntity
import ua.turskyi.data.entities.local.CountryEntity
import ua.turskyi.domain.model.PreflightSummaryModel
import ua.turskyi.domain.repository.AuthRepository
import ua.turskyi.domain.repository.CloudSyncRepository
import java.io.File

class CloudSyncRepositoryImpl(
    private val context: Context,
) : CloudSyncRepository, KoinComponent {

    companion object {
        private const val TAG = "CloudSyncRepository"
    }

    private val databaseSource: DatabaseSource by inject()
    private val authRepository: AuthRepository by inject()
    private val okHttpClient: OkHttpClient by inject()
    private val firebaseStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val backendBaseUrl: String
        get() {
            val url = BuildConfig.BACKEND_BASE_URL
            return if (url.endsWith("/")) url.dropLast(1) else url
        }

    override suspend fun performPreflightCheck(): Result<PreflightSummaryModel> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val visitedCountries = databaseSource.getVisitedLocalCountriesFromDb()
                val cities = databaseSource.getCities()

                var availableSelfies = 0
                var missingSelfies = 0
                val missingSelfiesMap = mutableMapOf<String, String>()

                visitedCountries.forEach { country ->
                    if (country.selfie.isNotEmpty() && !country.selfie.startsWith("https://")) {
                        val isAvailable = if (country.selfie.startsWith("content://")) {
                            try {
                                val inputStream = context.contentResolver.openInputStream(Uri.parse(country.selfie))
                                val size = inputStream?.available() ?: 0
                                inputStream?.close()
                                size > 0
                            } catch (e: Exception) {
                                Log.e(TAG, "Error checking selfie content URI", e)
                                false
                            }
                        } else {
                            val path = if (country.selfie.startsWith("file://")) {
                                country.selfie.removePrefix("file://")
                            } else {
                                country.selfie
                            }
                            val file = File(path)
                            file.exists() && file.length() > 0L
                        }

                        if (isAvailable) {
                            availableSelfies++
                        } else {
                            missingSelfies++
                            missingSelfiesMap[country.name] = country.flag
                        }
                    } else if (country.selfie.startsWith("https://")) {
                        availableSelfies++
                    }
                }

                Result.success(
                    PreflightSummaryModel(
                        countryCount = visitedCountries.size,
                        cityCount = cities.size,
                        availableSelfieCount = availableSelfies,
                        missingSelfieCount = missingSelfies,
                        missingSelfiesByCountry = missingSelfiesMap,
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "performPreflightCheck failed", e)
                Result.failure(e)
            }
        }

    override suspend fun authorizeMigration(): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val idToken = authRepository.getIdToken().getOrThrow()
            val request = Request.Builder()
                .url("$backendBaseUrl/api/sync/authorize-migration")
                .addHeader("Authorization", "Bearer $idToken")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body.string()

            if (response.code == 403) {
                Result.success(false)
            } else if (response.isSuccessful) {
                val json = JSONObject(bodyString)
                val allowed = json.optBoolean("allowed", false)
                Result.success(allowed)
            } else {
                Result.failure(Exception("Migration authorization failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "authorizeMigration failed", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadMigrationData(onProgress: (Int, Int) -> Unit): Result<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val idToken = authRepository.getIdToken().getOrThrow()
                val user = authRepository.getCurrentUser()
                    ?: throw Exception("User is not signed in")

                val visitedCountries = databaseSource.getVisitedLocalCountriesFromDb()
                val cities = databaseSource.getCities()

                val totalSteps = visitedCountries.size + cities.size
                var completedSteps = 0

                val countriesArray = JSONArray()
                visitedCountries.forEach { c ->
                    var selfieUrl = c.selfie
                    if (c.selfie.isNotEmpty() && !c.selfie.startsWith("https://")) {
                        try {
                            val storageRef = firebaseStorage.reference.child(
                                "selfies/${user.uid}_${c.id}.jpg"
                            )
                            if (c.selfie.startsWith("content://")) {
                                val inputStream = context.contentResolver.openInputStream(Uri.parse(c.selfie))
                                if (inputStream != null) {
                                    storageRef.putStream(inputStream).await()
                                    inputStream.close()
                                }
                            } else {
                                val path = if (c.selfie.startsWith("file://")) {
                                    c.selfie.removePrefix("file://")
                                } else {
                                    c.selfie
                                }
                                val file = File(path)
                                if (file.exists() && file.length() > 0L) {
                                    storageRef.putFile(Uri.fromFile(file)).await()
                                }
                            }
                            selfieUrl = storageRef.downloadUrl.await().toString()
                        } catch (e: Exception) {
                            Log.e(TAG, "Optional selfie upload failed for ${c.name}, continuing sync", e)
                            selfieUrl = ""
                        }
                    }

                    val countryObj = JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("flag", c.flag)
                        put("isVisited", c.isVisited)
                        put("selfie", selfieUrl)
                    }
                    countriesArray.put(countryObj)
                    completedSteps++
                    onProgress(completedSteps, totalSteps)
                }

                val citiesArray = JSONArray()
                cities.forEach { city ->
                    val cityObj = JSONObject().apply {
                        put("id", city.id)
                        put("name", city.name)
                        put("parentId", city.parentId)
                        put("month", city.month)
                    }
                    citiesArray.put(cityObj)
                    completedSteps++
                    onProgress(completedSteps, totalSteps)
                }

                val payload = JSONObject().apply {
                    put("countries", countriesArray)
                    put("cities", citiesArray)
                }

                val request = Request.Builder()
                    .url("$backendBaseUrl/api/sync/data")
                    .addHeader("Authorization", "Bearer $idToken")
                    .post(payload.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body.string()
                if (!response.isSuccessful) {
                    val errorMsg = try {
                        JSONObject(responseBody).optString("error", "HTTP ${response.code}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing error response body", e)
                        "HTTP ${response.code}"
                    }
                    throw Exception(errorMsg)
                }

                val resJson = JSONObject(responseBody)
                val serverCountryCount = resJson.optInt("serverCountryCount", visitedCountries.size)
                if (serverCountryCount != visitedCountries.size) {
                    throw Exception("Server verification count mismatch: expected ${visitedCountries.size}, got $serverCountryCount")
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "uploadMigrationData failed", e)
                Result.failure(e)
            }
        }

    override suspend fun confirmMigration(confirmed: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val idToken = authRepository.getIdToken().getOrThrow()
                val endpoint = if (confirmed) "authorize-migration" else "reject-migration"
                val request = Request.Builder()
                    .url("$backendBaseUrl/api/sync/$endpoint")
                    .addHeader("Authorization", "Bearer $idToken")
                    .post("{}".toRequestBody("application/json".toMediaType()))
                    .build()
                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}"))
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "confirmMigration failed", e)
                Result.failure(e)
            }
        }

    override suspend fun discardStagedMigration(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val idToken = authRepository.getIdToken().getOrThrow()
            val request = Request.Builder()
                .url("$backendBaseUrl/api/sync/discard-staged")
                .addHeader("Authorization", "Bearer $idToken")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()
            okHttpClient.newCall(request).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "discardStagedMigration failed", e)
            Result.failure(e)
        }
    }

    override suspend fun performBidirectionalSync(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val idToken = authRepository.getIdToken().getOrThrow()

            // 1. Fetch cloud data
            val getRequest = Request.Builder()
                .url("$backendBaseUrl/api/sync/data")
                .addHeader("Authorization", "Bearer $idToken")
                .get()
                .build()

            val response = okHttpClient.newCall(getRequest).execute()
            val responseBody = response.body.string()
            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optString("error", "HTTP ${response.code}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing sync response body", e)
                    "HTTP ${response.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            // 2. Merge cloud data into local database (selfie is already HTTPS download URL compatible with TravellingPro)
            val json = JSONObject(responseBody)
            val serverCountries = json.optJSONArray("countries") ?: JSONArray()
            for (i in 0 until serverCountries.length()) {
                val cObj = serverCountries.getJSONObject(i)
                val selfieUrl = cObj.optString("selfie", "")

                val country = CountryEntity(
                    id = cObj.optInt("id", 0),
                    name = cObj.optString("name", cObj.optString("title", "")),
                    flag = cObj.optString("flag", ""),
                    isVisited = cObj.optBoolean("isVisited", true),
                    selfie = selfieUrl,
                )
                databaseSource.insertCountry(country)
            }

            val serverCities = json.optJSONArray("cities") ?: JSONArray()
            for (i in 0 until serverCities.length()) {
                val cObj = serverCities.getJSONObject(i)
                val city = CityEntity(
                    id = cObj.optInt("id", 0),
                    name = cObj.optString("name", ""),
                    parentId = cObj.optInt("parentId", 0),
                    month = cObj.optString("month", ""),
                )
                databaseSource.insertCity(city)
            }

            // 3. Upload pre-existing local data back to cloud so both devices & cloud match 100%
            syncLocalChanges()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "performBidirectionalSync failed", e)
            Result.failure(e)
        }
    }

    override suspend fun restoreDataFromCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext performBidirectionalSync()
    }

    override suspend fun syncLocalChanges(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val idToken = authRepository.getIdToken().getOrNull() ?: return@withContext Result.failure(Exception("Not signed in"))
            val user = authRepository.getCurrentUser() ?: return@withContext Result.failure(Exception("Not signed in"))
            val visitedCountries = databaseSource.getVisitedLocalCountriesFromDb()
            val cities = databaseSource.getCities()

            val countriesArray = JSONArray()
            visitedCountries.forEach { c ->
                var selfieUrl = c.selfie
                if (c.selfie.isNotEmpty() && !c.selfie.startsWith("https://")) {
                    try {
                        val storageRef = firebaseStorage.reference.child(
                            "selfies/${user.uid}_${c.id}.jpg"
                        )
                        if (c.selfie.startsWith("content://")) {
                            val inputStream = context.contentResolver.openInputStream(Uri.parse(c.selfie))
                            if (inputStream != null) {
                                storageRef.putStream(inputStream).await()
                                inputStream.close()
                            }
                        } else {
                            val path = if (c.selfie.startsWith("file://")) {
                                c.selfie.removePrefix("file://")
                            } else {
                                c.selfie
                            }
                            val file = File(path)
                            if (file.exists() && file.length() > 0L) {
                                storageRef.putFile(Uri.fromFile(file)).await()
                            }
                        }
                        selfieUrl = storageRef.downloadUrl.await().toString()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to upload selfie during syncLocalChanges for country ${c.name}", e)
                        selfieUrl = ""
                    }
                }

                val countryObj = JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("flag", c.flag)
                    put("isVisited", c.isVisited)
                    put("selfie", selfieUrl)
                }
                countriesArray.put(countryObj)
            }

            val citiesArray = JSONArray()
            cities.forEach { city ->
                val cityObj = JSONObject().apply {
                    put("id", city.id)
                    put("name", city.name)
                    put("parentId", city.parentId)
                    put("month", city.month)
                }
                citiesArray.put(cityObj)
            }

            val payload = JSONObject().apply {
                put("countries", countriesArray)
                put("cities", citiesArray)
            }

            val request = Request.Builder()
                .url("$backendBaseUrl/api/sync/data")
                .addHeader("Authorization", "Bearer $idToken")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body.string()
            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optString("error", "HTTP ${response.code}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing response error body", e)
                    "HTTP ${response.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "syncLocalChanges failed", e)
            Result.failure(e)
        }
    }
}
