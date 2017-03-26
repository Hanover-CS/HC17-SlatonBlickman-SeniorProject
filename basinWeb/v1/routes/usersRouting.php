<?php

require_once "helper.php";

use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;

/*
* BEGIN USER ROUTES
*/

    
/**
* GET: Handles the route for getting an array of all users
* /users or /users/
* Accepted paramaters: ["sort" => ["_id", "facebook_id", "fname", "lname", "nickname"], "direction" => ["asc", "desc"]]
*/
$app->get("/users[/]", function (Request $request, Response $response, $args) {
    $params = $request->getQueryParams();
    //defaults for /users will indicate sorting field and direction
    $params = addDefaults("/users", $params);

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
* Accepted body: ["facebook_id", "fname", "lname"]
* TODO: Implement check to make sure htere are not two users with the same Facebook ID
*/
$app->post("/users[/]", function (Request $request, Response $response, $args) {
    $body = $request->getParsedBody();

    if(validPOST("/users", $body)){
        try{

            $user_insert = new dbOperation($this->db);
            $user_insert->getUser($body["facebook_id"], "true");
            if(!$user_insert->isSuccessful()) {
                $results = $user_insert->insertUser($body);
                //give back a link to the user
                $user_uri = $request->getUri() . $body["facebook_id"] . "?facebook_id=true"; 

                if($user_insert->isSuccessful()){
                    $body = ["success" => true, "link" => $user_uri];
                    $response = $response->withJSON($body, 201);
                }
                else{
                    $response = error($response, 500, "Problem inserting user", ["success" => $results]);
                }  
            } 
            else{
                $response = error($response, 409, "Resource already exists", null);
            }
        }
        catch(PDOexception $e){
            $response = error($response, 500, $e->getMessage(), []);
        }
    }
    else{
        $accepted =  ["facebook_id, fname, lname" ];
        $response = error($response, 400, "Invalid body!", ["accepted" => $accepted, "given" => $body]);
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
$app->get("/users/{id}[/]", function (Request $request, Response $response, $args) {
    $id = $args["id"];
    $params = $request->getQueryParams();

    //add some default parameters if they are not specified
    //since we can use a user id or facebook id for users, 
    //we will set it to use the facebook id unless told otherwise by a paramater
    $params = addDefaults("/users/id", $params);
    $method = $request->getMethod();

    //ensure we didn"t get any unexpected parameters with the request
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
$app->put("/users/{id}", function (Request $request, Response $response, $args) {
    $id = $args["id"];
    $body = $request->getParsedBody();
    $params = $request->getQueryParams();
    //set facebook_id to true unless parameter says otherwise
    $params = addDefaults("/users/id", $params);

    //ensure the body only includes fields we allow updating for
    if(validPUT("/users/id", $body)){
        try{
            $get_user = new dbOperation($this->db);
            $get_user->getUser($id, $params["facebook_id"]);

           	//user must exist before updating
            if($get_user->isSuccessful()){
                $user_update = $get_user; //rename object for sake of readability; we don"t really need to create a new object for this
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
$app->delete("/users/{id}", function (Request $request, Response $response, $args) {
    $id = $args["id"];
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
* GET: Handles route for getting a user"s events.
* Accepted params: ["created" => ["true", "false"], "attending" => ["true", "false"], "facebook_id" => ["true", "false"]]
* TODO: Implement check to make sure htere are not two users with the same Facebook ID
*/
$app->get("/users/{id}/events[/]", function (Request $request, Response $response, $args) {
    $id = $args["id"];
    $params = $request->getQueryParams();
    $method = $request->getMethod(); //this line isn"t used anywhere
    $params = addDefaults("/users/id/events", $params);

    if(validGET("/users/id/events", $params)){
        try{
            $user_query = new dbOperation($this->db); 
            $user_query->getUser($id, $params["facebook_id"]);

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
