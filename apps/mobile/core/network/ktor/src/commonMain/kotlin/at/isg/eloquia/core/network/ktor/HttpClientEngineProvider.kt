package at.isg.eloquia.core.network.ktor

import io.ktor.client.engine.HttpClientEngine

internal expect fun defaultHttpClientEngine(): HttpClientEngine
