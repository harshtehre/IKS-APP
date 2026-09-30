package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.sample.IksSampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("IKSphere", appName)
  }

  @Test
  fun `verify sample data completeness`() {
    assertTrue("At least 10 knowledge domains", IksSampleData.domains.size >= 10)
    assertTrue("At least 5 courses", IksSampleData.courses.size >= 5)
    assertTrue("At least 5 quiz questions", IksSampleData.quizQuestions.size >= 5)
    assertTrue("At least 5 flashcards", IksSampleData.flashcards.size >= 5)
  }
}
