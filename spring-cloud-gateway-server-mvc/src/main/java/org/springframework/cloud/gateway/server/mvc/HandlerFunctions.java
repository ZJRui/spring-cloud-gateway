/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gateway.server.mvc;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

public abstract class HandlerFunctions {

	protected HandlerFunctions() {

	}

	public static HandlerFunction<ServerResponse> http() {
		return new LookupProxyExchangeHandlerFunction();
	}

	public static ApplicationContext getApplicationContext(ServerRequest request) {
		Optional<Object> contextAttr = request.attribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (contextAttr.isEmpty()) {
			throw new IllegalStateException("No Application Context in request attributes");
		}
		return (ApplicationContext) contextAttr.get();
	}

	static class LookupProxyExchangeHandlerFunction implements HandlerFunction<ServerResponse> {

		private AtomicReference<ProxyExchangeHandlerFunction> proxyExchangeHandlerFunction = new AtomicReference<>();

		@Override
		public ServerResponse handle(ServerRequest serverRequest) {
			this.proxyExchangeHandlerFunction.compareAndSet(null, lookup(serverRequest));
			return proxyExchangeHandlerFunction.get().handle(serverRequest);
		}

		private static ProxyExchangeHandlerFunction lookup(ServerRequest request) {
			return getApplicationContext(request).getBean(ProxyExchangeHandlerFunction.class);
		}

	}

}
