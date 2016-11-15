<?php
 
 //guide referenced: http://www.slimframework.com/docs/tutorial/first-app.html


//including the required files
require_once '../include/dbOperation.php';
require_once '../include/validateRequest.php';
require '../vendor/autoload.php';

use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;
//

/* 
 * Configure application settings
*/

$config['displayErrorDetails'] = true;
$config['addContentLengthHeader'] = false;

$config['db']['host']   = "localhost";
$config['db']['user']   = "root";
$config['db']['pass']   = "";
$config['db']['dbname'] = "db_basin";

$app = new \Slim\App(["settings" => $config]);

$container = $app->getContainer();  

$container['logger'] = function($c) {
    $logger = new \Monolog\Logger('my_logger');
    $file_handler = new \Monolog\Handler\StreamHandler("../logs/app.log");
    $logger->pushHandler($file_handler);
    return $logger;
};

$container['db'] = function ($c) {
    $db = $c['settings']['db'];
    $pdo = new PDO("mysql:host=" . $db['host'] . ";dbname=" . $db['dbname'],
        $db['user'], $db['pass']);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
    return $pdo;
};

/*
 * Helper functions
*/

// function validateParams($app, $route, $method, $params){
//     $db = new dbOperation($app->db);
//     //use validate request file instead
//     if($route == "users"){
//        $cols = $db->getUserFields(); 
//     }
//     else{
//         $cols = $db->getEventFields();
//     }
//     $validParams = ["sort", "direction", "filters"];
//     return true;
// };


/*
 * Routes
*/

$app->get('/users/{id}', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $params = $request->getQueryParams();
    $method = $request->getMethod();
    if(validGET("users/id", $params)){
        try{
            $user_query = (new dbOperation($this->db))->getUser($id); 
            $response->getBody()->write(json_encode($user_query));
        }
        catch(PDOexception $e){
            $response->getBody()->write($e->getMessage());
        }
    }
    else{
        $response->getBody()->write("Invalid");
    }
    return $response;
});

$app->post('/users/{id}', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $body = $request->getParsedBody();
    $params = $request->getQueryParams();
    $method = $request->getMethod();
    if(validateParams($this, "users/id", $method, $params)){
        try{
            $user_update = (new dbOperation($this->db))->updateUser($id, $body, $params); 
            $response->getBody()->write(json_encode($user_update));
        }
        catch(PDOexception $e){
            $response->getBody()->write($e->getMessage());
        }
    }
    return $response;
});


$app->get('/users', function (Request $request, Response $response, $args) {
    $name = $request->getAttribute('facebook_id');
    try{
        $users_query = (new dbOperation($this->db))->getUsers($args);
         $response->getBody()->write(json_encode($users_query));
    }
    catch(PDOexception $e){
        $response->getBody()->write($e->getMessage());
    }
    $this->logger->addInfo("Getting all users");
    return $response;
});



$app->get('/', function($request, $response, $args) {
    //$query_c = new dbOperation($this->db);
   $response->getBody()->write( "Default page for http requests");
});     
 
// \Slim\Slim::registerAutoloader();
 
// //Creating a slim instance
// $app = new \Slim\Slim();
 

//  $app->get('/users', 'authenticateStudent', function() use ($app){
//     //$db = new DbOperation();
//     // $result = $db->getAssignments($student_id);
//     // $response = array();
//     // $response['error'] = false;
//     // $response['assignments'] = array();
//     // while($row = $result->fetch_assoc()){
//     //     $temp = array();
//     //     $temp['id']=$row['id'];
//     //     $temp['name'] = $row['name'];
//     //     $temp['details'] = $row['details'];
//     //     $temp['completed'] = $row['completed'];
//     //     $temp['faculty']= $db->getFacultyName($row['faculties_id']);
//     //     array_push($response['assignments'],$temp);
//     // }
//     echoResponse(200,$response);
// });

//Method to display response
// function echoResponse($status_code, $response)
// {
//     //Getting app instance
//     $app = \Slim\Slim::getInstance();
 
//     //Setting Http response code
//     $app->status($status_code);
 
//     //setting response content type to json
//     $app->contentType('application/json');
 
//     //displaying the response in json format
//     echo json_encode($response);
// }
 
 
// function verifyRequiredParams($required_fields)
// {
//     //Assuming there is no error
//     $error = false;
 
//     //Error fields are blank
//     $error_fields = "";
 
//     //Getting the request parameters
//     $request_params = $_REQUEST;
 
//     //Handling PUT request params
//     if ($_SERVER['REQUEST_METHOD'] == 'PUT') {
//         //Getting the app instance
//         $app = \Slim\Slim::getInstance();
 
//         //Getting put parameters in request params variable
//         parse_str($app->request()->getBody(), $request_params);
//     }
 
//     //Looping through all the parameters
//     foreach ($required_fields as $field) {
 
//         //if any requred parameter is missing
//         if (!isset($request_params[$field]) || strlen(trim($request_params[$field])) <= 0) {
//             //error is true
//             $error = true;
 
//             //Concatnating the missing parameters in error fields
//             $error_fields .= $field . ', ';
//         }
//     }
 
//     //if there is a parameter missing then error is true
//     if ($error) {
//         //Creating response array
//         $response = array();
 
//         //Getting app instance
//         $app = \Slim\Slim::getInstance();
 
//         //Adding values to response array
//         $response["error"] = true;
//         $response["message"] = 'Required field(s) ' . substr($error_fields, 0, -2) . ' is missing or empty';
 
//         //Displaying response with error code 400
//         echoResponse(400, $response);
 
//         //Stopping the app
//         $app->stop();
//     }
// }
 
// //Method to authenticate a student 
// function authenticateStudent(\Slim\Route $route)
// {
//     //Getting request headers
//     $headers = apache_request_headers();
//     $response = array();
//     $app = \Slim\Slim::getInstance();
 
//     //Verifying the headers
//     if (isset($headers['Authorization'])) {
 
//         //Creating a DatabaseOperation boject
//         $db = new DbOperation();
 
//         //Getting api key from header
//         $api_key = $headers['Authorization'];
 
//         //Validating apikey from database
//         if (!$db->isValidStudent($api_key)) {
//             $response["error"] = true;
//             $response["message"] = "Access Denied. Invalid Api key";
//             echoResponse(401, $response);
//             $app->stop();
//         }
//     } else {
//         // api key is missing in header
//         $response["error"] = true;
//         $response["message"] = "Api key is misssing";
//         echoResponse(400, $response);
//         $app->stop();
//     }
// }


$app->run();