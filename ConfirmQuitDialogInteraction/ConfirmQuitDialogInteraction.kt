
    

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
