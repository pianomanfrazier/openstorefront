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

/*global isEmpty*/

app.controller('SubmissionCtrl', ['$scope', 'localCache', 'business', '$filter', '$timeout', '$location', '$rootScope', '$q', '$route', '$anchorScroll', 'FileUploader', '$templateCache', '$uiModal', '$sce',
  function ($scope,  localCache, Business, $filter, $timeout, $location, $rootScope, $q, $route, $anchorScroll, FileUploader, $templateCache, $uiModal, $sce) { /*jshint unused: false*/

    $scope.test = 'This is a test';
    $scope.badgeFound = false;

    $scope.submitter = {};
    $scope.submitter.firstName;
    $scope.submitter.lastName;
    $scope.submitter.email;
    $scope.submitter.organization;
    $scope.current = 'top';
    $scope.optIn = false;


    $scope.componentId = null;
    $scope.component = {};
    $scope.backup = {};
    $scope.component.component = {};
    $scope.component.attributes = {};

    $scope.component.metadata = [];
    $scope.metadataForm = {};

    $scope.component.tags = [];
    $scope.tagsForm = {};


    $scope.contactForm = {};
    $scope.component.contacts = [];

    $scope.component.media = [];
    $scope.mediaForm = {};
    $scope.showMediaUpload = 'true';
    $scope.isFull = false;


    $scope.component.resources = [];
    $scope.resourceForm = {};
    $scope.showResourceUpload = 'false';
    $scope.isFullResource = false;


    $scope.dependencyForm = {};
    $scope.component.externalDependencies = [];

    $scope.details = {};

    $scope.formMedia;
    $scope.config = {
      sources: [
      // {src: $sce.trustAsResourceUrl("http://familyhistorydatabase.org/tempfiles/Never Be Alone.mp3"), type: "audio/mp3"},
      {src: $sce.trustAsResourceUrl("http://static.videogular.com/assets/videos/videogular.mp4"), type: "video/mp4"},
      {src: $sce.trustAsResourceUrl("http://static.videogular.com/assets/videos/videogular.webm"), type: "video/webm"},
      {src: $sce.trustAsResourceUrl("http://static.videogular.com/assets/videos/videogular.ogg"), type: "video/ogg"}
      ],
      tracks: [
      {
        src: "http://www.videogular.com/assets/subs/pale-blue-dot.vtt",
        kind: "subtitles",
        srclang: "en",
        label: "English",
        default: ""
      }
      ],
      theme: "bower_components/bower_components/videogular-themes-default/videogular.css"
      // plugins: {
      //   poster: "http://www.videogular.com/assets/images/videogular.png"
      // }
    };


    $scope.formFocused = function(form, reset){

      var keys = _.keys(form);
      if (!reset){
        for (var i = 0; i < keys.length; i++){
          if (keys[i][0] !== '$'){
            if (form[keys[i]].$focused){
              return true;
            }
          }
        }
        return false;
      } else {
        // console.log('form', form);
        // console.log('form', $scope);
        for (var i = 0; i < keys.length; i++){
          if (keys[i][0] !== '$'){
            form[keys[i]].$hasBeenFocused = false
          }
        }
      }
    }

    $scope.getMimeTypeClass = function(type){
      if (type.match('video.*')) {
        return 'fa-file-video-o'
      } else if (type.match('audio.*')){
        return 'fa-file-audio-o'
      } else if (type.match('application.*')){
        return 'fa-file-code-o'
      } else if (type.match('text.*')){
        return 'fa-file-text-o'
      } else if (type.match('image.*')){
        return 'fa-file-image-o'
      } else {
        return 'fa-file-o'
      }
    }

    $scope.setBadgeFound = function(attribute){
      if (attribute && attribute.badgeUrl) {
        $scope.badgeFound = true;
      }
    }

    $scope.getAttributeTypeDesc = function(type){
      var found = _.find($scope.allAttributes, {'attributeType': type});
      if (found) {
        return found.description;
      }
      return '';
    }

    $scope.initialSave = function($event){
      if ($scope.vitalsCheck()){
        $scope.submit(true).then(function(){
          $scope.scrollTo('details', 'details', '', $event);
        }, function(){
          if($event) {
            $event.preventDefault();
            $event.stopPropagation();
          }
        })
      } 
    }

    $scope.updateSave = function($event){
      if ($scope.vitalsCheck()){
        $scope.submit(false).then(function(){
          $scope.scrollTo('reviewAndSubmit', 'submit', '', $event)
          $scope.detailsDone = true;
        }, function(){
          if($event) {
            $event.preventDefault();
            $event.stopPropagation();
          }
        })
      } 
    }

    $scope.createInitialSubmit = function(){
      $scope.component.component = $scope.component.component || {};
      $scope.component.component.activeStatus = $scope.component.activeStatus || 'A';
      if ($scope.componentId) {
        $scope.component.component.componentId = $scope.componentId; 
      }
      var component = angular.copy($scope.component);
      component.attributes = $scope.getCompactAttributes(true);
      _.each($scope.allAttributes, function(attribute) {
        if (attribute.hideOnSubmission) {
          var found = _.find(attribute.codes, {'code':attribute.defaultAttributeCode});
          var exists = _.find(component.attributes, {'attributeType': attribute.attributeType});
          if (found && !exists) {
            component.attributes.push({
              componentAttributePk: {
                attributeCode: found.code,
                attributeType: attribute.attributeType
              }
            });
          }
        }
      });
      var submitter = angular.copy($scope.submitter);
      submitter.contactType = 'SUB';

      var found = _.find(component.contacts, {'contactType': 'SUB'});
      if (found) {
        var index = _.indexOf(component.contacts, found);
        component.contacts[index] = submitter
      } else {
        component.contacts.push(submitter);
      }
      

      var deferred = $q.defer();

      // console.log('$scope.component', $scope.component);
      deferred.resolve(component);

      return deferred.promise;
    }


    $scope.submit = function(initial){
      var deferred = $q.defer();
      if (initial){
        $scope.createInitialSubmit().then(function(component){
          var compare = angular.copy(component);
          for(var i = 0; i < compare.attributes.length; i++){
            compare.attributes[i] = {
              componentAttributePk: compare.attributes[i].componentAttributePk
            };
          }
          _.each(compare.attributes, function(attribute){
          })
          // console.log('INIT Diff', compare);
          // console.log('INIT Diff', $scope.backup);
          // console.log('INIT Diff', _.diff(compare,$scope.backup));
          Business.submissionservice.createSubmission(component).then(function(result){
            if (result && result.component && result.component.componentId){
              $scope.backup = angular.copy(result);              
              $scope.componentId = result.component.componentId;
              $scope.component = result;
              $scope.component.attributes = $scope.setupAttributes($scope.component.attributes);
            }
            // console.log('Success result', $scope.component);
            deferred.resolve();
          }, function(result){
            deferred.reject();
            // console.log('Fail result', result);
          });
        })
      } else {
        $scope.createInitialSubmit().then(function(component){
          component.attributes = $scope.getCompactAttributes(true);
          if ($scope.optIn) {
            component.component.notifyOfApprovalEmail = $scope.email;
          }

          component.contacts = component.contacts || [];

          _.each(component.contacts, function(contact){
            if (contact.contactType && contact.contactType.code){
              contact.contactType = contact.contactType.code;
            } else if (contact.contactType) {
              // console.log('contactType missing?', contact.contactType);
            }
          })

          // console.log('$scope.component', component);
          var compare = angular.copy(component);
          for(var i = 0; i < compare.attributes.length; i++){
            compare.attributes[i] = {
              componentAttributePk: compare.attributes[i].componentAttributePk
            };
          }
          _.each(compare.attributes, function(attribute){
          })
          // console.log('UPDATE Diff', compare);
          // console.log('UPDATE Diff', $scope.backup);
          // console.log('UPDATE Diff', _.diff(compare,$scope.backup));
          Business.submissionservice.updateSubmission(component).then(function(result){
            if (result && result.component && result.component.componentId){
              $scope.backup = angular.copy(result);              
              $scope.componentId = result.component.componentId;
              $scope.component = result;
              $scope.component.attributes = $scope.setupAttributes($scope.component.attributes);
            }
            deferred.resolve();
            // console.log('Success result', result);
          }, function(result){
            deferred.reject();
            // console.log('Fail result', result);
          });
        })
      }//
      return deferred.promise;
    }

    $scope.$watch('current', function(){
      $scope.badgeFound = false;
      if ($scope.current && $scope.current === 'submit') {
        $scope.badgeFound = false;
        _.each($scope.component.attributes, function(attribute){
          $scope.setBadgeFound(attribute);
        })
      }
    })



    $scope.checkAttributes = function(){
      // console.log('Compact list', _.compact($scope.component.attributes));
      // we need to compact the attributes list because it may have unused indexes.
      var list = angular.copy($scope.component.attributes);
      
      var requiredAttributes = _.filter(list, function(n){
        return n.requiredFlg && !n.hideOnSubmission;
      });
      
      if ((requiredAttributes.length )!== $scope.requiredAttributes.length) {
        return false;
      }
      return true;
    }

    $scope.setDefaultAttribute = function(index, attribute, required){

      if (required && !$scope.component.attributes[index]) {
        var found = _.find($scope.requiredAttributes, {'attributeType': attribute.attributeType});
        if (attribute.defaultAttributeCode) {
          found = _.find(attribute.codes, {code: attribute.defaultAttributeCode});
          if (found) {
            $scope.component.attributes[index] = found;
          }
        }
      } else {
        var found = _.find($scope.attributes, {'attributeType': attribute.attributeType});
        if (attribute.defaultAttributeCode) {
          found = _.find(attribute.codes, {code: attribute.defaultAttributeCode});
          if (found) {
            $scope.component.attributes[index] = found;
          }
        }
      }
    }

    $scope.getCompactAttributes = function(attributePK){
      // This is how we'll weed out the attributes we need for the submission
      var realAttributes = $scope.component.attributes;
      var attributes = [];
      _.each(realAttributes, function(attr){
        if (attr.constructor === Array){
          _.each(attr, function(item){
            if (attributePK && !item.componentAttributePk) {
              item.componentAttributePk = {
                'attributeType': item.attributeType,
                'attributeCode': item.code,
              };
            }
            attributes.push(item);
          })
        } else {
          if (attributePK && !attr.componentAttributePk) {
            attr.componentAttributePk = {
              'attributeType': attr.attributeType,
              'attributeCode': attr.code,
            };
          }
          attributes.push(attr);
        }
      })

      
      return attributes;      
    }

    $scope.setupAttributes = function(attributes){
      var result = {};
      _.each(attributes, function(attribute){
        var foundAttr = _.find($scope.allAttributes, {'attributeType': attribute.componentAttributePk.attributeType});
        if (foundAttr) {
          var foundAttr = $filter('makeattribute')(foundAttr.codes, foundAttr);
          var found = _.find(foundAttr, {'code': attribute.componentAttributePk.attributeCode});
          var merged = _.merge(found, attribute);
          if (merged.requiredFlg) {
            result[attribute.componentAttributePk.attributeType] = merged;
          } else {
            if (!result[attribute.componentAttributePk.attributeType]) {
              result[attribute.componentAttributePk.attributeType] = []
            } 
            result[attribute.componentAttributePk.attributeType].push(merged);
          }
        }
      })
      return result;
    }

    $scope.getComponent = function(){
      return JSON.stringify($scope.component, null, 4);
    }

    // Metadata section
    $scope.removeMetadata = function(index){
      $scope.component.metadata.splice(index, 1);
    }
    $scope.addMetadata = function(form){
      if ($scope.metadataForm.value && $scope.metadataForm.label) {
        $scope.component.metadata.push($scope.metadataForm);
        $scope.metadataForm = {};
        $scope.formFocused(form, true)
        $('#metadataLabel').focus();
      }
    }

    // tag section
    $scope.removeTag = function(index){
      $scope.component.tags.splice(index, 1);
    }
    $scope.addTag = function(form){
      if ( $scope.tagsForm.text ) {
        var found = _.find($scope.component.tags, {'text':$scope.tagsForm.text});
        if (!found) {
          $scope.component.tags.push($scope.tagsForm);
          $scope.tagsForm = {};
          $scope.formFocused(form, true)
          $('#tagLabel').focus();
        }
      }
    }

  // contact section
  $scope.getContactTypeDesc = function(type){
    var found = _.find($scope.contactTypes, {'code': type});
    return  found? found.description : type;
  }
  $scope.removeContact = function(index){
    var originalLength = $scope.component.contacts.length;
    var afterLength = $filter('contactsfilter')($scope.component.contacts, 'contactType').length;
    index = index + (originalLength - afterLength);
    $scope.component.contacts.splice(index, 1);
  }
  $scope.addContact = function(form){
    if ( $scope.contactForm ) {
      $scope.component.contacts.push($scope.contactForm);
      $scope.contactForm = {};
      $scope.formFocused(form, true)
      $('#contactType').focus();
    }
  }
  $scope.getContactType = function(type){
    var found = _.find($scope.contactTypes, {'code': type});
    return found? found.description: type;
  }


  // Media section
  $scope.oldMediaState = $scope.showMediaUpload;
  $scope.toggleShowMedia = function(val){
    if (val !== $scope.oldMediaState) {
      $scope.oldMediaState = val;
      $('#mediaUploadInput').val(null);
      $scope.mediaForm.typeCode = null;
      $scope.mediaForm.caption = null;
      $scope.mediaForm.link = null;
      $scope.lastMediaFile = '';
    }
  }


  $scope.resetMediaInput = function(){
    $('#mediaUploadInput').val(null);
    $scope.mediaForm.typeCode = null;
    $scope.mediaForm.caption = null;
    $scope.lastMediaFile = '';
  }

  $scope.addLinkToMedia = function(){
    $scope.component.media.push({
      mediaTypeCode: $scope.mediaForm.typeCode.code,
      typeCode: $scope.mediaForm.typeCode,
      caption: $scope.mediaForm.caption,
      link: $scope.mediaForm.link
    })
    $('#mediaUploadInput').val(null);
    $scope.mediaForm.typeCode = null;
    $scope.mediaForm.link = null;
    $scope.mediaForm.caption = null;
    $scope.lastMediaFile = '';
  }  


  // Resource section
  $scope.oldResourceState = $scope.showResourceUpload;
  $scope.toggleShowResource = function(val){
    if (val !== $scope.oldResourceState) {
      $scope.oldResourceState = val;
      $('#resourceUploadInput').val(null);
      $scope.resourceForm.typeCode = null;
      $scope.resourceForm.caption = null;
      $scope.resourceForm.link = null;
      $scope.lastResourceFile = '';
    }
  }


  $scope.resetResourceInput = function(){
    $('#resourceUploadInput').val(null);
    $scope.resourceForm.typeCode = null;
    $scope.resourceForm.caption = null;
    $scope.lastResourceFile = '';
  }

  $scope.addLinkToResource = function(){
    $scope.component.resources.push({
      resourceType: $scope.resourceForm.typeCode.code,
      typeCode: $scope.resourceForm.typeCode,
      description: $scope.resourceForm.caption,
      link: $scope.resourceForm.link
    })
    $('#resourceUploadInput').val(null);
    $scope.resourceForm.typeCode = null;
    $scope.resourceForm.link = null;
    $scope.resourceForm.caption = null;
    $scope.lastResourceFile = '';
  }

  // contact section
  $scope.removeDependency = function(index){
    $scope.component.externalDependencies.splice(index, 1);
  }
  $scope.addDependency = function(form){
    // console.log('$scope.dependencyForm', $scope.dependencyForm);
    
    if ( $scope.dependencyForm ) {
      $scope.component.externalDependencies.push($scope.dependencyForm);
      $scope.formFocused(form, true)
      $scope.dependencyForm = {};
      $('#dependencyFormName').focus();
    }
  }

  // validation section
  $scope.getStarted = function(){
    // return true;
    return $scope.submitter.firstName && $scope.submitter.lastName && $scope.submitter.email && $scope.submitter.organization;
  }

  $scope.vitalsCheck = function(log){
    // return true;
    if (false){
      console.log('getStarted', $scope.getStarted());
      console.log('component', $scope.component);
    }
    
    return $scope.getStarted() && $scope.component && $scope.component.component && $scope.component.component.name && $scope.component.component.description && $scope.component.component.organization && $scope.checkAttributes();
  }



  $scope.loadLookup = function(lookup, entity, loader){
    $scope.$emit('$TRIGGERLOAD', loader);

    Business.lookupservice.getLookupCodes(lookup, 'A').then(function (results) {
      $scope.$emit('$TRIGGERUNLOAD', loader);
      if (results) {
        $scope[entity]= results;
      }        
    });      
  };

  $scope.openInfo = function(attribute){
    var modalInstance = $uiModal.open({
      template: $templateCache.get('submission/attributesinfo.tpl.html'),
      controller: 'AttrsInfoCtrl',
      size: 'sm',
      resolve: {
        size: function() {
          return 'sm';
        },
        attribute: function() {
          return attribute;
        }
      }
    });

    modalInstance.result.then(function (result) {
    }, function () {
    });
  }

  $scope.getAttributes = function (override) { 
    Business.getFilters(override, false).then(function (result) {
      $scope.allAttributes = result ? angular.copy(result) : [];
      $scope.requiredAttributes = _.filter($scope.allAttributes, {requiredFlg: true, hideOnSubmission: false});
      // console.log('required', $scope.requiredAttributes);

      $scope.attributes = _.filter($scope.allAttributes, {requiredFlg: false});
    });
  }; 
  $scope.getAttributes();
  
  $scope.getCodesForType = function(type){
    var foundType = _.find($scope.allAttributes, {attributeType: type});
    return foundType !== undefined ? foundType.codes : [];
  }; 

  $scope.loadLookup('ContactType', 'contactTypes', 'contactFormLoader');  
  $scope.loadLookup('MediaType', 'mediaTypes', 'mediaFormLoader'); //
  $scope.loadLookup('ResourceType', 'resourceTypes', 'resourceFormLoader');  

  

  // Media Handling functions
  $scope.srcList = []; //
  $scope.queue = [];
  $scope.resourceQueue = [];
  $scope.addMedia = function (file, queue, form, loader) { //
    // if ($scope.mediaForm.link || 
    //   $scope.mediaUploader.queue.length === 0) {

    //   if (!$scope.mediaForm.link) {          
    //     $scope.mediaForm.originalName = $scope.mediaForm.originalFileName;  
    //   } else {
    //     $scope.mediaForm.mimeType = '';
    //   }

      // $scope.saveEntity({
      //   alertId: 'saveMedia',
      //   alertDiv: 'componentWindowDiv',
      //   loader: 'mediaFormLoader',
      //   entityName: 'media',
      //   entity: $scope.mediaForm,
      //   entityId: $scope.mediaForm.componentMediaId,
      //   formName: 'mediaForm',
      //   loadEntity: function () {
      //     $scope.loadMedia();
      //   }
      // });
    // } else {
      file.typeCode = $scope[form].typeCode;
      file.caption = $scope[form].caption;
      file.mimeType = file._file? file._file.type: file.file.type;
      if (file._file){
        $scope.readFile(file._file, function(result){
          queue.push({file: file, dom:result});
          if(!$scope.$$phase) {
            $scope.$apply();
          }
          $scope.$emit('$TRIGGERUNLOAD', loader);
        });
      } else {
        queue.push({file: file, dom:'<span>No Link or Preview Available</span>'});
        if(!$scope.$$phase) {
          $scope.$apply();
        }
        $scope.$emit('$TRIGGERUNLOAD', loader);
      }
      // $scope.mediaUploader.uploadAll();
    // }
  };

  $scope.readFile = function(file, callback){

    var reader;
    if (file.type.match('image.*')) {
      // console.log('file ===  image', file);
      var reader = new FileReader();
      // Closure to capture the file information.
      reader.onload = (function(theFile, callback) {

        return function(e) {
          // Render thumbnail.
          callback(
           ['<img class="thumb" src="', e.target.result,
           '" title="', escape(theFile.name), '" width="230"    height="270"/>'].join('')
           )
        };
      })(file, callback);
      reader.readAsDataURL(file);
    }else if (file.type.match('audio.*')){
      var reader = new FileReader();
      // Closure to capture the file information.
      reader.onload = (function(theFile, callback) {
        return function(e) {
          // Render thumbnail.

          callback(
            ['<audio controls><source src="', e.target.result,'   "type="audio/ogg"><source src="', e.target.result,' "type="audio/mpeg"></audio>'].join('')
            )
        };
      })(file, callback);
      reader.readAsDataURL(file);
    }else if (file.type.match('video.*')){
      var URL = window.URL || window.webkitURL;
      var type = file.type;

      var videoNode = document.createElement('video');

      var canPlay = videoNode.canPlayType(type);

      canPlay = (canPlay === '' ? 'no' : canPlay);

      var message = 'Can play type "' + type + '": ' + canPlay;

      var isError = canPlay === 'no';

      // console.log(message, isError);

      if (isError) {
        return;
      }

      var fileURL = URL.createObjectURL(file);
      if (!URL) {
        callback('<span>Your browser is not <a href="http://caniuse.com/bloburls">supported</a>!</span>')
      }   
      var srcs = [];
      srcs.push({src: $sce.trustAsResourceUrl(fileURL).$$unwrapTrustedValue(), type: 'video/mp4'});             
      srcs.push({src: $sce.trustAsResourceUrl(fileURL).$$unwrapTrustedValue(), type: 'video/webm'});             
      srcs.push({src: $sce.trustAsResourceUrl(fileURL).$$unwrapTrustedValue(), type: 'video/ogg'});             
      callback(
        ['<videogular> <vg-media vg-src=\''+JSON.stringify(srcs)+'\' vg-preload="\'none\'" vg-native-controls="true"></vg-media></videogular>'].join('')
        )
    } else {
      callback('<span>No Link or Preview Available</span>')
    }
  }

  // media uploader
  $scope.mediaUploader = new FileUploader({ //
    url: 'Media.action?UploadMedia',
    alias: 'file',
    queueLimit: 50,  
    removeAfterUpload: true,
    onAfterAddingFile: function(file){
      // console.log('We loaded the loader!', file.file);
      console.dir(file.file);
      $scope.$emit('$TRIGGERLOAD', 'mediaPreviewLoader');
      if (this.queue.length >= this.queueLimit) {
        $scope.isFull = true;
      }
      if (file._file) {
        $scope.lastMediaFile = file._file.name;
      } else if (file.file) {
        $scope.lastMediaFile = file.file.name;
      }
      $scope.addMedia(file, $scope.queue, 'mediaForm', 'mediaPreviewLoader');
      $scope.resetMediaInput();
    },
    onBeforeUploadItem: function(item) {
      $scope.$emit('$TRIGGERLOAD', 'mediaFormLoader');

      item.formData.push({
        "componentMedia.componentId" : $scope.componentForm.componentId
      });
      item.formData.push({
        "componentMedia.typeCode" : $scope.mediaForm.typeCode
      });
      if ($scope.mediaForm.caption) {
        item.formData.push({
          "componentMedia.caption": $scope.mediaForm.caption
        });
      }
      if ($scope.mediaForm.componentMediaId) {
        item.formData.push({
          "componentMedia.componentMediaId": $scope.mediaForm.componentMediaId
        });
      }
    },
    onSuccessItem: function (item, response, status, headers) {
      $scope.$emit('$TRIGGERUNLOAD', 'mediaFormLoader');

      //check response for a fail ticket or a error model
      if (response.success) {
        triggerAlert('Uploaded successfully', 'saveResource', 'componentWindowDiv', 3000);          
        $scope.cancelMediaEdit();
        $scope.loadMedia();          
      } else {
        if (response.errors) {
          var uploadError = response.errors.file;
          var enityError = response.errors.componentMedia;
          var errorMessage = uploadError !== undefined ? uploadError : '  ' + enityError !== undefined ? enityError : '';
          triggerAlert('Unable to upload media. Message: <br> ' + errorMessage, 'saveMedia', 'componentWindowDiv', 6000);
        } else {
          triggerAlert('Unable to upload media. ', 'saveMedia', 'componentWindowDiv', 6000);
        }
      }
    },
    onErrorItem: function (item, response, status, headers) {
      $scope.$emit('$TRIGGERUNLOAD', 'mediaFormLoader');
      triggerAlert('Unable to upload media. Failure communicating with server. ', 'saveMedia', 'componentWindowDiv', 6000);        
    }      
  });     


  // Resource uploader
  $scope.resourceUploader = new FileUploader({//
    url: 'Resource.action?UploadResource',
    alias: 'file',
    queueLimit: 50,  
    removeAfterUpload: true,
    onAfterAddingFile: function(file){
      // console.log('We loaded the loader!', file.file);
      // console.dir(file.file);
      $scope.$emit('$TRIGGERLOAD', 'resourcePreviewLoader');
      if (this.queue.length >= this.queueLimit) {
        $scope.isFull = true;
      }
      if (file._file) {
        $scope.lastResourceFile = file._file.name;
      } else if (file.file) {
        $scope.lastResourceFile = file.file.name;
      }
      $scope.addMedia(file, $scope.resourceQueue, 'resourceForm', 'resourcePreviewLoader');
      $scope.resetResourceInput();
    },
    onBeforeUploadItem: function(item) {
      $scope.$emit('$TRIGGERLOAD', 'resourceFormLoader');

      item.formData.push({
        "componentResource.componentId" : $scope.componentForm.componentId
      });
      item.formData.push({
        "componentResource.resourceType" : $scope.resourceForm.resourceType
      });
      item.formData.push({
        "componentResource.description" : $scope.resourceForm.description
      });
      item.formData.push({
        "componentResource.restricted" : $scope.resourceForm.restricted
      });        
      if ($scope.resourceForm.resourceId) {
        item.formData.push({
          "componentResource.resourceId" : $scope.resourceForm.resourceId
        });
      }
    },
    onSuccessItem: function (item, response, status, headers) {
      $scope.$emit('$TRIGGERUNLOAD', 'resourceFormLoader');

      //check response for a fail ticket or a error model
      if (response.success) {
        triggerAlert('Uploaded successfully', 'saveResource', 'componentWindowDiv', 3000); 
        $scope.cancelResourceEdit();
        $scope.loadResources();
      } else {
        if (response.errors) {
          var uploadError = response.errors.file;
          var enityError = response.errors.componentResource;
          var errorMessage = uploadError !== undefined ? uploadError : '  ' + enityError !== undefined ? enityError : '';
          triggerAlert('Unable to upload resource. Message: <br> ' + errorMessage, 'saveResource', 'componentWindowDiv', 6000);
        } else {
          triggerAlert('Unable to upload resource. ', 'saveResource', 'componentWindowDiv', 6000);
        }
      }
    },
    onErrorItem: function (item, response, status, headers) {
      $scope.$emit('$TRIGGERUNLOAD', 'resourceFormLoader');
      triggerAlert('Unable to upload resource. Failure communicating with server. ', 'saveResource', 'componentWindowDiv', 6000);      
    }      
  });

$scope.scrollTo = function(id, current, parent, $event) {
  var offset = 120;
  if($event) {
    $event.preventDefault();
    $event.stopPropagation();
  }
  $('li a:focus').blur();
  $scope.current = current;
  $timeout(function(){
    $('[data-spy="scroll"]').each(function () {
      var $spy = $(this).scrollspy('refresh')
    })

    $timeout(function(){
      if ($location.hash() !== id) {
          // set the $location.hash to `newHash` and
          // $anchorScroll will automatically scroll to it
          $location.hash(id);
        } else {
          // call $anchorScroll() explicitly,
          // since $location.hash hasn't changed
          $anchorScroll();
        }
        $timeout(function(){
          $scope.resetToggles();
          
          var topScroll = $(document).height() - ($(window).scrollTop() + $(window).height()) ;
          var returnScroll = ($(window).scrollTop() - $('#'+id).offset().top);
          if (topScroll === 0 && returnScroll < 0 ) {
            returnScroll = offset - (returnScroll  * -1);
          } else {
            returnScroll = offset
          }
          window.scrollBy(0, -returnScroll);
        })
        $timeout(function(){
          $scope.resetToggles();
          $('li.active').removeClass('active');
          if (parent) {
            $('[data-target="#'+parent+'"]').addClass('active');
          }
          $('[data-target="#'+id+'"]').addClass('active');
        },100)
      })
    }) //
  };//


  $timeout(function(){
    if(!$scope.$$phase) {
      $scope.$apply(function(){
        $('body').scrollspy({ target: '#scrollSpy', offset: 100 });
        $scope.scrollTo('top', 'top');
      })
    }
  })

  $scope.resetToggles = function(){
    $timeout(function() {
      $('[data-toggle=\'tooltip\']').tooltip();
    }, 300);
  }
  $scope.resetToggles();

}])
.filter('makeattribute', function() {
  return function(input, attribute) {
    _.each(input, function(code){
      if (code) {
        code.requiredFlg = attribute.requiredFlg || false;
        code.hideOnSubmission = attribute.hideOnSubmission || false;
        code.attributeType = attribute.attributeType;
      }
    })
    return input;
  };
})
.filter('shownattribute', function() {
  return function(input) {
    var result = [];
    _.each(input, function(attribute){
      if (attribute && !attribute.hideOnSubmission) {
        result.push(attribute)
      }
    })
    return result;
  };
})
.filter('contactsfilter', function() {
  return function(input, key) {
    if (!input || !input.length || !key) {
      return input;
    }
    return _.reject(input, function(n){
      return n[key] === 'SUB';
    });
  };
})
.controller('AttrsInfoCtrl', ['$scope', '$uiModalInstance', 'size', 'attribute', 'notificationsFactory', '$timeout', function ($scope, $uiModalInstance, size, attribute, Factory, $timeout) {
  $scope.attribute = angular.copy(attribute);
  $scope.title = attribute.description;

  $scope.getDescription = function(code) {
    return code.description? '<p>'+code.description+'</p>': '<p style="font-style: italic;">There is no detailed description for this code.</p>';
  }

  $scope.ok = function (validity) {
    $uiModalInstance.close('success');
  }

  $scope.cancel = function () {
    $uiModalInstance.dismiss('cancel');
  };
}])
.controller('myCtrl', function($scope) {
  $scope.options = [{
    'label': 'test 1',
    'code': 'CODE1'
  }, {
    'label': 'test 2',
    'code': 'CODE2'
  }]
})
.directive('multiselect', ['$templateCache', '$timeout', function($templateCache, $timeout) {
  return {
    restrict: 'E',
    replace: true,
    scope: {
      selected: '=',
      options: '@',
      list: '=',
      onChange: '&'
    },
    template: $templateCache.get('multiselect/select.tmp.html'),
    link: function(scope, elem, attrs) {
      scope.addToSelection = function(selection){
        scope.selected = scope.selected && scope.selected.constructor === Array? scope.selected: [];
        _.contains(scope.selected, selection) || !selection? '':scope.selected.push(selection);
        scope.onChange(true);
      }
      scope.removeItem = function(item){
        var index = _.find(scope.selected, {label: item.label});
        if (index) {
          index = _.indexOf(scope.selected, index);
          scope.selected.splice(index, 1);
        }
        if (scope.selected.length === 0){
          var elements = elem.find('select')[0].options;

          for(var i = 0; i < elements.length; i++){
            elements[i].selected = false;
          }
        }
      }
    }
  };
}]);