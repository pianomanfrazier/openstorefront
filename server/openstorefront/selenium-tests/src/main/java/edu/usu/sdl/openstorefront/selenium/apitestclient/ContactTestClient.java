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
package edu.usu.sdl.openstorefront.selenium.apitestclient;

import edu.usu.sdl.apiclient.ClientAPI;
import edu.usu.sdl.apiclient.rest.resource.ContactClient;
import edu.usu.sdl.openstorefront.core.entity.Contact;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ccummings
 */
public class ContactTestClient
		extends BaseTestClient
{

	private ContactClient apiContact;
	private static List<String> contactIds = new ArrayList<>();

	public ContactTestClient(ClientAPI client, APIClient apiClient)
	{
		super(client, apiClient);
		apiContact = new ContactClient(client);
	}

	public Contact createAPIContact(String firstName, String lastName, String email, String organization)
	{
		Contact contact = new Contact();
		contact.setFirstName(firstName);
		contact.setLastName(lastName);
		contact.setEmail(email);
		contact.setOrganization(organization);

		Contact contactAPI = apiContact.createContact(contact);
		contactIds.add(contactAPI.getContactId());

		return contactAPI;
	}

	public void deleteAPIContact(String id)
	{
		apiContact.deleteContact(id);
	}

	@Override
	public void cleanup()
	{
		for (String id : contactIds) {
			deleteAPIContact(id);
		}
	}

}
