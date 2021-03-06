package kin.devplatform.bi;

import static kin.devplatform.core.network.ApiClient.APPLICATION_JSON_KEY;
import static kin.devplatform.core.network.ApiClient.POST;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kin.devplatform.Configuration;
import kin.devplatform.core.network.ApiCallback;
import kin.devplatform.core.network.ApiClient;
import kin.devplatform.core.network.ApiException;
import kin.devplatform.core.network.Pair;
import okhttp3.Call;

class EventsApi {

	private ApiClient apiClient;

	/*
	 * Constructor for EventsApi
	 */
	EventsApi() {
		apiClient = new ApiClient(Configuration.getEnvironment().getBiUrl());
	}

	/**
	 * Build call for sendEvent
	 *
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 */
	public Call sendEventCall(Event event) throws ApiException {
		Object localVarPostBody = event;

		// create path and map variables
		String localVarPath = "";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();

		Map<String, String> localVarHeaderParams = new HashMap<String, String>();

		final String eventId = apiClient.parameterToString(event.getCommon().getEventId());
		localVarHeaderParams.put("X-REQUEST-ID", eventId);

		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		String[] applicationJson = {APPLICATION_JSON_KEY};
		final String localVarAccept = apiClient.selectHeaderAccept(applicationJson);
		localVarHeaderParams.put("Accept", localVarAccept);

		final String localVarContentType = apiClient.selectHeaderContentType(applicationJson);
		localVarHeaderParams.put("Content-Type", localVarContentType);

		return apiClient
			.buildCall(localVarPath, POST, localVarQueryParams, localVarCollectionQueryParams, localVarPostBody,
				localVarHeaderParams, localVarFormParams, null, null);
	}

	@SuppressWarnings("rawtypes")
	private Call sendEventValidateBeforeCall(Event event) throws ApiException {
		Call call = sendEventCall(event);
		return call;
	}

	/**
	 * Send event to BI
	 *
	 * @param callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 */
	public Call sendEventAsync(Event event, final ApiCallback<String> callback)
		throws ApiException {
		Call call = sendEventValidateBeforeCall(event);
		Type localVarReturnType = new TypeToken<String>() {
		}.getType();
		apiClient.executeAsync(call, localVarReturnType, callback);
		return call;
	}
}
