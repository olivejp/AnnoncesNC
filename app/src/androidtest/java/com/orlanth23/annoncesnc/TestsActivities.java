package com.orlanth23.annoncesnc;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.EditText;

import com.orlanth23.annoncesnc.ui.activity.ChangePasswordFirebaseActivity;

import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.Contract;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class TestsActivities {

    @Rule
    public ActivityTestRule<ChangePasswordFirebaseActivity> mActivityRule =
            new ActivityTestRule<>(ChangePasswordFirebaseActivity.class, false, false);

    @Contract("_ -> !null")
    private static Matcher<View> withError(final String expected) {
        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(org.hamcrest.Description description) {
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof EditText)) {
                    return false;
                }
                EditText editText = (EditText) view;
                return editText.getError().toString().equals(expected);
            }
        };
    }

    @Test
    public void ensureConnectionNeeded() {
        final ChangePasswordFirebaseActivity activity = mActivityRule.launchActivity(null);

        // Test que l'ancien mot de passe est obligatoire
        onView(withId(R.id.oldPassword))
                .perform(typeText(""), closeSoftKeyboard());

        onView(withId(R.id.btnChangePassword)).perform(click());

        // Check that the text was changed.
        String error = activity.getString(R.string.error_need_user_connection);
        onView(withId(R.id.oldPassword)).check(matches(withError(error)));
    }
}
