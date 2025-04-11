package dev.qixils.crowdcontrol.common.util;

import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;

public class CCResponseException extends RuntimeException {
	private final CCEffectResponse response;

	public CCResponseException(CCEffectResponse response) {
		super(response.getMessage());
		this.response = response;
	}

	public CCEffectResponse getResponse() {
		return response;
	}
}
