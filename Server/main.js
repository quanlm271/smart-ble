var express = require('express');
var bodyParser = require('body-parser')
var app = express();
app.use(bodyParser.json());
var server = app.listen(8081, function () {
  var host = server.address().address;
  var port = server.address().port;
  console.log(">> Example app listening at http://%s:%s", host, port)
})

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
    console.log('>> Error connecting to Db');
    return;
  }
  console.log('>> Connection established');
});

// read config json file
var fs = require('fs');
var configPath = "./Common/config.json";
var jsonConfig;
fs.readFile(configPath, 'utf8', function (err, data) {
	if (err) throw err;
	jsonConfig = JSON.parse(data);
	console.log(">> Read config file successfully");
});

// json data to response
var jsonRes = {};
// On query data failed
function OnDbErr (err) {
	console.log(err);
	jsonRes["result"] = jsonConfig["database_query_failed_code"];
}
// On incorrect requested json format
function OnDataIncorrect () {
	console.log("Data is incorrect !");
	jsonRes['result'] = jsonConfig["incorrect_requested_format_json_code"];
}

// Register
app.post('/register', function(req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
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
			jsonRes["result"] = jsonConfig["user_existing_code"];
			res.send(jsonRes);
		} else {
			con.query('insert into users set ?', user, function(err,result){
			  if(err) {
				OnDbErr(err);
				res.send(jsonRes);
				return;
			  }

			  console.log('>> Register, last inserted ID: ', result.insertId);
			  jsonRes["result"] = jsonConfig["user_register_success_code"];
			  res.send(jsonRes);
			});
		}
	});

});

// Login
app.post('/login', function(req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
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
			console.log(">> Login, User is not existing");
			jsonRes["result"] = jsonConfig["user_not_existing_code"];
			res.send(jsonRes);
		} else {
			//jsonRes["result"] = 0;
			//jsonRes["message"] = JSON.parse(JSON.stringify(result));
			
			// get list device
			//con.query("select o.user_type, u.user_id, u.user_name, u.email, l.lock_id, l.mac, l.name, l.status from owners as o INNER JOIN users as u on o.user_id = u.user_id LEFT JOIN `lock` as l on o.lock_id = l.lock_id where u.email = ?", user[0], function (err, result) {
			//	if(err) {
			//		OnDbErr(err);
			//		res.send(jsonRes);
			//		return;
			//	}
			//	console.log(">> Login, Total device: ", Object.keys(result).length);
			//	jsonRes["result"] = jsonConfig["login_success_code"];
			//	//jsonRes["message"] = JSON.parse(JSON.stringify(result));
			//	res.send(jsonRes);
			//});
			con.query("select user_id from users where email = ?", req.body.email, function (req, result) {
				if(err) {
					OnDbErr(err);
					res.send(jsonRes);
					return;
				}
				console.log(">> Login success");
				jsonRes["result"] = jsonConfig["login_success_code"];
				jsonRes["uid"] = result[0]["user_id"];
				res.send(jsonRes);
			});
		}
	});
});

//add enc
var enc_dec = require('./enc_dec');
var convertHex = require('convert-hex');
app.post ('/converthex', function(req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("phash")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	
	res.contentType('application/json');
	var hexData = req.body.phash;
	console.log(">> hex data: ", hexData);
	var bytesData = convertHex.hexToBytes(hexData);
	console.log(convertHex.bytesToHex(bytesData));
	//console.log(">> BytesData: ", bytesData);
	var byteArrayMac = bytesData.splice(16, 6);
	//console.log(">> byte array mac: ", byteArrayMac);
	var mac = byteArray2Mac(byteArrayMac);
	console.log(">> mac: ", mac);
	var byteArrayUserId = bytesData.splice(16, 2);
	//console.log(">> byteArrayUserId: ", byteArrayUserId);
	var user_id = parseInt(convertHex.bytesToHex(byteArrayUserId), 16);
	console.log(">> UID: ", user_id);
	
	// get lock_id success
	var where = [user_id, mac];
	con.query("SELECT * FROM `owners` as o, `lock` as l WHERE o.lock_id = l.lock_id and  user_id = ? and mac = ?", where, function (err, result) {
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		
		if(Object.keys(result).length == 0) {
			jsonRes["result"] = jsonConfig["user_not_owns_lock"];
			res.send(jsonRes);
			console.log(">> User does not owns the lock");
		} else {
			jsonRes["result"] = jsonConfig["user_owns_lock"];
			//res.send(jsonRes);
			console.log(">> User owns the lock");
			// get pin hex
			con.query("SELECT pin FROM `lock` where mac = ?", mac, function (err, result) {
				if(err) {
					OnDbErr(err);
					res.send(jsonRes);
					return;
					return;
				}
				
				var pinHex = result[0]["pin"];
				var pinHash = enc_dec.create_pin_hash(pinHex);
				var preHash = convertHex.bytesToHex(bytesData);
			});
		}
	});
});

// convert byte array to character array
function byteArray2String(array) {
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
		result += ("0" + byteArray[i].toString(16)).slice(-2);
		if(i < byteArray.length - 1) {
			result += ":";
		}
	}
	return result;
}

app.post ('/LoadDevice', function(req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("uid")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	
	var userId = req.body.uid;
	con.query("select o.user_type, l.lock_id, l.mac, l.name, l.status from owners as o, `lock` as l where o.user_id = ? and o.lock_id = l.lock_id", userId, function (err, result) {
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		var lockData = JSON.parse(JSON.stringify(result));
		console.log(">> Load device, total ", Object.keys(lockData).length);
		jsonRes["result"] = jsonConfig["result_success"];
		jsonRes["data"] = lockData;
		jsonRes["total"] = Object.keys(lockData).length;
		res.send(jsonRes);
	});
});

// Add Device
app.post ('/AddDevice', function(req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("name") || !req.body.hasOwnProperty("name")
		|| !req.body.hasOwnProperty("pin") || !req.body.hasOwnProperty("uid")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	
	var status = req.body.hasOwnProperty("status") ? req.body.status : "inactive";
	var set = [req.body.mac.toUpperCase(), req.body.name, req.body.pin, status];
	con.query("insert into `lock` set mac = ?, name = ?, pin = ?, status = ?", set, function (err, result) {
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		console.log('>> Add Device, last inserted ID: ', result.insertId);
		var type = req.body.hasOwnProperty("type") ? req.body.type : "root";
		set = [req.body.uid, result.insertId, type];
		con.query("insert into `owners` set user_id = ?, lock_id = ?, user_type = ?", set, function (err, result) {
			console.log(">> Add Device, Add Owner success");
			jsonRes["result"] = jsonConfig["result_success"];
			res.send(jsonRes);
		});
	});
});

// Get current owners of lock
app.post ('/GetOwners', function(req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("mac")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	
	con.query("select u.user_id, u.user_name, u.email from owners as o INNER JOIN users as u on o.user_id = u.user_id LEFT JOIN `lock` as l on o.lock_id = l.lock_id where l.mac = ?", req.body.mac, function (err, result) {
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		console.log(">> Get Owners, total: ", Object.keys(result).length);
		jsonRes["result"] = jsonConfig["result_success"];
		jsonRes["data"] = JSON.parse(JSON.stringify(result));
		res.send(jsonRes);
	});
});
