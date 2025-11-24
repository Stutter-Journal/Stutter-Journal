package at.isg.eloquia.core.domain.entries.di

import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.DeleteJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.GetJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.ObserveJournalEntriesUseCase
import at.isg.eloquia.core.domain.entries.usecase.UpdateJournalEntryUseCase
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.Clock.System

val entriesDomainModule: Module = module {
    single<Clock> { System }
    factory { ObserveJournalEntriesUseCase(get()) }
    factory { CreateJournalEntryUseCase(repository = get(), clock = get()) }
    factory { UpdateJournalEntryUseCase(repository = get(), clock = get()) }
    factory { DeleteJournalEntryUseCase(get()) }
    factory { GetJournalEntryUseCase(get()) }
}
