# play-gulp-standalone

[![Build Status](https://travis-ci.org/mmizutani/play-gulp-standalone.svg?branch=jspm)](https://travis-ci.org/mmizutani/play-gulp-standalone/tree/jspm)

This jspm branch is an Aurelia-jspm version of [play-gulp-standalone](https://github.com/mmizutani/play-gulp-standalone) demo app based on the [Yeoman Aurelia generator template](https://github.com/zewa666/generator-aurelia). You can execute [jspm](http://jspm.io/) commands in the sbt console session.

This seed project shows how to create an SPA Play Framework application using Gulp task runner for frontend asset compilation without depending on custom sbt plugins. This is a standalone version of my [SBT Play Gulp plugin](http://www.github.com/mmizutani/sbt-play-gulp) and allows for full control over how to integrate SBT and Gulp.

You can use this Play app template to create a Play Framework & Yeoman Gulp Angular hybrid project.

Unlike the [demo project for the SBT Play Gulp plugin](https://github.com/mmizutani/play-gulp-demo), you do not need to depend on my custom sbt plugin. You just need to adjust the following two files to match the directory structure of your frontend project in the `ui` directory:

* app/controllers/GulpAssets.scala
* project/PlayGulp.scala


## Online Demo

https://play-gulp-aurelia-jspm.herokuapp.com/


## Deploy your own to Heroku

Thanks to the powerful [Heroku multi build pack](https://github.com/ddollar/heroku-buildpack-multi), you can deploy this non-standard hybrid application to Heroku in one go.

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

Since this is a complex project, the first deployment to Heroku might take 5 to 10 minutes. Enjoy a cup or two of coffee while the Heroku server does heavy lifting for you.

If deployment to Heroku halted with an error like "warn Timed out on lookup for github:systemjs/plugin-text,", you should add a private GitHub access token to Heroku config variables to increase the timeout limits of package download for jspm:

```sh
$ heroku config:set JSPM_GITHUB_AUTH_TOKEN=your_github_username:your_github_access_token_generated_with_publicrepo_scope
```


## How this works

This template Play project is primarily based on the two key files: `app/controllers/GulpAssets.scala` and `project/PlayGulp.scala`.

`GulpAssets.scala` is a controller that serves JavaScript (compiled from coffee script, TypeScript etc.), CSS (compiled from SCSS) and other static assets such as images and icons from the Gulp AngularJS HTML project under `ui` directory. `PlayGulp.scala` is a Play project file which weaves Gulp tasks into sbt project compilation processes.

The majority of the code which was used in the sbt-play-gulp plugin moved from a sbt project to `play-gulp-standalone/project/PlayGulp.scala` directory of the Play project.


## How to run locally

Install npm (and optionally the LiveReload browser plugin).

Install gulp and bower packages:

```
$ npm install -g gulp bower jspm
```

Clone and initialize the repo:

```sh
$ git clone git@github.com:mmizutani/play-gulp-standalone.git
$ cd play-gulp-standalone
$ git checkout jspm
$ npm install # postinstall executes npm install and jspm install in ui directory
```

Check that Gulp runs successfully by running either

```sh
$ activator
> gulp
```

or

```sh
$ cd ui
$ gulp
```

Now we are ready to run the app at [http://localhost:9000](http://localhost:9000).

```sh
$ cd play-gulp-standalone
$ activator
> compile
> run
```

You can see the directory paths from which each static asset is served to the web browser:
```
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/.tmp/serve/index.html
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/bower_components/angular/angular.js
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/bower_components/angular-animate/angular-animate.js
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/.tmp/serve/app/index.css
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/bower_components/animate.css/animate.css
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/bower_components/toastr/toastr.css
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/bower_components/jquery/dist/jquery.js
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/bower_components/angular-cookies/angular-cookies.js
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/bower_components/angular-touch/angular-touch.js
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/bower_components/angular-sanitize/angular-sanitize.js
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/bower_components/angular-resource/angular-resource.js
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/bower_components/angular-ui-router/release/angular-ui-router.js
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/bower_components/angular-bootstrap/ui-bootstrap-tpls.js
[info] - controllers.GulpAssets - Serving /home/dev/play-gulp-standalone/ui/src/app/index.module.js
...
```

You can turn this verbose logging off by editing `conf/logback.xml`.

sbt's compile and run commands trigger the `gulp watch` task via the run hook added by `project/PlayGulp.scala`:

```scala
  playRunHooks <+= (gulpDirectory, gulpFile).map {
    (base, fileName) => GulpWatch(base, fileName)
  }
```

(sbt commands run in the project root folder `play-gulp-standalone/`, but gulp tasks inside the sbt console run in the subdirectory 'play-gulp-standalone/ui/')

You can do a local production test normally.

```
> stage
> testProd
```

sbt's dist command triggers the `gulp build` task and packages the static assets built by Gulp into the classpath of the distribution package.

```
> dist
[info] Your package is ready in /home/dev/play-gulp-standalone/target/universal/play-gulp-standalone-1.0.zip
```

You can then unzip `target/universal/play-gulp-standalone-1.0.zip` and again unzip ``/lib/play-gulp-standalone.play-gulp-standalone-1.0-assets.jar` to see how the built static assets are packaged for production.

```
    play-gulp-standalone-1.0.zip (unzipped)
     +- bin/
     +- conf/
     +- lib/
        +- xxx.jar
        +- yyy.jar
        +- play-gulp-standalone.play-gulp-standalone-1.0-assets.jar (unzipped)
           +- META-INF/
           +- public/
              +- assets/
              +- bower_components/
              +- scripts/
              +- styles/
              +- favicon.co
              +- index.html
```

## Note for Heroku deployment

Upon the initial push to Heroku, the server executes build processes in the following order.

1. Install the [Ruby build pack](https://github.com/heroku/heroku-buildpack-ruby) and bundle-install gems (`Gemfile`) for compiling SCSS.
1. Install the [Node.js build pack](https://github.com/heroku/heroku-buildpack-nodejs) (node and npm) and, as per the postinstall script in `package.json`, download node packages (`ui/package.json`) including Gulp and bower packages (`ui/bower.json`) such as AngularJS and Bootstrap.
1. Install the [Scala build pack](https://github.com/heroku/heroku-buildpack-scala), install Open-JDK, Scala and sbt and download Java/Scala libraries.
4. Build and stage the Play application.
5. Run the application with the command in `Procfile`.


If you want to deploy the app to Heroku from local development machine, it is required to install the multi-build pack before pushing:

```sh
$ heroku buildpacks:set https://github.com/ddollar/heroku-buildpack-multi.git
$ git push heroku master
```

For troubleshooting, you can ssh into an ephemeral (read-only) copy of your deployed Heroku app files (a [one-off dyno](https://devcenter.heroku.com/articles/one-off-dynos)):

```sh
$ heroku run bash
~ $ ls
  activator  activator.bat  activator-launch-1.3.6.jar  app  app.json  bin  build.sbt  conf  Gemfile  Gemfile.lock  LICENSE  package.json  Procfile  project  README.md  test  tmp  ui  vendor
~ $ exit
```

and tail the logs:

```sh
$ heroku logs -t
```

After the initial push to Heroku, build with the subsequent deploy will take shorter time thanks to caching by Heroku build packs, but it still takes long time. You should consider using the [heroku sbt plugin](https://devcenter.heroku.com/articles/deploying-scala-and-play-applications-with-the-heroku-sbt-plugin) for faster deployment. See the [Play documentation](https://www.playframework.com/documentation/2.4.x/ProductionHeroku#Deploying-with-the-sbt-heroku-plugin) for more details.
