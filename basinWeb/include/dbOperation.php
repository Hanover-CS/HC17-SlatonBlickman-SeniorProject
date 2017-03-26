<?php

 /**
 * Class to query basin database tables. dbOperation can execute any number of queries, but please be aware that it only keeps track of the last query result
 */
class dbOperation
{
    //Database connection link
    private $conn;

    //holds the result of each query
    private $results;
    private $fields;
 
    /**
    * Basic constructor. The connection is passed in instead of constant in case the database location changes.
    * @param $db the PDO object representing the connection to the MYSQL database
    */
    function __construct($db)
    {
        //Initializing our connection link of this class
        //by referring to the PDO object passed
        $this->conn = $db;
    }

    /*
     * Private helper functions
     */ 

    //Pass in the table to get the fields of hte table
    //Might be useful at some point or for debugging
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

    //Unused; can delete
    private function buildSQL($initQuery, $params){
        return "";
    }

    /*
     * Public helper functions
     */

    /**
    * Reset all the tables for testing in db_basin_test
    */
    public function resetTables(){
        //$database = $this->conn->query('select database()')->fetchColumn();
        $database = "no";
        if($database == "db_basin_test"){
            $this->results = true;
        }
        $this->results = false;
    }

    /**
    * Function to determine if the last query was successfully completed
    * @return boolean for succesful query
    */
    public function isSuccessful(){
        if($this->results == false){
            return false;
        }
        else{
            return true;
        }
        
    }

    /**
    * Returns result of last query
    * @return $result Can be different types of values depending on the last query executed
    */
    public function getResults(){
        return $this->results;
    }

    /*
     * Database methods
     */

    //-----------------

    /*
    * BEGIN USERS METHODS
    */

    /**
    * Returns fields of the user tables
    * @return array
    */
    public function getUserFields(){
        $this->fields = $this->getTableFields("users");

        return $this->fields;
    }

    /**
    * Returns all users from users adjusted by the given paramaters
    * @param $params the paramaters to adjust the SQL statement with
    * @return array of associative arrays representing users in the table
    */
    public function getUsers($params){
        $sql = 'SELECT * FROM users';

        $query = $this->conn->prepare($sql);
        $query->execute($params);
        //echo "<br>Selecting all users";
        $this->results = $query->fetchAll();

        return $this->results;
    }

    /**
    * Returns a specific user's information from users table
    * @param $id the id to select from the users table
    * @param $facebook_id boolean indicating whether to select using a facebook id
    * @return associative array of user information
    */
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

    /**
    * Update's a users information in hte users table
    * TODO: limit here the fields that can be updated
    * @param $id the id to select from the users table
    * @param $body the associative array of attributes to update for the user
    * @param $facebook_id boolean indicating whether to select using a facebook id
    * @return boolean indicating success
    */
    public function updateUser($id, $body, $facebook_id){
        $sql = "UPDATE users SET ";

        //create the statement through building the string instead of :somevar statements since we don't know what may be updated
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


    /**
    * Inserts a user into the users table
    * TODO: Would be helpful to return an object for the new user as well
    * @param $body the associative array of attributes to insert for the user
    * @return boolean indicating success
    */
    public function insertUser($body){
        $sql = "INSERT INTO users (facebook_id, fname, lname) VALUES
                (:facebook_id, :fname, :lname)";

        $insert = $this->conn->prepare($sql);
        $this->results = $insert->execute($body);

        return $this->results;
    }

    /**
    * Delete a user into the users table
    * @param $id the id to select from the users table
    * @param $facebook_id boolean indicating whether to select using a facebook id
    * @return boolean indicating success
    */
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

    //--------------------

    /*
    * BEGIN EVENTS METHODS
    */

    /**
    * Returns the fields present in the events table
    * @return array for the fields of events
    */
    public function getEventFields(){
        return $this->getTableFields("events");
    }

    /**
    * Returns all events from the events table
    * @param $params the params to adjust the query by
    * @return array of associative arrays for each event
    */
    public function getEvents($params){
        $sql = 'SELECT * FROM events';

        $query = $this->conn->prepare($sql);
        $query->execute($params);
        //echo "<br>Selecting all users";
        $this->results = $query->fetchAll();

        return $this->results;
    }

    /**
    * Returns a specific event from the events table joined with the user who created it (the coordinator)
    * @param $id the event id to selection from events
    * @return associative array representing the event and its coordinator
    */
    public function getEvent($id){
        $sql = "SELECT events.*, users.fname, users.lname, users.facebook_id
                FROM events 
                INNER JOIN users on users.facebook_id = events.facebook_created_by
                WHERE events._id = ?";

        $query = $this->conn->prepare($sql);
        $query->execute([$id]);
        
        $this->results = $query->fetch();
        
        return $this->results;
    }


    /**
    * Insert an event into the events table
    * @param $body the key-value pairs to insert as event information
    * @return boolean indicating success
    */
    public function insertEvent($body){
        $sql_vals = "INSERT INTO events (facebook_created_by, title, description, lat_coord, long_coord, time_start, date) ";
        $sql_vars = "VALUES ( :facebook_created_by, :title, :description, :lat_coord, :long_coord, :time_start, :date)";

        //ignore previous implementation
        // $sql_vals = "INSERT INTO events (";
        // $sql_vars = ") VALUES (";
        // $i = 0;
        // foreach($body as $key => $value){
        //     $i += 1;
        //     $sql_vals .= $key;
        //     $sql_vars .= ":" . $key;
        //     if($i != count($body)){
        //         $sql_vals .= ", ";
        //         $sql_vars .= ", ";
        //     }

        // }
        // $sql_vars = $sql_vars . ")";
        $sql = $sql_vals . $sql_vars;
        $insert = $this->conn->prepare($sql);
        $this->results = $insert->execute($body);

        return $this->results;
    }

    /**
    * Update an event in the events table
    * @param $id the event id to update
    * @param $body the key-value pairs to update as event information
    * @return boolean indicating success
    */
    public function updateEvent($id, $body){
        $body["id"] = $id;
        $this->getEvent($id);

        $sql = "UPDATE events SET title = :title, facebook_created_by = :facebook_created_by, lat_coord = :lat_coord,
            long_coord = :long_coord, description = :description, time_start = :time_start, date = :date
            WHERE _id = :id";
            
        $query = $this->conn->prepare($sql);
        $this->results = $query->execute($body);

        return $this->results;
    }

    /**
    * Delete an event into the events table
    * @param $id the event id to delete
    * @return boolean indicating success
    */
    public function deleteEvent($id){
        $sql = "DELETE FROM events WHERE _id = ?";
        $delete = $this->conn->prepare($sql);
        $this->results = $delete->execute([$id]);

        return $this->results;
    }

    //-----------------

    /*
    * BEGIN USER<->EVENTS RELATION METHODS
    */

    /**
    * Function for getting the events the user has created and/or attended
    * @param $id the user id whose events are wanted
    * @param $params paramaters to adjust selection; should at least have facebook_id set to true or false
    * @return Associative array with possible keys "created" and "attending" whose values are the corresponding events
    */
    public function getUserEvents($id, $params){
        $results = [];
        $results2 = [];

        if($params["created"] == "true"){
            $results = ["created" => $this->getUserEventsCreated($id, $params)];
        }

        if($params["attending"] == "true"){
            $results2 = ["attending" => $this->getUserEventsAttending($id, $params)];
        }

        $this->results = array_merge($results, $results2);
        
        return $this->results;
    }

    //Helpers for getUserEvents
    private function getUserEventsCreated($id, $params){
        $sql = "SELECT events.*, users.fname, users.lname, users.facebook_id 
                FROM events ";

        if($params["facebook_id"] == "true"){
            $sql .= "INNER JOIN users ON events.facebook_created_by = users.facebook_id WHERE users.facebook_id = ? ";
        }
        else{
            $sql .= "INNER JOIN users ON events.created_by = users._id WHERE users._id = ?";
        }
        //echo $sql;
        $select = $this->conn->prepare($sql);
        $select->execute([$id]);
        $this->results = $select->fetchAll();

        return $this->results;

    }

    private function getUserEventsAttending($id, $params){
        $sql = "SELECT events.*, coordinator.fname, coordinator.lname FROM attendees 
                INNER JOIN events ON attendees.event_id = events._id 
                INNER JOIN users AS attendee ON attendee.facebook_id = attendees.user_id
                INNER JOIN users AS coordinator ON events.facebook_created_by = coordinator.facebook_id ";

        if($params['facebook_id'] == 'true'){
            $sql .= "WHERE attendee.facebook_id = ? ";
        }
        else{
            $sql .= "WHERE attendee._id = ? ";
        }
        //echo $sql;
        $select = $this->conn->prepare($sql);
        $select->execute([$id]);
        $this->results = $select->fetchAll();

        return $this->results;

    }

    //end helpers


    /**
    * Function for getting the users that are attending an event by joining the attendees, events, and users table
    * @param $id the event id whose attendees are wanted
    * @param $params Does nothing. Included for future implementations
    * @return array with user json objects as values for users who are attending the event
    */
    public function getEventAttendance($id, $params){
        $sql = "SELECT users.* FROM attendees 
                INNER JOIN events ON attendees.event_id = events._id 
                INNER JOIN users ON users.facebook_id = attendees.user_id 
                WHERE events._id = ? ";

        //echo $sql;
        $select = $this->conn->prepare($sql);
        $select->execute([$id]);
        $this->results = $select->fetchAll();

        return $this->results;

    }

    /**
    * Function for associating a user as an attendee for an event. If the user is attending already, they will not be inserted. 
    * @param $event_id the event id whose attendees are wanted
    * @param $body the associative array holding the user_id to insert
    * @return boolean indicating success
    */
    public function insertEventAttendee($event_id, $body){
        if(!$this->isAttending($event_id, $body['user_id'])){
            $sql = ("INSERT INTO attendees (event_id, user_id) VALUES (:e_id, :u_id)");

            $vals = ["e_id" => $event_id, "u_id" => $body['user_id']];
            $insert = $this->conn->prepare($sql);
            $this->results = $insert->execute($vals);
        }

        return $this->results;
    }

    /**
    * Function for deleting a user from attending an event
    * @param $event_id the event id whose attendees are wanted
    * @param $user_id the user id to delete
    * @param $params does nothing. Exists for future implementations.
    * @return boolean indicating success
    */
    public function deleteEventAttendee($event_id, $user_id, $params){
        $sql = "DELETE FROM attendees WHERE event_id = :event_id AND user_id = :user_id";

        $vals = ["event_id" => $event_id, "user_id" => $user_id];
        $delete = $this->conn->prepare($sql);
        $this->results = $delete->execute($vals);

        return $this->results;
    }

   /**
    * Function to check if a user is attending an event
    * @param $event_id the id for the event
    * @param $user_id the id for the user
    * @return boolean indicating attendeance by the user
    */
    public function isAttending($event_id, $user_id){
        $sql = "SELECT * FROM attendees 
                WHERE event_id = :event_id AND user_id = :user_id ";

        $vals = ["event_id" => $event_id, "user_id" => $user_id];
        $select = $this->conn->prepare($sql);
        $select->execute($vals);
        $results = $select->fetchAll();
        $this->results = (sizeof($results) > 0);
        return $this->results;
    }

    /**
    * Stub. Always return null. No use for this currently.
    *
    */
    public function getEventsByLocation($lat, $long, $place){
        return null;
    }


}

?>  