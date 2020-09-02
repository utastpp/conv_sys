
var dictionaryMain = "dictionaryMainContentDiv";
function getDictionaryMain() {
    return dictionaryMain;
}
var dictionarySiblingMain = "";
function setDictionarySiblingMain(sibling) {
    dictionarySiblingMain = sibling;
}
function getDictionarySiblingMain() {
    return dictionarySiblingMain;
}

var dictionaryEventListenersRegistered = false;

var currentDictionary = {
    terms: [],
    statuses: [],
    synonyms: []
};

var dictionaryChooserData = {
        chooser1: {},
        chooser2: {}
    };

    function getCurrentDictionaryTerms() {
        return currentDictionary.terms;
    }

    function setCurrentDictionaryTerms(termList) {
        currentDictionary.terms = termList;
    }

    function getCurrentDictionaryStatuses() {
        return currentDictionary.statuses;
    }

    function setCurrentDictionaryStatuses(statusList) {
        currentDictionary.statuses = statusList;
    }

    function getCurrentDictionarySynonyms() {
        return currentDictionary.synonyms;
    }

    function setCurrentDictionarySynonyms(synonymList) {
        currentDictionary.synonyms = synonymList;
    }

    function noopCallback() {
    }
   
    function createDictionaryInterface(parent,sibling) {
        setDictionarySiblingMain(sibling);
        let mainDiv = getDictionaryMain();
        $("#" + parent).html("");
        $("#" + parent).append("<div id='" + mainDiv + "'></div>");
        //$("#" + mainDiv).hide();
        $("#" + mainDiv).append("<div class='columnHeading'>Dictionary</div>");
        $("#" + mainDiv).append("<div id='dictionaryUserInputDiv'></div>");
        $("#dictionaryUserInputDiv").append("<div class='column' id='dictionaryColumn'> </div>");
        $("#dictionaryColumn").append("<div id='dictionaryTermsDiv' class='leftColumn'></div>");
        $("#dictionaryColumn").append("<div id='dictionaryNewTermDiv' class='rightColumn'></div>");
        $("#dictionaryColumn").append("<div id='dictionarySynonymsDiv' class='leftColumn'></div>");
        //$("#dictionaryColumn").append("<div class='fullColumn'><input id='dictionaryCloseButton' type='button' value='Close' class='inlineButtonSmall hbutton'></div>");
        
        $("#dictionaryTermsDiv").append("<div class='columnHeading'>Registered terms</div>");
        $("#dictionaryTermsDiv").append("<div class='columnItem'><label>Select term:</label><select id='dictionaryTermList'></select></div>");
        $("#dictionaryTermsDiv").append("<div class='columnItem'><div class='admin'><label style='width:300px;'>Allow synonym in help</label><input type='checkbox' id = 'dictionaryRandomSynonymCheckbox' name='dictionaryRandomSynonymCheckbox' class='dictionaryCheckbox' value='1' checked></div>");
        $("#dictionaryTermsDiv").append("<div class='columnItem'><div class='rightbutton'><input id='dictionaryShowAllSynonymButton' type='button' value='Show synonyms' class='hbutton'></div>");                                                

        $("#dictionaryNewTermDiv").append("<div class='columnHeading'>Create new registered term</div>");
        $("#dictionaryNewTermDiv").append("<div class='columnItem'><div class='admin'><div class='userTextInputDiv'> <input id='dictionaryNewTerm' type='text' name='dictionaryNewTerm' placeholder='New term..'></div></div></div>");  
        $("#dictionaryNewTermDiv").append("<div class='columnItem'><div class='rightbutton'><input id='dictionaryNewTermButton' type='button' value='Add term' class='hbutton'></div></div>");        
                                                  
        $("#dictionarySynonymsDiv").append("<div class='columnHeading'>Synonym maintenance</div>");   
        $("#dictionarySynonymsDiv").append("<div class='columnItem'><div class='admin'><div class='userTextInputDiv'><input id='dictionaryNewSynonym' type='text' placeholder='New synonym...'></div></div></div>");
        $("#dictionarySynonymsDiv").append("<div class='columnItem'><div class='rightbutton'><input id='dictionaryNewSynonymButton' type='button' value='Add synonym' class='hbutton'></div></div>");
        $("#dictionarySynonymsDiv").append("<div class='columnItem'><div class='admin'><label>Select Synonym:</label><select id='dictionarySynonymList'></select></div></div>");         
        $("#dictionarySynonymsDiv").append("<div class='columnItem'><div class='rightbutton'><input id='dictionaryDeleteSynonymButton' type='button' value='Delete synonym' class='hbutton'></div>");                                                
        $("#dictionarySynonymsDiv").append("<div class='columnItem'><input id='dictionarySaveChangesButton' type='button' value='Save changes' class='majorbutton hbutton'><input id='dictionaryCancelChangesButton' type='button' value='Cancel changes' class='majorbutton hbutton'></div>");   
        dictionaryAddEventListeners();
    }
    
    function dictionaryDocumentReady(parent,sibling) {
        createDictionaryInterface(parent,sibling);
        updateRepresentativeTermList(dictionaryDocumentReadyCallback,null);   
        updateMetaDataInterface('KAdictionaryButton');
    }
    
    function dictionaryDocumentReadyCallback() {
        let theTerm = $("#dictionaryTermList").val();  
        getSynonymList(dictionaryDocumentReadyCallback2,theTerm);
    }
    
    function dictionaryDocumentReadyCallback2() {
        let theTerm = $("#dictionaryTermList").val();        
        updateRandomSynonymStatusList(updateRandomSynonymStatusListCallback,theTerm);        
    }
    
    function dictionaryMainDialogCloseCallback() {
        updateMetaDataInterface(null)
       
    }
   

    function termListChangeCallback(theTerm) {
        updateRandomSynonymCheckbox(theTerm);  
    }
    
    function getSynonymListEventHander(event) {
        let theCallback = event.data.theHandler;
        let theTerm = event.data.theValue;
        
        getSynonymList(theCallback,theTerm);
    }
    
    function dictionaryAddEventListeners() {
        if (!dictionaryEventListenersRegistered) {
            $('body').on('change','#dictionaryRandomSynonymCheckbox', true,disableTermChangeUntilSaved); 

            var theTerm =  $("#dictionaryTermList").val();
            let params = {theHandler:termListChangeCallback,theValue:theTerm};
            $('body').on('change','#dictionaryTermList',params,getSynonymListEventHander);
            
            $('body').on('click','#dictionarySaveChangesButton', processSaveChangesButton);
            $('body').on('click','#dictionaryCancelChangesButton', processCancelChangesButton);
            $('body').on('click','#dictionaryDeleteSynonymButton', processDeleteSynonymButton);
            $('body').on('click','#dictionaryNewTermButton', processNewTermButton);
            //$('body').on('click','#dictionaryCloseButton', processDictionaryCloseButton);
            $('body').on('click','#dictionaryShowAllSynonymButton', processDictionaryShowAllSynonymButton);
            
            $('body').on('click','#dictionaryNewSynonymButton', processNewSynonymButton);
            $('#dictionaryNewSynonym').keypress(function(e) {
            if (e.which === 13) {
                processNewSynonymButton();
            }
    });
            
            
            dictionaryEventListenersRegistered = true;
        }
    }

    function disableTermChangeUntilSaved(status) {
        $('#dictionaryTermList').prop('disabled',status);
        $('#dictionaryNewTerm').prop('disabled',status);
        $('#dictionaryNewTermButton').prop('disabled',status);
    }
    
    function updateRandomSynonymStatusList(callback,theTerm) {
        $.ajax({
            url : 'DictionaryServlet',
            //async: false,
            data : {
                mode : 'getRandomSynonymStatusList'
            },
            success : function(responseText) {
                if (responseText !== "false") {
                    let updatedStatusList = responseText.split(';');
                    setCurrentDictionaryStatuses(updatedStatusList);
                    callback(theTerm);
                }
            }
         });             
    }
    
    function updateRandomSynonymStatusListCallback(theTerm) {
        updateRandomSynonymCheckbox(theTerm);
    }
    
    function updateRandomSynonymCheckbox(theTerm) {
        if (theTerm === null) {
            $('#dictionaryTermList').prop("selectedIndex", 0);
            theTerm = $('#dictionaryTermList').val();
        }
        
        var index = $.inArray(theTerm,getCurrentDictionaryTerms());
        var currentStatuses = getCurrentDictionaryStatuses();
        var currentStatus = currentStatuses[index];
        if (currentStatus === "true") {
            $("#dictionaryRandomSynonymCheckbox").prop('checked',true);
        }
        else {
            $("#dictionaryRandomSynonymCheckbox").prop('checked',false);
        }    
    }
    
    function processDeleteSynonymButton() {
        var theTerm = $('#dictionaryTermList').val();
        var theSynonym = $('#dictionarySynonymList').val();

        if (theSynonym !== null) {
            $.ajax({
                 url : 'DictionaryServlet',
                 //async: false,
                 data : {
                     mode : 'deleteSynonym',
                     theTerm: theTerm,
                     theSynonym: theSynonym
                 },
                 success : function(responseText) {
                     if (responseText === "true") {
                        getSynonymList(processDeleteSynonymButtonCallback,theTerm);
                    }
                    else if (responseText === "deletingFalse") {
                        standardOK("confirmDialog", "Notice","Failed to delete synonym!")();
                        //alert("Failed to delete synonym!");
                    }
                    else {
                        standardOK("confirmDialog", "Error","Please load and initialise the knowledgebase first!")();

                        //alert("Please load and initialise the knowledgebase first!");
                    }
                 }
             }); 
        }
        else {
            standardOK("confirmDialog", "Error","No synonym was selected to be deleted!")();
            //alert("No synonym was selected to be deleted!");
        }
    }
    
    function processDeleteSynonymButtonCallback() {
        var size = $('#dictionarySynonymList  > option').length;
        if (size > 0) {
            $('#dictionarySynonymList').prop('selectedIndex',size-1);
        }
        disableTermChangeUntilSaved(true);    
    }
    
    function updateRepresentativeTermList(callback,theTerm){
        $.ajax({
             url : 'DictionaryServlet',
             dataType:'json',
             //async: false,
             data : {
                 mode : 'getRepresentativeTermList'
             },
            success : function(responseJSON) {
                if (responseJSON !== "false") { 
                    //let updatedTermList = responseText.split(";");
                    let updatedTermList = responseJSON;
                    setCurrentDictionaryTerms(updatedTermList);
                    $('#dictionaryTermList').empty();

                    $.each(updatedTermList,function(i,aTerm) {
                        if (aTerm.trim()) {
                           //alert("adding:'" + aTerm.trim() + "'");
                            $('#dictionaryTermList').append($('<option>', {
                                value: aTerm,
                                text: aTerm
                            }));
                        }
                    });                
                 }
                 
                 callback(theTerm);
            }
         }); 
         
         
    }
    
    function updateRepresentativeTermListCallback(theTerm) {
        if (theTerm !== null) {
             $('#dictionaryTermList').val(theTerm);
        }
        updateRandomSynonymStatusList(updateRandomSynonymStatusListCallback,theTerm);        
    }  
            
    function processSaveChangesButton() {
        var theTerm = $('#dictionaryTermList').val();
        var allowRandomSynonym;
        if ($("#dictionaryRandomSynonymCheckbox").is(':checked')) {
            allowRandomSynonym = 1;
        }
        else {
            allowRandomSynonym = 0;
        }
        $.ajax({
             url : 'DictionaryServlet',
             //async: false,
             data : {
                 mode : 'saveChanges',
                 theTerm: theTerm,
                 allowRandomSynonym: allowRandomSynonym
             },
             success : function(responseText) {
                if (responseText === "true") {
                    standardOK("confirmDialog", "Notice","Modifications saved for term " + theTerm)();
                    //alert("Modifications saved for term " + theTerm);
                    updateRepresentativeTermList(updateRepresentativeTermListCallback,theTerm);
                    //updateRandomSynonymStatusList(updateRandomSynonymStatusListCallback,theTerm);
                    //updateRandomSynonymCheckbox(theTerm);
                    disableTermChangeUntilSaved(false);

                }
                else {
                    standardOK("confirmDialog", "Error","Please load and initialise the knowledgebase first!")();
                    //alert("Please load and initialise the knowledgebase first!");                 
                }
             }                
        });
        
        
    }

    function processNewTermButton() {
        
        var newTerm = $("#dictionaryNewTerm").val();
        if (newTerm.length === 0) {
            standardOK("confirmDialog", "Error","Please enter a dictionary term value..")();

            //alert("Please enter a dictionary term value..");
            return;
        }
        if (newTerm.charAt(0) !== '/') {
            newTerm = "/" + newTerm;
        }
        if (newTerm.charAt(newTerm.length-1) !== '/') {
            newTerm = newTerm + "/";
        }
        
        newTerm = newTerm.replace(/ /g,'');
        
        $("#dictionaryNewTerm").val(newTerm);
        
        
        if (findExistingTerm(newTerm,getCurrentDictionaryTerms())===true) {
            standardOK("confirmDialog", "Error","Term already exists!")();
            //alert("Term already exists!");
        }
        else {
            var allowRandomSynonym;
            if ($("#dictionaryRandomSynonymCheckbox").is(':checked')) {
                allowRandomSynonym = 1;
            }
            else {
                allowRandomSynonym = 0;
            }
               
            $.ajax({
                 url : 'DictionaryServlet',
                 //async: false,
                 data : {
                     mode : 'addRepresentativeTerm',
                     theTerm: newTerm,
                     allowRandomSynonym: allowRandomSynonym
                 },
                 success : function(responseText) {
                     if (responseText === "true") {
                        $('#dictionaryTermList').prop('disabled', true);
                        $('#dictionaryNewTermButton').prop('disabled',true);
                        updateRepresentativeTermList(updateRepresentativeTermListCallback,newTerm);
                        $('#dictionarySynonymList').empty();
                        //alert("Trying to set selected to: '" + newTerm + "'");
                       // $('#dictionaryTermList').val(newTerm);
                        disableTermChangeUntilSaved(true);
                        $('#dictionaryNewTerm').empty();
                        standardOK("confirmDialog", "Notice","Please now add one or more synonyms..")();

                        //alert("Please now add at least one synonym..");
                    
                    }
                    else { 
                        standardOK("confirmDialog", "Error","Please load and initialise the knowledgebase first!")();

                        //alert("Please load and initialise the knowledgebase first!");                   
                    }
                 }                
             });
         }
     }
     
     
    function getSynonymList(callback,theTerm){
        if (theTerm === null) {     
            theTerm = $("#dictionaryTermList").val();
        }
        
        if (theTerm !== "") {
        
            $.ajax({
                 url : 'DictionaryServlet',
                 dataType:'json',
                 //async: false,
                 data : {
                     mode : 'getSynonymList',
                     theTerm: theTerm
                     //theTerm: $("#dictionaryTermList").val()
                 },
                 success : function(responseJSON) {
                    if (responseJSON !== "false") {
                        //let updatedSynonymList = responseJSON.split(';');
                        let updatedSynonymList = responseJSON;
                        setCurrentDictionarySynonyms(updatedSynonymList);

                        $('#dictionarySynonymList').empty();

                        $.each(updatedSynonymList,function(i,aTerm) {
                            if (aTerm.trim()) {
                                //alert("found synonym:'" + aTerm.trim() + "'");
                                $('#dictionarySynonymList').append($('<option>', {
                                    value: aTerm,
                                    text: aTerm
                                }));
                            }
                        });

                        callback(theTerm);
                    }
                 }
            });  
        }
        else {
            $('#dictionarySynonymList').empty();
        }
    }
    
    function processNewSynonymButtonCallback(theTerm) {
        var newSynonym = $("#dictionaryNewSynonym").val();

        if (newSynonym.length === 0) {
            standardOK("confirmDialog", "Error","Please enter a synonym value..")();

            //alert("Please enter a synonym value..");
            return;
        }
        
        if (findExistingTerm(newSynonym,getCurrentDictionarySynonyms())===true) {
            standardOK("confirmDialog", "Error","Cannot add synonym '" + newSynonym +"' as it already exists for this term!")();

                //alert("Cannot add synonym as it already exists for this term!");
        }
        else {
            
            $.ajax({
                url : 'DictionaryServlet',
                //async: false,
                data : {
                    mode : 'addNewSynonym',
                    theTerm: theTerm,
                    theSynonym: newSynonym
                },
                success : function(responseText) {
                    $('#dictionaryTermList').prop('disabled', false);
                    updateRepresentativeTermList(updateRepresentativeTermListCallback,theTerm);
                    if (responseText === "true") {
                        $('#dictionarySynonymList').append($('<option>', {
                                            value: newSynonym,
                                            text: newSynonym
                                        }));
                        $('#dictionarySynonymList').val(newSynonym); 
                        $('#dictionaryNewSynonym').val(''); 
                        $('#dictionaryNewTermButton').prop('disabled',false);
                        disableTermChangeUntilSaved(true);
                    }
                    else if (responseText === "patternFalse") {
                        standardOK("confirmDialog", "Error","Failed to add synonym regular expression '" + newSynonym +"' as it contains errors!")();
                        //alert("Failed to add synonym regular expression as it contains errors!");
                    }
                    else if (responseText === "addingFalse") {
                        standardOK("confirmDialog", "Error","Failed to add synonym '" + newSynonym + "' as it already exists for this term!")();

                        //alert("Failed to add synonym as it already exists for this term!");
                    }
                    else {
                        standardOK("confirmDialog", "Error","Please load and initialise the knowledgebase first!")();

                        //alert("Please load and initialise the knowledgebase first!");
                    }
                }
            });  
        }    
    }
    
    function processNewSynonymButton() { 
        getSynonymList(processNewSynonymButtonCallback, $("#dictionaryTermList").val());
    }
          
    function processCancelChangesButton() {
        disableTermChangeUntilSaved(false);
        standardOK("confirmDialog", "Notice","Synonym modifications for this term will not be saved to the kb, however any modifications recently made will remain in effect until the next kb reload")();

        //alert("Synonym modifications for this term will not be saved to the kb, however any modifications recently made will remain in effect until the next kb reload");
    } 
    
//    function processDictionaryCloseButton() {
//        updateMetaDataInterface(null)
//        showAndHide(getDictionarySiblingMain(),getDictionaryMain(),true);
//    }
    
    function processDictionaryShowAllSynonymButton() {
        getAllSynonymsList(processDictionaryShowAllSynonymButtonCallback); 
    }
    
    function getAllSynonymsList(callback) {
        let dataList = {mode:"getAllSynonymsList",
            theTerm: $("#dictionaryTermList").val()      
        };
        generalAjaxQuery('DictionaryServlet',callback,dataList);
    }
    
    function processDictionaryShowAllSynonymButtonCallback(theResponse) {
        let header = theResponse['header'];
        let rows = theResponse['rows'];
        let body = createBodyTableViewer(header, [200],rows);
        standardOKLarge("confirmDialog", "All synonyms",body)(); 
    }
    
