/* 
 * Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * See NOTICE.txt for more information.
 */

/* global Ext, CoreUtil, CoreService */

Ext.define('OSF.widget.RecentUserData', {
	extend: 'Ext.panel.Panel',
	alias: 'osf.widget.RecentUserData',

	layout: 'fit',
	
	initComponent: function () {
		this.callParent();
		
		var userDataPanel = this;
		
		userDataPanel.grid = Ext.create('Ext.grid.Panel', {
			columnLines: true,
			store: {
				fields: [
					{
						name: 'updateDts',
						type:	'date',
						dateFormat: 'c'
					}
				],
				proxy: {
					type: 'memory'	,
					reader: {
						type: 'json'
					}
				}
			},
			columns: [
				{ text: 'Data Type', dataIndex: 'dataType', width: 150 },
				{ text: 'Data', dataIndex: 'data', flex: 1, minWidth: 150 },
				{ text: 'User', dataIndex: 'username', width: 150 },
				{ text: 'Update Date', dataIndex: 'updateDts', width: 150, xtype: 'datecolumn', format:'m/d/y H:i:s'  }
			]
		});
		
		userDataPanel.add(userDataPanel.grid);
		userDataPanel.refresh();
		
	},
	refresh: function(){
		var userDataPanel = this;
		
		var data = [];
		userDataPanel.setLoading("Loading Questions...");
		Ext.Ajax.request({
			url: 'api/v1/resource/components/questionviews',
			callback: function() {
				userDataPanel.setLoading(false);
			},
			success: function(response, opts) {
				var questions = Ext.decode(response.responseText);
				userDataPanel.setLoading("Loading Review...");
				Ext.Ajax.request({
					url: 'api/v1/resource/components/reviewviews',
					callback: function() {
						userDataPanel.setLoading(false);
					},					
					success: function(response, opts) {
						var reviews = Ext.decode(response.responseText);
						userDataPanel.setLoading("Loading Tags...");
						Ext.Ajax.request({
							url: 'api/v1/resource/components/tagviews',
							callback: function() {
								userDataPanel.setLoading(false);
							},							
							success: function(response, opts) {
								var tags = Ext.decode(response.responseText);
								
								userDataPanel.setLoading("Loading Contacts...");
								Ext.Ajax.request({
									url: 'api/v1/resource/contacts',
									callback: function() {
										userDataPanel.setLoading(false);
									},									
									success: function(response, opts) {
										var contacts = Ext.decode(response.responseText);
										
										Ext.Array.each(questions, function(item){
											data.push({
												dataType: 'Question',
												data: item.question,
												username: item.username,
												updateDts: item.updateDts
											});
										});
										
										Ext.Array.each(reviews, function(item){
											data.push({
												dataType: 'Reviews',
												data: item.title,
												username: item.username,
												updateDts: item.updateDate
											});
										});
										
										Ext.Array.each(tags, function(item){
											data.push({
												dataType: 'Tags',
												data: item.text,
												username: item.createUser,
												updateDts: item.createDts
											});
										});
										
										Ext.Array.each(contacts.data, function(item){
											if (!item.adminModified && item.updateUser !== 'ANONYMOUS') {												
												data.push({
													dataType: 'Contact',
													data: item.firstName + ' ' + item.lastName,
													username: item.updateUser,
													updateDts: item.updateDts
												});
											}
										});										
										
										//filter by date
										data = Ext.Array.filter(data, function(item){
											var now = new Date();
											var max = Ext.Date.subtract(now, Ext.Date.DAY, 30);
											var updateDts = Ext.Date.parse(item.updateDts, 'c');
											if (updateDts < max) {
												return false;
											} else {
												return true;
											}
										});


										userDataPanel.grid.getStore().loadData(data);
									}
								});
							}
						});
					}
				});				
			}
		});
		
		
		
		
	}

});
