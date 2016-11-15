<?php

function validate($params, $validParams, $defaults){
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
		case "users/id":
			$validParams = ['facebook_id' => ['true', 'false']];
			break;
		case "/users":
			$validParams = ['sort' => ['_id', 'facebook_id', 'fname', 'lname', 'nickname'], 'direction' => ['asc', 'desc']];
			break;
		default:
			$validParams = [];
			break;
	}
	return validate($params, $validParams, []);
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