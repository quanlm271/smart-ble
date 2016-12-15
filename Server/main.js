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

// json data to response
var jsonRes = {};
// On query data failed
function OnDbErr (err) {
	console.log(err);
	jsonRes["result"] = 1;
	jsonRes["message"] = "Fail to connect to database";
}
// On incorrect requested json format
function OnDataIncorrect () {
	console.log("Data is incorrect !");
	jsonRes['result'] = 1;
	jsonRes['message'] = "Data is incorrect";
}

// Register
app.post('/register', function(req, res) {
	res.contentType('application/json');
	
	if(!req.body.hasOwnProperty("user_name") || !req.body.hasOwnProperty("email") 
		|| !req.body.hasOwnProperty("password") ) {
		OnDataIncorrect();
		res.send(jsonRes);
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
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		if(Object.keys(result).length == 1) {
			console.log(">> Register: User is existed");
			jsonRes["result"] = 2;
			jsonRes["message"] = "User is existed";
			res.send(jsonRes);
		} else {
			con.query('insert into users set ?', user, function(err,result){
			  if(err) {
				OnDbErr(err);
				res.send(jsonRes);
				return;
			  }

			  console.log('>> Last insert ID: ', result.insertId);
			  jsonRes["result"] = 0;
			  jsonRes["message"] = "Last insert ID: " + result.insertId;
			  res.send(jsonRes);
			});
		}
	});

});

// Login
app.post('/login', function(req, res) {
	res.contentType('application/json');
	
	if(!req.body.hasOwnProperty("email") || !req.body.hasOwnProperty("password") ) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	};
	
	// check if user is existing
	var user = [req.body.email, req.body.password];
	//console.log(">> data: ", user);
	con.query("SELECT * FROM `users` where email = ? and password = ?", user, function (err, result){
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		
		//console.log(">> Result: ", result);
		//console.log(">> Total item: ", Object.keys(result).length);
		if(Object.keys(result).length == 0) {
			jsonRes["result"] = 2;
			jsonRes["message"] = "User is not exist";
			res.send(jsonRes);
		} else {
			//jsonRes["result"] = 0;
			//jsonRes["message"] = JSON.parse(JSON.stringify(result));
			
			// get list device
			con.query("select o.user_type, u.user_id, u.user_name, u.email, l.lock_id, l.mac, l.name, l.status from owners as o INNER JOIN users as u on o.user_id = u.user_id LEFT JOIN `lock` as l on o.lock_id = l.lock_id where u.email = ?", user[0], function (err, result) {
				if(err) {
					OnDbErr(err);
					res.send(jsonRes);
					return;
				}
				console.log(">> Login, Total device: ", Object.keys(result).length);
				jsonRes["result"] = 0;
				jsonRes["message"] = JSON.parse(JSON.stringify(result));
				res.send(jsonRes);
			});
		}
	});
});

var convertHex = require('convert-hex');
app.post ('/converthex', function(req, res) {
	res.contentType('application/json');
	var hexData = req.body.string;
	console.log(">> hex data: ", hexData);
	var bytesData = convertHex.hexToBytes(hexData);
	console.log(">> BytesData: ", bytesData);
	var byteArrayMac = bytesData.splice(16, 6);
	//console.log(">> byte array mac: ", byteArrayMac);
	var mac = byteArray2Mac(byteArrayMac);
	console.log(">> mac: ", mac);
	var byteArrayUserId = bytesData.splice(22, 1);
	console.log(">> byteArrayUserId: ", byteArrayUserId);
	var user_id = convertHex.bytesToHex(byteArrayUserId);
	console.log(user_id);
});

// convert byte array to character array
function dec2String(array) {
  var result = "";
  for (var i = 0; i < array.length; i++) {
    result += String.fromCharCode(array[i]);
  }
  return result;
}

// convert byte array to mac string
function byteArray2Mac (byteArray) {
	var result = "";
	for (var i = 0; i < byteArray.length; i++) {
		result += byteArray[i].toString(16);
		if(i < byteArray.length - 1) {
			result += ":";
		}
	}
	return result;
}

// check if user onws the lock
