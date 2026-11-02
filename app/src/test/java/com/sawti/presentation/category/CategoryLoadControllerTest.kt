package com.sawti.presentation.category

import com.sawti.domain.model.Category
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.*
import org.junit.Test

class CategoryLoadControllerTest {
    private val categories = listOf(Category("daily", "قسم", 0, false, emptyList()))

    @Test fun successfulLoad() = runBlocking {
        var seeds = 0
        val loader = CategoryLoadController(this, { seeds++ }, { flowOf(categories) })
        loader.start()
        yield()
        assertEquals(1, seeds)
        assertFalse(loader.state.value.loading)
        assertNull(loader.state.value.error)
        assertEquals(categories, loader.state.value.categories)
    }

    @Test fun seedFailureThenRetrySucceeds() = runBlocking {
        var seeds = 0
        val loader = CategoryLoadController(this, {
            seeds++
            if (seeds == 1) error("seed failed")
        }, { flowOf(categories) })
        loader.start()
        yield()
        assertNotNull(loader.state.value.error)
        assertTrue(loader.retry())
        yield()
        assertEquals(2, seeds)
        assertEquals(categories, loader.state.value.categories)
        assertNull(loader.state.value.error)
    }

    @Test fun observationFailureResubscribesOnRetry() = runBlocking {
        var subscriptions = 0
        val loader = CategoryLoadController(this, {}, {
            subscriptions++
            if (subscriptions == 1) flow { throw IllegalStateException("query failed") }
            else flowOf(categories)
        })
        loader.start()
        yield()
        assertNotNull(loader.state.value.error)
        assertTrue(loader.retry())
        yield()
        assertEquals(2, subscriptions)
        assertEquals(categories, loader.state.value.categories)
    }

    @Test fun rapidRetryCannotStartParallelLoad() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        var seeds = 0
        val loader = CategoryLoadController(this, { seeds++; gate.await() }, { flowOf(categories) })
        loader.start()
        yield()
        repeat(10) { assertFalse(loader.retry()) }
        assertEquals(1, seeds)
        gate.complete(Unit)
        yield()
        assertFalse(loader.state.value.loading)
    }

    @Test fun repeatedFailureRemainsRetryable() = runBlocking {
        var attempts = 0
        val loader = CategoryLoadController(this, { attempts++; error("failed") }, { flowOf(categories) })
        loader.start()
        yield()
        assertTrue(loader.retry())
        yield()
        assertEquals(2, attempts)
        assertFalse(loader.state.value.loading)
        assertNotNull(loader.state.value.error)
    }

    @Test fun emptyCategoriesAreNotAnEndlessSpinner() = runBlocking {
        val loader = CategoryLoadController(this, {}, { flowOf(emptyList()) })
        loader.start()
        yield()
        assertFalse(loader.state.value.loading)
        assertNotNull(loader.state.value.error)
        assertTrue(loader.retry())
        yield()
    }

    @Test fun flowWithoutEmissionReportsFailure() = runBlocking {
        val loader = CategoryLoadController(this, {}, { emptyFlow() })
        loader.start()
        yield()
        assertFalse(loader.state.value.loading)
        assertNotNull(loader.state.value.error)
    }

    @Test fun laterFailureRetainsLoadedCategories() = runBlocking {
        val loader = CategoryLoadController(this, {}, {
            flow { emit(categories); error("later query failed") }
        })
        loader.start()
        yield()
        assertEquals(categories, loader.state.value.categories)
        assertNotNull(loader.state.value.error)
    }
}
