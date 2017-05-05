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
package edu.usu.sdl.openstorefront.ui.test;

import edu.usu.sdl.openstorefront.ui.test.admin.AdminTestSuite;
import edu.usu.sdl.openstorefront.ui.test.evaluator.EvaluatorTestSuite;
import edu.usu.sdl.openstorefront.ui.test.search.SearchTestSuite;
import edu.usu.sdl.openstorefront.ui.test.security.SecurityTestSuite;
import edu.usu.sdl.openstorefront.ui.test.user.UserToolTestSuite;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author dshurtleff
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
 // AdminTestSuite.class,
 // EvaluatorTestSuite.class,
 // SearchTestSuite.class,
    SecurityTestSuite.class,
 // UserToolTestSuite.class
})
public class MasterTestSuite
{

	@AfterClass
	public void generateReport()
	{
		//TODO: generate Report based on test results
	}

}
