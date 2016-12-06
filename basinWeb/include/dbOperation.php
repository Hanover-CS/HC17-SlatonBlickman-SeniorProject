<?php

 
class dbOperation
{
    //Database connection link
    private $conn;
    private $results;
    private $verb;
    private $fields;
 
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

    /*
     * Public helper functions
     */

    public function isSuccessful(){
        if($this->results == false){
            return false;
        }
        else{
            return true;
        }
        
    }

    /*
     * Getters and Setters
     */
    public function getResults(){
        return $this->results;
    }


    public function getUserFields(){
        $this->fields = $this->getTableFields("users");
        return $this->fields;
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
        $this->results = $query->fetch();
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
        $this->results = $query->execute($body);
        return $this->results;

    }

    public function insertUser($body){
        $sql = "INSERT INTO users (facebook_id, fname, lname) VALUES
                (:facebook_id, :fname, :lname)";
        $insert = $this->conn->prepare($sql);
        $this->results = $insert->execute($body);
        return $this->results;
    }

    public function deleteUser($id, $facebook_id){
        $sql = "DELETE FROM users";
        if($facebook_id == "true"){
            $sql = $sql . " WHERE facebook_id = :id AND _id != :id";
        }
        else{
            $sql = $sql . " WHERE facebook_id != :id AND _id = :id";
        }

        $delete = $this->conn->prepare($sql);
        $this->results = $delete->execute(["id" => $id]);
        return $this->results;

    }


    public function getEventFields(){
        return $this->getTableFields("events");
    }

    public function getEvents($params){
        $sql = 'SELECT * FROM events';
        $query = $this->conn->prepare($sql);
        $query->execute($params);
        //echo "<br>Selecting all users";
        $this->results = $query->fetchAll();
        return $this->results;
    }

    public function getEvent($id, $params){
        $sql = "SELECT events.*, users.fname, users.lname, users.facebook_id
                FROM events 
                INNER JOIN users on users.facebook_id = events.facebook_created_by
                WHERE events._id = ?";
        $query = $this->conn->prepare($sql);
        $query->execute([$id]);
        $this->results = $query->fetchAll();
        return $this->results;
    }

    public function insertEvent($body){
        $sql_vals = "INSERT INTO events (";
        $sql_vars = ") VALUES (";
        $i = 0;
        foreach($body as $key => $value){
            $i += 1;
            $sql_vals .= $key;
            $sql_vars .= ":" . $key;
            if($i != count($body)){
                $sql_vals .= ", ";
                $sql_vars .= ", ";
            }

        }
        $sql_vars = $sql_vars . ")";
        $sql = $sql_vals . $sql_vars;
        $insert = $this->conn->prepare($sql);
        $this->results = $insert->execute($body);
        return $this->results;
    }

    public function deleteEvent($id){
        $this->results = false;
        return false;
    }

    public function getUserEvents($id, $params){
        $results = [];
        $results2 = [];
        if($params["created"] == "true"){
            $results = ["created" => $this->getUserEventsCreated($id, $params)];
        }
        if($params['attending'] == "true"){
            $results2 = ["attending" => $this->getUserEventsAttending($id, $params)];
        }

        $this->results = array_merge($results, $results2);
        return $this->results;
    }

    public function getUserEventsCreated($id, $params){
        $sql = "SELECT events.*, users.fname, users.lname, users.facebook_id 
                FROM events 
                INNER JOIN users ON events.created_by = users._id ";

        if($params['facebook_id'] == 'true'){
            $sql .=  "WHERE users.facebook_id = ? ";
        }
        else{
            $sql .=  "WHERE users._id = ? ";
        }
        //echo $sql;
        $select = $this->conn->prepare($sql);
        $select->execute([$id]);
        $this->results = $select->fetchAll();
        return $this->results;

    }

    public function getUserEventsAttending($id, $params){
        $sql = "SELECT * FROM attendees 
                INNER JOIN events ON attendees.event_id = events._id 
                INNER JOIN users ON users._id = attendees.user_id ";

        if($params['facebook_id'] == 'true'){
            $sql .= "WHERE users.facebook_id = ? ";
        }
        else{
            $sql .= "WHERE users._id = ? ";
        }
        //echo $sql;
        $select = $this->conn->prepare($sql);
        $select->execute([$id]);
        $this->results = $select->fetchAll();
        return $this->results;

    }


    public function getEventAttendance($id, $params){
        $sql = "SELECT users.* FROM attendees 
                INNER JOIN events ON attendees.event_id = events._id 
                INNER JOIN users ON users._id = attendees.user_id 
                WHERE events._id = ? ";

        //echo $sql;
        $select = $this->conn->prepare($sql);
        $select->execute([$id]);
        $this->results = $select->fetchAll();
        return $this->results;

    }

    public function insertEventAttendee($event_id, $body){
        $sql = ("INSERT INTO attendees (event_id, user_id) VALUES (:e_id, :u_id)");
        $vals = ["e_id" => $event_id, "u_id" => $body['user_id']];
        $insert = $this->conn->prepare($sql);
        $this->results = $insert->execute($vals);
        return $this->results;
    }

    public function deleteEventAttendee($event_id, $user_id, $params){
        $sql = "DELETE FROM attendees WHERE event_id = :event_id AND user_id = :user_id";
        $vals = ["event_id" => $event_id, "user_id" => $user_id];
        $insert = $this->conn->prepare($sql);
        $this->results = $insert->execute($vals);
        return $this->results;
    }

    public function isAttending($event_id, $user_id, $params){
        $sql = "SELECT * FROM attendees 
                WHERE event_id = :event_id AND user_id = :user_id ";
        $vals = ["event_id" => $event_id, "user_id" => $user_id];
        //echo $sql;
        $select = $this->conn->prepare($sql);
        $select->execute($vals);
        $results = $select->fetchAll();
        if(sizeof($results) > 0){
            $this->results = true;
        }
        else{
            $this->results = false;
        }
        return $this->results;
    }

    public function getEventsByLocation($lat, $long, $place){
        return null;
    }



}

?>  