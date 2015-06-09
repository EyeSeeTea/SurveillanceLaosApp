/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.test.suitebuilder.annotation.LargeTest;

import org.eyeseetea.malariacare.DashboardDetailsActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.SurveyActivity;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 *
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SurveyEspressoTest extends MalariaEspressoTest{

    @Rule
    public IntentsTestRule<SurveyActivity> mActivityRule = new IntentsTestRule<>(
            SurveyActivity.class);

    @BeforeClass
    public static void init(){
        populateData(InstrumentationRegistry.getTargetContext().getAssets());
        mockSessionSurvey();
    }

    @Before
    public void setup(){
        super.setup();
    }

    @Test
    public void form_views() {
        onView(withId(R.id.tabSpinner)).check(matches(isDisplayed()));
        onView(withText("General Info")).check(matches(isDisplayed()));
    }

    @Test
    public void back_shows_dialog(){
        //GIVEN
        pressBack();

        //THEN
        onView(withText(android.R.string.no)).check(matches(isDisplayed()));
        onView(withText(android.R.string.yes)).check(matches(isDisplayed()));
    }

    @Test
    public void back_yes_intent(){
        //GIVEN
        pressBack();

        //WHEN
        onView(withText(android.R.string.yes)).perform(click());

        //THEN
        assertEquals(DashboardDetailsActivity.class, getActivityInstance().getClass());
    }

    @Test
    public void change_to_scored_tab(){
        //WHEN: Select 'Profile' tab
        whenTabSelected(1);

        //THEN
        onView(withText("HR - Nurses")).check(matches(isDisplayed()));
        onView(withId(R.id.subtotalScoreText)).check(matches(isDisplayed()));
    }

    @Test
    public void change_to_score(){
        //WHEN: Select 'Score' tab
        whenTabSelected(10);

        //THEN
        onView(withText(R.string.score_info_case1)).check(matches(isDisplayed()));
        onView(withText(R.string.score_info_case2)).check(matches(isDisplayed()));
        onView(withText(R.string.score_info_case3)).check(matches(isDisplayed()));
    }

    @Test
    public void change_to_composite_score(){
        //WHEN: Select 'Composite Score' tab
        whenTabSelected(11);

        //THEN
        onView(withText("Services, materials and reporting")).check(matches(isDisplayed()));
    }

    @Test
    public void in_c1_rdt_score_some_points(){
        //WHEN: Select 'C1-RDT' tab
        whenTabSelected(3);

        //WHEN: Some answers 'Yes'
        for(int i=6;i<=16;i++){
            whenDropDownAnswered(i,true);
        }

        //THEN
        onView(withId(R.id.score)).check(matches(withText("66")));
        onView(withId(R.id.qualitativeScore)).check(matches(withText(getActivityInstance().getString(R.string.fair))));
    }

    @Test
    public void global_scores_are_calculated(){
        //WHEN: Select 'C1-RDT' tab | Some answers 'Yes'
        whenTabSelected(3);

        for(int i=6;i<=16;i++){
            whenDropDownAnswered(i,true);
        }

        //WHEN: Select 'Score' tab
        whenTabSelected(10);

        //THEN
        onView(withId(R.id.totalScore)).check(matches(withText("8")));
        onView(withId(R.id.rdtAvg)).check(matches(withText("22")));
    }

    /**
     * Select the tab number 'x'
     * @param num Index of the tab to select
     */
    private void whenTabSelected(int num){
        onView(withId(R.id.tabSpinner)).perform(click());
        onData(is(instanceOf(Tab.class))).atPosition(num).perform(click());
    }

    /**
     * Answers the question at position 'x'.
     * @param position Index of the question to answer
     * @param answer True (Yes), False (No)
     */
    private void whenDropDownAnswered(int position,boolean answer){
        onData(is(instanceOf(Question.class))).
                inAdapterView(withId(R.id.listView)).
                atPosition(position).
                onChildView(withId(R.id.answer)).
                perform(click());
        int indexAnswer=answer?1:2;
        onData(is(instanceOf(Option.class))).atPosition(indexAnswer).perform(click());
    }

    private Activity getActivityInstance(){
        final Activity[] activity = new Activity[1];
        Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
        instrumentation.waitForIdleSync();
        instrumentation.runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                if (resumedActivities.iterator().hasNext()) {
                    activity[0] = (Activity) resumedActivities.iterator().next();
                }
            }
        });

        return activity[0];
    }
}