# hc07-basin
Slaton Blickman Senior Project Repository

Hello! Thanks for stopping by. This is my senior project for Winter 2017 at Hanover College.

This repository should really be separated into two distinct repositories, the basinWeb web application and basin Android application, but it currently exists in one for the sake of this as a senior project.

## basinWeb

The first part of this project is the basinWeb web application implemented through Slim PHP microframework (https://www.slimframework.com/). I aimed to make the HTTP request routing to reflect RESTful design principles.
Its purpose is to be the intermediary service between the MYSQL database tables needed for basin. This means that change can be made to the database tables or Android application without wreaking too much havoc.

If you are on the Hanover College campus network, you can access basinWeb through http://vault.hanover.edu/~blickmans15/services/basinWeb/index.php

basinWeb consists of these parts:
/vendor/ to hold libraries installed through composer
/v1/ to hold the main slim application ran through index.php and HTTP routing
/include/ to hold helper classes in functions for validating requests and doing database operations
/unitTests/ to hold the classes for testing. (App.php will always have hte same routes as index.php)
and /.idea/ which is a byproduct of committing basinWeb files through Android Studio

To run basinWeb on your server (I'm using WAMP to run locally and cygwin terminal to navigate files), you will need to follow these steps.

1. Have a MySQL database you can access. Import the tables through using the db_basin_import.sql file in /basinWeb/include/
2. Put the entirety of the basinWeb directory in your respective web server directory. 
2. Edit databaseKeys.php in /basinWeb/v1/ to reflect your database configuration.

And that's it. It should be good to go. 

Before running the unit tests for basinWeb, I suggest creating a test database to work with and follow the same steps above. You will have to edit testDatabaseKeys.php in /basinWeb/unitTests/. You may want to manually clear the table data before each round of testing just in case.  It is preferred not to run tests against the released database so as not to cause erroneous inserts, deletes, updates, etc.

To run the unit tests, open your terminal and navigate to /basinWeb/.
Run the following command:
```
./vendor/bin/phpunit ./unitTests/routeTest.php
```

This will exexcute the routeTest class that extends PHPUnit testing framework on App.php (Slim app turned testable php class). Any changes made in the routes in v1/routes will be reflected in App.php

Once I can figure out how to make phpDocumentor agree with Slim, a link to the documentation will be here.


## basin 

basin (lower-case intentional) is a facebook-integrated Android application for creating and attending events around the world. It is currently under review by Facebook to allow users to get more information about each other during application use. In the future, I also hope to enable basin events to be simultaneously shared and created on Facebook as well as opening messaging through user profile pages. Check this README for future updates on this process!

To install basin, you will need an Android device and Android Studio.

To install Android Studio, go here https://developer.android.com/studio/index.html.

Following Part 1 of these instructions should allow you to check out this project from Github. https://www.londonappdeveloper.com/how-to-clone-a-github-project-on-android-studio/

Follow the path app/src/main/java/edu/hanover/basin/ to view the Java code for this project. 

There is too much to explain through this README, so please the basinDocs for further documentation of the application.

You can open the /basinDocs/index.html to view the documentation for this code or go to http://vault.hanover.edu/~blickmans15/services/basinDocs/ if you are on Hanover College's network.



Credit to Evan Miller for the basin logo.





