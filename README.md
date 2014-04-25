# About

Doppelengine is an Entity Component System (ECS) framework being built with the [Typesafe Platform](http://typesafe.com/).

# Edit in Eclipse

First cd to the project root directory and run

```bash
$ ./activator eclipse
```

This generates all the files eclipse needs to build the app. Re-run this command every time you add/remove dependencies. I have included the standard eclipse meta-data files in .gitignore.

Your Eclipse version probably does not recognize Scala code. Visit the [scala-ide download page](http://scala-ide.org/download/sdk.html) and download the latest version of the Scala IDE. Open that up and pick a workspace. Right-click in the package explorer and choose `import` > `existing project into workspace` and find our app. You should be able to import it just like a java project.

# Edit in IntelliJ

The Eclipse Scala IDE was giving me problems with refactoring packages, so here's a quick guide to IntelliJ. 

- [Download IntelliJ IDEA](http://www.jetbrains.com/idea/)
- Generate this project's IntelliJ files: `$ ./activator gen-idea`
- Open IntelliJ and open the doppelengine project
- Install the Scala plugin: 
    - Preferences > Plugins > Install JetBrains plugin... 
    - Search for "Scala"
- You may need to tell IntelliJ where to find the latest JDK
    - Go to File > Project Structure > Project
    - Under Project SDKs, specify the JDK you want (eg. Java 1.7)
    - I had to add a new one. Use ```ls -l `which java` ``` to find the location of the JDK you're using
