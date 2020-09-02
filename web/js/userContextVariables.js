    var userContextVariablesMain = "KAuserContextVariablesWrapper";
    
    function getUserContextVariablesMain() {
        return userContextVariablesMain;
    }
    
    var userContextVariablesEventListenersRegistered = false;
    
    function userContextVariablesDocumentReady(parent,sibling) {
        createUserContextVariablesInterface(parent,sibling);
    }
    
     function userContextVariablesAddEventListeners() {
        if (!userContextVariablesEventListenersRegistered) {
            $('body').on('click','#variableEditButton',processVariableEditButton);  // criteria
            $('body').on('click','#variableActionButton',processVariableActionButton); // actions
            $('body').on('click','#newVariableCreateButton',processNewVariableCreateButton);
            $('body').on('click','#deleteVariableActionButton',processDeleteVariableActionButton);
     
            
            $('body').on('change','#currentContextVariables', function(e) {         
                var contextVar = $("#currentContextVariables").val();
                let dataList = {mode:"getContextVariableCriteria",variableName: contextVar,username: getSelectedUserName()};
                populateSelector("currentContextVariablesCriteria",dataList,noopCallback);  
                dataList = {mode:"getContextVariableOverride",variableName: contextVar};               
                populateField("currentVariableOverride",dataList,noopCallback);
                dataList = {mode:"getExistingContextVariableActions",variableName: contextVar};
                populateSelector("currentContextVariablesActions",dataList,noopCallback); 
            });
            
            //$('body').on('click','#contextDetectDictionaryTerm',processDetectDictionaryTerm);
            
            $('body').on('mouseup','#contextRecentInput',processDetectDictionaryTerm);
            
            $('body').on('click','#contextAddDetectedDictionaryTerm',processAddDetectedDictionaryTerm);
            $('body').on('click','#contextAddDictionaryTerm',processAddDictionaryTerm);
            $('body').on('click','#addMatchingCriteria',processAddMatchingCriteria);
            $('body').on('click','#saveContextVariableChangesButton',processSaveContextVariableChangesButton);
            $('body').on('click','#existingCriteriaDeleteButton',processExistingCriteriaDeleteButton);
            
            $('body').on('click','#addVariableAction',processAddVariableAction);

            
            $('body').on('change',"input[type=radio][name=actionTrigger]",function(e) {
                let triggerConditionRadioSelected = $("input[name='actionTrigger']:checked").val();
                if (triggerConditionRadioSelected === "1") {
                    disableItem("actionTriggerContainsContextVariable");
                }
                else {
                    enableItem("actionTriggerContainsContextVariable");
                }
            });
            
            $('body').on('change',"input[type=radio][name=actionValue]",function(e) {            
                let triggerValueRadioSelected = $("input[name='actionValue']:checked").val();
                if (triggerValueRadioSelected === "1") {
                    disableItem("actionTargetValueLiteral");
                    enableItem("actionTargetValueContextVariable");
                }
                else {
                    disableItem("actionTargetValueContextVariable");
                    dconsole("Enabling actionTargetValueLiteral");
                    enableItem("actionTargetValueLiteral");

                }
            });

            userContextVariablesEventListenersRegistered = true;
        }
    }
    
    /* ******************* User Context Variables  *************************** */
    // Main div for adding/modifying user context variables
    /* **************************************************************** */ 
    function createUserContextVariablesInterface(parentDiv,sibling) { 
        
        let mainWrapper = getUserContextVariablesMain();
        
        if ($("#" + getUserContextVariablesMain()).length)
            $("#" + getUserContextVariablesMain()).remove();
        
        $("#" + parentDiv).html("");           
        $("#" + parentDiv).prepend("<div id='" + mainWrapper + "' class='column'></div>");
        
        let dataList = {mode:"populateContextVariables"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',createUserContextVariablesInterfaceCallback,dataList);  
    }
    
    function createUserContextVariablesInterfaceCallback() {
        let mainWrapper = getUserContextVariablesMain();
        dconsole("createUserContextVariablesInterfaceCallback!!");
        $("#" + mainWrapper).append("<div id='newContextVariableDiv' class='fullColumn'></div>");
        newContextVariableContent('newContextVariableDiv');
        
        $("#" + mainWrapper).append("<div id='currentContextVariableDiv' class='fullColumn'></div>");
        currentContextVariablesContent('currentContextVariableDiv');
        
        $("#" + mainWrapper).append("<div class='fullColumn'><input id='variableEditButton' type='button' value='Edit criteria' class='inlineButtonSmallFloatLeft hbutton '><input id='variableActionButton' type='button' value='Edit actions' class='inlineButtonSmallFloatRight hbutton '></div>");
        $("#" + mainWrapper).append("<div class='fullColumn'><input id='saveContextVariableChangesButton' type='button' value='Save all changes' class='inlineButtonSmallFloatRight hbutton '></div>");
        
        userContextVariablesAddEventListeners();
    }
    
    function newContextVariableContent(parentDiv) {
        let myMainDiv = 'newContextVariableDivColumn';
        $("#" + parentDiv).html("");
        $("#" + parentDiv).append("<div class='tableHeader'>Create a new context variable</div>");
        $("#" + parentDiv).append("<div id='" + myMainDiv + "'> </div>");
        
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpan' style='margin-top:15px;'>@</span><input id='newVariableName' type='text' placeholder='New variable name...' class='textInput'><input id='newVariableCreateButton' type='button' value='Create...' class='inlineButtonSmallFloatLeft hbutton '></div>");
    }
    
    function currentContextVariablesContent(parentDiv) {
        let myMainDiv = 'currentContextVariableDivColumn';
        $("#" + parentDiv).html("");
        $("#" + parentDiv).append("<div class='tableHeader'>Current context variables</div>");
        $("#" + parentDiv).append("<div id='" + myMainDiv + "'> </div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpanFixedWidth'>Variable</span><select id='currentContextVariables' class='KAinputLeft'/></div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpanFixedWidth'>Criteria</span><select id='currentContextVariablesCriteria' class='KAinputLeft'/></div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpanFixedWidth' style='margin-top:15px;'>Override</span><input id='currentVariableOverride' type='text' class='textInputWideWidth' readonly/></div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpanFixedWidth'>Actions</span><select id='currentContextVariablesActions' class='KAinputLeft'/></div>");

        let dataList = {mode:"getContextVariablesForModification", username:getSelectedUserName()};
        populateSelector("currentContextVariables",dataList,currentContextVariablesContentCallback,myMainDiv); 
        //getContextVariablesForModification("currentContextVariables",currentContextVariablesContentCallback,getSelectedUserName());
    }
    
    
    function currentContextVariablesContentCallback(parentDiv) { 
        fireTriggerChange("currentContextVariables");
    }
    
    function userContextVariablesMainDialogCloseCallback() {
        updateMetaDataInterface(null);
    }
    
    function processSaveContextVariableChangesButton() {
        let dataList = {mode:"saveContextVariableChanges"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',processSaveContextVariableChangesButtonCallback,dataList);  
    }
    
    function processSaveContextVariableChangesButtonCallback(response) {
        if (response['status'] === "OK")
             standardOK("confirmDialog", "Notice","Changes saved!")();
    }
    
    function processAddMatchingCriteria() {
        
        // new criteria
        let criteria = $.trim($('#currentMatchingCriteria').val());
        let override =  $.trim($('#currentMatchingCriteriaOverride').val());
        dconsole("existingContextCriteria length is: " + getSelectorLength("existingContextCriteria"));
        dconsole("criteria is: " + criteria);
        if (criteria === "" && getSelectorLength("existingContextCriteria") === 0)
            standardOK("confirmDialog", "Error","You have not specified any matching criteria!")();
        else  {
            if (selectorContainsOption("existingContextCriteria",criteria)) {
                standardOK("confirmDialog", "Error","The criteria you are adding already exists for this variable.")();
            }
            else {
                let dataList = {mode:"addContextVariableMatchingCriteria",variableName: getStatusContextVar(), criteria : criteria, override:override};
                generalAjaxQuery('KnowledgeAcquisitionServlet',processAddMatchingCriteriaCallback,dataList);  
            }
        }
    }
    
    function processAddMatchingCriteriaCallback(response) {
        dataList = {mode:"getContextVariablesForModification", username:getSelectedUserName()};
        populateSelector("currentContextVariables",dataList,processAddMatchingCriteriaCallback2);          
        //getContextVariablesForModification("currentContextVariables",processAddMatchingCriteriaCallback2,getSelectedUserName());        
    }
    
    function processAddMatchingCriteriaCallback2() {
        if ($.trim($("#newVariableName").val()) !== "")
            updateSelector("currentContextVariables","@" + $("#newVariableName").val());
        else
            updateSelector("currentContextVariables",getStatusContextVar());
        $("#newVariableName").val("");
        standardOK("confirmDialog", "Notice","Criteria added, be sure to save changes..")();
        $("#metaDataDialog2").dialog("close");
        fireTriggerChange("currentContextVariables");
        
    }
    
    function processAddDetectedDictionaryTerm() {
        let current = $.trim($('#currentMatchingCriteria').val());
        if ($('#contextRecentInputDictionary').val() === "") 
            standardOK("confirmDialog", "Error","No detected dictionary term has been highlighted!")();
        else {
            if (current !== "")
                current = current + " " + $('#contextRecentInputDictionary').val();
            else
                current = $('#contextRecentInputDictionary').val();
            $('#currentMatchingCriteria').val($.trim(current));
        }
    }
    
    function processAddDictionaryTerm() {
        let current = $.trim($('#currentMatchingCriteria').val());
        if (current !== "")
            current = current + " " + getSelectorValue('currentDictionaryTerms');
        else
            current = getSelectorValue('currentDictionaryTerms');
        $('#currentMatchingCriteria').val($.trim(current));
    }
    
    function processDetectDictionaryTerm() {
        // get highlighted text first..
        dconsole("Mouse selection detected..");
        let highlightedText = window.getSelection().toString();
        if (highlightedText !== "") {
            let dataList = {mode:"findMatchingDictionaryTerm",highlightedText : highlightedText};
            generalAjaxQuery('KnowledgeAcquisitionServlet',processDetectDictionaryTermCallback,dataList,"contextRecentInputDictionary");  
        }   
    }
    
    function processDetectDictionaryTermCallback(response, destinationInput) {
        let dicTerm = response['result'];
        $("#" + destinationInput).val(dicTerm);
        $("#" + destinationInput).effect("highlight", {}, 3000);
        
    }
    
    function processNewVariableCreateButton() {
        let newVarName = $.trim($("#" + "newVariableName").val()).replace(/ /g,'');
        
        if (newVarName === "") {
            standardOK("confirmDialog", "Error","Please enter a new variable name to create!")();
        }
        else if (selectorContainsOption("currentContextVariables","@" + newVarName)) {
            dconsole("A match was found!");
            standardOK("confirmDialog", "Error","The new variable name already exists!")();
        }
        else {
            contextVariableMatchingCriteriaContent('metaDataDialog2',"@" + newVarName);
            standardProcessDialog('metaDataDialog2', "Variable matching criteria",processNewVariableCreateButtonCallback,null,550)();        
        }
    }
    
    function processVariableEditButton() {
        let variableName = getSelectorValue("currentContextVariables");
        setStatusContextVar(variableName);
        
        contextVariableMatchingCriteriaContent('metaDataDialog2',variableName);
        standardProcessDialog('metaDataDialog2', "Variable matching criteria",processVariableEditButtonCallback,null,550)();        
    }
    
    function processVariableActionButton() {
        let variableName = getSelectorValue("currentContextVariables");
        setStatusContextVar(variableName);
        
        contextVariableActionContent('metaDataDialog2',variableName);
        standardProcessDialog('metaDataDialog2', "Variable Action maintenance",processVariableActionButtonCallback,null,600)();        
    }
    
    // this is called after the dialog is closed..
    function processVariableEditButtonCallback() {    
        
    }
    
    // this is called after the dialog is closed..
    function processVariableActionButtonCallback() {
        dataList = {mode:"getExistingContextVariableActions", variableName:getStatusContextVar()};
        populateSelector("currentContextVariablesActions",dataList,noopCallback);
        standardOK("confirmDialog", "Notice","Don't forget to save any changes made before closing the User Context Variables dialog (Save all changes)")();
    }
    
    // this is called after the dialog is closed..
    function processNewVariableCreateButtonCallback() {
        
    }
    
    function processExistingCriteriaDeleteButton() {
        dconsole("Trying to delete " + getSelectorValue("existingContextCriteria"));
        let dataList = {mode:"deleteContextVariableCriteria",variableName: getStatusContextVar(), criteria: getSelectorValue("existingContextCriteria")};
        generalAjaxQuery('KnowledgeAcquisitionServlet',processExistingCriteriaDeleteButtonCallback,dataList);   
    }
    
    function processExistingCriteriaDeleteButtonCallback() {
        let dataList = {mode:"getContextVariableCriteria",variableName: getStatusContextVar(),username: getSelectedUserName()};
        populateSelector("existingContextCriteria",dataList,noopCallback); 
    }
    
    /*************************************************************************************************/
    /**************** Main context variable editing interface ****************************************/
    /*************************************************************************************************/
       
    function contextVariableMatchingCriteriaContent(parentDiv, variableName) {
        setStatusContextVar(variableName);
        
        let myContentDiv = "contextVariableMatchingCriteriaContentDiv";
        
        $("#" + parentDiv).html("");
        $("#" + parentDiv).append("<div id='" + myContentDiv + "'></div>");
        $("#" + myContentDiv).append("<div class='tableHeader'>Variable:" + variableName + "</div>");      
        $("#" + myContentDiv).append("<div class='tableHeader'>Add or modify matching criteria</div>");  
        $("#" + myContentDiv).append("<div class='boxed' id='contextDictionaryMatchDiv'></div>");
        $("#" + "contextDictionaryMatchDiv").append("<div class='fullblockWrapperSlim'><span class='leftSpan top15Margin'>Selected recent input (highlight sub-terms to find dictionary matches):</span></div>");
        $("#" + "contextDictionaryMatchDiv").append("<div class='fullblockWrapperSlim'><textarea id='contextRecentInput'></textarea></div>");
        //$("#" + myContentDiv).append("<div class='fullblockWrapperSlim'><input id='contextDetectDictionaryTerm' type='button' value='detect dict term' class='inlineButtonSmallFloatLeft hbutton'></div>");
        let dialogSelected = getStatusSelectedDialogIdKA();
        
        // see if the most recent (or selected) user dialog is useful..
        dconsole("dialogSelected:" + dialogSelected);
        let dataList = {mode:"getUserDialogFromSystemResponse",username: getSelectedUserName(), dialogID: dialogSelected};
        generalAjaxQuery('KnowledgeAcquisitionServlet',contextVariableMatchingCriteriaContentCallback,dataList,myContentDiv,variableName);  

    }
    
    function contextVariableMatchingCriteriaContentCallback(response,myContentDiv,newVariableName) {
        $("#contextRecentInput").val(response['result']);  // populate recent input with last (or selected) user dialog
        
        // First detected dictionary term:
        $("#" + "contextDictionaryMatchDiv").append("<div class='fullblockWrapperSlim'><span class='leftSpan top15Margin'>First detected dictionary term:</span><input id='contextRecentInputDictionary' type='text' class='textInputLeft' readonly placeholder='Nothing..' title='Any user-highlighted dictionary terms found above will be detected here. If a conversation dialog was selected prior to trying to modify user variables, the dialog will appear above. Alternatively you can type any text above to determine if there are detected dictionary terms in the input. Highlight any of the text with the mouse and detection will occur.'><input id='contextAddDetectedDictionaryTerm' type='button' value='Add detected term' class='inlineButtonSmallFloatRight hbutton'></div>");
        
        // All dict terms
        $("#" + myContentDiv).append("<div class='fullblockWrapperSlim'><span class='leftSpan'>All dict terms</span><select id='currentDictionaryTerms' class='KAinputLeft'/><input id='contextAddDictionaryTerm' type='button' value='Add term' class='inlineButtonSmallFloatRight hbutton'></div>");

        let dataList = {mode:"getRepresentativeTermList"};
        populateSelector("currentDictionaryTerms",dataList,contextVariableMatchingCriteriaContentCallback2,myContentDiv);   
  
    }
    
    function contextVariableMatchingCriteriaContentCallback2(myContentDiv) {
               
        // Existing criteria
        $("#" + myContentDiv).append("<div class='fullblockWrapperSlim'><span class='leftSpan'>Existing Criteria</span><select id='existingContextCriteria' class='KAinputLeft'/><input id='existingCriteriaDeleteButton' type='button' value='Delete criteria' class='inlineButtonSmallFloatRight hbutton'></div>");
        
        // New matching criteria
        $("#" + myContentDiv).append("<div class='fullblockWrapperSlim'><span class='leftSpan top15Margin'>New matching criteria:</span><input id='currentMatchingCriteria' class='KAinputRight' title='When this matching criteria is detected in user input, the variable value is set.  Actual text, such as dictionary synonyms associated with a matching dictionary term, are used as the variable value.  Generally one dictionary term should be used as criteria'/></div>");
       
        // Manual (value) override:
        $("#" + myContentDiv).append("<div class='fullblockWrapperSlim'><span class='leftSpan top15Margin'>Manual (value) override:</span><input id='currentMatchingCriteriaOverride' class='KAinputRight' placeholder='Optional override value' title='When the criteria for this variable matches user input, then the override value will be used for the variable value instead of the actual matching user input'></div>");        
        
        $("#" + myContentDiv).append("<div class='fullblockWrapperSlim'><input id='addMatchingCriteria' type='button' value='save criteria' class='inlineButtonSmallFloatRight hbutton'></div>");
        
        let dataList = {mode:"getContextVariableCriteria",variableName: getStatusContextVar(),username: getSelectedUserName()};
        populateSelector("existingContextCriteria",dataList,contextVariableMatchingCriteriaContentCallback3,myContentDiv); 
        
        
    }
    
    function contextVariableMatchingCriteriaContentCallback3(myContentDiv) {
        let dataList = {mode:"getContextVariableOverride",variableName: getStatusContextVar()};               
        populateField("currentMatchingCriteriaOverride",dataList,contextVariableMatchingCriteriaContentCallback4,myContentDiv);
    }
    
    function contextVariableMatchingCriteriaContentCallback4(myContentDiv) {
        dconsole("End of the line!");
    }
    
           
    function contextVariableActionContent(parentDiv, variableName) {
        setStatusContextVar(variableName);
        
        let myContentDiv = "contextVariableActionContentDiv";
        
        $("#" + parentDiv).html("");
        $("#" + parentDiv).append("<div id='" + myContentDiv + "'></div>");
        $("#" + myContentDiv).append("<div class='tableHeader'>Delete actions from defining variable:" + variableName + "</div>");   
        $("#" + myContentDiv).append("<div class='boxed' id='deleteActionContextVariableDiv'></div>");  
        $("#" + "deleteActionContextVariableDiv").append("<div class='fullblockWrapperSlim'><span class='leftSpan'>Existing actions:</span><select id='existingActionsContextVariable' class='KAinputLeft'/><input id='deleteVariableActionButton' type='button' value='Delete action' class='inlineButtonSmallFloatRight hbutton '></div>");   
        

        //$("#" + myContentDiv).append("<div class='columnHeading'>Add or modify Actions</div>"); 
        
        //$("#" + myContentDiv).append("<div class='boxed' id='actionSourceContextVariableDiv'></div>");  
        //$("#" + "actionSourceContextVariableDiv").append("<div class='tableHeader'>1 - Specify the variable that will define the action</div>");
        //$("#" + "actionSourceContextVariableDiv").append("<div class='fullblockWrapperSlim'><span class='leftSpan'>Action Variable:</span><select id='actionSourceContextVariable' class='KAinputLeft'/></div>");   
        
        $("#" + myContentDiv).append("<div class='boxed' id='actionTargetContextVariableDiv'></div>");
        $("#" + "actionTargetContextVariableDiv").append("<div class='tableHeader'>Add actions to defining variable:" + variableName + "</div></div>");
        $("#" + "actionTargetContextVariableDiv").append("<div class='tableHeader'>1 - Set the target variable that will receive a value when the action is triggered</div>");  
        $("#" + "actionTargetContextVariableDiv").append("<div class='fullblockWrapperSlim'><span class='leftSpan'>Target variable:</span><select id='actionTargetContextVariable' class='KAinputLeft'/></div>");   
        
        $("#" + myContentDiv).append("<div class='boxed' id='actionSourceTriggerContextVariableDiv'></div>");  
        $("#" + "actionSourceTriggerContextVariableDiv").append("<div class='tableHeader'>2 - Set the condition for the action to be triggered</div>");
        $("#" + "actionSourceTriggerContextVariableDiv").append("<div class='fullblockWrapperSuperSlim'><input type='radio' name='actionTrigger' value='1' class='actionRadio' checked><span class='leftSpanRadio'>Run immediately when the Action Variable has any value</span></div>");   
        $("#" + "actionSourceTriggerContextVariableDiv").append("<div class='fullblockWrapperSuperSlim'><input type='radio' name='actionTrigger' value='2' class='actionRadio'><span class='leftSpanRadio'>Run when the Action Variable contains a specified value defined by another variable</span></div>");   
        $("#" + "actionSourceTriggerContextVariableDiv").append("<div class='fullblockWrapperSlim'><span class='leftSpan'>Specified value:</span><select id='actionTriggerContainsContextVariable' class='KAinputLeft' disabled/></div>");   

        $("#" + myContentDiv).append("<div class='boxed' id='actionTargetValueDiv'></div>");
        $("#" + "actionTargetValueDiv").append("<div class='tableHeader'>3 - Set the value to use for the target variable when the action is triggered</div>");
        $("#" + "actionTargetValueDiv").append("<div class='fullblockWrapperSuperSlim'><input type='radio' name='actionValue' value='1' class='actionRadio' checked><span class='leftSpanRadio'>Value from another variable:</span><select id='actionTargetValueContextVariable' class='KAinputLeft'/></div>");   
        $("#" + "actionTargetValueDiv").append("<div class='fullblockWrapperSuperSlim'><input type='radio' name='actionValue' value='2' class='actionRadio'><span class='leftSpanRadio'>Literal value:</span><input id='actionTargetValueLiteral' class='KAinputLeft'type='text' disabled/></div>");   

        $("#" + myContentDiv).append("<div class='fullblockWrapperSlim'><input id='addVariableAction' type='button' value='Add action' class='inlineButtonSmallFloatRight hbutton'></div>");
        
        //let dataList = {mode:"getContextVariablesForModification", username:getSelectedUserName()};
        //populateSelector("actionSourceContextVariable",dataList,contextVariableActionContentCallback,myContentDiv); 
        
        let dataList = {mode:"getContextVariablesForModification", username:getSelectedUserName()};
        populateSelector("actionTargetContextVariable",dataList,contextVariableActionContentCallback,parentDiv); 
    }
    
    function contextVariableActionContentCallback(parentDiv) {
        let dataList = {mode:"getContextVariablesForModification", username:getSelectedUserName()};
        populateSelector("actionTargetValueContextVariable",dataList,contextVariableActionContentCallback2,parentDiv); 
    }
    
    
    
    function contextVariableActionContentCallback2(parentDiv) {
        let dataList = {mode:"getContextVariablesForModification", username:getSelectedUserName()};
        populateSelector("actionTriggerContainsContextVariable",dataList,contextVariableActionContentCallback3,parentDiv);
    }
    
    function contextVariableActionContentCallback3(parentDiv) {
        let dataList = {mode:"getExistingContextVariableActions", variableName:getStatusContextVar()};
        populateSelector("existingActionsContextVariable",dataList,contextVariableActionContentCallback4,parentDiv); 
    }
    
    function contextVariableActionContentCallback4(parentDiv) {
        updateButtonCoupledToSelector("deleteVariableActionButton","existingActionsContextVariable"); 
    }

    
    function processAddVariableAction() {        //let definingVariable = getSelectorValue("actionSourceContextVariable");
        let definingVariable = getStatusContextVar();
        let triggerCondition = "TRUE";  // default to radio 1 selected..
        let targetVariable = getSelectorValue("actionTargetContextVariable");
        let targetValue = getSelectorValue("actionTargetValueContextVariable"); // default to radio 1 selected..
        
        let triggerConditionRadioSelected = $("input[name='actionTrigger']:checked").val();
        let targetValueRadioSelected = $("input[name='actionValue']:checked").val();
                
        if (triggerConditionRadioSelected === "2") {
            triggerCondition = getSelectorValue("actionTriggerContainsContextVariable");
        }
        
        if (targetValueRadioSelected === "2") {
           targetValue = $("#actionTargetValueLiteral").val();
        }

        if (triggerCondition === definingVariable) {
            standardOK("confirmDialog", "Error","You cannot set the trigger condition variable to be the same as the defining variable!")();
        }
        else if (targetVariable === definingVariable) {
            standardOK("confirmDialog", "Error","You cannot set the target variable to be the same as the defining variable!")();
        }
        else {
            dconsole("Trying to add: " +  "Target:" +targetVariable + " Trigger:" + triggerCondition + " Value:"  + targetValue );
            if (addActionIntegrityCheck(targetVariable,triggerCondition)) {
                if (!selectorContainsOption("existingActionsContextVariable","Target:" +targetVariable + " Trigger:" + triggerCondition + " Value:"  + targetValue)) {
                    let dataList = {mode:"addContextVariableAction", definingVariable:definingVariable,triggerCondition:triggerCondition,targetVariable:targetVariable,targetValue:targetValue};
                    generalAjaxQuery('KnowledgeAcquisitionServlet',processAddVariableActionCallback,dataList);  
                }
                else {
                    standardOK("confirmDialog", "Error","This action already exists for the variable " + definingVariable + "!")();
                }
            }
            else {
                standardOK("confirmDialog", "Error","You are trying to add an action to a target that is already included in an existing action, but the same trigger condition already exists, or at least one trigger is TRUE!")();

            }
        }

    }
    
    function addActionIntegrityCheck(target,trigger) {
        let response = true;
        
        if (getSelectorLength("existingActionsContextVariable") > 0) {
            $("#existingActionsContextVariable option").each(function(index, theOption) {
                if (theOption.value.indexOf("Target:" + target) !== -1 ) {
                    // we are trying to add to the same target..
                    if (theOption.value.indexOf("Trigger:TRUE") !== -1 || trigger === "TRUE" || theOption.value.indexOf("Trigger:" + trigger) !== -1) {
                        response = false;
                    }
                }
            });      
        }
        return response;
    }
    
    function processAddVariableActionCallback(response) {
        standardOK("confirmDialog", "Notice","The action was successfully added!")(); 
        dataList = {mode:"getExistingContextVariableActions", variableName:getStatusContextVar()};
        populateSelector("existingActionsContextVariable",dataList,processAddVariableActionCallback2);            
    }
    
    function processAddVariableActionCallback2(response) {
        updateButtonCoupledToSelector("deleteVariableActionButton","existingActionsContextVariable");      
    }
    
    
    function processDeleteVariableActionButton() {
        let variableName = getStatusContextVar();
        let actionToDelete = getSelectorValue("existingActionsContextVariable");
        
        if (actionToDelete !== "" && actionToDelete !== null) {
        
            let targetVariable = actionToDelete.substring(7,actionToDelete.indexOf("Trigger:")-1);
            let trigger = actionToDelete.substring(actionToDelete.indexOf("Trigger:")+8,actionToDelete.indexOf("Value:")-1);
            let value = actionToDelete.substring(actionToDelete.indexOf("Value:")+6);
            let dataList = {mode:"deleteContextVariableAction", definingVariable:variableName,triggerCondition:trigger,targetVariable:targetVariable,targetVariableValue:value};
            generalAjaxQuery('KnowledgeAcquisitionServlet',processDeleteVariableActionButtonCallback,dataList); 
        }
        else {
            standardOK("confirmDialog", "Error","You cannot delete an empty action!")(); 

        }

    }
    
    function processDeleteVariableActionButtonCallback(response) {
        standardOK("confirmDialog", "Notice","The action was successfully deleted!")(); 
        dataList = {mode:"getExistingContextVariableActions", variableName:getStatusContextVar()};
        populateSelector("existingActionsContextVariable",dataList,processDeleteVariableActionButtonCallback2);     
    }
    
    function processDeleteVariableActionButtonCallback2(response) {
        updateButtonCoupledToSelector("deleteVariableActionButton","existingActionsContextVariable"); 
    }
    
    


    