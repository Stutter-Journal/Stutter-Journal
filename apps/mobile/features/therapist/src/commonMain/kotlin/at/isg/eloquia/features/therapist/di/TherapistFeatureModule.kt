package at.isg.eloquia.features.therapist.di

import at.isg.eloquia.features.therapist.data.api.TherapistApi
import at.isg.eloquia.features.therapist.data.api.TherapistApiImpl
import at.isg.eloquia.features.therapist.data.repository.TherapistRepositoryImpl
import at.isg.eloquia.features.therapist.domain.repository.TherapistRepository
import at.isg.eloquia.features.therapist.domain.usecase.GetMyTherapistUseCase
import at.isg.eloquia.features.therapist.presentation.MyTherapistViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val therapistFeatureModule: Module = module {
    // Data
    singleOf(::TherapistApiImpl) bind TherapistApi::class
    singleOf(::TherapistRepositoryImpl) bind TherapistRepository::class

    // Domain
    factoryOf(::GetMyTherapistUseCase)

    // Presentation
    viewModelOf(::MyTherapistViewModel)
}
