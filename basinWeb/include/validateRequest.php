<?php

/**
* Included in this file are helper functions to validate paramaters and bodies given in HTTP requests in basinWeb
*/

/**
* Function that checks if all parameters are valid for GET and whether the given value is also allowed
* @param $params the list of paramaters given from the HTTP request
* @param $validParams the parameters to check against params
* @return boolean whether $params is valid or not
*/
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

/**
* Function that checks if the body keys are valid for an HTTP request
* @param $body the body from the HTTP request
* @param $validBody the valid keys to check for in body
* @return boolean whether $body is valid or not
*/
function validateBody($body, $validBody){
    foreach($body as $key => $value){
        if(!in_array($key, $validBody)){
            return false;
        }
    }
    return true;
}

/**
* Function gets the defaykt parameters for the given route. Defaults to empty. Not all routes necessarily have any paramaters that can be given.
* @param @route the route to get default paramaters for
* @return AssociativeArray with paramaters as keys associated with a single default value
*/
function getDefaults($route){
    switch($route){
        case "/users/id":
            $defaults = ['facebook_id' => 'true'];
            break;
        case "/users":
            $defaults = ['sort' => 'fname', 'direction' => 'asc'];
            break;
        case "/users/id/events":
            $defaults = ['created' => 'true', 'attending' => 'true', 'facebook_id' => 'false'];
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

/**
* Function that adjusts parameter object to set default values for keys if the default paramater is not present
* @param $route the route to get defaults for from getDefaults($route)
* @param $params the paramaters given in the HTTP request to add defaults to
* @return $params the updated parameters with defaults
* @see getDefaults($route)
*/
function addDefaults($route, $params){
    $defaults = getDefaults($route);
    foreach($defaults as $default => $val){
        if(!array_key_exists($default, $params)){
            $params[$default] = $val;
        }
    }
    return $params;
}

/**
* Function gets the valid parameters for the given route. Defaults to empty. Not all routes necessarily have any paramaters that can be given.
* @param @route the route to get valid paramaters for
* @return AssociativeArray with paramaters as keys and valid values associated with the keys
*/
function getValidParams($route){
    switch($route){
        case "/users/id":
            $validParams = ['facebook_id' => ['true', 'false']];
            break;
        case "/users":
            $validParams = ['sort' => ['_id', 'facebook_id', 'fname', 'lname', 'nickname'], 'direction' => ['asc', 'desc']];
            break;
        case "/users/id/events":
            $validParams = ['created' => ['true', 'false'], 'attending' => ['true', 'false'], 'facebook_id' => ['true', 'false']];
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
            $validParams = [];
            break;
    }
    //$params = addDefaults($params, $defaults);
    return $validParams;

}

/**
* Function that checks if all parameters are valid for GET and whether the given value is also allowed
* @param $route the route to get valid paramaters for
* @param $params the parameters given in the HTTP request
* @return boolean whether $params is valid or not
*/
function validGET($route, $params){
    return validateGET($params, getValidParams($route));
}

/**
* Function that checks if the body keys are valid for POST
* @param $route the route to get valid paramaters for
* @param $body the body given in the HTTP request
* @return boolean whether $body is valid or not
*/
function validPOST($route, $body){
    switch($route){
        case "/users/id":
            return false;
            break;
        case "/users":
            $validBody = ['facebook_id', 'fname', 'lname'];;
            break;
        case "/events":
            $validBody = ['facebook_created_by', 'lat_coord', 'long_coord', 'description',  'time_start', 'title', 'date'];
            break;
        case "/events/id":
            return false;
            break;
        case "/events/id/attendees":
            $validBody = ['user_id'];
            break;
        default:
            return false;
            break;
    }
    return validateBody($body, $validBody);
}

/**
* Function that checks if the body keys are valid for PUT
* @param $route the route to get valid paramaters for
* @param $body the body given in the HTTP request
* @return boolean whether $body is valid or not
*/
function validPUT($route, $body){
    switch($route){
        case "/users/id":
            $validBody = ["fname", "lname", "about", "nickname"];
            break;
        case "/users":
            return false;
            break;
        case "/events":
            return false;
            break;
        case "/events/id":
            $validBody = ['facebook_created_by', 'lat_coord', 'long_coord', 'description',  'time_start', 'title', 'date'];
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

/**
* Function that checks if the body keys are valid for PUT
* TODO: This function currently always returns true and ignores paramater. Could probably just use validGET to validate.
* @param $route the route to get valid paramaters for
* @param $body the body given in the HTTP request
* @return boolean whether $body is valid or not
*/
function validDELETE($route, $params){
    // switch($route){
    //     case "/users/id":
    //         $validBody = true;
    //         break;
    //     case "/users":
    //         return false;
    //         break;
    //     case "/events":
    //         return false;
    //         break;
    //     case "/events/id":
    //         $validBody = true;
    //         break;
    //     case "/events/id/attendees":
    //         return false;
    //         break;
    //     default:
    //         return false;
    //         break;
    // }
    return true;
}


?>