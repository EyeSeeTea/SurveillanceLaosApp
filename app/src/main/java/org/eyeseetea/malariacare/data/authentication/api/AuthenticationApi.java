package org.eyeseetea.malariacare.data.authentication.api;

import android.support.annotation.NonNull;

import com.squareup.okhttp.Credentials;

import org.eyeseetea.malariacare.data.authentication.CredentialsReader;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.exception.ConfigJsonInvalidException;

public class AuthenticationApi {


    public static String getHardcodedApiCredentials() throws ConfigJsonInvalidException {
        return
                Credentials.basic(getHardcodedApiUser(),
                        getHardcodedApiPass());
    }

    public static String getApiCredentialsFromLogin() {
        return
                Credentials.basic(Session.getCredentials().getUsername(),
                        Session.getCredentials().getPassword());
    }

    @NonNull
    static String getHardcodedApiUser() throws ConfigJsonInvalidException {
        return CredentialsReader.getInstance().getUser();
    }

    @NonNull
    static String getHardcodedApiPass() throws ConfigJsonInvalidException {
        return CredentialsReader.getInstance().getPassword();
    }
}
