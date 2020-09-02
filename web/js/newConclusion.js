    var newConclusionMain = "KAnewConclusionsWrapper";
    
    function getNewConclusionMain() {
        return newConclusionMain;
    }
    
    var newConclusionEventListenersRegistered = false;
    
    function newConclusionDocumentReady(parent,sibling) {
        createNewConclusionInterface(parent,sibling);
        
        //updateMetaDataInterface('KAqueryBuilderButton');
    }
    
    /* *******************  New conclusion  *************************** */
    // Main div for adding new conclusion
    /* **************************************************************** */ 
    function createNewConclusionInterface(parentDiv,sibling) { 
        $("#" + parentDiv).html("");
        if ($("#KANewConclusionWrapper").length)
            $("#KANewConclusionWrapper").remove();
        
        $("#" + parentDiv).prepend("<div id='KANewConclusionWrapper'></div>");
        $("#KANewConclusionWrapper").append("<div class='columnHeading'>New Conclusion Creation</div>");
        
        // top line navigation and check boxes
        $("#KANewConclusionWrapper").append("<div id='KAnewConclusionSubWrapper1' class='KAnewConclusionSubWrapper'></div>");
        $("#KAnewConclusionSubWrapper1").append("<input id='KAaddConclusionButton' type='button' class='inlineButton' value='Add conclusion' disabled>");
        $("#KAnewConclusionSubWrapper1").append("<input id='KApreviewConclusionButton' type='button' class='inlineButton' value='Preview conclusion' disabled>");
        /*$("#KAnewConclusionSubWrapper1").append("<span class='checkText'>Replace Empty Query</span><input id='KAReplaceEmptyCheck' type='checkbox' class='inlineCheck'>");*/
        $("#KAnewConclusionSubWrapper1").append("<span class='checkText'>No prompting</span><input id='KARnoPromptCheck' type='checkbox' class='inlineCheck'>");
   
        // main text area
        $("#KANewConclusionWrapper").append("<div id='KAnewConclusionSubWrapper2' class='KAnewConclusionSubWrapper'></div>");
        $("#KAnewConclusionSubWrapper2").append("<div id='KAnewConclusionSubWrapper2a' class='fullblockWrapper useBorder'></div>");
        $("#KAnewConclusionSubWrapper2a").append("<div class='columnHeading'>Conclusion text</div>");
        $("#KAnewConclusionSubWrapper2a").append("<textarea id='conclusionText' class='KAtextArea'>");
        
        // buttons
        $("#KANewConclusionWrapper").append("<div id='KAnewConclusionSubWrapper3' class='KAnewConclusionSubWrapper'></div>");
        $("#KAnewConclusionSubWrapper3").append("<input id='KAclearQueryButton' type='button' class='inlineButton' value='clear query XML'>");
        $("#KAnewConclusionSubWrapper3").append("<input id='KAInsertStoredQueryButton' type='button' class='inlineButton' value='Insert stored database query'>");
        $("#KAnewConclusionSubWrapper3").append("<input id='KAInsertReplaceEmptyTemplateButton' type='button' class='inlineButton' value='Insert Replace Empty Template'>");
        
        // current query XML text area
        $("#KANewConclusionWrapper").append("<div id='KAnewConclusionSubWrapper4' class='KAnewConclusionSubWrapper'></div>");
        $("#KAnewConclusionSubWrapper4").append("<div id='KAnewConclusionSubWrapper4a' class='fullblockWrapper useBorder'></div>");
        $("#KAnewConclusionSubWrapper4a").append("<div class='columnHeading'>Last inserted query XML</div>");
        $("#KAnewConclusionSubWrapper4a").append("<textarea id='queryText' class='KAtextArea' readonly>");
        
        // insert context variable section
        $("#KANewConclusionWrapper").append("<div id='KAnewConclusionSubWrapper5' class='KAnewConclusionSubWrapper'></div>");
        $("#KAnewConclusionSubWrapper5").append("<div id='KAnewConclusionMidSection1'class='halfblockWrapper'></div>");
        //$("#KAnewConclusionSubWrapper5").append("<div id='KAnewConclusionMidSection2'class='halfblockWrapper'></div>");
        $("#KAnewConclusionMidSection1").append("<div class='columnHeading'>Insert context variable value</div>");
        $("#KAnewConclusionMidSection1").append("<span class='KAconclusionSpan'>Variable</span><select id='KAconclusionInsertContextVariable' class='KAconclusionInput'/>");
        getContextVariables("KAconclusionInsertContextVariable",noopCallback);
        $("#KAnewConclusionMidSection1").append("<input id='KAconvertToIntegerCheck' type='checkbox' class='inlineCheckLeft clearLeft'><span class='checkTextLeft'>Convert to integer</span>");
        $("#KAnewConclusionMidSection1").append("<input id='KAinsertContextVariableButton' type='button' class='inlineButtonSmall' value='Insert'>");
              
        // Actions section
        $("#KANewConclusionWrapper").append("<div id='KAnewConclusionSubWrapper6' class='KAnewConclusionSubWrapper'></div>");
        $("#KAnewConclusionSubWrapper6").append("<div id='KAnewConclusionSubWrapper6a' class='fullblockWrapper useBorder'></div>");
        $("#KAnewConclusionSubWrapper6a").append("<div class='columnHeading'>Conclusion Actions</div>");
        
        // Actions - category
        $("#KAnewConclusionSubWrapper6a").append("<div id='KAnewConclusionSubWrapper7' class='KAnewConclusionSubWrapper'></div>");
        
        $("#KAnewConclusionSubWrapper7").append("<span class='KAconclusionSpan'>Category</span><select id='KAconclusionActionCategory' class='KAconclusionInput'/></select>");
        getCommandCategoryList("KAconclusionActionCategory",noopCallback);
        
        // Actions - the action associated with a category
        $("#KAnewConclusionSubWrapper6a").append("<div id='KAnewConclusionSubWrapper8' class='KAnewConclusionSubWrapper'></div>");
        $("#KAnewConclusionSubWrapper8").append("<span class='KAconclusionSpan'>Action</span><select id='KAconclusionActionCommand' class='KAconclusionInput'/>");
        
        // Actions - parameter data associated with the action
        $("#KAnewConclusionSubWrapper6a").append("<div id='KAnewConclusionSubWrapper9' class='KAnewConclusionSubWrapper'></div>");
        $("#KAnewConclusionSubWrapper9").append("<span class='KAconclusionSpan'>Parameter data</span><input type='text' id='KAconclusionActionVariable' class='KAconclusionInput' readOnly disabled/><input type='button' id='KAgetParameterData' value='get parameter data' class='inlineButtonLeft' disabled><input type='button' id='KAupdateParameterData' value='reset' class='KAsmallButton'>");
        
        // Actions - contextual information associated with an action (i.e. info for setting a variable, unsetting a var etc
        $("#KAnewConclusionSubWrapper6a").append("<div id='KAnewConclusionActionData' class='KAnewConclusionSubSubWrapper'></div>");
        
        newConclusionAddEventListeners();
    }
    
    function newConclusionAddEventListeners() {
        if (!newConclusionEventListenersRegistered) {
            // New conclusion - Allow the user to clear the parameter data and select values again..
            $('body').on('click',"#KAupdateParameterData",function(e) {
                $("#KAconclusionActionVariable").val("");
                //fireTriggerChange("KAconclusionActionCommand");
            });

            // New conclusion - insert a stored query reference
            $('body').on('click',"#KAInsertStoredQueryButton",function(e) {
                insertStoredQuery("conclusionText",'queryText',insertStoredQueryCallback);       
            });
            
            // New conclusion - insert a context variable reference
            $('body').on('click',"#KAinsertContextVariableButton",function(e) {
                insertContextVariable("conclusionText",insertContextVariableCallback);       
            });
            
            // New conclusion - insert a context variable reference
            $('body').on('click',"#KAInsertReplaceEmptyTemplateButton",function(e) {
                standardOK('confirmDialog','Warning',"When using the replace template, a SYSTEM context variable, 'emptyDBResult' must be defined to define text when query results are undefined")();
                insertReplaceEmpty("conclusionText",insertReplaceEmptyCallback);       
            });
            
            
            // New conclusion - insert a context variable reference
            $('body').on('click',"#KAgetParameterData", showGetParameterDialog);
            
            
            // New conclusion - change on main selector for category 
            $('body').on('change','#KAconclusionActionCategory', function(e) {         
                var category = $("#KAconclusionActionCategory").val();
                $("#KAconclusionActionVariable").val("");

                if (category === "") { // we're not setting an action, so change the selectors to display nothing
                   resetSelector("KAconclusionActionCommand");
                   addItemToSelector("KAconclusionActionCommand","","");
                   updateSelector("KAconclusionActionCommand","");
                   //$("#KAsetContextVariableWrapper").remove();
                   disableItem('KAgetParameterData');
                   $("#KAconclusionActionVariable").val("");
                }
                else {
                    getCommandActionList("KAconclusionActionCommand",category,fireTriggerChange,"KAconclusionActionCommand");
                }
            });

            // New conclusion - change on main selector for action command
            $('body').on('change','#KAconclusionActionCommand', function(e) {
                $("#KAconclusionActionVariable").val("");
                if ($("#KAconclusionActionCommand").val() === "" || $("#KAconclusionActionCommand").val() === "LoadAllSchemaItems")                    
                    disableItem('KAgetParameterData');
                else
                    enableItem('KAgetParameterData');
            });
            
            // New conclusion - process radio button selection to determine the source of setting a context variable
            $('body').on('change',"input[type=radio][name=KAcontextSource]",function(e) {
                if (this.value === '1') {
                    $("#KAsourceContextVar").prop('disabled',false);
                    $("#KAliteralContextVar").prop('disabled',true);
                    $("#KAsourceContextVar").addClass("KAdivHighlight");
                    $("#KAliteralContextVar").removeClass("KAdivHighlight");
                }
                else {
                    $("#KAliteralContextVar").prop('disabled',false);
                    $("#KAsourceContextVar").prop('disabled',true); 
                    $("#KAsourceContextVar").removeClass("KAdivHighlight");
                    $("#KAliteralContextVar").addClass("KAdivHighlight");
                }
            });
            
            $('body').on('keyup','#KAliteralContextVarTarget', function(e) {                     
                if ($("#KAliteralContextVarTarget").val() === "")  // allow for chars to have been deleted..
                    $('#KAdestContextVar').prop('disabled',false);   
            });
            
            $('body').on('keypress','#KAliteralContextVarTarget', function(e) {
                let key = e.keyCode;
                
                if (((key >= 48 && key <= 57) || (key >= 65 && key <= 90) || (key >=97 && key <= 122))) {
                    $('#KAdestContextVar').prop('disabled',true);                   
                }
                else {                  
                    standardOK("confirmDialog3","ERROR","Only the characters a..z,A..z and 0-9 are allowed.")();
                    e.preventDefault();
                }
            });
            
            
            // New conclusion - process radio button selection to determine the source of a date context variable
            $('body').on('change',"input[type=radio][name=KAdateContextSource]",function(e) {
                if (this.value === '1') {
                    $("#KAdateSourceContextVar").prop('disabled',false);
                    $("#KAdateLiteralContextVar").prop('disabled',true);
                    $("#KAdateSourceContextVar").addClass("KAdivHighlight");
                    $("#KAdateLiteralContextVar").removeClass("KAdivHighlight");
                }
                else {
                    $("#KAdateLiteralContextVar").prop('disabled',false);
                    $("#KAdateSourceContextVar").prop('disabled',true); 
                    $("#KAdateSourceContextVar").removeClass("KAdivHighlight");
                    $("#KAdateLiteralContextVar").addClass("KAdivHighlight");
                }
            });

            // New conclusion - SetContextVariable
            $('body').on('click','#KAsetContextVarSetParameterButton', setContextVariableParameterData);
            
            // New conclusion - UnsetContextVariable   
            $('body').on('click','#KAunsetContextVarSetParameterButton', unsetContextVariableParameterData);       

            
            // New conclusion - SetParsedDateContextVariable
            $('body').on('click','#KAdateContextVarSetParameterButton', setParsedDateContextVariableParameterData);
                
            
            // New conclusion - LoadSchemaItem 
            $('body').on('click','#KAloadSchemaSetParameterButton', setLoadSchemaVariableParameterData);        

            
            // New conclusion - SaveSchemaItem    
            $('body').on('click','#KAsaveSchemaItemSetParameterButton', setSaveSchemaVariableParameterData); 
              

            // New conclusion - process radio button changes to determine source for saving schema item
            $('body').on('change',"input[type=radio][name=KAsaveSchemaItemContextSource]",function(e) {
                if (this.value === '1') {
                    $("#KAsaveSchemaItemSourceContextVar").prop('disabled',false);
                    $("#KAsaveSchemaItemLiteralValue").prop('disabled',true);
                    $("#KAsaveSchemaItemSourceContextVar").addClass("KAdivHighlight");
                    $("#KAsaveSchemaItemLiteralValue").removeClass("KAdivHighlight");
                }
                else {
                    $("#KAsaveSchemaItemLiteralValue").prop('disabled',false);
                    $("#KAsaveSchemaItemSourceContextVar").prop('disabled',true); 
                    $("#KAsaveSchemaItemSourceContextVar").removeClass("KAdivHighlight");
                    $("#KAsaveSchemaItemLiteralValue").addClass("KAdivHighlight");
                }
            });

            /* *******************  New conclusion submission ******************** */
            // final selected new conclusion submission
            $('body').on('click','#KAaddConclusionButton', function(e) { 
                if (isParameterDataOK())
                    constructNewConclusion(constructNewConclusionCallback);
                else
                   standardOK("confirmDialog","Error","You have set a conclusion action that requires parameter data, but you haven't provided any..")();  
            });
            
            
            // New conclusion - disable buttons if the main conclusion textarea is empty
            $('body').on('keyup', '#conclusionText', function(e) {  
                if (($("#conclusionText").val().replace(/ /g,"") === "")) {
                        $("#KAaddConclusionButton").prop('disabled',true);
                        $("#KApreviewConclusionButton").prop('disabled',true);
                }   
            });

            // New conclusion - prevent enter key being used in main conclusion textarea
            $('body').on('keypress', '#conclusionText', function(e) {
                $("#KAaddConclusionButton").prop('disabled',false);
                $("#KApreviewConclusionButton").prop('disabled',false);

                if (e.which === 13) {
                    alert("No new lines are permitted in a conclusion - use '\\n'");
                    e.preventDefault();
                }
            });

            // New conclusion - prevent new-line chars being pasted into the main conclusion textarea
            $('body').on('change', '#conclusionText', function(e) {
                $("#conclusionText").val($("#conclusionText").val().replace(/\n/g, ""));       
                $("#conclusionText").val($("#conclusionText").val().replace(/\r/g, "")); 

                if (($("#conclusionText").val().replace(/ /g,"") !== "")) {
                    $("#KAaddConclusionButton").prop('disabled',false);
                    $("#KApreviewConclusionButton").prop('disabled',false);
                }
                else {
                    $("#KAaddConclusionButton").prop('disabled',true);
                    $("#KApreviewConclusionButton").prop('disabled',true);  
                }   
            });
            
            
            // New conclusion - preview the conclusion
            $('body').on('click','#KApreviewConclusionButton', function(e) {  
                if ($("#conclusionText").val() !== "") {
                    previewNewConclusion();
                }
            });   
            
            $('body').on('click','#KAclearQueryButton', function(e) {  
                $("#queryText").val("");
            });
            
            newConclusionEventListenersRegistered = true;
        }
    }
    
    function isParameterDataOK() {
        let isOK = true;
        var command = $("#KAconclusionActionCommand").val();
        var paramData = $("#KAconclusionActionVariable").val();
        
        if (command === 'SetContextVariable' || 
            command === 'UnsetContextVariable' || 
            command === 'SetParsedDateContextVariable' ||
            command === 'LoadSchemaItem' ||
            command === 'SaveSchemaItem') {
            isOK = paramData !== "";
        }
        
        return isOK;
    }
    
    function showGetParameterDialog() {
        var category = $("#KAconclusionActionCategory").val();
        var command = $("#KAconclusionActionCommand").val();
        if (category === "" || category === undefined) {    
            standardOK("confirmDialog","Error","Please select an action category..")(); 
        }
        else if (command === "" || command === undefined) {
            standardOK("confirmDialog","Error","Please select an action command..")();
        }
        else {

            $("#KAconclusionActionVariable").val(""); // this is the parameter data

            if (command === 'SetContextVariable' || command === 'SetGlobalContextVariable' ) {
                setContextVariable("confirmDialog2");
                let buttonList = {
                    "Set parameter data": function () {
                        setContextVariableParameterData();
                    },
                    "Close": function () {
                        standardProcessDialogCloseHandler('confirmDialog2');
                    }   
                };
                standardProcessDialog('confirmDialog2', "Set context variable action",null,buttonList,500)();
                //setContextVariable("KAnewConclusionActionData");
                getContextVariables("KAdestContextVar",noopCallback);
                getContextVariables("KAsourceContextVar",noopCallback);               
            }
            else if (command === 'UnsetContextVariable') {
                unsetContextVariable("confirmDialog2");
                let buttonList = {
                    "Set parameter data": function () {
                        unsetContextVariableParameterData();
                    },
                    "Close": function () {
                        standardProcessDialogCloseHandler('confirmDialog2');
                    }   
                };
                standardProcessDialog('confirmDialog2', "Unset context variable action",null,buttonList,500)();
                //unsetContextVariable("KAnewConclusionActionData");
                getContextVariables("KAunsetDestContextVar",noopCallback);                
            }
            else if (command === 'SetParsedDateContextVariable') {
                setParsedDateContextVariable("confirmDialog2");
                let buttonList = {
                    "Set parameter data": function () {
                        setParsedDateContextVariableParameterData();
                    },
                    "Close": function () {
                        standardProcessDialogCloseHandler('confirmDialog2');
                    }   
                };
                standardProcessDialog('confirmDialog2', "Set date parsed from context variable action",null,buttonList,500)();

                //setParsedDateContextVariable("KAnewConclusionActionData");
                getContextVariables("KAdateDestContextVar",noopCallback);
                getContextVariables("KAdateSourceContextVar",noopCallback);
            }
            else if (command === 'LoadSchemaItem') {
                loadSchemaItem("confirmDialog2");
                let buttonList = {
                    "Set parameter data": function () {
                        setLoadSchemaVariableParameterData();
                    },
                    "Close": function () {
                        standardProcessDialogCloseHandler('confirmDialog2');
                    }   
                };
                 standardProcessDialog('confirmDialog2', "Specify which schema-backed context variable to load from database",null,buttonList,500)();
                //loadSchemaItem("KAnewConclusionActionData");
                getContextVariables("KAloadSchemaDestContextVar",noopCallback);  
            }
            else if (command === 'SaveSchemaItem') {
                saveSchemaItem("confirmDialog2");
                let buttonList = {
                    "Set parameter data": function () {
                        setSaveSchemaVariableParameterData();
                    },
                    "Close": function () {
                        standardProcessDialogCloseHandler('confirmDialog2');
                    }   
                };
                standardProcessDialog('confirmDialog2', "Specify which schema-backed context variable to save to database",null,buttonList,500)();

                //saveSchemaItem("KAnewConclusionActionData");
                getContextVariables("KAsaveSchemaItemDestContextVar",noopCallback);  
                getContextVariables("KAsaveSchemaItemSourceContextVar",noopCallback);  
            }
            else if (command === 'LoadAllSchemaItems') {              
            }
        }
    }
    
    function setLoadSchemaVariableParameterData() {
        $("#KAconclusionActionVariable").val($("#KAloadSchemaDestContextVar").val());
        //$("#KAloadSchemaItemWrapper").remove();
    }
    
    function setSaveSchemaVariableParameterData() {
        let value;
        if (!$("#KAsaveSchemaItemSourceContextVar").prop('disabled'))
            value = $("#KAsaveSchemaItemSourceContextVar").val();
        else
            value = $("#KAsaveSchemaItemLiteralValue").val();

        $("#KAconclusionActionVariable").val($("#KAsaveSchemaItemDestContextVar").val() + "_" + value);
        //$("#KAsaveSchemaItemWrapper").remove();
    }
    
    function setParsedDateContextVariableParameterData() {
        let value;
        if (!$("#KAdateSourceContextVar").prop('disabled'))
            value = $("#KAdateSourceContextVar").val();
        else
            value = $("#KAdateLiteralContextVar").val();

        $("#KAconclusionActionVariable").val($("#KAdateDestContextVar").val() + "_" + value);
        //$("#KAsetParsedDateContextVariableWrapper").remove();
    }
    
    function unsetContextVariableParameterData() {
        $("#KAconclusionActionVariable").val($("#KAunsetDestContextVar").val());
        //$("#KAsetContextVariableWrapper").remove();  
    }
        
    function setContextVariableParameterData() {
        let literalTargetVal = '@' + $("#KAliteralContextVarTarget").val();
        let found = false;
        $("#KAdestContextVar > option").each(function (e) {
            dconsole("Examining: " + $(this).val() + " with " + literalTargetVal);
            if ($(this).val() === literalTargetVal) {
               found = true; 
               dconsole("Match!");
            }
        });
        if (found) {
            standardOK("confirmDialog3","Error","The target variable '" + literalTargetVal + "' already exists in the target variable drop-down list. Please use that instead.")(); 
        }
        else {
            let value;
            if (!$("#KAsourceContextVar").prop('disabled'))
                value = $("#KAsourceContextVar").val();
            else
                value = $("#KAliteralContextVar").val();

            if (value === "" || value === undefined)
                standardOK("confirmDialog3","Error","The literal value set for the context variable is undefined")(); 
            else {
                let sourceVar = $("#KAdestContextVar").val();

                if ($("#KAdestContextVar").prop('disabled')  && $("#KAliteralContextVarTarget").val() !== "") {
                    dconsole("Dest context var is disabled! yay!");
                    sourceVar = '@' + $("#KAliteralContextVarTarget").val();
                }
                $("#KAconclusionActionVariable").val(sourceVar + "_" + value);
               // $("#KAsetContextVariableWrapper").remove();
            }
        }
    }
    
    function newConclusionMainDialogCloseCallback() {
       
    }
        
    function insertStoredQuery(parent1,parent2,callback) {
        let dataList = {mode:"getSavedQueryList"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',callback,dataList,parent1,parent2);  
    }
    
    function insertStoredQueryCallback(theResponse,parent1,parent2) {
        let rows = theResponse['rows'];     
        let header = theResponse['header'];
        let headerWidths = [20,130,200];
        
        // we want more tabular dialog to select from..
        let body = createBodyTableViewerChooserContent(header, headerWidths, rows, "chooser1");
        

        //let body = createChooserBodyContent(rows, "chooser1"); 
        let buttonList = [ 
            {                
                id: "conclusion-condition-query-insert",
                text: "Insert",
                click: conclusionInsertQueryButtonHandler
            },
            {                
                id: "conclusion-condition-query-close",
                text: "Close",
                click: conclusionInsertQueryCloseButtonHandler
            }                        
        ];

        // default chooser populates the queryChooserTableFieldNames item, even though we're not dealing with fieldnames here..
        let data = {resultData:"chooser1",selectionData:"queryChooserTableFieldNames",parent1:parent1,parent2:parent2};
        
        initialiseChooserSelectionData(chooserData,"chooser1",null,null);
        singleChooserDialog(chooserData,"confirmDialog","Choose query to insert",body,buttonList,data,true)(); 
    }
    
    function conclusionInsertQueryButtonHandler() {       
        // where to put the query selection data, and determine how that data was selected
        let parent1 = $(this).data("maindata").parent1;
        
        // parent2 here is the queryXML text area (for reference)
        let parent2 = $(this).data("maindata").parent2;
        $('#' + parent2).val('');
        
        let queryBaseData = $(this).data("maindata").resultData;
        let rowDataName = $(this).data("maindata").selectionData;
        
        let selectedRowData = getChooserSelectionDataFromTable(chooserData,queryBaseData,rowDataName);
        if (selectedRowData.length !== 0) {
        // format is "ID description.."
            let queryID = selectedRowData[0];
            let queryDescription = selectedRowData[1];
            
            // we have two ajax calls to do, one for the in-conclusion markup, the other to get the query contents for the read-only XML text area
            let dataList = {mode:"getInsertQueryMarkup", queryID:queryID, queryDescription:queryDescription};
            generalAjaxQuery('KnowledgeAcquisitionServlet',conclusionInsertQueryButtonHandlerCallback,dataList,parent1);
            
            dataList = {mode:"getInsertQueryContent", queryID:queryID};
            generalAjaxQuery('KnowledgeAcquisitionServlet',conclusionInsertQueryButtonHandlerCallback2,dataList,parent2);
        }
        else {
            standardOK("confirmDialog2", "Error","Please select a query to insert")();
        }
    }
    
    function conclusionInsertQueryButtonHandlerCallback(theResponse,parent) {
        let markup = theResponse['result'];
        $("#" + parent).val($("#" + parent).val() + markup);     
    }
    
    function conclusionInsertQueryButtonHandlerCallback2(theResponse,parent) {
        let markup = theResponse['result'];
        $("#" + parent).val($("#" + parent).val() + markup);     
    }
    
    function conclusionInsertQueryCloseButtonHandler() {
        $(this).dialog('close');
    }
    
    
    /* *******************  New conclusion insert context variable **** */
    // Insert context variable section
    /* **************************************************************** */
    
    function insertContextVariable(parent,callback) {
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            data : {
                mode : 'insertContextVariable',
                contextVariable: $("#KAconclusionInsertContextVariable").val(),
                convertToInt: $("#KAconvertToIntegerCheck").is(":checked")
            },          
            success : function(response) {
                callback(parent,response);
            }
        });            
    }
    
    function insertContextVariableCallback(parent,value) {
        $("#" + parent).val($("#" + parent).val() + value); 
        fireTriggerChange("conclusionText");
    }
    
    function insertReplaceEmpty(parent,callback) {
        
        
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            data : {
                mode : 'insertReplaceEmpty'
            },          
            success : function(response) {
                //if ($("#" + parent).val().includes(response.split(" ")[0]))  // first word of response is the current replace XML tag
                    //standardOK('confirmDialog',"ERROR","Only one Replace Empty template per conclusion is currently supported")();
               // else
                    callback(parent,response);
            }
        });            
    }
    
    function insertReplaceEmptyCallback(parent,value) {
        if ($("#" + parent).val() === "") {
            $("#" + parent).val(value);
        }
        else {
            $("#" + parent).val($("#" + parent).val() + " " + value); 
        } 
        fireTriggerChange("conclusionText");
    }
    
    /* *******************  New conclusion actions ******************** */
    // Top Actions section of new conclusion
    /* **************************************************************** */
     
    function getCommandCategoryList(selector,callback) {
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            data : {
                mode : 'getCommandCategoryList'
            },
            
            success : function(response) {
                resetSelector(selector);
                let firstValue = true;
                $.each(response,function(index, aCategory) {
                    addItemToSelector(selector,aCategory,aCategory);
                    if (firstValue) {
                        updateSelector(selector,aCategory);
                        firstValue = false;
                    }
                });
                
                callback();
            }
        });         
    }
    
    function getCommandActionList(selector,category,callback,triggerDest) {
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            data : {
                mode : 'getCommandActionList',
                category: category
            },
            
            success : function(response) {
                resetSelector(selector);
                let firstValue = true;
                $.each(response,function(index, aCategory) {
                    addItemToSelector(selector,aCategory,aCategory);
                    if (firstValue) {
                        updateSelector(selector,aCategory);
                        firstValue = false;
                    }
                });
                
                callback(triggerDest);
            }
        });         
    }
    
    
    

    
    /* *******************  New conclusion actions ******************** */
    // ContextVariable category
    /* **************************************************************** */

    function setContextVariable(parent) {
        $("#" + parent).html("");
        $("#" + parent).append("<div id='KAsetContextVariableWrapper'></div>");
        //$("#KAsetContextVariableWrapper").append("<div class='columnHeading'>Set Context Variable</div>");
        $("#KAsetContextVariableWrapper").append("<span class='KAconclusionSpanBigger'>Target variable:</span>");
        $("#KAsetContextVariableWrapper").append("<select id='KAdestContextVar' class='KAconclusionInput'/></select>");
        $("#KAsetContextVariableWrapper").append("<span class='KAconclusionSpanBigger'>Create target variable: @</span>");
        $("#KAsetContextVariableWrapper").append("<input id='KAliteralContextVarTarget' type='text' class='KAconclusionInput'/>");
        $("#KAsetContextVariableWrapper").append("<span class='KAconclusionSpanBigger'>Select source value:</span>");
        $("#KAsetContextVariableWrapper").append("<div><input type='radio' name='KAcontextSource' value='1' class='KAradio' checked><span class='KAradio'> source variable</span>");
        $("#KAsetContextVariableWrapper").append("<input type='radio' name='KAcontextSource' value='2' class='KAradio'><span class='KAradio'>literal value</span> </div>");
        $("#KAsetContextVariableWrapper").append("<span class='KAconclusionSpanBigger'>Value: source variable</span>");
        $("#KAsetContextVariableWrapper").append("<select id='KAsourceContextVar' class='KAconclusionInput'/></select>");
        $("#KAsetContextVariableWrapper").append("<span class='KAconclusionSpanBigger'>Value: literal value</span>");
        $("#KAsetContextVariableWrapper").append("<input id='KAliteralContextVar' type='text' class='KAconclusionInput' disabled/>");
        //$("#KAsetContextVariableWrapper").append("<input id='KAsetContextVarSetParameterButton' type='button' value='Set parameter data' class='inlineButton'/>");
        $("#KAsourceContextVar").addClass("KAdivHighlight");
    }
    
    function unsetContextVariable(parent) {
        $("#" + parent).html("");

        $("#" + parent).append("<div id='KAunsetContextVariableWrapper'></div>");
        //$("#KAunsetContextVariableWrapper").append("<div class='columnHeading'>Unset Context Variable</div>");
        $("#KAunsetContextVariableWrapper").append("<span class='KAconclusionSpan'>Target variable:</span>");
        $("#KAunsetContextVariableWrapper").append("<select id='KAunsetDestContextVar' class='KAconclusionInput'/></select>");       
        //$("#KAunsetContextVariableWrapper").append("<input id='KAunsetContextVarSetParameterButton' type='button' value='Set parameter data' class='inlineButton'/>");
    }
    
    function setParsedDateContextVariable(parent) {
        $("#" + parent).html("");
        $("#" + parent).append("<div id='KAsetParsedDateContextVariableWrapper'></div>");
        //$("#KAsetParsedDateContextVariableWrapper").append("<div class='columnHeading'>Set Parsed Date Context Variable</div>");
        $("#KAsetParsedDateContextVariableWrapper").append("<span class='KAconclusionSpanBigger'>Target variable:</span>");
        $("#KAsetParsedDateContextVariableWrapper").append("<select id='KAdateDestContextVar' class='KAconclusionInput'/></select>");
        $("#KAsetParsedDateContextVariableWrapper").append("<span class='KAconclusionSpanBigger'>Select source value:</span>");
        $("#KAsetParsedDateContextVariableWrapper").append("<div><input type='radio' name='KAdateContextSource' value='1' class='KAradio' checked><span class='KAradio'> source variable</span>");
        $("#KAsetParsedDateContextVariableWrapper").append("<input type='radio' name='KAdateContextSource' value='2' class='KAradio'><span class='KAradio'>literal value</span> </div>");
        $("#KAsetParsedDateContextVariableWrapper").append("<span class='KAconclusionSpanBigger'>Value: source variable</span>");
        $("#KAsetParsedDateContextVariableWrapper").append("<select id='KAdateSourceContextVar' class='KAconclusionInput'/></select>");
        $("#KAsetParsedDateContextVariableWrapper").append("<span class='KAconclusionSpanBigger'>Value: literal value</span>");
        $("#KAsetParsedDateContextVariableWrapper").append("<input id='KAdateLiteralContextVar' type='text' class='KAconclusionInput' disabled/>");
        //$("#KAsetParsedDateContextVariableWrapper").append("<input id='KAdateContextVarSetParameterButton' type='button' value='Set parameter data' class='inlineButton'/>");
        $("#KAdateSourceContextVar").addClass("KAdivHighlight");
    }
   
    /* *******************  New conclusion actions ******************** */
    // DatabaseItem category
    /* **************************************************************** */
    function loadSchemaItem(parent) {
        $("#" + parent).html("");

        //$("#KAloadSchemaItemWrapper").remove();
        $("#" + parent).append("<div id='KAloadSchemaItemWrapper'></div>");
        //$("#KAloadSchemaItemWrapper").append("<div class='columnHeading'>Set Target Schema Variable</div>");
        $("#KAloadSchemaItemWrapper").append("<span class='KAconclusionSpan'>Target variable:</span>");
        $("#KAloadSchemaItemWrapper").append("<select id='KAloadSchemaDestContextVar' class='KAconclusionInput'/></select>");
        //$("#KAloadSchemaItemWrapper").append("<input id='KAloadSchemaSetParameterButton' type='button' value='Set parameter data' class='inlineButton'/>");
    } 
    
    function saveSchemaItem(parent) {
        $("#" + parent).html("");
        $("#" + parent).append("<div id='KAsaveSchemaItemWrapper'></div>");
        //$("#KAsaveSchemaItemWrapper").append("<div class='columnHeading'>Save Schema Variable</div>");
        $("#KAsaveSchemaItemWrapper").append("<span class='KAconclusionSpan'>Target variable:</span>");
        $("#KAsaveSchemaItemWrapper").append("<select id='KAsaveSchemaItemDestContextVar' class='KAconclusionInput'/></select>");
        $("#KAsaveSchemaItemWrapper").append("<span class='KAconclusionSpan'>Select source value:</span>");
        $("#KAsaveSchemaItemWrapper").append("<div><input type='radio' name='KAsaveSchemaItemContextSource' value='1' class='KAradio' checked><span class='KAradio'> source variable</span>");
        $("#KAsaveSchemaItemWrapper").append("<input type='radio' name='KAsaveSchemaItemContextSource' value='2' class='KAradio'><span class='KAradio'>literal value</span> </div>");
        $("#KAsaveSchemaItemWrapper").append("<span class='KAconclusionSpan'>Value: source variable</span>");
        $("#KAsaveSchemaItemWrapper").append("<select id='KAsaveSchemaItemSourceContextVar' class='KAconclusionInput'/></select>");
        $("#KAsaveSchemaItemWrapper").append("<span class='KAconclusionSpan'>Value: literal value</span>");
        $("#KAsaveSchemaItemWrapper").append("<input id='KAsaveSchemaItemLiteralValue' type='text' class='KAconclusionInput' disabled/>");
        //$("#KAsaveSchemaItemWrapper").append("<input id='KAsaveSchemaItemSetParameterButton' type='button' value='Set parameter data' class='inlineButton'/>");
        $("#KAsaveSchemaItemSourceContextVar").addClass("KAdivHighlight");
    }
        
    function constructNewConclusion(callback) {
        //var result = false;
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'constructNewConclusion',
                text: $("#conclusionText").val(),
                category: $("#KAconclusionActionCategory").val(),
                command: $("#KAconclusionActionCommand").val(),
                variable: $("#KAconclusionActionVariable").val()
            },
            
            success : function(response) {
                if (response === "")
                    callback();
                else
                    standardOK("confirmDialog",
                        "Error",
                        response
                    )(); 
            }
        });      
    }
    
    function removeActionsDivs() {       
        $("#KAsetContextVariableWrapper").remove();       
        $("#KAunsetContextVariableWrapper").remove();      
        $("#KAsetParsedDateContextVariableWrapper").remove();      
        $("#KAloadSchemaItemWrapper").remove();       
        $("#KAsaveSchemaItemWrapper").remove();
    }
    
    function constructNewConclusionCallback() {
        //showAndHide("KAConclusionsWrapper","KANewConclusionWrapper",true,true);
        
        // update the conclusion table to show the new conclusion..
        $("#KAConclusions").html("");
        // populate the table body
        populateKAConclusionList('KAConclusions');
        standardOK("confirmDialog","Notice","New conclusion added!")(); 
        //$("#metaDataDialog2").dialog('close');

    }
    
    function previewNewConclusion() {
        getConclusionPreview(getConclusionPreviewPopupCallback);

    }
       
    function getConclusionPreview(callback) {

        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getConclusionPreview',
                text: $("#conclusionText").val()
            },
            success : function(responseText) {              
                callback(responseText);
            }
         });
    }
    
    function getConclusionPreviewPopupCallback(text) {
        standardOKLarge("confirmDialog","Conclusion Preview",text)();
    }