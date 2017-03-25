<?php
 
 /**
 * This is a web application that implements a RESTFUL design to act as an intermediary between the basin Android application and MYSQL database tables for basin.
 * It uses Slim Microframework for creation and handling of routes.
 * http://www.slimframework.com/
 */

//includes object that handles PDO functionality
require_once '../include/dbOperation.php';

//includes helper functions that will validate requests
require_once '../include/validateRequest.php';

//includes constants to abstract database access
//require_once '../../../protected/databaseKeys.php'; only used for release version
require_once './databaseKeys.php'; //use localhost for testing

//includes required Slim files
require '../vendor/autoload.php';

use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;

/* 
 * Configure application settings
*/

$config['displayErrorDetails'] = true;
$config['addContentLengthHeader'] = false;

/*
* Use constants from protected folder for database information
*/
$config['db']['host']   = DB_HOST;
$config['db']['user']   = DB_USERNAME;
$config['db']['pass']   = DB_PASSWORD;
$config['db']['dbname'] = DB_NAME;

//Initalize new Slim application with given settings
$app = new \Slim\App(["settings" => $config]);

$container = $app->getContainer(); 

//use the container from the application to set a PDO object with MYSQL
$container['db'] = function ($c) {
    $db = $c['settings']['db'];
    $pdo = new PDO("mysql:host=" . $db['host'] . ";dbname=" . $db['dbname'], 
        $db['user'], $db['pass']);

    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);

    return $pdo;
};

//Can be used if logging is desired
//logging gives errors on vault, so it's commented out
// $container['logger'] = function($c) {
//     $logger = new \Monolog\Logger('my_logger');
//     $file_handler = new \Monolog\Handler\StreamHandler("../logs/app.log");
//     $logger->pushHandler($file_handler);
//     return $logger;
// };

/*
 * ROUTES
*/

/**
* GET: returns the default page. 
* TODO: Return list of links to all available routes 
*/
$app->get('[/]', function($request, $response, $args) {
    //$query_c = new dbOperation($this->db);
   $response->getBody()->write( "Default page for http requests");
}); 

require_once './routes/usersRouting.php';
 
require_once './routes/eventsRouting.php';

require_once './routes/attendeesRouting.php';

/*
* START APPLICATION
*/
$app->run();
