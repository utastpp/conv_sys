    var systemContextVariablesMain = "KAsystemContextVariablesWrapper";
    var changesNotSaved = false;
    var willClose = true;
    
    function setChangesNotSaved() {
        changesNotSaved = true;
    }
    
    function setChangesSaved() {
        changesNotSaved = false;
    }
    
    function getChangesNotSaved() {
        return changesNotSaved;
    }
    
    function getSystemContextVariablesMain() {
        return systemContextVariablesMain;
    }
    
    var systemContextVariablesEventListenersRegistered = false;
    
    function systemContextVariablesDocumentReady(parent,sibling) {
        createSystemContextVariablesInterface(parent,sibling);
    }
    
     function systemContextVariablesAddEventListeners() {
        if (!systemContextVariablesEventListenersRegistered) {
            $('body').on('click','#newSystemVariableCreateButton',processSystemVariableCreateButton);  
            //$('body').on('click','#saveContextVariableChangesButton',processSaveSystemContextVariableChangesButton);  
            $('body').on('click','#newSystemVariableUpdateValueChangeButton',processNewSystemVariableUpdateValueChangeButton); 
            $('body').on('click','#variableAddOverridesButton',processVariableAddOverridesButton);  
            $('body').on('click','#newSystemVariableOverrideRuleLocationButton',processNewSystemVariableOverrideRuleLocationButton);  
            $('body').on('click','#newSystemVariableDeleteOverrideButton',processNewSystemVariableDeleteOverrideButton);  

            
            $('body').on('change','#currentSystemContextVariables', function(e) {         
                var contextVar = $("#currentSystemContextVariables").val();
                let dataList = {mode:"getSystemContextVariableValue",variableName: contextVar};
                populateField("currentSystemContextVariableValue",dataList,noopCallback);  
                dataList = {mode:"getSystemContextVariableOverrides",variableName: contextVar};               
                populateSelector("currentSystemContextVariableOverrides",dataList,currentOverrideCallback); 
            });
            
            $('body').on('click','#selectSystemVariableOverrideDiv' ,function(e) {
                if (e.target.id !== "selectSystemVariableOverrideDiv") {    
                    //console.log("div click: " + e.target.id);
                    let id = e.target.id.substr(4);
                    $("#newSystemVariableSelectedRule").val(id);
                }   
            });
            
            systemContextVariablesEventListenersRegistered = true;
        }
    }
    
    function currentOverrideCallback() {
        if (getSelectorValue("currentSystemContextVariableOverrides") === null)
           disableItem("newSystemVariableDeleteOverrideButton"); 
        else
            enableItem("newSystemVariableDeleteOverrideButton"); 
    }
    
    function processSystemVariableCreateButton() {
        let newVarName = $.trim($("#" + "newSystemVariableName").val()).replace(/ /g,'');
        let newVarValue = $.trim($("#" + "newSystemVariableValue").val());
        
        if (newVarName === "") {
            standardOK("confirmDialog", "Error","Please enter a new variable name to create!")();
        }
        else if (selectorContainsOption("currentSystemContextVariables","@SYSTEM" + newVarName)) {
            dconsole("A match was found!");
            standardOK("confirmDialog", "Error","The new variable name already exists!")();
        }
        else if (newVarValue ==="") {
            standardOK("confirmDialog", "Error","You must provide a value for the new variable!")();
        }
        else {
            let dataList = {mode:"addNewSystemContextVariable",variableName: newVarName, variableValue: newVarValue};
            generalAjaxQuery('KnowledgeAcquisitionServlet',processSystemVariableCreateButtonCallback,dataList);  
        }
    }
    
    function processSystemVariableCreateButtonCallback() {
        let dataList = {mode:"getSystemContextVariablesForModification"};
        populateSelector("currentSystemContextVariables",dataList,processSystemVariableCreateButtonCallback2);  
    }
    
    function processSystemVariableCreateButtonCallback2() {
        if ($.trim($("#newSystemVariableName").val()) !== "")
            updateSelector("currentSystemContextVariables","@SYSTEM" + $("#newSystemVariableName").val().replace(/ /g,''));
        fireTriggerChange("currentSystemContextVariables");

        $("#newSystemVariableName").val("");
        $("#newSystemVariableValue").val("");
        standardOK("confirmDialog", "Notice","New variable successfully created")();
        setChangesNotSaved();
    }
    
    function processSaveSystemContextVariableChangesButton(parentDiv) {
        let dataList = {mode:"saveContextVariableChanges"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',processSaveSystemContextVariableChangesButtonCallback,dataList,parentDiv);  
    }
    
    function processSaveSystemContextVariableChangesButtonCallback(response,parentDiv) {
        setStatusDialogOffsetDecrement();
         $('#' + parentDiv).dialog('close'); 
        standardOK("confirmDialog", "Notice","All changes successfully saved!")();
        setChangesSaved();
             
    }
    
    function processNewSystemVariableUpdateValueChangeButton() {
        let variableName = getSelectorValue("currentSystemContextVariables");
        let variableValue = $.trim($("#currentSystemContextVariableValue").val());
        if (variableValue !== "") {
            let dataList = {mode:"addNewSystemContextVariable",variableName:variableName,variableValue:variableValue};
            generalAjaxQuery('KnowledgeAcquisitionServlet',processNewSystemVariableUpdateValueChangeButtonCallback,dataList);  
        }
        else {
            standardOK("confirmDialog", "Error","You can't update the value to be empty!")();

        }
    }
    
    function processNewSystemVariableUpdateValueChangeButtonCallback() {
        standardOK("confirmDialog", "Notice","System variable value changed!")();
        setChangesNotSaved();
    }
    
    function processVariableAddOverridesButton() {
        let variableName = getSelectorValue("currentSystemContextVariables");
        createSystemContextVariablesOverridesInterface("metaDataDialog2",variableName);
        let buttonList = [ 
            {                
                id: "systemContextOverride-button-cancel",
                text: "Cancel",
                click: function () {
                    $(this).dialog('close');
                    setStatusDialogOffsetDecrement();
                }
                
            },                  
            {
                id: "systemContextOverride-button-update",
                text: "Update",
                click: function() {
                    systemContextOverrideUpdateButtonHander("metaDataDialog2",variableName);
                }
            }     
        ];
        standardProcessDialog('metaDataDialog2', "System Context Variable maintenance",systemContextVariablesMainDialogCloseCallback,buttonList,550)();       
    }
    
    function processNewSystemVariableOverrideRuleLocationButton() {
        let dataList = {mode:"getRulebaseJSON"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',processNewSystemVariableOverrideRuleLocationButtonCallback,dataList,"metaDataDialog3");   
    }
    
    function processNewSystemVariableOverrideRuleLocationButtonCallback(response,parentDiv) {
        dconsole("parentDiv is " + parentDiv);
        let myMainDiv = 'selectSystemVariableOverrideDiv';
        //$("#" + myMainDiv).html(""); 
        
        $("#" + parentDiv).html("");           
        $("#" + parentDiv).prepend("<div id='" + myMainDiv + "' class='column'></div>");
        
        $("#" + myMainDiv).append(response['result']);

        $("#" + parentDiv).append("<div class='fullColumn'><span class='leftSpan' style='margin-top:15px;'>Selected Rule:</span><input id='newSystemVariableSelectedRule' type='text' class='textInput' readonly></div>");
        let buttonList = [
            {
            id: "systemContextOverride-button-cancel",
                text: "Select",
                click: function () {
                    $("#currentSystemContextVariableRuleValue").val($("#newSystemVariableSelectedRule").val());
                    $(this).dialog('close');
                    setStatusDialogOffsetDecrement();
                }
            }
        ];
        standardProcessDialog(parentDiv, "Choose rule number for variable override",processNewSystemVariableOverrideRuleLocationButtonCallback2,buttonList,550)();         
    }
    
    function processNewSystemVariableOverrideRuleLocationButtonCallback2() {
        
    }
    
    function processNewSystemVariableDeleteOverrideButton() {
        let currentOverride = getSelectorValue("currentSystemContextVariableOverrides");
        if (currentOverride !== null) {
            dconsole("Override value is: " + currentOverride);
            let ruleNumber = currentOverride.substr(5,currentOverride.indexOf(',')-5);
            dconsole("Rule number is:" + ruleNumber);
            let variableName = getSelectorValue("currentSystemContextVariables");
            let dataList = {mode:"deleteSystemContextVariableOverride",variableName:variableName,ruleNumber:ruleNumber};
            generalAjaxQuery('KnowledgeAcquisitionServlet',processNewSystemVariableDeleteOverrideButtonCallback,dataList);   
        }
        else {
             standardOK("confirmDialog","Error","There is no override to delete!")();
        }
    }
    
    function processNewSystemVariableDeleteOverrideButtonCallback() {
        standardOK("confirmDialog","Notice","The system context variable override has been deleted.")();
        fireTriggerChange("currentSystemContextVariables");
        setChangesNotSaved();
    }
    
    /* ******************* System Context Variables  *************************** */
    // Main div for adding/modifying system context variables
    /* **************************************************************** */ 
    function createSystemContextVariablesInterface(parentDiv,sibling) { 
        
        let mainWrapper = getSystemContextVariablesMain();
        
        if ($("#" + getSystemContextVariablesMain()).length)
            $("#" + getSystemContextVariablesMain()).remove();
        
        $("#" + parentDiv).html("");           
        $("#" + parentDiv).prepend("<div id='" + mainWrapper + "' class='column'></div>");
        
        let dataList = {mode:"populateContextVariables"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',createSystemContextVariablesInterfaceCallback,dataList);  
    }
    
    function createSystemContextVariablesInterfaceCallback() {
        let mainWrapper = getSystemContextVariablesMain();
        $("#" + mainWrapper).append("<div id='newSystemContextVariableDiv' class='fullColumn'></div>");
        newSystemContextVariableContent('newSystemContextVariableDiv');
        
        $("#" + mainWrapper).append("<div id='currentSystemContextVariableDiv' class='fullColumn'></div>");
        currentSystemContextVariablesContent('currentSystemContextVariableDiv');
        
        $("#" + mainWrapper).append("<div class='fullColumn'><input id='variableAddOverridesButton' type='button' value='Add Override..' class='inlineButtonSmallFloatRight hbutton '></div>");
        //$("#" + mainWrapper).append("<div class='fullColumn'><input id='saveContextVariableChangesButton' type='button' value='Save all changes' class='inlineButtonSmallFloatRight hbutton '></div>");
        
        systemContextVariablesAddEventListeners();
    }
    
    function newSystemContextVariableContent(parentDiv) {
        let myMainDiv = 'newSystemContextVariableDivColumn';
        $("#" + parentDiv).html("");
        $("#" + parentDiv).append("<div class='tableHeader'>Create a new system context variable</div>");
        $("#" + parentDiv).append("<div id='" + myMainDiv + "'> </div>");     
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpan' style='margin-top:15px;'>@SYSTEM</span><input id='newSystemVariableName' type='text' placeholder='New variable name...' class='textInput'><input id='newSystemVariableValue' type='text' placeholder='New variable value...' class='textInput'></div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><input id='newSystemVariableCreateButton' type='button' value='Create...' class='inlineButtonSmallFloatRight hbutton '></div>");
    }
    
    function currentSystemContextVariablesContent(parentDiv) {
        let myMainDiv = 'currentSystemContextVariableDivColumn';
        $("#" + parentDiv).html("");
        $("#" + parentDiv).append("<div class='tableHeader'>Current system context variables</div>");
        $("#" + parentDiv).append("<div id='" + myMainDiv + "'> </div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpanFixedWidth'>Variable</span><select id='currentSystemContextVariables' class='KAinputLeft'/></div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpanFixedWidth'>Value</span><input id='currentSystemContextVariableValue' type='text' class='textInput'><input id='newSystemVariableUpdateValueChangeButton' type='button' value='Update value' class='inlineButtonSmallFloatRight hbutton '></div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpanFixedWidth'>Overrides</span><select id='currentSystemContextVariableOverrides' class='KAinputLeft'/><input id='newSystemVariableDeleteOverrideButton' type='button' value='Delete override' class='inlineButtonSmallFloatRight hbutton '></div>");

        let dataList = {mode:"getSystemContextVariablesForModification"};
        populateSelector("currentSystemContextVariables",dataList,currentSystemContextVariablesContentCallback,myMainDiv); 
    }
    
    // need to display value and fetch overrides for the currently selected system variable..
    function currentSystemContextVariablesContentCallback(parentDiv) { 
        fireTriggerChange("currentSystemContextVariables");
    }
    
    
    function systemContextVariablesMainDialogCloseCallback() {
        
        updateMetaDataInterface(null);
        
        if (getChangesNotSaved()) {
            standardDialog("confirmDialog",
                    "Confirm discard changes",
                    "You have not saved all system variable changes - are you sure you want to leave?",
                    discardChanges(), // YES
                    abortClose() //NO
            );
            dconsole("Returning wilClose:" + willClose);
            return willClose;
        }
    }
    
    function discardChanges(parentDiv) {
        return function() {
            standardOK("confirmDialog","Discard system context variable changes","All changes discarded")();
            $('#' + parentDiv).dialog('close');
            setStatusDialogOffsetDecrement();
            updateMetaDataInterface(null);
        };
    }
    
    function abortClose() {
        return function() {
            standardOK("confirmDialog","Close cancelled","You can now save all of your changes..")();
        };
    }
    
    
    function systemContextCancelButtonHander(parentDiv) {
        dconsole("Cancel called - parentDiv is " + parentDiv);
        if (getChangesNotSaved()) {
            standardDialog("confirmDialog",
                    "Confirm discard changes",
                    "You have not saved all system variable changes - are you sure you want to leave?",
                    discardChanges(parentDiv), // YES
                    abortClose //NO
            );  
        }
        else {
            setStatusDialogOffsetDecrement();
            $('#' + parentDiv).dialog('close');
        }
    }
    
    function systemContextSaveAllButtonHander(parentDiv) {
        dconsole("save all called!");
        processSaveSystemContextVariableChangesButton(parentDiv);
    }
    

    function createSystemContextVariablesOverridesInterface(parentDiv,variableName) {
        let myMainDiv = 'newSystemContextVariableOverrideDivColumn';
        $("#" + parentDiv).html("");
        $("#" + parentDiv).append("<div class='tableHeader'>Add System Context Variable Override for variable: " + variableName + "</div>");
        $("#" + parentDiv).append("<div id='" + myMainDiv + "'> </div>");     
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpanFixedWidth'>Rule Num</span><input id='currentSystemContextVariableRuleValue' type='text' class='textInput' readonly><input id='newSystemVariableOverrideRuleLocationButton' type='button' value='Select Rule...' class='inlineButtonSmallFloatLeft hbutton '></div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpanFixedWidth'>Override</span><input id='currentSystemContextVariableOverrideValue' type='text' class='textInput'></div>");

    }
    
    function systemContextOverrideUpdateButtonHander(parentDiv, variableName) {
        let variableValue = $.trim($("#currentSystemContextVariableOverrideValue").val());
        let ruleNum = $.trim($("#currentSystemContextVariableRuleValue").val());
        
        dconsole("Rule num:" + ruleNum + " and override:" + variableValue);
        if (variableValue !== "" && ruleNum !== "") {
            $("#" + parentDiv).dialog('close');
            setStatusDialogOffsetDecrement();
            let dataList = {mode:"addSystemContextVariableOverride",variableName:variableName,ruleNumber:ruleNum,override:variableValue};
            generalAjaxQuery('KnowledgeAcquisitionServlet',systemContextOverrideUpdateButtonHanderCallback,dataList);  
        }
        else {
            standardOK("confirmDialog","Error","The override value and/or rule number can't be empty")();
        }
    }
    
    function systemContextOverrideUpdateButtonHanderCallback() {
        standardOK("confirmDialog","Notice","The override value has been added!")();
        fireTriggerChange("currentSystemContextVariables");
        setChangesNotSaved();
    }