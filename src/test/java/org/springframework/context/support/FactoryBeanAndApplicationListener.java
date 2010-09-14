package org.springframework.context.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author Juergen Hoeller
 * @since 06.10.2004
 */
public class FactoryBeanAndApplicationListener implements FactoryBean, ApplicationListener {

	public Object getObject() throws Exception {
		return "";
	}

	public Class getObjectType() {
		return String.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void onApplicationEvent(ApplicationEvent event) {
	}

}
