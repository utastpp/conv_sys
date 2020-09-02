/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.gui;

/**
 *
 * @author dherbert
 */
public abstract interface UserInterface {
    public abstract String getInterfaceName();
    public abstract void addUser(String username);
    public abstract void removeUser(String username);
    public abstract void removeUserSessionHasExpired(String username);
    public abstract void updateInterfaceContent(String username);
    //public abstract void execute();
}
