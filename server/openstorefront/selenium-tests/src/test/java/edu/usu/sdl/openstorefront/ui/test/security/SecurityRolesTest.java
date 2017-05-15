package edu.usu.sdl.openstorefront.ui.test.security;

import edu.usu.sdl.openstorefront.ui.test.BrowserTestBase;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

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

/**
 *
 * @author besplin
 */

public class SecurityRolesTest 
		extends BrowserTestBase
{
    private static final Logger LOG = Logger.getLogger(BrowserTestBase.class.getName());
    
    @BeforeClass
    public static void setupTest() throws InterruptedException
	{
		login();
    }
		
	@Test
	public void signupForAccounts() throws InterruptedException{
	//	AccountSignupActivateTest newAccountSignup = new AccountSignupActivateTest();		
	//	newAccountSignup.signupActivate("autoUser");
	//	newAccountSignup.signupActivate("autoEval");
	//	newAccountSignup.signupActivate("autoAdmin");
	//	newAccountSignup.signupActivate("autoLibrarian");
	}
	
	@Test
    public void SecurityRole () throws InterruptedException {
		NewSecurityRole newSecurityRole = new NewSecurityRole();
		newSecurityRole.addRole("AUTO-User","autoUser");
    }

	@Test
	public void setSecurityRoles () {
		
	}
	
    @Test
    public void importDataRestrictionEntries () {
    
    }
	
	@Test
	public void verifySecurityPermissions () {
		
	}

}
