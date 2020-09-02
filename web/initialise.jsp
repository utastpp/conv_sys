<%-- 
    Document   : index
    Created on : Sep 8, 2016, 11:26:41 AM
    Author     : David Herbert, david.herbert@utas.edu.au
--%>
<%@page import="cmcrdr.main.DialogMain"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<link rel="stylesheet" href="./styles/styles.css" type="text/css" >
<script type="text/javascript" src="./js/jquery-3.1.0.min.js"></script>
<meta charset="utf-8">


<div id="mainContentDiv">
<div id="left_heading">Intelligent Conversation System</div>   
<div id="userInputDiv">
    <input id="headless" type="text" value="headless">
    <input id="userInput" type="text" value="ics-unit-outline">
    <input id="initialiseButton" type="button" value="Initialise System">                
</div>
<div id="userInputDiv">
    <input id="showButton" type="button" value="Show Admin interface">                
</div>

<div class="dialogContainerDiv">
</div>
</div>

<script>

    // When #typingInput_button has pressed
    $('#initialiseButton').click(function() {
        
        processUserInput();
            
    });
    
    $('#showButton').click(function() {   
        showAdminInterface();
            
    });
    
    // Processing event with typed input
    function processUserInput(){
        var stringInput = $('#userInput').val();
        var headless = $('#headless').val();
        processInitialise(stringInput,headless);
        //$('#userInput').val("");
    }
    
    function processInitialise(stringInput,headless){
        $.ajax({
             url : 'GetUserServlet',
             data : {
                 mode : 'initialise',
                 domain : stringInput,
                 headless: headless
             },
             success : function(responseText) {
                //var array = responseText.split(":");
                var response = responseText;
                
                $('div.dialogContainerDiv').append("<div class='system'>" + response + "</div>");	
     
                var d = $('div.dialogContainerDiv');
                d.scrollTop(d.prop("scrollHeight"));

             }
         });
        
    }
    
    function showAdminInterface(){
        $.ajax({
             async: false,
             url : 'GetUserServlet',
             data : {
                 mode : 'show'
             },
             success : function(responseText) {
                //var array = responseText.split(":");
                var response = responseText;
                
                $('div.dialogContainerDiv').append("<div class='system'>" + response + "</div>");	
     
                var d = $('div.dialogContainerDiv');
                d.scrollTop(d.prop("scrollHeight"));

             }
         });
        
    }

</script>
