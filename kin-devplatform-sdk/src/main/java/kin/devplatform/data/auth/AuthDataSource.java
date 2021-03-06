package kin.devplatform.data.auth;

import android.support.annotation.NonNull;
import kin.devplatform.KinCallback;
import kin.devplatform.base.ObservableData;
import kin.devplatform.core.network.ApiException;
import kin.devplatform.data.Callback;
import kin.devplatform.network.model.AuthToken;
import kin.devplatform.network.model.SignInData;

public interface AuthDataSource {

	void setSignInData(@NonNull final SignInData signInData);

	ObservableData<String> getAppID();

	String getDeviceID();

	String getUserID();

	String getEcosystemUserID();

	void setAuthToken(@NonNull final AuthToken authToken);

	AuthToken getAuthTokenSync();

	boolean isActivated();

	void activateAccount(@NonNull final KinCallback<Void> callback);

	interface Local {

		void setSignInData(@NonNull final SignInData signInData);

		void setAuthToken(@NonNull final AuthToken authToken);

		void getAppId(@NonNull final Callback<String, Void> callback);

		String getDeviceID();

		String getUserID();

		String getEcosystemUserID();

		AuthToken getAuthTokenSync();

		boolean isActivated();

		void activateAccount();

	}

	interface Remote {

		void setSignInData(@NonNull final SignInData signInData);

		AuthToken getAuthTokenSync();

		void activateAccount(@NonNull final Callback<AuthToken, ApiException> callback);
	}
}
