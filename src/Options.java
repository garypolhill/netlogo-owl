/*
 * : Options.java
 * 
 * Copyright (C) 2013 The James Hutton Institute
 * 
 * This file is part of NetLogo2OWL.
 * 
 * NetLogo2OWL is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * NetLogo2OWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with NetLogo2OWL. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contact information: Gary Polhill, The James Hutton Institute,
 * Craigiebuckler, Aberdeen. AB15 8QH. UK. gary.polhill@hutton.ac.uk
 */

import java.util.HashSet;
import java.util.Set;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Primitive;
import org.nlogo.api.Syntax;

/**
 * <!-- Options -->
 * 
 * <p>
 * Repository for various options for creating the OWL ontology. There are the
 * following options:
 * </p>
 * 
 * <ul>
 * <li><code>"owl2"</code> -- Include OWL 2 assertions (e.g. property chains)</li>
 * <li><code>"relations"</code> -- Include relational attribute assertions (e.g.
 * asymmetric)</li>
 * <li><code>"no-parcels"</code> -- Suppress spatial assertions</li>
 * <li><code>"none"</code> -- Assert that none of the above options are required
 * (the default)</li>
 * </ul>
 * 
 * <p>
 * There are no restrictions on when and how often the command can be run
 * (except that it must be run by the observer agent). The specified options
 * will be honoured by any subsequent calls to
 * <code>owl:{@link Structure}<code> or <code>owl:
 * {@link State}<code>.
 * </p>
 * 
 * @author Gary Polhill
 */
public class Options extends DefaultCommand implements Primitive {

  /**
   * Name for the "owl2" option
   */
  public static final String OWL2_OPTION = "owl2";

  /**
   * Name for the "relations" option
   */
  public static final String RELATIONS_OPTION = "relations";

  /**
   * Name for the "no-patches" option
   */
  public static final String NO_PATCHES_OPTION = "no-patches";

  /**
   * Name for the "none" option
   */
  public static final String NO_OPTIONS = "none";

  /**
   * Set of valid options
   */
  public static final Set<String> VALID_OPTIONS = new HashSet<String>();
  {
    VALID_OPTIONS.add(OWL2_OPTION);
    VALID_OPTIONS.add(RELATIONS_OPTION);
    VALID_OPTIONS.add(NO_PATCHES_OPTION);
    VALID_OPTIONS.add(NO_OPTIONS);
  };

  /**
   * Current set of options in the program
   */
  private Set<String> options;

  /**
   * Constructor
   */
  public Options() {
    options = new HashSet<String>();
  }

  /**
   * <!-- getSyntax -->
   * 
   * The syntax of this command is variadic, with at least one string containing
   * the name of an option
   * 
   * @see org.nlogo.api.DefaultCommand#getSyntax()
   * @return Syntax of this command
   */
  @Override
  public Syntax getSyntax() {
    return Syntax.commandSyntax(new int[] { Syntax.StringType() | Syntax.RepeatableType() }, 1);
  }

  /**
   * <!-- getAgentClassString -->
   * 
   * The command can only be run from the observer
   * 
   * @see org.nlogo.api.DefaultCommand#getAgentClassString()
   * @return String indicating as much
   */
  @Override
  public String getAgentClassString() {
    return "O";
  }

  /**
   * <!-- perform -->
   * 
   * Check the option(s) is (are) valid, and set them.
   * 
   * @see org.nlogo.api.Command#perform(org.nlogo.api.Argument[],
   *      org.nlogo.api.Context)
   * @param args
   * @param context
   * @throws ExtensionException
   * @throws LogoException
   */
  @Override
  public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
    options.clear();
    for(Argument arg: args) {
      String str = arg.getString();
      if(VALID_OPTIONS.contains(str)) {
        if(options.contains(str)) {
          throw new ExtensionException("Option " + str + " specified (at least) twice");
        }
        options.add(str);
      }
      else {
        throw new ExtensionException("Invalid argument to options command: \"" + str
          + "\". Each option must be one of: " + VALID_OPTIONS);
      }
    }

    // Confirm that "none" has only been used once, and remove it from the
    // option set. (The option set should be empty if no options are specified.)
    if(options.contains(NO_OPTIONS)) {
      if(options.size() > 1) throw new ExtensionException("Can't have option \"none\" and other options");
      options.remove(NO_OPTIONS);
    }

  }

  /**
   * <!-- hasOption -->
   * 
   * @param arg An option string
   * @return <code>true</code> if the option <code>arg</code> has been
   *         specified.
   * @throws IllegalArgumentException if <code>arg</code> is <code>null</code>
   *           or the <code>"none"</code> option. To test for the latter use
   *           {@link #noOptions()}
   */
  boolean hasOption(String arg) {
    if(arg == null || arg.equals(NO_OPTIONS)) throw new IllegalArgumentException(arg);
    return options.contains(arg);
  }

  /**
   * <!-- noOptions -->
   * 
   * @return <code>true</code> if the user stipulated no options
   */
  boolean noOptions() {
    return options.size() == 0;
  }

}
