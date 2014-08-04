/* 
* Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.
*
* Licensed under the Apache License, Version 2.0 (the 'License');
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an 'AS IS' BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
'use strict';

/* global isEmpty, setupPopovers, openClick:true, setupResults,
fullClick, openFiltersToggle, buttonOpen, buttonClose, toggleclass, resetAnimations,
filtClick*/

app.controller('ResultsCtrl', ['$scope', 'localCache', 'business', '$filter', '$timeout', '$location', '$rootScope', '$q', '$route', '$sce', function ($scope,  localCache, Business, $filter, $timeout, $location, $rootScope, $q, $route, $sce) { /*jshint unused: false*/


  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  // Here we put our variables...
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  // $scope.$emit('$TRIGGERLOAD', 'resultsLoad');
  setPageHeight($('#resultsPage'), 52);
  $scope.$emit('$TRIGGERLOAD', 'mainLoader');
  $scope.$emit('$TRIGGERLOAD', 'filtersLoad');
  $scope._scopename         = 'results';
  $scope.lastUsed           = new Date();
  $scope.searchCode         = null;
  $scope.searchTitle        = null;
  $scope.searchDescription  = null;
  $scope.details            = {};
  $scope.details.details    = null;
  $scope.isPage1            = true;
  $scope.showSearch         = false;
  $scope.showDetails        = false;
  $scope.showMessage        = false;
  $scope.orderProp          = '';
  $scope.query              = '';
  $scope.noDataMessage      = $sce.trustAsHtml('<p>There are no results for your search</p> <p>&mdash; Or &mdash;</p> <p>You have filtered out all of the results.</p><button class="btn btn-default" ng-click="clearFilters()">Reset Filters</button>');
  $scope.typeahead          = null;
  $scope.searchGroup        = null;
  $scope.searchKey          = null;
  $scope.filters            = null;
  $scope.resetFilters       = null;
  $scope.total              = null;
  $scope.ratingsFilter      = 0;
  $scope.modal              = {};
  $scope.modal.isLanding    = false;
  $scope.single             = false;
  $scope.isArticle          = false;
  $scope.tagsList           = Business.getTagsList();
  $scope.tagsList.sort();
  $scope.prosConsList       = Business.getProsConsList();
  $scope.watches            = Business.getWatches();
  $scope.expertise          = [
    //
    {'value':'1', 'label': 'Less than 1 month'},
    {'value':'2', 'label': 'Less than 3 months'},
    {'value':'3', 'label': 'Less than 6 months'},
    {'value':'4', 'label': 'Less than 1 year'},
    {'value':'5', 'label': 'Less than 3 years'},
    {'value':'6', 'label': 'More than 3 years'}
  //
  ];
  $scope.userRoles          = [
    //
    {'code':'ENDUSER', 'description': 'User'},
    {'code':'DEV', 'description': 'Developer'},
    {'code':'PM', 'description': 'Project Manager'}
  //
  ];
  // These variables are used for the pagination
  $scope.filteredTotal      = null;
  $scope.data               = {};
  $scope.rowsPerPage        = 200;
  $scope.pageNumber         = 1;
  $scope.maxPageNumber      = 1;



  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  // Here we put our functions...
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /***************************************************************
  * Set up typeahead, and then watch for selection made
  ***************************************************************/
  Business.componentservice.getComponentDetails().then(function(result) {
    Business.typeahead(result, 'name').then(function(value){
      $scope.typeahead = value;
    });
  });

  /***************************************************************
  * This grabs the user type codes and sets them to the scope.
  ***************************************************************/
  Business.lookupservice.getUserTypeCodes().then(function(lookup){
    $scope.userTypeCodes  = lookup;
    //TODO: chain load the review form    
  });

  /***************************************************************
  * This function selects the initial tab.
  * params: tab -- The tab that is selected
  ***************************************************************/
  $scope.setSelectedTab = function(tab) {
    $scope.selectedTab = tab;
  };

  /***************************************************************
  * Here we set the tab class
  * params: tab -- The tab to check to see if it is selected
  * returns: class -- The classes that the tab will have
  ***************************************************************/
  $scope.tabClass = function(tab) {
    if ($scope.selectedTab === tab) {
      return 'active';
    } else {
      return '';
    }
  };

  /***************************************************************
  * This function is looked at for auto suggestions for the tag list
  * if a ' ' is the user's entry, it will auto suggest the next 20 tags that
  * are not currently in the list of tags. Otherwise, it will look at the
  * string and do a substring search.
  * params: query -- The input that the user has typed so far
  * params: list -- The list of tags already tagged on the item
  * params: source -- The source of the tags options
  * returns: deferred.promise -- The promise that we will return a resolved tags list
  ***************************************************************/
  $scope.checkTagsList = function(query, list, source) {
    var deferred = $q.defer();
    var subList = null;
    if (query === ' ') {
      subList = _.reject(source, function(item) {
        return !!(_.where(list, {'text': item}).length);
      });
    } else {
      subList = _.filter(source, function(item) {
        return item.toLowerCase().indexOf(query.toLowerCase()) > -1;
      });
    }
    deferred.resolve(subList);
    return deferred.promise;
  };

  /***************************************************************
  * Description
  * params: param name -- param description
  * returns: Return name -- return description
  ***************************************************************/
  var getBody = function(route) {
    var deferred = $q.defer();
    $.get(route).then(function(responseData) {
      deferred.resolve(responseData);
    });
    return deferred.promise;
  };

  /***************************************************************
  * This function is called once we have the search request from the business layer
  * The order and manner in which we do this call will most likely change once
  * we get the httpbackend fleshed out.
  ***************************************************************/
  $scope.reAdjust = function(key) {
    $scope.searchGroup        = key;
    $scope.searchKey          = $rootScope.searchKey;
    
    if (!isEmpty($scope.searchGroup)) {
      // grab all of the keys in the filters
      $scope.searchKey        = $scope.searchGroup[0].key;
      $scope.searchCode       = $scope.searchGroup[0].code;
    } else {
      $scope.searchKey        = 'search';
      $scope.searchCode       = '';
    }

    Business.componentservice.doSearch($scope.searchKey, $scope.searchCode).then(function(result) {
      $scope.total = result || {};
      $scope.filteredTotal = $scope.total;

      /*Simulate wait for the filters*/
      $scope.filters = Business.getFilters();
      $scope.filters = angular.copy($scope.filters);
      $scope.filters = _.sortBy($scope.filters, function(item){
        return item.description;
      });
      $scope.$emit('$TRIGGERUNLOAD', 'filtersLoad');
      /*This is simulating the wait time for building the data so that we get a loader*/
      $timeout(function(){
        $scope.data.data = $scope.total;
        _.each($scope.data.data, function(item){
          if (item.description !== null && item.description !== undefined && item.description !== '') {
            var desc = item.description.match(/^(.*?)[.?!]\s/);
            item.shortdescription = (desc && desc[0])? desc[0] + '.': item.description;
          } else {
            item.shortdescription = 'This is a temporary short description';
          }
        });
        // $scope.$emit('$TRIGGERUNLOAD', 'resultsLoad');
        $scope.$emit('$TRIGGERUNLOAD', 'mainLoader');
        $scope.initializeData(key);
        adjustFilters();
        setupResults();
      }, 500);
    }); //
  }; //


  /***************************************************************
  * This is used to initialize the scope title, key, and code. Once we have a 
  * database, this is most likely where we'll do the first pull for data.
  *
  * TODO:: Add query prameters capabilities for this page so that we don't have
  * to rely on the local/session storrage to pass us the search key
  *
  * TODO:: When we do start using actual transfered searches from the main page
  * we need to initialize checks on the filters that were sent to us from that
  * page (or we need to disable the filter all together)
  * 
  * This function is called by the reAdjustment function in order
  * to reinitialze all of the data if the list of items changes.
  * which usually would hinge on the key of the search
  * params: key -- The search object we use to initialize data with.
  ***************************************************************/
  $scope.initializeData = function(key) {

    if (!isEmpty($scope.searchGroup)) {
      // grab all of the keys in the filters
      $scope.searchKey          = $scope.searchGroup[0].key;
      $scope.searchCode         = $scope.searchGroup[0].code;
      var keys = _.pluck($scope.filters, 'type');
      
      var foundFilter = null;
      var foundCollection = null;
      var type = '';
      


      // TODO: CLEAN UP THIS IF/ELSE switch!!!!!!!


      if (_.contains(keys, $scope.searchKey)) {
        $scope.showSearch         = true;
        
        foundFilter = _.where($scope.filters, {'type': $scope.searchGroup[0].key})[0];
        foundCollection = _.where(foundFilter.codes, {'code': $scope.searchGroup[0].code})[0];
        // if the search group is based on one of those filters do this
        if ($scope.searchCode !== 'all') {
          $scope.searchColItem      = foundCollection;
          $scope.searchTitle        = foundFilter.description + ', ' + foundCollection.label;
          $scope.modal.modalTitle   = foundFilter.description + ', ' + foundCollection.label;
          $scope.searchDescription  = foundCollection.description || 'The results on this page are restricted by an implied filter on the attribute: ' + $scope.searchTitle;
          if (foundCollection.landing !== undefined && foundCollection.landing !== null) {
            getBody(foundCollection.landing).then(function(result) {
              $scope.modal.modalBody = result;
              $scope.modal.isLanding = true;
            });
          } else {
            $scope.modal.modalBody = foundCollection.description || 'The results on this page are restricted by an implied filter on the attribute: ' + $scope.searchTitle;
            $scope.modal.isLanding = false;
          }
        } else {
          $scope.searchTitle        = $scope.searchType + ', All';
          $scope.modal.modalTitle   = $scope.searchType + ', All';
          $scope.searchDescription  = 'The results on this page are restricted by an implied filter on the attribute: ' + $scope.searchType;
          $scope.modal.modalBody          = 'This will eventually hold a description for this attribute type.';
          $scope.modal.isLanding = false;
        }
      } else if ($scope.searchGroup[0].key === 'search') {

        // Otherwise check to see if it is a search
        $scope.searchKey          = 'DOALLSEARCH';
        $scope.showSearch         = true;
        $scope.searchTitle        = $scope.searchGroup[0].code;
        $scope.modal.modalTitle   = $scope.searchGroup[0].code;
        $scope.searchDescription  = 'Search results based on the search key: ' + $scope.searchGroup[0].code;
        $scope.modal.modalBody    = 'The results on this page are restricted by an implied filter on words similar to the search key \'' + $scope.searchGroup[0].code + '\'';
      } else {
        // In this case, our tempData object exists, but has no useable data
        $scope.searchKey          = 'DOALLSEARCH';
        $scope.showSearch         = true;
        $scope.searchTitle        = 'All';
        $scope.modal.modalTitle   = 'All';
        $scope.searchDescription  = 'Search all results';
        $scope.modal.modalBody    = 'The results found on this page are not restricted by any implied filters.';
      }
    } else {
      // In this case, our tempData doesn't exist
      $scope.searchKey          = 'DOALLSEARCH';
      $scope.showSearch         = true;
      $scope.searchTitle        = 'All';
      $scope.modal.modalTitle   = 'All';
      $scope.searchDescription  = 'Search all results';
      $scope.modal.modalBody    = 'The results found on this page are not restricted by any implied filters.';
    }

    $scope.applyFilters();
    $scope.$broadcast('dataloaded', !$scope.single);
  };

  /***************************************************************
  * This function grabs the search key and resets the page in order to update the search
  ***************************************************************/
  var callSearch = function() {
    Business.componentservice.search(false, false, true).then(
    //This is the success function on returning a value from the business layer 

    // TODO: CLEAN UP THIS FUNCTION!!!!
    function(key) {

      var type = 'all';
      var code = '';
      var query = null;
      if (key === null || key === undefined) {
        if (!isEmpty($location.search()))
        {
          query = $location.search();
          if (query.type && query.code) {
            type = query.type;
            code = query.code;
          }
        }
        $scope.reAdjust([{ 'key': type, 'code': code }]);
      } else {
        type = '';
        code = '';
        // console.log('search', $location.search());
        
        if (!isEmpty($location.search()))
        {
          query = $location.search();
          if (query.type && query.code) {
            type = query.type;
            code = query.code;
          } else {
            type = 'all';
          }
          key = [{ 'key': type, 'code': code }];
        }
        // console.log('key', key);
        
        $scope.reAdjust(key);
      }
    },
    // This is the failure function that handles a returned error
    function(error) {
      console.error('ERROR: ', error);
      
      var type = 'all';
      var code = '';
      if (!isEmpty($location.search()))
      {
        var query = $location.search();
        if (query.type && query.code) {
          type = query.type;
          code = query.code;
        }
      }
      $scope.reAdjust([{ 'key': type, 'code': code }]);
    });
    //
  };

  /***************************************************************
  * This function is used by the reviews section in the details to remove
  * and add the ellipsis
  ***************************************************************/
  $scope.toggleclass = function(id, className) {
    toggleclass(id, className);
  };

  /***************************************************************
  * This function removes the inherent filter (if you click on apps, types no longer applies etc)
  ***************************************************************/
  var adjustFilters = function() {
    if ($scope.searchGroup[0].key) {
      $scope.filters = _.reject($scope.filters, function(item) {
        return item.key === $scope.searchGroup[0].key;
      });
    }
    $scope.resetFilters = JSON.parse(JSON.stringify($scope.filters));
  };

  /***************************************************************
  * This funciton calls the global buttonOpen function that handles page 
  * flyout animations according to the state to open the details
  ***************************************************************/
  $scope.doButtonOpen = function() {
    buttonOpen();
  };

  /***************************************************************
  * This funciton calls the global buttonClose function that handles page 
  * flyout animations according to the state to close the details
  ***************************************************************/
  $scope.doButtonClose =  function() {
    buttonClose();
  };

  /***************************************************************
  * This function handles toggleing filter checks per filter heading click.
  ***************************************************************/
  $scope.toggleChecks = function(collection, override){
    $scope.applyFilters();
  };

  /***************************************************************
  * This function updates the details when a component title is clicked on
  ***************************************************************/
  $scope.updateDetails = function(id, article){
    $scope.$emit('$TRIGGERLOAD', 'fullDetailsLoader');
    if (article && article.type === 'Article') {
      $scope.isArticle = true;
      localCache.save('landingRoute', article.route);
      $scope.$emit('$TRIGGERUNLOAD', 'fullDetailsLoader');
      $scope.$emit('$TRIGGEREVENT', '$TRIGGERLANDING', article.route);
      $scope.showDetails = true;
      if (!openClick) {
        buttonOpen();
      }
    } else {
      $scope.isArticle = false;

      $('.page2').scrollTop(0);
      if (!openClick) {
        buttonOpen();
      }
      $scope.showDetails = false;
      Business.componentservice.getComponentDetails(id).then( function (result){
        if (result)
        {
          $scope.details.details = result;

          // Code here will be linted with JSHint.
          /* jshint ignore:start */
          // Code here will be linted with ignored by JSHint.

          if ($scope.details.details.attributes[0] !== undefined) {

            _.each($scope.details.details.attributes, function(attribute) {
              if (attribute.type === 'DI2E-SVCV4-A') {

                var svcv4 = _.find(MOCKDATA2.svcv4, function(item) {
                  return item.TagValue_Number === attribute.code;
                });
                if (svcv4) {
                  attribute.codeDescription = svcv4.TagValue_Number + ' - ' + svcv4['TagValue_Service Name'];
                  attribute.svcv4 = svcv4;
                } else {
                  attribute.svcv4 = null;
                }
              }
            });
          }


          /* jshint ignore:end */

        }
        $scope.$emit('$TRIGGERUNLOAD', 'fullDetailsLoader');
        $scope.showDetails = true;
      });
    } //
  }; //

  /***************************************************************
  * This function adds a component to the watch list and toggles the buttons
  ***************************************************************/
  $scope.goToFullPage = function(id){
    var url = $location.absUrl().replace($location.url(), '');
    url = url + '/single?id=' + id;
    window.open(url, 'Component ' + id, 'window settings');
    // $location.search({
    //   'id': id
    // });
    // $location.path('/single');
  };

  /***************************************************************
  * This function resets the filters in the results page in order to clear
  * the filters as quickly as possible
  ***************************************************************/
  $scope.clearFilters = function() {
    $scope.orderProp = '';
    $scope.ratingsFilter = null;
    $scope.tagsFilter = null;
    $scope.query = null;
    if ($scope.resetFilters) {
      $scope.filters = JSON.parse(JSON.stringify($scope.resetFilters));
    }
    $scope.applyFilters();
  };

  /***************************************************************
  * This function applies the filters that have been given to us to filter the
  * data with
  ***************************************************************/
  $scope.applyFilters = function() {
    if ($scope.filteredTotal) {
      var results =
      // We must use recursive filtering or we will get incorrect results
      // the order DOES matter here.
      $filter('orderBy')
        //
        ($filter('ratingFilter')
          ($filter('tagFilter')
            ($filter('componentFilter')
              ($filter('filter')
              //filter by the string
              ($scope.total, $scope.query),
            // filter the data by the filters
            $scope.filters),
          // filter the data by the tags
          $scope.tagsFilter),
        // filter the data by the ratings
        $scope.ratingsFilter),
      // Then order-by the orderProp
      $scope.orderProp);

      // make sure we reset the data and then copy over the results  
      $scope.filteredTotal = [''];
      $scope.filteredTotal = results;

      // Do the math required to assure that we have a valid page number and 
      // maxPageNumber
      $scope.maxPageNumber = Math.ceil($scope.filteredTotal.length / $scope.rowsPerPage);
      if (($scope.pageNumber - 1) * $scope.rowsPerPage >= $scope.filteredTotal.length) {
        $scope.pageNumber = 1;
      }

      // Set the data that will be displayed to the first 'n' results of the filtered data
      $scope.data.data = $scope.filteredTotal.slice((($scope.pageNumber - 1) * $scope.rowsPerPage), ($scope.pageNumber * $scope.rowsPerPage));
      if ($scope.data.data.length) {
        $scope.showMessage = false;
      } else {
        $scope.showMessage = true;
      }
      // after a slight wait, reapply the popovers for the results ratings.
      $timeout(function() {
        setupPopovers();
      }, 300);
    }
  };



  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  // Here we put our Event Watchers
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /***************************************************************
  * Event for callSearch caught here. This is triggered by the nav
  * search bar when you are already on the results page.
  ***************************************************************/
  $scope.$on('$callSearch', function(event) {/*jshint unused: false*/
    callSearch();
  });

  /***************************************************************
  * Event to trigger an update of the details that are shown
  ***************************************************************/
  $scope.$on('$detailsUpdated', function(event, id) {/*jshint unused: false*/
    if ($scope.details.details && $scope.details.details.componentId === id) {
      $timeout(function() {
        $scope.updateDetails($scope.details.details.componentId, $scope.details.details.listingType);
      });
    }
  });


  /***************************************************************
  * Catch the enter/select event here for typeahead
  ***************************************************************/
  $scope.$on('$typeahead.select', function(event, value, index) {/*jshint unused: false*/
    $scope.applyFilters();
  });

  /*******************************************************************************
  * This function watches for the view content loaded event and runs a timeout 
  * function to handle the initial movement of the display buttons.
  *******************************************************************************/
  $scope.$on('$viewContentLoaded', function(){
    resetAnimations($('.page1'), $('.page2'), $('.filters'));
    $timeout(function() {
      // moveButtons($('#showPageRight'), $('.page1'));
      // moveButtons($('#showPageLeft'), $('.page2'));
      if (fullClick === 0) {
        if ($(window).width() >= 768) {
          if (filtClick === 0) {
            openFiltersToggle();
          }
        }
      }
    }, 1000);
  });



  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  // Here we put our Scope Watchers
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /***************************************************************
  * This function is used to watch the pagenumber variable. When it changes
  * we need to readjust the pagination
  ***************************************************************/
  $scope.$watch('pageNumber',function(val, old){ /* jshint unused:false */
    $scope.pageNumber = parseInt(val);
    if ($scope.pageNumber < 1) {
      $scope.pageNumber = 1;
    }
    if ($scope.pageNumber > $scope.maxPageNumber) {
      $scope.pageNumber = $scope.maxPageNumber;
    }

    var page = $scope.pageNumber;
    if (page < 1 || page === '' || isNaN(page) || page === null){
      page = 1;
    }

    if ($scope.filteredTotal) {
      $scope.data.data = $scope.filteredTotal.slice(((page - 1) * $scope.rowsPerPage), (page * $scope.rowsPerPage));
    } else {
      $scope.data.data = [];
    }
    $scope.applyFilters();

  });

  /***************************************************************
  * This function is used to watch the rowsPerPage variable. When it changes
  * we need to adjust pagination
  ***************************************************************/
  $scope.$watch('rowsPerPage',function(val, old){ /* jshint unused:false */
    var rowPP = $scope.rowsPerPage;
    if (rowPP < 1 || rowPP === '' || isNaN(rowPP) || rowPP === null){
      rowPP = 1;
    }
    $scope.pageNumber = 1;
    if ($scope.filteredTotal) {
      $scope.maxPageNumber = Math.ceil($scope.filteredTotal.length / rowPP);
    }
    $scope.applyFilters();
  });

  /***************************************************************
  * This function is used to watch the orderProp variable. When it changes
  * re-filter the data
  ***************************************************************/
  $scope.$watch('orderProp',function(val, old){ /* jshint unused:false */
    $scope.applyFilters();
  });

  /***************************************************************
  * This function is used to watch the query variable. When it changes
  * re-filter the data
  ***************************************************************/
  $scope.$watch('query',function(val, old){ /* jshint unused:false */
    $scope.applyFilters();
  });

  /***************************************************************
  * This function is used to watch the query variable. When it changes
  * re-filter the data
  ***************************************************************/
  $scope.$watch('ratingsFilter',function(val, old){ /* jshint unused:false */
    $scope.applyFilters();
  });

  /***************************************************************
  * This function is used to watch filters in order to show the 'applied'
  * message so that they won't forget one of the filters is applied.
  ***************************************************************/
  $scope.$watch('filters',function(val, old){ /* jshint unused:false */
    _.each($scope.filters, function(filter){
      filter.hasChecked = _.some(filter.codes, function(item){
        return item.checked;
      });
      if (!filter.hasChecked) {
        filter.checked = false;
      }
    });
  }, true);

  /***************************************************************
  * This function is a deep watch on the data variable to see if 
  * data.data changes. When it does, we need to see if the result set
  * for the search results is larger than the 'max' displayed
  ***************************************************************/
  $scope.$watch('data', function() {
    if ($scope.data && $scope.data.data) {
      // max needs to represent the total number of results you want to load
      // on the initial search.
      var max = 2000;
      // also, we'll probably check the total number of possible results that
      // could come back from the server here instead of the length of the
      // data we have already.
      if ($scope.data.data.length > max) {
        $scope.moreThan200 = true;
      } else {
        $scope.moreThan200 = false;
      }
    }
  }, true);

  callSearch();
  
}]);

