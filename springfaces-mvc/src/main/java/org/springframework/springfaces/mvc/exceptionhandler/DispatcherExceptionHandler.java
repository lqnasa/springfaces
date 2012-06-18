/*
 * Copyright 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.springfaces.mvc.exceptionhandler;

import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.springfaces.mvc.context.SpringFacesContext;
import org.springframework.springfaces.mvc.internal.MvcViewHandler;
import org.springframework.springfaces.mvc.servlet.Dispatcher;
import org.springframework.springfaces.util.FacesUtils;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

public class DispatcherExceptionHandler implements ExceptionHandler {

	// FIXME DC + complete + test

	private Dispatcher dispatcher;

	public DispatcherExceptionHandler(Dispatcher dispatcher) {
		Assert.notNull(dispatcher, "Dispatcher must not be null");
		this.dispatcher = dispatcher;
	}

	public boolean handle(SpringFacesContext context, Throwable exception) throws Exception {
		if (exception instanceof Exception) {
			return handle(context, (Exception) exception);
		}
		return false;
	}

	private boolean handle(SpringFacesContext context, Exception exception) throws Exception {
		ExternalContext externalContext = context.getFacesContext().getExternalContext();
		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
		Object handler = context.getHandler();
		ModelAndView modelAndView = processHandlerException(request, response, handler, exception);
		if (modelAndView != null) {
			WebUtils.clearErrorRequestAttributes(request);
			if (modelAndView.isReference()) {
				modelAndView.setView(this.dispatcher.resolveViewName(modelAndView.getViewName(), null,
						FacesUtils.getLocale(context.getFacesContext()), null));
			}
			MvcViewHandler.render(context.getFacesContext(), modelAndView);
			return true;
		}
		return false;
	}

	private ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception exception) throws Exception {
		try {
			return this.dispatcher.processHandlerException(request, response, handler, exception);
		} catch (Exception e) {
			if (e == exception) {
				return null;
			}
			throw e;
		}
	}

}