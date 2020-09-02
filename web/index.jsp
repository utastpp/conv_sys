<%-- 
    Document   : index
    Created on : Sep 8, 2016, 11:26:41 AM
    Author     : David Herbert, david.herbert@utas.edu.au
--%>
<%@page import="cmcrdr.main.DialogMain"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<script type="text/javascript" src="./js/jquery-3.3.1.min.js"></script>
<script type="text/javascript" src="./js/jquery-ui.min.js"></script>
<script type="text/javascript" src="./js/utils.js"></script>
<link rel="stylesheet" href="./styles/jquery-ui.min.css" type="text/css" >
<link rel="stylesheet" href="./styles/styles.css" type="text/css" >
<link rel="stylesheet" href="./styles/jquery.modal.css" type="text/css" media="screen" />


<meta charset="utf-8">

<div id="mainContentDiv">
    <div id="heading">Voice Chat System - web browser interface</div>
    <div class="userInputDiv">
        <div class="column">
            <div class="leftColumn">
                <div class="columnHeading">Chat details:</div>
                    <div class="columnItem">
                        <div class="columnItem"><label>Knowledge Base:</label><textarea id="domaindescription" disabled="true" style="font-style:italic;color:red"></textarea></div>
                    </div>
                <div id="controllingIO">
                    <div class="boxed">
                        <div class="columnHeading">Chat settings:<button id="chatSettingsButton" class='hbutton'>show</button></div>
                        <div id="chatSettings">
                            <div class="columnItem"><div class='admin'><label>Input:</label><input id="input_mode_button" type="button" value="speech" class='hbutton'>
                                    <label>Output:</label><input id="speaking_button" type="button" value="text & speech"></div></div>
                            <div class="columnItem">
                                <div class='admin'><label>Lang:</label><select id="selectLanguage" onchange="updateCountry()"></select>
                                </div></div>
                            <div class="columnItem">
                                <div class='admin'><label>Dialect:</label><select id="selectDialect"></select></div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="userTextDiv">
                    <div class="columnItem">
                      
                            <div class="userTextInputDiv">                                
                                <input id="userTextInput" type="text" placeholder="Enter text...">
                            </div>
                     
                    </div>

                    <div class="columnItem">                    
                        <input id="userInputButton" type="button" value="Send Chat" class='hbutton'>  
                    </div>
                </div>
              
                
                <div class="columnItem">
                    <div id="speechDiv">
                        <div id="results">
                            <span id="final_span" class="final"></span>
                            <span id="interim_span" class="interim"></span>
                            <p></p>
                        </div>
                        <p>Click the microphone icon below to speak (Chrome only)</p>
                        <button  id="startButton" onclick="startSpeechRecognition()" class='hbutton'>
                        <img id="startImg" src="mic.gif" alt="Start"></button>
                    </div>
                    
                    <div id="info">
                      <p id="info_speak_now">Speak now.</p>
                      <p id="info_no_speech">No speech was detected. You may need to adjust your microphone settings.</p>
                      <p id="info_no_microphone" style="display:none">
                        No microphone was found. Ensure that a microphone is installed and that microphone settings are configured correctly.</p>
                      <p id="info_allow">Click the "Allow" button above to enable your microphone.</p>
                      <p id="info_denied">Permission to use microphone was denied.</p>
                      <p id="info_blocked">Permission to use microphone is blocked. To change,
                        go to chrome://settings/content/microphone?search=microphone</p>
                      <p id="info_upgrade">Web Speech API is not supported by this browser.
                         Upgrade to <a href="//www.google.com/chrome">Chrome</a>
                         version 25 or later.</p>
                    </div>
                </div>
                
                
              
            </div>
            
            
            
            <div class="rightColumn">
                <div class="columnHeading">Chat text:</div>
                <div id="dialogMainDiv"></div> 
            </div> 
        </div>       
    </div>
</div>

<!-- placeholder for all confirmation dialogs !-->
<div id="confirmDialog"></div>

<script type="text/javascript" src="./js/speech.js"></script>
    


<script>
    
    
    
    $(document).ready(function() { 
        $("#mainContentDiv").hide();
        standardDialog("confirmDialog",
                    "Voice chat system",
                    "Welcome to the conversation system. It will take one moment to initialse the system.  Do you want to continue? ('No' will close this window..)",
                    initialise,
                    closeCallback
                ); 
    });
    
    function initialise() {
        $("#mainContentDiv").show();
        $('#chatSettings').hide();
        $('#userTextDiv').hide();
        getDomainDescription();
        getInitialGreeting();
    }
    
    function closeCallback() {    
        self.close();
    }
    
    $("#inferenceViewer").click(function() {
        var win = window.open("inferenceViewer.jsp",'_blank');
        if (win) {
            win.focus();
        }
        else {
            alert("Please allow popups for this website.");
        }
    });
    
    $("#ruleViewer").click(function() {
        var win = window.open("ruleViewer.jsp",'_blank');
        if (win) {
            win.focus();
        }
        else {
            alert("Please allow popups for this website.");
        }
    });
    
    
    // When #typingInput_button has pressed
    $('#userInputButton').click(function() {   
        processUserInput();
            
    });
    
    $('#chatSettingsButton').click(function() {
        if ($('#chatSettings').is(':visible')) {
            $('#chatSettings').hide(); 
            $('#chatSettingsButton').html('show');
        }
        else {
            $('#chatSettings').show(); 
            $('#chatSettingsButton').html('hide');
        }        
    });
        
    // When enter key has pressed on #typingInput 
    $('#userTextInput').keypress(function(e) {
        if(e.which === 13) {
            if ($("#userTextInput").val() === "")
                standardOK("confirmDialog","ERROR","You cannot submit an empty chat value..")();  
            else
                processUserInput();
        }
    });
    
    
    
    function escapeRegExp(str) {
        return str.replace(/([.*+?^=!:\$\{\}()|\[\]\/\\])/g, "\\$1");
    }
    
    function replaceAll(str, find, replace) {
        return str.replace(new RegExp(escapeRegExp(find), 'g'), replace);
    }
    
    function getDomainDescription() {
        $.ajax({
             url : 'GetUserServlet',
             data : {
                 mode : 'getDomainDescription'
             },
             success : function(responseText) {
                var desc = responseText;
                $('#domaindescription').val(desc);
             }
         }); 
    }
    
    function getInitialGreeting() {
        var reply = "";
        var NO_SPEAK = "#NOSPEAK";
        var END_NO_SPEAK = "#ENDNOSPEAK";
        
        $.ajax({
            //async: false,
            url : 'GetUserServlet',
            data : {
            mode : 'greeting'
            },
            success : function(responseText) {
                reply = responseText;
                $('#dialogMainDiv').append("<div class='system'>VoiceChat</div><div class='bubble blue'><pre>"+ replaceAll(replaceAll(responseText,NO_SPEAK,""),END_NO_SPEAK,"") +"</pre></div>");

                var tts_mode =  $('#speaking_button').val();
                
                if (tts_mode==='text & speech') {
                    var nospeakStartPosition = reply.indexOf(NO_SPEAK);
                    var nospeakEndPosition = reply.indexOf(END_NO_SPEAK);
                    var betterDiction;
                    if (nospeakStartPosition !== -1) {
                        postProcessDialog(reply.substring(0,nospeakStartPosition) + reply.substring(nospeakEndPosition + END_NO_SPEAK.length));
                        //betterDiction = postProcessDialog(reply.substring(0,nospeakStartPosition) + reply.substring(nospeakEndPosition + END_NO_SPEAK.length),getInitialGreetingCallback);
                    }
                    else {
                        postProcessDialog(reply,getInitialGreetingCallback);
                        //betterDiction = postProcessDialog(reply);
                    }            

                    
                }
            }
        });
    }
    
    function getInitialGreetingCallback(reply) {
        tts(reply);
    }
    
    // Processing event with typed input
    function processUserInput(){
        var stringInput = $('#userTextInput').val();
        preProcessDialog(stringInput,processUserInputCallback);
        
    }
    
    function processUserInputCallback(reply) {
        processDialog(reply);
        $('#userTextInput').val("");
    }
    


    function preProcessDialog(stringInput,callback){
        var reply = "";
        $.ajax({
             //async: false,
             url : 'GetUserServlet',
             data : {
                 mode : 'preprocess',
                 stringInput : stringInput
             },
             success : function(responseText) {
               reply = responseText;
               callback(reply);
             }
         });
         //return reply;
    }
    
    function postProcessDialog(stringInput,callback){
        var reply = "";
        $.ajax({
             //async: false,
             url : 'GetUserServlet',
             data : {
                 mode : 'postprocess',
                 stringInput : stringInput
             },
             success : function(responseText) {
               reply = responseText;
               callback(reply);
             }
         });
         //return reply;
    }
    
    function processDialog(stringInput){
        $.ajax({
             url : 'GetUserServlet',
             data : {
                 mode : 'dialog',
                 stringInput : stringInput
             },
             success : function(responseText) {
                var NO_SPEAK = "#NOSPEAK";
                var END_NO_SPEAK = "#ENDNOSPEAK";
                var reply = responseText;
                //alert("Response:" + reply);

                $('#dialogMainDiv').append("<div class='user'>User</div><div class='bubble bubble-alt green'><pre>"+stringInput+"</pre></div>");	
                //$('div.dialogContainerDiv').append("<div class='system'>ICS</div><div class='bubble'><p>"+array[0]+"</p></div>");
                $('#dialogMainDiv').append("<div class='system'>VoiceChat</div><div class='bubble blue'><pre>"+ replaceAll(replaceAll(responseText,NO_SPEAK,""),END_NO_SPEAK,"") + "</pre></div>");
                
                var tts_mode =  $('#speaking_button').val();
                if (tts_mode==='text & speech') {
                    
                    var nospeakStartPosition = reply.indexOf(NO_SPEAK);
                    var nospeakEndPosition = reply.indexOf(END_NO_SPEAK);
                    var betterDiction;
                    
                    if (nospeakStartPosition !== -1) {                       
                        betterDiction = reply.substring(0,nospeakStartPosition) + reply.substring(nospeakEndPosition + END_NO_SPEAK.length);
                        nospeakStartPosition = betterDiction.indexOf(NO_SPEAK);
                        while (nospeakStartPosition !== -1) {
                            nospeakEndPosition = betterDiction.indexOf(END_NO_SPEAK);
                            betterDiction = betterDiction.substring(0,nospeakStartPosition) + betterDiction.substring(nospeakEndPosition + END_NO_SPEAK.length);
                            betterDiction = betterDiction.replace(NO_SPEAK,"");
                            betterDiction = betterDiction.replace(END_NO_SPEAK,"");
                            nospeakStartPosition = betterDiction.indexOf(NO_SPEAK);
                        }
                        postProcessDialog(betterDiction,processDialogCallback);
                        //betterDiction = postProcessDialog(betterDiction);
                    }
                    else {
                        postProcessDialog(reply,processDialogCallback);
                        //betterDiction = postProcessDialog(reply);
                    } 
                    //alert("betterDiction before:" + betterDiction);
                    
                    /* moved to callback...
                    betterDiction = "<p>" + betterDiction + "</p>";
                    betterDiction = $(betterDiction).text();
                    //alert("betterDiction after" + betterDiction);

                    tts(betterDiction);
                    */
                }
                
                var d = $('#dialogMainDiv');
                d.scrollTop(d.prop("scrollHeight"));

             }
         });
    }
    
    function processDialogCallback(betterDiction) {
        betterDiction = "<p>" + betterDiction + "</p>";
        betterDiction = $(betterDiction).text();
        
        tts(betterDiction);
    }
    
    $('#speaking_button').click(function() {
        var tts_mode =  $('#speaking_button').val();
        if (tts_mode==='text & speech') {
            stopTts(); 
            $('#speaking_button').val("text");
            $.ajax({
             //async: false,
             url : 'GetUserServlet',
             data : {
                 mode : 'UIMethodChange',
                 statusString : 'OUTPUT_CHANGED_TO_TEXT_ONLY'
             },
             success : function(responseText) {
               reply = responseText;
             }
            });
        }
        else {
            $('#speaking_button').val("text & speech");
            $.ajax({
             //async: false,
             url : 'GetUserServlet',
             data : {
                 mode : 'UIMethodChange',
                 statusString : 'OUTPUT_CHANGED_TO_TEXT_AND_SPEECH'
             },
             success : function(responseText) {
               reply = responseText;
             }
         });
        }
    });
    
    $('#input_mode_button').click(function() {
        var typing_mode =  $('#input_mode_button').val();
        if(typing_mode ==='speech'){
            $('#input_mode_button').val('text'); 
            $('#speechDiv').hide();
            $('#userTextDiv').show();
            $('#userInput').focus();
            $.ajax({
             //async: false,
             url : 'GetUserServlet',
             data : {
                 mode : 'UIMethodChange',
                 statusString : 'INPUT_CHANGED_TO_TEXT'
             },
             success : function(responseText) {
               //reply = responseText;
             }
         });
            
        } else {
            $('#input_mode_button').val('speech');
            $('#speechDiv').show();
            $('#userTextDiv').hide();
            $.ajax({
             //async: false,
             url : 'GetUserServlet',
             data : {
                 mode : 'UIMethodChange',
                 statusString : 'INPUT_CHANGED_TO_SPEECH'
             },
             success : function(responseText) {
               //reply = responseText;
             }
         });
        }
    });
    
    


</script>
