# Setting up

This is a Scala app written on top of the [Play! Framework](http://www.playframework.com/). In order to run it you will need to install [scala](http://www.scala-lang.org/)

```bash
$ brew install scala
```

Install [sbt](http://www.scala-sbt.org/)

```bash
$ brew install sbt
```

Download and install the [Play! Framework](http://www.playframework.com/download). After downloading, unpackage the .zip and add the new folder to your PATH. For example:

```bash
export PATH=/Users/biff/play-2.1.1/:$PATH
```

Clone the repo and cd to the project's root directory

```bash
$ git clone git@github.com:Doppelgamer/doppelgamer.git
$ cd doppelgamer
```

and run the app.

```bash
$ play run
```

# Running Tests

We are using the [specs2](http://etorreborre.github.io/specs2/) testing framework for Scala. Play Framework integrates nicely with this. To run all tests:

```bash
$ play test
```

To run a single test:

```bash
$ play "test-only fully.qualified.className"
```

# Edit in Eclipse

First cd to the project root directory and run

```bash
$ play eclipse
```

This generates all the files eclipse needs to build the app. Re-run this command every time you add/remove dependencies. I have included the standard eclipse meta-data files in .gitignore.

Your Eclipse version probably does not recognize Scala code. Visit the [scala-ide download page](http://scala-ide.org/download/sdk.html) and download the latest version of the Scala IDE. Open that up and pick a workspace. Right-click in the package explorer and choose `import` > `existing project into workspace` and find our app. You should be able to import it just like a java project.

Lastly, there is an optional Play! plugin for Eclipse that makes it a pleasure to edit the routes file and the html templates. Go to `help` > `install new software`. Open up the drop-down menu at the top and select Scala IDE. Select `Scala IDE Plugins` > `Play2 support in Scala IDE` and click next. Continue through the installation as normal. After restarting Eclipse, you should have nice syntax highlighting in your routes and template files.

# [CoffeeScript?](http://coffeescript.org/)

I really like CoffeeScript and I'm wondering how practical it could be in writing the client-side engine. It's much more comprehensive and expressive than raw JS, but harder to debug since the browser reads it as JS. Also, it uses white space to delimit blocks, which can be a pain for tired eyes.

# Visit the Wiki

[Visit the wiki](https://github.com/biffbyrd/scalatree/wiki) for detail on the game universe, mechanics, and system.
