/*
 * Paintroid: An image manipulation application for Android.
 *  Copyright (C) 2010-2022 The Catrobat Team
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
package org.catrobat.paintroid.test.espresso.dialog

/*
 * Paintroid: An image manipulation application for Android.
 *  Copyright (C) 2010-2022 The Catrobat Team
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
import android.graphics.Paint
import android.graphics.Paint.Cap
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.catrobat.paintroid.MainActivity
import org.catrobat.paintroid.R
import org.catrobat.paintroid.test.espresso.util.EspressoUtils.DEFAULT_STROKE_WIDTH
import org.catrobat.paintroid.test.espresso.util.UiInteractions
import org.catrobat.paintroid.test.espresso.util.UiInteractions.setProgress
import org.catrobat.paintroid.test.espresso.util.UiMatcher.withProgress
import org.catrobat.paintroid.test.espresso.util.wrappers.ToolBarViewInteraction.onToolBarView
import org.catrobat.paintroid.test.espresso.util.wrappers.TopBarViewInteraction.onTopBarView
import org.catrobat.paintroid.test.utils.ScreenshotOnFailRule
import org.catrobat.paintroid.tools.ToolType
import org.hamcrest.core.IsNot
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrushPickerIntegrationTest {
    @get:Rule
    var launchActivityRule = ActivityTestRule(
        MainActivity::class.java
    )
    @get:Rule
    var screenshotOnFailRule = ScreenshotOnFailRule()
    @Before
    fun setUp() = onToolBarView().performSelectTool(ToolType.BRUSH)
    private fun getCurrentToolBitmapPaint(): Paint = launchActivityRule.activity.toolPaint.paint
    private fun getCurrentToolCanvasPaint(): Paint = launchActivityRule.activity.toolPaint.previewPaint

    private fun assertStrokePaint(strokePaint: Paint, expectedStrokeWidth: Int, expectedCap: Cap) {
        val paintStrokeWidth = strokePaint.strokeWidth.toInt()
        val paintCap = strokePaint.strokeCap
        assertEquals(
            "Stroke did not change",
            expectedStrokeWidth.toLong(),
            paintStrokeWidth.toLong()
        )
        assertEquals("Stroke cap not $expectedCap", expectedCap, paintCap)
    }
    private fun setStrokeWidth(strokeWidth: Int, expectedStrokeWidth: Int) =onView(withId(R.id.pocketpaint_stroke_width_seek_bar))
        .perform(setProgress(strokeWidth))
        .check(matches(withProgress(expectedStrokeWidth)))

    private fun setStrokeWidth(strokeWidth: Int) = setStrokeWidth(strokeWidth, strokeWidth)

    @Test
    fun brushPickerDialogDefaultLayoutAndToolChanges() {
        onView(withId(R.id.pocketpaint_brush_tool_preview))
            .check(matches(isDisplayed()))
        onView(withId(R.id.pocketpaint_stroke_width_seek_bar))
            .check(matches(isDisplayed()))
            .check(matches(withProgress(DEFAULT_STROKE_WIDTH)))
        onView(withId(R.id.pocketpaint_stroke_width_width_text))
            .check(matches(isDisplayed()))
            .check(
                matches(
                    withText(
                        Integer.toString(
                            DEFAULT_STROKE_WIDTH
                        )
                    )
                )
            )
        onView(withId(R.id.pocketpaint_stroke_ibtn_rect))
            .check(matches(isDisplayed()))
            .check(matches(IsNot.not(isChecked())))
        onView(withId(R.id.pocketpaint_stroke_ibtn_circle))
            .check(matches(isDisplayed()))
            .check(matches(isChecked()))
        setStrokeWidth(MIN_STROKE_WIDTH)
        setStrokeWidth(MIDDLE_STROKE_WIDTH)
        setStrokeWidth(MAX_STROKE_WIDTH)
        assertStrokePaint(this.getCurrentToolCanvasPaint(), MAX_STROKE_WIDTH, Cap.ROUND)
        onView(withId(R.id.pocketpaint_stroke_ibtn_rect))
            .perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.pocketpaint_stroke_ibtn_circle))
            .check(matches(IsNot.not(isChecked())))
        assertStrokePaint(this.getCurrentToolCanvasPaint(), MAX_STROKE_WIDTH, Cap.SQUARE)
        onToolBarView()
            .performCloseToolOptionsView()
        assertStrokePaint(this.getCurrentToolCanvasPaint(), MAX_STROKE_WIDTH, Cap.SQUARE)
    }

    @Test
    fun brushPickerDialogKeepStrokeOnToolChange() {
        val newStrokeWidth = 80
        setStrokeWidth(newStrokeWidth)
        onView(withId(R.id.pocketpaint_stroke_ibtn_rect))
            .perform(click())
        assertStrokePaint(this.getCurrentToolCanvasPaint(), newStrokeWidth, Cap.SQUARE)
        onToolBarView()
            .performCloseToolOptionsView()
            .performSelectTool(ToolType.CURSOR)
        onView(withId(R.id.pocketpaint_stroke_width_seek_bar))
            .check(matches(withProgress(newStrokeWidth)))
        assertStrokePaint(this.getCurrentToolCanvasPaint(), newStrokeWidth, Cap.SQUARE)
        onToolBarView()
            .performCloseToolOptionsView()
    }

    @Test
    fun brushPickerDialogMinimumBrushWidth() {
        setStrokeWidth(0, MIN_STROKE_WIDTH)
        setStrokeWidth(MIN_STROKE_WIDTH)
        onToolBarView()
            .performCloseToolOptionsView()
    }

    @Test
    fun brushPickerAntiAliasingOffAtMinimumBrushSize() {
        onView(withId(R.id.pocketpaint_stroke_width_seek_bar))
            .perform(UiInteractions.touchCenterLeft())
        onToolBarView()
            .performCloseToolOptionsView()
        val bitmapPaint: Paint = this.getCurrentToolBitmapPaint()
        val canvasPaint: Paint = this.getCurrentToolCanvasPaint()
        assertFalse("BITMAP_PAINT antialiasing should be off", bitmapPaint.isAntiAlias)
        assertFalse("CANVAS_PAINT antialiasing should be off", canvasPaint.isAntiAlias)
    }

    @Test
    fun setAntiAliasingNotOnWhenCancelPressed() {
        onTopBarView()
            .performOpenMoreOptions()
        onView(withText(R.string.menu_advanced))
            .perform(click())
        onView(withId(R.id.pocketpaint_antialiasing))
            .perform(click())
        onView(withText(R.string.cancel_button_text))
            .perform(click())
        val bitmapPaint: Paint = this.getCurrentToolBitmapPaint()
        val canvasPaint: Paint = this.getCurrentToolCanvasPaint()
        Assert.assertTrue("BITMAP_PAINT antialiasing should be on", bitmapPaint.isAntiAlias)
        Assert.assertTrue("CANVAS_PAINT antialiasing should be on", canvasPaint.isAntiAlias)
    }

    @Test
    fun setAntiAliasingOffWhenAdvancedSettingsTurnOffAndOn() {
        onTopBarView()
            .performOpenMoreOptions()
        onView(withText(R.string.menu_advanced))
            .check(matches(isDisplayed()))
        onView(withText(R.string.menu_advanced))
            .perform(click())
        onView(withId(R.id.pocketpaint_antialiasing))
            .perform(click())
        onView(withText(R.string.pocketpaint_ok))
            .perform(click())
        var bitmapPaint: Paint = this.getCurrentToolBitmapPaint()
        var canvasPaint: Paint = this.getCurrentToolCanvasPaint()
        Assert.assertFalse("BITMAP_PAINT antialiasing should be off", bitmapPaint.isAntiAlias)
        Assert.assertFalse("CANVAS_PAINT antialiasing should be off", canvasPaint.isAntiAlias)
        onTopBarView()
            .performOpenMoreOptions()
        onView(withText(R.string.menu_advanced))
            .perform(click())
        onView(withId(R.id.pocketpaint_antialiasing))
            .perform(click())
        onView(withText(R.string.pocketpaint_ok))
            .perform(click())
        bitmapPaint = this.getCurrentToolBitmapPaint()
        canvasPaint = this.getCurrentToolCanvasPaint()
        assertTrue("BITMAP_PAINT antialiasing should be on", bitmapPaint.isAntiAlias)
        assertTrue("CANVAS_PAINT antialiasing should be on", canvasPaint.isAntiAlias)
    }

    @Test
    fun brushPickerDialogRadioButtonsBehaviour() {
        onView(withId(R.id.pocketpaint_stroke_ibtn_rect))
            .check(matches(IsNot.not(isChecked())))
        onView(withId(R.id.pocketpaint_stroke_ibtn_circle))
            .check(matches(isChecked()))
        onView(withId(R.id.pocketpaint_stroke_ibtn_rect))
            .perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.pocketpaint_stroke_ibtn_circle))
            .check(matches(IsNot.not(isChecked())))
        onToolBarView()
            .performCloseToolOptionsView()
        assertStrokePaint(this.getCurrentToolCanvasPaint(), DEFAULT_STROKE_WIDTH, Cap.SQUARE)
        onToolBarView()
            .performOpenToolOptionsView()
        onView(withId(R.id.pocketpaint_stroke_ibtn_circle))
            .perform(click())
            .check(matches(isChecked()))
        onView(withId(R.id.pocketpaint_stroke_ibtn_rect))
            .check(matches(IsNot.not(isChecked())))
        assertStrokePaint(this.getCurrentToolCanvasPaint(), DEFAULT_STROKE_WIDTH, Cap.ROUND)
        onToolBarView()
            .performCloseToolOptionsView()
        assertStrokePaint(this.getCurrentToolCanvasPaint(), DEFAULT_STROKE_WIDTH, Cap.ROUND)
    }

    @Test
    fun brushPickerDialogEditTextBehaviour() {
        onView(withId(R.id.pocketpaint_stroke_width_width_text))
            .perform(replaceText(MIDDLE_STROKE_WIDTH.toString()))
        closeSoftKeyboard()
        onView(withId(R.id.pocketpaint_stroke_width_width_text))
            .check(matches(withText(MIDDLE_STROKE_WIDTH.toString())))
        onView(withId(R.id.pocketpaint_stroke_width_seek_bar))
            .check(matches(withProgress(MIDDLE_STROKE_WIDTH)))
    }

    companion object {
        private const val MIN_STROKE_WIDTH = 1
        private const val MIDDLE_STROKE_WIDTH = 50
        private const val MAX_STROKE_WIDTH = 100
    }
}
