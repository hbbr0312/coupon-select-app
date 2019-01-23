/*
 * server.js - Server for CS496 project4 which is coupon book mobile application service.
 *             Connected to MongoDB.
 */

// Assign http module to http variable for server.
var http = require('http');
var url = require('url');
var querystring = require('querystring');

// Use express, body-parser.
const express = require('express');
const app = express();
const bodyParser = require('body-parser');
app.use(bodyParser.json({limit : '50mb'}));
app.use(bodyParser.urlencoded({limit : '50mb', extended : true}));

// Use crypto to encode user passwords.
const crypto = require('crypto');

// Connect this node.js server with MongoDB.
var mongoose = require('mongoose');
mongoose.connect('mongodb://localhost:27017/p4');
var db = mongoose.connection;

db.on('error', function(){
	console.log('Connection Failed.');
});

db.once('open', function(){
	console.log('Connected.');
});

var Schema = mongoose.Schema;

// Server listen for port with port number 80.
app.listen(80, function(){
	console.log('Server is running...');
});


/*******************************
 * Membership handling routines.
 *******************************/

/*
 * GET - Deals with GET method,
 *       which checks whether the IDs entered by users are overlapped.
 *       Parameter : id(String) - User ID entered by user.
 *       Response : '0'/'1' (String) - '0' if there's a overlap, and
 *                                     '1' if there's no overlap.
 */
app.get('/getidvalidity', function(req, res){
	console.log('Get ID validity');
	// Parse parameters.
	var parsedUrl = url.parse(req.url);
	var parsedQuery = querystring.parse(parsedUrl.query, '&', '=');
	
	// Need only one parameter which is user identifier.
	if (Object.keys(parsedQuery).length != 1){
		res.write(' Error: Too many or less number of parameters.');
		res.end();
	}else{
		var id = parsedQuery.id;
		
		var Id = mongoose.model('Schema', new Schema({id : String, pssword : String, name : String, phone : String}), 'client');
		var condition = {'id' : id};
		var get = {'_id' : 0, 'id' : 1};
		
		Id.find(condition, get, function(error, data){
			console.log('Check if there exists overlapping for user ID.');
			if (error){
				console.log(error);
			}else{
				// If there's overlapping, send '0' as a response.
				if (data.toString() != ''){
					res.setHeader('Content-Type', 'text/plain');
					res.write(' 0');
					res.end();
				// If there's no overlapping, send '1' as a response.
				}else{
					res.setHeader('Content-Type', 'text/plain');
					res.write(' 1');
					res.end();
				}
			}
		});
		mongoose.deleteModel('Schema');
	}
});

/*
 * POST - Deals with POST method,
 *        which requests to save members' info.
 *        Parameter : None(Must be).
 *        Request body : id (String) - User ID entered by user.
 *                       password (String) - User Password entered by user.
 *                       name (String) - User name.
 *                       phone (String) - User phone number.
 *        Response : None.
 *        Use crypto to encode user password.
 */
app.post('/postmember', function(req, res){
	console.log('Post member information.');
	// Parse parameters.
	var parsedUrl = url.parse(req.url);
	var parsedQuery = querystring.parse(parsedUrl.query, '&', '=');
	
	// Need no parameter.
	if (Object.keys(parsedQuery).length != 0){
		res.write(' Error: There should be no parameter.');
		res.end();
	}else{
		var id = req.body.id;
		var password = req.body.password;
		var name = req.body.name;
		var phone = req.body.phone;
		var store = req.body.store;
		
		// Encode user password.
		password = crypto.createHash('sha512').update(password).digest('base64');
		
		var Client = mongoose.model('Schema', new Schema({id : String, password : String, name : String, phone : String, store : String}), 'client');
		var newInfo = {'id' : id, 'password' : password, 'name' : name, 'phone' : phone, 'store' : store};
		
		// If users do not check their id validity or do ignore the invalidity,
		// it is filterd at the application side.
		Client.create(newInfo, function(error){
			if (error){
				console.log(error);
			}else{
				console.log('New member joined');
				res.write(' success');
				res.end();
			}
		})
		mongoose.deleteModel('Schema');
	}
});

/*
 * POST - Deals with POST method,
 *        which manages the user LOGIN.
 *        Parameters : None(Must be).
 *        Request body : id (String) - User identifier.
 *                       password (String) - User password.
 *        Response : '-1'/'0'/'1' (String) - '-1' if there's no such ID,
 *                                           '0' if login fails because of password missmatch, and
 *                                           '1' if login successes.
 *        Encode password using crypto and compare with encoded password saved in DB.
 */
app.post('/postlogin', function(req, res){
	console.log('Post login information.');
	// Parse parameters.
	var parsedUrl = url.parse(req.url);
	var parsedQuery = querystring.parse(parsedUrl.query, '&', '=');
	
	// Need no parameter.
	if (Object.keys(parsedQuery).length != 0){
		res.wrtie(' Error: There should be no parameter.');
		res.end();
	}else{
		var id = req.body.id;
		var password = req.body.password;
		
		// Encode user password.
		password = crypto.createHash('sha512').update(password).digest('base64');
		
		var Client = mongoose.model('Schema', new Schema({id : String, password : String, name : String, phone : String, store : String}), 'client');
		var condition1 = {'id' : id};
		var condition2 = {'id' : id, 'password' : password};
		var get = {'_id' : 0, 'password' : 0, '__v' : 0};
		
		// Check if there exists ID match.
		Client.find(condition1, get, function(error, data1){
			console.log('Check if there exists ID match.');
			if (error){
				console.log(error);
			}else{
				// There's no match.
				if (data1.toString() == ''){
					res.setHeader('Content-Type', 'text/plain');
					res.write(' -1');
					res.end();
				// There's ID match.
				}else{
					Client.find(condition2, get, function(error, data2){
						if (error){
							console.log(error);
						}else{
							// There's no ID - password match.
							if (data2.toString() == ''){
								res.setHeader('Content-Type', 'text/plain');
								res.write(' 0');
								res.end();
							// Login success.
							}else{
								res.setHeader('Content-Type', 'text/plain');
								res.write(' ' + data2);
								res.end();
							}
						}
					});
				}
			}
		});
		mongoose.deleteModel('Schema');
	}
});

/*
 * GET - Deals with GET method,
 *       which requests store info.
 *       Parameters : name (String) - Store identifier.
 *       Response : Pair of 'color' and 'logo'.
 *                  (logo is encoded image content.)
 */
app.get('/getstoreinfo', function(req, res){
	console.log('Get store information.');
	// Parse parameters.
	var parsedUrl = url.parse(req.url);
	var parsedQuery = querystring.parse(parsedUrl.query, '&', '=');
	
	// Need only one parameter which is store identifier.
	if (Object.keys(parsedQuery).length != 1){
		res.write('Error: Too many or less number of parameters.');
		res.end();
	}else{
		var storename = parsedQuery.storename;
		
		var Store = mongoose.model('Schema', new Schema({storename : String, color : String, logo : String}), 'store');
		var condition = {'storename' : storename};
		var get = {'_id' : 0, 'storename' : 0};
		
		Store.find(condition, get, function(error, data){
			if (error){
				console.log(error);
			}else{
				res.setHeader('Content-Type', 'text/json');
				res.write(' ' + data.toString());
				res.end();
			}
		});
		mongoose.deleteModel('Schema');	
	}
});

/*
 * POST - Deals with POST method,
 *        which requests to change theme color and logo.
 *        Parameters : None(Must be).
 *        Request body : storename (String) - Store name.
 *                       color (String) - Theme color for coupon.
 *                       logo (String) - Encoded content of logo image.
 *        Response : None.
 */
app.post('/poststoreinfo', function(req, res){
	console.log('Post store information.');
	// Parse parameters.
	var parsedUrl = url.parse(req.url);
	var parsedQuery = querystring.parse(parsedUrl.query, '&', '=');
	
	// Need no parameter.
	if (Object.keys(parsedQuery).length != 0){
		res.write('Error: There should be no parameter.');
		res.end();
	}else{
		var storename = req.body.storename;
		var color = req.body.color;
		var logo = req.body.logo;
		console.log(storename);
		var Store = mongoose.model('Schema', new Schema({storename : String, color : String, logo : String}), 'store');
		var condition = {'storename' : storename};
		var update = {'$set' : {'storename' : storename, 'color' : color, 'logo' : logo}};
		var option = {upsert : true, new : true, useFindAndModify : false};
		
		Store.findOneAndUpdate(condition, update, option, function(error, data){
			if (error){
				console.log(error);
			}else{
				res.setHeader('Content-Type', 'text/json');
				res.write(' ' + data.toString());
				res.end();
			}
		});
		mongoose.deleteModel('Schema');
	}
});

/**************************************
 * End of membership handling routines.
 **************************************/

/***************************
 * Coupon handling routines.
 ***************************/

/*
 * GET - Deals with GET method,
 *       which requests coupon info.
 *       Parameters : id (String) - User identifer.
 *       Response : Pairs of store name, number of coupons, theme color, and logo of that store.
 */
app.get('/getcouponinfo', function(req, res){
	console.log('Get coupon information.');
	// Parse parameters.
	var parsedUrl = url.parse(req.url);
	var parsedQuery = querystring.parse(parsedUrl.query, '&', '=');
	
	// Need only one parameters.
	if (Object.keys(parsedQuery).length != 1){
		res.write(' Error: Too many or less number of parameters.');
		res.end();
	}else{
		var data1;
		var data2;
		
		var id = parsedQuery.id;
		
		var Coupon = mongoose.model('Schema', new Schema({store : String, id : String, num_coupon : String}), 'coupon');
		var condition = {"id" : id};
		var get = {"_id" : 0, "store" : 1, "num_coupon" : 1};
		
		// Get data1: store - num_coupon pairs.
		Coupon.find(condition, get, function(error, data){
			if (error){
				console.log(error);
			}else{
				// User has no coupon at all.
				// Creating entity routine is in POST.
				if (data.toString() == ''){
					var data = [];
					res.setHeader('Content-Type', 'text/json');
					res.write(' ' + data.toString());
					res.end();
					mongoose.deleteModel('Schema');
				}else{
					mongoose.deleteModel('Schema');
					
					// Get data2: color - logo pairs.
					data1 = data;
					data2 = [];
					
					var Store = mongoose.model('Schema', new Schema({storename : String, color : String, logo : String}), 'store');
					
					data1.forEach(function(e){
						condition = {'storename' : e.store};
						get = {'_id' : 0, 'storename' : 0, '__v' : 0};
						
						Store.find(condition, get, function(error, doc){
							if (error){
								console.log(error);
							}else{
								var new_data1 = {};
								var new_data2 = {};
								
								for (var k1 in e){
									new_data1[k1] = e[k1];
								}
								for (var k2 in doc[0]){
									new_data2[k2] = doc[0][k2];
								}
								
								var new_data = Object.assign(new_data1.toObject(), new_data2.toObject());
								data2.push(new_data);
								
								if (data2.length == data1.length){
									res.setHeader('Content-Type', 'text/json');
									res.write(JSON.stringify(data2));
									res.end();
								}
							}
						});
					});
					mongoose.deleteModel('Schema');
				}
			}
		});
	}
});

/*
 * POST - Deals with POST method,
 *        which requests to save number of coupons of user.
 *        Parametes : None(Must be).
 *        Request body : store (String) - Store name.
 *                     : id (String) - User identifier.
 *                     : change (String) - Change of number of coupons.
 *                     : code (String) - '0' if 'change' is positive, and
 *                                       '1' if 'change' is negative.
 *        Response : None.
 */
app.post('/postcouponinfo', function(req, res){
	console.log('Post coupon information.');
	// Parse parameters.
	var parsedUrl = url.parse(req.url);
	var parsedQuery = querystring.parse(parsedUrl.query, '&', '=');
	
	// Need no parameter.
	if (Object.keys(parsedQuery).length != 0){
		res.write(' Error: There should be no parameter.');
		res.end();
	}else{
		var store = req.body.store;
		var id = req.body.id;
		var change = req.body.change;
		var code = req.body.code;
		
		var Coupon = mongoose.model('Schema', new Schema({store : String, id : String, num_coupon : String}), 'coupon');
		var condition = {"store" : store, "id" : id};
		var get = {"_id" : 0, "__v" : 0, "store" : 0, "id" : 0};
		var get_create = {"_id" : 0, "__v" : 0, "store" : 0};
		var option = {upsert : true, new : true, useFindAndModify : false};
		
		Coupon.find(condition, get, function(error, data){
			if (error){
				console.log(error);
			}else{
				// code '0' - Change is positive.
				if (code == '0'){
					// This user is given first coupon for this store.
					// Create entity and save 'change' as num_coupon.
					if (data.toString() == ''){
						console.log('Create new coupon.');
						console.log(store);
						console.log(id);
						console.log(change);
						Coupon.create({"store" : store, "id" : id, "num_coupon" : change}, function(error){
							if (error){
								console.log(error);
							}else{
								Coupon.find(condition, get_create, function(error, newdata){
									if (error){
										console.log(error);
									}else{
										data = newdata;
										res.setHeader('Content-Type', 'text/json');
										res.write(' ' + data.toString());
										res.end();
									}
								});
							}
						});		
					// Thia user already has coupon for this store.
					// Find and update num_coupon.
					}else{
						console.log('Number of coupons increased.');
						var new_num = (Number(data[0].num_coupon) + Number(change)).toString();
						var update = {"$set" : {"num_coupon" : new_num}};
						Coupon.findOneAndUpdate(condition, update, option, function(error, doc){
							if (error){
								console.log(error);
							}else{
								res.setHeader('Content-Type', 'text/json');
								res.write(' ' + doc.toString());
								res.end();
							}
						});
					}
				// code '1' - Change is negative.
				// This user already has coupon for this store.
				// Find and update num_coupon.
				}else if (code == '1'){
					console.log('Number of coupons decreased.');
					var new_num = (Number(data[0].num_coupon) - Number(change)).toString();
					var update = {"$set" : {"num_coupon" : new_num}};
					Coupon.findOneAndUpdate(condition, update, option, function(error, doc){
						if (error){
							console.log(error);
						}else{
							res.setHeader('Content-Type', 'text/json');
							res.write(' ' + doc.toString());
							res.end();
						}
					});
				// Wrong code.
				}else{
					res.write(' Error: Wrong code.');
					res.end();
				}
			}
		});
		mongoose.deleteModel('Schema');
	}
});

/**********************************
 * End of coupon handling routines.
 **********************************/
