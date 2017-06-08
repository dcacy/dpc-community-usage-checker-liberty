

/*
 * call the /getAllCommunities API and create a datatable from the results
 */
function getAllCommunities() {
  $('#communitiesLoadingDiv').mask('Please Wait...<br/><img src="images/watson.gif">');
  $.get('api?action=getAllCommunities', 

     function (data, status, jq) {
      var communitiesTable = $('#communitiesTable').DataTable( {
        data: data.info,
        autoWidth: false,
        "columns": [
          { "data": "title" },
          { "data": "owner" },
          { "data": "created"},
          { "data": "updated"},
          { "data": "membercount"}            
        ],
        "columnDefs" : [
        	{ "className" : "communityName", "targets": 0},
        	{ "className" : "dt-body-right", "targets": 4},
          { "title": "Name", "targets": 0 },
          { "title": "Owner", "targets": 1 },
          { "title": "Created", "targets": 2, render: function(created, type) {
		      		// if type is display or filter then format the date
		      		if ( type === 'display' || type === 'filter') {
		      			return dateFormat(new Date(created), 'dd mmm yyyy h:MM:sstt');
		      		} else {
		      			// otherwise it must be for sorting so return the raw value
		      			return created;
		      		}    			
		      	} 
		      },
          { "title": "Last Updated", "targets": 3, render: function(updated, type) {
	        		// if type is display or filter then format the date
	        		if ( type === 'display' || type === 'filter') {
	        			return dateFormat(new Date(updated), 'dd mmm yyyy h:MM:sstt');
	        		} else {
	        			// otherwise it must be for sorting so return the raw value
	        			return updated;
	        		}    			
	        	} 
          },
          { "title": "Nbr of Members", "targets": 4 }
        ],
        "fnCreatedRow": function( nRow, aData, iDataIndex ) {
        	// create an attribute for the message ID so we can retrieve it later when we click on this message
        	nRow.getElementsByTagName('td')[0].setAttribute('community-id', aData.id); 
        }
      });
      
      communitiesTable.on('click', 'td', function(){
      	// highlight chosen message
    		$('.communityName').toggleClass('chosenCommunity',false); // un-highlight all communities
    		$(this).toggleClass('chosenCommunity'); // now highlight just this one

    		// cleanup
    		$('#tabs').remove();
    		$('#error').html('');
    		
        $('#detailsLoadingDiv').mask('<div style="text-align:center;" style="background-color:#fff;">Please Wait...<br/><img src="images/watson.gif"></div>',200);
        // we set the community-id attribute earlier so that it would be here now
    		$.get('api?action=getCommunityDetails', { id : this.getAttribute('community-id')}, processCommunityDetails, 'json')
    		.fail(function(err) {
    			console.log('an error occurred getting message details:', err);
    			$('#error').html(err.responseText);
    		})
    		.always(function() {
    			$('#detailsLoadingDiv').unmask();
    		});
    	});
    },
    'json')
    .fail(function(error) {
    	console.log('error getting all communities', error);
    	$('#error').html('error getting all communities:' + error);
    })
	  .always(function() {
	    $('#communitiesLoadingDiv').unmask();
	  });
}


function processCommunityDetails(json) {
	$('#communityDetailsWrapper').show();
	var tabsHeader = '';
	var tabsDetail = '';
	var tabsCounter = 0;
	var filesFound = false;
	var membersFound = false;
	var activitiesFound = false;
	
	// find members
	var members = json.find( function(item) {
		return item.type === 'members';
	});
	if (members.data.length > 0) {
		tabsHeader += '<li><a href="#tabs-' + tabsCounter + '">Members (' + members.data.length + ')</a></li>';
		tabsDetail += '<div id="tabs-' + tabsCounter + '">';
		tabsDetail += '<table id="membersTable"></table>';
		tabsDetail += '</div>';
		membersFound = true;
		tabsCounter++;
	}
	
	// find files
	var files = json.find( function(item) {
		return item.type === 'files';
	});
	if (files.data.length > 0) {
		tabsHeader += '<li><a href="#tabs-' + tabsCounter + '">Files (' + files.data.length + ')</a></li>';
		var fileSize = 0;
		$.each(files.data, function(index,file) {
			fileSize += file.size*1;
		});
		tabsDetail += '<div id="tabs-' + tabsCounter + '">';
		tabsDetail += '<div class="fileSizeDiv">Total files size:&nbsp;&nbsp;<span class="fileSize">' + fileSize.toLocaleString() + '</span></div>';
		tabsDetail += '<table id="filesTable"></table>';
		tabsDetail += '</div>';		
		filesFound = true;
		tabsCounter++;
	}
	
	// find activities
	var activities = json.find( function(item) {
		return item.type === 'activity';
	});
	if (activities.data.length > 0) {
		tabsHeader += '<li><a href="#tabs-' + tabsCounter + '">Recent Updates (' + activities.data.length + ')</a></li>';
		tabsDetail += '<div id="tabs-' + tabsCounter + '">';
		tabsDetail += '<table id="activitiesTable"></table>';
		tabsDetail += '</div>';
		activitiesFound = true;
		tabsCounter++;
	}
	
	// now build tabs markup
	var tabsText = '<div id="tabs" class="tabs">'
		+ '<ul>' + tabsHeader + '</ul>'
		+ tabsDetail
		+ '</div>';
	$('#communityDetails').html(tabsText);
	
	// now create datatables for the above markup
	if ( filesFound) {
		var filesTable = $('#filesTable').DataTable( {
		    data: files.data,
		    autoWidth: false,
		    searching: false,
		    paging: determinePaging(files.data),
		    "columns": [
		      { "data": "title" },
		      { "data": "size" }
		    ],
		    "columnDefs" : [
		    	{ "className": "dt-body-right", "targets": 1},
		      { "title": "Name", "targets": 0 },
		      { "title": "Size", "targets": 1, render: function(size, type) {
		    		// if type is display or filter then format the date
		    		if ( type === 'display' || type === 'filter') {
		    			return (size*1).toLocaleString();
		    		} else {
		    			// otherwise it must be for sorting so return the raw value
		    			return size;
		    		}    			
			    } 
		      }    
		    ]
		});
	}
	
	if ( membersFound ) {
		var membersTable = $('#membersTable').DataTable( {
	      data: members.data,
	      autoWidth: false,
	      searching: false,
	      paging: determinePaging(members.data),
	      "columns": [
	        { "data": "name" },
	        { "data": "email" }
	      ],
	      "columnDefs" : [
	        { "title": "Name", "targets": 0 },
	        { "title": "email", "targets": 1 },       
	      ]
	    });
	}
	
	if ( activitiesFound ) {
		console.log('activities data:', activities.data);
		var activitiesTable = $('#activitiesTable').DataTable( {
	      data: activities.data,
	      autoWidth: false,
	      searching: false,
	      paging: determinePaging(activities.data),
	      "columns": [
	        { "data": "author" },
	        { "data": "title" },
	        { "data": "publishedDate"}
	      ],
	      "columnDefs" : [
	        { "title": "Name", "targets": 0 },
	        { "title": "Title", "targets": 1 },
	        { "title": "Date", "targets": 2, render: function(publishedDate, type) {
	      		// if type is display or filter then format the date
		      		if ( type === 'display' || type === 'filter') {
		      			console.log('data is ', publishedDate);
		      			return dateFormat(new Date(publishedDate), 'dd mmm yyyy h:MM:sstt');
		      		} else {
		      			// otherwise it must be for sorting so return the raw value
		      			return publishedDate;
		      		}    			
		      	} 
		      },
	      ]
		});
	}	
	$('#tabs').tabs();

}

function determinePaging(someArray) {
	if (someArray.length > 10) {
		return true;
	} else {
		return false;
	}
}

$( document ).ready(function() {
	getAllCommunities();
});