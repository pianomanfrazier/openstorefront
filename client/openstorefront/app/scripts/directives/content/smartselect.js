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

app.directive('smartselect', ['$timeout', function($timeout) {
  // id needs to be unique across all implementations of the directive
  var nextId2 = 1;
  return {
    scope: {
      smartselect: '@',
      ngModel: '='
    },
    restrict: 'A',
    link: function postlink(scope, element, attrs) {
      var init = false;
      $timeout(function(){
        element.combobox({
          'appendTo': scope.smartselect || 'body'
        });
        scope.$watch('ngModel', function(nval, oval){
          if (init && !nval) {
            $timeout(function(){
              element.trigger('change');
            })
          }
          init = true;
        })
      }, 500);
    }
  };
}]);
