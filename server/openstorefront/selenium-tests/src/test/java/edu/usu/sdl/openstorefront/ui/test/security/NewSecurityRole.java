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
package edu.usu.sdl.openstorefront.ui.test.security;

import edu.usu.sdl.openstorefront.ui.test.BrowserTestBase;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author besplin
 */
public class NewSecurityRole
		extends SecurityTestBase
{

	private static final Logger LOG = Logger.getLogger(BrowserTestBase.class.getName());
	private int dragAndDropActionSleepTime = 75;  // *** In milliseconds ***

	@BeforeClass
	public static void setupTest()
	{
		login();
	}

	public NewSecurityRole()
	{

	}

	// Delete if active
	public void deleteRoleIfPresent(WebDriver driver, String roleName) throws InterruptedException
	{
		driver.get(webDriverUtil.getPage("AdminTool.action?load=Security-Roles"));
		//driver.navigate().refresh();
		sleep(1500);
		// Click on Table Row Col containing roleName
		if (tableClickRowCol("[data-test='securityRolesTable'] .x-grid-view", roleName, driver, 0)) {
			driver.findElement(By.xpath("//span[contains(.,'Delete')]")).click();
			sleep(750);
			driver.findElement(By.xpath("//span[contains(.,'Confirm')]")).click();
			sleep(500);

			// *** FIXES detached from page error message ***  TODO: Put in a seperate method if used again?
			boolean breakIt = true;
			while (true) {
				breakIt = true;
				try {
					sleep(500);
					// Command that was erring out (detached) earlier.
					tableClickRowCol("[data-test='securityRolesTable'] .x-grid-view", roleName, driver, 0);
					LOG.log(Level.INFO, "--- Waiting for element to show up so that it is not detached ---");
				} catch (Exception e) {
					if (e.getMessage().contains("element is not attached")) {
						breakIt = false;
					}
				}
				if (breakIt) {
					LOG.log(Level.INFO, "--- Successfully waited for detached element to show up, continuing on now --");
					break;
				}
			}

			// Check to ensure deletion
			if (tableClickRowCol("[data-test='securityRolesTable'] .x-grid-view", roleName, driver, 0)) {
				LOG.log(Level.WARNING, "*** Could NOT delete  '" + roleName + "' ***");
			} else {
				LOG.log(Level.INFO, "--- Old Security Role '" + roleName + "' DELETED ---");
				sleep(1000);
			}
		} else {
			LOG.log(Level.INFO, "--- Old (Deleted) Security Role " + roleName + " was not found. ---");
		}

	}

	public void addRoleBasic(WebDriver driver, String roleName) throws InterruptedException
	{
		// ********* OPTIMIZE with WebDriverWAIT *************************
		// ********** ADD WAY TO CHECK AND UNCHECK ******************

		driver.get(webDriverUtil.getPage("AdminTool.action?load=Security-Roles"));
		//driver.navigate().refresh();
		sleep(1500);
		driver.findElement(By.xpath("//span[contains(.,'Add')]")).click();
		sleep(1250); // Give box time to come up
		driver.findElement(By.xpath("//input[contains(@name,'roleName')]")).sendKeys(roleName);
		driver.findElement(By.xpath("//input[@name='description']")).sendKeys("Created by automation");
		driver.findElement(By.xpath("//input[@name='landingPage']")).sendKeys("/");
		sleep(500);
		driver.findElement(By.xpath("//span[contains(.,'Save')]")).click();
		sleep(2000);
		if (tableClickRowCol("[data-test='securityRolesTable'] .x-grid-view", roleName, driver, 0)) {
			LOG.log(Level.INFO, "--- Added the role " + roleName + ". ---");
		} else {
			LOG.log(Level.WARNING, "*** Could NOT ADD the role " + roleName + ". ***");
		}
	}

	public void addUserToRole(WebDriver driver, String roleName, String userName) throws InterruptedException
	{
		driver.get(webDriverUtil.getPage("AdminTool.action?load=Security-Roles"));
		//driver.navigate().refresh();
		sleep(1500);
		if (tableClickRowCol("[data-test='securityRolesTable'] .x-grid-view", roleName, driver, 0)) {
			driver.findElement(By.xpath("//span[contains(.,'Manage Users')]")).click();
			sleep(250); //Users with xxxx role is now up

			// Start typing in the Add User drop down box, then hit ENTER
			driver.findElement(By.xpath("//input[contains(@name,'username')]")).sendKeys(userName);
			sleep(1000);
			driver.findElement(By.xpath("//input[contains(@name,'username')]")).sendKeys(Keys.ENTER);
			sleep(1250);
			
			// TODO:  What if the user has not been created?  Error handling here.

			// Hit add button (when active?)
			driver.findElement(By.xpath("//span[@class='x-btn-button x-btn-button-default-toolbar-small x-btn-text  x-btn-icon x-btn-icon-left x-btn-button-center ']")).click();
			sleep(1250);

			// Verify it is in the list of added usernames
			boolean wasAdded = driver.findElement(By.xpath("//div[contains(.,'" + userName.toLowerCase() + "')]")).isDisplayed();
			if (wasAdded) {
				LOG.log(Level.INFO, "--- Successfully added " + userName.toLowerCase() + " to " + roleName + " role. ---");
			} else {
				LOG.log(Level.SEVERE, "!!!!! Could not add the user " + userName.toLowerCase() + " to the role " + roleName + " !!!!!");
			}
		} else {
			LOG.log(Level.SEVERE, "!!!!! Could not find the role " + roleName + " in order to add a managed user. !!!!!");
		}
	}

	public void deleteUserFromRole(WebDriver driver, String roleName, String userName) throws InterruptedException
	{
		// TODO
	}

	public void setPermissions(WebDriver driver, String roleName, Map<String, Boolean> permissions) throws InterruptedException
	{
		// Go to Security Role page, wait for the table to load.
		driver.get(webDriverUtil.getPage("AdminTool.action?load=Security-Roles"));
		WebDriverWait waitForTableLoad = new WebDriverWait(driver, 5);
		waitForTableLoad.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='securityRolesTable'] .x-grid-view")));

		// Click on the roleName (like AUTO-user) and then Manage Permissions		
		if (tableClickRowCol("[data-test='securityRolesTable'] .x-grid-view", roleName, driver, 0)) {
			driver.findElement(By.xpath("//span[contains(.,'Manage Permissions')]")).click();

			// Get "permissAvailDivLeft" which is the table on the LEFT, used for the destination of drag-and-drop
			WebDriverWait waitPermissAvailDivLeft = new WebDriverWait(driver, 10);
			WebElement permissAvailDivLeft = waitPermissAvailDivLeft.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='permissionsAvailableTable'] .x-panel-body")));

			// Get the table on the RIGHT used for the destination of drag-and-drop
			WebDriverWait waitCurrentRoleDivRight = new WebDriverWait(driver, 10);
			WebElement currentRoleDivRight = waitCurrentRoleDivRight.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='currentRolePermissionsTable'] .x-panel-body")));

			// *** Move LEFT TO RIGHT (Permissions Available -> Current Role Permissions IF permissions in the HashMap is true) ***
			try {  // avoid empty table error with catch below
				// Get list of Codes on the LEFT, use the autoEl tag from roleMangement.jsp and odd <tr> from the table to get Codes
				List<WebElement> permissionsAvailableList
						= driver.findElements(By.cssSelector("[data-test='permissionsAvailableTable'] .x-panel-body tr td:nth-child(odd)"));

				// Loop through Items in permissions AvailableList
				for (WebElement permissionsAvailableItem : permissionsAvailableList) {
					// System.out.println(permissAvailableItem.getText() + " is in PEMISSIONS AVAILABLE list in the table");

					// Loop through the permissions Hashmap in order to set the permissions
					for (String permissionToSet : permissions.keySet()) {
						// If the permissionToSet is equal to what was found in the LEFT table
						if (permissionToSet.equals(permissionsAvailableItem.getText())) {
							//	System.out.println("--- When looping through the LEFT table '" + permissionsAvailableItem.getText() 
							//			+ "' was found, which matches a setting '" + permissionToSet + "' ---");

							// If this Code or item is set to TRUE in the hashmap (meaning it should MOVE to the right)
							if (permissions.get(permissionToSet)) {
								// Move it to the right 
								System.out.println("--- '" + permissionToSet + "' *** READY TO BE MOVED to the RIGHT ***");
								WebDriverWait waitPermission = new WebDriverWait(driver, 5);
								WebElement permissionTableLeft
										= waitPermission.until(ExpectedConditions.elementToBeClickable(permissionsAvailableItem));
								Actions builder = new Actions(driver);
								builder.moveToElement(permissionTableLeft).perform();
								sleep(dragAndDropActionSleepTime);
								builder.dragAndDrop(permissionTableLeft, currentRoleDivRight);
								sleep(dragAndDropActionSleepTime);
								builder.perform();
								sleep(dragAndDropActionSleepTime);
								// LOG.log(Level.INFO, "\n--- MOVED '" + permissionToSet + "' to the CURRENT ROLE PERMISSIONS column, for \n'"
								//		+ roleName + "', per permissions.put in method 'setSecurityRoles' in SecurityRolesTest.java ---");
								break;
							} else {
								System.out.println("--- '" + permissionsAvailableItem.getText() + "' is 'false' so it should remain "
										+ "on the LEFT (Permissions Available) [NOT MOVED] ---");
							}
						}
					}
				}
			} catch (Exception e) {
				System.out.println(e + " *** Nothing in the LEFT-hand list (to move to the RIGHT), moving on... ***");
			}

			// *** Move RIGHT TO LEFT (Permissions Available <-- Current Role Permissions, IF permissions in the HashMap is 'false') ***
			try {  // avoid empty table error with catch below
				// Get list of Codes on the RIGHT, use the autoEl tag from roleMangement.jsp and odd <tr> from the table to get Codes
				List<WebElement> currentRolePermissionsList
						= driver.findElements(By.cssSelector("[data-test='currentRolePermissionsTable'] .x-panel-body tr td:nth-child(odd)"));

				// Loop through Items in Current Role Permissions (RIGHT) List
				for (WebElement currentRolePermissionItem : currentRolePermissionsList) {
					// System.out.println(currentRolePermissionItem.getText() + " is in Current Role Permissions list in the table");

					// Loop through the permissions Hashmap in order to set the permissions
					for (String permissionToSet : permissions.keySet()) {
						// If the permissionToSet is equal to what was found in the RIGHT table
						if (permissionToSet.equals(currentRolePermissionItem.getText())) {
							//	System.out.println("--- When looping through the RIGHT table '" + currentRolePermissionItem.getText() 
							//			+ "' was found, which matches a setting '" + permissionToSet + "' ---");

							// If this Code or item is set to FALSE (!permissions.get) in the hashmap (meaning it should MOVE to the LEFT)
							if (!permissions.get(permissionToSet)) {
								// Move it to the Left 
								System.out.println("--- '" + permissionToSet + "' *** READY TO BE MOVED to the LEFT ***");
								WebDriverWait waitPermission = new WebDriverWait(driver, 5);
								WebElement permissionTableRight
										= waitPermission.until(ExpectedConditions.elementToBeClickable(currentRolePermissionItem));
								Actions builder = new Actions(driver);
								builder.moveToElement(permissionTableRight).perform();
								sleep(dragAndDropActionSleepTime);
								builder.dragAndDrop(permissionTableRight, permissAvailDivLeft);
								sleep(dragAndDropActionSleepTime);
								builder.perform();
								sleep(dragAndDropActionSleepTime);
								// LOG.log(Level.INFO, "\n--- MOVED '" + permissionToSet + "' to the PERMISSIONS AVAILABLE column, for \n'"
								//		+ roleName + "', per permissions.put in method 'setSecurityRoles' in SecurityRolesTest.java ---");
								break;
							} else {
								System.out.println("--- '" + currentRolePermissionItem.getText() + "' is 'TRUE' so it should remain "
										+ "on the RIGHT (Current Role Permissions) *NOT MOVED* ---");
							}
						}
					}
				}
			} catch (Exception e) {
				LOG.log(Level.WARNING, " *** Nothing in the RIGHT-hand list (to move to the LEFT), moving on... ***\n" + e);
				System.out.println(" *** Nothing in the RIGHT-hand list (to move to the LEFT), moving on... ***\n" + e);
			}

			// Hit SAVE! (Permissions)
			driver.findElement(By.cssSelector(".x-window.x-layer .x-btn.x-box-item.x-toolbar-item")).click();
			WebDriverWait waitTableUnderneath = new WebDriverWait(driver, 5);
			// Look for the mask of "Loading..." to be gone after save, SLOWS it down (invisibility) 

			// Wait until table is visible underneath
			waitTableUnderneath.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='securityRolesTable'] .x-grid-view")));
			LOG.log(Level.INFO, "--- SAVED Permissions Settings for: " + roleName + "---");

		} else {
			LOG.log(Level.SEVERE, "!!!!! Could NOT find the Security Role '" + roleName + "'.  !!!!!"
					+ "\n !!!!! Please ensure the role is created using a call to 'addSecurityRole' !!!!!");
		}
	}

	// True in boolean means to add it to the accessible side, false means to remove it.
	public void setDataSources(WebDriver driver, String roleName, Map<String, Boolean> dataSource) throws InterruptedException
	{
		driver.get(webDriverUtil.getPage("AdminTool.action?load=Security-Roles"));
		WebDriverWait waitForTableLoad = new WebDriverWait(driver, 5);
		waitForTableLoad.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='securityRolesTable'] .x-grid-view")));

		if (tableClickRowCol("[data-test='securityRolesTable'] .x-grid-view", roleName, driver, 0)) {
			driver.findElement(By.xpath("//span[contains(.,'Manage Data Restrictions')]")).click();

			// Wait for Data Restrictions box to come up
			WebDriverWait waitRestrictionsBox = new WebDriverWait(driver, 10);
			waitRestrictionsBox.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".x-window.x-layer")));

			// Click on Data Sources tab
			List<WebElement> dataTabs = driver.findElements(By.cssSelector(".x-tab.x-unselectable.x-box-item.x-tab-default.x-top.x-tab-top.x-tab-default-top"));
			for (WebElement tab : dataTabs) {
				if (tab.getText().equals("Data Sources")) {
					tab.click();
				}
			}

			try {
				// Get list of Codes in the Restricted (Left) side.  Get table for Accessible (Right) side
				List<WebElement> restrictedList = driver.findElements(By.cssSelector("#dataSourcesGrid-body td:nth-child(odd)"));
				WebElement accessibleTable = driver.findElement(By.cssSelector("#dataSourcesInRoleGrid-body"));

				// Loop through restrictedList 
				for (WebElement restrictedItem : restrictedList) {
					//	System.out.println(restrictedItem.getText() + " is in RESTRICTED list in the table");

					// Loop through the user set data Hashmap
					for (String dataSourceToSet : dataSource.keySet()) {
						//	System.out.println(dataSourceToSet.toString() + " trying to match what is in the table to this that the user inputted.");

						// If the User setting equals the setting in the Restricted List
						if (dataSourceToSet.equals(restrictedItem.getText())) {
							// If it is supposed to be in Accessible or TRUE in hashmap
							if (dataSource.get(dataSourceToSet)) {
								// Move it to the right (Restricted to Accessible)
								System.out.println(dataSourceToSet + " *** READY TO BE MOVED to the RIGHT ***");

								Actions builder = new Actions(driver);
								builder.moveToElement(restrictedItem).perform();
								sleep(dragAndDropActionSleepTime);
								builder.dragAndDrop(restrictedItem, accessibleTable);
								sleep(dragAndDropActionSleepTime);
								builder.perform();
								sleep(dragAndDropActionSleepTime);

								// (new Actions(driver)).dragAndDrop(restrictedItem, accessibleTable).perform();
								// LOG.log(Level.INFO, "--- MOVED '" + dataSourceToSet + "' to the ACCESSIBLE Data Sources column, Security Role '" + roleName + "', per dataSource.put in method 'setSecurityRoles' in SecurityRolesTest.java ---");
								break;
							} else {
								System.out.println("--- '" + dataSourceToSet + "' is 'FALSE' so it should remain "
										+ "on the LEFT (Restricted) *NOT MOVED* ---");
							}
						}
					}
				}
			} catch (Exception e) {
				System.out.println(" *** Nothing in the LEFT-hand list (to move to the LEFT), moving on... ***\n" + e);
			}

			try {
				// Get list of Codes in the Accessible (Right) side.  Get table for Restricted (Left) side
				List<WebElement> accessibleList = driver.findElements(By.cssSelector("#dataSourcesInRoleGrid-body td:nth-child(odd)"));
				WebElement restrictedTable = driver.findElement(By.cssSelector("#dataSourcesGrid-body"));

				// Loop through accessibleList
				for (WebElement accessibleItem : accessibleList) {
					// System.out.println(accessibleItem.getText() + " is in ACCESSIBLE list in the table");

					// Loop through the user set data Hashmap
					for (String dataSourceToSet : dataSource.keySet()) {
						//	System.out.println(dataSourceToSet.toString() + " trying to match what is in the table to this that the user inputted.");

						// If the User setting equals the setting in the Restricted List
						if (dataSourceToSet.equals(accessibleItem.getText())) {
							// If it is supposed to be in Restricted or FALSE in hashmap
							if (!dataSource.get(dataSourceToSet)) {
								// Move it to the right (Restricted to Accessible)
								System.out.println(dataSourceToSet + " *** READY TO BE MOVED to the RIGHT***");
								Actions builder = new Actions(driver);
								builder.moveToElement(accessibleItem).perform();
								sleep(dragAndDropActionSleepTime);
								builder.dragAndDrop(accessibleItem, restrictedTable);
								sleep(dragAndDropActionSleepTime);
								builder.perform();
								sleep(dragAndDropActionSleepTime);
								// (new Actions(driver)).dragAndDrop(accessibleItem, restrictedTable).perform();
								// LOG.log(Level.INFO, "--- MOVED '" + dataSourceToSet + "' to the RESTRICTED Data Sources column, Security Role '" + roleName + "', per dataSource.put in method 'setSecurityRoles' in SecurityRolesTest.java ---");
								break;
							} else {
								System.out.println("--- '" + dataSourceToSet + "' is 'TRUE' so it should remain "
										+ "on the RIGHT (Accessible) *NOT MOVED* ---");
							}
						}
					}
				}
			} catch (Exception e) {
				System.out.println(" *** Nothing in the RIGHT-hand list (to move to the LEFT), moving on... ***\n" + e);
			}

			// Hit SAVE! (Data Restrictions)
			driver.findElement(By.cssSelector(".x-window .x-btn.x-box-item.x-toolbar-item")).click();
			// wait for table underneath to be visible again
			WebDriverWait waitTableUnderneath = new WebDriverWait(driver, 5);
			waitTableUnderneath.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='securityRolesTable'] .x-grid-view")));
			LOG.log(Level.INFO, "--- SAVED Data Restrictions (Data Sources & Data Sourceitivity) Settings ---");

		} else {
			LOG.log(Level.SEVERE, "!!!!! Could NOT find the Security Role '" + roleName + "'.  !!!!!"
					+ "\n !!!!! Please ensure the role is created using a call to 'addSecurityRole' !!!!!");
		}
	}

	// Feed in a <List> of Data Distributions to Activate
	public void setDataSensitivity(WebDriver driver, String roleName, Map<String, Boolean> dataSens) throws InterruptedException
	{
		driver.get(webDriverUtil.getPage("AdminTool.action?load=Security-Roles"));
		WebDriverWait waitForTableLoad = new WebDriverWait(driver, 5);
		waitForTableLoad.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='securityRolesTable'] .x-grid-view")));

		if (tableClickRowCol("[data-test='securityRolesTable'] .x-grid-view", roleName, driver, 0)) {
			driver.findElement(By.xpath("//span[contains(.,'Manage Data Restrictions')]")).click();

			// Wait for Data Restrictions box to come up
			WebDriverWait waitBox = new WebDriverWait(driver, 10);
			waitBox.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".x-window.x-layer")));

			try {
				// Click on Data Sensitvity tab
				List<WebElement> dataTabs = driver.findElements(By.cssSelector(".x-tab.x-unselectable.x-box-item.x-tab-default.x-top.x-tab-top.x-tab-default-top"));
				for (WebElement tab : dataTabs) {
					if (tab.getText().equals("Data Sensitivity")) {
						tab.click();
					}
				}

				// Get list of Codes in the Restricted (Left) side.  Get table for Accessible (Right) side
				List<WebElement> restrictedList = driver.findElements(By.cssSelector("#dataSensitivityGrid-body td:nth-child(odd)"));
				WebElement accessibleTable = driver.findElement(By.cssSelector("#dataSensitivitiesInRoleGrid-body"));

				// Loop through restrictedList 
				for (WebElement restrictedItem : restrictedList) {
					// System.out.println(restrictedItem.getText() + " is in RESTRICTED list in the table");

					// Loop through the user set data Hashmap
					for (String dataSensitvToSet : dataSens.keySet()) {
						// System.out.println(dataSensitvToSet.toString() + " trying to match what is in the table to this that the user inputted.");
						// If the User setting equals the setting in the Restricted List
						if (dataSensitvToSet.equals(restrictedItem.getText())) {
							// If it is supposed to be in Accessible or TRUE in hashmap
							if (dataSens.get(dataSensitvToSet)) {
								// Move it to the right (Restricted to Accessible)
								System.out.println(dataSensitvToSet + " *** READY TO BE MOVED to the RIGHT ***");

								Actions builder = new Actions(driver);
								builder.moveToElement(restrictedItem).perform();
								sleep(dragAndDropActionSleepTime);
								builder.dragAndDrop(restrictedItem, accessibleTable);
								sleep(dragAndDropActionSleepTime);
								builder.perform();
								sleep(dragAndDropActionSleepTime);
								//	(new Actions(driver)).dragAndDrop(restrictedItem, accessibleTable).perform();
								// LOG.log(Level.INFO, "--- MOVED '" + dataSensitvToSet + "' to the ACCESSIBLE Data Sensitivity column, Security Role '" + roleName + "', per dataSens.put in method 'setSecurityRoles' in SecurityRolesTest.java ---");
								break;
							} else {
								System.out.println("--- '" + dataSensitvToSet + "' is 'FALSE' so it should remain "
										+ "on the LEFT (Restricted) *NOT MOVED* ---");
							}
						}
					}
				}
			} catch (Exception e) {
				System.out.println(" *** Nothing in the LEFT-hand list (to move to the RIGHT), moving on... ***\n" + e);
			}

			try {
				// Get list of Codes in the Accessible (Right) side.  Get table for Restricted (Left) side
				List<WebElement> accessibleList = driver.findElements(By.cssSelector("#dataSensitivitiesInRoleGrid-body td:nth-child(odd)"));
				WebElement restrictedTable = driver.findElement(By.cssSelector("#dataSensitivityGrid-body"));

				// Loop through accessibleList
				for (WebElement accessibleItem : accessibleList) {
					// System.out.println(accessibleItem.getText() + " is in ACCESSIBLE list in the table");

					// Loop through the user set data Hashmap
					for (String dataSensitvToSet : dataSens.keySet()) {
						// System.out.println(dataSensitvToSet.toString() + " trying to match what is in the table to this that the user inputted.");

						// If the User setting equals the setting in the Restricted List
						if (dataSensitvToSet.equals(accessibleItem.getText())) {
							// If it is supposed to be in Restricted or FALSE in hashmap
							if (!dataSens.get(dataSensitvToSet)) {
								// Move it to the right (Restricted to Accessible)
								System.out.println(dataSensitvToSet + " *** READY TO BE MOVED to the LEFT ***");

								Actions builder = new Actions(driver);
								builder.moveToElement(accessibleItem).perform();
								sleep(dragAndDropActionSleepTime);
								builder.dragAndDrop(accessibleItem, restrictedTable);
								sleep(dragAndDropActionSleepTime);
								builder.perform();
								sleep(dragAndDropActionSleepTime);
								// (new Actions(driver)).dragAndDrop(accessibleItem, restrictedTable).perform();
								// LOG.log(Level.INFO, "--- MOVED '" + dataSensitvToSet + "' to the RESTRICTED Data Sensitivity column, Security Role '" + roleName + "', per dataSens.put in method 'setSecurityRoles' in SecurityRolesTest.java ---");
								break;
							} else {
								System.out.println("--- '" + dataSensitvToSet + "' is 'TRUE' so it should remain "
										+ "on the RIGHT (Accessible) *NOT MOVED* ---");
							}
						}
					}
				}
			} catch (Exception e) {
				System.out.println(" *** Nothing in the LEFT-hand list (to move to the RIGHT), moving on... ***\n" + e);
			}

			// Hit SAVE! (Data Sensitivity)
			driver.findElement(By.cssSelector(".x-window .x-btn.x-box-item.x-toolbar-item")).click();

			// wait for table underneath to be visible again
			WebDriverWait waitTableUnderneath = new WebDriverWait(driver, 5);
			waitTableUnderneath.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='securityRolesTable'] .x-grid-view")));
			LOG.log(Level.INFO, "--- SAVED Data Sensitivity (Data Sources & Data Sourceitivity) Settings ---");
		
		} else {
			LOG.log(Level.SEVERE, "!!!!! Could NOT find the Security Role '" + roleName + "'.  !!!!!"
					+ "\n !!!!! Please ensure the role is created using a call to 'addSecurityRole' !!!!!");
		}

	}
}