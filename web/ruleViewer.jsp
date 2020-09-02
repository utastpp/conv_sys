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
    <div id="heading">Rule Viewer</div>
   <!-- <div id="userInputDiv"> !-->
        <div class="column">
            
            <div id="leftColumnKB">
                <div class="columnHeading">Knowledge Base Rules</div>
                <div class="columnItem"> 
                    <div id="rulebaseViewer">                                
                       
                    </div>
                    
                </div>
                <input id="getRulebase" type="button" value="Refresh" class='majorbutton hbutton'>
            </div>
            <div id='rightColumnKB'>
            <div class="columnHeading">Rule details</div>   
            <div class="columnItem"><label>Rule ID</label><div id="ruleID" class="ruleDetailsDiv"><span></span></div></div>
            <div class="columnItem"><label>Condition</label><div id="ruleCondition" class="ruleDetailsDiv"><span></span></div></div>
            <div class="columnItem"><label>Cornerstone Case ID</label><div id="ruleCornerstoneCaseID" class="ruleDetailsDiv"><span></span></div></div>
            <div class="columnItem"><label>Conclusion</label><div id="ruleConclusion" class="ruleDetailsDiv"><span></span></div></div>
            <div class="columnItem"><label>Conclusion category</label><div id="ruleConclusionCategory" class="ruleDetailsDiv"><span></span></div></div>
            <div class="columnItem"><label>Conclusion action</label><div id="ruleConclusionAction" class="ruleDetailsDiv"><span></span></div></div>
            <div class="columnItem"><label>Stopped</label><div id="ruleStopped" class="ruleDetailsDiv"><span></span></div></div>
            <div class="columnItem"><label>Do Not Stack</label><div id="ruleDoNotStack" class="ruleDetailsDiv"><span></span></div></div>

                
        </div>       
   <!-- </div>!-->
</div>


<script>

 
    $(document).ready(function() {
        processGetRulebase();       
    });
    
   
    
    $('#getRulebase').click(function() {      
        processGetRulebase();
    });
    
   /* $('.rule').click(function(e) {
        alert("The clicked div was: " + e.target.id);
    });*/
    
    $('#rulebaseViewer').on("click",function(e) {
        if (e.target.id !== "rulebaseViewer") {        
            //console.log("div click: " + e.target.id);
            let id = e.target.id.substr(4);
            updateRuleDetails(id)
        }
    });
    
    function unhighlightRules(highlightClass) {
        $('.'+highlightClass).each(function(index,aRule) {
            $(this).removeClass(highlightClass);
        });
    }
    
    function highlightRule(ruleNumber,ruleHighlightClass) {
        $("#rule" + ruleNumber).addClass(ruleHighlightClass);
    }
    
    function updateRuleDetails(id) {
       //alert("The id is " + id);
       $("#ruleID span").text(id);
       
       $.ajax({
           type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getRuleDetails',
                ruleID: id
            },
            success : function(jsonObj) {
                $("#ruleID span").html(jsonObj['ruleID']);
                $("#ruleCondition span").html(jsonObj['ruleCondition']);
                $("#ruleCornerstoneCaseID span").html(jsonObj['ruleCornerstoneCaseID']);
                $("#ruleConclusion span").html(jsonObj['ruleConclusion']);
                $("#ruleConclusionCategory span").html(jsonObj['ruleConclusionCategory']);
                $("#ruleConclusionAction span").html(jsonObj['ruleConclusionAction']);
                $("#ruleStopped span").html(jsonObj['ruleStopped']);
                $("#ruleDoNotStack span").html(jsonObj['ruleDoNotStack']);
                
                unhighlightRules("rulebaseViewerSelected");
                highlightRule(id,"rulebaseViewerSelected");
            }
         });
 
    }
    
    function processGetRulebase() {
       $.ajax({
           type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getRulebase'
            },
            success : function(response) {
                $('#rulebaseViewer').html(response);
                   
            }
         });  
    }

    
    
    
    
</script>
