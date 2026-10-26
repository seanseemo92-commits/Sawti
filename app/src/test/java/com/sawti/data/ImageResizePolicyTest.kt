package com.sawti.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageResizePolicyTest {
    @Test fun landscapeIsProportionallyResized() {
        assertEquals(1024 to 768, ImageResizePolicy.outputSize(4000, 3000))
    }

    @Test fun portraitAndPanoramaAreBounded() {
        assertEquals(512 to 1024, ImageResizePolicy.outputSize(800, 1600))
        assertEquals(1024 to 256, ImageResizePolicy.outputSize(4096, 1024))
    }

    @Test fun tinyImagesAreNotUpscaled() {
        assertEquals(320 to 240, ImageResizePolicy.outputSize(320, 240))
        assertEquals(1 to 1024, ImageResizePolicy.outputSize(1, 32768))
    }

    @Test fun sampleSizeLimitsDecoderAllocation() {
        assertEquals(1, ImageResizePolicy.sampleSize(1024, 768))
        assertEquals(2, ImageResizePolicy.sampleSize(4000, 3000))
        assertEquals(4, ImageResizePolicy.sampleSize(8000, 6000))
    }

    @Test fun rejectsInvalidOrExtremeDimensions() {
        for ((w, h) in listOf(0 to 10, 100 to -1, 32769 to 100, 15000 to 15000)) {
            assertThrows(IllegalArgumentException::class.java) {
                ImageResizePolicy.sampleSize(w, h)
            }
        }
        assertTrue(ImageResizePolicy.MAX_EDGE <= 1024)
    }
}
