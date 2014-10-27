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
package edu.usu.sdl.openstorefront.service.io.integration;

import edu.usu.sdl.openstorefront.service.ServiceProxy;
import edu.usu.sdl.openstorefront.storage.model.IntegrationConfig;
import edu.usu.sdl.openstorefront.storage.model.IntegrationType;

/**
 * Base Integration Handler
 *
 * @author dshurtleff
 */
public abstract class BaseIntegrationHandler
{

	protected final IntegrationConfig integrationConfig;
	protected ServiceProxy serviceProxy = new ServiceProxy();

	public BaseIntegrationHandler(IntegrationConfig integrationConfig)
	{
		this.integrationConfig = integrationConfig;
	}

	public static BaseIntegrationHandler getIntegrationHandler(IntegrationConfig integrationConfig)
	{
		BaseIntegrationHandler baseIntegration = null;
		switch (integrationConfig.getIntegrationType()) {
			case IntegrationType.JIRA:
				baseIntegration = new JiraIntegrationHandler(integrationConfig);
				break;
		}
		return baseIntegration;
	}

	public abstract void processConfig();
}
