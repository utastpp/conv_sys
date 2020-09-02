<%-- 
    Document   : index
    Created on : Sep 8, 2016, 11:26:41 AM
    Author     : David Herbert, david.herbert@utas.edu.au
--%>
<%@page import="cmcrdr.main.DialogMain"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<script type="text/javascript" src="./js/jquery-3.3.1.min.js"></script>
<link rel="stylesheet" href="./styles/styles.css" type="text/css" >
<link rel="stylesheet" href="./js/jbox/Source/jBox.css">
<link rel="stylesheet" href="./js/jbox/Source/plugins/Notice/jBox.Notice.css">
<link rel="stylesheet" href="./js/jbox/Source/plugins/Confirm/jBox.Confirm.css">
<link rel="stylesheet" href="./js/jbox/Source/plugins/Image/jBox.Image.css">
<link rel="stylesheet" href="./js/jbox/Source/themes/NoticeFancy.css">

<script type="text/javascript" src="./js/jquery-3.1.0.min.js"></script>
<script type="text/javascript" src="./js/jbox/Source/jBox.js"></script>
<script type="text/javascript" src="./js/jbox/Source/plugins/Notice/jBox.Notice.js"></script>
<script type="text/javascript" src="./js/jbox/Source/plugins/Confirm/jBox.Confirm.js"></script>
<script type="text/javascript" src="./js/jbox/Source/plugins/Image/jBox.Image.js"></script>

<meta charset="utf-8">

<div id="mainContentDivKA">
    <div id="heading">Inference Viewer</div>
   <!-- <div id="userInputDiv"> !-->
        <div class="column">
            <div id="leftColumnKA">
                <div class="columnHeading">Dialog History</div>
                    <div class="columnItem">                      
                        <div id="dialogHistory"></div>
                        <!--<input id="getDialogHistory" type="button" value="Get History"> !-->
                    </div>
            </div>
            <div id="rightColumnKA">
                <div class="columnHeading">Knowledge Base</div>
                <div class="columnItem"> 
                    <div id="rulebase">                                
                       
                    </div>
                    
                </div>
                    <!--<input id="getRulebase" type="button" value="Get Rulebase">!-->
            </div>
        </div>
</div>


<script>

 
    $(document).ready(function() {
        startKnowledgeAcquisition();
        processGetRulebase(); 
    });
    
   
    $('#getDialogHistory').click(function() {      
        processGetDialogHistory();
    });
    
    $('#getRulebase').click(function() {      
        processGetRulebase();
    });
    
    $('#dialogHistory').on("click",function(e) {
        if (e.target.id !== "dialogHistory") {        
            console.log("div click: " + e.target.id);
            let dialogID = e.target.id.substr(6);
            if ($(e.target).hasClass("blue") || $(e.target).hasClass("systemPre")) {
                // we only want system reply dialogs to be selected, not user dialogs
                console.log("System dialog selected!");           
                updateRuleSelected(dialogID);
            }
        }
    });
    
    function updateRuleSelected(id) {
        //alert("Rule selected based on dialog " + id);
        
        $.ajax({
           type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            async: false,
            data : {
                mode : 'getFiredRuleList',
                dialogID : id
            },
            success : function(response) {
                unhighlightRules();
                let ruleList = response.split(',');
                $.each(ruleList, function(index, ruleNumber) {
                    console.log("Looking at rule:" + ruleNumber);              
                    highlightRule(ruleNumber);
                });
                
            }
        });
        
    }
    
    function unhighlightRules() {
        $(".ruleHighlight").each(function(index,aRule) {
            $(this).removeClass('ruleHighlight');
        });
    }
    
    function highlightRule(ruleNumber) {
        $("#rule" + ruleNumber).addClass('ruleHighlight');
    }
    
    function processGetDialogHistory() {
        dconsole("I was called...");
       $.ajax({
           type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            async: false,
            dataType:'json',
            data : {
                mode : 'getDialogHistory'
            },
            success : function(jsonObj) {
                let dialog = jsonObj['dialogRepository'];
                let systemReply = false;
                let userBubbleClass = 'bubble green';
                let systemBubbleClass = 'bubble blue';
                let systemPreClass = "systemPre";
                let userPreClass = "userPre";
                let dialogClass = "";
                let preClass = "";
                $.each(dialog,function(index, dialogData) {
                    let utterance = dialogData['dialogStr'];
                    console.log(dialogData);
                    if (!systemReply) {
                        dialogClass = userBubbleClass;
                        preClass = userPreClass;
                    }
                    else {   
                        dialogClass = systemBubbleClass;
                        preClass = systemPreClass;

                    }
                    
                    $('#dialogHistory').append("<div class='" + dialogClass + "' id='dialog" + dialogData['dialogId'] + "'><pre class='" + preClass + "' id='diapre" + dialogData['dialogId'] + "'>" + utterance+"</pre></div>");
                    
                    systemReply = !systemReply;
                });
            }
         });  
    }
    
    function processGetRulebase() {
       $.ajax({
           type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            async: false,
            data : {
                mode : 'getRulebase'
            },
            success : function(response) {
                $('#rulebase').append(response);
                   
            }
         });  
    }

    function startKnowledgeAcquisition() {
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            async: false,
            data : {
                mode : 'startKnowledgeAcquisition'
            },
            success : function(responseText) {
                if (responseText !== "false") {                
                    if (responseText !== "noKA") {
                        var myModal = new jBox('Confirm', {
                            confirmButton: 'Yes',
                            cancelButton: 'No',
                            content: responseText,
                            confirm: confirmedKnowledgeAcquisition,
                            cancel: cancelFunction,
                            closeOnConfirm: true
                        });
                        myModal.open();                               
                    }
                    else 
                        alert("Please provide some dialog before trying to create new knowledge...");              
                }
            }
         });             
    }
    
    function confirmFunction() {
       // alert("confirmed!");
    }
    
    function cancelFunction() {
        //alert("cancelled!");
    }
    
    function confirmedKnowledgeAcquisition() {
        
        processGetDialogHistory();
        
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            async: false,
            data : {
                mode : 'confirmedKnowledgeAcquisition'
            },
            success : function(responseText) {
                if (responseText !== "false") {
                    var myModal = new jBox('Confirm', {
                            confirmButton: 'Yes',
                            cancelButton: 'No',
                            content: responseText,
                            confirm: continueModeKnowledgeAcquisition,
                            cancel: getModeKnowledgeAcquisition,
                            closeOnConfirm: true
                        });
                        
                        myModal.open();      
                    
                }
            }
        });    
    }
    
    function continueModeKnowledgeAcquisition() {
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            async: false,
            data : {
                mode : 'continueModeKnowledgeAcquisition'
            },
            success : function(responseText) {
                if (responseText !== "false") {
                    //alert("continue" + responseText);                    
                }
            }
        });    
    }
    
    function getModeKnowledgeAcquisition() {
        //alert("newModeKnowledgeAcquisition");
        var myModal = new jBox('Confirm', {
                            confirmButton: 'Yes',
                            cancelButton: 'No',
                            content: "Is the recent dialog a totally new context? (if yes, rule is added to the root)",
                            confirm: newModeKnowledgeAcquisition,
                            cancel: arbitraryModeKnowledgeAcquisition,
                            closeOnConfirm: true
                        });
                        
                        myModal.open(); 
    }
    
         
    
    
    function newModeKnowledgeAcquisition() {
    
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            async: false,
            data : {
                mode : 'newModeKnowledgeAcquisition'
            },
            success : function(responseText) {
                if (responseText !== "false") {
                    alert("new" + responseText);                    
                }
            }
        });    
    }    

    function arbitraryModeKnowledgeAcquisition() {
    
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            async: false,
            data : {
                mode : 'arbitraryModeKnowledgeAcquisition'
            },
            success : function(responseText) {
                if (responseText !== "false") {
                    alert("arbitrary" + responseText);                    
                }
            }
        });    
    } 
    
    
    
</script>
