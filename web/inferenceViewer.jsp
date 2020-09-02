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
<script type="text/javascript" src="./js/dictionary.js"></script>
<script type="text/javascript" src="./js/queryBuilder.js"></script>
<script type="text/javascript" src="./js/conclusion.js"></script>
<script type="text/javascript" src="./js/newConclusion.js"></script>
<script type="text/javascript" src="./js/userContextVariables.js"></script>
<script type="text/javascript" src="./js/systemContextVariables.js"></script>
<script type="text/javascript" src="./js/knowledgeAcquisition.js"></script>
<script type="text/javascript" src="./js/preprocessor.js"></script>

<link rel="stylesheet" href="./styles/styles.css" type="text/css" >
<link rel="stylesheet" href="./styles/jquery-ui.min.css" type="text/css" >

<meta charset="utf-8">

<!-- placeholder for all confirmation dialogs !-->
<div id="confirmDialog"></div>
<div id="confirmDialog2"></div>
<div id="confirmDialog3"></div>
<div id='metaDataDialog'></div>
<div id='metaDataDialog2'></div>
<div id='metaDataDialog3'></div>
<div id='metaDataDialogButtonDialog'></div>
<div id='mainContentDivInferenceViewer'></div>



<script>
    
    
    // ######################################
    // NOTES FOR MAY: Add js interface for the database category items (server-side working!)
    // // something screwy is occuring in AdminJavaGUI look at line 512 - mode should be passed in, not set to modify.
    // This has an effect when we're trying to add a new rule to root but after it had a valid response (so do not want to 
    // add to previous context.  Dear me - REMEMBER!!!! Hmm still not working, wrong parent rule is being used instead of root.
    // MCRDR learner is setting mode to alter as the inference result doens't have the root rule.. (line 71 in MCRDRLearner)
    // Also might be line 177ish in AddRuleGUI - should be using dialog user's KA inference result?
    // 
    // finl comment, when adding subrule it's still not being added as exception (?), but alter so put as sibling
    // (see addAlternativeRule)
    // FINAL NOTE - permanelty setting mode to modify seems to fix - line 519 or AdminJavaGUI, and altered to use dialoig KA not last
    // stack inference result!
    // Summary? now working?
    // 
    // KA for invalid system response now working
    // add code now for creating new (simple) conclusions
    // // Note stopping not working...?
    // also check when adding new rule as new context why it's not going to root when
    // last response is valid..
    // ######################################
    // ######################################
    // ######################################
    // ######################################
    // ######################################
   
    
    var inferenceViewerEventListenersRegistered = false;
    
    $(document).ready(function() {
        getIsDomainInitialised(getIsDomainInitialisedCallback);
        createInferenceViewerInterface('mainContentDivInferenceViewer');
        utilsAddEventListeners();
        $(function() {
            $(document).tooltip({
                show: {effect: "highlight", duration: 1000, delay: 500},
                hide: {effect: "highlight", duration: 1000, delay: 500},
                position: { my: "left+15 center", at: "center" }//,
                //track: true
            });
        });
    });
    
    function noopCallback() {       
    } 
    
    
    function createInferenceViewerInterface(parent) {
        $('#' + parent).append("<div id='heading'>Inference Viewer</div>");
        $('#' + parent).append("   <div class='column' id='inferenceMainColumn'>");
        $('#' + 'inferenceMainColumn').append("<div class='majorcolumnA' id='inferenceMajorcolumnA'></div>");
        $('#' + 'inferenceMajorcolumnA').append("<div id='leftColumnKA'></div>");
        $('#' + 'leftColumnKA').append("<div class='columnHeading'>Dialog history</div>");
        $('#' + 'leftColumnKA').append("<div class='columnItem' id='inferenceDialogHistoryColumn'></div>");
        $('#' + 'inferenceDialogHistoryColumn').append("<div id='dialogHistory'></div>");
        $('#' + 'leftColumnKA').append(" <div class='columnItem' id='inferenceUserSelectColumn'></div>");
        
        $('#' + 'inferenceUserSelectColumn').append("<div class='admin'><label>Select user:</label> <select id='selectUser'></select></div>");
        $('#' + 'inferenceUserSelectColumn').append("<div class='fullblockWrapperSuperSlim'><input id='refresh' type='button' value='Refresh' class='majorbutton hbutton'></div>");
        $('#' + 'inferenceUserSelectColumn').append("<div class='fullblockWrapperSuperSlim'><input id='showVars' type='button' value='Show variables' class='majorbutton hbutton'><input id='showActions' type='button' value='Show actions' class='majorbutton hbutton'></div>");      
        $('#' + 'inferenceUserSelectColumn').append("<div class='fullblockWrapperSuperSlim'><input id='resetConv' type='button' value='Clear conversation' class='majorbutton hbutton'><input id='saveConv' type='button' value='Save conversation' class='majorbutton hbutton'></div>");

        $('#' + 'inferenceMainColumn').append("<div class='majorcolumnB' id='inferenceMajorcolumnB'></div>");
        $('#' + 'inferenceMajorcolumnB').append("<div id='rightColumnKA'></div>");
        $('#' + 'rightColumnKA').append("<div id='mainRuleHeading' class='columnHeading'>Knowledge-base rules</div>");
        $('#' + 'rightColumnKA').append("<div class='columnItem'><div id='rulebase'></div></div>");


        $('#' + 'inferenceMainColumn').append("  <div class='majorcolumnC' id='inferenceMajorcolumnC'></div>");
        $('#' + 'inferenceMajorcolumnC').append("<div id='rightColumnStack'></div>");
        $('#' + 'rightColumnStack').append("<div class='columnHeading'>Stack frames</div>");
        $('#' + 'rightColumnStack').append("<div class='columnItem'><div id='stack'></div></div>");


        $('#' + 'inferenceMainColumn').append(" <div id='legendDiv'></div>");
        $('#' + 'legendDiv').append("<div id='dialogLegend'></div>");
        $('#' + 'dialogLegend').append("<div class='columnHeading'>Dialog legend</div>");
        $('#' + 'dialogLegend').append("<div class='red legendItem'>Selected system reply</div>");
        $('#' + 'dialogLegend').append("<div class='green legendItem'>User dialog </div>");
        $('#' + 'dialogLegend').append("<div class='blue legendItem'>System reply</div>");
        $('#' + 'dialogLegend').append("<div class='dialogHighlightFromRuleOrStackLegend legendItem'>System replies related to selected rule or stack frame</div>");

        $('#' + 'legendDiv').append("<div id='ruleLegend'></div>");
        $('#' + 'ruleLegend').append("<div class='columnHeading'>Rule legend</div>");
        $('#' + 'ruleLegend').append("<div class='ruleHighlightSelectedLegend legendItem'>Selected rule</div>");
        $('#' + 'ruleLegend').append("<div class='ruleHighlightFromDialogSatisfiedLegend legendItem'>Rules satisfied by user dialog</div>");
        $('#' + 'ruleLegend').append("<div class='ruleHighlightFromDialogStartingRulesLegend legendItem'>Inference starting rules</div>");
        $('#' + 'ruleLegend').append("<div class='ruleHighlightFromStackLegend legendItem'>Rules satisfied in selected frame</div>");
        $('#' + 'ruleLegend').append("<div class='isNotStacked legendItem whiteBackground'>Rules that are not stacked</div>");
        $('#' + 'ruleLegend').append("<div class='isStopped legendItem whiteBackground'>Rules that stop their parent</div>");

        $('#' + 'legendDiv').append("<div id='stackLegend'></div>");
        $('#' + 'stackLegend').append("<div class='columnHeading'>Stack legend</div>");
        $('#' + 'stackLegend').append(" <div class='stackHighlightSelectedLegend legendItem'>Selected frame</div>");
        $('#' + 'stackLegend').append("<div class='stackHighlightCurrentLegend legendItem'>Frame containing dialog inference results</div>");
        $('#' + 'stackLegend').append("<div class='stackHighlightFromDialogSatisfiedLegend legendItem'>Frame containing dialog starting rules</div>");
        $('#' + 'stackLegend').append("<div class='stackHighlightFromRuleLegend legendItem'>Frames containing selected rule</div>");
        $('#' + 'stackLegend').append("<div class='stackFrameNotStacked legendItem'>Frames ignored in stack</div>");

        $('#' + 'inferenceMainColumn').append("<div class='ivcontrols'><input id='knowledgeAcquisitionButton' type='button' value='Knowledge Acquisition' class='navigation button'></div>");
        $('#' + 'inferenceMainColumn').append("<div class='ivcontrols'><input id='metaDataButton' type='button' value='Modify metadata' class='navigation button'></div>");
    
        inferenceViewerAddEventListeners();
    }
    
    function inferenceViewerAddEventListeners() {
        if (!inferenceViewerEventListenersRegistered) {
            
            $('body').on('click','#refresh', refreshGUI);
            
            $('body').on('click', '#resetConv', resetUser);
            
            $('body').on('click', '#saveConv', saveUser);
            
            $('body').on('click', '#showVars', showVariables);
            
            $('body').on('click', '#showActions', showActions);
            
            $('body').on('click','#metaDataButton',showMetaDataDialog);
            
            $('body').on('change',"#selectUser",function(e) {
                populateSelectUser(selectUserCallback,getSelectedUserName());
            });

            /* selecting a dialog ID */
            $('body').on('click','#dialogHistory', function(e) {
                if (e.target.id !== "dialogHistory") {        
                    let dialogID = e.target.id.substr(7);

                    if ($(e.target).hasClass("blue") || $(e.target).hasClass("systemPre")) {
                        // we only want system reply dialogs to be selected, not user dialogs
                        updateRuleAndStackSelectedFromDialogId(dialogID);
                        setStatusSelectedDialogIDKA(dialogID);
                        setStatusSelectedStackID(-1);
                    }
                }
            });
    
            /* selecting a stack frame */
            $('body').on('click','#stack',function(e) {
                if (e.target.id !== "stack") {        
                    let frameID = e.target.id.substr(5);               
                    updateRulesAndDialogHighlightedFromStackId(frameID);
                    setStatusSelectedDialogIDKA(0);
                    setStatusSelectedStackID(frameID);
                }
            });
    
            /* selecting a rule */
            $('body').on('click','#rulebase',function(e) {
                if (e.target.id.substr(0,5) !== "ruleb") {        
                    if (e.target.id.substr(0,4) === "rule") { 
                        let ruleId = e.target.id.substr(4);
                        updateDialogHistoryAndStackHighlightedFromRule(ruleId);
                        setStatusSelectedDialogIDKA(0);
                        setStatusSelectedStackID(-1);
                    }

                }
            });

            $('body').on('click','#knowledgeAcquisitionButton',function(e) {
                dconsole("KA begins..");
                let actualConversationCount = $(".bubble").length;
                
                if (actualConversationCount > 0)
                    confirmStartKnowledgeAcquisition();
                else
                    standardOK("confirmDialog","ERROR","The knowledge-base may have been reset - there may possibly be a user connected, but no current conversation history is active")();
            });
            
            
            /* *******************  metadata buttons *********** */
            // meta data button section
            /* **************************************************************** */
            $('body').on('click',"#KAdictionaryButton",function(e) {
                dictionaryDocumentReady("metaDataDialog",null);
                standardProcessDialog('metaDataDialog', "Dictionary",dictionaryMainDialogCloseCallback)();
            });

            $('body').on('click',"#KAqueryBuilderButton",function(e) {
                queryDocumentReady("metaDataDialog",null);
                standardProcessDialog('metaDataDialog', "Query Builder",queryMainDialogCloseCallback)();
            });
            
            $('body').on('click',"#KAuserVariablesButton",function(e) {
                userContextVariablesDocumentReady("metaDataDialog",null);
                standardProcessDialog('metaDataDialog', "User Context Variable maintenance",userContextVariablesMainDialogCloseCallback)();
            });
            
            $('body').on('click',"#KAsystemVariablesButton",function(e) {
                systemContextVariablesDocumentReady("metaDataDialog",null);
                let buttonList = [ 
                    {                
                        id: "systemContext-button-cancel",
                        text: "Cancel",
                        click: function() {
                            systemContextCancelButtonHander("metaDataDialog");
                        }
                    },                  
                    {
                        id: "systemContext-button-saveall",
                        text: "Save All Changes",
                        click: function() {
                            systemContextSaveAllButtonHander("metaDataDialog");
                        }
                    }     
                ];
                standardProcessDialog('metaDataDialog', "System Context Variable maintenance",systemContextVariablesMainDialogCloseCallback,buttonList,550)();
            });
            
            $('body').on('click',"#KApreprocessorButton",function(e) {
                preprocessorDocumentReady("metaDataDialog");
                standardProcessDialog('metaDataDialog', "Preprocessor maintenance",preprocessorMainDialogCloseCallback,null,550)();
            });
            
            $('body').on('click',"#KApostprocessorButton",function(e) {
                postprocessorDocumentReady("metaDataDialog",null);
                standardProcessDialog('metaDataDialog', "Postprocessor maintenance",postprocessorMainDialogCloseCallback)();
            });
                   
            $('body').on('click',"#KAresetKBButton",function(e) {
                standardDialog("confirmDialog",
                    "Reset Knowledge-base",
                    "This will reset the knowledge-base, removing all rules and data.  Are you sure you want to continue?",
                    //kaForInvalidResponse,
                    resetKnowledgebase,
                    standardOK("confirmDialog","Cancel reset KB","The reset of the Knowledge-base has been cancelled")         
                );    
            }); 
        }
        
        inferenceViewerEventListenersRegistered = true;
    }
    
    function saveUser() {
        dconsole("Save user..");
        if (getStatusIsValidUser()) {
            let username = getSelectedUserName();
            let dataList = {mode:"saveUser", username: username};   
            generalAjaxQuery('KnowledgeAcquisitionServlet',saveUserCallback,dataList,null,null);
        }
    }
    
    function saveUserCallback(response) {
        dconsole("saveUserCallback..");
        download('dialogHistory - ' + getSelectedUserName() + '.txt', response['result']);
        standardOK("confirmDialog","Notice","The selected user history has been downloaded.")();  
    }
    
    function resetUser() {
        if (getStatusIsValidUser()) {
            let username = getSelectedUserName();
            
            standardDialog("confirmDialog",
                "Clear selected user history",
                "This will clear the current conversation for the user: " + username + ".  Are you sure you want to continue?",
                resetUserCallback,
                standardOK("confirmDialog","Cancel clear user history","The clear user history has been cancelled.")         
            );    
        }
        else {
            standardOK("confirmDialog","Error","No valid user has been selected.")();
        }
    }
    
    function resetUserCallback(response) {
        let username = getSelectedUserName();
        let dataList = {mode:"resetUser", username: username};   
        generalAjaxStringQuery('KnowledgeAcquisitionServlet',resetUserCallback2,dataList,null,null);
    }
    
    function resetUserCallback2() {
         standardOK("confirmDialog","Notice","Selected user history has been cleared.")();
         refreshGUI();
    }
    
    function showVariables() {
        let username = getSelectedUserName();
        if (getStatusSelectedDialogIdKA() === 0 && getStatusSelectedStackID() === -1)
           standardOK("confirmDialog","Error","Please select a dialog or stack frame first.")();
        else {
            if (getStatusSelectedDialogIdKA() !== 0) {
                let dataList = {mode:"getContextVariablesAndValues",username:getSelectedUserName(),dialogID:getStatusSelectedDialogIdKA()};
                generalAjaxQuery('KnowledgeAcquisitionServlet',showVariablesCallback,dataList,null,null);
            }
            else if (getStatusSelectedStackID() !== -1) {
                let dataList = {mode:"getContextVariablesAndValuesFromStack",username:getSelectedUserName(),stackID:getStatusSelectedStackID()};
                generalAjaxQuery('KnowledgeAcquisitionServlet',showVariablesCallback,dataList,null,null);
            }
        }
    }
  
    function showVariablesCallback(response) {
        let header = response['header'];
        let rows = response['rows'];
        let headerWidths = [50,200,100];
        let body = createBodyTableViewer(header, headerWidths,rows);
        standardOKLarge("confirmDialog", "Current Context Variables",body,500)();
    }
    
    function showActions() {
        let dataList = {mode:"getAllExistingContextVariableActions"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',showActionsCallback,dataList,null,null);
    }
    
    function showActionsCallback(response) {
        let header = response['header'];
        let rows = response['rows'];
        let headerWidths = [200,200,200,200];
        let body = createBodyTableViewer(header, headerWidths,rows);
        standardOKLarge("confirmDialog", "Current Context Variable Actions",body,860)();
    }

    
    function showMetaDataDialog() {
        createMetaDataInterface("metaDataDialogButtonDialog","KAmetaData", false);
        standardProcessDialog('metaDataDialogButtonDialog', "Metadata management",showMetaDataDialogCallback,null,350)();
    }
    
    function showMetaDataDialogCallback() {
        $('#metaDataDialogButtonDialog').html("");
    }
    
    function resetKnowledgebase() {
        let dataList = {mode:"resetKnowledgebase"};
        generalAjaxStringQuery('KnowledgeAcquisitionServlet',resetKnowledgebaseCallback,dataList,null,null);
    }
    
    function resetKnowledgebaseCallback() {
        standardOK("confirmDialog","Notice","The knowledge-base has been cleared.")();
        $("#divKAforResponseBackButton").trigger('click');
        refreshGUI();
    }
    
    function processDialogUserSelect() {
        
    }
    
    function showAndHide(showDiv,hideDiv,deleteHideDiv, registerMainContentShowDiv) {
        $("#" + showDiv).show(300);
        if (hideDiv !== null)
            $("#" + hideDiv).hide(300);
        
        if (deleteHideDiv)
          $("#" + hideDiv).remove();
      
        if (registerMainContentShowDiv)
            setStatusMainContent(showDiv);
      
        // other items..
        let items = getStatusRegisterToBeDeleted();
        $.each(items,function(index,value) {
            $("#" + value).remove();
        });
        resetStatusRegisterToBeDeleted();
    }
   
    function getIsDomainInitialisedCallback() {
        if (getStatusIsDomainInitialised()) {
            populateSelectUser(getIsDomainInitialisedCallback2,null);
            processGetRulebase();           
        }          
        else {
            standardOK("confirmDialog","Error","The knowledge base has not been initialised.")();
            setItemMessage("stack","Domain uninitialised");
            setItemMessage("dialogHistory","Domain uninitialised");
            setItemMessage("rulebase","Domain uninitialised");
        }
        unhighlightAllStackFrames();
        unhighlightAllRules();
        unhighlightAllDialogs();
    }
    
    function getIsDomainInitialisedCallback2() {
        if (getStatusIsValidUser()) {
                processGetDialogHistory("dialogHistory","dialog",noopCallback);
                populateStack(getSelectedUserName());              
            }
        else {
                setItemMessage("stack","No users yet");
                setItemMessage("dialogHistory","No conversations yet..");
            }
    }  
 
    function refreshGUI() {
        if (getStatusIsDomainInitialised()) {
            populateSelectUser(refreshCallback,getSelectedUserName());
            processGetRulebase();
        }
        else {
           standardOK("confirmDialog","Error","The knowledge base has not been initialised.")();
           setItemMessage("stack","Domain uninitialised");
           setItemMessage("dialogHistory","Domain uninitialised");
           setItemMessage("rulebase","Domain uninitialised");
        }
        unhighlightAllStackFrames();
        unhighlightAllRules();
        unhighlightAllDialogs();
        setStatusSelectedDialogIDKA(0);
    }
    
    function refreshCallback() {
        if (getStatusIsValidUser()) {
                processGetDialogHistory("dialogHistory","dialog",noopCallback);
                populateStack(getSelectedUserName());
        }
        else {
                standardOK("confirmDialog","Status","No user conversation sessions have started yet..")();
                setItemMessage("stack","No users yet");
                setItemMessage("dialogHistory","No conversations yet..");
        }
    }
    
    function selectUserCallback() {
        processGetDialogHistory("dialogHistory","dialog",noopCallback);
        processGetRulebase();
        populateStack(getSelectedUserName());
    }

    function unhighlightAllRules() {
        unhighlightRules('ruleHighlightFromDialogSatisfied');
        unhighlightRules('ruleHighlightFromDialogStartingRules');
        unhighlightRules('ruleHighlightSelected');
        unhighlightRules('ruleHighlightFromStack');
    }
    
    function unhighlightAllStackFrames() {
        unhighlightStack('stackHighlightFromRule');
        unhighlightStack('stackHighlightFromDialogSatisfied');
        unhighlightStack('stackHighlightSelected');
        unhighlightStack('stackHighlightCurrent');
    }
    
    function unhighlightAllDialogs() {
        unhighlightDialog('dialogHighlightFromRuleOrStack');
        unhighlightDialog('dialogHighlightSelected');
        //setStatusSelectedDialogIDKA(0);
    }
    
    
    
    function setItemMessage(item,message) {
        $("#"+item).html("");
        $("#"+item).append("<div class='errormessage'>" + message + "</div>");
    }
    
    function populateStack(username) {
        $('#stack').html("");
        $.ajax({
           type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getMaxStackID',
                username : username
            },
            success : function(response) {
                maxStacks = parseInt(response);
                
                for (var i = maxStacks; i > 0; i--) {
                    $("#stack").append("<div id='stack" + i +"' class='stacked whiteBackground'>" + i +"</div>");
                    if (i !== 0) // no need to check frame 0..
                        getIsStackFrameNotStacked(i);

                }
                $("#stack").append("<div id='stack" + 0 +"' class='stackedBottom whiteBackground'>" + 0 +"</div>");

                
            }
        });    
    }
    
    function getIsStackFrameNotStacked(frameID) {
        let dataList = {mode:"getIsStackFrameNotStacked", username: getSelectedUserName(), frameID: frameID};
        generalAjaxStringQuery('KnowledgeAcquisitionServlet',getIsStackFrameNotStackedCallback,dataList,frameID,null);
    }
    
    function getIsStackFrameNotStackedCallback(response, frameID) {
        $("#" + "stack" + frameID).addClass('stackFrameNotStacked');
    }
    
    function updateDialogHistoryAndStackHighlightedFromRule(ruleId) {
        
        unhighlightAllDialogs();
        unhighlightAllRules();
        unhighlightAllStackFrames();
        
        highlightRule(ruleId,'ruleHighlightSelected');
        if (currentStatus.validUser) {
            $.ajax({
               type: 'GET',
                url : 'KnowledgeAcquisitionServlet',
                dataType:'json',
                //async: false,
                data : {
                    mode : 'getDialogsAndStackFramesFromRule',
                    username: getSelectedUserName(),
                    ruleID : ruleId
                },
                success : function(response) {
                    var dialogIds = response['dialogList'].split(',');                   
                       
                    var stackFrameIds = response['stackFrameList'].split(',');

                    // work out scrolling
                    let firstDialog = $("#dialogHistory div:first-child").attr("id");
                    
                    //let firstFoundDialog = 2;
                    let firstFoundDialog = firstDialog;
                    let firstFoundFrame = 0;

                    $.each(dialogIds, function(index, dialogId) {
                        if (dialogId !== '') {
                            highlightDialog("adialog" + dialogId,'dialogHighlightFromRuleOrStack');
                            //if (firstFoundDialog === 2)
                            if (firstFoundDialog === firstDialog)
                                firstFoundDialog = "adialog" + dialogId;
                        }
                    }); 

                    $.each(stackFrameIds, function(index, frameId) {
                        if (frameId !== '') {
                            highlightStack(frameId,'stackHighlightFromRule');
                            if (firstFoundFrame === 0)
                                firstFoundFrame = frameId;
                        }
                    }); 

                    $('#' + firstFoundDialog).get(0).scrollIntoView();
                    $('#stack' + firstFoundFrame).get(0).scrollIntoView();

                }
            });
        }
        else {
            standardOK("confirmDialog","Notice","No conversations have occured yet so selecting rules have no effect")();
        }
    }

    function updateRulesAndDialogHighlightedFromStackId(frameId) {
        unhighlightAllDialogs();
        unhighlightAllRules();
        unhighlightAllStackFrames();

        highlightStack(frameId,'stackHighlightSelected');
        
        if (currentStatus.validUser) {
            $.ajax({
                type: 'GET',
                url : 'KnowledgeAcquisitionServlet',
                dataType:'json',
                //async: false,
                 data : {
                     mode : 'getDialogsAndRulesFromStackFrame',
                     username: getSelectedUserName(),
                     stackFrameID: frameId
                 },
                 success : function(response) {
                    let ruleList = response['ruleList'].split(',');
                    let dialogIds = response['dialogList'].split(',');

                    let firstFoundDialog = 2;
                    let firstFoundRule = 0;

                     $.each(ruleList, function(index, ruleNumber) {
                        highlightRule(ruleNumber,'ruleHighlightFromStack');
                        if (firstFoundRule === 0)
                            firstFoundRule = ruleNumber;
                    });

                    $.each(dialogIds, function(index, dialogId) {
                        if (dialogId !== '') {
                            highlightDialog("adialog" + dialogId,'dialogHighlightFromRuleOrStack');
                            if (firstFoundDialog === 2)
                                firstFoundDialog = dialogId;
                        }
                    }); 

                    $('#rule' + firstFoundRule).get(0).scrollIntoView();
                    $('#adialog' + firstFoundDialog).get(0).scrollIntoView();
                 }
            });  
        }
        else {
            standardOK("confirmDialog","Notice","No conversations have occured yet so selecting frames have no effect")();
        }
    }
    
    function updateRuleAndStackSelectedFromDialogId(dialogId) {
        
        $.ajax({
           type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            //async: false,
            data : {
                mode : 'getFiredRuleList',
                username: getSelectedUserName(),
                dialogID : dialogId
            },
            success : function(response) {
                unhighlightAllDialogs();
                unhighlightAllRules();
                unhighlightAllStackFrames();
                
                highlightDialog("adialog" + dialogId,'dialogHighlightSelected');

                let stackId = response['stackId'];
                let satisfiedByStackId = response['satisfiedByStackId'];
                let startingRuleList = response['startingRuleList'].split(',');
                let ruleList = response['ruleList'].split(',');
                
                let firstFoundRule = 0;
                

                $.each(startingRuleList, function(index, ruleNumber) {
                    highlightRule(ruleNumber,'ruleHighlightFromDialogStartingRules');
                });
                
                $.each(ruleList, function(index, ruleNumber) {
                    highlightRule(ruleNumber,'ruleHighlightFromDialogSatisfied');
                    if (firstFoundRule === 0)
                        firstFoundRule = ruleNumber;
                });
                highlightStack(satisfiedByStackId,'stackHighlightFromDialogSatisfied');

                highlightStack(stackId,'stackHighlightCurrent');

                /* scrolling */
                $('#rule' + firstFoundRule).get(0).scrollIntoView();
                $('#stack' + stackId).get(0).scrollIntoView();
            }
        });
        
    }
    
    function unhighlightRules(highlightClass) {
        $('.'+highlightClass).each(function(index,aRule) {
            $(this).removeClass(highlightClass);
        });
    }
    
    function highlightRule(ruleNumber,ruleHighlightClass) {
        $("#rule" + ruleNumber).addClass(ruleHighlightClass);
    }
    
    function unhighlightStack(stackHighlightClass) {
        $('.'+stackHighlightClass).each(function(index,aStack) {
            $(this).removeClass(stackHighlightClass);
            $(this).addClass("whiteBackground");

        });
    }
    
    function highlightStack(stackNumber,stackHighlightClass) {
        $("#stack" + stackNumber).addClass(stackHighlightClass);
        $("#stack" + stackNumber).removeClass("whiteBackground");
    }  
    
    function highlightDialog(dialogId,dialogHighlightClass) {
        $("#" + dialogId).addClass(dialogHighlightClass);
    }
    
    function unhighlightDialog(dialogHighlightClass) {
        $('.'+dialogHighlightClass).each(function(index,aDialog) {
            $(this).removeClass(dialogHighlightClass);
        });
    }
    
    function getSelectedUserName() {
        var username = $("#selectUser").val();
        if (username === "" || username === null) {
            $("#selectUser").val($("#selectUser option:first").val());
        }
        return $("#selectUser").val();
    }
    
    function processGetDialogHistory(divName,dialogIdPrefix,callback) {
        $('#' + divName).html("");
        var username = getSelectedUserName();
        if (username !== "" && username !== null) {
            $.ajax({
               type: 'GET',
                url : 'KnowledgeAcquisitionServlet',
                //async: false,
                dataType:'json',
                data : {
                    username: getSelectedUserName(),
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
                        utterance = escapeHTML(utterance.replace("\\n","\n"));
                        if (!systemReply) {
                            dialogClass = userBubbleClass;
                            preClass = userPreClass;
                        }
                        else {   
                            dialogClass = systemBubbleClass;
                            preClass = systemPreClass;

                        }

                        $('#' + divName).append("<div class='" + dialogClass + "' id='" + "a" + dialogIdPrefix + dialogData['dialogId'] + "'><pre class='" + preClass + "' id='" + "b" + dialogIdPrefix + dialogData['dialogId'] + "'>" + utterance+"</pre></div>");

                        systemReply = !systemReply;
                    });
                    
                    var d = $('#' + divName);
                    d.scrollTop(d.prop("scrollHeight"));
                    callback();
                }
            }); 
        }
         
           
    }
    
    function processGetRulebase() {
       $('#rulebase').html(""); 
       $.ajax({
           type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getRulebase'
            },
            success : function(response) {
                $('#rulebase').append(response);
                   
            }
         });  
    }
    
    function getSessionID(callback,parentCallback,selectedUser) {
        var sessionId;
        
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getSessionID'
            },
            
            success : function(response) {
                sessionId = response;
                currentStatus.sessionID = sessionId;
                
                callback(parentCallback,selectedUser);
            }
        });
        
        //return sessionId;
    }
    
    function populateSelectUser(callback,selectedUser) {
        getSessionID(populateSelectUserCallback,callback,selectedUser);
    }

    function populateSelectUserCallback(callback,selectedUser) {
        
        var sessionId = currentStatus.sessionID;
        
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getUsers'
            },
            
            success : function(responseText) {
                var users = responseText.split(',');
                let textValue;
                $('#selectUser').empty();
                currentStatus.validUser = users[0].trim().length > 0;                
                $.each(users,function(i,aUser) {
                    textValue = aUser;
                    
                    if (textValue === sessionId)
                        textValue = "(your chat session)";

                    $('#selectUser').append($('<option>', {
                            value: aUser,
                            text: textValue
                    }));
                }); 
                
                // set the selected user to the selectedUser passed as a parameter
                if (selectedUser !== null) {
                    $("#selectUser").val(selectedUser); 
                }
                else {
                    if ($.inArray(sessionId,users) !== -1) {
                        $("#selectUser").val(sessionId); 
                    }
                    else {
                        $("#selectUser").prop('selectedIndex',0);
                    }
                }
                    
                callback();    
            }});
        }          
    
    function hideDivItem(divName) {
        $("#" + divName).hide(800);
    }
    
    function showDivItem(divName) {
        $("#" + divName).show(800);
    }
    
    function clearDivItem(divName) {
        $("#" + divName).html("");
    }
    
    function deleteDivItem(divName) {
        $("#" + divName).remove();
    }
        

      
</script>
