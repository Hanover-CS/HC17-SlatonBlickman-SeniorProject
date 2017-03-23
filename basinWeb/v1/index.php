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
require_once '../../../protected/databaseKeys.php';

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
 * Helper functions
*/

/**
* Function for easily creating error messages when things go wrong in our requests
*
* example: error($response, 404, "User not found")
*
* @param Response $response the HTTP response object that contains the underlying JSON that will be returned for 
* each route
* @param $code an integer HTTP code that represents what errored
* @param $msg a string describing what errored
* @param $info a string giving useful information about the request; i.e. the given parameters and accepted ones. It will not be included in the response if set to null.
* @return Response the altered response object
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
 * ROUTES
*/

/**
* GET: returns the default page. 
* TODO: Return list of links to all available routes 
*/
$app->get('/', function($request, $response, $args) {
    //$query_c = new dbOperation($this->db);
   $response->getBody()->write( "Default page for http requests");
}); 

//--------------------

/*
* BEGIN USER ROUTES
*/


/**
* GET: Handles the route for getting an array of all users
* /users or /users/
* Accepted paramaters: ['sort' => ['_id', 'facebook_id', 'fname', 'lname', 'nickname'], 'direction' => ['asc', 'desc']]
*/
$app->get('/users[/]', function (Request $request, Response $response, $args) {
    $params = $request->getQueryParams();
    //defaults for /users will indicate sorting field and direction
    $params = addDefaults('/users', $params);

    if(validGET("/users", $params)){
        try{
            $users_query = new dbOperation($this->db);
            $results = $users_query->getUsers($params);

            if($users_query->isSuccessful()){
                $response = $response->withJSON(["users"=>$results], 200);
            }
            else{
                //return an empty list if there are no users in the database
                $response = $response->withJSON(["users"=>[]], 200);
            }
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage(), []);
        }
       // $this->logger->addInfo("Getting all users");
    }
    else{
        $acceptedParams = ["accepted_params" => getValidParams("/users")];
        $response = error($response, 400, "Invalid parameters!", $accepted_params);
    }

    return $response;
});

/**
* POST: Handles route for adding a new user
* Accepted body: ['facebook_id', 'fname', 'lname']
* TODO: Implement check to make sure htere are not two users with the same Facebook ID
*/
$app->post('/users[/]', function (Request $request, Response $response, $args) {
    $body = $request->getParsedBody();

    if(validPOST('/users', $body)){
        try{
            $user_insert = new dbOperation($this->db);
            $results = $user_insert->insertUser($body);
            //give back a link to the user
            $user_uri = $request->getUri() . $body['facebook_id'] . '?facebook_id=true'; 

            if($user_insert->isSuccessful()){
                $body = ["success" => true, "location" => $user_uri];
                $response = $response->withJSON($body, 201);
            }
            else{
                $response = error($response, 500, "Problem inserting user", ["success" => $results]);
            }   
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage(), []);
        }
    }
    else{
        $response = error($response, 400, "Invalid body!", ["accepted_body" => "facebook_id, fname, lname" ]);
    }
    //$this->logger->addInfo("Getting all users");

    return $response;
});


/**
* GET: Handles the route for getting a user
* /users/{id} or /users/{id}/
* 
* Accepted parameter: facebook_id : true/false
*/
$app->get('/users/{id}[/]', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $params = $request->getQueryParams();

    //add some default parameters if they are not specified
    //since we can use a user id or facebook id for users, 
    //we will set it to use the facebook id unless told otherwise by a paramater
    $params = addDefaults("/users/id", $params);
    $method = $request->getMethod();

    //ensure we didn't get any unexpected parameters with the request
    if(validGET("/users/id", $params)){
        try{
            $user_query = new dbOperation($this->db); 
            $results = $user_query->getUser($id, $params["facebook_id"]);

            if($user_query->isSuccessful()){
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
        //give back a list of the given and accepted params for debugging
        $acceptedParams = ["given_params" => $params, "accepted_params" => getValidParams("/users/id")];
        $response = error($response, 400, "Invalid parameters!", $acceptedParams);
    }

    return $response;
});

/**
* PUT: Handles the route for updating a user
* /users/{id} or /users/{id}/
* 
* Accepted parameter: facebook_id => true/false
* Accepted body: ["fname", "lname", "about", "nickname"]
*/
$app->put('/users/{id}', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $body = $request->getParsedBody();
    $params = $request->getQueryParams();
    //set facebook_id to true unless parameter says otherwise
    $params = addDefaults("/users/id", $params);

    //ensure the body only includes fields we allow updating for
    if(validPUT("/users/id", $body)){
        try{
            $get_user = new dbOperation($this->db);
            $get_user->getUser($id, $params['facebook_id']);

            //user must exist before updating
            if($get_user->isSuccessful()){
                $user_update = $get_user; //rename object for sake of readability; we don't really need to create a new object for this
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

/**
* DELETE: Handles the route for deleting a user
* /users/{id} or /users/{id}/
* 
* Accepted parameter: facebook_id => true/false
*/
$app->delete('/users/{id}', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $body = $request->getParsedBody();
    $params = $request->getQueryParams();
    //default facebook_id to true
    $params = addDefaults("/users/id", $params);

    //validate body and parameters
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
        $given = ["params_given" => $params];
        $response = error($response, 400, "Invalid content in parameters!", $given);
    }

    return $response;
});


/**
* GET: Handles route for getting a user's events.
* Accepted params: ['created' => ['true', 'false'], 'attending' => ['true', 'false'], 'facebook_id' => ['true', 'false']]
* TODO: Implement check to make sure htere are not two users with the same Facebook ID
*/
$app->get('/users/{id}/events[/]', function (Request $request, Response $response, $args) {
    $id = $args['id'];
    $params = $request->getQueryParams();
    $method = $request->getMethod(); //this line isn't used anywhere
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

/*
* END USER ROUTES
*/

//--------------------
 
/*
* BEGIN EVENT ROUTES
*/


/**
* GET: Handles route for getting all events
* Accepted params: None
*/
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
                //return an empty array instead of 404 if no results 
                $response = $response->withJSON(["events"=>[]], 200);
            }
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage(), null);
        }
        //$this->logger->addInfo("Getting all events");
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", ["accepted_params" => getValidParams("/events")]);
    }

    return $response;
});   

/**
* POST: Handles route for adding a new event
* Accepted params: none
* Accepted body: ['facebook_created_by', 'lat_coord', 'long_coord', 'description',  'time_start', 'title', 'date'];
* TODO: Serve back link to event created in response
*/
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
        //$this->logger->addInfo("adding new user");
    }
    else{
        $acceptedParams = [];
        $response = error($response, 400, "Invalid parameters!", ["accepted_params" => getValidParams("/events")]);
    }

    return $response;
});  


/**
* GET: Handles route for getting an event
* Accepted params: none
*/
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

            }
            else{
                $response = error($response, 404, "No event was found with that id", null);
            }
        }
        catch(PDOexception $e){
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

/**
* DELETE: Handles route for deleting an event
* Accepted params: none
*/
$app->delete('/events/{id}[/]', function($request, $response, $args) {
    $id = $args["id"];
    $params = $request->getQueryParams();
    $params = addDefaults('/events/id', $params);


    if(validDELETE("events/id", $params) and validGET("/events/id", $params)){
        try{
            $events_query = new dbOperation($this->db);
            ///$results = $events_query->getEvent($id, $params);
            $results = $events_query->deleteEvent($id);

            if($events_query->isSuccessful()){
                $response = $response->withJSON(["marked for deletion"=>$results], 202);
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


/**
* PUT: Handles route for updating an event
* Accepted params: none
* Accepted body: ['facebook_created_by', 'lat_coord', 'long_coord', 'description',  'time_start', 'title', 'date']
*/
$app->put('/events/{id}[/]', function($request, $response, $args) {
    $body = $request->getParsedBody();
    $id = $args["id"];

    if(validPUT("/events/id", $body)){
        try{
            $events_query = new dbOperation($this->db);

            //check if that event exists
            $events_query->getEvent($id);
            $event_success = $events_query->isSuccessful();

            //check if that is a valid user for creating an event
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



/**
* POST: Handles route for adding a new attendee to an event
* Accepted params: none 
* Accepted body: ['user_id'] 
* NOTE: user_id MUST be a facebook id or the response will 404
*/
$app->post('/events/{id}/attendees[/]', function($request, $response, $args) {
    $id = $args["id"];
    $params = $request->getQueryParams();
    $params = addDefaults('/events/id', $params);
    $body = $request->getParsedBody();

    if(validPOST("/events/id/attendees", $body)){
        try{
            //check if that user and event exist
            $events_query = new dbOperation($this->db);
            $users_query = new dbOperation($this->db);
            $user_results = $users_query->getUser($body['user_id'], 'true');
            $event_results = $events_query->getEvent($id, $params);

            if($events_query->isSuccessful() && $users_query->isSuccessful()){
                $insert_attendee = $events_query;
                $results = $insert_attendee->insertEventAttendee($id, $body);
                $results = ["success" => $results, "link" => $request->getUri()]; //I'm not sure this getUri does anything useful

                if($insert_attendee->isSuccessful()){
                     $response = $response->withJSON($results, 200);
                }
                else{
                     $response = error($response, 500, "Unknown problem when executing POST", null);
                }
                // $response = $response->withJSON($results, 200);

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

/**
* GET: Handles route for getting all attendees of an event
* Accepted params: none 
* returns list of users as JSON objects
*/
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


/**
* GET: Handles route for checking whether a particular user is attending an event
* Accepted params: none 
* returns JSON object holding a boolean indicating true for attending and false otherwise
*/
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


/**
* DELETE: Handles route for deleting a user's attendance of an event
* Accepted params: none 
*/
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

/*
* END EVENT ROUTES
*/  

//--------------------

/*
* START APPLICATION
*/
$app->run();
