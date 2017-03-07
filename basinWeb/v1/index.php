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
    $e = ["code "=> $code, "error" => $msg];
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
        $acceptable = ["params_given" => $params];
        $response = error($response, 400, "Invalid content in parameters!", $acceptable);
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
            $response = error($response, 500, $e->getMessage(), []);
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
$app->post('/users[/]', function (Request $request, Response $response, $args) {
    $body = $request->getParsedBody();
    if(validPOST('/users', $body)){
        try{
            $user_insert = new dbOperation($this->db);
            $results = $user_insert->insertUser($body);
            $user_uri = $request->getUri() . $body['facebook_id'] . '?facebook_id=true';
            if($user_insert->isSuccessful()){
                $body = ["success" => true, "location" => $user_uri];
                $response = $response->withJSON($body, 201);
            }
            else{
                $response = error($response, 500, "Problem executing POST.", ["success" => $results]);
            }   
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage(), []);
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
            $user_query->getUser($id, $params['facebook_id']);
            if($user_query->isSuccessful()){
                $results = $user_query->getUserEvents($id, $params);
                $response = $response->withJSON(["events" => $results], 200);
            }
            else{
                $response = error($response, 404, "No user with that id was found.", 
                                   ["used_facebook_id" => $params["facebook_id"]]);
            }

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
 

// .../events
//Add a new event
$app->post('/events[/]', function($request, $response, $args) {
    $body = $request->getParsedBody();
    if(validPOST("/events", $body)){
        try{
            $events_query = new dbOperation($this->db);
            $results = $events_query->insertEvent($body);
            if($events_query->isSuccessful()){
                $results = ["success" => $results];
                $response = $response->withJSON($results, 201);
            }
            else{
                $response = error($response, 500, "Unknown problem when executing POST", null);
            }
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage(), null);
        }
        $this->logger->addInfo("adding new user");
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", ["accepted_params" => getValidParams("/events")]);
    }
    return $response;
});  


//get all events
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
            $response = error($response, 500, $e->getMessage(), null);
        }
        $this->logger->addInfo("Getting all events");
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", ["accepted_params" => getValidParams("/events")]);
    }
    return $response;
});   

// .../events/{event_id}
//get event at the id
$app->get('/events/{id}[/]', function($request, $response, $args) {
    $id = $args["id"];
    $params = $request->getQueryParams();
    $params = addDefaults('/events/id', $params);
    if(validGET("/events/id", $params)){
        try{
            $events_query = new dbOperation($this->db);
            ///$results = $events_query->getEvent($id, $params);
            $results = $events_query->getEvent($id);
            if($events_query->isSuccessful()){
                $response = $response->withJSON($results, 200);
                //$response->getBody()->write($results);
            }
            else{
                $response = error($response, 404, "No event was found with that id", null);
                //$response->getBody()->write($results);
            }
        }
        catch(PDOexception $e){
            //$response->getBody()->write($e);
            $response = error($response, 500, $e->getMessage(), null);
        }
        //$this->logger->addInfo("Getting all events");
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", ["accepted_params" => getValidParams("/events/id")]);
    }
    return $response;
});   

//PUT .../events/id to update information
$app->put('/events/{id}[/]', function($request, $response, $args) {
    $body = $request->getParsedBody();
    $id = $args["id"];
    if(validPUT("/events/id", $body)){
        try{
            $events_query = new dbOperation($this->db);
            $events_query->getEvent($id);
            $event_success = $events_query->isSuccessful();
            $events_query->getUser($body["facebook_created_by"], "true");
            $user_success = $events_query->isSuccessful();
            if($event_success && $user_success) {
                $results = $events_query->updateEvent($id, $body);
                if($events_query->isSuccessful()){
                    $results = ["success" => $results];
                    $response = $response->withJSON($results, 200);
                }
                else{
                    $response = error($response, 500, "PUT failed", null);
                }
            }
            else{
                $response = error($response, 404, "No event or user was found with that id", null);
            }
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage(), null);
        }
    }
    else{
        $response = error($response, 400, "Bad request!", ["accepted_params" => getValidParams("/events/id")]);
    }
    return $response;
});  



//POST ../events/id/attendees to add a new attendee
$app->post('/events/{id}/attendees[/]', function($request, $response, $args) {
    $id = $args["id"];
    $params = $request->getQueryParams();
    $params = addDefaults('/events/id', $params);
    $body = $request->getParsedBody();
    if(validPOST("/events/id/attendees", $body)){
        try{
            $events_query = new dbOperation($this->db);
            $users_query = new dbOperation($this->db);
            $user_results = $users_query->getUser($body['user_id'], 'true');
            $event_results = $events_query->getEvent($id, $params);
            if($events_query->isSuccessful() && $users_query->isSuccessful()){
                $insert_attendee = $events_query;
                $results = $insert_attendee->insertEventAttendee($id, $body);
                $results = ["success" => $results, "link" => $request->getUri()];
                if($insert_attendee->isSuccessful()){
                     $response = $response->withJSON($results, 200);
                }
                $response = $response->withJSON($results, 200);
                //$response->getBody()->write($results);
            }
            else{
                $response = error($response, 404, "No event was found with that id", 
                    ["event sucess" => $events_query->isSuccessful(), "user success" => $users_query->isSuccessful()], null);
                //$response->getBody()->write($results);
            }
        }
        catch(PDOexception $e){
            //$response->getBody()->write($e);
            $response = error($response, 500, $e->getMessage(), null);
        }
        //$this->logger->addInfo("Getting all events");
    }
    else{
        $acceptedContent = ["accepted_params" => getValidParams("/events/id")];
        $response = error($response, 400, "Invalid parameters or body!", $acceptedContent);
    }
    return $response;
});  

//GET  .../events/{event_id}/attendees
$app->get('/events/{id}/attendees[/]', function($request, $response, $args) {
    //$query_c = new dbOperation($this->db);
    $id = $args["id"];
    $params = $request->getQueryParams();
    $params = addDefaults('/events/id', $params);
    if(validGET("/events/id", $params)){
        try{
            $events_query = new dbOperation($this->db);
            $events_query->getEvent($id, $params);
            if($events_query->isSuccessful()){
                $results = $events_query->getEventAttendance($id, $params);
                $response = $response->withJSON(["users" => $results], 200);
                //$response->getBody()->write($results);
            }
            else{
                $response = error($response, 404, "No event was found with that id", null);
                //$response->getBody()->write($results);
            }
        }
        catch(PDOexception $e){
            //$response->getBody()->write($e);
            $response = error($response, 500, $e->getMessage(), null);
        }
        //$this->logger->addInfo("Getting all events");
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", ["accepted_params" => getValidParams("/events/id")]);
    }
    return $response;
});   


//GET  .../events/{event_id}/attendees/{user_id}
//checks whether or not this user is attending this event
$app->get('/events/{event_id}/attendees/{user_id}[/]', function($request, $response, $args) {
    //$query_c = new dbOperation($this->db);
    $event_id = $args["event_id"];
    $user_id = $args["user_id"];
    $params = $request->getQueryParams();
    $params = addDefaults('/events/id', $params);
    if(validGET("/events/id", $params)){
        try{
            $query = new dbOperation($this->db);
            $query->getEvent($event_id, $params);
            if($query->isSuccessful()){
                $query->getUser($user_id, 'true');
                $query->isSuccessful();
                if($query->isSuccessful()){
                    $results = $query->isAttending($event_id, $user_id, $params);
                    $response = $response->withJSON(["attending" => $results], 200);
                    //$response->getBody()->write($results);
                }
                else{
                    $response = error($response, 404, "No user was found with that id", null);
                }
            }
            else{
                $response = error($response, 404, "No event was found with that id", null);
                //$response->getBody()->write($results);
            }
        }
        catch(PDOexception $e){
            //$response->getBody()->write($e);
            $response = error($response, 500, $e->getMessage());
        }
        //$this->logger->addInfo("Getting all events");
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", ["accepted_params" => getValidParams("/events/id")]);
    }
    return $response;
});   


//DELETE .../events/id/attendees/id
//deletes user from attending list for event
$app->delete('/events/{event_id}/attendees/{user_id}[/]', function($request, $response, $args) {
    //$query_c = new dbOperation($this->db);
    $event_id = $args["event_id"];
    $user_id = $args["user_id"];
    $params = $request->getQueryParams();
    $params = addDefaults('/events/id', $params);
    if(validGET("/events/id", $params)){
        try{
            $query = new dbOperation($this->db);
            $query->getEvent($event_id, $params);
            if($query->isSuccessful()){
                $query->getUser($user_id, 'true');
                if($query->isSuccessful()){
                    $results = $query->deleteEventAttendee($event_id, $user_id, $params);
                    $results = ["success" => $results];
                    $response = $response->withJSON($results, 200);
                    //$response->getBody()->write($results);
                }
                else{
                    $response = error($response, 404, "No user was found with that id", null);
                }
            }
            else{
                $response = error($response, 404, "No event was found with that id", null);
                //$response->getBody()->write($results);
            }
        }
        catch(PDOexception $e){
            //$response->getBody()->write($e);
            $response = error($response, 500, $e->getMessage());
        }
        //$this->logger->addInfo("Getting all events");
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", ["accepted_params" => getValidParams("/events/id")]);
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