/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.gui;

import cmcrdr.dialog.DialogInstance;
import cmcrdr.dialog.IDialogInstance;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class MyListCellRenderer extends JLabel implements ListCellRenderer<Object> {

    /**
     *
     */
    public MyListCellRenderer() {
         setOpaque(true);
     }

     @Override
     public Component getListCellRendererComponent(JList<?> list,
                                                   Object value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus) {

         setText(value.toString());
         
         Color background;
         Color foreground;

         // check if this cell repres
         if (isSelected) {
             background = Color.LIGHT_GRAY;
             foreground = Color.WHITE;

         // unselected, and not the DnD drop location
         } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
             if(((IDialogInstance)value).getDialogTypeCode()==DialogInstance.SYSTEM_TYPE){
                if( ((IDialogInstance)value).getIsLastRuleNode()){
                   background = Color.RED;
                   foreground = Color.WHITE;  
                }
             } 
         };

         setBackground(background);
         setForeground(foreground);

         return this;
     }
 }
