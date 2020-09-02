    var conclusionMain = "KAconclusionsWrapper";
    
    function getConclusionMain() {
        return conclusionMain;
    }
    
    var conclusionEventListenersRegistered = false;

    function conclusionAddEventListeners() {
        if (!conclusionEventListenersRegistered) {
            
            /*********************************************************************/
            /*********************** Existing conclusion  ************************/
            /*********************************************************************/
            // Existing conclusion - selecting an existing conclusion
            $('body').on('click','.conclusionline', function(e) {       
                let theID = 0;
                
                
                let theSelectedTarget = e.target.id.toString();
                
                if (theSelectedTarget.startsWith('conclusionline')) {
                    theID = theSelectedTarget.substring(14);
                }
                else {
                    theID = $(e.target).parent().prop('id').substring(14);
                }
                                          
                
                /*
                if ($(e.target).is('.KAConclusion'))
                    theID = e.target.id.toString().substring(10);
                else if ($(e.target).is('.KAActionCategory'))
                    theID = e.target.id.toString().substring(14);
                else if ($(e.target).is('.KAAction'))
                    theID = e.target.id.toString().substring(6);
                */
                updateConclusionInfo(theID);
            });
            
            // Existing conclusion - selected conclusion submission
            $('body').on('click','#KAsetConclusionButton', function(e) {  
                if ($("#conclusion").val() === "") {
                    standardOK("confirmDialog",
                                "Error",
                                "You have not specified a conclusion."
                            )();
                }
                else {
                    constructConclusion(knowledgeAcquisitionConclusionSubmittedCallback);
                }
            });

            /*********************************************************************/
            /*********************** New conclusion  *****************************/
            /*********************************************************************/
            // New conclusion - add a new conclusion
            $('body').on('click','#KAnewConclusionButton', function(e) { 
                createNewConclusionInterface("metaDataDialog2",null);
                standardProcessDialog('metaDataDialog2', "New Conclusion Creation",newConclusionMainDialogCloseCallback)();
            });  

            
            $('body').on('click','#KAreturnToNewConclusionButton', function(e) {  
                showAndHide("KANewConclusionWrapper","KApreviewNewConclusion",true,true);
            });
            
            conclusionEventListenersRegistered = true;
        }
    }

    function createConclusionInterface(parentDiv) {
        $("#" + parentDiv).append("<div class='columnHeading'>Rule Conclusion Selection</div>");
        $("#" + parentDiv).append("<div id='KAConclusionsTable'></div");
        
        // Table headers
        $("#KAConclusionsTable").append("<div class='columnHeading'>Existing Conclusions</div>");
        $("#KAConclusionsTable").append("<div class='tableHeader KAConclusion'>Conclusion</div>");
        $("#KAConclusionsTable").append("<div class='tableHeader KAActionCategory'>Action Category</div>");
        $("#KAConclusionsTable").append("<div class='tableHeader KAAction'>Action</div>");
        
        // body of table
        $("#KAConclusionsTable").append("<div id='KAConclusions'></div");
        
        // Add new conclusion button
        //$("#KAConclusionsTable").append("<div id='KAnewConclusion'><input id='KAnewConclusionButton' type='button' value='Add new conclusion..'></div");
        $("#KAConclusionsWrapper").append("<div id='KAnewConclusion'><input id='KAnewConclusionButton' type='button' value='Add new conclusion..'></div");
        
        
        // populate the table body
        populateKAConclusionList('KAConclusions');
        
        // selected conclusion     
        $("#KAConclusionsWrapper").append("<div id='KAselections'></div>");
        $("#KAselections").append("<div class='columnHeading'>Current selected conclusion</div>");
        $("#KAselections").append("<div id='KASelectedConclusion'>Selected Conclusion: <input id='conclusion'></div");
        $("#KAselections").append("<div id='KASelectedCategory'>Selected Action Category: <input id='category'></div");
        $("#KAselections").append("<div id='KASelectedAction'>Selected Action: <input id='action'></div");
        $("#KAConclusionsWrapper").append("<div id='KAsetConclusion'><input id='KAsetConclusionButton' type='button' value='Submit selected conclusion..'></div");          
        
        conclusionAddEventListeners();
    }            
    
    
    
    // selected conclusion update
    function updateConclusionInfo(conclusionID) {
        let aConclusion = $("#conclusion" + conclusionID).html();
        let aCategory = $("#actionCategory" + conclusionID).html();
        let anAction = $("#action" + conclusionID).html();
        if (aCategory === "&nbsp;")
            aCategory = "";
        if (anAction === "&nbsp;")
            anAction = "";
        $("#conclusion").val(unescapeHTML(aConclusion));
        $("#category").val(aCategory);
        $("#action").val(anAction);
    }
       
    
    
    function constructConclusion(callback) {
        
        var conclusion = $("#conclusion").val();
        var actionCategory = $("#category").val();
        var action = $("#action").val();
        
        $.ajax({
            type: 'GET',
            url : 'KnowledgeAcquisitionServlet',
            //async: false,
            data : {
                mode : 'constructConclusion',
                conclusion: conclusion,
                actionCategory: actionCategory,
                action: action                            
            },
            
            success : function(response) {
                if (response === "true")
                    callback();
            }
        });  
    }
       
    
    