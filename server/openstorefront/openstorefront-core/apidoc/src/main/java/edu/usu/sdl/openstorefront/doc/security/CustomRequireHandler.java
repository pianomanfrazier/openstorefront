/*
 * Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.doc.security;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;

/**
 * Allows for custom security checking
 *
 * @author dshurtleff
 */
public interface CustomRequireHandler
{
	/**
	 * Get the description of the security check for the API doc
	 * @return
	 */
	String getDescription();

	/**
	 * Provide a special handling for complex security cases.
	 * @param resourceInfo
	 * @param requestContext
	 * @param requireSecurity
	 * @return true to proceed checking or false to indicated failed security check
	 */
	boolean specialSecurityCheck(ResourceInfo resourceInfo, ContainerRequestContext requestContext, RequireSecurity requireSecurity);
	
}
