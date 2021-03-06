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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usu.sdl.apiclient.ClientAPI;
import edu.usu.sdl.apiclient.rest.resource.UserSavedSearchClient;
import edu.usu.sdl.openstorefront.core.entity.UserSavedSearch;
import edu.usu.sdl.openstorefront.core.model.search.SearchElement;
import edu.usu.sdl.openstorefront.core.model.search.SearchModel;
import edu.usu.sdl.openstorefront.core.model.search.SearchOperation;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ccummings
 */
public class UserSavedSearchTestClient
		extends BaseTestClient
{
	
	private UserSavedSearchClient apiUserSavedSearch;
	private static List<String> savedSearchIds = new ArrayList<>();
	
	public UserSavedSearchTestClient(ClientAPI client, APIClient apiClient)
	{
		super(client, apiClient);
		apiUserSavedSearch = new UserSavedSearchClient(client);
	}
	
	public UserSavedSearch createAPIUserSavedSearch() throws JsonProcessingException
	{
		// Search Elements
		SearchElement element = new SearchElement();
		element.setSearchType(SearchOperation.SearchType.COMPONENT);
		element.setField("Name");
		element.setValue("This is a rad api saved search value");
		element.setMergeCondition(SearchOperation.MergeCondition.OR);
		List<SearchElement> searchElements = new ArrayList<>();
		searchElements.add(element);
		// Search Model
		SearchModel model = new SearchModel();
		model.setSearchElements(searchElements);
		ObjectMapper objMapper = new ObjectMapper();
		String request = objMapper.writeValueAsString(model);
		
		UserSavedSearch search = new UserSavedSearch();
		search.setSearchName("An API Test User Saved Search");
		search.setSearchRequest(request);
		search.setUsername("user");
		
		UserSavedSearch apiUserSearch = apiUserSavedSearch.createNewSearch(search);
		savedSearchIds.add(apiUserSearch.getUserSearchId());
		
		return apiUserSearch;
	}

	@Override
	public void cleanup()
	{
		for (String id : savedSearchIds) {
			apiUserSavedSearch.deleteUserSearch(id);
		}
	}

}
