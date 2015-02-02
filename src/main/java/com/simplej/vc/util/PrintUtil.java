/*
 PrintUtil.java
 Copyright (C) 2004 Jorge Vargas

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.simplej.vc.util;

import java.awt.*;
import java.io.*;
import java.io.StringReader;

public class PrintUtil {

    public static void print(PrintJob pjob, Graphics pg, String s,
                             String header, String footer) {
        int pageNum = 1;
        int linesForThisPage = 0;
        int linesForThisJob = 0;
        if (!(pg instanceof PrintGraphics)) {
            throw new IllegalArgumentException(
                                         "Graphics context not PrintGraphics");
        }
        StringReader sr = new StringReader(s);
        LineNumberReader lnr = new LineNumberReader(sr);
        int pageHeight = pjob.getPageDimension().height;
        int pageWidth = pjob.getPageDimension().width;
        Font font = new Font("monospaced", Font.PLAIN, 12);
        pg.setFont(font);
        FontMetrics fm = pg.getFontMetrics(font);
        int fontHeight = fm.getHeight();
        int fontDescent = fm.getDescent();
        String separator = generateSeparator(pageWidth - 2);
        int curHeight = 0;
        try {
            String nextLine = lnr.readLine();
            while (nextLine != null) {
                if ((curHeight + fontHeight * 4) > pageHeight) {
                    curHeight += fontHeight * 2;
                    pg.drawString(separator, 1, curHeight - fontDescent);
                    curHeight += fontHeight;
                    pg.drawString(footer, 0, curHeight - fontDescent);
                    String page = "Page " + pageNum;
                    pg.drawString(page, (int) (pageWidth - page.length())
                                  / 2,
                                  curHeight - fontDescent);
                    pageNum++;
                    linesForThisPage = 0;
                    pg.dispose();
                    pg = pjob.getGraphics();
                    curHeight = 0;
                    if (pg != null) {
                        pg.setFont(font);
                    } else {
                        break;
                    }
                }
                if (linesForThisPage == 0) {
                    curHeight += fontHeight;
                    pg.drawString(header, 0, curHeight - fontDescent);
                    curHeight += fontHeight;
                    pg.drawString(separator, 1, curHeight - fontDescent);
                    curHeight += fontHeight;
                }
                curHeight += fontHeight;
                pg.drawString(nextLine, 0, curHeight - fontDescent);
                linesForThisPage++;
                linesForThisJob++;
                nextLine = lnr.readLine();
            }
            pg.drawString(footer, 0, pageHeight - fontDescent);
            String page = "Page " + pageNum;
            pg.drawString(page, (int) (pageWidth - page.length()) / 2,
                          pageHeight - fontDescent);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    private static String generateSeparator(int count) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < count; i++)
            sb.append("-");
        return sb.toString();
        
    }

}

