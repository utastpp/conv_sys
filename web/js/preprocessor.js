    var preprocessorMain = "preprocessorWrapper";
    var preprocessorModifyActionID = -1;
    
    
    function getPreprocessorMain() {
        return preprocessorMain;
    }
    
    var preprocessorEventListenersRegistered = false;
    
    function preprocessorDocumentReady(parentDiv) {
        dconsole("preprocessorDocumentReady");
        createPreprocessorMainInterface(parentDiv);
    }
    
     function preprocessorAddEventListeners() {
        if (!preprocessorEventListenersRegistered) {
            
            $('body').on('mouseup','#dialogHistoryPreprocessorDiv',processDetectPreprocessorTerm);
            $('body').on('click','#preprocessorPreviewButton',processPreprocessorPreviewButton);
            $('body').on('click',"#preprocessorAddButton",function(e) {
                processPreprocessorAddButton("actionsPreprocessorDiv");
            });
            
            $('body').on('click','#preprocessorMoveUpButton',processPreprocessorMoveUpButton);
            $('body').on('click','#preprocessorMoveDownButton',processPreprocessorMoveDownButton);
            $('body').on('click','#preprocessorDeleteButton',processPreprocessorDeleteButton);
            $('body').on('click','#preprocessorModifyButton',processPreprocessorModifyButton);
            $('body').on('click','#preprocessorSaveButton',processPreprocessorSaveButton);

            // selecting an action (single selection only)
            $('body').on('click','.preprocessorAction', function(e) {       
                let theID = 0;      
                theID = e.target.id.toString().substring(9); 
                unhighlightItems("queryTableItemSelected");
                highlightItem(e.target.id.toString(),"queryTableItemSelected");
                dconsole("Selected item:" + theID);
                preprocessorModifyActionID = theID;
                processGetPreprocessorActionDetails(preprocessorModifyActionID);
                
            });
            
            
            preprocessorEventListenersRegistered = true;
        }
    }
    
    function processPreprocessorModifyButton() {
        if (preprocessorModifyActionID !== -1) {
            standardDialog("confirmDialog",
                    "Confirm Action modify",
                    "Are you sure you want to modify the selected Action?",
                    confirmedPreprocessorModify, // YES
                    standardOK("confirmDialog2","Notice","Action not modifed..") //NO
            ); 
        }
        else {
            standardOK("confirmDialog","Error","Please select an action from the Actions list to modify!")();
        }
    }
    
    function confirmedPreprocessorModify() {
        let replaceText = $.trim($("#preprocessorReplace").val());
        let matchText = $.trim($("#preprocessorMatch").val());
        let replaceCheckbox = $("#preprocessorReplaceCheckbox").is(":checked");
        let regexCheckbox = $("#preprocessorRegexCheckbox").is(":checked");
        let wordMatchCheckbox = $("#preprocessorWordMatchCheckbox").is(":checked");
        let startCheckbox = $("#preprocessorStartCheckbox").is(":checked");
        let endCheckbox = $("#preprocessorEndCheckbox").is(":checked");
        let uppcaseCheckbox = $("#preprocessorUppcaseCheckbox").is(":checked");
        let lowercaseCheckbox = $("#preprocessorLowercaseCheckbox").is(":checked");
        let trimCheckbox = $("#preprocessorTrimCheckbox").is(":checked");
        
        if (matchText === "") {
            standardOK("confirmDialog","Error","No match text has been specified to in the action's attributes")();   
        }
        else if (uppcaseCheckbox && lowercaseCheckbox) {
            standardOK("confirmDialog","Error","There is no point converting to upper and then lower at the same time!")();
        }
        else {
            let dataList = {mode:"modifyPreprocessorAction",matchText:matchText,replaceText:replaceText,
                                regex: regexCheckbox, wordOnly:wordMatchCheckbox,startInput: startCheckbox,
                                endInput:endCheckbox, replace:replaceCheckbox, upper:uppcaseCheckbox, lower:lowercaseCheckbox,
                                trim:trimCheckbox,actionID:preprocessorModifyActionID};
            generalAjaxQuery('KnowledgeAcquisitionServlet',confirmedPreprocessorModifyCallback,dataList);  
        }     
    }
    
    function confirmedPreprocessorModifyCallback() {
        showActionsViewer("actionsPreprocessWrapper");  
    }
    
    function processPreprocessorMoveUpButton() {
        dconsole("Move up!");
        dconsole("I have been called..");
        if (preprocessorModifyActionID !== -1) {
            let dataList = {mode:"moveUpPreprocessorAction", actionID:preprocessorModifyActionID};
            generalAjaxQuery('KnowledgeAcquisitionServlet',processPreprocessorMoveUpButtonCallback,dataList); 
        }
        else {
            standardOK("confirmDialog","Error","Please select an action from the Actions list to move!")();
        }
    }
    
    function processPreprocessorMoveUpButtonCallback(response) {
        preprocessorModifyActionID--;
        showActionsViewer("actionsPreprocessWrapper");        
    }
    
    function processPreprocessorMoveDownButton() {
        dconsole("Move down!");
        dconsole("I have been called..");
        if (preprocessorModifyActionID !== -1) {
            let dataList = {mode:"moveDownPreprocessorAction", actionID:preprocessorModifyActionID};
            generalAjaxQuery('KnowledgeAcquisitionServlet',processPreprocessorMoveDownButtonCallback,dataList); 
        }
        else {
            standardOK("confirmDialog","Error","Please select an action from the Actions list to move!")();
        }
    }
    
    function processPreprocessorMoveDownButtonCallback(response) {
        preprocessorModifyActionID++;
        showActionsViewer("actionsPreprocessWrapper");        
    }
    
    function processPreprocessorDeleteButton() {
        dconsole("Delete!");
        if (preprocessorModifyActionID !== -1) {
            standardDialog("confirmDialog",
                    "Confirm Action delete",
                    "Are you sure you want to delete the selected Action?",
                    confirmedPreprocessorDelete, // YES
                    standardOK("confirmDialog2","Notice","Action not deleted..") //NO
            );        
        }
        else {
            standardOK("confirmDialog","Error","Please select an action from the Actions list to delete!")();       
        }
    }
    
    function confirmedPreprocessorDelete() {
        let dataList = {mode:"deletePreprocessorAction", actionID:preprocessorModifyActionID};
        generalAjaxQuery('KnowledgeAcquisitionServlet',processPreprocessorDeleteButtonCallback,dataList); 
    }
    
    function processPreprocessorDeleteButtonCallback(response) {
        preprocessorModifyActionID = -1;
        showActionsViewer("actionsPreprocessWrapper");     
    }
    
    function processPreprocessorSaveButton() {
        let dataList = {mode:"savePreprocessorActions"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',processPreprocessorSaveButtonCallback,dataList);      
    }
    
    function processPreprocessorSaveButtonCallback(response) {
        standardOK("confirmDialog","Notice","All actions saved!")();   
    }
    
    function processDetectPreprocessorTerm() {
        dconsole("Mouse selection detected..");
        let highlightedText = window.getSelection().toString();
        if (highlightedText !== "") {
            $("#preprocessorMatch").val(highlightedText);
            $("#preprocessorPreview").val(highlightedText);
            $("#preprocessorMatch").effect("highlight", {}, 3000);
            $("#preprocessorPreview").effect("highlight", {}, 3000);
        }   
    }
    
    function processGetPreprocessorActionDetails(actionID) {
        dconsole("I have been called..");
        let dataList = {mode:"getPreprocessorActionDetails", actionID:actionID};
        generalAjaxQuery('KnowledgeAcquisitionServlet',processGetPreprocessorActionDetailsCallback,dataList);  
    }
    
    function processGetPreprocessorActionDetailsCallback(response) {
        let rows = response['rows'];
        $.each(rows,function(index,rowItem) {

            switch (index) {
                case 0: 
                    $("#preprocessorMatch").val(rowItem);
                    break;
                case 1: 
                    $("#preprocessorRegexCheckbox").prop("checked", $.trim(rowItem) === "True");
                    break;
                case 2: 
                    $("#preprocessorWordMatchCheckbox").prop('checked', $.trim(rowItem) === "True");
                    break;
                case 3: 
                    $("#preprocessorStartCheckbox").prop('checked', $.trim(rowItem) === "True");
                    break;
                case 4: 
                    $("#preprocessorEndCheckbox").prop('checked', $.trim(rowItem) === "True");
                    break;
                case 5: 
                    $("#preprocessorReplace").val(rowItem);
                    break;  
                case 6: 
                    $("#preprocessorReplaceCheckbox").prop('checked', $.trim(rowItem) === "True");
                    break;   
                case 7: 
                    $("#preprocessorUppcaseCheckbox").prop('checked', $.trim(rowItem) === "True");
                    break; 
                case 8: 
                    $("#preprocessorLowercaseCheckbox").prop('checked', $.trim(rowItem) === "True");
                    break;  
                case 9: 
                    $("#preprocessorTrimCheckbox").prop('checked', $.trim(rowItem) === "True");
                    break;                
            }

        });
        
        
        /*  0 getMatchText()};
            1 getRegex()==true?"True":"False"};  
            2 getWordOnly()==true?"True":"False"};    
            3 getStartOfInput()==true?"True":"False"}; 
            4 getEndOfInput()==true?"True":"False"};
            5 getReplaceText()};             
            6 getReplaceOption()==true?"True":"False"}; 
            7 getUpperOption()==true?"True":"False"};    
            8 getLowerOption()==true?"True":"False"};    
            9 getTrimOption()==true?"True":"False"};
        */
    }
    
    function processPreprocessorPreviewButton() {
        let previewText = $.trim($("#preprocessorPreview").val());
        let replaceText = $.trim($("#preprocessorReplace").val());
        let matchText = $.trim($("#preprocessorMatch").val());
        let replaceCheckbox = $("#preprocessorReplaceCheckbox").is(":checked");
        let regexCheckbox = $("#preprocessorRegexCheckbox").is(":checked");
        let wordMatchCheckbox = $("#preprocessorWordMatchCheckbox").is(":checked");
        let startCheckbox = $("#preprocessorStartCheckbox").is(":checked");
        let endCheckbox = $("#preprocessorEndCheckbox").is(":checked");
        let uppcaseCheckbox = $("#preprocessorUppcaseCheckbox").is(":checked");
        let lowercaseCheckbox = $("#preprocessorLowercaseCheckbox").is(":checked");
        let trimCheckbox = $("#preprocessorTrimCheckbox").is(":checked");
        
        if (previewText === "" || matchText === "") {
            standardOK("confirmDialog","Error","No match text or preview text has been specified to preview")();   
        }
        else if (uppcaseCheckbox && lowercaseCheckbox) {
            standardOK("confirmDialog","Error","There is no point converting to upper and then lower at the same time!")();
        }
        else {     
            let dataList = {mode:"getPreprocessorPreview",previewText:previewText,matchText:matchText,replaceText:replaceText,
                                regex: regexCheckbox, wordOnly:wordMatchCheckbox,startInput: startCheckbox,
                                endInput:endCheckbox, replace:replaceCheckbox, upper:uppcaseCheckbox, lower:lowercaseCheckbox,
                                trim:trimCheckbox};
            generalAjaxQuery('KnowledgeAcquisitionServlet',processPreprocessorPreviewButtonCallback,dataList);
        }
    }
    
    function processPreprocessorPreviewButtonCallback(response) {
        let rows = response['rows'];
        if (rows[0] === "#EXCEPTION#") {
            dconsole(rows[1]);
            standardOK("confirmDialog","Error","The action creation would fail due to a parse error: " + rows[1])();
        }
        else {
            standardOK("confirmDialog","Action Preview Result",rows[0])();
        }
    }
    
    function processPreprocessorAddButton(parentDiv) {
        let replaceText = $.trim($("#preprocessorReplace").val());
        let matchText = $.trim($("#preprocessorMatch").val());
        let replaceCheckbox = $("#preprocessorReplaceCheckbox").is(":checked");
        let regexCheckbox = $("#preprocessorRegexCheckbox").is(":checked");
        let wordMatchCheckbox = $("#preprocessorWordMatchCheckbox").is(":checked");
        let startCheckbox = $("#preprocessorStartCheckbox").is(":checked");
        let endCheckbox = $("#preprocessorEndCheckbox").is(":checked");
        let uppcaseCheckbox = $("#preprocessorUppcaseCheckbox").is(":checked");
        let lowercaseCheckbox = $("#preprocessorLowercaseCheckbox").is(":checked");
        let trimCheckbox = $("#preprocessorTrimCheckbox").is(":checked");
        
        if (matchText === "") {
            standardOK("confirmDialog","Error","No match text has been specified to in the action's attributes")();   
        }
        else if (uppcaseCheckbox && lowercaseCheckbox) {
            standardOK("confirmDialog","Error","There is no point converting to upper and then lower at the same time!")();
        }
        else {
            let dataList = {mode:"addPreprocessorAction",matchText:matchText,replaceText:replaceText,
                                regex: regexCheckbox, wordOnly:wordMatchCheckbox,startInput: startCheckbox,
                                endInput:endCheckbox, replace:replaceCheckbox, upper:uppcaseCheckbox, lower:lowercaseCheckbox,
                                trim:trimCheckbox};
            generalAjaxQuery('KnowledgeAcquisitionServlet',processPreprocessorAddButtonCallback,dataList,parentDiv);  
        }
    }
    
    function processPreprocessorAddButtonCallback(response,parentDiv) {
        
        if (response['status'] !== "OK")
             standardOK("confirmDialog", "Error","Action not added!")();
         
        else { 
            processPreprocessorGetActions(parentDiv);
        }
    }
    
    function showActionsViewer(parentDiv) {
        let myMainDiv = 'actionsPreprocessorDiv';
        $("#" + parentDiv).html("");
        $("#" + parentDiv).append("<div class='tableHeader'>Actions list</div>");   
        $("#" + parentDiv).append("<div id='" + myMainDiv + "'> </div>");               
        processPreprocessorGetActions(myMainDiv);       
    }
    
    function processPreprocessorGetActions(parentDiv) { 
        //preprocessorModifyActionID = -1;
        let dataList = {mode:"getPreprocessorActions"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',getPreprocessorActionsCallback,dataList,parentDiv);     
    }
    
    function getPreprocessorActionsCallback(response,parentDiv) {
        let rows = response['rows'];
        let extraStyles = "";
        let body = "";
        if (rows[0] === "#EXCEPTION#") {
            dconsole(rows[1]);
             standardOK("confirmDialog","Error","The action creation would fail due to a parse error: " + rows[1])();
        }
        else {
            $.each(rows,function(index,rowItem) {
                if (index%2 === 1)
                    extraStyles = "tableAlt1";
                else
                    extraStyles = "tableAlt2"; 
                
                body += "<div id='preAction" + index + "' class='preprocessorAction " + extraStyles + "'>" + rowItem + "</div>";  
            });
            $("#" + parentDiv).html(body);
            if (preprocessorModifyActionID !== -1 )
                highlightItem("preAction" + preprocessorModifyActionID,"queryTableItemSelected");
        }
        
    }
   
    
    
    /* ******************* Preprocessor *************************** */
    // Main div for adding/modifying preprocessor values
    /* **************************************************************** */ 
    function createPreprocessorMainInterface(myParentDiv) { 
        dconsole("My parent div is: " + myParentDiv);
        let mainWrapper = getPreprocessorMain();
        
        if ($("#" + getPreprocessorMain()).length) {
            dconsole("Length is " + $("#" + getPreprocessorMain()).length);
            $("#" + getPreprocessorMain()).remove();
        }
        
        $("#" + myParentDiv).html("");           
        $("#" + myParentDiv).prepend("<div id='" + mainWrapper + "' class='column'></div>");
        $("#" + mainWrapper).append("<div id='preprocessorDiv' class='fullColumn'></div>");
        preprocessorContent('preprocessorDiv');           
                
        preprocessorAddEventListeners();
        //let dataList = {mode:"populateContextVariables"};
        //generalAjaxQuery('KnowledgeAcquisitionServlet',createSystemContextVariablesInterfaceCallback,dataList);  
    }

    
    function preprocessorContent(parentDiv) {
        let myMainDiv = 'preprocessorDivColumn';
        $("#" + parentDiv).html("");
        $("#" + parentDiv).append("<div class='tableHeader'>Create a new preprocessor action</div>");
        $("#" + parentDiv).append("<div id='" + myMainDiv + "'> </div>");  
        
        $("#" + myMainDiv).append("<div class='sectionHeader'>Text input</div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpan' style='margin-top:15px;'>Match text</span><input id='preprocessorMatch' type='text' placeholder='Text to match...' class='textInputWideWidth'></div>");       
        $("#" + myMainDiv).append("<div class='smallColumn'><span class='leftSpan' style='margin-top:15px;'><input type='checkbox' id = 'preprocessorRegexCheckbox' name='preprocessorRegexCheckbox' value='0'><span class='checkboxRightSpan'>Regex (R)</span>");
        $("#" + myMainDiv).append("<div class='smallColumn'><span class='leftSpan' style='margin-top:15px;'><input type='checkbox' id = 'preprocessorWordMatchCheckbox' name='preprocessorWordMatchCheckbox' value='1' checked><span class='checkboxRightSpan'>Word match (w)</span>");
        $("#" + myMainDiv).append("<div class='smallColumn'><span class='leftSpan' style='margin-top:15px;'><input type='checkbox' id = 'preprocessorStartCheckbox' name='preprocessorStartCheckbox' value='0'><span class='checkboxRightSpan'>Must be at start (s)</span>");
        $("#" + myMainDiv).append("<div class='smallColumn'><span class='leftSpan' style='margin-top:15px;'><input type='checkbox' id = 'preprocessorEndCheckbox' name='preprocessorEndCheckbox' value='0'><span class='checkboxRightSpan'>Must be at end (e)</span>");
        
        $("#" + myMainDiv).append("<div class='sectionHeader'>Text output</div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpan' style='margin-top:15px;'>Replace text</span><input id='preprocessorReplace' type='text' placeholder='Replacement text...' class='textInputWideWidth leftSpan'><span class='leftSpan' style='margin-top:15px;'><input type='checkbox' id = 'preprocessorReplaceCheckbox' name='preprocessorReplaceCheckbox' value='1' checked><span class='checkboxRightSpan'>Replace matched (r)</span></div>");
        $("#" + myMainDiv).append("<div class='fullColumn' id='preprocessorReplaceDiv'></div>");
        $("#" + 'preprocessorReplaceDiv').append("<div class='smallColumn'><span class='leftSpan' style='margin-top:15px;'><input type='checkbox' id = 'preprocessorUppcaseCheckbox' name='preprocessorUppcaseCheckbox' value='0'><span class='checkboxRightSpan'>To uppercase (u)</span>");
        $("#" + 'preprocessorReplaceDiv').append("<div class='smallColumn'><span class='leftSpan' style='margin-top:15px;'><input type='checkbox' id = 'preprocessorLowercaseCheckbox' name='preprocessorLowercaseCheckbox' value='0'><span class='checkboxRightSpan'>To lowercase (l)</span>");
        $("#" + 'preprocessorReplaceDiv').append("<div class='smallColumn'><span class='leftSpan' style='margin-top:15px;'><input type='checkbox' id = 'preprocessorTrimCheckbox' name='preprocessorTrimCheckbox' value='0'><span class='checkboxRightSpan'>Trim result (t)</span>");

        
        $("#" + myMainDiv).append("<div class='sectionHeader'>Action preview</div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><span class='leftSpan' style='margin-top:15px;'>Preview text</span><input id='preprocessorPreview' type='text' placeholder='Text to preview...' class='textInput leftSpan'><input id='preprocessorPreviewButton' type='button' value='Preview action' class='inlineButtonSmallFloatLeft hbutton '></div>");

        $("#" + myMainDiv).append("<div class='sectionHeader'>Action maintenance</div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><input id='preprocessorAddButton' type='button' value='Add action' class='inlineButtonSmallFloatLeft hbutton '><input id='preprocessorModifyButton' type='button' value='Modify action' class='inlineButtonSmallFloatLeft hbutton '><input id='preprocessorDeleteButton' type='button' value='Delete action' class='inlineButtonSmallFloatLeft hbutton '></div>");

        $("#" + myMainDiv).append("<div class='fullColumn'><input id='preprocessorSaveButton' type='button' value='Save all changes' class='inlineButtonSmallFloatRight hbutton '></div>");

        $("#" + myMainDiv).append("<div class='halfColumn useBorder' id='dialogHistoryPreprocessWrapper'></div>");
        $("#" + myMainDiv).append("<div class='halfColumn useBorder' id='actionsPreprocessWrapper'></div>");
        //$("#" + myMainDiv).append("<div class='halfColumn useBorder' id='preprocessorActionsDiv'><div class='sectionHeader'>Actions</div></div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><input id='preprocessorMoveUpButton' type='button' value='Move up' class='inlineButtonSmallFloatRight hbutton '><input id='preprocessorMoveDownButton' type='button' value='Move down' class='inlineButtonSmallFloatRight hbutton '></div>");
        $("#" + myMainDiv).append("<div class='fullColumn'><input id='preprocessorDeleteButton' type='button' value='Delete' class='inlineButtonSmallFloatRight hbutton '></div>");
        

        showDialogViewer("dialogHistoryPreprocessWrapper");
        initialiseActionList();
    }
    
    function initialiseActionList() {
        let dataList = {mode:"initialiseActionList"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',initialiseActionListCallback,dataList);  

    }
       
    function initialiseActionListCallback() {      
        showActionsViewer("actionsPreprocessWrapper");
    }
    
    function preprocessorMainDialogCloseCallback() {      
        updateMetaDataInterface(null);             
        dconsole("closing preprocessor");
    }
    
    function showDialogViewer(parentDiv) {
        let myMainDiv = 'dialogHistoryPreprocessorDiv';
        $("#" + parentDiv).append("<div class='tableHeader'>Dialog History</div>");   
        $("#" + parentDiv).append("<div id='" + myMainDiv + "'> </div>"); 

        processGetDialogHistory(myMainDiv,"dialogpreprocessor",showDialogViewerCallback);
    }
    
    function showDialogViewerCallback() {
        dconsole("showDialogViewerCallback"); 
    }
    
    
    function processPreprocessorActionAddButton() {
        let querySnippetDescription = $("#querySnippetDescription").val();
        
        if (querySnippetDescription.trim() === "") {
            standardOK("confirmDialog2", "Error","Please provide a description for the snippet")();
        }
        else {
            let fields = $("#querySelectedFields").val();
            let joins = $("#queryJoinFields").val();
            let criteria = $("#queryCriteriaFields").val();
            
            let dataList = {mode:"saveQuerySnippet",
                queryDescription:querySnippetDescription,
                queryFields:fields,
                queryJoins:joins,
                queryCriteria:criteria
            };
            if (fields === "" && joins === "" & criteria === "" ) {
                standardOK("confirmDialog2", "Error","No fields, joins or criteria have been set to be saved in a snippet")();
            }
            else {              
                generalAjaxQuery('KnowledgeAcquisitionServlet',processQuerySnippetSaveButtonCallback,dataList);
            }
        }
    }
    
    