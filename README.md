# hc07-basin
Slaton Blickman Senior Project Repository

Hello! Thanks for stopping by. This is my senior project for Winter 2017 at Hanover College.

This repository should really be separated into two distinct repositories, the basinWeb web application and basin Android application, but it currently exists in one for the sake of this as a senior project.

## basinWeb

The first part of this project is the basinWeb web application implemented through Slim PHP microframework. I aimed to make the HTTP request routing to reflect RESTful design principles.
Its purpose is to be the intermediary service between the MYSQL database tables needed for basin. This means that change can be made to the database tables or Android application without wreaking too much havoc.

If you are on the Hanover College campus, you can access basinWeb through http://vault.hanover.edu/~blickmans15/services/basinWeb/index.php

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

Before running the unit tests for basinWeb, I suggest creating a test database to work with and follow the same steps above. You will have to edit testDatabaseKeys.php in /basinWeb/unitTests/.

To run the unit tests, open your terminal and navigate to /basinWeb/.
Run the following command: ./vendor/bin/phpunit ./unitTests/routeTest.php

This will exexcute the routeTest class that extends PHPUnit testing framework on App.php (Slim app turned testable php class). Any changes made in the routes in v1/routes will be reflected in App.php









