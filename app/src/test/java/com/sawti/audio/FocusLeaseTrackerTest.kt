package com.sawti.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusLeaseTrackerTest {
    @Test fun staleCompletionCannotClearNewFocusOwner() {
        val leases = FocusLeaseTracker()
        val first = leases.next()
        val second = leases.next()
        assertFalse(leases.owns(first))
        assertFalse(leases.clear(first))
        assertTrue(leases.owns(second))
        assertTrue(leases.clear(second))
        assertFalse(leases.owns(second))
    }

    @Test fun tokensAreUniqueAcrossRapidPlayStops() {
        val leases = FocusLeaseTracker()
        val a = leases.next()
        assertTrue(leases.clear(a))
        val b = leases.next()
        assertFalse(leases.owns(a))
        assertTrue(leases.owns(b))
    }
}
