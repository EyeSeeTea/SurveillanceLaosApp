package org.eyeseetea.malariacare.strategies;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.User;
import org.eyeseetea.malariacare.data.sync.importer.PullController;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.ALoginUseCase;
import org.eyeseetea.malariacare.domain.usecase.LoadUserAndCredentialsUseCase;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;
import org.eyeseetea.malariacare.domain.usecase.pull.PullUseCase;
import org.hisp.dhis.client.sdk.ui.views.FontButton;

public class LoginActivityStrategy extends ALoginActivityStrategy {

    private static final String TAG = ".LoginActivityStrategy";

    public LoginActivityStrategy(LoginActivity loginActivity) {
        super(loginActivity);
    }

    /**
     * LoginActivity does NOT admin going backwads since it is always the first activity.
     * Thus onBackPressed closes the app
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginActivity.startActivity(intent);
    }

    @Override
    public void onCreate() {
        if (existsLoggedUser()) {
            LoadUserAndCredentialsUseCase loadUserAndCredentialsUseCase =
                    new LoadUserAndCredentialsUseCase(loginActivity);

            loadUserAndCredentialsUseCase.execute();

            finishAndGo(DashboardActivity.class);
        } else {
            addDemoButton();
        }
    }

    private boolean existsLoggedUser() {
        return User.getLoggedUser() != null && !ProgressActivity.PULL_CANCEL;
    }

    private void addDemoButton() {
        ViewGroup loginViewsContainer = (ViewGroup) loginActivity.findViewById(
                R.id.layout_login_views);

        loginActivity.getLayoutInflater().inflate(R.layout.demo_login_button, loginViewsContainer,
                true);

        FontButton demoButton = (FontButton) loginActivity.findViewById(R.id.demo_login_button);

        demoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Credentials demoCrededentials = Credentials.createDemoCredentials();

                loginActivity.mLoginUseCase.execute(demoCrededentials,
                        new ALoginUseCase.Callback() {
                            @Override
                            public void onLoginSuccess() {
                                executePullDemo();
                            }

                            @Override
                            public void onServerURLNotValid() {
                                Log.e(this.getClass().getSimpleName(), "Server url not valid");
                            }

                            @Override
                            public void onInvalidCredentials() {
                                Log.e(this.getClass().getSimpleName(), "Invalid credentials");
                            }

                            @Override
                            public void onNetworkError() {
                                Log.e(this.getClass().getSimpleName(), "Network Error");
                            }
                        });
            }
        });
    }

    private void executePullDemo() {
        PullController pullController = new PullController(loginActivity);
        PullUseCase pullUseCase = new PullUseCase(pullController);

        pullUseCase.execute(true, new PullUseCase.Callback() {
            @Override
            public void onComplete() {
                finishAndGo(DashboardActivity.class);
            }

            @Override
            public void onStep(PullStep step) {
                Log.d(this.getClass().getSimpleName(), step.toString());
            }

            @Override
            public void onError(String message) {
                Log.e(this.getClass().getSimpleName(), message);
            }

            @Override
            public void onNetworkError() {
                Log.e(this.getClass().getSimpleName(), "Network Error");
            }
        });
    }

    public void finishAndGo(Class<? extends Activity> activityClass) {
        loginActivity.startActivity(new Intent(loginActivity, activityClass));

        loginActivity.finish();
    }

    @Override
    public void finishAndGo() {
        finishAndGo(ProgressActivity.class);
    }
}
