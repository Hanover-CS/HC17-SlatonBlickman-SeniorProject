<?php

require_once "helper.php";

use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;

/*
* BEGIN EVENT ROUTES
*/

/**
* GET: Handles route for getting all events
* Accepted params: None
*/
$app->get("/events[/]", function($request, $response, $args) {
    $params = $request->getQueryParams();
    $params = addDefaults("/events", $params);

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
* Accepted body: ["facebook_created_by", "lat_coord", "long_coord", "description",  "time_start", "title", "date"];
* TODO: Serve back link to event created in response
*/
$app->post("/events[/]", function($request, $response, $args) {
    $body = $request->getParsedBody();

    if(validPOST("/events", $body)){
        try{
            $events_query = new dbOperation($this->db);
            $events_query->getUser($body["facebook_created_by"], "true");
            if($events_query->isSuccessful()){
                $results = $events_query->insertEvent($body);

                if($events_query->isSuccessful()){

                    $results = ["success" => $results, "link" => "/events/" . $results];
                    $response = $response->withJSON($results, 201);
                }
                else{
                    $response = error($response, 500, "Unknown problem when executing POST", null);
                }
            }
            else{
                $response = error($response, 404, "No user with that facebook id was found", null);
            }
        }
        catch(PDOexception $e){
            $response = error($response, 409, $e->getMessage(), null);
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
$app->get("/events/{id}[/]", function($request, $response, $args) {
    $id = $args["id"];
    $params = $request->getQueryParams();
    $params = addDefaults("/events/id", $params);

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
$app->delete("/events/{id}[/]", function($request, $response, $args) {
    $id = $args["id"];
    $params = $request->getQueryParams();
    $params = addDefaults("/events/id", $params);


    if(validDELETE("events/id", $params) and validGET("/events/id", $params)){
        try{
            $events_query = new dbOperation($this->db);

            $events_query->getEvent($id);
            if($events_query->isSuccessful()){
                $results = $events_query->deleteEvent($id);
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
* Accepted body: ["facebook_created_by", "lat_coord", "long_coord", "description",  "time_start", "title", "date"]
*/
$app->put("/events/{id}[/]", function($request, $response, $args) {
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
