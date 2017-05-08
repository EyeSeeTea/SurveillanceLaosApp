package org.eyeseetea.malariacare.strategies;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.User;
import org.eyeseetea.malariacare.data.sync.importer.PullController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.pull.PullFilters;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;
import org.eyeseetea.malariacare.domain.usecase.pull.PullUseCase;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;

public class LoginActivityStrategy extends ALoginActivityStrategy{

    private static final String TAG = ".LoginActivityStrategy";
    public static final String EXIT = "exit";

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
        if (loginActivity.getIntent().getBooleanExtra(EXIT, false)) {
            loginActivity.finish();
        }
//        if (existsLoggedUser() && PopulateDB.hasMandatoryTables()) {
//            LoadUserAndCredentialsUseCase loadUserAndCredentialsUseCase =
//                    new LoadUserAndCredentialsUseCase(loginActivity);
//
//            loadUserAndCredentialsUseCase.execute();
//
//            finishAndGo(DashboardActivity.class);
//        } else {
//            //TODO jsanchez, this is necessary because oncreate is called from
//            //AsyncTask review Why is invoked from AsyncTask, It's not very correct
//            PopulateDB.wipeDataBase();
//        }
    }

    private boolean existsLoggedUser() {
        return User.getLoggedUser() != null;
    }

    private void executePullDemo() {
        PullController pullController = new PullController(loginActivity);
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();

        PullUseCase pullUseCase = new PullUseCase(pullController, asyncExecutor, mainExecutor);

        PullFilters pullFilters = new PullFilters();
        pullFilters.setDemo(true);

        pullUseCase.execute(pullFilters, new PullUseCase.Callback() {
            @Override
            public void onComplete() {
                loginActivity.onFinishLoading(null);
                finishAndGo(DashboardActivity.class);
            }

            @Override
            public void onStep(PullStep step) {
                Log.d(this.getClass().getSimpleName(), step.toString());
            }

            @Override
            public void onError(String message) {
                loginActivity.onFinishLoading(null);
                Log.e(this.getClass().getSimpleName(), message);
            }

            @Override
            public void onPullConversionError() {
                loginActivity.onFinishLoading(null);
                Log.e(this.getClass().getSimpleName(), "Pull conversion error");
            }

            @Override
            public void onCancel() {
                loginActivity.onFinishLoading(null);
                Log.e(this.getClass().getSimpleName(), "Pull cancel");
            }

            @Override
            public void onNetworkError() {
                loginActivity.onFinishLoading(null);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }


    @Override
    public void initViews() {
        EditText passwordEditText = (EditText) loginActivity.findViewById(R.id.edittext_password);
        passwordEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        TextInputLayout passwordHint =
                (TextInputLayout) loginActivity.findViewById(R.id.password_hint);
        passwordHint.setHint(loginActivity.getResources().getText(R.string.login_pin));
    }

    @Override
    public void onLoginSuccess(Credentials credentials) {
        finishAndGo();
    }
}
