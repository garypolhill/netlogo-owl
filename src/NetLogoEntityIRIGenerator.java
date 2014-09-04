/*
 * : NetLogoEntityIRIGenerator.java
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

import org.nlogo.api.ExtensionException;
import org.semanticweb.owlapi.model.IRI;

/**
 * <!-- NetLogoEntityIRIGenerator -->
 * 
 * Interface to follow for IRI generators
 * 
 * @author Gary Polhill
 */
public interface NetLogoEntityIRIGenerator {
  /**
   * <!-- getEntityIRI -->
   * 
   * Generate a standardised IRI for an entity and automatically manage
   * capitalisation and differences between OWL legal names and NetLogo legal
   * names.
   * 
   * @param iri Ontology IRI
   * @param nlEntity NetLogo entity name
   * @param isClass <code>true</code> if the entity is a class (in which case,
   *          its first letter will be uppercase, and all others lower);
   *          <code>false</code> otherwise (in which case, all letters will be
   *          lower case)
   * @return an IRI
   * @throws ExtensionException
   */
  public IRI getEntityIRI(IRI iri, String nlEntity, boolean isClass) throws ExtensionException;

  /**
   * <!-- getEntityIRI -->
   * 
   * Generate a standardised IRI from a NetLogo entity in the namespace of the
   * model structure ontology
   * 
   * @param nlEntity
   * @param isClass
   * @return
   * @throws ExtensionException
   */
  public IRI getEntityIRI(String nlEntity, boolean isClass) throws ExtensionException;

  /**
   * <!-- getGlobalIRI -->
   * 
   * @return The IRI of the 'global entity', an OWL individual that has all the
   *         global variables
   * @throws ExtensionException
   */
  public IRI getGlobalIRI() throws ExtensionException;

  /**
   * <!-- hasDomainSpecified -->
   * 
   * @param link NetLogo link
   * @return <code>true</code> if the link has a domain specified by the owl:
   *         {@link Domain} command
   */
  public boolean hasDomainSpecified(String link);

  /**
   * <!-- hasRangeSpecified -->
   * 
   * @param link NetLogo link
   * @return <code>true</code> if the link has a range specified by the owl:
   *         {@link Range} command
   */
  public boolean hasRangeSpecified(String link);

  /**
   * <!-- getDomain -->
   * 
   * @param link NetLogo link
   * @return The name of the breed specified as the domain of the link
   */
  public String getDomain(String link);

  /**
   * <!-- getRange -->
   * 
   * @param link NetLogo link
   * @return The name of the breed specified as the range of the link
   */
  public String getRange(String link);
}
