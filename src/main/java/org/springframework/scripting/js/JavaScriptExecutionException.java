package org.springframework.scripting.js;

import org.springframework.core.NestedRuntimeException;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-09-22, 19:29:07
 */
public class JavaScriptExecutionException extends NestedRuntimeException {
	public JavaScriptExecutionException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
