package org.springframework.scripting.js;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-09-18, 21:10:12
 */
public interface HelloService {

	String hello(String name);

	String helloParameterized(String name, Date effectiveDate, int age, Locale locale);

	String helloToUsers(Collection<User> users);
}
