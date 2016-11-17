<?php

 
class dbOperation
{
    //Database connection link
    private $conn;
    private $results;
    private $verb;
 
    //Class constructor
    function __construct($db)
    {
        //Initializing our connection link of this class
        //by referring to the PDO object passed
        $this->conn = $db;
    }

    /*
     * Private helper functions
     */ 

    private function getTableFields($table){
        $sql = "DESCRIBE " . $table;
        $query = $this->conn->query($sql);
        $fields = $query->fetchAll();
        $field_names = [];
        foreach ($fields as $field){
            $field_names[] = $field["Field"];
        }
        return $field_names;
    }

    private function buildSQL($initQuery, $params){
        return "";
    }

    public function getResults(){
        if(count($this->results) >= 1){
            return $this->results;
        }
        else{
            return [];
        }
    }

    public function isSuccessful(){
        if($this->results == false){
            return false;
        }
        else{
            return true;
        }
        
    }

    private function setResults($results){
        $this->results = $results;
        return null;
    }

    private function setVerb($verb){
        $this->verb = $verb;
        return null;
    }
    /*
     * Getters and Setters
     */



    public function getUserFields(){
        $this->results = $this->getTableFields("users");
        return $this->results;
    }

    public function getUsers($params){
        $sql = 'SELECT * FROM users';
        $query = $this->conn->prepare($sql);
        $query->execute($params);
        //echo "<br>Selecting all users";
        $this->results = $query->fetchAll();
        return $this->results;
    }

    public function getUser($id, $facebook_id){
        if($facebook_id == "true"){
            $sql = "SELECT * FROM users WHERE facebook_id = ? AND _id != ?";
        }
        else{
            $sql = "SELECT * FROM users WHERE _id = ? AND facebook_id != ?";
        }
        $params = [$id, $id];
        $query = $this->conn->prepare($sql);
        $query->execute($params);
        //echo "Selecting all users with _id" + $id;
        $this->setResults($query->fetch());
        return $this->results;
    }

    public function updateUser($id, $body, $facebook_id){
        $sql = "UPDATE users SET ";
        $i = 0;
        foreach($body as $key => $value){
            $i += 1;
            $sql = $sql . $key . " = " . ":" . $key;
            if($i < count($body)){
                $sql = $sql . ", ";
            }

        }
        if($facebook_id == "true"){
            $sql = $sql . " WHERE facebook_id = :id AND _id != :id";
        }
        else{
            $sql = $sql . " WHERE facebook_id != :id AND _id = :id";
        }
        $body["id"] = $id;
        $query = $this->conn->prepare($sql);
        $results = $query->execute($body);
        $this->setResults($results);
        return $results;

    }

    public function insertUser($body){
        $sql = "INSERT INTO users (facebook_id, fname, lname) VALUES
                (:facebook_id, :fname, :lname)";
        $insert = $this->conn->prepare($sql);
        $this->setResults($insert->execute($body));
        return $this->results;
    }

    public function deleteUser($id){
        return null;
    }


    public function getEventFields(){
        return $this->getTableFields("events");
    }

    public function getEvents($params){
        return null;
    }

    public function getEvent($id, $params){
        return null;
    }

    public function insertEvent($id, $params){
        return null;
    }

    public function deleteEVent($id){
        return null;
    }

    public function getUserEvents($user_id, $params){
        return null;
    }


    public function getEventAttendance($id, $params){
        return null;
    }

    public function insertEventAttendee($event_id, $user_id, $params){
        $sql = ("INSERT INTO attendees (event_id, user_id) VALUES (:e_id, :u_id)");
        $vals = ["e_id" => $event_id, "u_id" => $user_id];
        return null;
    }

    public function deleteEventAttendee($event_id, $user_FB_id, $params){
        return null;
    }


    public function getEventsByLocation($lat, $long, $place){
        return null;
    }



}

?>  