/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.knowledgeacquisition;

import cmcrdr.logger.Logger;
import java.util.ArrayList;
import java.util.Arrays;

public class TableResponse {
    String status;
    ArrayList<String> header = new ArrayList<>();
    ArrayList<ArrayList<String>> rows = new ArrayList<>();

    public void setHeader(String[] theHeader) {
        for (String aHeaderItem: theHeader) {
            header.add(aHeaderItem);               
        }
    }

    public ArrayList<ArrayList<String>> getRows() {
        return rows;
    }

    public String[] getRowsAsStringArray() {
        String[] response = new String[rows.size()];
        int i = 0;

        for (ArrayList<String> aRow: rows) {
            String columns = new String();
            for (String aColumn: aRow) {
                columns += aColumn;
            }
            //Logger.info("Adding column data:" + columns);
            response[i] = columns;
            i++;
        }

        return response;
    }
    
    public void setSingleColumnRows(String[] theRows) {
        ArrayList<String> row;
        if (header.size() == 1) {
           for (String aRowItem: theRows) {
               row = new ArrayList<>();
               row.add(aRowItem);
               rows.add(row);             
            } 
        }       
    }

    public void setRows(ArrayList<ArrayList<String>> newRows) {
        rows = newRows;
    }

    /*public String[] getHeader() {
        return header.toArray(new String[header.size()]);
    }*/

    public void addRow(String[] aRow) {
        ArrayList<String> row = new ArrayList<>();
        if (aRow.length <= header.size()) {
            for (String aRowItem: aRow) {
                //Logger.info("TableResponse: Adding row item:" + aRowItem);
                row.add(aRowItem);
            }
            rows.add(row);
        }
    }

    public void setStatus(String theStatus) {
        status = theStatus;
    }
}