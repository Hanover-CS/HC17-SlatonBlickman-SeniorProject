<?php
 
 //guide referenced: http://www.slimframework.com/docs/tutorial/first-app.html


//including the required files
require_once '../include/dbOperation.php';
require_once '../include/validateRequest.php';
require '../vendor/autoload.php';

use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;


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

function error(Response $response, $code, $msg, $info){
    $e = ["code"=>$code, "error" => $msg];
    if($info != null){
        $e['information'] = $info;
    }
    $response = $response->withJSON($e, $code);    
    return $response;
    
}

function new_response(Response $response, $code, $body){
    $response = $response->withJSON($body, $code);
    return $response;
}
/*
 * Routes
*/

//GET USER AT ID
//COMPLETE
$app->get('/users/{id}', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $params = $request->getQueryParams();
    $params = addDefaults("/users/id", $params);
    if(validGET("/users/id", $params)){
        try{
            $user_query = new dbOperation($this->db); 
            $results = $user_query->getUser($id, $params["facebook_id"]);
            if($user_query->isSuccessful()){
                //$response->getBody()->write(json_encode($results));
                $response = $response->withJSON($results, 200);
            }
            else{  
                $response = error($response, 404, "No user with that id was found", ["used_facebook_id" => $params["facebook_id"]]);
            }
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage(), null);
        }
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", $acceptedParams);
    }
    return $response;
});

//UPDATE USER AT ID
//TODO
$app->put('/users/{id}', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $body = $request->getParsedBody();
    $params = $request->getQueryParams();
    $params = addDefaults("/users/id", $params);
    if(validPUT("/users/id", $body) and validGET("/users/id", $params)){
        try{
            $user_update = new dbOperation($this->db);
            $results = $user_update->updateUser($id, $body, $params["facebook_id"]); 
            if($user_update->isSuccessful()){
                $response = $response->withJSON(["success" => $results], 200);
            }
            else{
                $response = error($response, 500, "PUT failed", null);
            }
        }
        catch(PDOexception $e){
             $response = error($response, 500, $e->getMessage(), null);
        }
    }
    else{
        $acceptable = ["body_given" => $body, "params_given" => $params];
        $response = error($response, 400, "Invalid content in body or parameters!", $acceptable);
    }
    return $response;
});

//GET ALL USERS
//COMPLETE
$app->get('/users', function (Request $request, Response $response, $args) {
    $params = $request->getQueryParams();
    $params = addDefaults('/users', $params);
    if(validGET("/users", $params)){
        try{
            $users_query = new dbOperation($this->db);
            $results = $users_query->getUsers($params);
            if($users_query->isSuccessful()){
                $response = $response->withJSON($results, 200);
            }
            else{
                $response = error($response, 404, "No users matching the given parameters were found.", []);
            }
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage());
        }
        $this->logger->addInfo("Getting all users");
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", $acceptedParams);
    }
    return $response;
});

//ADD NEW USER
//COMPLETE
$app->post('/users', function (Request $request, Response $response, $args) {
    $body = $request->getParsedBody();
    if(validPOST('/users', $body)){
        try{
            $user_insert = new dbOperation($this->db);
            $results = $user_insert->insertUser($body);
            if($user_insert->isSuccessful()){
                $body = ["success" => $results, "location" => '/users/' . $body['facebook_id'] . '?facebook_id=true'];
                $response = $response->withJSON($body, 201);
            }
            else{
                $response = error($response, "Problem executing POST.", ["success" => $results]);
            }   
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage(), null);
        }
    }
    else{
        $response = error($response, 400, "Invalid body!", ["accepted_body" => "facebook_id, fname, lname" ]);
    }
    $this->logger->addInfo("Getting all users");
    return $response;
});

//get the events the user has attended 
$app->get('/users/{id}/attendence', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $params = $request->getQueryParams();
    $method = $request->getMethod();
    $params = addDefaults("/users/id", $params);
    if(validGET("/users/id", $params)){
        try{
            $user_query = new dbOperation($this->db); 
            $results = $user_query->getUser($id, $params["facebook_id"]);
            if($user_query->isSuccessful()){
                //$response->getBody()->write(json_encode($results));
                $response = new_response($response, 200, $results);
            }
            else{  
                $response = error($response, 404, "No user with that id was found", ["used_facebook_id" => $params["facebook_id"]]);
            }
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage(), null);
        }
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", $acceptedParams);
    }
    return $response;
});

//DEFAULT PAGE
//RETURN LINKS TO OTHER PAGES
$app->get('/', function($request, $response, $args) {
    //$query_c = new dbOperation($this->db);
   $response->getBody()->write( "Default page for http requests");
});     
 

$app->run();