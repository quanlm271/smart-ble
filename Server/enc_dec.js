var convert= require('convert-hex')
var CryptoJS = require("crypto-js");

module.exports ={
	
// PIN HASH: 38515351395153513551535131515351
// PIN DATA: 00A138393531C4A7C46353536C316543
// ENC: 85B59B6F699BF920D6C1DF1A9674591D
// PreHash: 85B59B6F699BF920D6C1DF1A9674591D
// PHASH: 85B59B6F699BF920D6C1DF1A9674591D88C25512345A0010

	create_pin_hash: function(stringHexPin){
		var bytePinHash = [];
		var bytePIN = convert.hexToBytes(stringHexPin);
		for(var i=0; i<4; i++ ){
            bytePinHash[i*4]=bytePIN[i];
        }
        for(var i=0; i<16; i++){
            if(i%4 != 0){
                if(i%2 != 0)
                    bytePinHash[i] = 0x51; //Q
                else
                    bytePinHash[i] = 0x53; //S
            }
        }
        var result = convert.bytesToHex(bytePinHash)
       	console.log("PIN HASH: ",result);
       	return result;
	},

	//mac, uid in pindata from app
	create_command_hash: function(pinData, userType, masterKey){
		var hexCommandData = '';

		var index = 0;
		//11
		for(var i=0; i<pinData.length/2; i++){
			if( i<2 || i>6)//remove 4 bytes pin + 1 byte random
				hexCommandData += pinData[2*i] + pinData[2*i+1];
		}
		//1
		hexCommandData += userType;
		//4
		hexTime = this.getHexTimeStamp()
		console.log("TIME",hexTime)
		hexCommandData += hexTime;

		var enc = this.aes_enc(hexCommandData, masterKey);
		var cm_data = CryptoJS.enc.Hex.parse(hexCommandData);
		// console.log(cm_data.toString())
		var ms_key = CryptoJS.enc.Hex.parse(masterKey);
		var result = CryptoJS.AES.encrypt(cm_data, ms_key, {mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.ZeroPadding})
		result = CryptoJS.enc.Base64.parse(result.toString())
		console.log("HASH", result.toString())
		return result.toString();
	},
	
	getPinData:function(preHash, pinHash){
		var result = this.aes_dec(preHash,pinHash)
		return result.toString().toUpperCase();
	},

	convert_HexToString:function(hexString){
		var hex = hexString.toString();//force conversion
	    var str = '';
	    for (var i = 0; i < hex.length; i += 2)
	        str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
		return str
	},

	convert_macToBytes:function(mac){
		return convert.hexToBytes(mac.replace(':',''));
	},

	create_random_int:function(range) {
		return Math.floor((Math.random() * range) + 1);
	},

	convert_uidToBytes:function(uid){
		var s = s1 + s2;
		var r = conv.hexToBytes(s);
		console.dir((r[1] & 0xFF) |
		            ((r[0] & 0xFF) << 8));
	},

	getHexTimeStamp(){
		var date = new Date()
		var unix = Math.round(new Date().getTime()/1000)
		var dateByte = this.toBytesInt32(unix)
		var hexDate = convert.bytesToHex(dateByte)
		return hexDate.toString()
	},
	toBytesInt32 (num) {
	    arr = [
	         (num & 0xff000000) >> 24,
	         (num & 0x00ff0000) >> 16,
	         (num & 0x0000ff00) >> 8,
	         (num & 0x000000ff)
	    ];
	    return arr;
	},

	//Input hexstring, output hexstring 16 bytes
	aes_enc:function(mData, mKey){
		var mData = CryptoJS.enc.Hex.parse(mData);
		var mKey = CryptoJS.enc.Hex.parse(mKey);
		var re = CryptoJS.AES.encrypt(mData, mKey, {mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.ZeroPadding})
		re = CryptoJS.enc.Base64.parse(re.toString())
		console.log("ENC:",re.toString(CryptoJS.enc.Hex))
		return re.toString()
	},

	//Input hexstring, output hexstring 16 bytes
	aes_dec:function(mData, mKey){
		var mData = CryptoJS.enc.Hex.parse(mData);
		var base64_Data = mData.toString(CryptoJS.enc.Base64);
		var mKey = CryptoJS.enc.Hex.parse(mKey);
		var re = CryptoJS.AES.decrypt(base64_Data, mKey,  {mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.ZeroPadding})
		console.log("DEC:",re.toString())
		return re.toString();
	},

	// Convert PinData to Json
	PinDataToJson : function (hexPinData) {
		var arrayPinData = convert.hexToBytes(hexPinData);
		var jsonPinData = {};
		jsonPinData["command id"] = convert.bytesToHex(arrayPinData.splice(0, 2));
		jsonPinData["pin"] = convert.bytesToHex(arrayPinData.splice(0, 4));
		jsonPinData["random_byte"] = convert.bytesToHex(arrayPinData.splice(0, 4));
		jsonPinData["session key"] = convert.bytesToHex(arrayPinData.splice(0, 6));
		return jsonPinData;
	}
}