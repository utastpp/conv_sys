/* 
Dave Herbert
May/June 2019

 */
    //var chooserSelectionData = {};
    // global object for data exchange
    var currentStatus = {
        dialogIdKA: 0,
        stackID: -1,
        validUser: false,
        isDomainInitialised: false,
        sessionID: 0,
        //isValidMostRecentSystemResponse: false,
        //mostRecentSystemResponseString: "",
        //isValidPastSystemResponse: false,
        //pastSystemResponseString: "",
        mostRecentDialogID: 0,
        registerToBeDeleted: [],
        KAmainContent: "",
        contextVar: "",
        dialogOffset: 0
    };
    
    var chooserData = {
        chooser1: {},
        chooser2: {}
    };
    
    function setStatusSelectedDialogIDKA(dialogId) {
        currentStatus.dialogIdKA = dialogId;
    }
    
    function getStatusSelectedDialogIdKA() {
        return currentStatus.dialogIdKA;
    }
    
    function getStatusDialogOffset() {
        return currentStatus.dialogOffset;
    }
    
    function setStatusDialogOffset(offset) {
        currentStatus.dialogOffset = offset;
    }
    
    function setStatusDialogOffsetIncrement() {
        currentStatus.dialogOffset = currentStatus.dialogOffset + 50; 
    }
    
    function setStatusDialogOffsetDecrement() {
        currentStatus.dialogOffset = currentStatus.dialogOffset - 50;
        if (currentStatus.dialogOffset < 0)
            currentStatus.dialogOffset = 0;  
        
    }
    
    function setStatusContextVar(varName) {
        currentStatus.contextVar = varName;
    }
    
    function getStatusContextVar() {
        return currentStatus.contextVar;
    }
    
    function setStatusSelectedStackID(frameID) {
        dconsole("    current status stackID is set to: " +  frameID);
        currentStatus.stackID = frameID;
    }
    
    function getStatusSelectedStackID() {
        return currentStatus.stackID;
    }
    
    function getStatusIsDomainInitialised() {
        return currentStatus.isDomainInitialised;
    }
    
    function getStatusIsValidUser() {
        return currentStatus.validUser;
    }
    
    function getIsDomainInitialised(callback) {
        //var isInitialised = false;
        $.ajax({
           type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'getIsDomainInitialised'
            },
            success : function(response) {
                let isInitialised;
                if (response === "false") {                   
                    isInitialised = false;                    
                }
                else {
                    isInitialised = true;
                }
                currentStatus.isDomainInitialised = isInitialised;
                callback();
            }
        });
    }
    
    
    
    
    const singleSelectionMode = "single";
    const multiSelectionMode = "multi";
    var utilsEventListenersRegistered = false;
    
    var DEBUG = true;
    function dconsole(text) {
        if (DEBUG)
            console.log(text);
    }
    
    function utilsAddEventListeners() {
        if (!utilsEventListenersRegistered) {
            $('body').on('click','.chooserBodyContent', function(e) {       
                let theID = "";      
                //theID = e.target.id.toString().substring(18); 
                let theBaseTarget = $("#" + e.target.id.toString()).data('target');
                
                if ( e.target.id.toString().startsWith('chooserBodyContentItem')) {
                    theID = $(e.target).parent().prop('id');
                }
                else
                    theID =  e.target.id.toString();
                
                
                
                if (isSingleChooserSelectionMode(chooserData,theBaseTarget)) {
                    unhighlightItems("chooserBodyContentItemSelected");
                    //highlightItem(e.target.id.toString(),"chooserBodyContentItemSelected");
                    highlightItem(theID,"chooserBodyContentItemSelected");

                    let selectionDataList = getSingleItemValueWithClass("chooserBodyContentItemSelected");
                    // set up the data selection target
                    setChooserSelectionData(chooserData,theBaseTarget,"queryChooserTableFieldNames",selectionDataList);
                }
                else if (isMultiChooserSelectionMode(chooserData,theBaseTarget)) {
                    // toggle item if already highlighted..
                   // if (isHighlighted(e.target.id.toString(),"chooserBodyContentItemSelected")) {
                    if (isHighlighted(theID,"chooserBodyContentItemSelected")) {
                        unhighlightItem(theID,"chooserBodyContentItemSelected");
                        //unhighlightItem(e.target.id.toString(),"chooserBodyContentItemSelected");
                    }
                    else {
                        //highlightItem(e.target.id.toString(),"chooserBodyContentItemSelected");
                        highlightItem(theID,"chooserBodyContentItemSelected");
                    }

                    let selectionDataList = getMultiItemValuesWithClass("chooserBodyContentItemSelected");
                    // set up the data selection target
                    setChooserSelectionData(chooserData,theBaseTarget,"queryChooserTableFieldNames",selectionDataList);
                    //setChooserSelectionData("queryChooserTableFieldNames",$("#" + e.target.id.toString()).html());
                }

                if (getNumberOfHighlightedItems("chooserBodyContentItemSelected") >= 1) {
                    enableChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                    enableChooserButton(chooserData[theBaseTarget]["enableChooserButton2"]);
                }
                else {
                    disabledChooserButton(chooserData[theBaseTarget]["enableChooserButton1"]);
                    disabledChooserButton(chooserData[theBaseTarget]["enableChooserButton2"]);
                }               
            });
            utilsEventListenersRegistered = true;
        }
    }
    
    function setChooserSelectionData(base, chooser, item, data) {

        base[chooser][item] = data;
        //return base;
    }
    
    function getChooserSelectionData(base,chooser,item) {
        //return chooserSelectionData[item];
        return base[chooser][item];
    }
    
    function getChooserSelectionDataFromTable(base,chooser,item) {
        let dataRowHTML = base[chooser][item];
        let row = [];
        let rowID = "";
        // get the ID of the selected row
        $('.chooserBodyContentItemSelected').each(function(index,item) {
           rowID = item.id;
        });
        
        if (rowID !== "") {    
            $('#' + rowID).find('.viewerRowItem').each(function(index,item) {
                let theItemID = $(item).prop("id");

                row.push($("#" + theItemID).html());
            });
        }
        
        return row;
    }
    
    function initialiseChooserSelectionData(base,chooser,buttonToEnable1,buttonToEnable2) {
        //chooserSelectionData = {};
        base[chooser] = {};
        if (buttonToEnable1 !== null)
            base[chooser]["enableChooserButton1"] = buttonToEnable1;
        if (buttonToEnable2 !== null)
            base[chooser]["enableChooserButton2"] = buttonToEnable2;

        //return base;
    }
    
    function setChooserSingleSelectionMode(base,chooser) {
        //chooserSelectionData["mode"] = singleSelectionMode; 
        base[chooser]["mode"] = singleSelectionMode; 
        //return base;
    }
    
    function setChooserMultiSelectionMode(base,chooser) {
        base[chooser]["mode"] = multiSelectionMode; 
    }
    
    function isSingleChooserSelectionMode(base,chooser) {  
        return base[chooser]["mode"] === singleSelectionMode;
    }
    
    function isMultiChooserSelectionMode(base,chooser) {
        return base[chooser]["mode"] === multiSelectionMode;
    }
    
    function fireTriggerChange(item) {
        $("#" + item).trigger('change');        
    }

/* ************************** dialog box definitions *************************  */
    function standardDialog(parentDiv, title,body,yesfn,nofn) { 
        setStatusDialogOffsetIncrement();
         
        $("#" + parentDiv).html("<p>" + body +"</p>");
        $("#" + parentDiv).dialog({
            dialogClass: "no-close",
            show: "",
            resizable: false,
            draggable: true,
            modal: true,
            title: title,
            height: 'auto',
            maxHeight: 500,
            width: 300,
            //position: { my: "center top", at: "center top+20", of: window },
            position: { my: "left+" + getStatusDialogOffset() + " top+" + getStatusDialogOffset(), at: "left top+25", of: window },

            buttons: {
                "Yes": function () {
                    $(this).dialog('close');
                    setStatusDialogOffsetDecrement();
                    yesfn();
                },
                "No": function () {
                    $(this).dialog('close');
                    setStatusDialogOffsetDecrement();
                    if (nofn !== undefined)
                        nofn();
                }
            }
        });         
    }
    
    function standardOK(parentDiv, title,body) {
        let effect = "";
        if (title === "Error") 
            effect = "shake";
        
        return function() {
            setStatusDialogOffsetIncrement();
            
            $("#" + parentDiv).html(body);
            $("#" + parentDiv).dialog({
                dialogClass: "no-close",
                show: effect,
                duration: 400,
                resizable: false,
                draggable: true,
                modal: true,
                title: title,
                height: 150,
                width: 300,
                //position: { my: "center top", at: "center top+20", of: window },
                position: { my: "left+" + getStatusDialogOffset() + " top+" + getStatusDialogOffset(), at: "left top+25", of: window },
                buttons: {
                    "OK": function () {
                        setStatusDialogOffsetDecrement();
                        $(this).dialog('close');
                    }
                }
            });    
        };
    }
    
    function standardOKWithCallback(parentDiv, title,body,callback,callbackData) {        
        return function() {
            setStatusDialogOffsetIncrement();
            $("#" + parentDiv).html(body);
            $("#" + parentDiv).dialog({
                dialogClass: "no-close",
                resizable: false,
                draggable: true,
                modal: true,
                title: title,
                height: 150,
                width: 300,
                //position: { my: "center top", at: "center top+20", of: window },
                position: { my: "left+" + getStatusDialogOffset() + " top+" + getStatusDialogOffset(), at: "left top+25", of: window },

                buttons: {
                    "OK": function () {
                        $(this).dialog('close');
                        setStatusDialogOffsetDecrement();
                        callback(callbackData);
                    }
                }
            });    
        };
    }
    
    function standardOKLarge(parentDiv, title,body,width) {
        let theWidth = 600;
        if (width !== null)
            theWidth = width;
        
        return function() {
            let effect = "";
            if (title === "Error") 
                effect = "shake";
            setStatusDialogOffsetIncrement();
            $("#" + parentDiv).html("<p>" + body +"</p>");
            $("#" + parentDiv).dialog({
                dialogClass: "no-close dialogYScroll",
                show: effect,
                resizable: false,
                draggable: true,
                modal: true,
                title: title,
                height: 450,
                width: theWidth,
                //position: { my: "center top", at: "center top+20", of: window },
                position: { my: "left+" + getStatusDialogOffset() + " top+" + getStatusDialogOffset(), at: "left top+25", of: window },

                buttons: {
                    "OK": function () {
                        $(this).dialog('close');
                        setStatusDialogOffsetDecrement();
                    }
                }
            });    
        };
    }
    
    
    function standardProcessDialog(parentDiv, title, callback, buttonList,setWidth) {
        return function() {
            let data = {callback: callback};
            let buttons = {
                    "Close": function () {
                        standardProcessDialogCloseHandler(parentDiv);
                    }
                };
            
            let width = 'auto';
                    
            if (buttonList !== undefined && buttonList !== null) {
                buttons = buttonList;
            }
            
            if (setWidth !== undefined && setWidth !== null) {
                width = setWidth;
            }
                        
            setStatusDialogOffsetIncrement();
            
            $("#" + parentDiv).data('maindata',data).dialog({
                dialogClass: "no-close dialogYScroll",
                resizable: false,
                draggable: true,
                modal: true,
                title: title,
                height: 'auto',
                width: width,
                //position: { my: "center+" + getStatusDialogOffset() + " top+" + getStatusDialogOffset(), at: "center top", of: window },
                position: { my: "left+" + getStatusDialogOffset() + " top+" + getStatusDialogOffset(), at: "left top+25", of: window },
                buttons: buttons,
            });           
        };
    }
    
    function standardProcessDialogCloseHandler(parentDiv) {
        setStatusDialogOffsetDecrement();
        let dataCallback = $('#' + parentDiv).data("maindata").callback;
        if (dataCallback !== null) {
           dataCallback();
        }
        $('#' + parentDiv).dialog('close');           
    }
    
    function createBodyTableViewerChooserContent(header, headerWidths, rows, targetResult) {
        let body = "<div class='chooserBodyContentDiv'>";
        
        let extraStyles = "";
        
        // header row
        body += "<div class='viewerTableHeader'>";
        $.each(header,function(index,headerItem) {
            body += "<div class='viewerHeaderItem' style='width:" + headerWidths[index] + "px;'>" + headerItem + "</div>";
        });
        body += "</div>";
        
        // rows
        $.each(rows, function(index,rowOfItems) {
            if (index%2 === 1)
                extraStyles = "tableAlt1";
            else
                extraStyles = "tableAlt2"; 
            
            body += "<div id='chooserBodyContent" + index + "' class='chooserBodyContent " + extraStyles + "' data-target='" + targetResult + "'>";
            $.each(rowOfItems, function(index2, item) {
                body += "<div id='chooserBodyContentItem" + index + index2 + "' class='viewerRowItem' style='width:" + headerWidths[index2] + "px;' data-target='" + targetResult + "'>" + escapeHTML(item) + "</div>";
            });
            body += "</div>";
        });
        
        body += "</div>";

        return body;    
    }
    
    function createBodyTableViewer(header, headerWidths, rows) {
        let body = "<div class='viewerContentDiv'>";
        
        let extraStyles = "";
        
        // header row
        body += "<div class='viewerTableHeader'>";
        $.each(header,function(index,headerItem) {
            body += "<div class='viewerHeaderItem' style='width:" + headerWidths[index] + "px;'>" + headerItem + "</div>";
        });
        body += "</div>";
        
        // rows
        $.each(rows, function(index,rowOfItems) {
            // see if we have an embedded sub-header row..
            if (rowOfItems[0] === ".subheader1" || rowOfItems[0] === ".subheader2") {
                body += "<div class='" + rowOfItems[0].substring(1) + "'>"; 
                $.each(rowOfItems,function(index3,headerItem) {
                    if (index3 === 0)
                        body += "<div class='viewerHeaderItem' style='width:" + headerWidths[index3] + "px;'>" + "" + "</div>";
                    else
                        body += "<div class='viewerHeaderItem' style='width:" + headerWidths[index3] + "px;'>" + headerItem + "</div>";
                });
                body += "</div>";
            }
            else {
                if (index%2 === 1)
                    extraStyles = "tableAlt1";
                else
                    extraStyles = "tableAlt2"; 

                body += "<div class='viewerBodyContent " + extraStyles + "'>";
                $.each(rowOfItems, function(index2, item) {
                    body += "<div class='viewerRowItem' style='width:" + headerWidths[index2] + "px;'>" + escapeHTML(item) + "</div>";
                });
                body += "</div>";
            }
        });
    
        
        body += "</div>";

        return body;    
    }
    
    function createChooserBodyContent(listOfItems, targetResult) {
        let body = "<div class='chooserBodyContentDiv'>";
        
        let extraStyles = "";
        
        $.each(listOfItems,function(index,item) {
            if (item.length > 1) {
                let combinedItem = "";
                $.each(item, function(index2,subItem) {
                    if (combinedItem === "")
                        combinedItem += subItem;
                    else
                        combinedItem += " " + subItem;
                });
                item = combinedItem;
            }
            if (index%2 === 1)
                extraStyles = "tableAlt1";
            else
                extraStyles = "tableAlt2";        
            body += "<div id='chooserBodyContent" + index + "' class='chooserBodyContent " + extraStyles + "' data-target='" + targetResult + "'>" + item + "</div>";
        });
        
        body += "</div>";

        return body;
    }
    
    function singleChooserDialogDefault(base,parentDiv,title,body,cancelCallback,okCallback,theData, shouldOffset) {
        setChooserSingleSelectionMode(base,theData.resultData);
        $("#" + parentDiv).html("<p>" + body +"</p>");

        let position1 = "center top";
        let position2 = "center top+20";

        if (shouldOffset) {
            position2 = "center+100 top+100";
        }
        
        $("#" + parentDiv).dialog({
                dialogClass: "no-close dialogYScroll",
                resizable: false,
                draggable: true,
                modal: true,
                title: title,
                height: 250,
                width: 300,
                position: { my: position1, at: position2, of: window },
                buttons: [ 
                    {                
                    id: "chooser-button-cancel",
                    text: "Cancel",
                    click:  cancelCallback                       
                    },
                    {
                        id: "chooser-button-ok",
                        text: "Ok",
                        disabled: true,
                        click: okCallback
                    }     
                ]
            });   
    }
    
    
    function multiChooserDialog(base,parentDiv,title,body,buttonList,theData,shouldOffset) {      
        return function() {
            setChooserMultiSelectionMode(base,theData.resultData);
            
            $("#" + parentDiv).html(body);
            
            let position1 = "center top";
            let position2 = "center top+20";
            let width = 300;
            let height = 250;
            
            if (shouldOffset) {
                position2 = "center+100 top+100";
                width = 350;
                height = 500;
            }

            $("#" + parentDiv).data('maindata',theData).dialog({
                    dialogClass: "no-close dialogYScroll",
                    resizable: false,
                    draggable: true,
                    modal: true,
                    title: title,
                    height: height,
                    width: width,
                    //position: { my: "center top", at: "center top+20", of: window },
                    position: { my: position1, at: position2, of: window },
                    buttons: buttonList
                });  
        };
    }
    function singleChooserDialog(base,parentDiv,title,body,buttonList,theData, shouldOffset) {
        return function() {
            setChooserSingleSelectionMode(base,theData.resultData);
            $("#" + parentDiv).html("<p>" + body +"</p>");
            let position1 = "center top";
            let position2 = "center top+20";

            if (shouldOffset) {
                position2 = "center+100 top+100";
            }

            $("#" + parentDiv).data('maindata',theData).dialog({
                    //create: registerCustomElementsEvents(customElementList),
                    dialogClass: "no-close dialogYScroll",
                    resizable: false,
                    draggable: true,
                    modal: true,
                    title: title,
                    //height: 250,
                    height: "auto",
                    maxHeight: 500,
                    width: 500,
                    position: { my: position1, at: position2, of: window },
                    buttons: buttonList
                });   
        };
    }
    
    
    function enableChooserButton(divName) {
        $("#" + divName).button( "option", "disabled", false );
    }    
    function disableChooserButton(divName) {
        $("#" + divName).button( "option", "disabled", true );
    }
    
    function toggleButton(buttonName) {
        $("#" + buttonName).prop('disabled',!$("#" + buttonName).prop('disabled'));
        
    } 
    function toggleItem(itemName) {
        $("#" + itemName).prop('disabled',!$("#" + itemName).prop('disabled'));       
    }
    
    function enableItem(itemName) {
        $("#" + itemName).prop('disabled',false);
        $("#" + itemName).effect("highlight", {}, 1000);

    }
    
    function disableItem(itemName) {
        $("#" + itemName).prop('disabled',true);
    }
    
    function isEnabledItem(itemName) {
        return !$("#" + itemName).prop('disabled');
    }
    
 /* **************************************************************************  */ 
 
    function findExistingTerm(searchTerm,theTerms) {
        found = false;
        $.each(theTerms,function(i,aTerm) {
            if (aTerm.trim()) {
                if (aTerm === searchTerm) {
                    found = true; 
                }
            }
        });
        return found;
    }
    
    function highlightItem(itemID,highlightClass) {
        $("#" + itemID).addClass(highlightClass);
    }
    
    function unhighlightItem(itemID,highlightClass) {
        $("#" + itemID).removeClass(highlightClass);
    }
    
    function unhighlightItems(highlightClass) {
        $('.'+highlightClass).each(function(index,item) {
            $(this).removeClass(highlightClass);
        });
    }
    
    function isHighlighted(itemID,highlightClass) {
        return $("#" + itemID).hasClass(highlightClass);
    }
    
    function getNumberOfHighlightedItems(highlightClass) {
        return $('.' + highlightClass).length;
    }
    
    function addClassToItemsWithClass(itemClass,classToAdd) {
        $('.'+itemClass).each(function(index,item) {
            $(item).addClass(classToAdd);
        });
    }
    
    function removeClassFromItem(itemId,itemClass) {
        $("#" + itemId).removeClass(itemClass);
    }
    
    function removeClassFromItemsWithClass(itemClass,classToRemove) {
        $('.'+itemClass).each(function(index,item) {
            $(this).removeClass(classToRemove);
        }); 
    }
    
    function getIsSingleItemWithClass(classToFind) {
        return $('.'+ classToFind).length === 1;
    }
    
    function getSingleItemValueWithClass(classToFind) {
        let singleItemValueList = null;
        if (getIsSingleItemWithClass(classToFind)) {
            $('.'+classToFind).each(function(index,item) {
                if (singleItemValueList === null)
                    singleItemValueList = [];
                singleItemValueList.push($("#" + item.id).html());
            });
        }       
        return singleItemValueList;
    }
    
    function getMultiItemValuesWithClass(classToFind) {
        let valueList = null;
            $('.'+classToFind).each(function(index,item) {
                if (valueList === null)
                    valueList = [];
                valueList.push($("#" + item.id).html());
            });
      
        return valueList;
    }
    
    function getSelectorValue(target) {
        var item = $("#" + target).val();
        if (item === "" || item === null) {
            $("#" + target).val($("#" + target + " option:first").val());
        }
        return $("#" + target).val();
    }
    
    function getSelectorLength(target) {
        let length = $("#" + target + " option").length;
        return length;
    }
    
    function generalAjaxQuery(url,callback,dataList,callbackData1,callbackdata2,callbackdata3) {
        $.ajax({
            url : url,
            dataType:'json',
            data : dataList,
            success : function(response) {
                let status = response['status'];
                if (status !== "OK") {
                    standardOK("confirmDialog", "Error",status)();
                }
                else  {
                    callback(response,callbackData1,callbackdata2,callbackdata3);
                }
            }
            
        });       
    }
    
    function generalAjaxStringQuery(url,callback,dataList,callbackData1,callbackdata2) {
        $.ajax({
            url : url,
            data : dataList,
            success : function(response) {               
                if (response !== "OK"  && response !== "IGNORE") {
                    standardOK("confirmDialog", "Error",response)();
                }
                else if (response === "OK") {
                    callback(response,callbackData1,callbackdata2);
                }
            }
            
        });       
    }
    
    function escapeHTML(data) {
        let escapedData = "";
        if (data !== null) {
            escapedData = data.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
        
            // allow embedded breaks, signified by #br#
            escapedData = escapedData.replace(/\#br\#/g,'<br/>');
        }
        
        return escapedData;
    }
    
    function unescapeHTML(data) {
        return data.replace(/&amp;/g,'&').replace(/&lt;/g,'<').replace(/&gt;/g,'>');
    }
    
    function updateField(theField,theValue) {
        if (theValue === "&nbsp;")
            theValue = "";
        $("#" + theField).val(theValue);
    }
       
    function updateSelector(selector,itemText) {
        $("#" + selector + " option").filter(function() {
            return $(this).text() === itemText;
        }).prop('selected',true);
    }
    
    function resetSelector(selector) {
        $("#" + selector).html("");
    }
    
    function addItemToSelector(selector,item,text) {
        $("#" + selector).append("<option value='" + item + "'>" + text + "</option>");
        
    }
    
    function download(filename, text) {
        var pom = document.createElement('a');
        pom.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
        pom.setAttribute('download', filename);

        if (document.createEvent) {
            var event = document.createEvent('MouseEvents');
            event.initEvent('click', true, true);
            pom.dispatchEvent(event);
        }
        else {
            pom.click();
        }
    } 
    
    function populateSelector(selector,dataList,callback,callbackData) {
       generalAjaxQuery('KnowledgeAcquisitionServlet',populateSelectorCallback,dataList,callback,selector,callbackData);
    }
    
    function populateSelectorCallback(response,callback,selector,callbackData) {
        let rows = response['rows'];
        
        resetSelector(selector);
        let firstValue = true;
        $.each(rows,function(index, aCategory) {
            addItemToSelector(selector,aCategory,aCategory);
            if (firstValue) {
                updateSelector(selector,aCategory);
                firstValue = false;
            }
        });

        callback(callbackData);
    }
    
    function getContextVariables(selector,callback,username) {
        if (username === undefined)
            username = "";
        let dataList = {mode:"getContextVariables", username:username};
        generalAjaxQuery('KnowledgeAcquisitionServlet',getContextVariablesCallback,dataList,callback,selector);
    }
    
    function getContextVariablesForModification(selector,callback,username) {
        if (username === undefined)
            username = "";
        let dataList = {mode:"getContextVariablesForModification", username:username};
        generalAjaxQuery('KnowledgeAcquisitionServlet',getContextVariablesCallback,dataList,callback,selector);
    }
     
    function getContextVariablesCallback(response,callback,selector) {     
        let rows = response['rows'];
        
        resetSelector(selector);
        let firstValue = true;
        $.each(rows,function(index, aCategory) {
            addItemToSelector(selector,aCategory,aCategory);
            if (firstValue) {
                updateSelector(selector,aCategory);
                firstValue = false;
            }
        });

        callback();
       
    }
    
    function populateField(field,dataList,callback,callbackData) {
       generalAjaxQuery('KnowledgeAcquisitionServlet',populateFieldCallback,dataList,callback,field,callbackData);
    }
    
    function populateFieldCallback(response,callback,field,callbackData) {
        let fieldData = response['result']; // must be only one row!

        $("#" + field).val(fieldData);
        callback(callbackData);
    }
    
//    function populateField(field,callback) {
//        $.ajax({
//            type: 'GET',
//            url : 'KnowledgeAcquisitionServlet',
//            dataType:'json',
//            data : {
//                mode : 'getContextVariableOverride',
//                variableName: variableName
//            },
//            
//            success : function(response) {
//                let override = response['result']; // msut be only one row!
//
//                $("#" + parent).val(override);
//                callback();
//            }
//        });         
//    }
    
//    function getContextVariableCriteria(selector,variableName, username,callback) {
//        dconsole("Calling getContextVariableCriteria with var name:" + variableName + " and username: " + username);
//        $.ajax({
//            type: 'GET',
//            url : 'KnowledgeAcquisitionServlet',
//            dataType:'json',
//            data : {
//                mode : 'getContextVariableCriteria',
//                variableName: variableName,
//                username: username
//            },
//            
//            success : function(response) {
//                let rows = response['rows'];
//                
//                resetSelector(selector);
//                let firstValue = true;
//                $.each(rows,function(index, aCategory) {
//                    dconsole("Adding " + aCategory + " to selector " + selector);
//                    addItemToSelector(selector,aCategory,aCategory);
//                    if (firstValue) {
//                        updateSelector(selector,aCategory);
//                        firstValue = false;
//                    }
//                });
//                
//                callback();
//            }
//        });         
//    }

    function selectorContainsOption(selector,option) {
        let exists = false;
        $("#" + selector + " option").each(function(index, theOption) {
            if (theOption.value === option) {
                exists = true;
                return false;
            }
        });
        return exists;
    }

    function updateButtonCoupledToSelector(button, selector) {
        if (getSelectorLength(selector) === 0)
            disableItem(button);
        else
            enableItem(button);    
    }