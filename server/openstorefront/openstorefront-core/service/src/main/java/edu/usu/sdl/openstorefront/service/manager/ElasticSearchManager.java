/*
 * Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.service.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.manager.Initializable;
import edu.usu.sdl.openstorefront.common.manager.PropertiesManager;
import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import edu.usu.sdl.openstorefront.core.entity.ApprovalStatus;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttribute;
import edu.usu.sdl.openstorefront.core.entity.ComponentReview;
import edu.usu.sdl.openstorefront.core.entity.ComponentTag;
import edu.usu.sdl.openstorefront.core.model.search.SearchSuggestion;
import edu.usu.sdl.openstorefront.core.view.ComponentSearchView;
import edu.usu.sdl.openstorefront.core.view.ComponentSearchWrapper;
import edu.usu.sdl.openstorefront.core.view.FilterQueryParams;
import edu.usu.sdl.openstorefront.core.view.SearchQuery;
import edu.usu.sdl.openstorefront.service.ServiceProxy;
import edu.usu.sdl.openstorefront.service.search.IndexSearchResult;
import edu.usu.sdl.openstorefront.service.search.SearchServer;
import edu.usu.sdl.openstorefront.service.search.SolrComponentModel;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

/**
 *
 * @author dshurtleff
 */
public class ElasticSearchManager
		implements Initializable, SearchServer
{

	private static final Logger LOG = Logger.getLogger(ElasticSearchManager.class.getName());

	private static final String INDEX = "openstorefront";
	private static final String INDEX_TYPE = "component";
	private static final String ELASTICSEARCH_ALL_FIELDS = "_all";

	private static AtomicBoolean started = new AtomicBoolean(false);
	private static AtomicBoolean indexCreated = new AtomicBoolean(false);
	
	private static RestClient lowLevelRestClient;
	private static RestHighLevelClient client;

	public static void init()
	{
		// Initialize Elasticsearch
		
		String host = PropertiesManager.getValue(PropertiesManager.KEY_ELASTIC_HOST, "localhost");
		Integer port = Convert.toInteger(PropertiesManager.getValue(PropertiesManager.KEY_ELASTIC_PORT, "9200")); // was 9300
		
		lowLevelRestClient = RestClient.builder(new HttpHost(host, port, "http")).build();
		client = new RestHighLevelClient(lowLevelRestClient);
		
		LOG.log(Level.INFO, MessageFormat.format("Connecting to ElasticSearch at {0}", host + ":" + port));
	}

	public static RestHighLevelClient getClient()
	{
		
		//	Check if index already exists...
		//	As of now (Elasticsearch 5.6.3), the best way to check if an index exists is to try and create it!
		//	... then catch the error if thrown... High/Low-level REST client doesn't support checking the existence indices.
		//		per: https://discuss.elastic.co/t/high-level-rest-client-admin-api/100461
		
		
		if (!indexCreated.get() && client != null) {
			try {
				
				XContentBuilder source = JsonXContent.contentBuilder().startObject().endObject();
				StringEntity entity = new StringEntity(source.string(), ContentType.APPLICATION_JSON);
				
				//	Perform a request attempting to create an index
				Response response = lowLevelRestClient.performRequest("PUT", "/" + INDEX, Collections.emptyMap(), entity);												
				LOG.log(Level.INFO, "Search index: " + INDEX + " has been created.{0}", response.getStatusLine().getStatusCode());
				indexCreated.set(true);
				
			} catch (ResponseException e) {
				//	Index was already created...
				indexCreated.set(true);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, null, e);	
			}
			
		}

		return client;
	}

	public static void cleanup()
	{
		if (lowLevelRestClient != null) {
			try {
				lowLevelRestClient.close();
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void initialize()
	{
		ElasticSearchManager.init();
		started.set(true);
	}

	@Override
	public void shutdown()
	{
		ElasticSearchManager.cleanup();
		started.set(false);
	}

	private ServiceProxy service = ServiceProxy.getProxy();

	@Override
	public ComponentSearchWrapper search(SearchQuery searchQuery, FilterQueryParams filter)
	{
		ComponentSearchWrapper componentSearchWrapper = new ComponentSearchWrapper();

		IndexSearchResult indexSearchResult = doIndexSearch(searchQuery.getQuery(), filter);

		SearchServerManager.updateSearchScore(searchQuery.getQuery(), indexSearchResult.getSearchViews());

		componentSearchWrapper.setData(indexSearchResult.getSearchViews());
		componentSearchWrapper.setResults(indexSearchResult.getSearchViews().size());
		componentSearchWrapper.setTotalNumber(indexSearchResult.getTotalResults());

		return componentSearchWrapper;
	}

	@Override
	public IndexSearchResult doIndexSearch(String query, FilterQueryParams filter)
	{
		return doIndexSearch(query, filter, null);
	}

	@Override
	public IndexSearchResult doIndexSearch(String query, FilterQueryParams filter, String[] addtionalFieldsToReturn)
	{
		IndexSearchResult indexSearchResult = new IndexSearchResult();

		int maxSearchResults = 10000;
		if (filter.getMax() < maxSearchResults) {
			maxSearchResults = filter.getMax();
		}

		if (StringUtils.isBlank(query)) {
			query = "*";
		}

		// Initialize Search Phrases
		// (Searching Different Phrases [Quoted Words])
		List<String> searchPhrases = new ArrayList<>();

		// Convert Query To StringBuilder
		StringBuilder queryString = new StringBuilder(query);

		// Search For Quotes
		while (queryString.indexOf("\"") != -1) {

			// Store Index Of First Quote
			int quoteStartIndex = queryString.indexOf("\"");

			// Store Index Of Next Quote
			int quoteEndIndex = queryString.indexOf("\"", quoteStartIndex + 1);

			// Check If We Failed To Find Next Quote
			if (quoteEndIndex == -1) {

				// Remove First Quote
				queryString.deleteCharAt(quoteStartIndex);
			} else {

				// Create Sub-String Of Quoted Phrase
				String subQueryString = queryString.substring(quoteStartIndex, quoteEndIndex + 1);

				// Remove Sub-String From Query
				queryString.delete(quoteStartIndex, quoteEndIndex + 1);

				// Add Sub-String To Search Phrases
				searchPhrases.add(subQueryString);

				//////////
				// TRIM //
				//////////
				// Replace All Double Spaces
				while (queryString.indexOf("  ") != -1) {

					// Get Index Of Double Space
					int doubleSpaceIndex = queryString.indexOf("  ");

					queryString.replace(doubleSpaceIndex, doubleSpaceIndex + 2, " ");
				}

				// Search For Beginning Whitespace
				if (queryString.length() > 0 && queryString.charAt(0) == ' ') {

					// Remove Whitespace
					queryString.deleteCharAt(0);
				}

				// Search For Ending Whitespace
				if (queryString.length() > 0 && queryString.charAt(queryString.length() - 1) == ' ') {

					// Remove Whitespace
					queryString.deleteCharAt(queryString.length() - 1);
				}
			}
		}

		// Initialize ElasticSearch Query
		BoolQueryBuilder esQuery = QueryBuilders.boolQuery();

		// Check For Remaining Query Items
		if (queryString.length() > 0) {

			String actualQuery = "";
			String allUpperQuery = "";
			String allLowerQuery = "";
			String properCaseQuery = "";

			if (isHyphenatedWithoutSpaces(queryString.toString())) {
				// Create custom queries
				allUpperQuery = createSubstringOfQuery(queryString.toString().toUpperCase());
				allLowerQuery = createSubstringOfQuery(queryString.toString().toLowerCase());
				properCaseQuery = createSubstringOfQuery(toProperCase(queryString.toString()));
				actualQuery = createSubstringOfQuery(queryString.toString());

			} else {
				// Create custom queries
				allUpperQuery = queryString.toString().toUpperCase();
				allLowerQuery = queryString.toString().toLowerCase();
				properCaseQuery = toProperCase(queryString.toString());
				actualQuery = queryString.toString();
			}

			// Custom query for entry name
			esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, allUpperQuery));
			esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, allLowerQuery));
			esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, properCaseQuery));
			esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_NAME, actualQuery));

			esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, allUpperQuery));
			esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, allLowerQuery));
			esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, properCaseQuery));
			esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, actualQuery));

			// Custom query for entry organization
			esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, allUpperQuery));
			esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, allLowerQuery));
			esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, properCaseQuery));
			esQuery.should(QueryBuilders.wildcardQuery(ComponentSearchView.FIELD_ORGANIZATION, actualQuery));
			
			// Custom query for description
			esQuery.should(QueryBuilders.matchPhraseQuery("description", actualQuery));

			// Fuzzy query on all fields using actual input
			esQuery.should(QueryBuilders.fuzzyQuery(ELASTICSEARCH_ALL_FIELDS, actualQuery));
		}

		// Loop Through Search Phrases
		for (String phrase : searchPhrases) {

			esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_NAME, phrase));
			esQuery.should(QueryBuilders.matchPhraseQuery(ComponentSearchView.FIELD_ORGANIZATION, phrase));
			esQuery.should(QueryBuilders.matchPhraseQuery("description", phrase.toLowerCase()));
		}
		FieldSortBuilder sort = new FieldSortBuilder(filter.getSortField())
				.unmappedType("String") // currently the only fileds we are searching/sorting on are strings
				.order(OpenStorefrontConstant.SORT_ASCENDING.equals(filter.getSortOrder()) ? SortOrder.ASC : SortOrder.DESC);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
			.query(esQuery)
			.from(filter.getOffset())
			.size(maxSearchResults)
			.sort(sort);
		SearchRequest searchRequest = new SearchRequest(INDEX)
			.source(searchSourceBuilder);
		
		try {
			performIndexSearch(searchRequest, indexSearchResult);
		} catch (IOException ex) {
			Logger.getLogger(ElasticSearchManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ElasticsearchStatusException e) {
			
			//	if a status exception occurs, it is likely the fielddata == false for description.
			//		Thus, update the mapping.
			updateMapping();
			
			//	try to perform the index search one more time...
			try {
				performIndexSearch(searchRequest, indexSearchResult);
			} catch (IOException | ElasticsearchStatusException ex) {
				throw new OpenStorefrontRuntimeException("Unable to perform search!", "check index [" + INDEX + "] mapping", ex);
			}
		}

		return indexSearchResult;
	}
	
	private void performIndexSearch (SearchRequest searchRequest, IndexSearchResult indexSearchResult) throws IOException, ElasticsearchStatusException
	{
		// perform search
		SearchResponse response = ElasticSearchManager.getClient().search(searchRequest);

		indexSearchResult.setTotalResults(response.getHits().getTotalHits());
		indexSearchResult.setMaxScore(response.getHits().getMaxScore());

		ObjectMapper objectMapper = StringProcessor.defaultObjectMapper();
		for (SearchHit hit : response.getHits().getHits()) {
			try {
				ComponentSearchView view = objectMapper.readValue(hit.getSourceAsString(), new TypeReference<ComponentSearchView>()
				{
				});
				if (service.getComponentService().checkComponentApproval(view.getComponentId())) {
					view.setSearchScore(hit.getScore());
					indexSearchResult.getSearchViews().add(view);
					indexSearchResult.getResultsList().add(SolrComponentModel.fromComponentSearchView(view));
				} else {
					LOG.log(Level.FINER, MessageFormat.format("Component is no longer approved and active.  Removing index.  {0}", view.getComponentId()));
					indexSearchResult.setTotalResults(indexSearchResult.getTotalResults() - 1);
					deleteById(view.getComponentId());
				}
			} catch (IOException ex) {
				throw new OpenStorefrontRuntimeException("Unable to handle search result", "check index database", ex);
			}
		}
		indexSearchResult.applyDataFilter();
	}

	protected String toProperCase(String query)
	{
		final String DELIMITERS = " '-/*";

		StringBuilder searchQuery = new StringBuilder();
		boolean capNext = true;

		for (char c : query.toCharArray()) {

			if (capNext && Character.isLetter(c)) {
				c = (capNext) ? Character.toUpperCase(c) : Character.toLowerCase(c);
				searchQuery.append(c);
				capNext = (DELIMITERS.indexOf((int) c) >= 0);
			} else {
				searchQuery.append(c);
				capNext = (DELIMITERS.indexOf((int) c) >= 0);
			}
		}
		return searchQuery.toString();
	}

	protected boolean isHyphenatedWithoutSpaces(String query)
	{
		if (query.indexOf('-') >= 0 && query.charAt(0) != '"') {

			for (int i = 1; i < query.length() - 1; i++) {
				if (query.charAt(i) == '-' && query.charAt(i - 1) != ' ' && query.charAt(i + 1) != ' ') {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	protected String createSubstringOfQuery(String query)
	{
		int indexOfHyphen = query.indexOf('-');

		return query.substring(0, indexOfHyphen);
	}

	@Override
	public List<SearchSuggestion> searchSuggestions(String query, int maxResult, String componentType)
	{
		List<SearchSuggestion> searchSuggestions = new ArrayList<>();

		FilterQueryParams filter = FilterQueryParams.defaultFilter();

		//ignore case
		query = "*" + query.toLowerCase() + "*";

		//query everything we can
		String extraFields[] = {
			SolrComponentModel.FIELD_NAME,
			SolrComponentModel.FIELD_ORGANIZATION,
			SolrComponentModel.FIELD_DESCRIPTION
		};
		IndexSearchResult indexSearchResult = doIndexSearch(query, filter, extraFields);

		if (StringUtils.isNotBlank(componentType)) {
			indexSearchResult.setResultsList(
					indexSearchResult.getResultsList()
							.stream()
							.filter((result) -> componentType.equals(result.getComponentType()))
							.collect(Collectors.toList())
			);
			indexSearchResult.setSearchViews(
					indexSearchResult.getSearchViews()
							.stream()
							.filter((result) -> componentType.equals(result.getComponentType()))
							.collect(Collectors.toList())
			);
		}

		if (StringUtils.isBlank(query)) {
			query = "";
		}

		//apply weight to items
		String queryNoWild = query.replace("*", "").toLowerCase();
		for (SolrComponentModel model : indexSearchResult.getResultsList()) {
			int score = 0;

			if (StringUtils.isNotBlank(model.getName())
					&& model.getName().toLowerCase().contains(queryNoWild)) {
				score += 100;
			}

			if (StringUtils.isNotBlank(model.getOrganization())
					&& model.getOrganization().toLowerCase().contains(queryNoWild)) {
				score += 50;
			}

			int count = StringUtils.countMatches(model.getDescription().toLowerCase(), queryNoWild);
			score += count * 5;

			model.setSearchWeight(score);
		}

		//sort
		indexSearchResult.getResultsList().sort((SolrComponentModel o1, SolrComponentModel o2) -> Integer.compare(o2.getSearchWeight(), o1.getSearchWeight()));

		//window
		List<SolrComponentModel> topItems = indexSearchResult.getResultsList().stream().limit(maxResult).collect(Collectors.toList());

		for (SolrComponentModel model : topItems) {

			SearchSuggestion suggestion = new SearchSuggestion();
			suggestion.setName(model.getName());
			suggestion.setComponentId(model.getId());
			suggestion.setQuery("\"" + model.getName() + "\"");

			// Only include approved components.
			if (service.getComponentService().checkComponentApproval(suggestion.getComponentId())) {
				searchSuggestions.add(suggestion);
			}
		}

		return searchSuggestions;
	}

	@Override
	public void index(List<Component> components)
	{
		Objects.requireNonNull(components);

		if (!components.isEmpty()) {
			ObjectMapper objectMapper = StringProcessor.defaultObjectMapper();
			BulkRequest bulkRequest = new BulkRequest();

			//pull attribute and map
			ComponentAttribute componentAttribute = new ComponentAttribute();
			componentAttribute.setActiveStatus(ComponentAttribute.ACTIVE_STATUS);
			List<ComponentAttribute> componentAttributes = componentAttribute.findByExample();
			Map<String, List<ComponentAttribute>> attributeMap = componentAttributes.stream().collect(Collectors.groupingBy(ComponentAttribute::getComponentId));

			//pull reviews and map
			ComponentReview componentReview = new ComponentReview();
			componentReview.setActiveStatus(ComponentReview.ACTIVE_STATUS);
			List<ComponentReview> componentReviews = componentReview.findByExample();
			Map<String, List<ComponentReview>> reviewMap = componentReviews.stream().collect(Collectors.groupingBy(ComponentReview::getComponentId));

			//pull tags and map
			ComponentTag componentTag = new ComponentTag();
			componentTag.setActiveStatus(ComponentReview.ACTIVE_STATUS);
			List<ComponentTag> componentTags = componentTag.findByExample();
			Map<String, List<ComponentTag>> tagMap = componentTags.stream().collect(Collectors.groupingBy(ComponentTag::getComponentId));

			for (Component component : components) {

				//convert to search result object
				componentAttributes = attributeMap.getOrDefault(component.getComponentId(), new ArrayList<>());
				componentReviews = reviewMap.getOrDefault(component.getComponentId(), new ArrayList<>());
				componentTags = tagMap.getOrDefault(component.getComponentId(), new ArrayList<>());

				ComponentSearchView componentSearchView = ComponentSearchView.toView(component, componentAttributes, componentReviews, componentTags);
				try {
					bulkRequest.add(new IndexRequest(INDEX, INDEX_TYPE, componentSearchView.getComponentId()).source(objectMapper.writeValueAsBytes(componentSearchView)));
				} catch (JsonProcessingException ex) {
					LOG.log(Level.SEVERE, null, ex);
				}
			}
			
			BulkResponse bulkResponse;
			try {
				bulkResponse = ElasticSearchManager.getClient().bulk(bulkRequest);
				
				if (bulkResponse.hasFailures()) {
					bulkResponse.forEach(response -> {
						if (StringUtils.isNotBlank(response.getFailureMessage())) {
							LOG.log(Level.WARNING, MessageFormat.format("A component failed to index: {0}", response.getFailureMessage()));
						}
					});
				} else {
					LOG.log(Level.FINE, "Index components successfully");
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void deleteById(String id)
	{
		DeleteRequest deleteRequest = new DeleteRequest(INDEX, INDEX_TYPE, id);
		DeleteResponse response;
		try {
			response = client.delete(deleteRequest);
			LOG.log(Level.FINER, MessageFormat.format("Found Record to delete: {0}", response.getId()));
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void deleteAll()
	{
		//query all (delete in groups)
		int start = 0;
		int max = 1000;
		long total = max;

		while (start < total) {
			
			//	Create the search request
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
				.query(QueryBuilders.matchAllQuery())
				.from(start)
				.size(max);
			SearchRequest searchRequest = new SearchRequest(INDEX)
				.types(INDEX_TYPE)
				.source(sourceBuilder);
			
			SearchResponse response;
			try {
				response = ElasticSearchManager.getClient().search(searchRequest);
				
				SearchHits searchHits = response.getHits();
				BulkRequest bulkRequest = new BulkRequest();
				if (searchHits.getTotalHits() > 0) {
					//bulk delete results
					searchHits.forEach(hit -> {
						bulkRequest.add(new IndexRequest(INDEX, INDEX_TYPE, hit.getId()).source(XContentType.JSON, "componentId", hit.getId()));
						deleteById(hit.getId());
					});

					//	Process the bulk request (ensure there were no failures)
					BulkResponse bulkResponse = ElasticSearchManager.getClient().bulk(bulkRequest);
					if (bulkResponse.hasFailures()) {
						bulkResponse.forEach(br -> {
							if (StringUtils.isNotBlank(br.getFailureMessage())) {
								LOG.log(Level.WARNING, MessageFormat.format("A component failed to delete: {0}", br.getFailureMessage()));
							}
						});
					}
				}
				start += searchHits.getHits().length;
				total = searchHits.getTotalHits();
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, null, ex);
			}

		}
	}

	@Override
	public void saveAll()
	{
		Component component = new Component();
		component.setActiveStatus(Component.ACTIVE_STATUS);
		component.setApprovalState(ApprovalStatus.APPROVED);
		List<Component> components = component.findByExample();

		index(components);
	}

	@Override
	public void resetIndexer()
	{
		deleteAll();
		saveAll();
		updateMapping();
	}

	@Override
	public boolean isStarted()
	{
		return started.get();
	}

	private static void updateMapping ()
	{
		// Update description field to use fielddata=true
		//	Here, we must update all types
		try {

			// construct body of request
			final XContentBuilder source = JsonXContent
				.contentBuilder()
				.startObject()
					.startObject("properties")
						.startObject("description")
							.field("type", "text")
							.field("fielddata", true)
						.endObject()
					.endObject()
				.endObject();

			//	Use low-level REST client to perform re-mapping
			StringEntity entity = new StringEntity(source.string(), ContentType.APPLICATION_JSON);
			
			//	Perform a PUT request to update the description mapping
			lowLevelRestClient.performRequest("PUT", "/" + INDEX + "/_mapping/" + "description?update_all_types", Collections.emptyMap(), entity);

		} catch (IOException ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
}
