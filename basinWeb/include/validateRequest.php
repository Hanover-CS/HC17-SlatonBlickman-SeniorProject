<?php

function validateGET($params, $validParams){
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

function validateBody($body, $validBody){
    foreach($body as $key => $value){
        if(!in_array($key, $validBody)){
            return false;
        }
    }
    return true;
}

function addDefaults($route, $params){
    $defaults = getDefaults($route);
    foreach($defaults as $default => $val){
        if(!array_key_exists($default, $params)){
            $params[$default] = $val;
        }
    }
    return $params;
}

function getDefaults($route){

    switch($route){
        case "/users/id":
            $defaults = ['facebook_id' => 'false'];
            break;
        case "/users":
            //$defaults = ['sort' => 'fname', 'direction' => 'asc'];
            $defaults = [];
            break;
        case "/events":
            $defaults = [];
            break;
        case "/events/id":
            $defaults = [];
            break;
        case "/events/id/attendees":
            $defaults = [];
            break;
        default:
            $defaults = [];
            break;
    }
    //$params = addDefaults($params, $defaults);
    return $defaults;
}


function validGET($route, $params){

    switch($route){
        case "/users/id":
            $validParams = ['facebook_id' => ['true', 'false']];
            break;
        case "/users":
            $validParams = ['sort' => ['_id', 'facebook_id', 'fname', 'lname', 'nickname'], 'direction' => ['asc', 'desc']];
            break;
        case "/events":
            $validParams = [];
            break;
        case "/events/id":
            $validParams = [];
            break;
        case "/events/id/attendees":
            $validParams = [];
            break;
        default:
            return false;
            break;
    }
    //$params = addDefaults($params, $defaults);
    return validateGET($params, $validParams);
}

function validPOST($route, $body){

    switch($route){
        case "/users/id":
            return false;
            break;
        case "/users":
            $validBody = ['facebook_id', 'fname', 'lname'];;
            break;
        case "/events":
            $validBody = [];
            break;
        case "/events/id":
            return false;
            break;
        case "/events/id/attendees":
            return false;
            break;
        default:
            return false;
            break;
    }
    return validateBody($body, $validBody);
}

function validPUT($route, $body){
    switch($route){
        case "/users/id":
            $validBody = ["fname", "lname", "about"];
            break;
        case "/users":
            return false;
            break;
        case "/events":
            return false;
            break;
        case "/events/id":
            $validBody = [];
            break;
        case "/events/id/attendees":
            return false;
            break;
        default:
            return false;
            break;
    }
    return validateBody($body, $validBody);
}

function validDELETE($route, $params){
    return false;
}


?>