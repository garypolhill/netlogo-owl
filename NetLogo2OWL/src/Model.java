/*
 * : Model.java
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

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Primitive;
import org.nlogo.api.Syntax;
import org.semanticweb.owlapi.model.IRI;

/**
 * <!-- Model -->
 * 
 * @author Gary Polhill
 */
public class Model extends DefaultCommand implements Primitive {
  private OWLExtension extension = null;

  /**
   * <!-- setExtension -->
   * 
   * The extension is used to store the imports to that other commands can
   * access them
   * 
   * @param extension The OWLExtension object
   */
  public void setExtension(OWLExtension extension) {
    this.extension = extension;
  }

  /**
   * <!-- getSyntax -->
   * 
   * The syntax of this command is variadic, with at least one string containing
   * an imported ontology IRI
   * 
   * @see org.nlogo.api.DefaultCommand#getSyntax()
   * @return Syntax of this command
   */
  @Override
  public Syntax getSyntax() {
    return Syntax.commandSyntax(new int[] { Syntax.StringType() });
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
   * @see org.nlogo.api.Command#perform(org.nlogo.api.Argument[],
   *      org.nlogo.api.Context)
   * @param args
   * @param context
   * @throws ExtensionException
   * @throws LogoException
   */
  @Override
  public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
    if(extension == null) {
      throw new ExtensionException("Bug: Extension not initialised properly (NetLogo/OWL extension fault)");
    }
    if(extension.getModelIRI() != null) {
      throw new ExtensionException("You may not set the owl:model IRI more than once");
    }

    extension.setModelIRI(IRI.create(args[0].getString()));
  }

}
