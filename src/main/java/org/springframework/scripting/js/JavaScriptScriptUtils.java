package org.springframework.scripting.js;

import org.springframework.scripting.ScriptSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-09-21, 23:05:20
 */
public class JavaScriptScriptUtils {
	public static Object createJavaScriptObject(String script, Class[] actualInterfaces) throws IOException, ScriptException {

		return Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), actualInterfaces, new JavaScriptInvocationHandler(script, actualInterfaces));
	}

	private static class JavaScriptInvocationHandler implements InvocationHandler {
		private final ScriptEngine engine;
		private final Class[] interfaces;

		private JavaScriptInvocationHandler(String script, Class[] interfaces) throws ScriptException {
			this.interfaces = interfaces;
			engine = new ScriptEngineManager().getEngineByName("JavaScript");
			engine.eval(script);
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (ReflectionUtils.isEqualsMethod(method))
				return equals(args[0]);
			if (ReflectionUtils.isHashCodeMethod(method)) 
				return this.hashCode();
			if (ReflectionUtils.isToStringMethod(method))
				return toString();
			System.out.println(proxy + " / " + method + " / " + args);
			return null;
		}
	}
}
