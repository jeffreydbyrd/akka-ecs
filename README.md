# About

Doppelengine is a multiplayer game server being built with the [Typesafe Platform](http://typesafe.com/).

# How to Run

Clone the repo and cd to the project's root directory

```bash
$ git clone git@github.com:Doppelgamer/doppelengine.git
$ cd doppelengine
```

and run the app.

```bash
$ ./activator run
```

# Running Tests

We're using [Scalatest](http://www.scalatest.org/)

```bash
$ ./activator test
```

To run a single test:

```bash
$ ./activator "test-only fully.qualified.className"
```

# Activator UI

You can the above (and more) with the activator UI.

```bash
$ ./activator ui
```

It should open in a browser window. From here you can code, build, test, run, or inspect various processes. Visit the [Activator docs](https://typesafe.com/activator/docs) to see what else you can do.

# Edit in Eclipse

First cd to the project root directory and run

```bash
$ ./activator eclipse
```

This generates all the files eclipse needs to build the app. Re-run this command every time you add/remove dependencies. I have included the standard eclipse meta-data files in .gitignore.

Your Eclipse version probably does not recognize Scala code. Visit the [scala-ide download page](http://scala-ide.org/download/sdk.html) and download the latest version of the Scala IDE. Open that up and pick a workspace. Right-click in the package explorer and choose `import` > `existing project into workspace` and find our app. You should be able to import it just like a java project.

Lastly, there is an optional Play! plugin for Eclipse that makes it a pleasure to edit the routes file and the html templates. Go to `help` > `install new software`. Open up the drop-down menu at the top and select Scala IDE. Select `Scala IDE Plugins` > `Play2 support in Scala IDE` and click next. Continue through the installation as normal. After restarting Eclipse, you should have nice syntax highlighting in your routes and template files.
