// import module
var mysql = require("mysql");
// create a connection to the db
var con = mysql.createConnection({
  host: "localhost",
  user: "root",
  password: "",
  database: "smartlock"
});
// connect
con.connect(function(err){
  if(err){
    console.log('Error connecting to Db');
    return;
  }
  console.log('Connection established');
});

var express = require('express');
var bodyParser = require('body-parser')
var app = express();
app.use(bodyParser.json());
var server = app.listen(8081, function () {

  var host = server.address().address;
  var port = server.address().port;

  console.log("Example app listening at http://%s:%s", host, port)

})
app.post('/register', function(req, res) {
	res.contentType('application/json');
	var data = {
		result  : 0,
		message : ""
	};
	
	if(!req.body.hasOwnProperty("user_name") || !req.body.hasOwnProperty("email") 
		|| !req.body.hasOwnProperty("password") ) {
		console.log("Data is incorrect !");
		data['result'] = 1;
		data['message'] = "Data is incorrect";
		res.send(data);
		return;
	};
	
	var user = {
	   user_name : req.body.user_name,
	   password : req.body.password,
	   email: req.body.email
	};
	
	// check if user is existing
	con.query("SELECT * FROM `users` where email = ?", user["email"], function (err, result){
		if(err) {
			console.log(err);
			data["result"] = 1;
			data["message"] = "Fail to connect to database";
			res.send(data);
			return;
		}
		if(Object.keys(result).length == 1) {
			console.log(">> Register: User is existed");
			data["result"] = 2;
			data["message"] = "User is existed";
			res.send(data);
		} else {
			con.query('insert into users set ?', user, function(err,result){
			  if(err) {
				console.log(err);
				data["result"] = 1;
				data["message"] = "Fail to connect to database";
				res.send(data);
				return;
			  }

			  console.log('>> Last insert ID: ', result.insertId);
			  data["result"] = 0;
			  data["message"] = "Last insert ID: " + result.insertId;
			  res.send(data);
			});
		}
	});

});

app.post('/login', function(req, res) {
	res.contentType('application/json');
	var data = {
		result  : 0,
		message : ""
	};
	
	if(!req.body.hasOwnProperty("email") || !req.body.hasOwnProperty("password") ) {
		console.log("Data is incorrect !");
		data['result'] = 1;
		data['message'] = "Data is incorrect";
		res.send(data);
		return;
	};
	
	// check if user is existing
	var user = [req.body.email, req.body.password];
	con.query("SELECT * FROM `users` where email = ? and password = ?", user, function (err, result){
		if(err) {
			console.log(err);
			data["result"] = 1;
			data["message"] = "Fail to connect to database";
			res.send(data);
			return;
		}
		
		console.log(">> Result: ", result);
		console.log(">> Total item: ", Object.keys(result).length);
		if(Object.keys(result).length == 0) {
			data["result"] = 2;
			data["message"] = "User is not exist";
		} else {
			data["result"] = 0;
			data["message"] = JSON.parse(JSON.stringify(result));
		}
		res.send(data);
	});
});