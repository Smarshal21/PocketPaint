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

import android.graphics.Paint
import android.graphics.Paint.Cap
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.catrobat.paintroid.MainActivity
import org.catrobat.paintroid.R
import org.catrobat.paintroid.test.espresso.util.UiInteractions
import org.catrobat.paintroid.test.espresso.util.UiMatcher
import org.catrobat.paintroid.test.espresso.util.wrappers.ToolBarViewInteraction
import org.catrobat.paintroid.test.espresso.util.wrappers.TopBarViewInteraction
import org.catrobat.paintroid.test.utils.ScreenshotOnFailRule
import org.catrobat.paintroid.tools.ToolType
import org.hamcrest.core.IsNot
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrushPickerIntegrationTest {
    @Rule
    var launchActivityRule = ActivityTestRule(
        MainActivity::class.java
    )

    @Rule
    var screenshotOnFailRule = ScreenshotOnFailRule()
    @Before
    fun setUp() {
        ToolBarViewInteraction.onToolBarView()
            .performSelectTool(ToolType.BRUSH)
    }

    private fun assertStrokePaint(strokePaint: Paint, expectedStrokeWidth: Int, expectedCap: Cap) {
        val paintStrokeWidth = strokePaint.strokeWidth.toInt()
        val paintCap = strokePaint.strokeCap
        Assert.assertEquals(
            "Stroke did not change",
            expectedStrokeWidth.toLong(),
            paintStrokeWidth.toLong()
        )
        Assert.assertEquals("Stroke cap not $expectedCap", expectedCap, paintCap)
    }

    private fun setStrokeWidth(strokeWidth: Int, expectedStrokeWidth: Int) {
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_width_seek_bar))
            .perform(UiInteractions.setProgress(strokeWidth))
            .check(ViewAssertions.matches(UiMatcher.withProgress(expectedStrokeWidth)))
    }

    private fun setStrokeWidth(strokeWidth: Int) {
        setStrokeWidth(strokeWidth, strokeWidth)
    }

    @Test
    fun brushPickerDialogDefaultLayoutAndToolChanges() {
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_brush_tool_preview))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_width_seek_bar))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(UiMatcher.withProgress(DEFAULT_STROKE_WIDTH)))
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_width_width_text))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withText(
                        Integer.toString(
                            DEFAULT_STROKE_WIDTH
                        )
                    )
                )
            )
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_ibtn_rect))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(IsNot.not(ViewMatchers.isChecked())))
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_ibtn_circle))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isChecked()))
        setStrokeWidth(MIN_STROKE_WIDTH)
        setStrokeWidth(MIDDLE_STROKE_WIDTH)
        setStrokeWidth(MAX_STROKE_WIDTH)
        assertStrokePaint(this.currentToolCanvasPaint, MAX_STROKE_WIDTH, Cap.ROUND)
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_ibtn_rect))
            .perform(ViewActions.click())
            .check(ViewAssertions.matches(ViewMatchers.isChecked()))
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_ibtn_circle))
            .check(ViewAssertions.matches(IsNot.not(ViewMatchers.isChecked())))
        assertStrokePaint(this.currentToolCanvasPaint, MAX_STROKE_WIDTH, Cap.SQUARE)
        ToolBarViewInteraction.onToolBarView()
            .performCloseToolOptionsView()
        assertStrokePaint(this.currentToolCanvasPaint, MAX_STROKE_WIDTH, Cap.SQUARE)
    }

    @Test
    fun brushPickerDialogKeepStrokeOnToolChange() {
        val newStrokeWidth = 80
        setStrokeWidth(newStrokeWidth)
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_ibtn_rect))
            .perform(ViewActions.click())
        assertStrokePaint(this.currentToolCanvasPaint, newStrokeWidth, Cap.SQUARE)
        ToolBarViewInteraction.onToolBarView()
            .performCloseToolOptionsView()
            .performSelectTool(ToolType.CURSOR)
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_width_seek_bar))
            .check(ViewAssertions.matches(UiMatcher.withProgress(newStrokeWidth)))
        assertStrokePaint(this.currentToolCanvasPaint, newStrokeWidth, Cap.SQUARE)
        ToolBarViewInteraction.onToolBarView()
            .performCloseToolOptionsView()
    }

    @Test
    fun brushPickerDialogMinimumBrushWidth() {
        setStrokeWidth(0, MIN_STROKE_WIDTH)
        setStrokeWidth(MIN_STROKE_WIDTH)
        ToolBarViewInteraction.onToolBarView()
            .performCloseToolOptionsView()
    }

    @Test
    fun brushPickerAntiAliasingOffAtMinimumBrushSize() {
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_width_seek_bar))
            .perform(UiInteractions.touchCenterLeft())
        ToolBarViewInteraction.onToolBarView()
            .performCloseToolOptionsView()
        val bitmapPaint: Paint = this.currentToolBitmapPaint
        val canvasPaint: Paint = this.currentToolCanvasPaint
        Assert.assertFalse("BITMAP_PAINT antialiasing should be off", bitmapPaint.isAntiAlias)
        Assert.assertFalse("CANVAS_PAINT antialiasing should be off", canvasPaint.isAntiAlias)
    }

    @Test
    fun setAntiAliasingNotOnWhenCancelPressed() {
        TopBarViewInteraction.onTopBarView()
            .performOpenMoreOptions()
        Espresso.onView(ViewMatchers.withText(R.string.menu_advanced))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_antialiasing))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.cancel_button_text))
            .perform(ViewActions.click())
        val bitmapPaint: Paint = this.currentToolBitmapPaint
        val canvasPaint: Paint = this.currentToolCanvasPaint
        Assert.assertTrue("BITMAP_PAINT antialiasing should be on", bitmapPaint.isAntiAlias)
        Assert.assertTrue("CANVAS_PAINT antialiasing should be on", canvasPaint.isAntiAlias)
    }

    @Test
    fun setAntiAliasingOffWhenAdvancedSettingsTurnOffAndOn() {
        TopBarViewInteraction.onTopBarView()
            .performOpenMoreOptions()
        Espresso.onView(ViewMatchers.withText(R.string.menu_advanced))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(R.string.menu_advanced))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_antialiasing))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.pocketpaint_ok))
            .perform(ViewActions.click())
        var bitmapPaint: Paint = this.currentToolBitmapPaint
        var canvasPaint: Paint = this.currentToolCanvasPaint
        Assert.assertFalse("BITMAP_PAINT antialiasing should be off", bitmapPaint.isAntiAlias)
        Assert.assertFalse("CANVAS_PAINT antialiasing should be off", canvasPaint.isAntiAlias)
        TopBarViewInteraction.onTopBarView()
            .performOpenMoreOptions()
        Espresso.onView(ViewMatchers.withText(R.string.menu_advanced))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_antialiasing))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.pocketpaint_ok))
            .perform(ViewActions.click())
        bitmapPaint = this.currentToolBitmapPaint
        canvasPaint = this.currentToolCanvasPaint
        Assert.assertTrue("BITMAP_PAINT antialiasing should be on", bitmapPaint.isAntiAlias)
        Assert.assertTrue("CANVAS_PAINT antialiasing should be on", canvasPaint.isAntiAlias)
    }

    @Test
    fun brushPickerDialogRadioButtonsBehaviour() {
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_ibtn_rect))
            .check(ViewAssertions.matches(IsNot.not(ViewMatchers.isChecked())))
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_ibtn_circle))
            .check(ViewAssertions.matches(ViewMatchers.isChecked()))
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_ibtn_rect))
            .perform(ViewActions.click())
            .check(ViewAssertions.matches(ViewMatchers.isChecked()))
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_ibtn_circle))
            .check(ViewAssertions.matches(IsNot.not(ViewMatchers.isChecked())))
        ToolBarViewInteraction.onToolBarView()
            .performCloseToolOptionsView()
        assertStrokePaint(this.currentToolCanvasPaint, DEFAULT_STROKE_WIDTH, Cap.SQUARE)
        ToolBarViewInteraction.onToolBarView()
            .performOpenToolOptionsView()
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_ibtn_circle))
            .perform(ViewActions.click())
            .check(ViewAssertions.matches(ViewMatchers.isChecked()))
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_ibtn_rect))
            .check(ViewAssertions.matches(IsNot.not(ViewMatchers.isChecked())))
        assertStrokePaint(this.currentToolCanvasPaint, DEFAULT_STROKE_WIDTH, Cap.ROUND)
        ToolBarViewInteraction.onToolBarView()
            .performCloseToolOptionsView()
        assertStrokePaint(this.currentToolCanvasPaint, DEFAULT_STROKE_WIDTH, Cap.ROUND)
    }

    @Test
    fun brushPickerDialogEditTextBehaviour() {
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_width_width_text))
            .perform(ViewActions.replaceText(MIDDLE_STROKE_WIDTH.toString()))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_width_width_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(MIDDLE_STROKE_WIDTH.toString())))
        Espresso.onView(ViewMatchers.withId(R.id.pocketpaint_stroke_width_seek_bar))
            .check(ViewAssertions.matches(UiMatcher.withProgress(MIDDLE_STROKE_WIDTH)))
    }

    companion object {
        private const val MIN_STROKE_WIDTH = 1
        private const val MIDDLE_STROKE_WIDTH = 50
        private const val MAX_STROKE_WIDTH = 100
    }
}