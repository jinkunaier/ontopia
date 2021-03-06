/*
 * #!
 * Ontopia Rest
 * #-
 * Copyright (C) 2001 - 2016 The Ontopia Project
 * #-
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
 * !#
 */

package net.ontopia.topicmaps.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssociationRole extends Reifiable {

	private Association association;
	private Topic player;
	private Topic type;

	public AssociationRole() {
	}

	public AssociationRole(String objectId) {
		super(objectId);
	}

	public Association getAssociation() {
		return association;
	}

	public Topic getPlayer() {
		return player;
	}

	public Topic getType() {
		return type;
	}

	public void setPlayer(Topic player) {
		this.player = player;
	}

	public void setType(Topic type) {
		this.type = type;
	}

	public void setAssociation(Association association) {
		this.association = association;
	}
}
