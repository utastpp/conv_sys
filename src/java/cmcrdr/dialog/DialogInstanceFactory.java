
package cmcrdr.dialog;

import cmcrdr.logger.Logger;

public class DialogInstanceFactory {

    /**
     *
     */
    protected static String classPath = "cmcrdr.dialog.";

    /**
     *
     * @param name
     * @return
     */
    public static IDialogInstance createDialogInstance(String name) {
        IDialogInstance dialogInstance = null;
        try {
            dialogInstance = (IDialogInstance) Class.forName(classPath + name + "Instance").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            Logger.info("There was a problem creating the dialog instance..");
        }
        return dialogInstance;
    }
    
}
