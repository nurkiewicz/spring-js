package org.springframework.scripting.js;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.ScriptFactory;
import org.springframework.scripting.ScriptSource;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-08-24, 22:58:28
 */
public class JavaScriptScriptFactory implements ScriptFactory {

	private String scriptSourceLocator;
	private Class[] scriptInterfaces;

	public JavaScriptScriptFactory(String scriptSourceLocator, Class[] scriptInterfaces) {
		this.scriptSourceLocator = scriptSourceLocator;
		this.scriptInterfaces = scriptInterfaces;
	}

	public String getScriptSourceLocator() {
		return scriptSourceLocator;
	}

	public Class[] getScriptInterfaces() {
		return scriptInterfaces;
	}

	public boolean requiresConfigInterface() {
		return true;
	}

	public Object getScriptedObject(ScriptSource scriptSource, Class[] actualInterfaces) throws IOException, ScriptCompilationException {
		try {
			return JavaScriptScriptUtils.createJavaScriptObject(scriptSource, actualInterfaces);
		} catch (ScriptException e) {
			throw new ScriptCompilationException(scriptSource, e);
		}
	}

	public Class getScriptedObjectType(ScriptSource scriptSource) throws IOException, ScriptCompilationException {
		return null;
	}

	public boolean requiresScriptedObjectRefresh(ScriptSource scriptSource) {
		return scriptSource.isModified();
	}
}