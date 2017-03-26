<?php

use blickman\unitTestAPI\App;
use Slim\Http\Environment;
use Slim\Http\Request;
require "./unitTests/App.php";

/**
* Class for doing unit tests on basinWeb routes.
* followed this guide for installation and instruction:
* https://medium.com/@Andela/writing-testable-api-apps-in-slim-framework-29905970941b#.g3hj5rjsh
* Need to have an empty database tables. To import the tables, execute '../include/db_basin_import.sql' in your database
*/
class routeTest extends PHPUnit_Framework_TestCase
{
	protected $app;

	private function printMsg ($msg) {
		echo ("\n" . $msg . "\n");
	}

	private function getJSON($body){
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

	    $this->printMsg("only has one key 'users'");
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
	    $result = $this->getJSON($response->getBody());
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

	    $result = $this->getJSON($response->getBody());

	    $this->printMsg("has key events");
	    $this->assertArrayHasKey("events", $result);
	    
	    $this->printMsg("events has 2 elements");
	    $this->assertTrue(sizeof($result["events"]) == 2);

	    $this->printMsg("contains key created");
	    $this->assertArrayHasKey("created", $result["events"]);
	    
	    $this->printMsg("contains key attending");
	    $this->assertArrayHasKey("attending", $result["events"]);

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