package net.rokoucha.superseisan.di

import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class CustomTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: android.content.Context?
    ): android.app.Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
