package org.coinor.opents;


/**
 * This class runs if someone mistakenly double-clicks on the OpenTS_10.jar file
 * or runs <tt>java -jar OpenTS_10.jar</tt>.
 *
 *
 * p><em>This code is licensed for public use under the Common Public License version 0.5.</em><br/>
 * The Common Public License, developed by IBM and modeled after their industry-friendly IBM Public License,
 * differs from other common open source licenses in several important ways:
 * <ul>
 *  <li>You may include this software with other software that uses a different (even non-open source) license.</li>
 *  <li>You may use this software to make for-profit software.</li>
 *  <li>Your patent rights, should you generate patents, are protected.</li>
 * </ul>
 * </p>
 * <p><em>Copyright © 2002 Robert Harder</em></p>
 *
 * @author Robert Harder
 * @author rharder@usa.net
 * @since 1.0
 * @version 1.0 exp7
 */
public class Main
{   
    /**
     * OpenTS version number.
     *
     * @since 1.0
     */
    public final static String VERSION = "1.0 exp7";
    
    
    private final static String[] credits =
        { "OpenTS - Java Tabu Search",
          "Version " + VERSION,
          "Robert Harder",
          "rharder@usa.net",
          "",
          "Use these classes to help build a ",
          "structured and efficient Java tabu search."
        };
         
    /**
     * Returns a {@link java.lang.String} representation of the OpenTS version number.
     *
     * @return version
     * @since 1.0
     */
    public static String getVersion()
    {   return VERSION;
    }   // end getVersion
        
        
    
    @SuppressWarnings("deprecation")
	public static void main( String[] args )
    {   
        System.out.println("");
        for( int i = 0; i < credits.length; i++ )
            System.out.println( credits[i] );
        System.out.println("");
        
        try
        {   java.awt.Frame f = new java.awt.Frame( credits[0] );
            f.setLayout( new java.awt.GridLayout(credits.length,1) );
            for( int i = 0; i < credits.length; i++ )
                f.add( new java.awt.Label( credits[i], java.awt.Label.CENTER ) );
            f.pack();
            java.awt.Toolkit t = f.getToolkit();
            f.setLocation( (t.getScreenSize().width-f.getSize().width)/2, (t.getScreenSize().height-f.getSize().height)/2 );
            f.addWindowListener( new java.awt.event.WindowAdapter()
            {   public void windowClosing( java.awt.event.WindowEvent e )
                {   System.exit(0);
                }   // end windowClosed
            }); // end WindowAdapter
            f.show();
        }   // end try
        catch( Exception e ){}
    }   // end main
}   // end class Main

