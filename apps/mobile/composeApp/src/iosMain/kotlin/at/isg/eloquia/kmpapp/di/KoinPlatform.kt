package at.isg.eloquia.kmpapp.di

import at.isg.eloquia.core.data.entries.di.entriesDataModule
import org.koin.core.module.Module

actual fun platformModules(): List<Module> = listOf(entriesDataModule)
