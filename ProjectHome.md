## Introduction ##

This project looks for a set of routines commonly used to do neuronal data analysis.
Initially we will explore the neuronal coding for sensory objects on **primary sensory**
cortices and **hippocampus**.

We defined a set of reusable Java components that can be incorporated in another
application. Each component is fully tested and has a separate documentation page in our
Wiki. As of now, they are:

  * [SpikeHandler](SpikeHandlerComponent.md)
    * And the [spike data file format](SpikeDataFileFormat.md)
  * [SpikeRateMatrix and CountMatrix](CountMatrixComponent.md)
  * [BehaviorHandler](BehaviorHandlerComponent.md)
    * And the [behavior data file format](BehaviorDataFileFormat.md)
  * [PatternHandler](PatternHandlerComponent.md)
    * And the [pattern data file format](PatternDataFileFormat.md)

The elements outlined above form the core of our project. We have also built components for the specific user case of applying supervised learning techniques to identify behavior in neuron activity (the `nda.analysis` package). The later components have yet to be documented.

If you are new to this project and want to know what problems our project can help you with, we have set up a [FAQ wiki page](FAQ.md).


## Code Organization ##

The project lives in the SVN repository as an Eclipse IDE project in the root tree. The
directory layout is as follows:

  * src/: Main source code folder
  * test/: Source code folder for all test code (unit and integration tests)
  * setup/: Real and made up data files used to test the code
  * wiki/: Source code for our wiki pages
  * lib/: Third party libraries used in this project
  * doc/: Automatically generated documentation (Doxygen)
  * legacy/: Old legacy code that we are currently improving by refactoring and pulling into src/


### Dependencies ###

  * JUnit: our testing framework
  * Apache Commons Math: general math and statistics library
  * Weka: machine learning algorithms


### Using as a library ###

You have two options:

  1. Use one of our generated JAR files in the _Downloads_ tab in this page and import it in your project
  1. Download the code from our SVN repository (see the _Source_ tab) and use the newest version available


### Contributing ###

There are several ways you can contribute to the project. As an user of this library,
you can report bugs and issues at our _Issue Tracker_, or maybe just show us some idea
you have about a cool feature we could implement.

You could also become an active contributer to the project. For one time changes, the
easiest way is to contribute a patch. You can download our code using SVN (follow the instructions depicted on the _Source_ tab in this page) and then make some changes, generate a patch, and send it to us through the Issue Tracker. If you want to contribute
more actively, we encourage you to become an official comitter of the project, having
direct access to our source code repository.



## Documentation ##

The main documentation of the project consists of the Wiki pages in this site. Read then
carefully and choose which components you will use in your application. After that,
you can download our code and access the Javadoc documentation directly in your IDE or
you can use the Doxygen tool to automatically generate HTML pages with documentation
extracted from our source code.


## Contact ##

This project is in active development and is currently being used for research at the
[Brain Institute](http://www.neuro.ufrn.br/incerebro/), UFRN - Brazil. We are eager to
hear about your interest in this project. How are you using it, your impressions,
suggestions and complaints. Get in touch with us through our issue tracker or our project email: **neurobjects@neuro.ufrn.br**.

We look forward to hearing from you and maybe welcome you as a contributor to the project.