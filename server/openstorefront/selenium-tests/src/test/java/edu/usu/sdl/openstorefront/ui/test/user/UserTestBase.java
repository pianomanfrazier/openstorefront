/*
 * Copyright 2017 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.ui.test.user;

import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.ui.test.BrowserTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author dshurtleff
 */
public class UserTestBase
		extends BrowserTestBase
{

	String prevApprovalState = null;
	
	@BeforeClass
	public static void setupBaseTest()
	{
		login();
	}
	
	public void setUserAutoApprove()
	{
		
	}

	protected static Component createUserComponent(String componentName)
	{
		Component myEntry = apiClient.getComponentRESTTestClient().createAPIComponent(componentName);
		System.out.println("Entry name: " + myEntry.getName());
		
		return myEntry;
	}

	@AfterClass
	public static void cleanupBase()
	{
		
	}

}
