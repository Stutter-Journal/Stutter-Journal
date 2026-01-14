package at.isg.eloquia.features.therapist.di

import at.isg.eloquia.features.therapist.presentation.TherapistViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val therapistFeatureModule: Module = module {
    viewModelOf(::TherapistViewModel)
}
