<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="../../../../client/layout/adminlayout.jsp">
    <stripes:layout-component name="contents">
	
	<script src="scripts/component/integrationConfigWindow.js?v=${appVersion}" type="text/javascript"></script>
		
	<script type="text/javascript">
		/* global Ext, CoreUtil */
		Ext.onReady(function(){	

			var integrationWindow = Ext.create('OSF.component.IntegrationWindow', {	
			});		

			var componentConfigStore = Ext.create('Ext.data.Store', {
				id: 'componentConfigStore',
				autoLoad: true,
				proxy: {
					type: 'ajax',
					url: '/openstorefront/api/v1/resource/components/integration?status=ALL'
				}
			});

			var componentConfigGrid = Ext.create('Ext.grid.Panel', {
				title: 'Component Configuration',
				id: 'componentConfigGrid',
				store: componentConfigStore,
				columnLines: true,
				listeners: {
					selectionchange: function (grid, record, index, opts) {
						if (Ext.getCmp('componentConfigGrid').getSelectionModel().hasSelection()) {
								Ext.getCmp('componentConfigGrid-tools-run').enable();
								Ext.getCmp('componentConfigGrid-tools-edit').enable();
								Ext.getCmp('componentConfigGrid-tools-toggleActivation').enable();
								Ext.getCmp('componentConfigGrid-tools-delete').enable();
								if (record[0].data.activeStatus === 'A') {
									Ext.getCmp('componentConfigGrid-tools-toggleActivation').setText('Deactivate');
								}
								else {
									Ext.getCmp('componentConfigGrid-tools-toggleActivation').setText('Activate');
								}
							} 
						else {
								Ext.getCmp('componentConfigGrid-tools-run').disable();
								Ext.getCmp('componentConfigGrid-tools-edit').disable();
								Ext.getCmp('componentConfigGrid-tools-toggleActivation').disable();
								Ext.getCmp('componentConfigGrid-tools-delete').disable();
						}
					}
				},
				columns: [
					{ text: 'Component', dataIndex: 'componentName', flex: 2},
					{ 
						text: 'Start Time', 
						dataIndex: 'lastStartTime', 
						xtype: 'datecolumn',
						format: 'm/d/y H:i:s A',
						flex: 1
					},
					{ 
						text: 'End Time', 
						dataIndex: 'lastEndTime',
						xtype: 'datecolumn',
						format: 'm/d/y H:i:s A',
						flex: 1
					},
					{ 
						text: 'Status', 
						dataIndex: 'status', 
						flex: 1,
						renderer: function(value, metadata) {
							if (value === 'C') return 'Complete';
							else if (value === 'E') return 'Error';
							else if (value === 'P') return 'Pending';
							else if (value === 'W') return 'Working';
						}
					},
					{ 
						text: 'Active Status', 
						dataIndex: 'activeStatus', 
						flex: 0.5,
						renderer: function(value, metadata) {
							if (value === 'A') {
								metadata.tdCls = 'alert-success';
								return "Active";
							}
							if (value === 'I') {
								metadata.tdCls = 'alert-warning';
								return "Inactive";
							}
						}
					}

				],
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						items: [
							{
								text: 'Refresh',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-refresh',
								handler: function () {
									componentConfigStore.load();
									componentConfigGrid.getSelectionModel().deselectAll();
								}
							},
							{
								xtype: 'tbseparator'
							},
							{
								text: 'Add New Configuration',
								id: 'componentConfigGrid-tools-add',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-plus',
								handler: function () {
									actionAddNewConfiguration();
								}
							},
							{
								xtype: 'tbseparator'
							},
							{
								text: 'Run Job',
								id: 'componentConfigGrid-tools-run',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-bolt',
								disabled: true,
								handler: function () {
									var record = componentConfigGrid.getSelection()[0];
									actionRunJob(record);
								}
							},
							{
								text: 'Edit',
								id: 'componentConfigGrid-tools-edit',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-edit',
								disabled: true,
								handler: function () {
									var record = componentConfigGrid.getSelection()[0];
									actionEditIntegration(record);
								}
							},
							{
								text: 'Deactivate',
								id: 'componentConfigGrid-tools-toggleActivation',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-power-off',
								disabled: true,
								handler: function () {
									var record = componentConfigGrid.getSelection()[0];
									actionToggleIntegration(record);
								}
							},
							{
								text: 'Delete',
								id: 'componentConfigGrid-tools-delete',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-trash',
								disabled: true,
								handler: function () {
									var record = componentConfigGrid.getSelection()[0];
									actionDeleteIntegration(record);
								}
							},
							{
								xtype: 'tbfill'
							},
							{
								text: 'Run All Jobs',
								id: 'componentConfigGrid-tools-runAll',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-bolt',
								tooltip: 'Run all jobs with an active component configuration.',
								handler: function () {
									actionRunAllJobs();
								}
							},


						]
					}
				]
			});
			
			var entryPickWindow = Ext.create('Ext.window.Window' , {
				id: 'entryPickWindow',
				title: 'Choose Entry',
				modal: true,
				width: '40%',
				y: '10em',
				layout: 'fit',
				items: [
					{
						xtype: 'form',
						id: 'entryPickForm',
						style: 'padding: 20px;',
						width: '100%',
						items: [
							{
								xtype: 'label',
								text: 'Choose an entry for integration configuration:'
							},
							{
								xtype: 'combobox',
								displayField: 'description',
								valueField: 'code',
								width: '100%',
								emptyText: 'Select an entry',
								store: Ext.create('Ext.data.Store', {
									id: 'entryPickStore',
									autoLoad: true,
									proxy: {
										type: 'ajax',
										url: '/openstorefront/api/v1/resource/components/lookup?'
									},
									sorters: [{
										property: 'description',
										direction: 'ASC'
									}]
								}),
								listeners: {
									select: function(combo, record, eOpts) {
										integrationWindow.show();
										integrationWindow.loadConfigs(record.getData().code);
										integrationWindow.setTitle('Integration: ' + record.getData().description);
										entryPickWindow.hide();
									}
								}
							}

						]
					}
				]
			})

			var actionAddNewConfiguration = function actionAddNewConfiguration() {
				entryPickWindow.show();
				Ext.getCmp('entryPickForm').reset();
			};

			var actionRunJob = function actionRunJob(record) {
				var componentId = record.getData().componentId;
				var componentName = record.getData().componentName;
				var url = '/openstorefront/api/v1/resource/components/';
				url += componentId + '/integration/run';
				var method = 'POST';

				Ext.Ajax.request({
					url: url,
					method: method,
					success: function (response, opts) {
						var message = 'Successfully started job for "' + componentName + '"';
						Ext.toast(message, '', 'tr');
						Ext.getCmp('componentConfigGrid').getStore().load();
						Ext.getCmp('componentConfigGrid').getSelectionModel().deselectAll();
					},
					failure: function (response, opts) {
						Ext.MessageBox.alert('Failed to' + what,
											 'Error: Could not start job for "' + componentName + '"');
					}
				});

			};

			var actionRunAllJobs = function actionRunAlljobs() {

				Ext.Ajax.request({
					url: '/openstorefront/api/v1/resource/components/integrations/run',
					method: 'POST',
					success: function (response, opts) {
						Ext.toast('Sent request to run all jobs', '', 'tr');
						Ext.getCmp('componentConfigGrid').getStore().load();
						Ext.getCmp('componentConfigGrid').getSelectionModel().deselectAll();
					},
					failure: function (response, opts) {
						Ext.MessageBox.alert('Failed', 'Could not complete request to run all jobs.');
					}
				});


			}

			var actionEditIntegration = function actionEditIntegration(record) {
				integrationWindow.show();
				integrationWindow.loadConfigs(record.getData().componentId);
				integrationWindow.setTitle('Integration: ' + record.getData().componentName);
			};

			var actionToggleIntegration = function actionToggleIntegration(record) {
				var componentId = record.getData().componentId;
				var componentName = record.getData().componentName;
				var activeStatus = record.getData().activeStatus;
				var url = '/openstorefront/api/v1/resource/components/';
				url += componentId + '/integration/';
				var method = 'PUT';
				if (activeStatus === 'A') {
					var what = 'deactivate';
					url += 'inactivate';
				}
				else {
					var what = 'activate';
					url += 'activate';
				}

				Ext.Ajax.request({
					url: url,
					method: method,
					success: function (response, opts) {
						var message = 'Successfully ' + what + 'd integration for "' + componentName + '"';
						Ext.toast(message, '', 'tr');
						Ext.getCmp('componentConfigGrid').getStore().load();
						Ext.getCmp('componentConfigGrid').getSelectionModel().deselectAll();
					},
					failure: function (response, opts) {
						Ext.MessageBox.alert('Failed to' + what,
											 "Error: Could not " + what + ' integration for "' + componentName + '"');
					}
				});
			};

			var actionDeleteIntegration = function actionDeleteIntegration(record) {
				var componentId = record.getData().componentId;
				var componentName = record.getData().componentName;
				var url = '/openstorefront/api/v1/resource/components/';
				url += componentId + '/integration';
				var method = 'DELETE';

				Ext.Ajax.request({
					url: url,
					method: method,
					success: function (response, opts) {
						var message = 'Successfully deleted integration for "' + componentName + '"';
						Ext.toast(message, '', 'tr');
						Ext.getCmp('componentConfigGrid').getStore().load();
						Ext.getCmp('componentConfigGrid').getSelectionModel().deselectAll();
					},
					failure: function (response, opts) {
						Ext.MessageBox.alert('Failed to delete',
											 'Error: Could not delete integration for "' + componentName + '"');
					}
				});
			};


			var jiraConfigStore = Ext.create('Ext.data.Store', {
				id: 'jiraConfigStore',
				autoLoad: true,
				proxy: {
					type: 'ajax',
					url: '/openstorefront/api/v1/resource/attributes/attributexreftypes/detail'
				}
			});

			var addEditMappingWin = Ext.create('Ext.window.Window', {
				id: 'addEditMappingWin',
				title: 'Add/Edit Mapping',
				modal: true,
				width: '60%',
				y: '5em',
				layout: 'fit',
				items: [
					{
						xtype: 'form',
						id: 'addEditMappingForm',
						width: '100%',
						padding: '10px',
						defaults: {
							width: '100%'
						},
						items: [
							{
								xtype: 'combobox',
								id: 'jiraProjectSelection',
								fieldLabel: 'Select a Jira Project:',
								displayField: 'description',
								valueField: 'code',
								store: Ext.create('Ext.data.Store', {
									id: 'jiraProjectStore',
									autoLoad: true,
									proxy: {
										type: 'ajax',
										url: '/openstorefront/api/v1/service/jira/projects'
									}
								}),
								listeners: {
									select: function (combo, record, eOpts) {
										var code = record.getData().code;
										var url = '/openstorefront/api/v1/service/jira/projects/';
										url += code;

										Ext.getCmp('jiraProjectIssueSelection').setStore({
											autoLoad: true,
											proxy: {
												type: 'ajax',
												url: url
											}
										});
									}
								}
							},
							{
								xtype: 'combobox',
								id: 'jiraProjectIssueSelection',
								fieldLabel: 'Select a Jira Project Issue Type:',
								displayField: 'name',
								valueField: 'code',
								editable: false,
								listeners: {
									select: function (combo, record, eOpts) {
										var projectSelection = Ext.getCmp('jiraProjectSelection').getSelection();
										var projectCode = projectSelection.getData().code;
										var issueType = record.getData().name;
										var url = '/openstorefront/api/v1/service/jira/projects/';
										url += projectCode + '/' + issueType + '/fields';

										Ext.getCmp('jiraFieldSelection').setStore({
											autoLoad: true,
											proxy: {
												type: 'ajax',
												url: url
											}
										});
									}
								}
							},
							{
								xtype: 'combobox',
								fieldLabel: 'Select a Storefront Attribute Type:',
								valueField: 'attributeType',
								displayField: 'description',
								store: Ext.create('Ext.data.Store', {
									id: 'attributeTypeStore',
									autoLoad: true,
									proxy: {
										type: 'ajax',
										url: '/openstorefront/api/v1/resource/attributes'
									}
								})
							},
							{
								xtype: 'combobox',
								id: 'jiraFieldSelection',
								displayField: 'name',
								valueField: 'name',
								fieldLabel: 'Select the Jira Field:',
								editable: false,
								listeners: {
									select: function (combo, record, eOpts) {
										console.log(record);
										var allowedValues = record.getData().allowedValues;
										var form = Ext.getCmp('fieldAssignmentForm');
										var jiraCodesStore = Ext.getStore('jiraCodesStore');

										Ext.Array.each(allowedValues, function(value) {
											jiraCodesStore.add(value);
											form.add({
												xtype: 'textfield',
												fieldLabel: value.value
											});
		
										});
										
									}
								}
							},
							{
								xtype: 'panel',
								layout: {
									type: 'hbox',
									align: 'stretch',
									padding: '10px;'
								},
								items: [
									{
										xtype: 'grid',
										store: Ext.create('Ext.data.Store', {
											id: 'jiraCodesStore'
										}),
										flex: 1,
										columns: [
											{ text: 'Jira Codes', dataIndex: 'value', flex: 1},
										]
									},
									{
										xtype: 'form',
										style: {
											paddingLeft: '20px'
										},
										flex: 1,
										id: 'fieldAssignmentForm',
									}
								]
							}
						]
					}
				]
			});

			var jiraConfigGrid = Ext.create('Ext.grid.Panel', {
				title: 'Jira Mapping Configuration',
				id: 'jiraConfigGrid',
				store: jiraConfigStore,
				columns: [
					{ text: 'Project Id', dataIndex: 'projectType', flex: 1},
					{ text: 'Issue Type', dataIndex: 'issueType', flex: 1},
					{ text: 'Attribute', dataIndex: 'attributeName', flex: 2},
					{ text: 'Custom Field', dataIndex: 'fieldName', flex: 2},
				],
				listeners: {
					selectionchange: function (grid, record, index, opts) {
						if (Ext.getCmp('jiraConfigGrid').getSelectionModel().hasSelection()) {
								Ext.getCmp('jiraConfigGrid-tools-edit').enable();
								Ext.getCmp('jiraConfigGrid-tools-delete').enable();
							} 
						else {
								Ext.getCmp('jiraConfigGrid-tools-edit').disable();
								Ext.getCmp('jiraConfigGrid-tools-delete').disable();
						}
					}
				},
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						items: [
							{
								text: 'Refresh',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-refresh',
								handler: function () {
									jiraConfigStore.load();
									jiraConfigGrid.getSelectionModel().deselectAll();
								}
							},
							{
								xtype: 'tbseparator'
							},
							{
								text: 'Add New Mapping',
								id: 'jiraConfigGrid-tools-add',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-plus',
								handler: function () {
									actionAddMapping();
								}
							},
							{
								text: 'Edit',
								id: 'jiraConfigGrid-tools-edit',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-edit',
								disabled: true,
								handler: function () {
									var record = jiraConfigGrid.getSelection()[0];
									actionEditMapping(record);
								}
							},
							{
								text: 'Delete',
								id: 'jiraConfigGrid-tools-delete',
								scale: 'medium',
								iconCls: 'fa fa-2x fa-trash',
								disabled: true,
								handler: function () {
									var record = jiraConfigGrid.getSelection()[0];
									actionDeleteMapping(record);
								}
							}	
						]
					}
				]

			});

			var actionAddMapping = function actionAddMapping() {

			};

			var actionEditMapping = function actionEditMapping(record) {
				addEditMappingWin.show();

			};

			var actionDeleteMapping = function actionDeleteMapping(record) {

			};


			var mainPanel = Ext.create('Ext.tab.Panel', {
				title: 'Manage Integration <i class="fa fa-question-circle"  data-qtip="Allows for the configuration of data integration with external systems such as JIRA"></i>',
				items: [
					componentConfigGrid,
					jiraConfigGrid
				]
			});


			Ext.create('Ext.container.Viewport', {
				layout: 'fit',
				items: [
					mainPanel
				]
			});
			


		});		
	</script>	
		
	</stripes:layout-component>
</stripes:layout-render>	
