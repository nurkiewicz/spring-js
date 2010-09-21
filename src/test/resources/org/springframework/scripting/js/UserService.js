importPackage(java.util);
importPackage(org.springframework.scripting.js);

var log = org.apache.commons.logging.LogFactory.getLog("org.springframework.scripting.js.JavaScriptUserService");

function validate(users) {
	log.debug("Validating: " + users);
	var entries = users.entrySet().toArray();
	var ids = [];
	for(entry in entries) {
		ids.push(entries[entry].key);
	}
	return new HashSet(Arrays.asList(ids));
}

function loadByIds(ids) {
	var result = new ArrayList();
	for(id in ids) {
		result.add(new User().withName(id));
	}
	return result;
}

function dump(user) {
	return "User[name=" + user.getName() + ", country=" + dumpCountry(user.getCountry()) + "]";
}

function dumpCountry(country) {
	return "Country[capital=" + country.getCapital() + "]";
}