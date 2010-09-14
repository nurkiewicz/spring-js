/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.jmx.support;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.Constants;

/**
 * Provides supporting infrastructure for registering MBeans with an
 * {@link javax.management.MBeanServer}. The behavior when encountering
 * an existing MBean at a given {@link ObjectName} is fully configurable
 * allowing for flexible registration settings.
 *
 * <p>All registered MBeans are tracked and can be unregistered by calling
 * the #{@link #unregisterBeans()} method.
 *
 * <p>Sub-classes can receive notifications when an MBean is registered or
 * unregistered by overriding the {@link #onRegister(ObjectName)} and
 * {@link #onUnregister(ObjectName)} methods respectively.
 *
 * <p>By default, the registration process will fail if attempting to
 * register an MBean using a {@link javax.management.ObjectName} that is
 * already used.
 *
 * <p>By setting the {@link #setRegistrationBehaviorName(String) registrationBehaviorName}
 * property to <code>REGISTRATION_IGNORE_EXISTING</code> the registration process
 * will simply ignore existing MBeans leaving them registered. This is useful in settings
 * where multiple applications want to share a common MBean in a shared {@link MBeanServer}.
 *
 * <p>Setting {@link #setRegistrationBehaviorName(String) registrationBehaviorName} property
 * to <code>REGISTRATION_REPLACE_EXISTING</code> will cause existing MBeans to be replaced
 * during registration if necessary. This is useful in situations where you can't guarantee
 * the state of your {@link MBeanServer}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setServer
 * @see #setRegistrationBehaviorName
 * @see org.springframework.jmx.export.MBeanExporter
 */
public class MBeanRegistrationSupport {

	/**
	 * Constant indicating that registration should fail when
	 * attempting to register an MBean under a name that already exists.
	 * <p>This is the default registration behavior.
	 */
	public static final int REGISTRATION_FAIL_ON_EXISTING = 0;

	/**
	 * Constant indicating that registration should ignore the affected MBean
	 * when attempting to register an MBean under a name that already exists.
	 */
	public static final int REGISTRATION_IGNORE_EXISTING = 1;

	/**
	 * Constant indicating that registration should replace the affected MBean
	 * when attempting to register an MBean under a name that already exists.
	 */
	public static final int REGISTRATION_REPLACE_EXISTING = 2;


	/**
	 * Constants for this class.
	 */
	private static final Constants constants = new Constants(MBeanRegistrationSupport.class);

	/**
	 * <code>Log</code> instance for this class.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The <code>MBeanServer</code> instance being used to register beans.
	 */
	protected MBeanServer server;

	/**
	 * The beans that have been registered by this exporter.
	 */
	protected final Set<ObjectName> registeredBeans = new LinkedHashSet<ObjectName>();

	/**
	 * The action take when registering an MBean and finding that it already exists.
	 * By default an exception is raised.
	 */
	private int registrationBehavior = REGISTRATION_FAIL_ON_EXISTING;


	/**
	 * Specify the <code>MBeanServer</code> instance with which all beans should
	 * be registered. The <code>MBeanExporter</code> will attempt to locate an
	 * existing <code>MBeanServer</code> if none is supplied.
	 */
	public void setServer(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Return the <code>MBeanServer</code> that the beans will be registered with.
	 */
	public final MBeanServer getServer() {
		return this.server;
	}

	/**
	 * Set the registration behavior by the name of the corresponding constant,
	 * e.g. "REGISTRATION_IGNORE_EXISTING".
	 * @see #setRegistrationBehavior
	 * @see #REGISTRATION_FAIL_ON_EXISTING
	 * @see #REGISTRATION_IGNORE_EXISTING
	 * @see #REGISTRATION_REPLACE_EXISTING
	 */
	public void setRegistrationBehaviorName(String registrationBehavior) {
		setRegistrationBehavior(constants.asNumber(registrationBehavior).intValue());
	}

	/**
	 * Specify  what action should be taken when attempting to register an MBean
	 * under an {@link javax.management.ObjectName} that already exists.
	 * <p>Default is REGISTRATION_FAIL_ON_EXISTING.
	 * @see #setRegistrationBehaviorName(String)
	 * @see #REGISTRATION_FAIL_ON_EXISTING
	 * @see #REGISTRATION_IGNORE_EXISTING
	 * @see #REGISTRATION_REPLACE_EXISTING
	 */
	public void setRegistrationBehavior(int registrationBehavior) {
		this.registrationBehavior = registrationBehavior;
	}


	/**
	 * Actually register the MBean with the server. The behavior when encountering
	 * an existing MBean can be configured using the {@link #setRegistrationBehavior(int)}
	 * and {@link #setRegistrationBehaviorName(String)} methods.
	 * @param mbean the MBean instance
	 * @param objectName the suggested ObjectName for the MBean
	 * @throws JMException if the registration failed
	 */
	protected void doRegister(Object mbean, ObjectName objectName) throws JMException {
		ObjectInstance registeredBean = null;
		try {
			registeredBean = this.server.registerMBean(mbean, objectName);
		}
		catch (InstanceAlreadyExistsException ex) {
			if (this.registrationBehavior == REGISTRATION_IGNORE_EXISTING) {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring existing MBean at [" + objectName + "]");
				}
			}
			else if (this.registrationBehavior == REGISTRATION_REPLACE_EXISTING) {
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Replacing existing MBean at [" + objectName + "]");
					}
					this.server.unregisterMBean(objectName);
					registeredBean = this.server.registerMBean(mbean, objectName);
				}
				catch (InstanceNotFoundException ex2) {
					logger.error("Unable to replace existing MBean at [" + objectName + "]", ex2);
					throw ex;
				}
			}
			else {
				throw ex;
			}
		}

		// Track registration and notify listeners.
		ObjectName actualObjectName = (registeredBean != null ? registeredBean.getObjectName() : null);
		if (actualObjectName == null) {
			actualObjectName = objectName;
		}
		this.registeredBeans.add(actualObjectName);
		onRegister(actualObjectName, mbean);
	}

	/**
	 * Unregisters all beans that have been registered by an instance of this class.
	 */
	protected void unregisterBeans() {
		for (ObjectName objectName : this.registeredBeans) {
			doUnregister(objectName);
		}
		this.registeredBeans.clear();
	}

	/**
	 * Actually unregister the specified MBean from the server.
	 * @param objectName the suggested ObjectName for the MBean
	 */
	protected void doUnregister(ObjectName objectName) {
		try {
			// MBean might already have been unregistered by an external process.
			if (this.server.isRegistered(objectName)) {
				this.server.unregisterMBean(objectName);
				onUnregister(objectName);
			}
			else {
				if (logger.isWarnEnabled()) {
					logger.warn("Could not unregister MBean [" + objectName + "] as said MBean " +
							"is not registered (perhaps already unregistered by an external process)");
				}
			}
		}
		catch (JMException ex) {
			if (logger.isErrorEnabled()) {
				logger.error("Could not unregister MBean [" + objectName + "]", ex);
			}
		}
	}

	/**
	 * Return the {@link ObjectName ObjectNames} of all registered beans.
	 */
	protected final ObjectName[] getRegisteredObjectNames() {
		return this.registeredBeans.toArray(new ObjectName[this.registeredBeans.size()]);
	}


	/**
	 * Called when an MBean is registered under the given {@link ObjectName}. Allows
	 * subclasses to perform additional processing when an MBean is registered.
	 * <p>The default implementation delegates to {@link #onRegister(ObjectName)}.
	 * @param objectName the actual {@link ObjectName} that the MBean was registered with
	 * @param mbean the registered MBean instance
	 */
	protected void onRegister(ObjectName objectName, Object mbean) {
		onRegister(objectName);
	}

	/**
	 * Called when an MBean is registered under the given {@link ObjectName}. Allows
	 * subclasses to perform additional processing when an MBean is registered.
	 * <p>The default implementation is empty. Can be overridden in subclasses.
	 * @param objectName the actual {@link ObjectName} that the MBean was registered with
	 */
	protected void onRegister(ObjectName objectName) {
	}

	/**
	 * Called when an MBean is unregistered under the given {@link ObjectName}. Allows
	 * subclasses to perform additional processing when an MBean is unregistered.
	 * <p>The default implementation is empty. Can be overridden in subclasses.
	 * @param objectName the {@link ObjectName} that the MBean was registered with
	 */
	protected void onUnregister(ObjectName objectName) {
	}

}
