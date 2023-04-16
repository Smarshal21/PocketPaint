 {
    fun onPositiveButton(): ViewInteraction {
        return Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(android.R.id.button1),
                ViewMatchers.withText(R.string.save_button_text),
                ViewMatchers.isAssignableFrom(
                    Button::class.java
                )
            )
        )
    }

    fun checkPositiveButton(matcher: ViewAssertion?): ConfirmQuitDialogInteraction {
        onPositiveButton()
            .check(matcher)
        return this
    }

   

    fun checkNegativeButton(matcher: ViewAssertion?): ConfirmQuitDialogInteraction {
        onNegativeButton()
            .check(matcher)
        return this
    }

    fun checkNeutralButton(matcher: ViewAssertion?): ConfirmQuitDialogInteraction {
        Espresso.onView(ViewMatchers.withId(android.R.id.button3))
            .check(matcher)
        return this
    }

    fun checkMessage(matcher: ViewAssertion?): ConfirmQuitDialogInteraction {
        Espresso.onView(ViewMatchers.withText(R.string.closing_security_question))
            .check(matcher)
        return this
    }

    fun checkTitle(matcher: ViewAssertion?): ConfirmQuitDialogInteraction {
        Espresso.onView(ViewMatchers.withText(R.string.closing_security_question_title))
            .check(matcher)
        return this
    }

    companion object {
        @JvmStatic
		fun onConfirmQuitDialog(): ConfirmQuitDialogInteraction {
            return ConfirmQuitDialogInteraction()
        }
    }
}
