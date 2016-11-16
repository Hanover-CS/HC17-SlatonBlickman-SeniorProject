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
            $defaults = ['facebook_id' => 'false'];
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
    //$params = addDefaults($params, $defaults);
    return validateGET($params, $validParams);
}

function validPOST($route, $params, $body){
    return false;
}

function validPUT($route, $params, $body){
    return false;
}

function validDELETE($route, $params){
    return false;
}


?>