

    var queryMain = "queryMainContentDiv";
    function getQueryMain() {
        return queryMain;
    }
    
    var querySiblingMain = "";
    
    function setQuerySiblingMain(sibling) {
        querySiblingMain = sibling;
    }
    
    function getQuerySiblingMain() {
        return querySiblingMain;
    }

    var queryEventListenersRegistered = false;

    const queryMarkupModeJoin = 0;
    const queryMarkupModeFieldSelectionNormal = 1;
    const queryMarkupModeCriteria = 2;
    const queryMarkupModeOrder = 3;
    
//    var chooserData = {
//        chooser1: {},
//        chooser2: {}
//    };
    
    var queryJoinCount = 0;

    function noopCallback() {
    }
   
    function createQueryInterface(parent,sibling) {
        setQuerySiblingMain(sibling);
        let mainDiv = getQueryMain();
        $("#" + parent).html("");
        $("#" + parent).append("<div id='" + mainDiv + "'></div>");
        //$("#" + mainDiv).hide();
        $("#" + mainDiv).append("<div class='columnHeading'>Query Builder</div>");
        $("#" + mainDiv).append("<div id='queryUserInputDiv'></div>");
        $("#queryUserInputDiv").append("<div class='column' id='queryColumn'> </div>");
        
        $("#queryColumn").append("<div class='fullColumn'><input id='queryPreviewButton' type='button' value='Preview' class='inlineButtonSmall hbutton'></div>");
        $("#queryColumn").append("<div class='fullColumn'><input id='queryShowAllQueriesButton' type='button' value='Show all queries' class='inlineButton hbutton' ><input id='queryLoadSaveSnippetButton' type='button' value='Load/Save snippet' class='inlineButton hbutton' title='Create commonly used partial queries..'></div>");
        
        $("#queryColumn").append("<div id='queryTablesListDiv' class='leftColumnQuery'></div>");
        $("#queryColumn").append("<div id='queryConditionsDiv' class='rightColumnQuery'></div>");      
        
        // Table names
        $("#queryTablesListDiv").append("<div class='tableHeader'>Reference tables:</div>");
        
        // main query SQL data creation
        $("#queryConditionsDiv").append("<div class='fullColumn' id='querySelectedFieldsMainDiv'></div>");
        $("#queryConditionsDiv").append("<div class='fullColumn' id='queryJoinConditionsMainDiv'></div>");
        $("#queryConditionsDiv").append("<div class='fullColumn' id='queryCriteriaMainDiv'></div>");
        
        $("#querySelectedFieldsMainDiv").append("<div class='tableHeader'>Selected Fields</div>");
        $("#queryJoinConditionsMainDiv").append("<div class='tableHeader'>Join conditions</div>");
        $("#queryCriteriaMainDiv").append("<div class='tableHeader'>Criteria</div>");

        /* ********************************************************************************************************************************** */
        // selected table fields  
        /* ********************************************************************************************************************************** */
        $("#querySelectedFieldsMainDiv").append("<div class='fullColumn' id='querySelectedFieldsMainDiv1'></div>");
        $("#querySelectedFieldsMainDiv").append("<div class='fullColumn' id='querySelectedFieldsMainDiv2'></div>");
        $("#querySelectedFieldsMainDiv").append("<div class='fullColumn' id='querySelectedFieldsMainDiv3'></div>");
        
        
        $("#querySelectedFieldsMainDiv1").append("<input id='querySelectedFields' type='text' placeholder='selected fields...' class='textInputFull textInput' readonly title='Fields you choose will be added here automatically'>");
        
        $("#querySelectedFieldsMainDiv2").append("<input id='queryClearFieldsButton' type='button' value='clear' class='inlineButtonSmall hbutton'>");
        $("#querySelectedFieldsMainDiv2").append("<input id='queryChooseOrderByButton' type='button' value='Choose order-by' class='inlineButtonSmall hbutton'>");
        $("#querySelectedFieldsMainDiv2").append("<input type='checkbox' id = 'queryCountCheckbox' name='queryCountCheckbox' value='1'><span class='rightSpan'>Count</span>");
        $("#querySelectedFieldsMainDiv2").append("<input id='queryChooseFieldsButton' type='button' value='Choose fields' class='inlineButtonSmall hbutton'>");

        $("#querySelectedFieldsMainDiv3").append("<span class='leftSpan'>Field name from context variable</span><select id='queryContextVariable' class='KAinputLeft'></select>");
        $("#querySelectedFieldsMainDiv3").append("<input id='queryInsertContextVariableButton' type='button' value='Insert' class='inlineButtonSmall hbutton'>");
        getContextVariables("queryContextVariable",noopCallback);
        /* ********************************************************************************************************************************** */
        
        /* ********************************************************************************************************************************** */
        // join fields  
        /* ********************************************************************************************************************************** */
        $("#queryJoinConditionsMainDiv").append("<div class='fullColumn' id='queryJoinConditionsMainDiv1'></div>");
        $("#queryJoinConditionsMainDiv").append("<div class='fullColumn' id='queryJoinConditionsMainDiv2'></div>");
        
        $("#queryJoinConditionsMainDiv1").append("<input id='queryJoinFields' type='text' placeholder='join fields...' class='textInputFull textInput' readonly>");
        $("#queryJoinConditionsMainDiv1").append("<input id='queryJoinFieldsTemp' type='text' placeholder='temp data...' class='textInputFull textInput hiddenInput' readonly>");
        
        $("#queryJoinConditionsMainDiv2").append("<input id='queryClearJoinsButton' type='button' value='clear' class='inlineButtonSmall hbutton'>");
        $("#queryJoinConditionsMainDiv2").append("<input type='checkbox' id = 'queryJoinMultivaluedCheckbox' name='queryJoinMultivaluedCheckbox' value='1'><span class='rightSpan'>Mv</span>");
        $("#queryJoinConditionsMainDiv2").append("<input id='queryChooseRightJoinButton' type='button' value='Choose right field' class='inlineButtonSmall hbutton' disabled>");
        $("#queryJoinConditionsMainDiv2").append("<input id='queryChooseLeftJoinButton' type='button' value='Choose left field' class='inlineButtonSmall hbutton'>");

        /* ********************************************************************************************************************************** */
        // criteria 
        /* ********************************************************************************************************************************** */
        $("#queryCriteriaMainDiv").append("<div class='fullColumn' id='queryCriteriaMainDiv1'></div>");
        $("#queryCriteriaMainDiv").append("<div class='fullColumn' id='queryCriteriaMainDiv2'></div>");

        $("#queryCriteriaMainDiv1").append("<input id='queryCriteriaFields' type='text' placeholder='criteria value...' class='textInputFull textInput' readonly>");
        
        $("#queryCriteriaMainDiv2").append("<input id='queryClearCriteriaButton' type='button' value='clear' class='inlineButtonSmall hbutton'>");
        $("#queryCriteriaMainDiv2").append("<input id='queryChooseCriteriaButton' type='button' value='Choose criteria' class='inlineButtonSmall hbutton'>");

        // cancel and save buttons and query description
        $("#queryColumn").append("<div class='fullColumn padding5' id='querySaveButtons'>");
        $("#querySaveButtons").append("<input id='querySaveButton' type='button' value='Save' class='inlineButtonSmall hbutton'>");
        //$("#querySaveButtons").append("<input id='queryCancelButton' type='button' value='Cancel' class='inlineButtonSmall hbutton'>");
        $("#querySaveButtons").append("<input id='queryDescription' type='text' placeholder='description...' class='textInputRight'>");

        $("#querySaveButtons").append("<span class='rightSpan padding10'>Query description: </span>");
        
        queryAddEventListeners();
    }
    
    function queryDocumentReady(parent,sibling) {
        createQueryInterface(parent,sibling);
        getQueryTableNames("queryTablesListDiv",queryDocumentReadyCallback);
        queryJoinCount = 0;
        updateMetaDataInterface('KAqueryBuilderButton');
    }
    
    function queryDocumentReadyCallback() {
        
    }
    
    function queryDocumentReadyCallback2() {
                
    }
    
    function queryMainDialogCloseCallback() {
        updateMetaDataInterface(null);
    }

    function queryAddEventListeners() {
        if (!queryEventListenersRegistered) {

            $('body').on('click','#queryPreviewButton', processQueryPreviewButton);
            $('body').on('click','#queryLoadSaveSnippetButton', processQueryLoadSaveSnippetButton);
            $('body').on('click','#queryShowAllQueriesButton', processQueryShowAllQueriesButton);
            $('body').on('click','#querySaveButton', processQuerySaveButton);
            //$('body').on('click','#queryCancelButton', processQueryCancelButton);
            $('body').on('click','#queryChooseFieldsButton', processQueryChooseFieldsButton);
            $('body').on('click','#queryClearFieldsButton', processQueryClearFieldsButton);
            $('body').on('click','#queryInsertContextVariableButton', processQueryInsertContextVariableButton);
            $('body').on('click','#queryChooseOrderByButton', processQueryChooseOrderByButton);
            $('body').on('click','#queryChooseLeftJoinButton', processQueryChooseLeftJoinButton);
            $('body').on('click','#queryChooseRightJoinButton', processQueryChooseRightJoinButton);
            $('body').on('click','#queryClearJoinsButton', processQueryChooseClearJoinsButton);
            $('body').on('click','#queryChooseCriteriaButton', processQueryChooseCriteriaButton);
            $('body').on('click','#queryClearCriteriaButton', processQueryClearCriteriaButton);
            $('body').on('click','#querySnippetDeleteButton', processQuerySnippetDeleteButton);
            $('body').on('click','#querySnippetSaveButton', processQuerySnippetSaveButton);
            $('body').on('click','#querySnippetInsertButton', processQuerySnippetInsertButton);
            
            
            
            
            
            // selecting a table (single selection only)
            $('body').on('click','.queryTableItem', function(e) {       
                let theID = 0;      
                theID = e.target.id.toString().substring(14); 
                unhighlightItems("queryTableItemSelected");
                highlightItem(e.target.id.toString(),"queryTableItemSelected");
                setSelectedTableID(theID,noopCallback);
            });
            
            
            // selecting item(s) from a chooser selection dialog
            // need to check the current selection mode          
            
            
            
            // format field dialog
            $('body').on('keyup', '#queryPrefixText, #queryPostfixText', function(e) {  
                let theBaseTarget = $("#queryPrefixText").data("target");
                if (($("#queryPrefixText").val().replace(/ /g,"") === "") && ($("#queryPostfixText").val().replace(/ /g,"") === "")) {
                    disabledChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                }
                else {
                    enableChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                }
            });
                       
            $('body').on('keyup', '#queryFixedCriteriaValue', function(e) {  
                let theBaseTarget = $("#queryFixedCriteriaValue").data("target");
                if (isEnabledItem("queryFixedCriteriaValue")) {
                    if ($("#queryFixedCriteriaValue").val().replace(/ /g,"") === "") {
                        disabledChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                    }
                    else {
                        enableChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                    }
                }
                
            });
            // criteria match radio selection
            $('body').on('change',"input[type=radio][name=queryCriteriaMatchRadioGroup]",function(e) {
                let theID = e.target.id.toString();               
                let theBaseTarget = $("#" + theID).data('target');
                setChooserSelectionData(chooserData,theBaseTarget,"queryCriteriaRadioSelection",this.value);
                
                if (this.value === "1") {
                    disableItem("queryContextCriteriaValue");
                    enableItem("queryFixedCriteriaValue");
                    if ($("#queryFixedCriteriaValue").val().replace(/ /g,"") !== "")
                        enableChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                }
                else if (this.value === "2") {
                    enableItem("queryContextCriteriaValue");
                    enableItem("queryFixedCriteriaValue");
                    if ($("#queryFixedCriteriaValue").val().replace(/ /g,"") !== "")
                        enableChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                    else
                        disableChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                }
                else if (this.value === "3") {
                    enableItem("queryContextCriteriaValue");
                    enableItem("queryFixedCriteriaValue");
                    if ($("#queryFixedCriteriaValue").val().replace(/ /g,"") !== "")
                        enableChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                    else
                        disableChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                }
                else if (this.value === "4") {
                    enableItem("queryContextCriteriaValue");
                    disableItem("queryFixedCriteriaValue");
                    enableChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                }
                else if (this.value === "5") {
                    enableItem("queryContextCriteriaValue");
                    disableItem("queryFixedCriteriaValue");
                    enableChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                }
            });

            queryEventListenersRegistered = true;
        }
    }
    
    function processQueryShowAllQueriesButton() {
        let dataList = {mode:"getSavedQueryList"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',processQueryShowAllQueriesButtonCallback,dataList);   
        //getSavedQueryList(processQueryShowAllQueriesButtonCallback);     
    }
    
    function createQueryLoadSaveSnippetInterface(body) {
        body += "<div class='fullblockWrapperSlim'><input id='querySnippetDeleteButton' type='button' class='inlineButtonSmallerFloatLeft' value='Delete'><input id='querySnippetInsertButton' type='button' class='inlineButtonSmallerFloatLeft' value='Insert'></div>";        
        body += "<div class='fullblockWrapperSlim'><div class='tableHeader'>Create new snippet</div></div>";
        /*body += "<div class='fullblockWrapperSlim'><span class='KAconclusionSpan'>Fields:</span><input id='querySnippetSaveFields' type='text' class='textInput' data-target='chooser1' placeholder='selected fields..'></div>";
        body += "<div class='fullblockWrapperSlim'><span class='KAconclusionSpan'>Joins:</span><input id='querySnippetSaveJoins' type='text' class='textInput' data-target='chooser1' placeholder='selected joins..'></div>";
        body += "<div class='fullblockWrapperSlim'><span class='KAconclusionSpan'>Criteria:</span><input id='querySnippetSaveCriteria' type='text' class='textInput' data-target='chooser1' placeholder='selected criteria'></div>";*/
        body += "<div class='fullblockWrapperSlim'><input id='querySnippetDescription' type='text' class='textInput' data-target='chooser1' placeholder='Enter snippet description'>";
        body += "<input id='querySnippetSaveButton' type='button' class='inlineButtonSmallerFloatLeft' value='Save'></div>";
        
        return body;
    }
    
    function processQueryLoadSaveSnippetButton() {
        let dataList = {mode:"getSavedQuerySnippetList"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',processQueryLoadSaveSnippetButtonCallback,dataList);   
    }
    
    function processQueryLoadSaveSnippetButtonCallback(theResponse) {   
        let rows = theResponse['rows'];
        //let body = "<div class='fullblockWrapperSlim'><div class='tableHeader'>Saved query snippets</div></div>";
        
        let header = theResponse['header'];
        let headerWidths = [50,100,200];
        let body = createBodyTableViewerChooserContent(header, headerWidths, rows, "chooser1");        
        
        //body += createChooserBodyContent(rows, "chooser1");       
        body = createQueryLoadSaveSnippetInterface(body);
        
        let buttonList = [ 
            {                
                id: "query-snippet-close",
                text: "Close",
                click: querySnippetCloseButtonHandler
            }                        
        ];

        // default chooser populates the queryChooserTableFieldNames item, even though we're not dealing with fieldnames here..
        let data = {resultData:"chooser1",selectionData:"queryChooserTableFieldNames"};
        
        initialiseChooserSelectionData(chooserData,"chooser1",null,null);
        singleChooserDialog(chooserData,"confirmDialog","Query snippets",body,buttonList,data,true)();         
    }
    
    function querySnippetCloseButtonHandler() {
        $(this).dialog("close");
    }
    
    function processQuerySnippetInsertButton() {
        let dialogData = $("#confirmDialog").data("maindata");
        
        let queryBaseData = dialogData.resultData;       
        let rowDataName = dialogData.selectionData;
        //let selectedRowData = getChooserSelectionData(chooserData,queryBaseData,rowDataName);
        let selectedRowData = getChooserSelectionDataFromTable(chooserData,queryBaseData,rowDataName);
        
        if (selectedRowData.length !== 0) {

        // format is "ID description.."
            let queryToInsert = selectedRowData[0];
            let queryDescription = selectedRowData[1];
            //let queryToInsert = selectedRowData[0].split(' ')[0];
            let dataList = {mode:"insertQuerySnippet",queryID:queryToInsert};
            generalAjaxQuery('KnowledgeAcquisitionServlet',processQuerySnippetInsertButtonCallback,dataList);
        }
        else {
            standardOK("confirmDialog2", "Error","Please select a snippet to insert")();
        }
    }
    
    function processQuerySnippetInsertButtonCallback(theResponse) {
        let rows = theResponse['rows'];
        let fields = rows[0];
        let joins = rows[1];
        let criteria = rows[2];
        
        $("#querySelectedFields").val($("#querySelectedFields").val() + fields);
        $("#queryJoinFields").val($("#queryJoinFields").val() + joins);
        $("#queryCriteriaFields").val($("#queryCriteriaFields").val() + criteria);
        
    }
    
    function processQuerySnippetSaveButton() {
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
    
    function processQuerySnippetSaveButtonCallback(theResponse) {
        let dataList = {mode:"getSavedQuerySnippetList"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',processQuerySnippetSaveButtonCallback2,dataList);
    }
    
    function processQuerySnippetSaveButtonCallback2(theResponse) {   
        let rows = theResponse['rows']; 
        
        let header = theResponse['header'];
        let headerWidths = [50,100,200];
        let body = createBodyTableViewerChooserContent(header, headerWidths, rows, "chooser1");
        //let body = "<div class='fullblockWrapperSlim'><div class='tableHeader'>Saved query snippets</div></div>";
        //body += createChooserBodyContent(rows, "chooser1");       
        body = createQueryLoadSaveSnippetInterface(body);
        $("#confirmDialog").html(body);
    }
    
    function processQuerySnippetDeleteButton() {

        let dialogData = $("#confirmDialog").data("maindata");
        
        let queryBaseData = dialogData.resultData;       
        let rowDataName = dialogData.selectionData;
        //let selectedRowData = getChooserSelectionData(chooserData,queryBaseData,rowDataName);
        let selectedRowData = getChooserSelectionDataFromTable(chooserData,queryBaseData,rowDataName);

        
        if (selectedRowData.length !== 0) {
        // format is "ID description.."
            //let queryToDelete = selectedRowData[0].split(' ')[0];
            let queryToDelete = selectedRowData[0];
            let dataList = {mode:"deleteQuerySnippet",queryID:queryToDelete};
            generalAjaxQuery('KnowledgeAcquisitionServlet',processQuerySnippetDeleteButtonCallback,dataList);
        }
        else {
            standardOK("confirmDialog2", "Error","Please select a snippet to delete")();

        }
    }
    
    function processQuerySnippetDeleteButtonCallback(theResponse) {
        let dataList = {mode:"getSavedQuerySnippetList"};
        generalAjaxQuery('KnowledgeAcquisitionServlet',processQuerySnippetDeleteButtonCallback2,dataList);  
    }
    
    function processQuerySnippetDeleteButtonCallback2(theResponse) {
        
        let rows = theResponse['rows'];
        //let body = "<div class='fullblockWrapperSlim'><div class='tableHeader'>Saved query snippets</div></div>";
        //body += createChooserBodyContent(rows, "chooser1");   
        let header = theResponse['header'];
        let headerWidths = [50,100,200];
        let body = createBodyTableViewerChooserContent(header, headerWidths, rows, "chooser1");
        
        body = createQueryLoadSaveSnippetInterface(body);
        
        $("#confirmDialog").html(body);
    }       
    
    //function processQueryShowAllQueriesButtonCallback(header, rows) {
    function processQueryShowAllQueriesButtonCallback(theResponse) {
        let header = theResponse['header'];
        let rows = theResponse['rows'];
        let headerWidths = [20,130,200];
        let body = createBodyTableViewer(header, headerWidths,rows);
        standardOKLarge("confirmDialog", "All saved queries",body,700)();
    }  
    
//    function getSavedQueryList(callback) {
//        $.ajax({
//            url : 'KnowledgeAcquisitionServlet',
//            dataType:'json',
//            data : {
//                mode: 'getSavedQueryList'
//            },
//            success : function(response) {
//                let header = response['header'];
//                let rows = response['rows'];
//                let status = response['status'];
//                if (status !== "OK") {
//                    standardOK("confirmDialog", "Error",status)();
//                }
//                else {                   
//                    callback(header,rows);
//                }
//            }
//            
//        });       
//    }
      
    function processQueryChooseFieldsButton() {
        let chosenTableNameList = getSingleItemValueWithClass("queryTableItemSelected");
       
        if (chosenTableNameList === null) {
            standardOK("confirmDialog", "Error","Please select a table in Reference tables first!")();
            return;
        }
        //let chosenTableName =  chosenTableNameList[0];
        getQueryFieldNames(processQueryChooseFieldsButtonCallback);     
    }
    
    function processQueryChooseFieldsButtonCallback(fieldNames) {
        let body = createChooserBodyContent(fieldNames,"chooser1");
        
        let buttonList = [ 
                    {                
                        id: "chooser-button-cancel",
                        text: "Cancel",
                        click: queryChooserCancelButtonHander
                    },
                    {
                        id: "chooser-button-format",
                        text: "FORMAT",
                        disabled: true,
                        click: queryChooserFormatButtonHander
                        
                    }  
                    ,
                    {
                        id: "chooser-button-ok",
                        text: "Ok",
                        disabled: true,
                        click: queryChooserOKButtonHander
                        
                    }     
                ];
                
        // set up the final callback for the OK button..       
        let data = {resultData:"chooser1", okCallback: selectedFieldsNormalInsert, okCallbackData:"queryChooserTableFieldNames"};   
        
        initialiseChooserSelectionData(chooserData,"chooser1","chooser-button-ok","chooser-button-format");
        multiChooserDialog(chooserData,"confirmDialog","Select one or more table fields",body,buttonList,data,false)();
    }

    function queryChooserCancelButtonHander() {     
        $(this).dialog("close");
    }
    
    function queryChooserOKButtonHander() {
        // where to put the query selection data, and determine how that data was selected
        let queryBaseData = $(this).data("maindata").resultData;
        let callback = $(this).data("maindata").okCallback;
        let callbackData = $(this).data("maindata").okCallbackData;
        
        callback(queryBaseData, callbackData);              
        
        $(this).dialog("close");
    }
    
    function queryChooserFormatButtonHander() {
        
        let queryBaseData = $(this).data("maindata").resultData;
        let callback = $(this).data("maindata").okCallback;
        let callbackData = $(this).data("maindata").okCallbackData;
        
        let chosenTableNameList = getSingleItemValueWithClass("queryTableItemSelected");
       
        if (chosenTableNameList === null) {
            standardOK("confirmDialog2", "Error","Please select a table in Reference tables first!")();
            return;
        }
        
        let fieldList = getChooserSelectionData(chooserData,queryBaseData,callbackData);

        if (fieldList === null || fieldList === undefined || fieldList.length !== 1) {
            standardOK("confirmDialog2", "Error","Please select one field at a time to format.")();
            return;
        }
        else {
            let body = "<div class='fullblockWrapper'>Table: " + chosenTableNameList[0] + " Field:" + fieldList[0] + "</div>";
            body += "<div class='fullblockWrapper'>" + "Prefix text:" + "<input id='queryPrefixText' type='text' class='textInput' data-target='chooser2'>" + "</div>";
            body += "<div class='fullblockWrapper'>" + "Postfix text:" + "<input id='queryPostfixText' type='text' class='textInput' data-target='chooser2'>" + "</div>";
            
            let buttonList = [ 
                        {                
                            id: "format-chooser-button-cancel",
                            text: "Cancel",
                            click: queryChooserCancelButtonHander
                        },
                        {
                            id: "format-chooser-button-ok",
                            text: "Ok",
                            disabled: true,
                            click: queryChooserOKButtonHander

                        }     
                    ];

            // set up the final callback for the OK button..       
            let data = {resultData:"chooser2",okCallback: formatOKHandler, okCallbackData:[chosenTableNameList[0],fieldList[0]]};   
            
            initialiseChooserSelectionData(chooserData,"chooser2","format-chooser-button-ok",null);
            multiChooserDialog(chooserData,"confirmDialog2","Format table field selection",body,buttonList,data,true)();
        }

    }
    
    function formatOKHandler(queryBaseData, data) {

        let tableName = data[0];
        let fieldList = [data[1]]; // only one element
        let prefixVal = $("#queryPrefixText").val();
        let postfixVal = $("#queryPostfixText").val();
        let isFormatMarkup = true;
        
        getQueryMarkup(queryMarkupModeFieldSelectionNormal,fieldList,formatOKHandlerCallback,isFormatMarkup,prefixVal,postfixVal,false,false);
        
        $("#confirmDialog2").dialog("close");
        
        // close the parent (which is still open!)
        $("#confirmDialog").dialog("close");
    }
    
    function formatOKHandlerCallback(fieldMarkup) {
        $("#" + "querySelectedFields").val($("#" + "querySelectedFields").val() + fieldMarkup);
        
        $("#confirmDialog2").dialog("close");
        
        // close the parent (which is still open!)
        $("#confirmDialog").dialog("close");
    }
    
    function processQueryChooseLeftJoinButton() {
        let chosenTableNameList = getSingleItemValueWithClass("queryTableItemSelected");
       
        if (chosenTableNameList === null) {
            standardOK("confirmDialog", "Error","Please select a table in Reference tables first!")();
            return;
        }
        //let chosenTableName =  chosenTableNameList[0];
        getQueryFieldNames(processQueryChooseLeftJoinButtonCallback);     
        
    }
    
    function processQueryChooseRightJoinButton() {
        let chosenTableNameList = getSingleItemValueWithClass("queryTableItemSelected");
       
        if (chosenTableNameList === null) {
            standardOK("confirmDialog", "Error","Please select a table in Reference tables first!")();
            return;
        }
        //let chosenTableName =  chosenTableNameList[0];
        getQueryFieldNames(processQueryChooseRightJoinButtonCallback);     
        
    }
    
    function processQueryChooseLeftJoinButtonCallback(fieldNames) {
        let body = createChooserBodyContent(fieldNames,"chooser1");
        
        let buttonList = [ 
                    {                
                        id: "chooser-button-cancel",
                        text: "Cancel",
                        click: queryChooserCancelButtonHander
                    },                  
                    {
                        id: "chooser-button-ok",
                        text: "Ok",
                        disabled: true,
                        click: queryChooserOKButtonHander
                        
                    }     
                ];
                
        // set up the final callback for the OK button..       
        let data = {resultData:"chooser1", okCallback: selectedFieldLeftJoinInsert, okCallbackData:"queryChooserTableFieldNames"};   
        
        initialiseChooserSelectionData(chooserData,"chooser1","chooser-button-ok",null);
        singleChooserDialog(chooserData,"confirmDialog","Select one field for left join.",body,buttonList,data,false)();
    }
    
    function processQueryChooseRightJoinButtonCallback(fieldNames) {
        let body = createChooserBodyContent(fieldNames,"chooser1");
        
        let buttonList = [ 
                    {                
                        id: "chooser-button-cancel",
                        text: "Cancel",
                        click: queryChooserCancelButtonHander
                    },                  
                    {
                        id: "chooser-button-ok",
                        text: "Ok",
                        disabled: true,
                        click: queryChooserOKButtonHander
                        
                    }     
                ];
                
        // set up the final callback for the OK button..       
        let data = {resultData:"chooser1", okCallback: selectedFieldRightJoinInsert, okCallbackData:"queryChooserTableFieldNames"};   
        
        initialiseChooserSelectionData(chooserData,"chooser1","chooser-button-ok",null);
        singleChooserDialog(chooserData,"confirmDialog","Select one field for right join.",body,buttonList,data,false)();
    }

    function selectedFieldLeftJoinInsert(baseChooser, chooserItem) {
        let fieldList = getChooserSelectionData(chooserData,baseChooser,chooserItem);
        getQueryMarkup(queryMarkupModeJoin,fieldList,selectedFieldLeftJoinInsertCallback,false,null,null,true,false);
        toggleButton("queryChooseLeftJoinButton");
        toggleButton("queryChooseRightJoinButton");
        queryJoinCount++;
    }
    
    function selectedFieldRightJoinInsert(baseChooser, chooserItem) {
        let fieldList = getChooserSelectionData(chooserData,baseChooser,chooserItem);
        getQueryMarkup(queryMarkupModeJoin,fieldList,selectedFieldRightJoinInsertCallback,false,null,null,false,$("#queryJoinMultivaluedCheckbox").is(":checked"));
        toggleButton("queryChooseLeftJoinButton");
        toggleButton("queryChooseRightJoinButton");
        queryJoinCount++;
    }
    
    function selectedFieldLeftJoinInsertCallback(markup) {
        //$("#" + "queryJoinFields").val($("#" + "queryJoinFields").val() + markup);
        $("#" + "queryJoinFieldsTemp").val(markup);
    }
    
    function selectedFieldRightJoinInsertCallback(markup) {
        //$("#" + "queryJoinFields").val($("#" + "queryJoinFields").val() + markup);
        $("#" + "queryJoinFieldsTemp").val($("#" + "queryJoinFieldsTemp").val() + markup);
        getQuerySpecificOuterMarkup(selectedFieldRightJoinInsertCallback2,"JOIN",$("#" + "queryJoinFieldsTemp").val());
    }  
    
    function getQuerySpecificOuterMarkup(callback, queryType, queryText) {
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            data : {
                queryType: queryType, 
                queryText: queryText,
                mode: 'getQuerySpecificOuterMarkup'
            },
            success : function(response) {
                let markup = response['result'];
                let status = response['status'];
                if (status !== "OK") {
                    standardOK("confirmDialog", "Error",status)();
                }
                else {
                    callback(markup);
                }
            }
            
        });                
    }
    
    function selectedFieldRightJoinInsertCallback2(markup) {
        $("#" + "queryJoinFields").val($("#" + "queryJoinFields").val() + markup);
    }
    
    function processQueryClearFieldsButton() {
        $("#" + "querySelectedFields").val("");
    }
    
    function processQueryChooseClearJoinsButton() {
        $("#" + "queryJoinFields").val("");
    }

    function selectedFieldsNormalInsert(baseChooser, chooserItem) {  
        let fieldList = getChooserSelectionData(chooserData,baseChooser,chooserItem);
        getQueryMarkup(queryMarkupModeFieldSelectionNormal,fieldList,selectedFieldsNormalInsertCallback,false,null,null,false,false);
    }
      
    function selectedFieldsNormalInsertCallback(fieldMarkup) {
        $("#" + "querySelectedFields").val($("#" + "querySelectedFields").val() + fieldMarkup);
    }
    
    function processQueryInsertContextVariableButton() {
        let contextVar = getSelectorValue("queryContextVariable");
        getQueryContextVarSelectorMarkup(contextVar,selectedFieldsNormalInsertCallback);
    }
      
    function getQueryContextVarSelectorMarkup(contextVarName,callback) {
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            data : {
                contextVar: contextVarName, 
                currentMarkup: $("#querySelectedFields").val(),
                mode: 'getQueryContextVarSelectorMarkup'
            },
            success : function(response) {
                let markup = response['result'];
                let status = response['status'];
                if (status !== "OK") {
                    standardOK("confirmDialog", "Error",status)();
                }
                else {
                    callback(markup);
                }
            }
            
        });            
    }
    
    function processQueryChooseOrderByButton() {
        let chosenTableNameList = getSingleItemValueWithClass("queryTableItemSelected");
       
        if (chosenTableNameList === null) {
            standardOK("confirmDialog", "Error","Please select a table in Reference tables first!")();
            return;
        }       
        getQueryFieldNames(processQueryChooseOrderByButtonCallback);     
    }
    
    function processQueryChooseOrderByButtonCallback(fieldNames) {
        let body = createChooserBodyContent(fieldNames,"chooser1");
        
        let buttonList = [ 
                    {                
                        id: "chooser-button-cancel",
                        text: "Cancel",
                        click: queryChooserCancelButtonHander
                    },
                    /*{
                        id: "chooser-button-format",
                        text: "FORMAT",
                        disabled: true,
                        click: queryChooserFormatButtonHander
                        
                    }  
                    ,*/
                    {
                        id: "chooser-button-ok",
                        text: "Ok",
                        disabled: true,
                        click: queryChooserOKButtonHander
                        
                    }     
                ];
                 
        let data = {resultData:"chooser1",okCallback: selectedFieldsOrderBy, okCallbackData:"queryChooserTableFieldNames"};   
        
        initialiseChooserSelectionData(chooserData,"chooser1","chooser-button-ok");
        singleChooserDialog(chooserData,"confirmDialog","Select one table field to order by",body,buttonList,data,false)();
 
    }
    
    function selectedFieldsOrderBy(baseChooser, chooserItem) {
        let fieldList = getChooserSelectionData(chooserData,baseChooser,chooserItem);
        getQueryMarkup(queryMarkupModeOrder,fieldList,selectedFieldsNormalInsertCallback,false,null, null,false,false);  
        
    }
      
    function getQueryMarkup(queryMode, selectedFieldList,callback, isFormatMarkup,prefixData,postfixData, isLeftJoin, isMultivalued) {
  
        switch (queryMode) {
            case queryMarkupModeJoin: 
            case queryMarkupModeOrder: 
            case queryMarkupModeFieldSelectionNormal: 
              
                $.ajax({
                    url : 'KnowledgeAcquisitionServlet',
                    dataType:'json',
                    data : {
                        fieldList : JSON.stringify(selectedFieldList),
                        countSelected: $("#queryCountCheckbox").is(":checked"),
                        currentMarkup: $("#querySelectedFields").val(),
                        queryMode: queryMode,
                        isFormatMarkup: isFormatMarkup,
                        prefixData: prefixData,
                        postfixData: postfixData,
                        isLeftJoin: isLeftJoin,
                        multivalued: isMultivalued,                   
                        mode: 'getQueryMarkup'
                    },
                    success : function(response) {
                        let markup = response['result'];
                        let status = response['status'];
                        if (status !== "OK") {
                            standardOK("confirmDialog", "Error",status)();
                        }
                        else {
                            callback(markup);
                        }
                    }
                });  
                break; 
            }
    }
    
    function setSelectedTableID(theID,callback) {
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            data : {
                tableID : theID,
                mode: 'setSelectedTableID'
            },
            success : function() {               
                callback();
            }

         });             
    }
   
    function getQueryTableNames(target,callback) {
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            data : {
                mode : 'getQueryTableNames'
            },
            success : function(responseJSON) {
                let extraStyles = "";
                
                $.each(responseJSON,function(index, anItem) {
                    if (index%2 === 1)
                        extraStyles = "tableAlt1";
                    else
                        extraStyles = "tableAlt2";
                    
                    $("#" + target).append("<div id='queryTableItem" + index + "' class='queryTableItem " + extraStyles + "'>" + anItem + "</div>");  
                });
                callback();
            }

         });             
    }
    
    function getQueryFieldNames(callback) {
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            data : {
                mode : 'getQueryFieldNames'
            },
            success : function(responseJSON) {
                let extraStyles = "";
                let i = 0;
                let fieldNames = [];
                
                $.each(responseJSON,function(index, aField) {
                    fieldNames.push(aField);
                });
                callback(fieldNames);
            }

         });             
    }
    
    function processQuerySaveButton() {
        if (queryJoinCount%2 !== 0) {
            standardOK("confirmDialog", "Error","There is an issue with the join conditions - a right join condition to match a left join condition is missing!")();
        }
        else {
            let selectedFields = $("#querySelectedFields").val();
            let joinFields = $("#queryJoinFields").val();
            let criteriaFields = $("#queryCriteriaFields").val();
            let description = $("#queryDescription").val();
            let queryText = selectedFields + joinFields + criteriaFields;
            
            let okToGo = true;
            
            if (selectedFields === "") {
                standardOK("confirmDialog", "Error","You need to specify at least one Selected Field to use in the query")();
                okToGo = false;
            }
            if (criteriaFields === "") {
                standardOK("confirmDialog", "Error","You need to specify some criteria to limit the results of the query")();
                okToGo = false;
            }
            if (description === "") {
                standardOK("confirmDialog", "Error","You need to specify a description for the query before it is saved..!")();
                okToGo = false;
            }
            
            if (okToGo) {
                setSavedQuery(processQuerySaveButtonCallback,queryText,description);               
            } 
        }
    }
    
    function setSavedQuery(callback,queryText,description) {
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            data : {
                queryText : queryText,   
                queryDescription: description,
                mode: 'setSavedQuery'
            },
            success : function(response) {
                let markup = response['result'];
                let status = response['status'];
                if (status !== "OK") {
                    standardOK("confirmDialog", "Error",status)();
                }
                else {
                    callback(markup);
                }
            }
        });  
    }
    
    function processQuerySaveButtonCallback(markup) {
        //updateMetaDataInterface(null);
        //showAndHide(getQuerySiblingMain(),getQueryMain(),true);
        standardOK("confirmDialog", "Notice",markup)();
    }
    
    //function processQueryCancelButton() {
        //updateMetaDataInterface(null);
        //showAndHide(getQuerySiblingMain(),getQueryMain(),true);
    //}
    
    function processQueryPreviewButton() {
        let fields = $("#querySelectedFields").val();
        let joins = $("#queryJoinFields").val();
        let criteria = $("#queryCriteriaFields").val();
        let queryText = fields + joins + criteria;
        if (fields !== "") {
            getQueryPreviewText(processQueryPreviewButtonCallback,queryText);
        }
        else {
            standardOK("confirmDialog", "Error","You must at least choose fields in Selected Fields before attempting to preview..!")();
        }
    }
    
    function processQueryPreviewButtonCallback(previewText) {
        standardOK("confirmDialog", "Preview",previewText)();
    }
    
    function getQueryPreviewText(callback, queryText) {
  
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            data : {
                queryText : queryText,              
                mode: 'getQueryPreviewText'
            },
            success : function(response) {
                let markup = response['result'];
                let status = response['status'];
                if (status !== "OK") {
                    standardOK("confirmDialog", "Error",status)();
                }
                else {
                    callback(markup);
                }
            }
        });  
    }
    
    function processQueryChooseCriteriaButton() {
        let chosenTableNameList = getSingleItemValueWithClass("queryTableItemSelected");
       
        if (chosenTableNameList === null) {
            standardOK("confirmDialog", "Error","Please select a table in Reference tables first!")();
            return;
        }
        //let chosenTableName =  chosenTableNameList[0];
        getQueryFieldNames(processQueryChooseCriteriaButtonCallback);         
    }
    
    function processQueryChooseCriteriaButtonCallback(fieldList) {
        let body = createChooserBodyContent(fieldList,"chooser1");
        
        let buttonList = [ 
                    {                
                        id: "chooser-button-cancel",
                        text: "Cancel",
                        click: queryChooserCancelButtonHander
                    },                  
                    {
                        id: "chooser-button-ok",
                        text: "Ok",
                        disabled: true,
                        click: queryCriteriaOKButtonHander
                        
                    }     
                ];
                
        // set up the final callback for the OK button..       
        let data = {resultData:"chooser1", okCallback: queryCriteriaOKButtonHander, okCallbackData:"queryChooserTableFieldNames"};   
        
        initialiseChooserSelectionData(chooserData,"chooser1","chooser-button-ok",null);
        singleChooserDialog(chooserData,"confirmDialog","Select one field for criteria matching.",body,buttonList,data,false)();    
    }
    
    function queryCriteriaOKButtonHander() {
        let queryBaseData = $(this).data("maindata").resultData;
        let callback = $(this).data("maindata").okCallback;  // we were called directly but the button, so this is not used
        let callbackData = $(this).data("maindata").okCallbackData; 
        
        let chosenTableNameList = getSingleItemValueWithClass("queryTableItemSelected");
       
        if (chosenTableNameList === null) {
            standardOK("confirmDialog2", "Error","Please select a table in Reference tables first!")();
            return;
        }
        
        let fieldList = getChooserSelectionData(chooserData,queryBaseData,callbackData);

        if (fieldList === null || fieldList === undefined || fieldList.length !== 1) {
            standardOK("confirmDialog2", "Error","Please select one field at a time to format.")();
            return;
        }
        else {
            let body = "<div class='fullWidthHeader'><span class='leftSpan'>Table: " + chosenTableNameList[0]+"</span>" + "<span class='leftSpan'>Field: " + fieldList[0] + "</span></div>";
            //body += "<div class='fullblockWrapper'>Table: " + chosenTableNameList[0] + " Field:" + fieldList[0] + "</div>";
            //body += "<div class='column'> ";
                body += "<div class='fullWidthWrapper'>" + "<input type='checkbox' id='queryAllowPartialMatchCheckbox' name='queryAllowPartialMatch' value='1'><span class='rightSpan'>Allow partial match</span>" + "</div>";
                body += "<div class='fullWidthHeader'>" + "<span class='leftSpan'>Criterion matching value selection:" + "<span></div>";
                body += "<div class='fullWidthWrapper useBorder'>" ;
                    body += "<div class='fullWidthWrapper'>" + "<input id='queryCriteriaMatch1' type='radio' name='queryCriteriaMatchRadioGroup' value='1' class='criteriaRadio' data-target='chooser2'><span class='leftSpan'>Fixed value</span>" + "</div>";
                    body += "<div class='fullWidthWrapper'>" + "<input id='queryCriteriaMatch2' type='radio' name='queryCriteriaMatchRadioGroup' value='2' class='criteriaRadio' data-target='chooser2'><span class='leftSpan'>Fixed value with context variable</span>" + "</div>";
                    body += "<div class='fullWidthWrapper'>" + "<input id='queryCriteriaMatch3' type='radio' name='queryCriteriaMatchRadioGroup' value='3' class='criteriaRadio' data-target='chooser2'><span class='leftSpan'>Fixed value with number from context variable</span>" + "</div>";
                    body += "<div class='fullWidthWrapper'>" + "<input id='queryCriteriaMatch4' type='radio' name='queryCriteriaMatchRadioGroup' value='4' class='criteriaRadio' data-target='chooser2'><span class='leftSpan'>Context variable</span>" + "</div>";
                    body += "<div class='fullWidthWrapper'>" + "<input id='queryCriteriaMatch5' type='radio' name='queryCriteriaMatchRadioGroup' value='5' class='criteriaRadio' data-target='chooser2'><span class='leftSpan'>Number from context variable</span>" + "</div>";
                body += "</div>";
                body += "<div class='fullWidthWrapper'><span class='leftSpan'>Fixed value: </span>" + "<input id='queryFixedCriteriaValue' type='text' class='textInput' data-target='chooser2' disabled>" + "</div>";
                body += "<div class='fullWidthWrapper'><span class='leftSpan'>Context variable:</span>" + "<select id='queryContextCriteriaValue' class='KAinputLeft' disabled></select>" + "</div>";
            //body += "</div>";
            getContextVariables("queryContextCriteriaValue",noopCallback);
            let buttonList = [ 
                        {                
                            id: "criteria-chooser-button-cancel",
                            text: "Cancel",
                            click: queryChooserCancelButtonHander
                        },
                        {
                            id: "criteria-chooser-button-ok",
                            text: "Ok",
                            disabled: true,
                            click: queryChooserOKButtonHander

                        }     
                    ];

            // set up the final callback for the OK button..       
            let data = {resultData:"chooser2",okCallback: criteriaOKHandler, okCallbackData:[chosenTableNameList[0],fieldList[0]]};   
            
            initialiseChooserSelectionData(chooserData,"chooser2","criteria-chooser-button-ok",null);
            multiChooserDialog(chooserData,"confirmDialog2","Criterion creation",body,buttonList,data,true)();
       
        
        }
        
    }
    
    function criteriaOKHandler(queryBaseData, data) {

        let tableName = data[0];
        let fieldList = [data[1]]; // only one element
        let radioSelected = getChooserSelectionData(chooserData,queryBaseData,"queryCriteriaRadioSelection");
        let fixedValue = $("#queryFixedCriteriaValue").val();
        let contextVar = getSelectorValue("queryContextCriteriaValue");
        let partialMatch = $("#queryAllowPartialMatchCheckbox").is(":checked");
        
        getQueryCriterionMarkup(criteriaOKHandlerCallback, fieldList,fixedValue,contextVar,partialMatch,radioSelected);
        
        
    }
    
    function criteriaOKHandlerCallback(markup) {
        $("#queryCriteriaFields").val($("#queryCriteriaFields").val() + markup);
        $("#confirmDialog2").dialog("close");
        
        // close the parent (which is still open!)
        $("#confirmDialog").dialog("close");
    }
    
    function getQueryCriterionMarkup(callback, fieldList,fixedValue,contextVar,partialMatch,radioSelected) {
  
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            data : {
                fieldList : JSON.stringify(fieldList),
                partialMatch: partialMatch,
                fixedValue: fixedValue,  
                contextVar: contextVar,
                radioSelection: radioSelected,
                mode: 'getQueryCriterionMarkup'
            },
            success : function(response) {
                let markup = response['result'];
                let status = response['status'];
                if (status !== "OK") {
                    standardOK("confirmDialog", "Error",status)();
                }
                else {
                    callback(markup);
                }
            }
        });  
    }
    
    function processQueryClearCriteriaButton() {
        $("#queryCriteriaFields").val("");
    }
    