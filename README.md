# Running the App

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

Now you should be able to clone the repo and cd to the project's root directory

```bash
$ git clone git@bitbucket.org:biffbyrd/scalatree.git
$ cd scalatree
```

and run the app.

```bash
$ play run
```

# Edit in Eclipse

First cd to the project root directory and run

```bash
$ play eclipse
```

to turn our app into an eclipse project. All of the eclipse-related files should be excluded from git in .gitignore. Then visit the [scala-ide download page](http://scala-ide.org/download/sdk.html) and download the 2.10.x version of the Scala IDE. Open that up and pick a workspace. Right-click in the package explorer and choose `import` > `existing project into workspace` and find our app. You should be able to import it just like a java project.

Lastly, there is an optional Play! plugin for Eclipse that makes it a pleasure to edit the routes file and the html templates. Go to `help` > `install new software`. Open up the drop-down menu at the top and select Scala IDE. Select `Scala IDE Plugins` > `Play2 support in Scala IDE` and click next. Continue through the installation as normal. After restarting Eclipse, you should have nice syntax highlighting in your routes and template files.
