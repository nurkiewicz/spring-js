/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.core.CollectionFactory;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class CollectingReaderEventListener implements ReaderEventListener {

	private final List defaults = new LinkedList();

	private final Map componentDefinitions = CollectionFactory.createLinkedMapIfPossible(8);

	private final Map aliasMap = CollectionFactory.createLinkedMapIfPossible(8);

	private final List imports = new LinkedList();


	public void defaultsRegistered(DefaultsDefinition defaultsDefinition) {
		this.defaults.add(defaultsDefinition);
	}

	public List getDefaults() {
		return Collections.unmodifiableList(this.defaults);
	}

	public void componentRegistered(ComponentDefinition componentDefinition) {
		this.componentDefinitions.put(componentDefinition.getName(), componentDefinition);
	}

	public ComponentDefinition getComponentDefinition(String name) {
		return (ComponentDefinition) this.componentDefinitions.get(name);
	}

	public ComponentDefinition[] getComponentDefinitions() {
		Collection collection = this.componentDefinitions.values();
		return (ComponentDefinition[]) collection.toArray(new ComponentDefinition[collection.size()]);
	}

	public void aliasRegistered(AliasDefinition aliasDefinition) {
		List aliases = (List) this.aliasMap.get(aliasDefinition.getBeanName());
		if(aliases == null) {
			aliases = new ArrayList();
			this.aliasMap.put(aliasDefinition.getBeanName(), aliases);
		}
		aliases.add(aliasDefinition);
	}

	public List getAliases(String beanName) {
		List aliases = (List) this.aliasMap.get(beanName);
		return aliases == null ? null : Collections.unmodifiableList(aliases);
	}

	public void importProcessed(ImportDefinition importDefinition) {
		this.imports.add(importDefinition);
	}

	public List getImports() {
		return Collections.unmodifiableList(this.imports);
	}

}
