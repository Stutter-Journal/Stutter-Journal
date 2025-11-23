package at.isg.eloquia.core.domain.entries.di

import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.ObserveJournalEntriesUseCase
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Clock.*

val entriesDomainModule: Module = module {
    single { System }
    factory { ObserveJournalEntriesUseCase(get()) }
    factory { CreateJournalEntryUseCase(repository = get(), clock = get()) }
}
