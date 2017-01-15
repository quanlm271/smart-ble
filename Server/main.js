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
		
		if(Object.keys(result).length == 0) {
			console.log(">> Login, User is not existing");
			jsonRes["result"] = jsonConfig["user_not_existing_code"];
			res.send(jsonRes);
		} else {
			console.log(">> Login success");
			jsonRes["result"] = jsonConfig["login_success_code"];
			jsonRes["uid"] = result[0]["user_id"];
			jsonRes["user_name"] = result[0]["user_name"];
			res.send(jsonRes);
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
	//console.log(">> BytesData: ", bytesData);
	var byteArrayMac = bytesData.splice(16, 6);
	//console.log(">> byte array mac: ", byteArrayMac);
	var mac = byteArray2Mac(byteArrayMac);
	console.log(">> mac: ", mac);
	var byteArrayUserId = bytesData.splice(16, 2);
	//console.log(">> byteArrayUserId: ", byteArrayUserId);
	var user_id = parseInt(convertHex.bytesToHex(byteArrayUserId), 16);
	console.log(">> UID: ", user_id);
	
	// get pin from mac
	con.query("SELECT * FROM `lock` WHERE mac = ?", mac, function (err, result){
		if(err){
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		
		var lock_id = result[0]["lock_id"];
		var where = [user_id, lock_id];
		con.query("SELECT * FROM `owners` where user_id = ? and lock_id = ?", where, function (err, result){
			if(err){
				OnDbErr(err);
				res.send(jsonRes);
				return;
			}
			
			var pinHex = result[0]["pin"];
			var pinHash = enc_dec.create_pin_hash(pinHex);
			var preHash = convertHex.bytesToHex(bytesData);
			var pinData = enc_dec.getPinData(preHash, pinHash);
			console.log(">> ConvertHex, PinData: ", pinData);
			var jsonPinData = enc_dec.PinDataToJson(pinData);
			if(pinHex == jsonPinData["pin"]) {
				console.log(">> ConvertHex, PIN is correct");
				jsonRes["result"] = jsonConfig["result_success"];
				res.send(jsonRes);
			} else {
				console.log(">> ConvertHex, PIN is not correct");
				jsonRes["result"] = jsonConfig["pin_not_correct_code"];
				res.send(jsonRes);
			}
		});
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
	return result.toUpperCase();
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
	if(!req.body.hasOwnProperty("mac") || !req.body.hasOwnProperty("name")|| !req.body.hasOwnProperty("pin") || !req.body.hasOwnProperty("uid")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	
	// 1. check if lock is in database
	con.query("SELECT * FROM `lock` where mac = ?", req.body.mac.toUpperCase(), function (err, result){
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		
		var status = req.body.hasOwnProperty("status") ? req.body.status : "inactive";
		var type = req.body.hasOwnProperty("type") ? req.body.type : "root";
			
		if(Object.keys(result).length == 0) {
			// 2. if not, insert lock	
			var set = [req.body.mac.toUpperCase(), req.body.name, req.body.pin, status];
			con.query("insert into `lock` set mac = ?, name = ?, pin = ?, status = ?", set, function (err, result) {
				if(err) {
					OnDbErr(err);
					res.send(jsonRes);
					return;
				}
				console.log('>> Add New Device, MAC: ' + req.body.mac + ', NAME = ' + req.body.name);
				set = [req.body.uid, result.insertId, type, req.body.pin];
				con.query("insert into `owners` set user_id = ?, lock_id = ?, user_type = ?, pin = ?", set, function (err, result) {
					console.log(">> Add Owner As Root Successfully");
					jsonRes["result"] = jsonConfig["result_success"];
					res.send(jsonRes);
				});
			});
		} else {
			var lock_id = result[0]["lock_id"];
			// 3. if yes, update name and pin
			var set = [req.body.name, req.body.pin, status, lock_id];
			con.query("update `lock` set name = ?, pin = ?, status = ? where lock_id = ?", set, function (err, result) {
				if(err) {
					OnDbErr(err);
					res.send(jsonRes);
					return;
				}
				console.log('>> Update Device, MAC: ' + req.body.mac + ', NAME = ' + req.body.name);
				set = [req.body.uid, lock_id, type, req.body.pin];
				con.query("insert into `owners` set user_id = ?, lock_id = ?, user_type = ?, pin = ?", set, function (err, result) {
					console.log(">> Add Owner As Root Successfully");
					jsonRes["result"] = jsonConfig["result_success"];
					res.send(jsonRes);
				});
			});
		}
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
	
	con.query("select o.user_type, u.user_id, u.user_name, u.email from owners as o INNER JOIN users as u on o.user_id = u.user_id LEFT JOIN `lock` as l on o.lock_id = l.lock_id where l.mac = ?", req.body.mac, function (err, result) {
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

// API Search User
app.post ('/SearchUser', function(req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("info")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	var like = '%' + req.body.info + '%';
	con.query("SELECT u.user_id, u.user_name, u.email FROM `users` as u WHERE u.email LIKE ? LIMIT 2", like, function (err, result) {
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		console.log(">> Search user, result: ", result);
		jsonRes["result"] = jsonConfig["result_success"];
		jsonRes["data"] = JSON.parse(JSON.stringify(result));
		res.send(jsonRes);
	});
});

// API: Add Owner & Lock
app.post('/AddOwner', function (req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("email") || !req.body.hasOwnProperty("mac") || !req.body.hasOwnProperty("type")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	var where = [req.body.email, req.body.mac];
	con.query("select o.user_type, u.user_id, u.email, l.lock_id, l.mac from owners as o INNER JOIN users as u on o.user_id = u.user_id LEFT JOIN `lock` as l on o.lock_id = l.lock_id where u.email = ? and l.mac = ?", where, function (err, result) {
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		if(Object.keys(result).length > 0) {
			console.log(">> Add Owner failed, Owner owned the lock");
			jsonRes["result"] = jsonConfig["user_owns_lock"];
			res.send(jsonRes);
		} else {
			con.query("SELECT user_id FROM `users` where email = ?", req.body.email, function (err, result) {
				var user_id = result[0]["user_id"];
				con.query("SELECT lock_id FROM `lock` WHERE mac = ?", req.body.mac, function (err, result) {
					var lock_id = result[0]["lock_id"];
					var pin = result[0]["pin"];
					var set = [user_id, lock_id, req.body.type, pin];
					con.query("insert into `owners` set user_id = ?, lock_id = ?, user_type = ?, pin = ?", set, function (err, result) {
						console.log(">> Add Owner success");
						jsonRes["result"] = jsonConfig["result_success"];
						res.send(jsonRes);
					});
				});
			});
		}
	});
});

// API: Edit Owner
app.post('/EditOwner', function (req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("email") || !req.body.hasOwnProperty("mac") || !req.body.hasOwnProperty("type")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	
	con.query("SELECT user_id FROM `users` where email = ?", req.body.email, function (err, result) {
		var user_id = result[0]["user_id"];
		con.query("SELECT lock_id FROM `lock` WHERE mac = ?", req.body.mac, function (err, result) {
			var lock_id = result[0]["lock_id"];
			var set = [req.body.type, user_id, lock_id];
			con.query("update owners set user_type = ? where user_id = ? and lock_id = ?", set, function (err, result) {
				console.log(">> Edit Owner success");
				jsonRes["result"] = jsonConfig["result_success"];
				res.send(jsonRes);
			});
		});
	});
});

// API: Remove Owner
app.post('/RemoveOwner', function (req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("email") || !req.body.hasOwnProperty("mac")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	
	con.query("SELECT user_id FROM `users` where email = ?", req.body.email, function (err, result) {
		var user_id = result[0]["user_id"];
		con.query("SELECT lock_id FROM `lock` WHERE mac = ?", req.body.mac, function (err, result) {
			var lock_id = result[0]["lock_id"];
			var set = [user_id, lock_id];
			con.query("delete from owners WHERE user_id = ? and lock_id = ?", set, function (err, result) {
				console.log(">> Remove Owner success");
				jsonRes["result"] = jsonConfig["result_success"];
				res.send(jsonRes);
			});
		});
	});
});

// API: Remove Owner
app.post('/RemoveAllLockOwner', function (req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("lock_id")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	
	con.query("DELETE from owners where lock_id = ?", req.body.lock_id, function (err, result) {
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}

		console.log(">> Remove all owners success");
				jsonRes["result"] = jsonConfig["result_success"];
				res.send(jsonRes);
	});
});

// API: Check new device
app.post('/CheckNewDevice', function (req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("mac")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	
	con.query("SELECT * FROM `lock` where mac = ?", req.body.mac, function (err, result){
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		
		if(Object.keys(result).length > 0) {
			var lock_id = result[0]["lock_id"];
			var name = result[0]["name"];
			con.query("SELECT * FROM `owners` where lock_id = ?", lock_id, function (err, result){
				if(err) {
					OnDbErr(err);
					res.send(jsonRes);
					return;
				}
				
				if(Object.keys(result).length > 0) {
					console.log(">> Check new device: " + name + " has " + Object.keys(result).length + " owners");
					jsonRes["result"] = jsonConfig["lock_has_owner_code"];
					res.send(jsonRes);
				} else {
					console.log(">> Check new device: " + name + " has no owners");
					jsonRes["result"] = jsonConfig["lock_has_no_owner_code"];
					res.send(jsonRes);
				}
			});
		} else {
			console.log(">> Check new device: " + req.body.mac + " has no owners");
			jsonRes["result"] = jsonConfig["lock_has_no_owner_code"];
			res.send(jsonRes);
		}
	});
});

// API: CheckTypeUser
app.post('/CheckTypeUser', function (req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("user_id") || !req.body.hasOwnProperty("lock_id")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	var where = [req.body.user_id, req.body.lock_id];
	con.query("SELECT * FROM `owners` where user_id = ? and lock_id = ?", where, function (err, result){
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		
		if(Object.keys(result).length > 0) {
			console.log(">> CheckTypeUser: ", result[0]["user_type"]);
			jsonRes["type"] = result[0]["user_type"];
			jsonRes["result"] = jsonConfig["result_success"];
			res.send(jsonRes);
		} else {
			console.log("CheckTypeUser, User does not own lock ");
			jsonRes["result"] = jsonConfig["user_not_owns_lock"];
			res.send(jsonRes);
		}
	});
});

// API: Change PIN
app.post('/ChangePin', function (req, res) {
	res.contentType('application/json');
	
	// check if incorrect requested json format
	if(!req.body.hasOwnProperty("user_id") || !req.body.hasOwnProperty("lock_id") || !req.body.hasOwnProperty("new_pin")) {
		OnDataIncorrect();
		res.send(jsonRes);
		return;
	}
	
	var update = [req.body.new_pin, req.body.user_id, req.body.lock_id];
	con.query("UPDATE owners set pin = ? where user_id = ? and lock_id = ?", update, function(err, result){
		if(err) {
			OnDbErr(err);
			res.send(jsonRes);
			return;
		}
		console.log(">> Change PIN successfully");
		jsonRes["result"] = jsonConfig["result_success"];
		res.send(jsonRes);
	});
});