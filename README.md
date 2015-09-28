# play-gulp-standalone

A seed project which shows how to create an SPA Play Framework application using Gulp task runner for frontend asset compilation without depending on custom sbt plugins. This is a standalone version of my [SBT Play Gulp plugin](http://www.github.com/mmizutani/sbt-play-gulp) and allows for full control over how to integrate SBT and Gulp.

You can use this Play app template to create a Play Framework & Yeoman Gulp Angular hybrid project.

Unlike the [demo project](https://github.com/mmizutani/play-gulp-demo) for the SBT Play Gulp plugin, you do not need to depend on my custom sbt plugin.


## Online Demo

https://play-gulp-standalone.herokuapp.com


## Deploy your own to Heroku

Thanks to the powerful [Heroku multi build pack](https://github.com/ddollar/heroku-buildpack-multi), you can deploy this non-standard hybrid application to Heroku in one go.

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

Since this is a complex project, the first deployment to Heroku might take 5 to 10 minutes. Enjoy a cup or two of coffee while the Heroku server does heavy lifting for you.


## How this works

This template Play project is primarily based on the two key files: `app/controllers/GulpAssets.scala` and `project/PlayGulp.scala`.

`GulpAssets.scala` is a controller that serves JavaScript (compiled from coffee script, TypeScript etc.), CSS (compiled from SCSS) and other static assets such as images and icons from the Gulp AngularJS HTML project under `ui` directory. `PlayGulp.scala` is a Play project file which weaves Gulp tasks into sbt project compilation processes.

The majority of the code which was used in the sbt-play-gulp plugin moved from a sbt project to `play-gulp-standalone/project/PlayGulp.scala` directory of the Play project.


## How to run locally

Install npm (and optionally the LiveReload browser plugin).

Install gulp and bower packages:

```
$ npm install -g gulp bower
```

Clone and initialize the repo:

```sh
$ git clone git@github.com:mmizutani/play-gulp-standalone.git
$ cd play-gulp-standalone
$ ./postinstall.sh
```

Check that Gulp runs successfully by running either

```sh
$ activator
> gulp
Using gulpfile ~/play-gulp-standalone/ui/gulpfile.js
Starting 'clean'...
Finished 'clean' after 17 ms
Starting 'default'...
Starting 'scripts'...
Starting 'styles'...
Starting 'partials'...
Starting 'fonts'...
Starting 'other'...
Finished 'default' after 397 ms
gulp-inject 2 files into index.scss.
all files 7.88 kB
Finished 'scripts' after 641 ms
Finished 'partials' after 325 ms
Finished 'fonts' after 292 ms
Finished 'other' after 254 ms
Finished 'styles' after 2.04 s
Starting 'inject'...
gulp-inject 1 files into index.html.
gulp-inject 10 files into index.html.
Finished 'inject' after 70 ms
Starting 'html'...
gulp-inject 1 files into index.html.
'dist/' styles/app-e5505249f9.css 120.34 kB
'dist/' styles/vendor-1dddaadd0b.css 58.62 kB
'dist/' scripts/app-b3239829ef.js 6.26 kB
'dist/' scripts/vendor-3123871ad2.js 442.91 kB
'dist/' index.html 636 B
'dist/' all files 628.78 kB
Finished 'html' after 13 s
Starting 'build'...
Finished 'build' after 6.71 μs
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


## How to use a different Yeoman Gulp template

Install Yeoman and Yeoman gulp-angular template generator in addition to gulp and bower:

```
$ npm install -g yo generator-gulp-angular gulp bower
```

Scaffold your frontend AngularJS HTML project in the subdirectory `play-gulp-standalone/ui`:

```
$ git clone git@github.com:mmizutani/play-gulp-standalone.git
$ cd play-gulp-standalone
$ rm -rf ui
$ mkdir ui
$ cd ui
$ yo gulp-angular
```

and choose any options which you prefer:
```
? Which version of Angular do you want? 1.4.0 (stable)
? Which Angular's modules would you want to have? (ngRoute and ngResource will be addressed after) angular-animate.js (enable animation features), angular-cookies.js (handle cookie management), angular-touch.js (for mobile development), angular-sanitize.js (to securely parse and manipulate HTML)
? Would you need jQuery or perhaps Zepto? jQuery 2.x (new version, lighter, IE9+)
? Would you like to use a REST resource library? ngResource, the official support for RESTful services
? Would you like to use a router ? UI Router, flexible routing with nested views
? Which UI framework do you want? Bootstrap, the most popular HTML, CSS, and JS framework
? How do you want to implements your Bootstrap components? Angular UI Bootstrap, Bootstrap components written in pure AngularJS by the AngularUI Team
? Which CSS preprocessor do you want? Sass (Node), Node.js binding to libsass, the C version of the popular stylesheet preprocessor, Sass.
? Which JS preprocessor do you want? None, I like to code in standard JavaScript.
? Which html template engine would you want? None, I like to code in standard HTML.
```

When running `npm install gulp-protractor`, you might encounter an error related to selenium-webdriver, node-gyp and bufferutil for [unmet optional dependency](https://code.google.com/p/selenium/issues/detail?id=8566). As a workaround, this repo contains the gulp-protractor folder so that Gulp works regardless of this bug.

For the error that the `libsass` binding was not found, run

```
$ cd ui
$ npm rebuild node-sass
```


To adjust the project directory structure, move the bower_components directory and change paths referring to it accordingly:

```
$ mv bower_components src/
```

```diff
ui/gulp/conf.js
@@ -25,7 +25,7 @@
 exports.wiredep = {
   exclude: [/bootstrap.js$/, /bootstrap-sass-official\/.*\.js/, /bootstrap\.css/],
-  directory: 'bower_components'
+  directory: 'src/bower_components'
 };
```

```diff
ui/.bowerrc
@@ -1,3 +1,3 @@
 {
-  "directory": "bower_components"
+  "directory": "src/bower_components"
 }
```

Then check whether running Gulp's default task finishes successfully:

```
$ cd ui
$ gulp
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
