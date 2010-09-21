package org.springframework.scripting.js;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-09-21, 19:35:37
 */
public interface UserService {

	Set<Integer> validate(Map<Integer, User> users);

	List<User> loadByIds(int[] ids);

	String dump(User user);

}
