package dev.qixils.crowdcontrol.common.util;

import live.crowdcontrol.cc4j.CrowdControl;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class ThreadUtil {
	private static final Logger log = LoggerFactory.getLogger(ThreadUtil.class);
	public static long TRY_FOR = (CrowdControl.QUEUE_DURATION - 10L) * 1000L;

	public static boolean sleep() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			return false;
		}
		return !Thread.interrupted();
	}

	@CheckReturnValue
	public static @NotNull CCEffectResponse waitForSuccess(Supplier<CCEffectResponse> supplier) {
		CCEffectResponse resp = null;
		long time = System.currentTimeMillis();
		int i = 0;
		while (System.currentTimeMillis() - time < TRY_FOR) {
			try {
				resp = supplier.get();
			} catch (CCResponseException e) {
				resp = e.getResponse();
			} catch (Exception e) {
				log.warn("Failed to supply", e);
			}
			if (resp != null && (resp.getStatus() == ResponseStatus.SUCCESS || resp.getStatus() == ResponseStatus.FAIL_PERMANENT || resp.getStatus() == ResponseStatus.TIMED_BEGIN)) break;
			if (i++ > 0 && !sleep()) break;
		}
		return resp; // if resp is null then cc4j will handle the fail response for us (eventually...)
	}

	@CheckReturnValue
	public static @NotNull CCEffectResponse waitForSuccess(Supplier<CCEffectResponse> supplier, @NotNull Executor executor) {
		return waitForSuccess(() -> CompletableFuture.supplyAsync(supplier, executor).join());
	}
}
