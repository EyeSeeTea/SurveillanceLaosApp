/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.fragments.DashboardSentFragment;
import org.eyeseetea.malariacare.fragments.DashboardUnsentFragment;
import org.eyeseetea.malariacare.fragments.MonitorFragment;
import org.eyeseetea.malariacare.fragments.ReviewFragment;
import org.eyeseetea.malariacare.fragments.StockFragment;
import org.eyeseetea.malariacare.fragments.SurveyFragment;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.GradleVariantConfig;

public class DashboardActivity extends BaseActivity {

    private final static String TAG = ".DashboardActivity";
    public static DashboardActivity dashboardActivity;
    /**
     * Move to that question from reviewfragment
     */
    public static Question moveToQuestion;
    TabHost tabHost;
    MonitorFragment monitorFragment;
    DashboardUnsentFragment unsentFragment;
    DashboardSentFragment sentFragment;
    StockFragment stockFragment;
    ReviewFragment reviewFragment;
    SurveyFragment surveyFragment;
    /**
     * Flag that controls the fragment change animations
     */
    boolean isMoveToLeft;
    private boolean reloadOnResume = true;
    /**
     * Flags required to decide if the survey must be deleted or not on pause the surveyFragment
     */
    private boolean isLoadingReview = false;

    /**
     * Flags required to decide if the survey must be deleted or not
     */
    private boolean isBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        AsyncPopulateDB asyncPopulateDB = new AsyncPopulateDB(this);
        asyncPopulateDB.execute((Void) null);
        dashboardActivity = this;
        setContentView(R.layout.tab_dashboard);
        Survey.removeInProgress();
        if (savedInstanceState == null) {
            initAssess();
            initImprove();
            initMonitor();
            if (GradleVariantConfig.isStockFragmentActive()) {
                initStock();
            }
        }
        initTabHost(savedInstanceState);
        /* set tabs in order */
        LayoutUtils.setTabHosts(this);
        LayoutUtils.setDivider(this);
        //set the tabs background as transparent
        setTabsBackgroundColor(R.color.tab_unpressed_background);

        //set first tab as selected:
        tabHost.getTabWidget().getChildAt(0).setBackgroundColor(
                getResources().getColor(R.color.tab_pressed_background));

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                /** If current tab is android */

                //set the tabs background as transparent
                setTabsBackgroundColor(R.color.tab_unpressed_background);

                //If change of tab from surveyFragment or FeedbackFragment they could be closed.
                if (isSurveyFragmentActive()) {
                    onSurveyBackPressed();
                }
                if (isReviewFragmentActive()) {
                    exitReviewOnChangeTab(null);
                }
                if (tabId.equalsIgnoreCase(getResources().getString(R.string.tab_tag_assess))) {
                    unsentFragment.reloadData();
                } else if (tabId.equalsIgnoreCase(
                        getResources().getString(R.string.tab_tag_improve))) {
                    sentFragment.reloadData();
                } else if (tabId.equalsIgnoreCase(
                        getResources().getString(R.string.tab_tag_stock))) {
                    stockFragment.reloadData();
                } else if (tabId.equalsIgnoreCase(
                        getResources().getString(R.string.tab_tag_monitor))) {
                    monitorFragment.reloadData();
                }
                tabHost.getCurrentTabView().setBackgroundColor(
                        getResources().getColor(R.color.tab_pressed_background));
            }
        });
        // init tabHost
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            tabHost.getTabWidget().getChildAt(i).setFocusable(false);
        }

    }

    public void setTabHostsWithText() {
        Context context = PreferencesState.getInstance().getContext();
        setTab(context.getResources().getString(R.string.tab_tag_assess), R.id.tab_assess_layout,
                context.getResources().getString(R.string.unsent_data));
        setTab(context.getResources().getString(R.string.tab_tag_improve), R.id.tab_improve_layout,
                context.getResources().getString(R.string.sent_data));
        if (GradleVariantConfig.isStockFragmentActive()) {
            setTab(context.getResources().getString(R.string.tab_tag_stock), R.id.tab_stock_layout,
                    context.getResources().getString(R.string.tab_tag_stock));
        }
        setTab(context.getResources().getString(R.string.tab_tag_monitor), R.id.tab_monitor_layout,
                context.getResources().getString(R.string.monitor_button));
        if (GradleVariantConfig.isStockFragmentActive()) {
            initStock();
        }
    }

    public void setTabHostsWithImages() {
        Context context = PreferencesState.getInstance().getContext();
        setTab(context.getResources().getString(R.string.tab_tag_assess), R.id.tab_assess_layout,
                context.getResources().getDrawable(R.drawable.tab_assess));
        setTab(context.getResources().getString(R.string.tab_tag_improve), R.id.tab_improve_layout,
                context.getResources().getDrawable(R.drawable.tab_improve));
        if (GradleVariantConfig.isStockFragmentActive()) {
            setTab(context.getResources().getString(R.string.tab_tag_stock), R.id.tab_stock_layout,
                    context.getResources().getDrawable(R.drawable.tab_stock));
        }
        setTab(context.getResources().getString(R.string.tab_tag_monitor), R.id.tab_monitor_layout,
                context.getResources().getDrawable(R.drawable.tab_monitor));
    }


    /**
     * Sets a divider drawable and background.
     */
    public void setDivider() {
        tabHost.getTabWidget().setShowDividers(TabWidget.SHOW_DIVIDER_MIDDLE);
        tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_line);
        tabHost.getTabWidget().setBackgroundColor(
                ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                        R.color.tab_unpressed_background));
    }

    private void setTabsBackgroundColor(int color) {
        //set the tabs background as transparent
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            tabHost.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(color));
        }
    }

    /**
     * Init the conteiner for all the tabs
     */
    private void initTabHost(Bundle savedInstanceState) {
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
    }

    /**
     * Set tab in tabHost
     *
     * @param tabName is the name of the tab
     * @param layout  is the id of the layout
     */
    private void setTab(String tabName, int layout, String text) {
        TabHost.TabSpec tab = tabHost.newTabSpec(tabName);
        tab.setContent(layout);
        tab.setIndicator(text);
        tabHost.addTab(tab);
        addTagToLastTab(tabName);
    }

    /**
     * Set tab in tabHost
     *
     * @param tabName is the name of the tab
     * @param layout  is the id of the layout
     */
    private void setTab(String tabName, int layout, Drawable image) {
        TabHost.TabSpec tab = tabHost.newTabSpec(tabName);
        tab.setContent(layout);
        tab.setIndicator("", image);
        tabHost.addTab(tab);
        addTagToLastTab(tabName);
    }

    private void addTagToLastTab(String tabName) {
        TabWidget tabWidget = tabHost.getTabWidget();
        int numTabs = tabWidget.getTabCount();
        LinearLayout tabIndicator = (LinearLayout) tabWidget.getChildTabViewAt(numTabs - 1);

        ImageView imageView = (ImageView) tabIndicator.getChildAt(0);
        imageView.setTag(tabName);
    }


    public void initAssess() {
        isLoadingReview = false;
        unsentFragment = new DashboardUnsentFragment();
        unsentFragment.setArguments(getIntent().getExtras());
        replaceListFragment(R.id.dashboard_details_container, unsentFragment);
    }

    public void restoreAssess() {
        replaceFragment(R.id.dashboard_details_container, surveyFragment);
    }

    /**
     * This method initializes the reviewFragment
     */
    public void initReview() {
        if (reviewFragment == null) {
            reviewFragment = new ReviewFragment();
        }
        replaceFragment(R.id.dashboard_details_container, reviewFragment);
    }

    /**
     * This method initializes the Improve fragment(DashboardSentFragment)
     */
    public void initImprove() {
        sentFragment = new DashboardSentFragment();
        sentFragment.setArguments(getIntent().getExtras());
        sentFragment.reloadData();
        replaceListFragment(R.id.dashboard_completed_container, sentFragment);
    }

    /**
     * This method initializes the Stock fragment(StockFragment)
     */
    public void initStock() {
        stockFragment = new StockFragment();
        stockFragment.setArguments(getIntent().getExtras());
        stockFragment.reloadData();
        replaceFragment(R.id.dashboard_stock_container, stockFragment);
    }

    /**
     * This method initializes the Survey fragment
     */
    public void initSurvey() {
        isBackPressed = false;
        tabHost.getTabWidget().setVisibility(View.GONE);
        if (surveyFragment == null) {
            surveyFragment = new SurveyFragment();
        }
        replaceFragment(R.id.dashboard_details_container, surveyFragment);
    }

    /**
     * This method initializes the Monitor fragment
     */
    public void initMonitor() {
        if (monitorFragment == null) {
            monitorFragment = new MonitorFragment();
        }
        replaceFragment(R.id.dashboard_charts_container, monitorFragment);
    }


    // Add the fragment to the activity, pushing this transaction
    // on to the back stack.
    private void replaceFragment(int layout, Fragment fragment) {
        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(layout, fragment);
        ft.commit();
    }

    private void replaceListFragment(int layout, ListFragment fragment) {
        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(layout, fragment);
        ft.commit();
    }

    @NonNull
    private FragmentTransaction getFragmentTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (isMoveToLeft) {
            isMoveToLeft = false;
            ft.setCustomAnimations(R.animator.anim_slide_in_right, R.animator.anim_slide_out_right);
        } else {
            ft.setCustomAnimations(R.animator.anim_slide_in_left, R.animator.anim_slide_out_left);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        return ft;
    }

    /**
     * Init the fragments
     */
    private void setFragmentTransaction(int layout, ListFragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(layout, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    protected void initTransition() {
        this.overridePendingTransition(R.transition.anim_slide_in_right,
                R.transition.anim_slide_out_right);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }


    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    public void setReloadOnResume(boolean doReload) {
        this.reloadOnResume = false;
    }

    public void getSurveysFromService() {
        Log.d(TAG, "getSurveysFromService (" + reloadOnResume + ")");
        if (!reloadOnResume) {
            //Flag is readjusted
            reloadOnResume = true;
            return;
        }
        Intent surveysIntent = new Intent(this, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        this.startService(surveysIntent);
    }

    /**
     * Just to avoid trying to navigate back from the dashboard. There's no parent activity here
     */
    @Override
    public void onBackPressed() {
        isMoveToLeft = true;
        if (isReviewFragmentActive()) {
            hideReview();
        } else if (isSurveyFragmentActive()) {
            onSurveyBackPressed();
        } else if (isReviewFragmentActive()) {
            onSurveyBackPressed();
        } else {
            confirmExitApp();
        }
    }


    public void confirmExitApp() {
        Log.d(TAG, "back pressed");
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit the app?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).create().show();
    }

    /**
     * It is called when the user press back in a surveyFragment
     */
    private void onSurveyBackPressed() {
        Log.d(TAG, "onBackPressed");
        Survey survey = Session.getSurvey();
        if (!survey.isSent()) {
            int infoMessage = survey.isInProgress() ? R.string.survey_info_exit_delete
                    : R.string.survey_info_exit;
            new AlertDialog.Builder(this)
                    .setTitle(R.string.survey_info_exit)
                    .setMessage(infoMessage)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            //Reload data using service
                            isBackPressed = true;
                            closeSurveyFragment();
                        }
                    }).create().show();
        } else {
            closeSurveyFragment();
        }
    }

    /**
     * This method shows the review fragment to te user before decides if send the survey.
     */
    public void showReviewFragment() {
        isLoadingReview = true;
        initReview();
    }


    /**
     * This method closes the survey fragment.
     * If the active survey was completed it will be saved as completed.
     * After that, loads the Assess fragment(DashboardUnSentFragment) in the Assess tab.
     */
    public void closeSurveyFragment() {
        tabHost.getTabWidget().setVisibility(View.VISIBLE);
        isLoadingReview = false;
        ScoreRegister.clear();
        boolean isSent = false;
        if (Session.getSurvey() != null) {
            isSent = Session.getSurvey().isSent();
        }
        if (isBackPressed) {
            beforeExit();
        }
        surveyFragment.unregisterReceiver();
        if (isSent) {
            tabHost.setCurrentTabByTag(getResources().getString(R.string.tab_tag_improve));
            initAssess();
        } else {
            initAssess();
            unsentFragment.reloadData();
        }
    }

    /**
     * This method closes the Feedback Fragment and loads the Improve fragment
     * (DashboardSentFragment)
     * in the Improve tab
     */
    public void closeReviewFragment() {
        tabHost.getTabWidget().setVisibility(View.VISIBLE);
        isLoadingReview = false;
        initAssess();
        unsentFragment.reloadData();
    }


    public void beforeExit() {
        Survey survey = Session.getSurvey();
        if (survey != null) {
            boolean isInProgress = survey.isInProgress();
            survey.getValuesFromDB();
            //Exit + InProgress -> delete
            if (isBackPressed && isInProgress) {
                Session.setSurvey(null);
                survey.delete();
                isBackPressed = false;
                return;
            }

            //InProgress -> update status
            if (isInProgress) {
                survey.updateSurveyStatus();
            }

            //Completed | Sent -> no action
        }
    }

    /**
     * Called when the user clicks the New Survey button
     */
    public void newSurvey(View view) {
        Program program = new Select().from(Program.class).querySingle();
        // Put new survey in session
        Survey survey = new Survey(null, program, Session.getUser());
        survey.save();
        Session.setSurvey(survey);
        //Look for coordinates
        prepareLocationListener(survey);

        initSurvey();
    }

    /**
     * Called when the user clicks the exit Review button
     */
    public void exitReview(View view) {
        showDone();
    }

    /**
     * Called when the user clicks in other tab
     */
    public void exitReviewOnChangeTab(View view) {
        ScoreRegister.clear();
        beforeExit();
        closeReviewFragment();
    }

    /**
     * Show a final dialog to announce the survey is over
     */
    public void showDone() {
        AlertDialog.Builder msgConfirmation = new AlertDialog.Builder(this)
                .setTitle(R.string.survey_completed)
                .setMessage(R.string.survey_completed_text)
                .setCancelable(false)
                .setPositiveButton(R.string.survey_send, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Survey survey = Session.getSurvey();
                        survey.updateSurveyStatus();
                        closeSurveyFragment();
                    }
                });
        msgConfirmation.setNegativeButton(R.string.survey_review, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                DashboardActivity.moveToQuestion = (Session.getSurvey().getValues().get(
                        0).getQuestion());
                hideReview();
            }
        });

        msgConfirmation.create().show();
    }

    /**
     * Checks if a survey fragment is active
     */
    private boolean isReviewFragmentActive() {
        return isFragmentActive(reviewFragment, R.id.dashboard_details_container);
    }

    /**
     * Checks if a survey fragment is active
     */
    private boolean isSurveyFragmentActive() {
        return isFragmentActive(surveyFragment, R.id.dashboard_details_container);
    }

    /**
     * Checks if a dashboardUnsentFragment is active
     */
    private boolean isFragmentActive(Fragment fragment, int layout) {
        Fragment currentFragment = this.getFragmentManager().findFragmentById(layout);
        if (currentFragment.equals(fragment)) {
            return true;
        }
        return false;
    }

    /**
     * This method moves to the Assess tab and open the active survey.
     */
    public void openSentSurvey() {
        tabHost.setCurrentTabByTag(getResources().getString(R.string.tab_tag_assess));
        initSurvey();
    }

    /**
     * This method hide the reviewFragment restoring the Assess tab with the active SurveyFragment
     */
    public void hideReview(Question question) {
        moveToQuestion = question;
        hideReview();
    }

    /**
     * This method hide the reviewFragment restoring the Assess tab with the active SurveyFragment
     */
    public void hideReview() {
        isMoveToLeft = true;
        restoreAssess();
    }

    public boolean isLoadingReview() {
        return isLoadingReview;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class AsyncPopulateDB extends AsyncTask<Void, Void, Exception> {

        //User user;
        DashboardActivity dashboardActivity;

        AsyncPopulateDB(DashboardActivity dashboardActivity) {
            this.dashboardActivity = dashboardActivity;
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                if (!BuildConfig.multiuser) {
                    Log.i(TAG, "Creating demo login from dashboard ...");
                    LoginUseCase loginUseCase = new LoginUseCase(dashboardActivity);

                    Credentials demoCrededentials = Credentials.createDemoCredentials();

                    loginUseCase.execute(demoCrededentials);
                }

                PopulateDB.initDataIfRequired(getAssets());
            } catch (Exception ex) {
                Log.e(TAG, "Error initializing DB: ", ex);
                return ex;
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Exception exception) {
            //Error
            if (exception != null) {
                new AlertDialog.Builder(DashboardActivity.this)
                        .setTitle(R.string.dialog_title_error)
                        .setMessage(exception.getMessage())
                        .setNeutralButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        finish();
                                    }
                                }).create().show();
                return;
            }

            getSurveysFromService();
        }
    }

}
