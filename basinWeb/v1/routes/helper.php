<?php

use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;

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