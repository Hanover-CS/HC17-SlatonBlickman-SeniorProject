<?php

require_once 'helper.php';

use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;
/*
* BEGIN ATTENDEES ROUTING
*/

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