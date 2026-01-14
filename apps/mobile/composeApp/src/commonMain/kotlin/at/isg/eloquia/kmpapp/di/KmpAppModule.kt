package at.isg.eloquia.kmpapp.di

import at.isg.eloquia.kmpapp.presentation.components.AddConnectionViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val kmpAppModule: Module = module {
    viewModelOf(::AddConnectionViewModel)
}
