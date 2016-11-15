<?php

function validateGET($params, $validParams, $defaults){
    foreach($params as $key => $value){
        if (!array_key_exists($key, $validParams)){
            return false;
        }
        else{
            $validArgs = $validParams[$key];
            if(!in_array($value, $validArgs)){
                return false;
            }
        }
    }
    return true;
}

function validGET($route, $params){

    switch($route){
        case "/users/id":
            $validParams = ['facebook_id' => ['true', 'false']];
            $defaults = [];
            break;
        case "/users":
            $validParams = ['sort' => ['_id', 'facebook_id', 'fname', 'lname', 'nickname'], 'direction' => ['asc', 'desc']];
            $defaults = [];
            break;
        case "/events":
            $validParams = [];
            $defaults = [];
            break;
        case "/events/id":
            $validParams = [];
            $defaults = [];
            break;
        case "/events/id/attendees":
            $validParams = [];
            $defaults = [];
            break;
        default:
            return false;
            break;
    }
    return validateGET($params, $validParams, $defaults);
}

function validPOST($route, $params){
    return false;
}

function validPUT($route, $params){
    return false;
}

function validDELETE($route, $params){
    return false;
}


?>