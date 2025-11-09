package at.isg.eloquia.kmpapp.screens.detail

import androidx.lifecycle.ViewModel
import at.isg.eloquia.kmpapp.data.MuseumObject
import at.isg.eloquia.kmpapp.data.MuseumRepository
import kotlinx.coroutines.flow.Flow

class DetailViewModel(private val museumRepository: MuseumRepository) : ViewModel() {
    fun getObject(objectId: Int): Flow<MuseumObject?> =
        museumRepository.getObjectById(objectId)
}
