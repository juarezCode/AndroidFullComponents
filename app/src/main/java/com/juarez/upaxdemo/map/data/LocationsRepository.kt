package com.juarez.upaxdemo.map.data

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.toObjects
import com.juarez.upaxdemo.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface LocationsRepository {
    suspend fun saveLocation(location: Location)
    fun getLocations(): Flow<Resource<List<Location>>>
}

class LocationsRepositoryImp @Inject constructor(
    private val locationsCollection: CollectionReference,
) : LocationsRepository {

    override suspend fun saveLocation(location: Location) {
        locationsCollection.add(location).await()
    }

    override fun getLocations(): Flow<Resource<List<Location>>> = flow {
        emit(Resource.Loading(true))
        try {
            val snapshot = locationsCollection.get().await()
            val locations = snapshot.toObjects<Location>()
            emit(Resource.Success(locations))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected Error"))
        }
        emit(Resource.Loading(false))
    }

}