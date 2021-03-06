package kin.devplatform.data.order;

import static kin.devplatform.exception.ClientException.INTERNAL_INCONSISTENCY;

import android.support.annotation.NonNull;
import kin.devplatform.core.network.ApiException;
import kin.devplatform.data.Callback;
import kin.devplatform.network.model.Order;
import kin.devplatform.network.model.Order.Status;
import kin.devplatform.util.ErrorUtil;

class GetOrderPollingCall extends Thread {

	private static final int[] DELAY_SECONDS = {2, 4, 8, 16, 32, 32, 32, 32, 32};
	private static final int SEC_IN_MILLI = 1000;
	private static final int DELAYED_ATTEMPTED_NUMBER = 5;

	private final OrderDataSource.Remote remote;
	private final String orderID;
	private final Callback<Order, ApiException> callback;

	GetOrderPollingCall(@NonNull final OrderDataSource.Remote remote, final String orderID,
		@NonNull final Callback<Order, ApiException> callback) {
		this.remote = remote;
		this.orderID = orderID;
		this.callback = callback;
	}

	@Override
	public void run() {
		getOrder(0);
	}

	private void getOrder(int pollingIndex) {
		try {
			if (pollingIndex < DELAY_SECONDS.length) {
				Order order = remote.getOrderSync(orderID);
				if (order == null || order.getStatus() == Status.PENDING) {
					if (order != null && pollingIndex == DELAYED_ATTEMPTED_NUMBER) {
						callback.onResponse(order.status(Status.DELAYED));
					}
					sleep(DELAY_SECONDS[pollingIndex] * SEC_IN_MILLI);
					getOrder(++pollingIndex);
				} else {
					callback.onResponse(order);
				}
			} else {
				callback.onFailure(ErrorUtil.createOrderTimeoutException());
			}
		} catch (final InterruptedException e) {
			callback.onFailure(toApiException(e));
		}
	}

	private ApiException toApiException(InterruptedException e) {
		return new ApiException(INTERNAL_INCONSISTENCY, e);
	}
}