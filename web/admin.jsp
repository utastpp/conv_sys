<%-- 
    Document   : index
    Created on : Sep 8, 2016, 11:26:41 AM
    Author     : David Herbert, david.herbert@utas.edu.au
--%>
<%@page import="cmcrdr.main.DialogMain"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<link rel="stylesheet" href="./styles/styles.css" type="text/css" >
<script type="text/javascript" src="./js/jquery-3.3.1.min.js"></script>
<script type="text/javascript" src="./js/jquery-ui.min.js"></script>
<script type="text/javascript" src="./js/utils.js"></script>
<link rel="stylesheet" href="./styles/styles.css" type="text/css" >
<link rel="stylesheet" href="./styles/jquery-ui.min.css" type="text/css" >
<meta charset="utf-8">


<div id="mainContentDiv">
    <div id="heading">Chat System</div>   
  
    <div class="userInputDiv">
        <div class="column">
            <div class="leftColumn">
                <div class="columnHeading">Current Status:</div>
                    <div class="columnItem">
                        <div class="columnItem"><div class="admin"><label>Loaded domain:</label><input id="curdomain" value="" disabled="true" style="font-style:italic;color:red"/></div></div>
                        <div class="columnItem"><div class="admin"><label>Description:</label><textarea id="curdescription"  disabled="true" style="font-style:italic;color:red"></textarea></div></div>
                    </div>
            </div> 
        </div>
        <div class="column">
        <div class="leftColumn">
          <div class="columnHeading">Open existing knowledge base:</div> 
          <div class="columnItem"><div class="admin"><label>Name:</label><select id="domainList"></select></div></div>
            <div class="columnItem"><div class="admin"><label>Description:</label><textarea id="domaindescription" disabled="true" style="font-style:italic;color:red"></textarea></div></div>
          <div class="boxed">
            <div class="columnHeading2"><b>Modify External Reference Database (optional)</b></div>
            <div class="columnItem"><div class="admin"><label>Host:</label><input id="modhost" value="" /></div></div>
            <div class="columnItem"><div class="admin"><label>Database:</label><input id="moddatabase" value=""  /></div></div>
            <div class="columnItem"><div class="admin"><label>Username:</label><input id="modusername" value="" /></div></div>
            <div class="columnItem"><div class="admin"><label>Password:</label><input type="password" id="modpassword" value="" /></div></div>
          </div>
          <button id="loadButton" class='majorbutton hbutton'>Load...</button>
        </div>
      <div class="rightColumn">
        <div class="columnHeading">Create new knowledge base:</div>
        <div class="columnItem"><div class="admin"><label>Name:</label><input id="domain" placeholder="Enter a domain name..." /></div></div>
          <div class="columnItem"><div class="admin"><label>Description:</label><textarea id="newdescription" placeholder="Enter a description..."></textarea></div></div>
        <div class="boxed">
          <div class="columnHeading2"><b>Reference Database </b></div>
          <div class="columnItem"><div class="admin"><label>Host:</label><input id="refhost" value="" /></div></div>
          <div class="columnItem"><div class="admin"><label>Database:</label><input id="refdatabase" value=""  /></div></div>
          <div class="columnItem"><div class="admin"><label>Username:</label><input id="refusername" value="" /></div></div>
          <div class="columnItem"><div class="admin"><label>Password:</label><input type="password" id="refpassword" value="" /></div></div>
        </div>
          <div class="boxed">
          <div class="columnHeading2"><b>Reference Database Fields (auto populate)</b></div>
          <div class="columnItem"><div class="admin"><button id="getFieldsButton" class='hbutton'>Get Fields...</button></div>
              <div id="fieldItems"></div>             
          </div>
          </div>
        <button id="createButton" class='majorbutton hbutton'>Create KB...</button>
      </div>
    </div>
        
    <div class="column">
        <div class="leftColumn">
            <div class="columnHeading">Interface Options:</div>
            <div class="columnItem"><div class="admin"><button id="showAdminGUI" class='hbutton'>Show Admin Java GUI...</button></div></div>
        </div>
    </div>   
  </div>
</div>
<div id="confirmDialog"></div>

<script>
    
    function noopCallback() {      
    }
    
    $(document).ready(function() {
        clearItems();
        getLoadedDomain(readyCallback);
//        getActiveDomainDescription();
//        getDomainList();
//        getNonActiveDomainDescription();
    });
    
    function readyCallback() {
        getActiveDomainDescription(readyCallback2);
    }
    
    function readyCallback2() {
        getDomainList(readyCallback3);
    }
    
    function readyCallback3() {
        getNonActiveDomainDescription(noopCallback);
    }
    
    $('#domainList').on('change', function() {
        getNonActiveDomainDescription(noopCallback);
    })

    // When #typingInput_button has pressed
    $('#loadButton').click(function() {      
        processLoadButton(loadButtonCallback);
//        getLoadedDomain();
//        getActiveDomainDescription();
    });
    
    function loadButtonCallback() {
        getLoadedDomain(loadButtonCallback2);
    }
    
    function loadButtonCallback2() {
       getActiveDomainDescription(noopCallback);
    }
    
    $('#createButton').click(function() {   
        processCreateButton(createButtonCallback);
        // refresh the list of KBs as we've probably just created one..
//        getDomainList();
//        getLoadedDomain();
//        getActiveDomainDescription();
    });
    
    function createButtonCallback() {
        getDomainList(createButtonCallback2);
    }
    
    function createButtonCallback2() {
        getLoadedDomain(createButtonCallback3)
    }
    
    function createButtonCallback3() {
        getActiveDomainDescription();
    }
    
    
    $('#showAdminGUI').click(function() {   
        processShowAdminGUIButton();
    });
    
    $('#getFieldsButton').click(function() { 
        processGetFieldsButton();
    });
    
    function processGetFieldsButton() {
        
        var theHost = $('#refhost').val();
        var theDatabase = $('#refdatabase').val();
        var theUsername = $('#refusername').val();
        var thePassword = $('#refpassword').val();
        
        if (theHost.trim() && theDatabase.trim() && theUsername.trim() && thePassword.trim()) {       
            $.ajax({
                 url : 'AdminServlet',
                 data : {
                     mode : 'getReferenceDatabaseFields',
                     host: theHost,
                     database: theDatabase,
                     username: theUsername,
                     password: thePassword
                 },
                 success : function(responseText) {
                    if (responseText === "no connection") {
                        
                        standardOK("confirmDialog","Error","Database connection failed..")();
                           
                    } 
                    else {                  
                        var names = responseText.split(';');
                        $('#fieldItems').empty();

                        $.each(names,function(i,aName) {
                            //alert("Looking at:" + aFile);
                            if (aName.trim()) {
                                $('#fieldItems').append('<div id="columnItem"><label>' + aName + ':</label><input type="checkbox" name="databaseReferenceFields" value="' + aName +'" checked></div>');
                            }
                        }); 
                    }

                 }
             });
        }
        else {
            standardOK("confirmDialog","Error","You must set the database details first...")(); 
        }
    }
    
    
    function clearItems() {
        $('#domainList').empty();
        $('#fieldItems div').remove();
        $('#curdomain').val("");
        $('#curdescription').val("");
        $('#domaindescription').val("");
        $('#modhost').val("");
        $('#moddatabase').val("");
        $('#modusername').val("");
        $('#modpassword').val("");
    }
    
    function getCheckedFieldNameValues() {
        var theValues = [];
        $.each($("input[name='databaseReferenceFields']:checked"),function() {
            theValues.push($(this).val());
        });
        return theValues;
    }
    
    function getReferenceDatabaseName() {
        $.ajax({
            url : 'AdminServlet',
            //async: false,
            data : {
                mode : 'getReferenceDatabaseName',
            },
            success : function(responseText) {
                var name = responseText;
                $('#moddatabase').val(name);
                $('#modhost').val("");
                $('#modusername').val("");
                $('#modpassword').val("");
            }
        });
    }
   
    
    function getDomainList(){

        $.ajax({
             url : 'AdminServlet',
             data : {
                 mode : 'getDomainList'
             },
             success : function(responseText) {
                var files = responseText.split(';');
                //var myModal = new jBox('Modal', {
                //    content: response
                //});
                //myModal.open();
                
                $('#domainList').empty();
                
                $.each(files,function(i,aFile) {
                    //alert("Looking at:" + aFile);
                    if (aFile.trim()) {
                        $('#domainList').append($('<option>', {
                            value: aFile,
                            text: aFile
                        }));
                    }
                });                
             }
         });   
    }
    
    function getLoadedDomain(callback){

        $.ajax({
             url : 'AdminServlet',
             data : {
                 mode : 'getLoadedDomain'
             },
             success : function(responseText) {
                $('#curdomain').val();

                if (responseText.trim()) {
                    $('#curdomain').val(responseText);
                }
                callback();
             }
        });
    }
    
    function getActiveDomainDescription(callback){

        $.ajax({
             url : 'AdminServlet',
             data : {
                 mode : 'getActiveDomainDescription'
             },
             success : function(responseText) {
                $('#curdescription').val();

                if (responseText.trim()) {
                    $('#curdescription').val(responseText);
                } 
                callback();
             }
        });
    }
    
    function getNonActiveDomainDescription(callback){
        var theDomain = $('#domainList').val();
        
        $.ajax({
             url : 'AdminServlet',
             data : {
                 mode : 'getNonActiveDomainDescription',
                 domain: theDomain
             },
             success : function(responseText) {
                $('#domaindescription').val();

                if (responseText.trim()) {
                    $('#domaindescription').val(responseText);
                }
                callback();
             }
        });
    }
    
    function processUserInput(){
        var theDomain = $('#domainList').val();
        processInitialise(theDomain,noopCallback);
        //$('#userInput').val("");
    }
    

    function processCreateButton(callback){
        var fieldNamesToUse = getCheckedFieldNameValues();
        var theDomain = $('#domain').val();
        var theDescription = $('#newdescription').val();
        var theHost = $('#refhost').val();
        var theDatabase = $('#refdatabase').val();
        var theUsername = $('#refusername').val();
        var thePassword = $('#refpassword').val();
        if (fieldNamesToUse.length === 0) {
            standardOK("confirmDialog","Error","You must specify fields to use from the database (select 'Get Source Fields...' button")();          
        } 
        else {
            if (theDomain.trim() && theDescription.trim() && theHost.trim() && theDatabase.trim() && theUsername.trim() && thePassword.trim()) {
                $('#domain').val("");
                $('#newdescription').val("");
                $('#refhost').val("");
                $('#refdatabase').val("");
                $('#refusername').val("");
                $('#refpassword').val("");
                $('#fieldItems div').remove();
               processCreate(theDomain,theDescription,theHost,theDatabase,theUsername,thePassword,fieldNamesToUse,callback);
                
            }
            else {
                standardOK("confirmDialog","Error","Some requested items are missing..")();
            }
        }
    }
    

    function processLoadButton(){
        var theDomain = $('#domainList').val();
        var theHost = $('#modhost').val();
        var theDatabase = $('#moddatabase').val();
        var theUsername = $('#modusername').val();
        var thePassword = $('#modpassword').val();
        
        processInitialise(theDomain,theHost,theDatabase,theUsername,thePassword,processLoadButtonCallback);
        //getReferenceDatabaseName();             
    }
    
    function processLoadButtonCallback() {
        getReferenceDatabaseName(noopCallback); 
    }
    
    function processInitialise(theDomain,theHost,theDatabase,theUsername,thePassword,callback) {    
         $.ajax({
             url : 'AdminServlet',
             //async: false,
             data : {
                 mode : 'initialise',
                 domain : theDomain,
                 host: theHost,
                 database: theDatabase,
                 username: theUsername,
                 password: thePassword
             },
             success : function(responseText) {
                var response = responseText;
                standardOK("confirmDialog","Status",response)();
                callback();
             }
         });
    }
    
    function processCreate(theDomain,theDescription,theHost,theDatabase,theUsername,thePassword, theFieldNames,callback){
        $.ajax({
             url : 'AdminServlet',
             //async: false,
             data : {
                 mode : 'create',
                 domain : theDomain,
                 description: theDescription,
                 host: theHost,
                 database: theDatabase,
                 username: theUsername,
                 password: thePassword,
                 fieldNames: theFieldNames.join(",")
             },
             success : function(responseText) {
                var response = responseText;
                standardOK("confirmDialog","Status",response)();
                callback();

             }
         });
        
    }
    
    function processShowAdminGUIButton() {
        $.ajax({
             url : 'AdminServlet',
             data : {
                 mode : 'showAdminGUI'
             }           
         });    
    }
    
//    function standardOK(title,body) {
//        return function() {
//            $("#confirmDialog").html("<p>" + body +"</p>");
//            $("#confirmDialog").dialog({
//                dialogClass: "no-close",
//                resizable: false,
//                draggable: false,
//                modal: true,
//                title: title,
//                height: 150,
//                width: 300,
//                buttons: {
//                    "OK": function () {
//                        $(this).dialog('close');
//                    }
//                }
//            });    
//        };
//    }
    
</script>
