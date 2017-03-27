<?php

require_once "helper.php";

use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;


/**
* GET: returns the default page. 
* TODO: Return list of links to all available routes 
*/
$app->get("[/]", function($request, $response, $args) {
	$routes = [];
	$events = [];
	$users = [];

	$users["/users/"] = ["methods" => ["GET", "POST"]];
	$users["/users/{user_id}/"] = ["methods" => ["GET", "PUT", "DELETE"]];
	$users["/users/{user_id/events"] = ["methods" => ["GET"]];

	$events["/events/"] = ["methods" => ["GET", "POST"]];
	$events["/events/{event_id}"] = ["methods" => ["GET", "PUT", "DELETE"]];
	$events["/events/{event_id/attendees"] = ["methods" => ["GET", "POST"]];
	$events["/events/{event_id/attendees/{user_id/"] = ["methods" => ["GET", "DELETE"]];

	$routes["user routes"] = $users;
	$routes["event routes"] = $events;

	$response = $response->withJSON($routes, 200);

	return $response;
});