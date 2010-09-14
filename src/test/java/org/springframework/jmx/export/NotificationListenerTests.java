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

package org.springframework.jmx.export;

import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.junit.Ignore;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jmx.AbstractMBeanServerTests;
import org.springframework.jmx.JmxTestBean;
import org.springframework.jmx.access.NotificationListenerRegistrar;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.jmx.support.ObjectNameManager;

/**
 * @author Rob Harrop
 * @author Mark Fisher
 */
@Ignore  // Getting CCEs regarding ObjectName being cast to String
public class NotificationListenerTests extends AbstractMBeanServerTests {

	public void testRegisterNotificationListenerForMBean() throws Exception {
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");
		JmxTestBean bean = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName, bean);

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();

		Map notificationListeners = new HashMap();
		notificationListeners.put(objectName, listener);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListenerMappings(notificationListeners);
		exporter.afterPropertiesSet();

		// update the attribute
		String attributeName = "Name";
		server.setAttribute(objectName, new Attribute(attributeName, "Rob Harrop"));
		assertEquals("Listener not notified", 1, listener.getCount(attributeName));
	}

	public void testRegisterNotificationListenerWithWildcard() throws Exception {
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");
		JmxTestBean bean = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName, bean);

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();

		Map notificationListeners = new HashMap();
		notificationListeners.put("*", listener);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListenerMappings(notificationListeners);
		exporter.afterPropertiesSet();

		// update the attribute
		String attributeName = "Name";
		server.setAttribute(objectName, new Attribute(attributeName, "Rob Harrop"));
		assertEquals("Listener not notified", 1, listener.getCount(attributeName));
	}

	public void testRegisterNotificationListenerWithHandback() throws Exception {
		String objectName = "spring:name=Test";
		JmxTestBean bean = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName, bean);

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();
		Object handback = new Object();

		NotificationListenerBean listenerBean = new NotificationListenerBean();
		listenerBean.setNotificationListener(listener);
		listenerBean.setMappedObjectName("spring:name=Test");
		listenerBean.setHandback(handback);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListeners(new NotificationListenerBean[]{listenerBean});
		exporter.afterPropertiesSet();

		// update the attribute
		String attributeName = "Name";
		server.setAttribute(ObjectNameManager.getInstance("spring:name=Test"), new Attribute(attributeName, "Rob Harrop"));

		assertEquals("Listener not notified", 1, listener.getCount(attributeName));
		assertEquals("Handback object not transmitted correctly", handback, listener.getLastHandback(attributeName));
	}

	public void testRegisterNotificationListenerForAllMBeans() throws Exception {
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");
		JmxTestBean bean = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName, bean);

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();

		NotificationListenerBean listenerBean = new NotificationListenerBean();
		listenerBean.setNotificationListener(listener);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListeners(new NotificationListenerBean[]{listenerBean});
		exporter.afterPropertiesSet();

		// update the attribute
		String attributeName = "Name";
		server.setAttribute(objectName, new Attribute(attributeName, "Rob Harrop"));

		assertEquals("Listener not notified", 1, listener.getCount(attributeName));
	}

	public void testRegisterNotificationListenerWithFilter() throws Exception {
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");
		JmxTestBean bean = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName, bean);

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();

		NotificationListenerBean listenerBean = new NotificationListenerBean();
		listenerBean.setNotificationListener(listener);
		listenerBean.setNotificationFilter(new NotificationFilter() {
			public boolean isNotificationEnabled(Notification notification) {
				if (notification instanceof AttributeChangeNotification) {
					AttributeChangeNotification changeNotification = (AttributeChangeNotification) notification;
					return "Name".equals(changeNotification.getAttributeName());
				}
				else {
					return false;
				}
			}
		});

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListeners(new NotificationListenerBean[]{listenerBean});
		exporter.afterPropertiesSet();

		// update the attributes
		String nameAttribute = "Name";
		String ageAttribute = "Age";

		server.setAttribute(objectName, new Attribute(nameAttribute, "Rob Harrop"));
		server.setAttribute(objectName, new Attribute(ageAttribute, new Integer(90)));

		assertEquals("Listener not notified for Name", 1, listener.getCount(nameAttribute));
		assertEquals("Listener incorrectly notified for Age", 0, listener.getCount(ageAttribute));
	}

	public void testCreationWithNoNotificationListenerSet() {
		try {
			new NotificationListenerBean().afterPropertiesSet();
			fail("Must have thrown an IllegalArgumentException (no NotificationListener supplied)");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testRegisterNotificationListenerWithBeanNameAndBeanNameInBeansMap() throws Exception {
		String beanName = "testBean";
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");

		SelfNamingTestBean testBean = new SelfNamingTestBean();
		testBean.setObjectName(objectName);

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerSingleton(beanName, testBean);

		Map beans = new HashMap();
		beans.put(beanName, beanName);

		Map listenerMappings = new HashMap();
		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();
		listenerMappings.put(beanName, listener);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListenerMappings(listenerMappings);
		exporter.setBeanFactory(factory);
		exporter.afterPropertiesSet();
		assertIsRegistered("Should have registered MBean", objectName);

		server.setAttribute(objectName, new Attribute("Age", new Integer(77)));
		assertEquals("Listener not notified", 1, listener.getCount("Age"));
	}

	public void testRegisterNotificationListenerWithBeanNameAndBeanInstanceInBeansMap() throws Exception {
		String beanName = "testBean";
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");

		SelfNamingTestBean testBean = new SelfNamingTestBean();
		testBean.setObjectName(objectName);

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerSingleton(beanName, testBean);

		Map beans = new HashMap();
		beans.put(beanName, testBean);

		Map listenerMappings = new HashMap();
		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();
		listenerMappings.put(beanName, listener);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListenerMappings(listenerMappings);
		exporter.setBeanFactory(factory);
		exporter.afterPropertiesSet();
		assertIsRegistered("Should have registered MBean", objectName);

		server.setAttribute(objectName, new Attribute("Age", new Integer(77)));
		assertEquals("Listener not notified", 1, listener.getCount("Age"));
	}

	public void testRegisterNotificationListenerWithBeanNameBeforeObjectNameMappedToSameBeanInstance() throws Exception {
		String beanName = "testBean";
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");

		SelfNamingTestBean testBean = new SelfNamingTestBean();
		testBean.setObjectName(objectName);

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerSingleton(beanName, testBean);

		Map beans = new HashMap();
		beans.put(beanName, testBean);

		Map listenerMappings = new HashMap();
		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();
		listenerMappings.put(beanName, listener);
		listenerMappings.put(objectName, listener);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListenerMappings(listenerMappings);
		exporter.setBeanFactory(factory);
		exporter.afterPropertiesSet();
		assertIsRegistered("Should have registered MBean", objectName);

		server.setAttribute(objectName, new Attribute("Age", new Integer(77)));
		assertEquals("Listener should have been notified exactly once", 1, listener.getCount("Age"));
	}

	public void testRegisterNotificationListenerWithObjectNameBeforeBeanNameMappedToSameBeanInstance() throws Exception {
		String beanName = "testBean";
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");

		SelfNamingTestBean testBean = new SelfNamingTestBean();
		testBean.setObjectName(objectName);

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerSingleton(beanName, testBean);

		Map beans = new HashMap();
		beans.put(beanName, testBean);

		Map listenerMappings = new HashMap();
		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();
		listenerMappings.put(objectName, listener);
		listenerMappings.put(beanName, listener);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListenerMappings(listenerMappings);
		exporter.setBeanFactory(factory);
		exporter.afterPropertiesSet();
		assertIsRegistered("Should have registered MBean", objectName);

		server.setAttribute(objectName, new Attribute("Age", new Integer(77)));
		assertEquals("Listener should have been notified exactly once", 1, listener.getCount("Age"));
	}

	public void testRegisterNotificationListenerWithTwoBeanNamesMappedToDifferentBeanInstances() throws Exception {
		String beanName1 = "testBean1";
		String beanName2 = "testBean2";

		ObjectName objectName1 = ObjectName.getInstance("spring:name=Test1");
		ObjectName objectName2 = ObjectName.getInstance("spring:name=Test2");

		SelfNamingTestBean testBean1 = new SelfNamingTestBean();
		testBean1.setObjectName(objectName1);

		SelfNamingTestBean testBean2 = new SelfNamingTestBean();
		testBean2.setObjectName(objectName2);

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerSingleton(beanName1, testBean1);
		factory.registerSingleton(beanName2, testBean2);

		Map beans = new HashMap();
		beans.put(beanName1, testBean1);
		beans.put(beanName2, testBean2);

		Map listenerMappings = new HashMap();
		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();
		listenerMappings.put(beanName1, listener);
		listenerMappings.put(beanName2, listener);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.setNotificationListenerMappings(listenerMappings);
		exporter.setBeanFactory(factory);
		exporter.afterPropertiesSet();
		assertIsRegistered("Should have registered MBean", objectName1);
		assertIsRegistered("Should have registered MBean", objectName2);

		server.setAttribute(ObjectNameManager.getInstance(objectName1), new Attribute("Age", new Integer(77)));
		assertEquals("Listener not notified for testBean1", 1, listener.getCount("Age"));

		server.setAttribute(ObjectNameManager.getInstance(objectName2), new Attribute("Age", new Integer(33)));
		assertEquals("Listener not notified for testBean2", 2, listener.getCount("Age"));
	}

	public void testNotificationListenerRegistrar() throws Exception {
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");
		JmxTestBean bean = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName, bean);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.afterPropertiesSet();

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();

		NotificationListenerRegistrar registrar = new NotificationListenerRegistrar();
		registrar.setServer(server);
		registrar.setNotificationListener(listener);
		registrar.setMappedObjectName(objectName);
		registrar.afterPropertiesSet();

		// update the attribute
		String attributeName = "Name";
		server.setAttribute(objectName, new Attribute(attributeName, "Rob Harrop"));
		assertEquals("Listener not notified", 1, listener.getCount(attributeName));

		registrar.destroy();

		// try to update the attribute again
		server.setAttribute(objectName, new Attribute(attributeName, "Rob Harrop"));
		assertEquals("Listener notified after destruction", 1, listener.getCount(attributeName));
	}

	public void testNotificationListenerRegistrarWithMultipleNames() throws Exception {
		ObjectName objectName = ObjectName.getInstance("spring:name=Test");
		ObjectName objectName2 = ObjectName.getInstance("spring:name=Test2");
		JmxTestBean bean = new JmxTestBean();
		JmxTestBean bean2 = new JmxTestBean();

		Map beans = new HashMap();
		beans.put(objectName, bean);
		beans.put(objectName2, bean2);

		MBeanExporter exporter = new MBeanExporter();
		exporter.setServer(server);
		exporter.setBeans(beans);
		exporter.afterPropertiesSet();

		CountingAttributeChangeNotificationListener listener = new CountingAttributeChangeNotificationListener();

		NotificationListenerRegistrar registrar = new NotificationListenerRegistrar();
		registrar.setServer(server);
		registrar.setNotificationListener(listener);
		//registrar.setMappedObjectNames(new Object[] {objectName, objectName2});
		registrar.setMappedObjectNames(new String[] {"spring:name=Test", "spring:name=Test2"});
		registrar.afterPropertiesSet();

		// update the attribute
		String attributeName = "Name";
		server.setAttribute(objectName, new Attribute(attributeName, "Rob Harrop"));
		assertEquals("Listener not notified", 1, listener.getCount(attributeName));

		registrar.destroy();

		// try to update the attribute again
		server.setAttribute(objectName, new Attribute(attributeName, "Rob Harrop"));
		assertEquals("Listener notified after destruction", 1, listener.getCount(attributeName));
	}


	private static class CountingAttributeChangeNotificationListener implements NotificationListener {

		private Map attributeCounts = new HashMap();

		private Map attributeHandbacks = new HashMap();

		public void handleNotification(Notification notification, Object handback) {
			if (notification instanceof AttributeChangeNotification) {
				AttributeChangeNotification attNotification = (AttributeChangeNotification) notification;
				String attributeName = attNotification.getAttributeName();

				Integer currentCount = (Integer) this.attributeCounts.get(attributeName);

				if (currentCount != null) {
					int count = currentCount.intValue() + 1;
					this.attributeCounts.put(attributeName, new Integer(count));
				}
				else {
					this.attributeCounts.put(attributeName, new Integer(1));
				}

				this.attributeHandbacks.put(attributeName, handback);
			}
		}

		public int getCount(String attribute) {
			Integer count = (Integer) this.attributeCounts.get(attribute);
			return (count == null) ? 0 : count.intValue();
		}

		public Object getLastHandback(String attributeName) {
			return this.attributeHandbacks.get(attributeName);
		}
	}


	public static class SelfNamingTestBean implements SelfNaming {

		private ObjectName objectName;

		private int age;

		public void setObjectName(ObjectName objectName) {
			this.objectName = objectName;
		}

		public ObjectName getObjectName() throws MalformedObjectNameException {
			return this.objectName;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public int getAge() {
			return this.age;
		}
	}

}
