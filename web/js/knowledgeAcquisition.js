/* ************************** Knowledge Acquisition **************************  */
    var knowledgeAcquisitionMain = "knowledgeMainWrapper";
    
    function getKnowledgeAcquisitionMain() {
        return knowledgeAcquisitionMain;
    }
    
    var knowledgeAcquisitionEventListenersRegistered = false;
    
    function knowledgeAcquisitionAddEventListeners() {
        if (!knowledgeAcquisitionEventListenersRegistered) {
            $('body').on('change','#KAAttributeSelector', function(e) {   
                populateCaseAttributeOperatorSelector($("#KAAttributeSelector option:selected").text());
                updateField("KAConditionCriteria", $("#KAAttributeSelector option:selected").val());
            });

            $('body').on('click','#KAAddConditionButton', function(e) {   
                addCompletedConditionLine();
            });

            $('body').on('change','#KAConditionCriteria', function(e) {
                if ($("#KAConditionCriteria").val() !== "") 
                    $("#KAAddConditionButton").prop('disabled', false);
                else
                    $("#KAAddConditionButton").prop('disabled', true);
            });  

            $('body').on('click','#KAsubmitButton', function(e) {
                askToSubmitNewRule();  
            });
            
            $('body').on('click','.conditionline', function(e) {       
                let theID = 0;  
                if ($(e.target).is('.KAconditionAttribute'))
                    theID = e.target.id.toString().substring(9);
                else if ($(e.target).is('.KAconditionType'))
                    theID = e.target.id.toString().substring(4);
                else if ($(e.target).is('.KAconditionValue'))
                    theID = e.target.id.toString().substring(5);
                else if ($(e.target).is('.conditionline'))
                    theID = e.target.id.toString().substring(13);

                let theItem = $("#attribute" + theID).html();
                let theValue = $("#value" + theID).html();

                updateSelector("KAAttributeSelector",theItem);
                updateField("KAConditionCriteria",theValue);
                populateCaseAttributeOperatorSelector($("#KAAttributeSelector option:selected").text());

            });
            
            $('body').on("change","#KAisStoppingRule",function(e){
                let isStopped = $("#KAisStoppingRule").is(":checked");

                let numOfConditions = $(".conditionSubmitLine").length;
                if (numOfConditions > 0 && isStopped) {
                    standardOK("confirmDialog","ERROR","You cannot select 'Make stopping rule' if you have already added conditions..")();
                    disableItem('KAsubmitButton');   
                }
                else if (numOfConditions > 0 && !isStopped) {
                    enableItem('KAsubmitButton');
                }
                else if (numOfConditions === 0 && isStopped) {
                    enableItem('KAsubmitButton');
                }
                
                else if (numOfConditions === 0 && !isStopped) {
                     disableItem('KAsubmitButton');   
                }
                
            });
            
            knowledgeAcquisitionEventListenersRegistered = true;
            
        }
    }
    
    /* starting point for conclusion selection.. */
    function createKnowledgeAcquisitionInterface() {
        dconsole("createKnowledgeAcquisitionInterface - ask for conclusions etc");   
        
        let mainDiv = getKnowledgeAcquisitionMain();

        if ($("#" + mainDiv).length)
            $("#" + mainDiv).remove();
        
        // the whole knowledge acquisition div
        $("body").append("<div id='" + mainDiv +  "'></div>");
        
        $("#" + mainDiv).append("<div id='divKnowledgeAcquisition'></div>");
        
        showAndHide(mainDiv,"mainContentDivInferenceViewer",false,false);
        
        $("#divKnowledgeAcquisition").append("<div id='KAhistoryWrapper'></div");
        dialogHistory("KAhistoryWrapper");
        
        
        
        $("#divKnowledgeAcquisition").append("<div id='KAcontentWrapper'></div");
              
        $("#KAcontentWrapper").append("<div id='KAConclusionsWrapper'></div");
        createConclusionInterface("KAConclusionsWrapper");
        setStatusMainContent("KAConclusionsWrapper");
        
        $("#divKnowledgeAcquisition").append("<div id='KAmetaDataWrapper'></div");
        createMetaDataInterface("KAmetaDataWrapper","KAmetaData",true);

        
        $("#divKnowledgeAcquisition").append("<div id='KAnavigationWrapper'></div");
        $("#KAnavigationWrapper").append('<div class="columnHeading">Navigation Menu</div>');
        
        addBackButton("Return to inference viewer",
                        "KAnavigationWrapper",
                        "divKnowledgeAcquisitionBackButton",
                            {requestedDiv:"mainContentDivInferenceViewer",
                            hideDiv: "divKnowledgeAcquisition",
                            clear:true, 
                            removeSelf:true, 
                            removeOthers:true,
                            registerContent:false});
                        
        knowledgeAcquisitionAddEventListeners();                
    }
    
    // move on to constructing conditions..
    function knowledgeAcquisitionConclusionSubmittedCallback() {
        createRuleConditionInterface();
    }
 

/* ************************** Global status        **************************  */
    // The last (immediate) system response - was it valid?
    function getStatusIsValidMostRecentSystemResponse() {
        return currentStatus.isValidMostRecentSystemResponse;
    }
    // The last (immediate) system response
    function getStatusMostRecentSystemResponseString() {
        return currentStatus.mostRecentSystemResponseString;
    }    
    function getStatusMostRecentDialogID() {
        return currentStatus.mostRecentDialogID;
    }
    
    function getStatusIsValidPastSystemResponse() {
        return currentStatus.isValidPastSystemResponse;
    }
    // The most recent valid system response (if any) found.
    // This may not have been the last (immediate) system response..
    function getStatusPastSystemResponseString() {
        return currentStatus.pastSystemResponseString;
    }
    
    
    function getStatusConstructNewCondition() {
        return currentStatus.constructNewCondition;
    }
    function setStatusRegisterToBeDeleted(item) {
        currentStatus.registerToBeDeleted.push(item);
    }
    function getStatusRegisterToBeDeleted() {
        return currentStatus.registerToBeDeleted;
    }
    function resetStatusRegisterToBeDeleted() {
        currentStatus.registerToBeDeleted = [];
    }
    function getStatusMainContent() {
        return currentStatus.KAmainContent;
    }
    function setStatusMainContent(content) {
        currentStatus.KAmainContent = content;
    }

 /* ************************** Async servlet calls      **************************  */   
    
    function getIsValidMostRecentSystemResponse(callback) {
        var isValid = false;
    
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getIsValidMostRecentSystemResponse',
                username: getSelectedUserName()
            },
            success : function(responseText) {
                isValid = (responseText === "true");
                //currentStatus.isValidMostRecentSystemResponse = isValid;
                callback(isValid);
            }
         });
         
         //return isValid;
    }
          
    function getMostRecentSystemResponseString(callback) {
        var systemResponse = "";
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getMostRecentSystemResponseString',
                username: getSelectedUserName()
            },
            
            success : function(response) {
                //systemResponse = response;
                //currentStatus.mostRecentSystemResponseString = systemResponse;
                callback(response);
            }
        });
        
    }

    function getMostRecentUserDialogID(callback) {
        var id = "-1";
        
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getMostRecentUserDialogID',
                username: getSelectedUserName()
            },
            
            success : function(response) {
                id = response;
                currentStatus.mostRecentDialogID = id;
                callback();
            }
        });
        
        //return id;
    }
    
    function getMostRecentSystemDialogID(callback) {
        var id = "-1";
        
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getMostRecentSystemDialogID',
                username: getSelectedUserName()
            },
            
            success : function(response) {
                id = response;
                currentStatus.dialogIdKA = id;  // Setting for metadata use...
                callback();
            }
        });
        
        //return id;
    }

    function getIsValidPastSystemResponse(callback, currentIsValid) {
        var isValid = false;
    
        $.ajax({
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getIsValidPastSystemResponse',
                username: getSelectedUserName()
            },
            success : function(responseText) {
                isValid = (responseText === "true");
                
//                if (responseText === "false") 
//                    isValid = false;
//                else
//                    isValid = true;
//                
//                currentStatus.isValidPastSystemResponse = isValid;
                callback(isValid,currentIsValid);
            }
         });
    }
    
    function getPastSystemResponseString(callback,currentIsValid) {
        var sysResponse = "";
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getPastSystemResponseString',
                username: getSelectedUserName()
            },
            
            success : function(response) {
                //sysResponse = response;
                //currentStatus.pastSystemResponseString = sysResponse;
                callback(response,currentIsValid);
            }
        });
    }
    
    function getInferenceResult(callback) {
        $.ajax({
                type: 'GET',
                url : 'KnowledgeAcquisitionServlet',

                data : {
                    mode : 'getInferenceResult',
                    username: getSelectedUserName()
                },
                
                success : function(response) {
                    if (response === "true")
                        callback();
                }
        });
    }
    
    function generateCaseForKnowledgeAcquisition(callback, caseMode) {
        $.ajax({
                type: 'GET',
                url : 'KnowledgeAcquisitionServlet',

                data : {
                    mode : 'generateCaseForKnowledgeAcquisition',
                    caseMode : caseMode, 
                    username: getSelectedUserName(),
                    dialogID: getStatusMostRecentDialogID()
                },
                
                success : function(response) {
                    if (response === "true")
                        callback();
                }
        });
    }
    
    // note must have generated a case (generateCaseForKnowledgeAcquisition) prior to calling
    function prepareLearner(callback) {
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'prepareLearner'
            },
            
            success : function(response) {
                if (response === "true")
                        callback();
            }
        });
    }   
    
    function submitRule(callback,isStoppingRule,doNotStack) {
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'submitRule',
                isStoppingRule: isStoppingRule,
                doNotStack: doNotStack                          
            },
            
            success : function(response) {
                if (response === "true")
                    callback();
            }
        });      
    }
    
    function constructNewCondition(callback, anAttribute, anOperator, aValue, isInitial) {
        //var result = false;
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'constructNewCondition',
                attribute: anAttribute,
                operator: anOperator,
                value: aValue,
                initial: isInitial
            },
            
            success : function(response) {
                result = response;
                currentStatus.constructNewCondition = result;
                callback();
            }
        });  
        
        //return result;
    }
    
    function getCaseAttributes(callback,parentDiv,selector) {
        $.ajax({
                type: 'GET',
                url : 'KnowledgeAcquisitionServlet',
                dataType:'json',
                //async: false,
                data : {
                    mode : 'getCaseAttributes'
                },

                success : function(response) {
                    let extraStyles = "";
                    let i = 0;
                    
                    resetSelector(selector);
                    let attributeValues = response['attributeValues'];
                    $.each(attributeValues,function(index, anAttribute) {                                         
                        let attribute = index;
                        let attributeType = "TEXT";
                        let attributeValue = anAttribute['value'];
                        
                        if (attributeValue === "")
                            attributeValue = "&nbsp;";


                        if (i%2 === 1)
                            extraStyles = "conditionAlt1";
                        else
                            extraStyles = "conditionAlt2";
                        
                        // ignore history as it's unused in the implementation 
                        if (attribute !== "History") {
                            
                            addItemToSelector(selector, attributeValue, attribute);
                            
                            $("#" + parentDiv).append("<div id='conditionline" + i + "' class='conditionline " + extraStyles + "'></div>");
                                $("#conditionline" + i).append("<div id='attribute" + i +"' class='KAconditionAttribute'>"+attribute+"</div>");     
                                $("#conditionline" + i).append("<div id='type" + i +"' class='KAconditionType'>"+attributeType+"</div>");
                                $("#conditionline" + i).append("<div id='value" + i +"' class='KAconditionValue'>"+attributeValue+"</div>");
                            i += 1;
                        }
                    });
                    callback();
                }
        });           
    }
    
    function populateCaseAttributeOperatorSelector(theAttribute) {
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            //async: false,
            data : {
                mode : 'getAttributeOperators',
                attribute: theAttribute
            },
            
            success : function(response) {
                $("#KAOperatorSelector").html("");
                             
                $.each(response,function(index, operatorData) {
                    $("#KAOperatorSelector").append("<option value='" + operatorData + "'>" + operatorData + "</option>");                  
                });
            }
        }); 
        
    }   

    function createMetaDataInterface(parent,theClass, showHeading) {
        
        if (showHeading) // we might be in a dialog which already has a heading..
            $("#" +parent).append("<div class='columnHeading'>Metadata management</div>");
        $("#" +parent).append("<div id='metaDataInterface' class='" + theClass + "'></div>");
        //$("#metaDataInterface").append("<div class='columnHeading'>Metadata management</div>");
        $("#metaDataInterface").append("<input id='KAqueryBuilderButton' type='button' class='inlineButtonSmallFloatLeft' value='Query builder'>");
        $("#metaDataInterface").append("<input id='KAdictionaryButton' type='button' class='inlineButtonSmallFloatLeft' value='Dictionary'>");
        $("#metaDataInterface").append("<input id='KAuserVariablesButton' type='button' class='inlineButtonSmallFloatLeft' value='User variables'>");
        $("#metaDataInterface").append("<input id='KAsystemVariablesButton' type='button' class='inlineButtonSmallFloatLeft' value='System variables'>");
        $("#metaDataInterface").append("<input id='KApreprocessorButton' type='button' class='inlineButtonSmallFloatLeft' value='Preprocessor'>");
        $("#metaDataInterface").append("<input id='KApostprocessorButton' type='button' class='inlineButtonSmallFloatLeft' value='Postprocessor'>");
        $("#metaDataInterface").append("<input id='KAresetKBButton' type='button' class='inlineButtonSmallFloatLeft' value='Reset KB'>");
    }
    const metDataInterfaceButtons = ['KAqueryBuilderButton','KAdictionaryButton','KAuserVariablesButton','KAsystemVariablesButton'];
    
    // disable the current functional button so it can't be reselected while the 
    // associated display div is active
    function updateMetaDataInterface(buttonToHide) {
        if (buttonToHide !== null) {
            disableItem(buttonToHide);          
        }
        $.each(metDataInterfaceButtons, function(index, value) {
           if (value !== buttonToHide) {
               enableItem(value);
           } 
        });
    }
    
    // Construct the currently-selected user's dialog history
    function dialogHistory(parentDiv) {
        $("#" + parentDiv).append('<div class="columnHeading">Dialog history</div>');
        $("#" + parentDiv).append("<div id='KAhistory'></div");

        processGetDialogHistory('KAhistory',"KAdialog",dialogHistoryCallback);      
    }
    
    function dialogHistoryCallback() {
        addClassToItemsWithClass("bubble","itemTransparency");
        addClassToItemsWithClass("systemPre","itemTransparency");
        addClassToItemsWithClass("userPre","itemTransparency");
        
        highlightDialog("aKAdialog" + getStatusMostRecentDialogID(),'dialogHighlightSelected');
        removeClassFromItem("aKAdialog" + getStatusMostRecentDialogID(),"itemTransparency"); 
    }
    
/* ------------------------------------------------------------------------------- */
/* Knowledge Acquisition - Conclusions
/* ------------------------------------------------------------------------------- */  

/* Populate destinationDiv with a table of conclusion values */
    function populateKAConclusionList(destinationDiv) {
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            dataType:'json',
            //async: false,
            data : {
                mode : 'getKAConclusionList'
            },
            
            success : function(response) {
                $("#" + destinationDiv).html("");
                
                let extraStyles = "";
                let i = 0;
                $.each(response,function(index, conclusionData) {
                    let conclusion = conclusionData['aConclusion'];
                    let category = conclusionData['anActionCategory'];
                    let action = conclusionData['anAction'];
                    
                    if (category === "")
                        category = "&nbsp;";
                    
                    if (action === "")
                        action = "&nbsp;";
                   
                    if (i%2 === 1)
                        extraStyles = "conclusionAlt1";
                    else
                        extraStyles = "conclusionAlt2";
                    
                    $("#" + destinationDiv).append("<div id='conclusionline" + i + "' class='conclusionline " + extraStyles + "'></div>");
                        $("#conclusionline" + i).append("<div id='conclusion" + i +"' class='KAConclusion'>"+ escapeHTML(conclusion) + "</div>");     
                        $("#conclusionline" + i).append("<div id='actionCategory" + i +"' class='KAActionCategory'>"+category+"</div>");
                        $("#conclusionline" + i).append("<div id='action" + i +"' class='KAAction'>"+action+"</div>");
                    i += 1;
                });
            }
        });       
    }
      
/* ------------------------------------------------------------------------------- */
/* Knowledge Acquisition - rule conditions
/* ------------------------------------------------------------------------------- */      
    
    // condition creation
    function createRuleConditionInterface() {
        if ($("#KAConditionsWrapper").length)
            $("#KAConditionsWrapper").remove();
        
        $("#KAcontentWrapper").prepend("<div id='KAConditionsWrapper'></div");
        showAndHide("KAConditionsWrapper","KAConclusionsWrapper",false,true);

        addBackButton("<< Return to conclusions",
            //"divKnowledgeAcquisition",
            "KAnavigationWrapper",
            "divKaReturnToConclusionsBackButton2",
                {requestedDiv:"KAConclusionsWrapper",
                hideDiv: "KAConditionsWrapper",
                clear:true,
                removeSelf:true, 
                removeOthers:false,
                registerContent: true});
 
        $("#KAConditionsWrapper").append("<div class='columnHeading'>Rule condition selection</div>");
        $("#KAConditionsWrapper").append("<div id=KAcurrentAttributesTable></div>");
        $("#KAcurrentAttributesTable").append("<div class='columnHeading'>Current case attribute values</div>");

        $("#KAConditionsWrapper").append("<div id=KAselectAttributesSelectorsDiv></div>");
        $("#KAselectAttributesSelectorsDiv").append("<div class='columnHeading'>Condition selection</div>");
        $("#KAselectAttributesSelectorsDiv").append("<select id='KAAttributeSelector'></select>");
        $("#KAselectAttributesSelectorsDiv").append("<select id='KAOperatorSelector'></select>");
        $("#KAselectAttributesSelectorsDiv").append("<input id='KAConditionCriteria'>");
        $("#KAselectAttributesSelectorsDiv").append("<input id='KAAddConditionButton' type='button' value='Add' class='inlineButton navigation hbutton' disabled>");
        

        $("#KAConditionsWrapper").append("<div id=KAallSelectedConditions></div>");
        $("#KAallSelectedConditions").append("<div class='columnHeading'>All selected conditions</div>");
        $("#KAallSelectedConditions").append("<div class='tableHeader KAconditionSubmitAttribute'>Attribute</div>");
        $("#KAallSelectedConditions").append("<div class='tableHeader KAconditionSubmitOperator'>Operator</div>");
        $("#KAallSelectedConditions").append("<div class='tableHeader KAconditionSubmitCriteria'>Value</div>");
       
        // final rule submission buttons
        $("#KAConditionsWrapper").append("<div id=KAsubmissionButtons></div>");
        $("#KAsubmissionButtons").append("<input id='KAsubmitButton' type='button' value='Submit rule' class='standardRight navigation hbutton' disabled>");
        $("#KAsubmissionButtons").append("<div class='standardRight'><input type='checkbox' id='KAisStoppingRule' class='inlineCheckbox'/><span class='rightSpan'>Make stopping rule</span></div>");
        $("#KAsubmissionButtons").append("<div class='standardRight'><input type='checkbox' id='KAisDoNotStack' class='inlineCheckbox'/><span class='rightSpan'>Do not stack rule</span></div>");

        populateAttributeValuesTable(createRuleConditionInterfaceCallback,"KAcurrentAttributesTable","KAAttributeSelector");
        
    }
    
    function createRuleConditionInterfaceCallback() {
        populateCaseAttributeOperatorSelector("Recent");
        updateField("KAConditionCriteria",$("#value0").html());  // note this will not work if an attribute preceeds Recent in future..
        if ($("#KAConditionCriteria").value !== "")
            $("#KAAddConditionButton").prop('disabled', false);
    }
   
    function addCompletedConditionLine() {
        
        let isStopped = $("#KAisStoppingRule").is(":checked");
        
        if (isStopped) {
            standardOK("confirmDialog","Error","You cannot add conditions if 'Make stopping rule' is selected.  Reset and try again.")();
        }
        else {
            // Find out how many lines we already have..
            var conditionLineID = $(".conditionSubmitLine").length;

            // We need to determine if we're back here after the end user reset the interface.  If so, 
            // need to remove conditions already added server-side..
            let initial = false;
            if (conditionLineID === 0) {
                // we have no conditions in the GUI (but may have set some server-side on a previous attempt)
                initial = true;
            }


            let attribute = $("#KAAttributeSelector option:selected").text();
            let operator = $("#KAOperatorSelector").val();
            let value = $("#KAConditionCriteria").val();
            
            constructNewCondition(addCompletedConditionLineCallback, attribute,operator,value, initial);
        }
    }
 
    function addCompletedConditionLineCallback() {
        // Find out how many lines we already have..
        var conditionLineID = $(".conditionSubmitLine").length;
       
        let attribute = $("#KAAttributeSelector option:selected").text();
        let operator = $("#KAOperatorSelector").val();
        let value = $("#KAConditionCriteria").val();
        
        let conditionStatus = getStatusConstructNewCondition();
        if (conditionStatus === "true") {

            $("#KAallSelectedConditions").append("<div id='conditionSubmitLine" + conditionLineID + "' class='conditionSubmitLine'></div>");

            $("#conditionSubmitLine" + conditionLineID).append("<div id='conditionSubmitLineAttribute" + conditionLineID + "' class='KAconditionSubmitAttribute'></div>");
            $("#conditionSubmitLineAttribute" + conditionLineID).append(attribute);

            $("#conditionSubmitLine" + conditionLineID).append("<div id='conditionSubmitLineOperator" + conditionLineID + "' class='KAconditionSubmitOperator'></div>");
            $("#conditionSubmitLineOperator" + conditionLineID).append(operator);

            $("#conditionSubmitLine" + conditionLineID).append("<div id='conditionSubmitLineCriteria" + conditionLineID + "' class='KAconditionSubmitCriteria'></div>");
            $("#conditionSubmitLineCriteria" + conditionLineID).append(value);
            $("#KAsubmitButton").prop('disabled', false);
            
            
        }
        else if (conditionStatus === "exists") {
            $("#KAsubmitButton").prop('disabled', true);
             standardOK("confirmDialog",
                "Error",
                 "Duplicate condition cannot be added."
            )(); 
        }
        else {
            $("#KAsubmitButton").prop('disabled', true);
            standardOK("confirmDialog",
                "Error",
                 "The condition you're trying to add is not suitable for this case."
            )(); 
        }
    }

    function populateAttributeValuesTable(callback, divName, selector) {
        $("#" + divName).append("<div class='tableHeader KAconditionAttribute'>Attribute</div>");
        $("#" + divName).append("<div class='tableHeader KAconditionType'>Type</div>");
        $("#" + divName).append("<div class='tableHeader KAconditionValue'>Value</div>");
        //$("#" + divName).append("<div class='tableHeader KAconditionVariable'>Variable</div>");
        getCaseAttributes(callback,divName, selector);
    }     

    function backButtonCallback(event) {
        let requestedDiv = event.data.requestedDiv;
        let clear = event.data.clear;
        let removeSelf = event.data.removeSelf;
        let removeOthers = event.data.removeOthers;
        let hideDiv = event.data.hideDiv;
        let registerContent = event.data.registerContent;
        
        if (removeSelf)
            $("#" + event.target.id).remove();
        
        if (removeOthers) {
            removeAllBackButtons();
            removeClassFromItemsWithClass("itemTransparency","itemTransparency");
        }
        
        showAndHide(requestedDiv,hideDiv,clear,registerContent) ; 
               
        
    }
    
    function addBackButton(buttonText,currentDiv, buttonID, callbackParams) {
        $("#" + currentDiv).append('<input type="button" id="' + buttonID + '" value="' + buttonText +'" class="inlineButton navigation hbutton backButton">');
        $('body').on('click','#'+buttonID, callbackParams, backButtonCallback);
    }
    
    function removeBackButton(buttonID) {
        $("#" + buttonID).remove();
    }  
    
    function removeAllBackButtons() { 
        $('.backButton').each(function(index,aButton) {
            $(aButton).remove();
        });
    }
    
    
    
    // FINAL RULE SUBMISSION
    function askToSubmitNewRule() {
        standardDialog("confirmDialog",
                "Knowledge Acquisition",
                "Are you sure you want to execute the current knowledge acquisition?",
                prepareNewRuleSubmission,
                standardOK("confirmDialog",
                "Knowledge acquisition",
                 "The current rule has not been submitted.."
                )
        ); 
    }

    function getIsKnowledgeAcquisitionContextAtRoot(callback) {
        let dataList = {mode:"getIsKnowledgeAcquisitionContextAtRoot",username: getSelectedUserName()};
        generalAjaxQuery('KnowledgeAcquisitionServlet',callback,dataList);  
    }
    
    function prepareNewRuleSubmission(parentDisplayDiv) {
        let isStoppingRule = $("#KAisStoppingRule").is(':checked');
        let doNotStack = $("#KAisDoNotStack").is(':checked');
        if (isStoppingRule) {
            getIsKnowledgeAcquisitionContextAtRoot(prepareNewRuleSubmissionCallback)
            
        }
        else
            submitRule(submitRuleCallback,isStoppingRule,doNotStack);
    }

    function prepareNewRuleSubmissionCallback(response) {
        let isStoppingRule = $("#KAisStoppingRule").is(':checked');
        let doNotStack = $("#KAisDoNotStack").is(':checked');
        
        if (response['result'] === "true" && isStoppingRule) {
           standardOK("confirmDialog","ERROR","You cannot add a stopping rule as a direct child of the root of the knowledge-base as this would invalidate all rules!")();  
        } 
        else {
            submitRule(submitRuleCallback,isStoppingRule,doNotStack);
        }
    }
    
    function submitRuleCallback() {  
        standardOK("confirmDialog",
                "Knowledge acquisition",
                "New rule successfuly added..",               
        )();
        
        showAndHide("mainContentDivInferenceViewer","divKnowledgeAcquisition",true,false);
        //returnToInferenceViewer();
        refreshGUI();  // we've added a new rule, make sure it's displayed!
    }
    
    /* ------------------------------------------------------------------------------- */
    /* Determine knowledge acquisition rule location (context)
    /* ------------------------------------------------------------------------------- */
  
    /* KA capture methodology:
    most recent response is valid:
        ask if new conclusion *corrects* current response
            YES: add conclusion as child to current context
            NO: ask if new conclusion related to a most recent (valid) previous response
                YES: add conclusion as child of most recent valid previous response
                NO: add conclusion to root * consider whether to ask for arbitrary dialog location first..

    most recent response is not valid:
        ask if new conclusion related to a most recent (valid) previous response
            YES: add conclusion as child of most recent valid previous response
            NO: add conclusion to root
    */
    
    function confirmStartKnowledgeAcquisition() {
        if (getStatusIsDomainInitialised()) {
            if (getStatusIsValidUser()) {
                standardDialog("confirmDialog",
                    "Confirm Knowledge Acquisition",
                    "Do you want to start knowledge acquisition?",
                    startKnowledgeAcquisition, // YES
                    standardOK("confirmDialog","Cancel knowledge acquisition","Knowledge acquisition is cancelled") //NO
                ); 
            }
            else {
                standardOK("confirmDialog","Notice","No user conversation sessions have started yet so knowledge acquisition is not possible.")();
            }           
        }
        else {
            standardOK("confirmDialog","Error","The domain knowledge-base has not been initialised")();
        } 
    }
  
    function startKnowledgeAcquisition() {
        
        // set the last system reponse dialog ID (for metadata usage such as creating context vars)
        getMostRecentSystemDialogID(startKnowledgeAcquisitionCallback);
        
        
       
    }
    
    function startKnowledgeAcquisitionCallback() {
        // see if the the most recent response was valid
         getIsValidMostRecentSystemResponse(getIsValidMostRecentSystemResponseCallback);
    }
    
    function getIsValidMostRecentSystemResponseCallback(isValid) {
        dconsole("getIsValidMostRecentSystemResponseCallback");
        //if (getStatusIsValidMostRecentSystemResponse()) {
        
        if (isValid) {
            dconsole("   most recent response VALID");
            getMostRecentSystemResponseString(getIsValidMostRecentSystemResponseCallback2);  
        }
        else {
            dconsole("   most recent response NOT VALID");         
            standardDialog("confirmDialog",
                "Knowledge Acquisition",
                "The system did not understand the most recent response. Do you want to add new knowledge to correct it?",
                //kaInvalidResponseToRoot,
                askCheckPastContextWantedInvalid, // YES - invalid response add but see where to add..
                standardOK("confirmDialog", // NO - cancel knowledge acquisition
                        "Cancel knowledge acquisition",
                        "Knowledge acquisition is cancelled"
                )            
            );
                     
        }
    }
    
    // We had a valid response, so see if we are correcting it..
    function getIsValidMostRecentSystemResponseCallback2(mostRecentResponseString) {
        //var mostRecentResponse = getStatusMostRecentSystemResponseString();
        standardDialog("confirmDialog",
                "Knowledge Acquisition",
                "Is the new knowledge related to one or more of the most recent response(s)?<br/>" + mostRecentResponseString.replace("\n","<br\>"),
                kaMostRecentSystemResponse,  // YES - correct it
                askCheckPastContextWantedValid // NO - find out where to add..
            );
    }
    
    // we did not have a valid response or user does not want to correct most recent response
    //  - but see if we are continuing an older context..
    function askCheckPastContextWantedInvalid() {
        getIsValidPastSystemResponse(getIsValidPastSystemResponseCallback,false);     
    }
    function askCheckPastContextWantedValid() {
        getIsValidPastSystemResponse(getIsValidPastSystemResponseCallback,true);     
    }
    
    function getIsValidPastSystemResponseCallback(isValidPast,currentIsValid) {
        if (isValidPast) {
            dconsole("   a past response is VALID");
            getPastSystemResponseString(getPastSystemResponseCallback,currentIsValid);  
        }
        else {
            dconsole("   no past response ie INVALID");
            if (currentIsValid)
                kaValidResponseToRoot();
            else
                kaInvalidResponseToRoot();
        }       
    }
    
    function getPastSystemResponseCallback(pastResponseString,currentIsValid) {
        let kaFunction;
        if (currentIsValid)
            kaFunction = kaValidResponseToRoot;
        else
            kaFunction = kaInvalidResponseToRoot; 
            
        standardDialog("confirmDialog",
                "Knowledge Acquisition",
                "Is the new knowledge related to the past response ['" + pastResponseString + "']?",
                kaPastSystemResponse,  // YES - correct it
                kaFunction // NO - add to root      
        );
    }
    
    
    /* ------------------------------------------------------------------------------- */
    // NOT CURRENTLY USED...    
    // we are adding rule to possibly an arbitrary location..
    /* ------------------------------------------------------------------------------- */
    function askNewKnowledgeContextLocation() {
        return function() {         
            if (getStatusSelectedDialogIdKA() !== 0) {
                standardDialog("confirmDialog",
                    "Knowledge Acquisition",
                    "Do you want to correct the response related to the dialog you selected?",
                    kaForSelectedResponse,
                    askKaNewKnowledgeCheckNewContext()
                ); 
            }
        }
    }
     
    /* ------------------------------------------------------------------------------- */
    /* Knowledge Acquisition - final stages before requesting rule details..
     * Used by all modes of knowledge acquisition
    /* ------------------------------------------------------------------------------- */  
    function kaPrepareLearner() {
        dconsole("kaPrepareLearner - preparing learner");        
        prepareLearner(kaGetInferenceResult);
    } 
    
    function kaGetInferenceResult() {
        dconsole("kaInvalidResponseToRootCallback3 - getting inference result");
                    
        let dataList = {mode:"getInferenceResult",username: getSelectedUserName(), selectedConclusion: -1};
        generalAjaxQuery('KnowledgeAcquisitionServlet',kaGetInferenceResultCallback,dataList);   
        //getInferenceResult(createKnowledgeAcquisitionInterface);
    } 
    
    function kaGetInferenceResultCallback(response) {
        
        if (response['rows'].length > 1) {
            standardOKWithCallback("confirmDialog2", // NO - cancel knowledge acquisition
                "Notice",
                "There was more than one conclusion in the past system response.  Please select one of them in the next dialog box to correct. ",
                selectWrongConclusion,
                response)();    
            dconsole("   Found more than one conclusion from inference:");

            
            //selectWrongConclusion(response);
        }
        else {
            createKnowledgeAcquisitionInterface();
        }
    }
    
    function selectWrongConclusion(theResponse) {
        let rows = theResponse['rows'];
        //let body = "<div class='fullblockWrapperSlim'><div class='tableHeader'>Saved query snippets</div></div>";
        
        let header = theResponse['header'];
        let headerWidths = [50,300];
        let body = createBodyTableViewerChooserContent(header, headerWidths, rows, "chooser1");             
        
        let buttonList = [ 
            {                
                id: "query-snippet-close",
                text: "Select conclusion",
                click: knowledgeAcquisitionSelectWrongConclusionHandler
            }                        
        ];

        // default chooser populates the queryChooserTableFieldNames item, even though we're not dealing with fieldnames here..
        let data = {resultData:"chooser1",selectionData:"queryChooserTableFieldNames"};
        
        initialiseChooserSelectionData(chooserData,"chooser1",null,null);
        singleChooserDialog(chooserData,"confirmDialog","Choose conclusion to correct",body,buttonList,data,true)();         
    }
    
    function knowledgeAcquisitionSelectWrongConclusionHandler() {
        let dialogData = $("#confirmDialog").data("maindata");
        
        let conclusionBaseData = dialogData.resultData;       
        let rowDataName = dialogData.selectionData;
        //let selectedRowData = getChooserSelectionData(chooserData,queryBaseData,rowDataName);
        let selectedRowData = getChooserSelectionDataFromTable(chooserData,conclusionBaseData,rowDataName);
        if (selectedRowData.length !== 0) {

        // format is "ID description.."
            let selectedConclusionID = selectedRowData[0];
            let selectedConclusion = selectedRowData[1];
            dconsole("Selected conclusion [" + selectedConclusionID + "] is " + selectedConclusion);
            $("#confirmDialog").dialog("close");
            
            let dataList = {mode:"getInferenceResult",username: getSelectedUserName(), selectedConclusion: selectedConclusionID};
            generalAjaxQuery('KnowledgeAcquisitionServlet',kaGetInferenceResultCallback,dataList);   
            
        }
        else {
            standardOK("confirmDialog2", "Error","Please select a conclusion")();
        }
    }
    
    /* ------------------------------------------------------------------------------- */
    /* Knowledge Acquisition - Handle KA for system response adding totally new rule to root
    /* ------------------------------------------------------------------------------- */  
    
    // Get the most recent dialog ID (of the selected user)
    function kaInvalidResponseToRoot() {
        dconsole("kaInvalidResponseToRoot");
        getMostRecentUserDialogID(kaInvalidResponseToRootCallback);
    }  
    // generate a new case
    function kaInvalidResponseToRootCallback() {
        dconsole("kaInvalidResponseToRootCallback - generating case and user interface");
        generateCaseForKnowledgeAcquisition(kaPrepareLearner,'new');
    }
    
    // the following were added in case we need to discriminate in future revision
    // to add a valid result to the root.. unlikely.. but here anyway..
    // Get the most recent dialog ID (of the selected user)
    function kaValidResponseToRoot() {
        dconsole("kaValidResponseToRoot");
        getMostRecentUserDialogID(kaValidResponseToRootCallback);
    }  
    // generate a new case
    function kaValidResponseToRootCallback() {
        dconsole("kaValidResponseToRootCallback - generating case and user interface");
        generateCaseForKnowledgeAcquisition(kaPrepareLearner,'new');
    }
    
    /* ------------------------------------------------------------------------------- */
    /* Knowledge Acquisition - Adding rule in context of most recent (and valid!) system response..
    /* ------------------------------------------------------------------------------- */      
    function kaMostRecentSystemResponse() {
        dconsole("kaMostRecentSystemResponse");
        getMostRecentUserDialogID(kaMostRecentSystemResponseCallback);     
    }   
    // generate a new case
    function kaMostRecentSystemResponseCallback() {
        dconsole("kaMostRecentSystemResponseCallback - generating case and user interface");
        generateCaseForKnowledgeAcquisition(kaPrepareLearner,'immediate');
    }    

    /* ------------------------------------------------------------------------------- */
    /* Knowledge Acquisition - Adding rule in context of a past valid system response..
    /* ------------------------------------------------------------------------------- */     
    function kaPastSystemResponse() {
        dconsole("kaPastSystemResponse");
        getMostRecentUserDialogID(kaPastSystemResponseCallback);  
    }
    // generate a new case
    function kaPastSystemResponseCallback() {
        dconsole("kaPastSystemResponse - generating case and user interface");
        generateCaseForKnowledgeAcquisition(kaPrepareLearner,'continue');
    }    
    