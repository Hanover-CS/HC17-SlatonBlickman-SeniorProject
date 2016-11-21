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

/*
 * Routes
*/

//GET USER AT ID
//COMPLETE
$app->get('/users/{id}[/]', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $params = $request->getQueryParams();
    $params = addDefaults("/users/id", $params);
    $method = $request->getMethod();
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
        $acceptedParams = ["given_params" => $params, "accepted_params" => getValidParams("/users/id")];
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
            $get_user = new dbOperation($this->db);
            $get_user->getUser($id, $params['facebook_id']);
            if($get_user->isSuccessful()){
                $user_update = $get_user;
                $results = $user_update->updateUser($id, $body, $params["facebook_id"]); 
                if($user_update->isSuccessful()){
                    $response = $response->withJSON(["success" => true], 200);
                }
                else{
                    $response = error($response, 500, "PUT failed", null);
                }
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
        $acceptable = ["body_given" => $body, "params_given" =>  $params];
        $response = error($response, 400, "Invalid content in body or parameters!", $acceptable);
    }
    return $response;
});

//DELETE USER AT ID
//TODO
$app->delete('/users/{id}', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $body = $request->getParsedBody();
    $params = $request->getQueryParams();
    $params = addDefaults("/users/id", $params);
    if(validDELETE("/users/id", $body) and validGET("/users/id", $params)){
        try{    
            $get_user = new dbOperation($this->db);
            $get_user->getUser($id, $params["facebook_id"]);
            if($get_user->isSuccessful()){
                $user_delete = $get_user;
                $results = $user_delete->deleteUser($id, $params["facebook_id"]); 
                if($user_delete->isSuccessful()){
                    $response = $response->withJSON(["success" => true], 200);
                }
                else{
                    $response = error($response, 500, "DELETE failed", null);
                }
            }
            else{
                $response = error($response, 404, "No user with that id was found.",  ["used_facebook_id" => $params["facebook_id"]]);
            }
        }
        catch(PDOexception $e){
             $response = error($response, 500, $e->getMessage(), null);
        }
    }
    else{
        //getvalidbody
        $acceptable = ["params_given" => $params];
        $response = error($response, 400, "Invalid content in   parameters!", $acceptable);
    }
    return $response;
});

//GET ALL USERS
//COMPLETE
$app->get('/users[/]', function (Request $request, Response $response, $args) {
    $params = $request->getQueryParams();
    $params = addDefaults('/users', $params);
    if(validGET("/users", $params)){
        try{
            $users_query = new dbOperation($this->db);
            $results = $users_query->getUsers($params);
            if($users_query->isSuccessful()){
                $response = $response->withJSON(["users"=>$results], 200);
            }
            else{
                $response = $response->withJSON(["users"=>[]], 200);
            }
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage());
        }
        $this->logger->addInfo("Getting all users");
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", ["accepted_params" => getValidParams("/users")]);
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
            $user_uri = $request->getUri() . '/' . $body['facebook_id'] . '?facebook_id=true';
            if($user_insert->isSuccessful()){
                $body = ["success" => true, "location" => $user_uri];
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



//get the events the user has attended and/or created
$app->get('/users/{id}/events[/]', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $params = $request->getQueryParams();
    $method = $request->getMethod();
    $params = addDefaults("/users/id/events", $params);
    if(validGET("/users/id/events", $params)){
        try{
            $user_query = new dbOperation($this->db); 
            $results = $user_query->getUserEvents($id, $params);
            $response = $response->withJSON(["events" => $results], 200);

        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage(), null);
        }
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", ["accepted_params" => getValidParams("/users/id/events")]);
    }
    return $response;
});
 
 
//What to do to check whether or not a user is attending a specific event

// .../events
$app->get('/events[/]', function($request, $response, $args) {
    $params = $request->getQueryParams();
    $params = addDefaults('/events', $params);
    if(validGET("/events", $params)){
        try{
            $events_query = new dbOperation($this->db);
            $results = $events_query->getEvents($params);
            if($events_query->isSuccessful()){
                $response = $response->withJSON(["events"=>$results], 200);
            }
            else{
                $response = $response->withJSON(["events"=>[]], 200);
            }
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage());
        }
        $this->logger->addInfo("Getting all users");
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", ["accepted_params" => getValidParams("/users")]);
    }
    return $response;
});   

// .../events/{event_id}
$app->get('/events/{id}[/]', function($request, $response, $args) {
    //$query_c = new dbOperation($this->db);
   $response->getBody()->write( "Default page for specific http event requests");
});   

// .../events/{event_id}/attendees

$app->get('/events/{id}/attendees[/]', function($request, $response, $args) {
    //$query_c = new dbOperation($this->db);
   $response->getBody()->write( "Default page for specific http event attendees requests");
});   

// .../events/{event_id}/attendees/{user_id}

//DEFAULT PAGE
//RETURN LINKS TO OTHER PAGES
$app->get('/', function($request, $response, $args) {
    //$query_c = new dbOperation($this->db);
   $response->getBody()->write( "Default page for http requests");
});    

$app->run();