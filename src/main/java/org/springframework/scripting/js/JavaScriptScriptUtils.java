package org.springframework.scripting.js;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.springframework.util.StringUtils.uncapitalize;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-09-21, 23:05:20
 */
public class JavaScriptScriptUtils {

	private static final Log log = LogFactory.getLog(JavaScriptScriptUtils.class);

	public static Object createJavaScriptObject(ScriptSource script, Class[] actualInterfaces) throws IOException, ScriptException {
		return Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), actualInterfaces, new JavaScriptInvocationHandler(script));
	}

	private static class JavaScriptInvocationHandler implements InvocationHandler {

		private final ScriptEngine engine;

		private JavaScriptInvocationHandler(ScriptSource script) throws ScriptException, IOException {
			log.debug("Creating proxy for script '" + script + "'");
			engine = new ScriptEngineManager().getEngineByName("JavaScript");
			if(script instanceof ResourceScriptSource)
				engine.put(ScriptEngine.FILENAME, ((ResourceScriptSource) script).getResource().getURI().getPath());
//			CompiledScript compiledScript = ((Compilable) engine).compile(script.getScriptAsString());
			engine.eval(script.getScriptAsString());
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (ReflectionUtils.isEqualsMethod(method))
				return equals(args[0]);
			if (ReflectionUtils.isHashCodeMethod(method)) 
				return this.hashCode();
			if (ReflectionUtils.isToStringMethod(method))
				return toString();
			if (isSetter(method)) {
				addBeanToScriptContext(method.getName(), args[0]);
				return null;
			}
			return invokeScriptMethod(method, args);
		}

		private Object invokeScriptMethod(Method method, Object[] args) throws ScriptException, NoSuchMethodException {
			try {
				return ((Invocable) engine).invokeFunction(method.getName(), args);
			} catch (ScriptException e) {
				throw new JavaScriptExecutionException(e.getMessage(), e);
			} catch (NoSuchMethodException e) {
				throw new JavaScriptExecutionException("Script does not define: " + method.getName() + " function", e);
			}
		}

		private void addBeanToScriptContext(String setterName, Object bean) {
			final String beanName = uncapitalize(setterName.substring(3));
			log.debug("Putting " + bean + " to the " + engine + " context with name '" + beanName + "'");
			engine.put(beanName, bean);
		}

		private boolean isSetter(Method method) {
			return method.getName().startsWith("set") && method.getParameterTypes().length == 1;
		}
	}
}
