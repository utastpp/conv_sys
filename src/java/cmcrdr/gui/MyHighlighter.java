package cmcrdr.gui;


import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
class MyHighlighter extends DefaultHighlighter
{
    private JTextComponent jTComponent;

    /**
     * @see javax.swing.text.DefaultHighlighter#install(javax.swing.text.JTextComponent)
     */
    @Override
    public final void install(final JTextComponent c)
    {
        super.install(c);
        this.jTComponent = c;
    }

    /**
     * @see javax.swing.text.DefaultHighlighter#deinstall(javax.swing.text.JTextComponent)
     */
    @Override
    public final void deinstall(final JTextComponent c)
    {
        super.deinstall(c);
        this.jTComponent = null;
    }

    /**
     * Same algo, except width is not modified with the insets.
     * 
     * @see javax.swing.text.DefaultHighlighter#paint(java.awt.Graphics)
     */
    @Override
    public final void paint(final Graphics g)
    {
        final Highlighter.Highlight[] highlights = getHighlights();
        final int len = highlights.length;
        for (int i = 0; i < len; i++)
        {
            Highlighter.Highlight info = highlights[i];
            if (info.getClass().getName().indexOf("LayeredHighlightInfo") > -1)
            {
                // Avoid allocing unless we need it.
                final Rectangle a = this.jTComponent.getBounds();
                final Insets insets = this.jTComponent.getInsets();
                a.x = insets.left;
                a.y = insets.top;
                // a.width -= insets.left + insets.right + 100;
                a.height -= insets.top + insets.bottom;
                for (; i < len; i++)
                {
                    info = highlights[i];
                    if (info.getClass().getName().indexOf(
                            "LayeredHighlightInfo") > -1)
                    {
                        final Highlighter.HighlightPainter p = info
                                .getPainter();
                        p.paint(g, info.getStartOffset(), info
                                .getEndOffset(), a, this.jTComponent);
                    }
                }
            }
        }
    }
}