/*
 * Paintroid: An image manipulation application for Android.
 * Copyright (C) 2010-2015 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.paintroid.test.junit.command

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.catrobat.paintroid.command.Command
import org.catrobat.paintroid.command.implementation.FillCommand
import org.catrobat.paintroid.contract.LayerContracts
import org.catrobat.paintroid.model.Layer
import org.catrobat.paintroid.model.LayerModel
import org.catrobat.paintroid.tools.helper.JavaFillAlgorithmFactory
import org.catrobat.paintroid.tools.implementation.MAX_ABSOLUTE_TOLERANCE
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.LinkedList
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FillCommandTest {
    private var layerModel: LayerContracts.Model? = null
    private lateinit var bitmapUnderTest: Bitmap
    @Before
    fun setUp() {
        bitmapUnderTest =
            Bitmap.createBitmap(INITIAL_WIDTH, INITIAL_HEIGHT, Bitmap.Config.ARGB_8888)
        layerModel = LayerModel()
        val layer = Layer(bitmapUnderTest)
        (layerModel as LayerModel).addLayerAt(0, layer)
        (layerModel as LayerModel).currentLayer = layer
    }

    @Test
    fun testFillingOnEmptyBitmap() {
        val width = 10
        val height = 20
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        layerModel!!.currentLayer!!.bitmap = bitmap
        val clickedPixel = Point(width / 2, height / 2)
        val targetColor = Color.BLACK
        val paint = Paint()
        paint.color = targetColor
        val fillCommand = FillCommand(JavaFillAlgorithmFactory(), clickedPixel, paint, NO_TOLERANCE)
        fillCommand.run(Canvas(), layerModel!!)
        val pixels = getPixelsFromBitmap(bitmap)
        Assert.assertEquals("Wrong array size", height.toLong(), pixels.size.toLong())
        Assert.assertEquals("Wrong array size", width.toLong(), pixels[0].size.toLong())
        for (row in 0 until height) {
            for (col in 0 until width) {
                Assert.assertEquals(
                    "Color should have been replaced",
                    targetColor.toLong(),
                    pixels[row][col].toLong()
                )
            }
        }
    }

    @Test
    fun testFillingOnNotEmptyBitmap() {
        val width = 6
        val height = 8
        val clickedPixel = Point(width / 2, height / 2)
        val targetColor = Color.GREEN
        val boundaryColor = Color.RED
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        layerModel!!.currentLayer!!.bitmap = bitmap
        val paint = Paint()
        paint.color = targetColor
        var pixels = getPixelsFromBitmap(bitmap)
        pixels[0][1] = boundaryColor
        pixels[1][0] = boundaryColor
        putPixelsToBitmap(bitmap, pixels)
        val fillCommand = FillCommand(JavaFillAlgorithmFactory(), clickedPixel, paint, NO_TOLERANCE)
        fillCommand.run(Canvas(), layerModel!!)
        pixels = getPixelsFromBitmap(bitmap)
        Assert.assertEquals(
            "Color of upper left pixel should not have been replaced",
            0,
            pixels[0][0].toLong()
        )
        Assert.assertEquals(
            "Boundary color should not have been replaced",
            boundaryColor.toLong(), pixels[0][1].toLong()
        )
        Assert.assertEquals(
            "Boundary color should not have been replaced",
            boundaryColor.toLong(), pixels[1][0].toLong()
        )
        Assert.assertEquals(
            "Pixel color should have been replaced",
            targetColor.toLong(), pixels[1][1].toLong()
        )
        for (row in 0 until height) {
            for (col in 0 until width) {
                if (row > 1 || col > 1) {
                    Assert.assertEquals(
                        "Pixel color should have been replaced",
                        targetColor.toLong(),
                        pixels[row][col].toLong()
                    )
                }
            }
        }
    }

    @Test
    fun testFillingWithMaxColorTolerance() {
        val width = 6
        val height = 8
        val clickedPixel = Point(width / 2, height / 2)
        val targetColor = Color.argb(0xFF, 0xFF, 0xFF, 0xFF)
        val replacementColor = 0
        val maxTolerancePerChannel = 0xFF
        val boundaryColor = Color.argb(
            maxTolerancePerChannel,
            maxTolerancePerChannel,
            maxTolerancePerChannel,
            maxTolerancePerChannel
        )
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        layerModel!!.currentLayer!!.bitmap = bitmap
        bitmap.eraseColor(replacementColor)
        val paint = Paint()
        paint.color = targetColor
        var pixels = getPixelsFromBitmap(bitmap)
        pixels[0][1] = boundaryColor
        pixels[1][0] = boundaryColor
        putPixelsToBitmap(bitmap, pixels)
        val fillCommand =
            FillCommand(JavaFillAlgorithmFactory(), clickedPixel, paint, MAX_TOLERANCE)
        fillCommand.run(Canvas(), layerModel!!)
        pixels = getPixelsFromBitmap(bitmap)
        for (row in 0 until height) {
            for (col in 0 until width) {
                Assert.assertEquals(
                    "Pixel color should have been replaced",
                    targetColor.toLong(),
                    pixels[row][col].toLong()
                )
            }
        }
    }

    @Test
    fun testFillingWhenOutOfTolerance() {
        val width = 6
        val height = 8
        val clickedPixel = Point(width / 2, height / 2)
        val targetColor = Color.argb(0xFF, 0xFF, 0xFF, 0xFF)
        val replacementColor = 0
        val maxTolerancePerChannel = 0xFF
        val boundaryColor = Color.argb(
            maxTolerancePerChannel,
            maxTolerancePerChannel,
            maxTolerancePerChannel,
            maxTolerancePerChannel
        )
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        layerModel!!.currentLayer!!.bitmap = bitmap
        bitmap.eraseColor(replacementColor)
        val paint = Paint()
        paint.color = targetColor
        var pixels = getPixelsFromBitmap(bitmap)
        pixels[0][1] = boundaryColor
        pixels[1][0] = boundaryColor
        putPixelsToBitmap(bitmap, pixels)
        val fillCommand =
            FillCommand(JavaFillAlgorithmFactory(), clickedPixel, paint, MAX_TOLERANCE - 1)
        fillCommand.run(Canvas(), layerModel!!)
        pixels = getPixelsFromBitmap(bitmap)
        for (row in 0 until height) {
            for (col in 0 until width) {
                if (row == 0 && col == 0) {
                    Assert.assertNotEquals(
                        "Pixel color should not have been replaced",
                        targetColor.toLong(),
                        pixels[row][col].toLong()
                    )
                    continue
                }
                Assert.assertEquals(
                    "Pixel color should have been replaced",
                    targetColor.toLong(),
                    pixels[row][col].toLong()
                )
            }
        }
    }

    @Test
    fun testEqualTargetAndReplacementColorWithTolerance() {
        val width = 8
        val height = 8
        val clickedPixel = Point(width / 2, height / 2)
        val boundaryPixel = Point(width / 4, height / 4)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        layerModel!!.currentLayer!!.bitmap = bitmap
        val targetColor = 0
        val boundaryColor = Color.argb(0xFF, 0xFF, 0xFF, 0xFF)
        bitmap.eraseColor(targetColor)
        val paint = Paint()
        paint.color = targetColor
        var pixels = getPixelsFromBitmap(bitmap)
        pixels[boundaryPixel.x][boundaryPixel.y] = boundaryColor
        putPixelsToBitmap(bitmap, pixels)
        val fillCommand =
            FillCommand(JavaFillAlgorithmFactory(), clickedPixel, paint, HALF_TOLERANCE)
        fillCommand.run(Canvas(), layerModel!!)
        pixels = getPixelsFromBitmap(bitmap)
        for (row in 0 until height) {
            for (col in 0 until width) {
                if (row == boundaryPixel.y && col == boundaryPixel.y) {
                    Assert.assertEquals(
                        "Pixel color should not have been replaced",
                        boundaryColor.toLong(),
                        pixels[row][col].toLong()
                    )
                    continue
                }
                Assert.assertEquals(
                    "Pixel color should have been replaced",
                    targetColor.toLong(),
                    pixels[row][col].toLong()
                )
            }
        }
    }

    @Test
    fun testFillingWhenTargetColorIsWithinTolerance() {
        val targetColor = -0x551156
        val boundaryColor = -0x10000
        val replacementColor = -0x1
        val height = 8
        val width = 8
        val topLeftQuarterPixel = Point(width / 4, height / 4)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        layerModel!!.currentLayer!!.bitmap = bitmap
        bitmap.eraseColor(replacementColor)
        val paint = Paint()
        paint.color = targetColor
        val pixels = getPixelsFromBitmap(bitmap)
        for (col in 0 until width) {
            pixels[height / 2][col] = targetColor
        }
        val boundaryPixel = Point(width / 2, height / 4)
        pixels[boundaryPixel.y][boundaryPixel.x] = boundaryColor
        putPixelsToBitmap(bitmap, pixels)
        val fillCommand =
            FillCommand(JavaFillAlgorithmFactory(), topLeftQuarterPixel, paint, HALF_TOLERANCE)
        fillCommand.run(Canvas(), layerModel!!)
        val actualPixels = getPixelsFromBitmap(bitmap)
        for (row in 0 until height) {
            for (col in 0 until width) {
                if (row == boundaryPixel.y && col == boundaryPixel.x) {
                    Assert.assertEquals(
                        "Wrong pixel color for boundary pixel",
                        boundaryColor.toLong(),
                        actualPixels[row][col].toLong()
                    )
                } else {
                    Assert.assertEquals(
                        "Wrong pixel color for pixel[$row][$col]",
                        targetColor.toLong(), actualPixels[row][col].toLong()
                    )
                }
            }
        }
    }

    @Test
    fun testFillingWithSpiral() {
        val targetColor = -0x551156
        val boundaryColor = -0x10000
        val replacementColor = -0x1
        val pixels = createPixelArrayAndDrawSpiral(replacementColor, boundaryColor)
        val height = pixels.size
        val width = pixels[0].size
        val clickedPixel = Point(1, 1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        layerModel!!.currentLayer!!.bitmap = bitmap
        bitmap.eraseColor(replacementColor)
        val paint = Paint()
        paint.color = targetColor
        putPixelsToBitmap(bitmap, pixels)
        val fillCommand =
            FillCommand(JavaFillAlgorithmFactory(), clickedPixel, paint, HALF_TOLERANCE)
        fillCommand.run(Canvas(), layerModel!!)
        val actualPixels = getPixelsFromBitmap(bitmap)
        val expectedPixels = createPixelArrayAndDrawSpiral(targetColor, boundaryColor)
        for (row in 0 until height) {
            for (col in 0 until width) {
                Assert.assertEquals(
                    "Wrong pixel color for pixels[$row][$col]",
                    expectedPixels[row][col].toLong(), actualPixels[row][col].toLong()
                )
            }
        }
    }

    @Test
    fun testComplexDrawing() {
        val targetColor = -0x551156
        val boundaryColor = -0x10000
        val replacementColor = -0x1
        val paint = Paint()
        paint.color = targetColor
        var pixels = createPixelArrayForComplexTest(replacementColor, boundaryColor)
        val height = pixels.size
        val width = pixels[0].size
        val clickedPixels = ArrayList<Point>()
        val topLeft = Point(0, 0)
        val topRight = Point(width - 1, 0)
        val bottomRight = Point(width - 1, height - 1)
        val bottomLeft = Point(0, height - 1)
        clickedPixels.add(topLeft)
        clickedPixels.add(topRight)
        clickedPixels.add(bottomRight)
        clickedPixels.add(bottomLeft)
        for (clickedPixel in clickedPixels) {
            pixels = createPixelArrayForComplexTest(replacementColor, boundaryColor)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            layerModel!!.currentLayer!!.bitmap = bitmap
            bitmap.eraseColor(replacementColor)
            putPixelsToBitmap(bitmap, pixels)
            val fillCommand =
                FillCommand(JavaFillAlgorithmFactory(), clickedPixel, paint, HALF_TOLERANCE)
            fillCommand.run(Canvas(), layerModel!!)
            val actualPixels = getPixelsFromBitmap(bitmap)
            val expectedPixels = createPixelArrayForComplexTest(targetColor, boundaryColor)
            for (row in pixels.indices) {
                for (col in pixels[0].indices) {
                    Assert.assertEquals(
                        "Wrong pixel color, clicked " + clickedPixel.x + "/" + clickedPixel.y,
                        expectedPixels[row][col].toLong(), actualPixels[row][col].toLong()
                    )
                }
            }
        }
    }

    @Test
    fun testSkipPixelsInCheckRangesFunction() {
        val targetColor = -0x551156
        val boundaryColor = -0x10000
        val replacementColor = -0x1
        val paint = Paint()
        paint.color = targetColor
        val clickedPixel = Point(0, 0)
        var pixels = createPixelArrayForSkipPixelTest(replacementColor, boundaryColor)
        val height = pixels.size
        val width = pixels[0].size
        pixels = createPixelArrayForSkipPixelTest(replacementColor, boundaryColor)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        layerModel!!.currentLayer!!.bitmap = bitmap
        bitmap.eraseColor(replacementColor)
        putPixelsToBitmap(bitmap, pixels)
        val fillCommand =
            FillCommand(JavaFillAlgorithmFactory(), clickedPixel, paint, HALF_TOLERANCE)
        fillCommand.run(Canvas(), layerModel!!)
        val actualPixels = getPixelsFromBitmap(bitmap)
        val expectedPixels = createPixelArrayForSkipPixelTest(targetColor, boundaryColor)
        for (row in 0 until height) {
            for (col in 0 until width) {
                Assert.assertEquals(
                    "Wrong pixel color",
                    expectedPixels[row][col].toLong(),
                    actualPixels[row][col].toLong()
                )
            }
        }
    }

    @Ignore("Flaky test, sometimes fails on Jenkins. runtime.gc() is not an assurance that memory will be freed might.")
    @Test
    fun testCommandsDoNotLeakMemory() {
        val commands: MutableList<Command> = LinkedList()
        val testBitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888)
        val clickedPixel = Point(10, 10)
        val paint = Paint()
        val canvas = Canvas()
        val layerModel = LayerModel()
        val testLayer = Layer(testBitmap)
        layerModel.addLayerAt(0, testLayer)
        layerModel.currentLayer = testLayer
        val runtime = Runtime.getRuntime()
        runtime.gc()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        for (i in 0..4) {
            val fillCommand = FillCommand(JavaFillAlgorithmFactory(), clickedPixel, paint, 0.5f)
            fillCommand.run(canvas, layerModel)
            commands.add(fillCommand)
        }
        runtime.gc()
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        assertThat(
            memoryAfter / 1024,
            Matchers.`is`(Matchers.lessThan(memoryBefore / 1024 + 10))
        )
    }

    private fun createPixelArrayForComplexTest(
        backgroundColor: Int,
        boundaryColor: Int
    ): Array<IntArray> {
        return arrayOf(
            intArrayOf(
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor
            ),
            intArrayOf(
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                boundaryColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                boundaryColor,
                boundaryColor,
                backgroundColor,
                backgroundColor
            ),
            intArrayOf(
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor
            ),
            intArrayOf(
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor
            ),
            intArrayOf(
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor
            ),
            intArrayOf(
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor
            ),
            intArrayOf(
                backgroundColor,
                backgroundColor,
                boundaryColor,
                boundaryColor,
                boundaryColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor
            ),
            intArrayOf(
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                boundaryColor,
                boundaryColor,
                boundaryColor,
                boundaryColor,
                backgroundColor
            ),
            intArrayOf(
                boundaryColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                boundaryColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor
            ),
            intArrayOf(
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                boundaryColor,
                boundaryColor,
                backgroundColor,
                backgroundColor
            ),
            intArrayOf(
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                backgroundColor
            )
        )
    }

    private fun createPixelArrayForSkipPixelTest(
        backgroundColor: Int,
        boundaryColor: Int
    ): Array<IntArray> {
        return arrayOf(
            intArrayOf(
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                boundaryColor
            ),
            intArrayOf(
                backgroundColor,
                backgroundColor,
                boundaryColor,
                backgroundColor,
                boundaryColor
            ),
            intArrayOf(
                backgroundColor,
                boundaryColor,
                backgroundColor,
                backgroundColor,
                boundaryColor
            ),
            intArrayOf(
                backgroundColor,
                backgroundColor,
                boundaryColor,
                boundaryColor,
                backgroundColor
            ),
            intArrayOf(
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor,
                backgroundColor
            )
        )
    }

    private fun createPixelArrayAndDrawSpiral(
        backgroundColor: Int,
        boundaryColor: Int
    ): Array<IntArray> {
        val width = 10
        val height = 10
        val pixels = Array(height) { IntArray(width) }
        for (x in 0 until width) {
            for (y in 0 until height) {
                pixels[y][x] = backgroundColor
            }
        }
        pixels[4][4] = boundaryColor
        pixels[5][4] = boundaryColor
        pixels[5][5] = boundaryColor
        pixels[4][6] = boundaryColor
        pixels[3][6] = boundaryColor
        pixels[2][5] = boundaryColor
        pixels[2][4] = boundaryColor
        pixels[2][3] = boundaryColor
        pixels[3][2] = boundaryColor
        pixels[4][2] = boundaryColor
        pixels[5][2] = boundaryColor
        pixels[6][2] = boundaryColor
        pixels[7][3] = boundaryColor
        pixels[7][4] = boundaryColor
        return pixels
    }

    private fun getPixelsFromBitmap(bitmap: Bitmap): Array<IntArray> {
        val pixels = Array(bitmap.height) { IntArray(bitmap.width) }
        for (i in 0 until bitmap.height) {
            bitmap.getPixels(pixels[i], 0, bitmap.width, 0, i, bitmap.width, 1)
        }
        return pixels
    }

    private fun putPixelsToBitmap(bitmap: Bitmap, pixels: Array<IntArray>) {
        Assert.assertEquals("Height is inconsistent", bitmap.height.toLong(), pixels.size.toLong())
        Assert.assertEquals("Width is inconsistent", bitmap.width.toLong(), pixels[0].size.toLong())
        for (i in 0 until bitmap.height) {
            bitmap.setPixels(pixels[i], 0, bitmap.width, 0, i, bitmap.width, 1)
        }
    }

    companion object {
        private const val NO_TOLERANCE = 0.0f
        private val HALF_TOLERANCE: Float = MAX_ABSOLUTE_TOLERANCE / 2.0f
        private val MAX_TOLERANCE: Float = MAX_ABSOLUTE_TOLERANCE.toFloat()
        private const val INITIAL_HEIGHT = 80
        private const val INITIAL_WIDTH = 80
    }
}