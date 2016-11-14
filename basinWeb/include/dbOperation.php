<?php

 
class dbOperation
{
    //Database connection link
    private $conn;
 
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


    /*
     * Getters and Setters
     */

    public function getUserFields(){
        return $this->getTableFields("users");
    }

    public function getUsers($params){
        $sql = 'SELECT * FROM users_test';
        $query = $this->conn->prepare($sql);
        $query->execute();
        echo "<br>Selecting all users";
        return $query->fetchAll();
    }

    public function getUser($id){
        $sql = "SELECT * FROM users_test WHERE _id = ?";
        $query = $this->conn->prepare($sql);
        $query->execute([$id]);
        //echo "Selecting all users with _id" + $id;
        $q = $query->fetchAll();
        return $q;
    }

    public function updateUser($id, $params){
        return null;

    }

    public function insertUser($id, $params){
        return null;
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

    public function getUserEvents($user_FB_id, $params){
        return null;
    }


    public function getEventAttendance($id, $params){
        return null;
    }

    public function insertEventAttendee($event_id, $user_FB_id, $params){
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