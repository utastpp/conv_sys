/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dialog;

import cmcrdr.event.EventInstance;


public interface IDialogInstance {

    /**
     *
     * @return
     */
    public IDialogInstance cloneDialogInstance();

    /**
     *
     * @param dialogId
     */
    public void setDialogId(int dialogId);    

    /**
     *
     * @return
     */
    public int getDialogId();

    /**
     *
     * @param dialogType
     */
    public void setDialogType(String dialogType);    

    /**
     *
     * @return
     */
    public String getDialogTag();    

    /**
     *
     * @return
     */
    public String getDialogType();

    /**
     *
     * @param dialogTypeCode
     */
    public void setDialogTypeCode(int dialogTypeCode);

    /**
     *
     * @return
     */
    public int getDialogTypeCode();

    /**
     *
     * @param dialogStr
     */
    public void setDialogStr(String dialogStr);

    /**
     *
     * @return
     */
    public String getDialogStr();

    /**
     *
     * @param caseId
     */
    public void setGeneratedCaseId(int caseId);

    /**
     *
     * @return
     */
    public int getGeneratedCaseId();

    /**
     *
     * @param caseId
     */
    public void setDerivedCaseId(int caseId);

    /**
     *
     * @return
     */
    public int getDerivedCaseId();

    /**
     *
     * @param stackId
     */
    public void setStackId(int stackId);

    /**
     *
     * @return
     */
    public int getStackId();

    /**
     *
     * @param isProcessingNull
     */
    public void setIsRuleFired(boolean isProcessingNull);

    /**
     *
     * @return
     */
    public boolean getIsRuleFired();

    /**
     *
     * @param isLastRuleNode
     */
    public void setIsLastRuleNode(boolean isLastRuleNode);

    /**
     *
     * @return
     */
    public boolean getIsLastRuleNode();

    /**
     *
     * @param eventInstance
     */
    public void setEventInstance(EventInstance eventInstance);

    /**
     *
     * @return
     */
    public EventInstance getEventInstance();

    /**
     *
     * @return
     */
    @Override
    public String toString();

}
