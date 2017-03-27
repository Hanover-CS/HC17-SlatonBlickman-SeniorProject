<?php

use blickman\unitTestAPI\App;
use Slim\Http\Environment;
use Slim\Http\Request;
require "./unitTests/App.php";

/**
* Class for doing unit tests on basinWeb routes.
* followed this guide for installation and instruction:
* https://medium.com/@Andela/writing-testable-api-apps-in-slim-framework-29905970941b#.g3hj5rjsh
* Need to have an empty database tables. To import the tables, execute "../include/db_basin_import.sql" in your database
*/
class routeTest extends PHPUnit_Framework_TestCase
{
	protected $app;

	//set these to access links between functions
	private $eventLink;
	private $userLink;

	//set to access a posted body between functions
	private $body;

	private function printMsg ($msg) {
		echo ("\n" . $msg . "\n");
	}

	private function fromJSON($body){
		return json_decode($body, true);
	}

	private function getResponseResult($method, $uri, $body){
		$env = Environment::mock([
	        "REQUEST_METHOD" => $method,
	        "REQUEST_URI"    => $uri,
	        "CONTENT_TYPE"   => "application/x-www-form-urlencoded"
	        ]);

		//$this->printMsg(json_encode($body));
		$request = Request::createFromEnvironment($env)->withParsedBody($body);
	    $this->app->getContainer()["request"] = $request;
	    $response = $this->app->run(true);

	    return $response;
	}

	public function setUp() {
		$this->eventLink = "";
		$this->userLink = "";
		$this->body = [];

        $this->app = (new App())->get();
    }

    public function testMainPageGET() {
    	$this->printMsg("MAIN PAGE TEST");

    	$response = $this->getResponseResult("GET", "/", null);

	    $this->assertSame($response->getStatusCode(), 200);
	    //$this->assertSame((string)$response->getBody(), "Default page for http requests");
    }

     public function testGETUsers() {
      	$this->printMsg("TEST GET /users");

		$uri = "/users/";

     	$response = $this->getResponseResult("GET", $uri, null);
	    $this->assertSame($response->getStatusCode(), 200);

	 	$result = json_decode($response->getBody(), true);

	    $this->printMsg("has key users");
	    $this->assertArrayHasKey("users", $result);

	    $this->printMsg("contains array of users");
	    $this->assertSame("array", gettype($result["users"]));

	    $this->printMsg("only has one key users");
	    $this->assertTrue(count($result) == 1);

	}

	public function testPOSTUsers(){
	    $this->printMsg("TEST POST /users TO ADD USER");

		$uri = "/users/";

	    $this->printMsg("with invalid body");
	    $fb = "0";
	    $fname = "Slaton";
	    $lname = "Blickman";

	    $body = ["facebook_id" => $fb, "fname" => $fname, "lname" => $lname, "junk" => "asd"];
	    $response = $this->getResponseResult("POST", $uri, $body);
	    $this->assertSame($response->getStatusCode(), 400);

	    $this->printMsg("with valid body");	
	    $body = ["facebook_id" => $fb, "fname" => $fname, "lname" => $lname];
	    $response = $this->getResponseResult("POST", $uri, $body);
	    $returned = (string)($response->getBody());
	    //$this->printMsg($returned);
	    $this->assertSame($response->getStatusCode(), 201);

	    $this->printMsg("POST again");
	   	$response = $this->getResponseResult("POST", $uri, $body);
	    $returned = (string)($response->getBody());
	    //$this->printMsg($returned);
	    $this->assertSame($response->getStatusCode(), 409);

	    $this->printMsg("only one user created");
	    $response = $this->getResponseResult("GET", $uri, null);
	    $this->assertSame($response->getStatusCode(), 200);
	 	$result = json_decode($response->getBody(), true);
	 	$this->assertTrue(count($result) == 1);

    }

    public function testGETUser(){
    	$this->printMsg("TEST GET /users/{id}");
    	$uri = "/users/";
    	$fb = "0";
    	$fname = "Slaton";
    	$lname = "Blickman";

    	$this->printMsg("get created user");
	    $response = $this->getResponseResult("GET", $uri . $fb, null);
	    $this->assertSame($response->getStatusCode(), 200);
	    $result = $this->fromJSON($response->getBody());
	    $this->assertSame($result["facebook_id"], $fb);
	    $this->assertSame($result["fname"], $fname);
	    $this->assertSame($result["lname"], $lname);

	    $this->printMsg("get not-created user");
	   	$response = $this->getResponseResult("GET", $uri . "1", null);
	    $this->assertSame($response->getStatusCode(), 404);

    }

    public function testPUTUser(){
    	$this->printMsg("TEST PUT /users/{id}");

    	$uri = "/users/";
    	$fb = "0";
    	$new_fname = "Ziggy";
    	$new_lname = "Stardust";
    	$nickname = "David Bowie";
    	$about = "I'm a star.";

		$this->printMsg("update user with invalid body");
		$body = ["facebook_id" => $fb, "fname" => $new_fname, "lname" => $new_lname];
		$response = $this->getResponseResult("PUT", $uri . $fb, $body);
		$this->assertSame($response->getStatusCode(), 400);

		$this->printMsg("update user with valid body");
		$body = ["nickname" => $nickname, "about" => $about, "fname" => $new_fname, "lname" => $new_lname];
		$response = $this->getResponseResult("PUT", $uri . $fb, $body);
		$this->assertSame($response->getStatusCode(), 200);

    }

    public function testGETUserEvents(){
    	$this->printMsg("TEST GET /users/{id}/events");

    	$uri = "/users/";
    	$fb = "0";
    	$uri .= $fb . "/events";
		
		$response = $this->getResponseResult("GET", $uri, null);
	    $this->assertSame($response->getStatusCode(), 200);

	    $result = $this->fromJSON($response->getBody());

	    $this->printMsg("has key events");
	    $this->assertArrayHasKey("events", $result);
	    
	    $this->printMsg("events has 2 elements");
	    $this->assertTrue(sizeof($result["events"]) == 2);

	    $this->printMsg("contains key created");
	    $this->assertArrayHasKey("created", $result["events"]);
	    
	    $this->printMsg("contains key attending");
	    $this->assertArrayHasKey("attending", $result["events"]);

    }

    public function testGETEvents(){
    	$this->printMsg("TEST GET /events/");

    	$uri = "/events/";

    	$this->printMsg("valid route");
    	$response = $this->getResponseResult("GET", $uri, null);
    	$this->assertSame($response->getStatusCode(), 200);

    	$result = $this->fromJSON($response->getBody());

    	$this->printMsg("has key events");
	    $this->assertArrayHasKey("events", $result);

	    $this->printMsg("contains array of events");
	    $this->assertSame("array", gettype($result["events"]));

	    $this->printMsg("only has one key events");
	    $this->assertTrue(count($result) == 1);

    }

    public function testPOSTEvents(){
    	$this->printMsg("TEST POST /events/");

    	$uri = "/events/";
    	$fb = "0";
    	// Accepted body: ["facebook_created_by", "lat_coord", "long_coord", "description",  "time_start", "title", "date"];

    	$this->printMsg("with invalid body");
    	$body = ["junk" => "asd"];
    	$response = $this->getResponseResult("POST", $uri, $body);
    	$this->assertSame($response->getStatusCode(), 400);

    	$body = ["facebook_created_by" => "11", "lat_coord" => 87.0, "long_coord" => 87.0, 
    		"description" => "testing",  "time_start" => "01:00", "title" => "webtest", "date" => "03-03-03"];
    	$this->body = $body;
    	$this->printMsg("with invalid facebook id");
    	$response = $this->getResponseResult("POST", $uri, $body);
    	$this->assertSame($response->getStatusCode(), 404);


    	$this->printMsg("with valid body");
    	$body["facebook_created_by"] = $fb;
    	$response = $this->getResponseResult("POST", $uri, $body);
    	//$this->printMsg(json_code($response->getBody()));
    	$result = $this->fromJSON($response->getBody());

    	$this->printMsg("has key success");
    	$this->assertArrayHasKey("success", $result);


    	$this->printMsg("has key link");
    	$this->assertArrayHasKey("link", $result);
    	$this->printMsg("link is " . $result["link"]);
    	$this->eventLink = $result["link"];


    }

    public function testGETEvent(){
    	$this->printMsg("TEST GET /event/{id}");

    	$uri = $this->eventLink;

    	$this->printMsg("get created event");
    	$response = $this->getResponseResult("GET", $uri, null);
    	$this->assertSame($response->getStatusCode(), 200);

    	$result = $this->fromJSON($response->getBody());

    	for($i = 0; $i < sizeof($this->body); $i++){
    		$key = $this->body[$i];
    		$value = $this->body[$key];

    		$this->assertArrayHasKey($key, $result);
    		$this->assertSame($value, $result[$key]);
    	}
    }

    public function testPUTEvent(){
		//Accepted body: ["facebook_created_by", "lat_coord", "long_coord", "description",  "time_start", "title", "date"]
        $this->printMsg("TEST PUT /event/{id}");

    	if($this->eventLink == null){
    		$response = $this->getResponseResult("GET", "/events/", null);
    		$result = $this->fromJSON($response->getBody());
    		$uri = "/events/" . $result["events"][0]["_id"];
    		// $uri = "/events/33";
    	}
    	else{
    		$uri = $this->eventLink;
    	}

    	// $uri = "/events/57";

    	$body = ["facebook_created_by" => "0", "lat_coord" => 87.0, "long_coord" => 87.0, 
    		"description" => "testing",  "time_start" => "01:00", "title" => "webtest", "date" => "03-03-03"];

        $invalidBody = $body;
        $invalidBody["junk"] = "asda";
        $response = $this->getResponseResult("PUT", $uri, $invalidBody);
        $this->assertSame($response->getStatusCode(), 400);

        $this->printMsg("put with invalid user id");
        $invalidBody2 = $body;
        $invalidBody2["facebook_created_by"] = "11";
        $response = $this->getResponseResult("PUT", $uri, $invalidBody2);
        $this->assertSame($response->getStatusCode(), 404);

        $this->printMsg("put with invalid event id");
        $response = $this->getResponseResult("PUT", "/events/999999", $invalidBody2);
        $this->assertSame($response->getStatusCode(), 404);

        $this->printMsg("put with valid body");
        $new_body = $body;
        $new_body["title"] = "Check me out!";
        //$this->printMsg(json_encode($new_body));
        $response = $this->getResponseResult("PUT", $uri, $new_body);
        //$this->printMsg((string)$response->getBody());
        $this->assertSame($response->getStatusCode(), 200);

       	$response = $this->getResponseResult("GET", $uri, null);

        $result = $this->fromJSON($response->getBody());

        $this->assertSame($new_body["title"], $result["title"]);
    
    }


    public function testGETEventAttendees(){
    	$this->printMsg("TEST GET /event/{id}/attendees");

    	if($this->eventLink == null){
    		$response = $this->getResponseResult("GET", "/events/", null);
    		$result = $this->fromJSON($response->getBody());
    		$uri = "/events/" . $result["events"][0]["_id"] . "/attendees";
    		// $uri = "/events/33";
    	}
    	else{
    		$uri = $this->eventLink . "/attendees";
    	}

    	$response = $this->getResponseResult("GET", $uri, null);
    	$this->assertSame($response->getStatusCode(), 200);

    	$result = $this->fromJSON($response->getBody());

    	//$this->printMsg((string) $response->getBody());
    	$this->assertArrayHasKey("users", $result);
    	$this->assertSame("array", gettype($result["users"]));

    }

    public function testPOSTEventAttendees(){
    	$this->printMsg("TEST POST /event/{id}/attendees");

    	if($this->eventLink == null){
    		$response = $this->getResponseResult("GET", "/events/", null);
    		$result = $this->fromJSON($response->getBody());
    		$uri = "/events/" . $result["events"][0]["_id"] . "/attendees";
    		// $uri = "/events/33";
    	}
    	else{
    		$uri = $this->eventLink . "/attendees";
    	}

    	$this->printMsg("with invalid user id");
    	$body = ["user_id" => "11"];
    	$response = $this->getResponseResult("POST", $uri, $body);
    	$this->assertSame($response->getStatusCode(), 404);

    	$this->printMsg("with valid user id");
    	$body = ["user_id" => "0"];
    	$response = $this->getResponseResult("POST", $uri, $body);
    	$this->assertSame($response->getStatusCode(), 200);

    }

    public function testGETEventUserAttendee(){
    	$this->printMsg("TEST GET /event/{id}/attendees/{user_id}");
    	if($this->eventLink == null){
    		$response = $this->getResponseResult("GET", "/events/", null);
    		$result = $this->fromJSON($response->getBody());
    		$uri = "/events/" . $result["events"][0]["_id"] . "/attendees/";
    		// $uri = "/events/33";
    	}
    	else{
    		$uri = $this->eventLink . "/attendees/";
    	}

    	$bad_uri = $uri . "11";
    	$good_uri = $uri . "0";

    	$this->printMsg("with invalid user id");
    	$this->printMsg("Using uri  " . $bad_uri);
    	$response = $this->getResponseResult("GET", $bad_uri, null);
    	$this->assertSame($response->getStatusCode(), 404);

    	$this->printMsg("with valid user id");
    	$this->printMsg("Using uri  " . $good_uri);
    	$response = $this->getResponseResult("GET", $good_uri, null);
    	$this->assertSame($response->getStatusCode(), 200);
    }


    //Done last for clean up
    public function testDELETEEventAttendee(){
    	$this->printMsg("TEST DELETE /event/{id}/attendees/{user_id}");

    	if($this->eventLink == null){
    		$response = $this->getResponseResult("GET", "/events/", null);
    		$result = $this->fromJSON($response->getBody());
    		$uri = "/events/" . $result["events"][0]["_id"] . "/attendees/";
    		// $uri = "/events/33";
    	}
    	else{
    		$uri = $this->eventLink . "/attendees/";
    	}

  		$bad_uri = $uri . "11";
    	$good_uri = $uri . "0";

    	$this->printMsg("with invalid user id");
    	$this->printMsg("Using uri  " . $bad_uri);
    	$response = $this->getResponseResult("DELETE", $bad_uri, null);
    	$this->assertSame($response->getStatusCode(), 404);

    	$this->printMsg("with valid user id");
    	$this->printMsg("Using uri  " . $good_uri);
    	$response = $this->getResponseResult("DELETE", $good_uri, null);
    	$this->assertSame($response->getStatusCode(), 200);


    }

    public function testDELETEEVent(){
    	$this->printMsg("TEST DELETE /events/{id}");

		$response = $this->getResponseResult("GET", "/events/", null);
		$result = $this->fromJSON($response->getBody());
		$uri = "/events/" . $result["events"][0]["_id"];
		// $uri = "/events/33";

    	$bad_uri = "/events/999999";

    	$this->printMsg("with invalid event id");
    	$this->printMsg("Using uri  " . $bad_uri);
    	$response = $this->getResponseResult("DELETE", $bad_uri, null);
    	$this->assertSame($response->getStatusCode(), 404);


    	$this->printMsg("with valid event id");
    	$this->printMsg("Using uri  " . $uri);
    	$response = $this->getResponseResult("DELETE", $uri, null);
    	$this->assertSame($response->getStatusCode(), 202);


    }

    public function testDELETEUser(){
    	$this->printMsg("TEST DELETE /users/{id}");

    	$uri = "/users/";
    	$fb = "0";

    	$this->printMsg("delete user with existing id");
    	$response = $this->getResponseResult("DELETE", $uri . $fb, null);
		$this->assertSame($response->getStatusCode(), 200);

		$this->printMsg("delete user with nonexisting id");
    	$response = $this->getResponseResult("DELETE", $uri . "1" , null);
		$this->assertSame($response->getStatusCode(), 404);

    }


}