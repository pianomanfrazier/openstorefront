/* 
 * Copyright 2015 Space Dynamics Laboratory - Utah State University Research Foundation.
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

'use strict';

app.controller('helpCtrl', ['$scope', '$draggableInstance', 'business', 
  function ($scope, $draggableInstance, Business) {
 
    $scope.control = {};
    $scope.control.showHelp = true;
   
    $scope.help = {};
    $scope.helpSection = {};     
   
    $scope.loadHelp = function() {
      $scope.$emit('$TRIGGERLOAD', 'tagsLoader');   
      Business.systemservice.getHelp().then(function(results){
        $scope.$emit('$TRIGGERUNLOAD', 'tagsLoader');
        $scope.help = results;        
        $scope.showHelpSection($scope.help.helpSection);        
      }, function(results) {
        $scope.$emit('$TRIGGERUNLOAD', 'tagsLoader');
      });
    };
    $scope.loadHelp();
   
   $scope.showHelpSection = function(helpSection) {
     
     if ($scope.helpSection.selected) {
       $scope.helpSection.selected = false;
     }
     $scope.helpSection = helpSection;     
     $scope.helpSection.selected = true;
   };
   
   $scope.popout = function(){
      utils.openWindow('help', 'Help');
      $draggableInstance.close();
   };
   
    
}]);


app.controller('helpSingleCtrl', ['$scope', 'business', 
  function ($scope, Business) {
 
    $scope.control = {};
    $scope.control.showHelp = true;
   
    $scope.help = {};
    $scope.helpSection = {}; 
   
    $scope.loadHelp = function() {
      $scope.$emit('$TRIGGERLOAD', 'tagsLoader');   
      Business.systemservice.getHelp().then(function(results){
        $scope.$emit('$TRIGGERUNLOAD', 'tagsLoader');
        $scope.help = results;        
        $scope.showHelpSection($scope.help.helpSection);        
      }, function(results) {
        $scope.$emit('$TRIGGERUNLOAD', 'tagsLoader');
      });
    };
    $scope.loadHelp();
   
   $scope.showHelpSection = function(helpSection) {
     if ($scope.helpSection.selected) {
       $scope.helpSection.selected = false;
     }
     $scope.helpSection = helpSection;     
     $scope.helpSection.selected = true;    
   };
    
}]);