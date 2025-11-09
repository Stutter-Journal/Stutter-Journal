package at.isg.eloquia.screens.detail

import androidx.lifecycle.ViewModel
import at.isg.eloquia.data.MuseumObject
import at.isg.eloquia.data.MuseumRepository
import kotlinx.coroutines.flow.Flow

class DetailViewModel(private val museumRepository: MuseumRepository) : ViewModel() {
    fun getObject(objectId: Int): Flow<MuseumObject?> =
        museumRepository.getObjectById(objectId)
}
