package kin.devplatform.data.auth;

import static kin.devplatform.core.util.DateUtil.getDateFromUTCString;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import java.util.Calendar;
import java.util.Date;
import kin.devplatform.KinCallback;
import kin.devplatform.base.ObservableData;
import kin.devplatform.bi.EventLogger;
import kin.devplatform.bi.events.StellarAccountCreationRequested;
import kin.devplatform.core.network.ApiException;
import kin.devplatform.data.Callback;
import kin.devplatform.network.model.AuthToken;
import kin.devplatform.network.model.SignInData;
import kin.devplatform.util.ErrorUtil;

public class AuthRepository implements AuthDataSource {


	private static AuthRepository instance = null;

	private final AuthDataSource.Local localData;
	private final AuthDataSource.Remote remoteData;

	private final EventLogger eventLogger;

	private SignInData cachedSignInData;
	private AuthToken cachedAuthToken;
	private ObservableData<String> appId = ObservableData.create(null);

	private AuthRepository(@NonNull EventLogger eventLogger, @NonNull AuthDataSource.Local local,
		@NonNull AuthDataSource.Remote remote) {
		this.eventLogger = eventLogger;
		this.localData = local;
		this.remoteData = remote;
	}

	public static void init(@NonNull EventLogger eventLogger, @NonNull AuthDataSource.Local localData,
		@NonNull AuthDataSource.Remote remoteData) {
		if (instance == null) {
			synchronized (AuthRepository.class) {
				if (instance == null) {
					instance = new AuthRepository(eventLogger, localData, remoteData);
				}
			}
		}
	}

	public static AuthRepository getInstance() {
		return instance;
	}

	@Override
	public void setSignInData(@NonNull SignInData signInData) {
		cachedSignInData = signInData;
		localData.setSignInData(signInData);
		remoteData.setSignInData(signInData);
		postAppID(signInData.getAppId());
	}

	@Override
	public ObservableData<String> getAppID() {
		loadCachedAppIDIfNeeded();
		return appId;
	}

	@Override
	public String getDeviceID() {
		return localData.getDeviceID();
	}

	@Override
	public String getUserID() {
		return localData.getUserID();
	}

	@Override
	public String getEcosystemUserID() {
		return localData.getEcosystemUserID();
	}

	private void loadCachedAppIDIfNeeded() {
		if (TextUtils.isEmpty(appId.getValue())) {
			localData.getAppId(new Callback<String, Void>() {
				@Override
				public void onResponse(String appID) {
					postAppID(appID);
				}

				@Override
				public void onFailure(Void t) {
					// No Data Available
				}
			});
		}
	}

	@Override
	public AuthToken getAuthTokenSync() {
		if (cachedAuthToken != null) {
			return cachedAuthToken;
		} else {
			if (cachedSignInData != null) {
				AuthToken authToken = localData.getAuthTokenSync();
				if (authToken != null && !isAuthTokenExpired(authToken)) {
					setAuthToken(authToken);
				} else {
					if (authToken == null) {
						eventLogger.send(StellarAccountCreationRequested.create());
					}
					refreshTokenSync();
				}
				return cachedAuthToken;
			} else {
				return null;
			}
		}
	}

	@Override
	public boolean isActivated() {
		return localData.isActivated();
	}

	@Override
	public void activateAccount(@NonNull final KinCallback<Void> callback) {
		remoteData.activateAccount(new Callback<AuthToken, ApiException>() {
			@Override
			public void onResponse(AuthToken response) {
				localData.activateAccount();
				setAuthToken(response);
				callback.onResponse(null);
			}

			@Override
			public void onFailure(ApiException e) {
				callback.onFailure(ErrorUtil.fromApiException(e));
			}
		});
	}

	private boolean isAuthTokenExpired(AuthToken authToken) {
		if (authToken == null) {
			return true;
		} else {
			Date expirationDate = getDateFromUTCString(authToken.getExpirationDate());
			if (expirationDate != null) {
				return Calendar.getInstance().getTimeInMillis() > expirationDate.getTime();
			} else {
				return true;
			}
		}
	}

	private void refreshTokenSync() {
		AuthToken authToken = remoteData.getAuthTokenSync();
		if (authToken != null) {
			setAuthToken(authToken);
		}
	}

	@Override
	public void setAuthToken(@NonNull AuthToken authToken) {
		cachedAuthToken = authToken;
		localData.setAuthToken(authToken);
		postAppID(authToken.getAppID());
	}

	private void postAppID(@Nullable String appID) {
		appId.postValue(appID);
	}
}
