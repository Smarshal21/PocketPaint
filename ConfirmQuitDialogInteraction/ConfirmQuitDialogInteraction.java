/*
 *  Paintroid: An image manipulation application for Android.
 *  Copyright (C) 2010-2022 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.paintroid.test.espresso.util.wrappers;


public final class ConfirmQuitDialogInteraction extends CustomViewInteraction {
	private ConfirmQuitDialogInteraction() {
		super(onView(withText(R.string.closing_security_question)).inRoot(isDialog()));
	}

	public static ConfirmQuitDialogInteraction onConfirmQuitDialog() {
		return new ConfirmQuitDialogInteraction();
	}
	 
	
	public ViewInteraction onNegativeButton() {
		return onView(allOf(withId(android.R.id.button2), withText(R.string.discard_button_text), isAssignableFrom(Button.class)));
	}

	
	public ConfirmQuitDialogInteraction checkMessage(ViewAssertion matcher) {
		onView(withText(R.string.closing_security_question))
				.check(matcher);
		return this;
	}

	public ConfirmQuitDialogInteraction checkTitle(ViewAssertion matcher) {
		onView(withText(R.string.closing_security_question_title))
				.check(matcher);
		return this;
	}
	
}
