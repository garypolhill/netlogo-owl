/*
 * : Structure.java
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nlogo.api.Agent;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Link;
import org.nlogo.api.LogoException;
import org.nlogo.api.Primitive;
import org.nlogo.api.Program;
import org.nlogo.api.Syntax;
import org.nlogo.api.World;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

/**
 * <!-- Structure -->
 * 
 * <p>
 * Build a structure ontology. NetLogo breeds are mapped to OWL classes, NetLogo
 * links to OWL object properties (unless they have "own" variables, in which
 * case the link is reified), and NetLogo "own" variables to OWL data
 * properties. Patches are an OWL class.
 * </p>
 * 
 * <p>
 * The ontology is designed to have the minimum number of 'standard' entities
 * possible, but these are inevitably needed if patches are included; the
 * #location object property records the location of a Turtle if it isn't
 * hidden, and the #x and #y data properties record co-ordinates of Turtles and
 * Patches. To switch off spatial data, use the <code>"no-patches"</code>
 * option, which will suppress the declaration of these entities.
 * </p>
 * 
 * <p>
 * NetLogo links can be directed or undirected, and the directed links can be
 * asserted as symmetric (since aRb <=> bRa). Undirected links can be asserted
 * as asymmetric, and both kinds of link as irreflexive, since in NetLogo
 * turtles cannot link to themselves. These relational attributes are not
 * included by default, but the <code>"relations"</code> option enables them.
 * Axioms asserting asymmetric and irreflexive object properties were only
 * introduced in OWL 2. To include them, the <code>"owl2"</code> option should
 * also be stipulated. This will further introduce property chain axioms for
 * reified links.
 * </p>
 * 
 * @author Gary Polhill
 */
public class Structure extends DefaultCommand implements Primitive {

  /**
   * Netlogo's plabel-color variable name for the colour of the patch label
   */
  public static final String NETLOGO_PLABEL_COLOR_VAR = "plabel-color";

  /**
   * Netlogo's plabel variable name for the label of the patch
   */
  public static final String NETLOGO_PLABEL_VAR = "plabel";

  /**
   * Netlogo's pcolor variable name for the patch colour
   */
  public static final String NETLOGO_PCOLOR_VAR = "pcolor";

  /**
   * Netlogo's pxcor variable name for the x co-ordinate of the patch
   */
  public static final String NETLOGO_PXCOR_VAR = "pxcor";

  /**
   * Netlogo's pycor variable name for the y co-ordinate of the patch
   */
  public static final String NETLOGO_PYCOR_VAR = "pycor";

  /**
   * Class name to use for patches
   */
  public static final String PATCH_CLASS = "Patch";

  /**
   * Class name to use for turtles if there are no breeds
   */
  public static final String TURTLE_CLASS = "Turtle";

  /**
   * Property name to use for links if there are no link breeds
   */
  public static final String LINK_PROPERTY = "link";

  /**
   * Property name to use to state that turtles are located in a particular
   * patch
   */
  public static final String LOCATION_PROPERTY = "location";

  /**
   * Property name used for X co-ordinate
   */
  public static final String X_PROPERTY = "x";

  /**
   * Property name used for Y co-ordinate
   */
  public static final String Y_PROPERTY = "y";

  /**
   * Link to the OWLExtension object to access ontology information set by other
   * commands
   */
  private OWLExtension extension = null;

  /**
   * <!-- getSyntax -->
   * 
   * The Structure command takes as argument the name of a file to save the
   * ontology to.
   * 
   * @see org.nlogo.api.DefaultCommand#getSyntax()
   * @return Syntax
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
   * Implement the command. Create an ontology, inspect the program to add
   * axioms to it, and then save it.
   * 
   * @see org.nlogo.api.Command#perform(org.nlogo.api.Argument[],
   *      org.nlogo.api.Context)
   * @param args Arguments to the command (logical URI and physical file to save
   *          it to)
   * @param context Context of the command (should be observer)
   * @throws ExtensionException
   * @throws LogoException
   */
  @Override
  public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
    if(extension == null) {
      throw new ExtensionException("Bug: Extension not initialised properly (NetLogo/OWL extension fault)");
    }
    IRI logicalIRI = extension.getModelIRI();
    if(logicalIRI == null) {
      throw new ExtensionException("You must set the model IRI using the owl:model command");
    }

    String physical = args[0].getString();
    IRI physicalIRI = IRI.create(new File(physical));

    Agent observer = context.getAgent();
    World world = observer.world();
    Program program = world.program();

    try {
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLDataFactory factory = OWLManager.getOWLDataFactory();
      OWLOntology ontology = manager.createOntology(logicalIRI);

      Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
      for(IRI iri: extension.imports()) {
        manager.applyChange(new AddImport(ontology, factory.getOWLImportsDeclaration(iri)));
      }

      axioms.addAll(getBreedAxioms(logicalIRI, factory, program, extension));
      axioms.addAll(getPatchAxioms(logicalIRI, factory, program, extension, extension.getOptions()));
      axioms.addAll(getLinkAxioms(logicalIRI, factory, program, world, extension, extension.getOptions()));
      axioms.addAll(getGlobalAxioms(logicalIRI, factory, program, extension));
      manager.addAxioms(ontology, axioms);
      manager.saveOntology(ontology, physicalIRI);
    }
    catch(OWLOntologyCreationException e) {
      throw new ExtensionException("Cannot create OWL ontology \"" + logicalIRI + "\": " + e);
    }
    catch(OWLOntologyStorageException e) {
      throw new ExtensionException("Cannot save OWL ontology \"" + logicalIRI + "\" to \"" + physical + "\": " + e);
    }
  }

  /**
   * <!-- getGlobalAxioms -->
   * 
   * Get all the axioms declaring global variables
   * 
   * @param logicalIRI IRI of the ontology
   * @param factory OWLDataFactory to build axioms with
   * @param program NetLogo program to inspect
   * @param generator IRI generator
   * @return DataProperty axioms asserted for each of the globals
   * @throws ExtensionException
   */
  public static Set<OWLAxiom> getGlobalAxioms(IRI logicalIRI, OWLDataFactory factory, Program program,
      NetLogoEntityIRIGenerator generator) throws ExtensionException {
    Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

    Set<String> globals = new HashSet<String>();
    globals.addAll(program.globals());

    for(String global: globals) {
      axioms.add(factory.getOWLDeclarationAxiom(factory.getOWLDataProperty(generator.getEntityIRI(global, false))));
    }

    return axioms;
  }

  /**
   * <!-- getLinkAxioms -->
   * 
   * Create axioms for links. If there are no link breeds, create default axioms
   * for NetLogo 'links'.
   * 
   * @param logicalIRI IRI of the ontology
   * @param factory OWLDataFactory to build axioms with
   * @param program NetLogo program to inspect
   * @param world
   * @param generator IRI generator
   * @param options Options set from the options command
   * @return ObjectProperty and Class axioms (as appropriate) for each link
   *         breed
   */
  public static Set<OWLAxiom> getLinkAxioms(IRI logicalIRI, OWLDataFactory factory, Program program, World world,
      NetLogoEntityIRIGenerator generator, Options options) throws ExtensionException {
    Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

    Set<String> directedLinks = new HashSet<String>();
    Set<String> undirectedLinks = new HashSet<String>();

    if(options.hasOption(Options.RELATIONS_OPTION)) {

      // The following is a rather horrid way to get the list of directed and
      // undirected link breeds: Check the members of each link breed to see if
      // they are directed.

      for(Agent agent: world.links().agents()) {
        Link link = (Link)agent;

        if(link.isDirectedLink()) {
          directedLinks.add(link.getBreed().printName().toLowerCase());
        }
        else {
          undirectedLinks.add(link.getBreed().printName().toLowerCase());
        }
      }

      for(String linkBreed: directedLinks) {
        if(undirectedLinks.contains(linkBreed)) {
          throw new ExtensionException("Members of " + linkBreed + " are not consistently directed or undirected");
        }
      }
      for(String linkBreed: undirectedLinks) {
        if(directedLinks.contains(linkBreed)) {
          throw new ExtensionException("Members of " + linkBreed + " are not consistently directed or undirected");
        }
      }
    }

    if(program.linkBreeds().size() == 0) {
      addLink(axioms, logicalIRI, factory, LINK_PROPERTY, program.linksOwn(), generator, directedLinks,
          undirectedLinks, options);
    }
    else {
      for(String link: program.linkBreeds().keySet()) {
        String linkName =
          program.linkBreedsSingular().containsKey(link) ? program.linkBreedsSingular().get(link) : link;
        linkName = linkName.toLowerCase();

        addLink(axioms, logicalIRI, factory, linkName, program.linkBreedsOwn().get(link), generator, directedLinks,
            undirectedLinks, options);
      }
    }
    return axioms;
  }

  /**
   * <!-- addLink -->
   * 
   * Add a link to the ontology. If the link has its 'own' variables then we
   * have to implement the link as an intermediary class, with an inverse
   * functional object property linking from the 'from' agent, and a functional
   * property linking to the 'to' agent.
   * 
   * @param axioms Set of axioms to add to
   * @param logicalIRI Logical IRI of the ontology
   * @param factory OWLDataFactory to use to build axioms
   * @param link Name of the link
   * @param owns List of the link's 'own' variables
   * @param generator IRI generator
   * @param directedLinks Set of directed link names
   * @param undirectedLinks Set of undirected link names
   * @throws ExtensionException
   */
  private static void addLink(Set<OWLAxiom> axioms, IRI logicalIRI, OWLDataFactory factory, String link,
      List<String> owns, NetLogoEntityIRIGenerator generator, Set<String> directedLinks, Set<String> undirectedLinks,
      Options options) throws ExtensionException {
    String domain = generator.hasDomainSpecified(link) ? generator.getDomain(link) : null;
    String range = generator.hasRangeSpecified(link) ? generator.getRange(link) : null;

    OWLObjectProperty linkProp = factory.getOWLObjectProperty(generator.getEntityIRI(link, false));

    if(owns.size() == 0) {
      // No "owns": create the link
      addLinkProperty(axioms, logicalIRI, factory, link, domain, range, false, false, generator);
    }
    else {
      // Create a reified link
      String linkClassStr = link.substring(0, 1).toUpperCase() + link.substring(1);
      addClassAndProperties(axioms, logicalIRI, factory, linkClassStr, owns, generator);
      addLinkProperty(axioms, logicalIRI, factory, reifyOut(link), domain, linkClassStr, false, true, generator);
      addLinkProperty(axioms, logicalIRI, factory, reifyIn(link), linkClassStr, range, true, false, generator);

      if(options.hasOption(Options.OWL2_OPTION)) {
        // Create a property chain
        OWLObjectProperty link_Prop = factory.getOWLObjectProperty(generator.getEntityIRI(reifyOut(link), false));
        OWLObjectProperty _linkProp = factory.getOWLObjectProperty(generator.getEntityIRI(reifyIn(link), false));

        axioms.add(factory.getOWLSubPropertyChainOfAxiom(Arrays.asList(link_Prop, _linkProp), linkProp));

        // Since the named property is a superproperty of the chain, domains and
        // ranges need to be asserted -- they won't be inferred
        if(domain != null) {
          axioms.add(factory.getOWLObjectPropertyDomainAxiom(linkProp,
              factory.getOWLClass(generator.getEntityIRI(domain, true))));
        }
        if(range != null) {
          axioms.add(factory.getOWLObjectPropertyRangeAxiom(linkProp,
              factory.getOWLClass(generator.getEntityIRI(range, true))));
        }
      }
    }

    // Relational attribute assertions
    if(owns.size() == 0 || options.hasOption(Options.OWL2_OPTION)) {
      if(options.hasOption(Options.RELATIONS_OPTION)) {

        if(options.hasOption(Options.OWL2_OPTION)) {
          axioms.add(factory.getOWLIrreflexiveObjectPropertyAxiom(linkProp));
        }

        if(directedLinks.contains(link)) {
          if(options.hasOption(Options.OWL2_OPTION)) {
            axioms.add(factory.getOWLAsymmetricObjectPropertyAxiom(linkProp));
          }
        }
        else if(undirectedLinks.contains(link)) {
          axioms.add(factory.getOWLSymmetricObjectPropertyAxiom(linkProp));
        }
        else {
          // Can't make a relational attribute assertion because there were no
          // links of this breed from which to assess directness
          axioms.add(factory.getOWLAnnotationAssertionAxiom(
              linkProp.getIRI(),
              factory.getOWLAnnotation(factory.getRDFSComment(),
                  factory.getOWLLiteral("No members of this link breed to compute directedness"))));
        }
      }
    }

  }

  /**
   * <!-- reifyOut -->
   * 
   * @param link link name
   * @return Property name to use for reified links from the 'from' node to the
   *         reified link class.
   */
  public static String reifyOut(String link) {
    return link + "_";
  }

  /**
   * <!-- reifyIn -->
   * 
   * @param link link name
   * @return Property name to use for reified links from the reified link class
   *         to the 'to' node.
   */
  public static String reifyIn(String link) {
    return "_" + link;
  }

  /**
   * <!-- addLinkProperty -->
   * 
   * Add axioms to create an OWLObjectProperty
   * 
   * @param axioms Set of axioms to add to
   * @param logicalIRI Logical URI for the ontology
   * @param factory OWLDataFactory to build axioms from
   * @param link Name of the link
   * @param domain Name of the link's domain
   * @param range Name of the link's range
   * @param functional <code>true</code> if the property is functional
   * @param inverseFunctional <code>true</code> if the property is inverse
   *          functional
   * @param generator
   * @throws ExtensionException
   */
  private static void addLinkProperty(Set<OWLAxiom> axioms, IRI logicalIRI, OWLDataFactory factory, String link,
      String domain, String range, boolean functional, boolean inverseFunctional, NetLogoEntityIRIGenerator generator)
      throws ExtensionException {
    OWLObjectProperty property = factory.getOWLObjectProperty(generator.getEntityIRI(link, false));
    axioms.add(factory.getOWLDeclarationAxiom(property));
    if(domain != null) {
      OWLClass domainClass = factory.getOWLClass(generator.getEntityIRI(domain, true));
      axioms.add(factory.getOWLObjectPropertyDomainAxiom(property, domainClass));
    }
    if(range != null) {
      OWLClass rangeClass = factory.getOWLClass(generator.getEntityIRI(range, true));
      axioms.add(factory.getOWLObjectPropertyRangeAxiom(property, rangeClass));
    }
    if(functional) {
      axioms.add(factory.getOWLFunctionalObjectPropertyAxiom(property));
    }
    if(inverseFunctional) {
      axioms.add(factory.getOWLInverseFunctionalObjectPropertyAxiom(property));
    }
  }

  /**
   * <!-- getPatchAxioms -->
   * 
   * Add patch class axioms
   * 
   * @param logicalIRI Logical IRI of the ontology
   * @param factory OWLDataFactory to build axioms
   * @param program NetLogo program
   * @param generator Entity IRI generator
   * @return Set of axioms about patches' variables
   * @throws ExtensionException
   */
  public static Set<OWLAxiom> getPatchAxioms(IRI logicalIRI, OWLDataFactory factory, Program program,
      NetLogoEntityIRIGenerator generator, Options options) throws ExtensionException {
    Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

    if(!options.hasOption(Options.NO_PATCHES_OPTION)) {

      if(program.patchesOwn().size() > State.NETLOGO_PATCH_OWN_ARRAY_START) {
        ArrayList<String> arr =
          new ArrayList<String>(program.patchesOwn().size() - State.NETLOGO_PATCH_OWN_ARRAY_START);

        for(String var: program.patchesOwn()) {
          if(var.equalsIgnoreCase(NETLOGO_PXCOR_VAR) || var.equalsIgnoreCase(NETLOGO_PYCOR_VAR)
            || var.equalsIgnoreCase(NETLOGO_PCOLOR_VAR) || var.equalsIgnoreCase(NETLOGO_PLABEL_VAR)
            || var.equalsIgnoreCase(NETLOGO_PLABEL_COLOR_VAR)) {
            continue;
          }
          arr.add(var);
        }
        addClassAndProperties(axioms, logicalIRI, factory, PATCH_CLASS, arr, generator);
      }

      // Add netlogo axioms for patches and spatial location. We can't give X
      // and Y a domain because they will be used for patches and (visible)
      // turtles, and we don't know in advance which breeds of turtles will
      // always be visible.

      axioms.add(factory.getOWLDataPropertyRangeAxiom(
          factory.getOWLDataProperty(generator.getEntityIRI(X_PROPERTY, false)),
          factory.getOWLDatatype(XSDVocabulary.DOUBLE.getIRI())));

      axioms.add(factory.getOWLDataPropertyRangeAxiom(
          factory.getOWLDataProperty(generator.getEntityIRI(Y_PROPERTY, false)),
          factory.getOWLDatatype(XSDVocabulary.DOUBLE.getIRI())));

      if(generator.hasDomainSpecified(LOCATION_PROPERTY)) {
      	addLinkProperty(axioms, logicalIRI, factory, LOCATION_PROPERTY, generator.getDomain(LOCATION_PROPERTY), PATCH_CLASS, true, false, generator);
      }
      else {
      	addLinkProperty(axioms, logicalIRI, factory, LOCATION_PROPERTY, null, PATCH_CLASS, true, false, generator);
      }
    }

    return axioms;
  }

  /**
   * <!-- getBreedAxioms -->
   * 
   * Create axioms about breeds. If there are no breeds, just use Turtles.
   * 
   * @param logicalIRI Logical IRI of the ontology
   * @param factory OWLDataFactory to create axioms with
   * @param program NetLogo program to look for breeds in
   * @param generator Entity IRI generator
   * @return Set of axioms making assertions about breeds
   * @throws ExtensionException
   */
  public static Set<OWLAxiom> getBreedAxioms(IRI logicalIRI, OWLDataFactory factory, Program program,
      NetLogoEntityIRIGenerator generator) throws ExtensionException {
    Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

    if(program.breeds().size() == 0) {
      addClassAndProperties(axioms, logicalIRI, factory, TURTLE_CLASS, program.turtlesOwn(), generator);
    }
    else {
      for(String breed: program.breeds().keySet()) {
        String breedName = program.breedsSingular().containsKey(breed) ? program.breedsSingular().get(breed) : breed;
        addClassAndProperties(axioms, logicalIRI, factory, breedName, program.breedsOwn().get(breed), generator);
      }
    }
    return axioms;
  }

  /**
   * <!-- addClassAndProperties -->
   * 
   * Add a class and list of properties to the set of axioms
   * 
   * @param axioms Set of axioms to add to
   * @param logicalIRI Logical IRI of the ontology
   * @param factory OWLDataFactory to build axioms with
   * @param name Name of the class to create
   * @param owns Variables associated with the class (to create as data
   *          properties)
   * @param generator Entity IRI generator
   * @throws ExtensionException
   */
  private static void addClassAndProperties(Set<OWLAxiom> axioms, IRI logicalIRI, OWLDataFactory factory, String name,
      Iterable<String> owns, NetLogoEntityIRIGenerator generator) throws ExtensionException {
    OWLClass namedClass = factory.getOWLClass(generator.getEntityIRI(name, true));
    axioms.add(factory.getOWLDeclarationAxiom(namedClass));

    for(String own: owns) {
      OWLDataProperty ownProperty = factory.getOWLDataProperty(generator.getEntityIRI(own, false));
      axioms.add(factory.getOWLDeclarationAxiom(ownProperty));
      axioms.add(factory.getOWLDataPropertyDomainAxiom(ownProperty, namedClass));
    }
  }

  /**
   * <!-- setExtension -->
   * 
   * @param extension
   */
  public void setExtension(OWLExtension extension) {
    this.extension = extension;
  }

}
